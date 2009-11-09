package com.google.wave.extensions.tweety;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.Gadget;
import com.google.wave.api.GadgetView;
import com.google.wave.api.Range;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;
import com.google.wave.api.oauth.impl.PopupLoginFormHandler;
import com.google.wave.api.oauth.impl.SingletonPersistenceManagerFactory;
import com.google.wave.extensions.tweety.controller.FetchController;
import com.google.wave.extensions.tweety.controller.SearchController;
import com.google.wave.extensions.tweety.controller.TimelineController;
import com.google.wave.extensions.tweety.model.Tweet;
import com.google.wave.extensions.tweety.model.TwitterWave;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.cache.CacheException;
import javax.jdo.PersistenceManager;

/**
 * The main servlet for Tweety robot. This robot currently supports two mode:
 * <ul>
 * <li>Timeline Mode: Where the robot will fetch and display the user and his
 * friend's timeline.</li>
 * <li>Search Mode: Where the robot will perform a Twitter search based on the
 * given search term.</li>
 * </ul>
 * 
 * @author mprasetya@google.com (Marcel Prasetya)
 * @author kimwhite@google.com (Kimberly White)
 */
public class TweetyServlet extends AbstractRobotServlet {

  /**
   * Key for the popup gadget's state.
   */
  private static final String GADGET_STATE = "popupClosed";

  /**
   * The key of a link annotation.
   */
  private static final String LINK_ANNOTATION_KEY = "link/manual";

  private static final Logger LOG = Logger.getLogger(TweetyServlet.class.getName());

  /**
   * The key of a tweet id annotation.
   */
  private static final String TWEET_ID_ANNOTATION_KEY = "tweetid";
  
  /**
   * The participant id of this robot.
   */
  private static final String TWEETY_ID = "tweety-wave@appspot.com";

  /**
   * A regular expression that matches a URL. The robot uses this regular
   * expression to find and linkify URL.
   */
  private static final String URL_REGEX =
      "(http://|https://)[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?";

  /**
   * Appends a tweet as a blip to the the given {@link Wavelet}.
   * 
   * @param wavelet The {@link Wavelet} to append the tweet to.
   * @param tweet The tweet to be appended.
   */
  private void appendTweet(Wavelet wavelet, Tweet tweet) {
    // Set the metadata: author, creation time, and tweet id annotation.
    TextView textView = wavelet.appendBlip().getDocument();
    textView.setAuthor(tweet.getAuthor() + "@" + getRobotAddress());
    textView.setCreationTime(tweet.getTime());
    textView.setAnnotation(TWEET_ID_ANNOTATION_KEY, tweet.getId());

    String content = tweet.getText();
    textView.insert(0, content);

    // Linkify the content.
    Matcher matcher = Pattern.compile(URL_REGEX).matcher(content);
    while (matcher.find()) {
      textView.setAnnotation(new Range(matcher.start(), matcher.end()), LINK_ANNOTATION_KEY,
          matcher.group());
    }
  }

  /**
   * Appends a list of tweets as blips to the given {@link Wavelet}.
   * 
   * @param wavelet The {@link Wavelet} to append the tweets to.
   * @param tweets The tweets to be appended.
   */
  protected void appendTweets(Wavelet wavelet, List<Tweet> tweets) {
    for (int i = tweets.size() - 1; i >= 0; --i) {
      appendTweet(wavelet, tweets.get(i));
    }
  }

  @Override
  protected String getRobotAddress() {
    return "tweety-wave.appspot.com";
  }

