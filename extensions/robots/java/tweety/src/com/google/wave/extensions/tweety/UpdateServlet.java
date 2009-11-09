package com.google.wave.extensions.tweety;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.Wavelet;
import com.google.wave.api.oauth.impl.SingletonPersistenceManagerFactory;
import com.google.wave.extensions.tweety.controller.FetchController;
import com.google.wave.extensions.tweety.controller.SearchController;
import com.google.wave.extensions.tweety.controller.TimelineController;
import com.google.wave.extensions.tweety.model.Tweet;
import com.google.wave.extensions.tweety.model.TwitterWave;
import com.google.wave.extensions.tweety.util.Util;
import com.google.wave.extensions.tweety.util.WaveSubmittedTweetsCache;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

import javax.cache.CacheException;
import javax.jdo.PersistenceManager;

/**
 * The servlet that is responsible for updating the Twitter Wave periodically
 * with new tweets that are posted to Twitter. The timer is specified in
 * {@code capabilities.xml}.
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 * @author kimwhite@google.com (Kimberly White)
 */
public class UpdateServlet extends TweetyServlet {

  private static final Logger LOG = Logger.getLogger(UpdateServlet.class.getName());
  
  /**
   * Helper class to post and fetch data to and from Twitter.
   */
  private TwitterService twitterService;
  
  @SuppressWarnings("unchecked")
  @Override
  public void processEvents(RobotMessageBundle robotMessageBundle) {
    
    int attempt = 0;
    while (attempt++ < 3) {
      try {
        PersistenceManager pm = SingletonPersistenceManagerFactory.get().getPersistenceManager();
        List<TwitterWave> twitterWaves =
            (List<TwitterWave>) pm.newQuery(TwitterWave.class).execute();
        for (TwitterWave twitterWave : twitterWaves) {
          
          // Construct an oauthService with the user key and Twitter OAuth info.
          String authUserId = twitterWave.getCreator() + "@" + twitterWave.getWaveId();
          TwitterService twitterService = new TwitterService(authUserId, getRobotAddress());
          
          if (twitterService.hasAuthorization()) {
            fetchTweetsForWave(robotMessageBundle, twitterWave);
          }
          pm.makePersistent(twitterWave);
        }
        pm.close();
        break;
      } catch (DatastoreTimeoutException e) {
        // Do nothing.
      }
    }
  }

  /**
   * Helper method to fetch a list of tweets for the given Twitter Wave.
   *
   * @param robotMessageBundle The message bundle that was received from Google
   *     Wave, that contains the request context.
   * @param twitterWave The Twitter Wave that we want to fetch the tweet for.
   */
  private void fetchTweetsForWave(RobotMessageBundle robotMessageBundle, TwitterWave twitterWave) {
    // Create a wavelet object for this Twitter Wave.
    Wavelet wavelet = robotMessageBundle.getWavelet(twitterWave.getWaveId(),
        twitterWave.getWaveletId());

    FetchController controller = twitterWave.isInSearchMode() ?
        new SearchController(twitterService, wavelet.getRootBlip(), 
            twitterWave, robotMessageBundle.getEvents()):
        new TimelineController(twitterService, wavelet.getRootBlip(), 
            twitterWave, robotMessageBundle.getEvents());

    try {
      // Fetch new tweets from Twitter.
      List<Tweet> newTweets = controller.fetch();
      appendTweets(wavelet, newTweets);

      // Update the latest tweet id, and purge the cache.
      WaveSubmittedTweetsCache cache = new WaveSubmittedTweetsCache();
      if (!newTweets.isEmpty()) {
        twitterWave.setLatestTweetId(newTweets.get(0).getId());
        cache.purge(twitterWave.getWaveId(), twitterWave.getLatestTweetId());
      } else {
        String lastTweetSubmitted = cache.last(twitterWave.getWaveId());
        if (!Util.isEmpty(lastTweetSubmitted) &&
            lastTweetSubmitted.compareTo(twitterWave.getLatestTweetId()) > 0) {
          twitterWave.setLatestTweetId(lastTweetSubmitted);
          cache.purge(twitterWave.getWaveId(), lastTweetSubmitted);
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
  }
}
