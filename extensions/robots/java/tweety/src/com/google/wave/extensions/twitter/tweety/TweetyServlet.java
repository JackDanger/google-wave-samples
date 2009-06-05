// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.twitter.tweety;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.Range;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;
import com.google.wave.extensions.twitter.tweety.controller.FetchController;
import com.google.wave.extensions.twitter.tweety.controller.LoginController;
import com.google.wave.extensions.twitter.tweety.controller.SearchController;
import com.google.wave.extensions.twitter.tweety.controller.TimelineController;
import com.google.wave.extensions.twitter.tweety.model.Tweet;
import com.google.wave.extensions.twitter.tweety.model.TwitterWave;
import com.google.wave.extensions.twitter.tweety.util.PersistenceManagerHelper;

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
 *     friend's timeline.</li>
 * <li>Search Mode: Where the robot will perform a Twitter search based on the
 *     given search term.</li>
 * </ul>
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public class TweetyServlet extends AbstractRobotServlet {

  /**
   * A regular expression that matches the @reply token. The robot uses this
   * regular expression to strip out @reply from tweet.
   */
  private static final String AT_REPLY_REGEX = "@[^\\s]* ";
  
  /**
   * A regular expression that matches a URL. The robot uses this regular
   * expression to find and linkify URL.
   */
  private static final String URL_REGEX =
    "(http://|https://)[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?";

  /**
   * The key of a link annotation.
   */
  private static final String LINK_ANNOTATION_KEY = "link/manual";
  
  /**
   * The key of a tweet id annotation.
   */
  private static final String TWEET_ID_ANNOTATION_KEY = "tweetid";

  /**
   * The participant id of this robot.
   */
  private static final String TWEETY_ID = "tweety-wave@appspot.com";

  private static final Logger LOG = Logger.getLogger(TweetyServlet.class.getName());

  @Override
  public void processEvents(RobotMessageBundle robotMessageBundle) {
    Wavelet wavelet = robotMessageBundle.getWavelet();
    Blip rootBlip = wavelet.getRootBlip();

    // Handle the flow when Tweety was just added to a new wave.
    if (robotMessageBundle.wasSelfAdded()) {
      rootBlip.getDocument().setAuthor(TWEETY_ID);
      wavelet.setTitle("Sign into Twitter");
      LoginController loginBlip = new LoginController(wavelet.getRootBlip());
      loginBlip.renderForm();
      return;
    }

    PersistenceManager pm = PersistenceManagerHelper.getPersistenceManager();
    TwitterWave twitterWave = TwitterWave.getTwitterWave(pm, wavelet.getWaveId(),
        wavelet.getWaveletId());

    try {
      // Handle the login flow.
      if (!twitterWave.isLoggedIn()) {
        LoginController loginController = new LoginController(wavelet.getRootBlip());
        if (loginController.isButtonClicked()) {
          loginController.populateTwitterWave(twitterWave);

          FetchController controller = twitterWave.isInSearchMode() ?
              new SearchController(rootBlip, twitterWave) :
              new TimelineController(rootBlip, twitterWave);

          // Fetch and append tweets.
          appendTweets(wavelet, controller.fetch());

          // Clear the login form and render the search or update form.
          controller.renderForm();
        }
      } else {
        // Handle the normal flow after logging in.
        FetchController controller = twitterWave.isInSearchMode() ?
            new SearchController(rootBlip, twitterWave) :
            new TimelineController(rootBlip, twitterWave);

        // Fetch tweets when search or update button is clicked.
        if (controller.isButtonClicked()) {
          appendTweets(wavelet, controller.execute());
        }

        // Handle blip submitted events, that are considered as @replies.
        for (Event blipSubmittedEvent : robotMessageBundle.getBlipSubmittedEvents()) {
          tweetBlipContent(blipSubmittedEvent.getBlip(), twitterWave);
        }
      }
    } catch (IOException e) {
      LOG.warning("There is a problem connecting to Twitter. Cause:" + e.getMessage());
    } catch (JSONException e) {
      LOG.warning("There is a problem parsing response from Twitter. Cause:" + e.getMessage());
    } catch (ParseException e) {
      LOG.warning("There is a problem parsing datetime response from Twitter. Cause:" +
          e.getMessage());
    } catch (CacheException e) {
      LOG.warning("There is a problem parsing response from Twitter. Cause:" + e.getMessage());
    }
    pm.makePersistent(twitterWave);
    pm.close();
  }

  @Override
  protected String getRobotAddress() {
    return "tweety-wave.appspot.com";
  }

  /**
   * Appends a list of tweets as blips to the given {@link Wavelet}.
   *
   * @param wavelet The {@link Wavelet} to append the tweets to.
   * @param tweets  The tweets to be appended.
   */
  protected void appendTweets(Wavelet wavelet, List<Tweet> tweets) {
    for (int i = tweets.size() - 1; i >= 0; --i) {
      appendTweet(wavelet, tweets.get(i));
    }
  }

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

    // Remove @user, in the case of @reply tweet.
    String content = tweet.getText().replaceFirst(AT_REPLY_REGEX, "");
    textView.insert(0, content);

    // Linkify the content.
    Matcher matcher = Pattern.compile(URL_REGEX).matcher(content);
    while (matcher.find()) {
      textView.setAnnotation(new Range(matcher.start(), matcher.end()),
          LINK_ANNOTATION_KEY, matcher.group());
    }
  }

  /**
   * Tweets the given blip.
   *
   * @param blip The blip which content will be submitted to Twitter.
   * @param twitterWave The Twitter Wave that submitted the blip.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *     response.
   * @throws CacheException If there is a problem fetching data from Google
   *     App Engine Memcache.
   */
  private void tweetBlipContent(Blip blip, TwitterWave twitterWave)
      throws CacheException, IOException, JSONException, ParseException {
    // Don't tweet changes to the root blip.
    if (blip.getBlipId() != null &&
        blip.getBlipId().equals(blip.getWavelet().getRootBlipId())) {
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
    TwitterService.tweet(twitterWave.getUsername(), twitterWave.getPassword(), content,
        parentTweetId, blip.getWavelet().getWaveId());
  }
}