  @Override
  public void processEvents(RobotMessageBundle robotMessageBundle) {

    final Wavelet wavelet = robotMessageBundle.getWavelet();
    final Blip rootBlip = wavelet.getRootBlip();
    // Construct an oauthService with the user key and Twitter OAuth info.
    final String authUserId = wavelet.getCreator() + "@" + wavelet.getWaveId();
    final TwitterService twitterService = new TwitterService(authUserId, getRobotAddress());
    final PopupLoginFormHandler loginForm = new PopupLoginFormHandler(getRobotAddress());
        
    // Handle the flow when Tweety was just added to a new wave.
    if (robotMessageBundle.wasSelfAdded()) {
      rootBlip.getDocument().setAuthor(TWEETY_ID);
      wavelet.setTitle("Sign into Twitter");

      // Checks if the user is authorized with the service provider.
      // If not, fetches the request token and authorizes it.
      if (!twitterService.checkAuthorization(wavelet, loginForm)) {
        LOG.info("Request token fetched and authorized");
        return;
      }
    }
    
    PersistenceManager pm = SingletonPersistenceManagerFactory.get().getPersistenceManager();
    TwitterWave twitterWave =
        TwitterWave.getTwitterWave(pm, wavelet.getWaveId(), wavelet.getWaveletId(), wavelet
            .getCreator());

    try {
      // When user logs in via the popup, finish OAuth by exchanging
      // request token for an access token.
      for (Event event : robotMessageBundle.getEvents()) {
        if (event.getType() == EventType.DOCUMENT_CHANGED) {
          // Get all the gadgets in the wave (typically only be one).
          TextView document = wavelet.getRootBlip().getDocument();
          GadgetView gadgetView = document.getGadgetView();
          for (Gadget gadget : gadgetView.getGadgets()) {
            if ((gadget != null) && ("true".equals(gadget.getField(GADGET_STATE)))) {
              if (twitterService.checkAuthorization(wavelet, loginForm)) {
                FetchController controller =
                    twitterWave.isInSearchMode() ? new SearchController(twitterService, rootBlip,
                        twitterWave, robotMessageBundle.getEvents()) : new TimelineController(
                        twitterService, rootBlip, twitterWave, robotMessageBundle.getEvents());

                // Fetch and append tweets.
                appendTweets(wavelet, controller.fetch());

                // Clear the login form and render the search or update form.
                controller.renderForm();
              }
            }
          }
        } 
        
        if (event.getType() == EventType.FORM_BUTTON_CLICKED || 
            event.getType() == EventType.BLIP_SUBMITTED) {
          // Handle the normal flow after logging in.
          FetchController controller =
              twitterWave.isInSearchMode() ? new SearchController(twitterService, rootBlip,
                  twitterWave, robotMessageBundle.getEvents()) : new TimelineController(
                  twitterService, rootBlip, twitterWave, robotMessageBundle.getEvents());

          // Fetch tweets when search or update button is clicked.
          if (controller.isButtonClicked()) {
            appendTweets(wavelet, controller.execute());
          }

          // Handle blip submitted events, that are considered as
          // @replies.
          for (Event blipSubmittedEvent : robotMessageBundle.getBlipSubmittedEvents()) {
            tweetBlipContent(twitterService, blipSubmittedEvent.getBlip(), twitterWave);
          }
        }
      }
    } catch (IOException e) {
      LOG.warning("There is a problem connecting to Twitter. Cause:" + e.getMessage());
    } catch (JSONException e) {
      LOG.warning("There is a problem parsing response from Twitter. Cause:" + e.getMessage());
    } catch (ParseException e) {
      LOG.warning("There is a problem parsing datetime response from Twitter. Cause:"
          + e.getMessage());
    } catch (CacheException e) {
      LOG.warning("There is a problem parsing response from Twitter. Cause:" + e.getMessage());
    }

    pm.makePersistent(twitterWave);
    pm.close();
  }

  /**
   * Tweets the given blip.
   * 
   * @param blip The blip which content will be submitted to Twitter.
   * @param twitterWave The Twitter Wave that submitted the blip.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *         response.
   * @throws CacheException If there is a problem fetching data from Google App
   *         Engine Memcache.
   */
  private void tweetBlipContent(TwitterService service, Blip blip, TwitterWave twitterWave)
      throws CacheException, IOException, JSONException, ParseException {
    // Don't tweet changes to the root blip.
    if (blip.getBlipId() != null && blip.getBlipId().equals(blip.getWavelet().getRootBlipId())) {
      return;
    }

    String content = blip.getDocument().getText();

    // If the parent blip is a tweet, set the parent tweet id and prepend
    // the content with @<author>.
    Blip parent = blip.getParent();
    String parentTweetId = null;
    List<Annotation> annotations = parent.getDocument().getAnnotations(TWEET_ID_ANNOTATION_KEY);
    if (!annotations.isEmpty()) {
      parentTweetId = annotations.get(0).getValue();
      String author = parent.getDocument().getAuthor();
      if (author != null) {
        content = "@" + author.substring(0, author.indexOf("@")) + " " + content;
      }
    }

    // Submit the tweet to Twitter.
    service.tweet(content, parentTweetId, blip.getWavelet().getWaveId());
  }
}
