// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.tweety;

import com.google.wave.api.ParticipantProfile;
import com.google.wave.api.Wavelet;
import com.google.wave.api.oauth.LoginFormHandler;
import com.google.wave.api.oauth.OAuthService;
import com.google.wave.api.oauth.impl.OAuthServiceException;
import com.google.wave.api.oauth.impl.OAuthServiceImpl;
import com.google.wave.extensions.tweety.model.Tweet;
import com.google.wave.extensions.tweety.util.Util;
import com.google.wave.extensions.tweety.util.WaveSubmittedTweetsCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.cache.CacheException;

/**
 * Helper class to post and fetch data to and from Twitter.
 * 
 * @author mprasetya@google.com (Marcel Prasetya)
 * @author kimwhite@google.com (Kimberly White)
 */
public class TwitterService {

  /**
   * The id to obtain a Twitter user profile image URL from Twitter's JSON
   * response.
   */
  private static final String PROFILE_IMAGE_URL = "profile_image_url";

  /**
   * The id to obtain a Twitter user display name from Twitter's JSON response.
   */
  private static final String PROFILE_NAME = "name";

  /**
   * The id to obtain the list of tweets from Twitter's search JSON response.
   */
  private static final String RESULTS = "results";

  /**
   * The id to obtain the tweet creation time from Twitter's JSON response.
   */
  private static final String TWEET_RESPONSE_CREATED_AT = "created_at";

  /**
   * The id to obtain the tweet author's screen name from Twitter's JSON
   * response.
   */
  private static final String TWEET_RESPONSE_AUTHOR = "screen_name";

  /**
   * The id to obtain the user object from Twitter's JSON response.
   */
  private static final String TWEET_RESPONSE_USER = "user";

  /**
   * The id to obtain the tweet author's screen name from Twitter's JSON
   * response in search mode.
   */
  private static final String SEARCH_TWEET_RESPONSE_AUTHOR = "from_user";

  /**
   * The id to obtain the tweet content from Twitter's JSON response.
   */
  private static final String TWEET_RESPONSE_TEXT = "text";

  /**
   * The id to obtain the tweet id from Twitter's JSON response.
   */
  private static final String TWEET_RESPONSE_ID = "id";

  /**
   * The URL to fetch a user's timeline from Twitter.
   */
  private static final String FETCH_URL = "http://twitter.com/statuses/friends_timeline.json";

  /**
   * The URL to fetch a user's profile from Twitter.
   */
  private static final String PROFILE_URL = "http://twitter.com/users/show.json";

  /**
   * The URL to perform search on Twitter.
   */
  private static final String SEARCH_URL = "http://search.twitter.com/search.json";

  /**
   * The URL to send an update/tweet to Twitter for an authenticated user.
   */
  private static final String UPDATE_URL = "http://twitter.com/statuses/update.json";

  /**
   * Used to authenticate with Twitter using OAuth.
   */
  private OAuthService oauthService;
  
  /**
   * Request token Url.
   */
  public static final String REQUEST_TOKEN_URL = "http://twitter.com/oauth/request_token";

  /**
   * Access token Url.
   */
  public static final String ACCESS_TOKEN_URL = "http://twitter.com/oauth/access_token";

  /**
   * User authorization Url.
   */
  public static final String AUTHORIZE_URL = "http://twitter.com/oauth/authenticate";

  /**
   * The consumer secret used to authenticate using OAuth. Unique to this
   * registered app.
   */
  public static final String CONSUMER_SECRET = "N5oP8zusrBrHjGcBkI7Iv6vKIep9Xe62b2q7WG45ils";

  /**
   * The consumer key used to authenticate using OAuth. Unique to this
   * registered app.
   */
  public static final String CONSUMER_KEY = "FCMWkdHY0cG0jUbep5gxDw";

  /**
   * The date formatter object to convert a tweet creation time to milliseconds
   * since epoch.
   */
  private static final DateFormat TWEET_DATE_FORMATTER =
      new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");

  /**
   * The date formatter object to convert a search response tweet creation time
   * to milliseconds since epoch.
   */
  private static final DateFormat SEARCH_TWEET_DATE_FORMATTER =
      new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

  private static final Logger LOG = Logger.getLogger(TwitterService.class.getName());

  /**
   * Initializes an instance of TwitterService without initializing an OAuthService.
   */
  public TwitterService() {
  }
  
  /**
   * Initializes an instance of TwitterService with an instance of oauthService
   * to handle OAuth post and get requests.
   * 
   * @param authUserId The userId used as a datastore key.
   */
  public TwitterService(String authUserId, String remoteHost) {
    String callbackUrl = "http://" + remoteHost + "/callback.html";
    oauthService = OAuthServiceImpl.newInstance(
        authUserId, CONSUMER_KEY, CONSUMER_SECRET, REQUEST_TOKEN_URL,
        AUTHORIZE_URL, callbackUrl, ACCESS_TOKEN_URL);
  }
  
  /**
   * Verifies that the user profile contains a request token (i.e. user has
   * logged in). If user profile does not exist or does not contain a request
   * token, fetches a request token and renders the login form so the request
   * token can be signed.
   * 
   * If the user profile contains a request token and "confirmed" is true,
   * exchanges the signed request token with the service provider for an access
   * token.
   * 
   * @param wavelet The wavelet on which the robot resides.
   * @param loginForm the form that handles user authorization in wave.
   * @return boolean True if user is authorized, false if rendering a login form 
   *     to authorize the user is required.
   */
  public boolean checkAuthorization(Wavelet wavelet, LoginFormHandler loginForm) {
    return oauthService.checkAuthorization(wavelet, loginForm);
  }
  
  /**
   * Checks if the user is authorized.
   * 
   * @return True if the user has an access token.
   */
  public boolean hasAuthorization(){
    return oauthService.hasAuthorization();
  }
  
  /**
   * Posts a tweet to Twitter.
   * 
   * @param text The content of the tweet.
   * @param inReplyToTweetId The id of an existing tweet that the tweet is in
   *        reply to.
   * @param waveId The id of the wave that generate this tweet.
   * @return A {@link Tweet} object that represents the response from Twitter.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *         response.
   * @throws CacheException If there is a problem saving data into Google App
   *         Engine Memcache.
   */
  public Tweet tweet(String text, String inReplyToTweetId,
      String waveId) throws IOException, JSONException, ParseException, CacheException {
    // Set up query parameters for posting a tweet.
    Map<String, String> queryParameters = new HashMap<String, String>();
    queryParameters.put("status", text);
    LOG.info("Status added to query param: " + queryParameters);
    if (!Util.isEmpty(inReplyToTweetId)) {
      queryParameters.put("in_reply_to_status_id", inReplyToTweetId);
    }

    // Send the tweet to Twitter.
    Tweet tweet = parseTweet(new JSONObject(fetchUrl(UPDATE_URL, queryParameters, true)), false);

    // Keep track of tweet that was originated from Wave.
    new WaveSubmittedTweetsCache().add(waveId, tweet.getId());

    return tweet;
  }

  /**
   * Performs a Twitter search.
   * 
   * @param searchQuery The search query string.
   * @param sinceTweetId Returns tweets with ids greater than the given id.
   * @param waveId The id of the wave that triggers the search.
   * @return A list of {@link Tweet} that match the search term.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *         response.
   * @throws CacheException If there is a problem fetching data from Google App
   *         Engine Memcache.
   */
  public List<Tweet> search(String searchQuery, String sinceTweetId, String waveId)
      throws IOException, JSONException, ParseException, CacheException {
    Map<String, String> queryParameters = new HashMap<String, String>();
    // Limit result to English and 10 entries.'
    queryParameters.put("lang", "en");
    queryParameters.put("rpp", "10");

    // Set the starting tweet id.
    if (!Util.isEmpty(sinceTweetId)) {
      queryParameters.put("&since_id", sinceTweetId);
    }

    // Set the search query.
    queryParameters.put("q", searchQuery);

    // Send the search request to Twitter.
    String jsonString = fetchUrl(SEARCH_URL, queryParameters, false);
    return parseTweets(waveId, jsonString, true);
  }

  /**
   * Fetch a timeline from Twitter for the authenticated user.
   * 
   * @param sinceTweetId Returns tweets with ids greater than the given id.
   * @param waveId The id of the wave that triggers the fetch.
   * @return A list of {@link Tweet}.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *         response.
   * @throws CacheException If there is a problem fetching data from Google App
   *         Engine Memcache.
   */
  public List<Tweet> fetchTimeline(String sinceTweetId, String waveId) throws IOException,
      JSONException, ParseException, CacheException {
    Map<String, String> queryParameters = new HashMap<String, String>();
    if (!Util.isEmpty(sinceTweetId)) {
      queryParameters.put("since_id", sinceTweetId);
    }

    // Fetch the tweets from Twitter.
    String jsonString = fetchUrl(FETCH_URL, queryParameters, false);
    return parseTweets(waveId, jsonString, false);
  }

  /**
   * Fetches the profile of the given screen name.
   * 
   * @param screenName The screen name to query.
   * @return A {@link ParticipantProfile} of the given screen name.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   */
  public ParticipantProfile getProfile(String screenName) throws IOException, JSONException {
    Map<String, String> queryParameters = new HashMap<String, String>();
    queryParameters.put("screen_name", screenName);
    JSONObject profile = new JSONObject(fetchUrl(PROFILE_URL, queryParameters, false));
    return new ParticipantProfile(profile.getString(PROFILE_NAME), profile
        .getString(PROFILE_IMAGE_URL), "http://www.twitter.com/" + screenName);
  }

  /**
   * Helper method to parse the JSON response from Twitter into a list of
   * {@link Tweet} objects. This method will exclude all tweets that were
   * generated from Wave (by calling {@link #tweet(String, String, String)} in
   * the result.
   * 
   * @param waveId The wave that triggers the call to Twitter.
   * @param jsonResponse The JSON response from Twitter.
   * @param isSearch Flag that determines whether this is a search API call or a
   *        timeline API call.
   * @return A list of {@link Tweet}.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *         response.
   * @throws CacheException If there is a problem fetching data from Google App
   *         Engine Memcache.
   */
  private static List<Tweet> parseTweets(String waveId, String jsonResponse, boolean isSearch)
      throws JSONException, ParseException, CacheException {
    List<Tweet> tweets = new ArrayList<Tweet>();
    JSONArray tweetsAsJson =
        isSearch ? new JSONObject(jsonResponse).getJSONArray(RESULTS) : new JSONArray(jsonResponse);

    // Parse the tweets.
    WaveSubmittedTweetsCache cache = new WaveSubmittedTweetsCache();
    for (int i = 0; i < tweetsAsJson.length(); ++i) {
      if (!tweetsAsJson.isNull(i)) {
        // Parse a single tweet.
        Tweet tweet = parseTweet(tweetsAsJson.getJSONObject(i), isSearch);

        // Add the tweet to the result list if it wasn't submitted from Google
        // Wave.
        if (!cache.contains(waveId, tweet.getId())) {
          tweets.add(tweet);
        }
      }
    }
    return tweets;
  }

  /**
   * Sets OAuthService in order to use to send and fetch tweets.
   * 
   * @param oauth {@link OAuthService}
   */
  public void setOauthService(OAuthService oauth) {
    oauthService = oauth;
  }

  /**
   * Helper method to parse a tweet's JSON representation.
   * 
   * @param tweetAsJson A JSON representation of a tweet.
   * @param isSearch Flag that determines whether this is a search tweet or a
   *        timeline tweet.
   * @return A list of {@link Tweet}.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *         response.
   */
  private static Tweet parseTweet(JSONObject tweetAsJson, boolean isSearch) throws JSONException,
      ParseException {
    String id = tweetAsJson.getString(TWEET_RESPONSE_ID);
    String text = tweetAsJson.getString(TWEET_RESPONSE_TEXT);
    String author =
        isSearch ? tweetAsJson.getString(SEARCH_TWEET_RESPONSE_AUTHOR) : tweetAsJson.getJSONObject(
            TWEET_RESPONSE_USER).getString(TWEET_RESPONSE_AUTHOR);
    long createdAt =
        convertTwitterDateTimeIntoLong(tweetAsJson.getString(TWEET_RESPONSE_CREATED_AT), isSearch);
    return new Tweet(id, text, author, createdAt);
  }

  /**
   * Helper method to convert tweet's creation time into a long.
   * 
   * @param dateTime Tweet's creation time.
   * @param isSearch Flag that determines java collection<string, string>whether this is a search tweet or a
   *        timeline tweet.
   * @return The tweet's creation time, since epoch.
   * @throws ParseException If there is a problem parsing the date.
   */
  private static long convertTwitterDateTimeIntoLong(String dateTime, boolean isSearch)
      throws ParseException {
    return isSearch ? SEARCH_TWEET_DATE_FORMATTER.parse(dateTime).getTime() : TWEET_DATE_FORMATTER
        .parse(dateTime).getTime();
  }

  /**
   * Helper method to fetch a URL.
   * 
   * @param urlString The URL to fetch.
   * @param parameters The query parameters to pass in.
   * @param isPost Tells whether the fetcher should use POST or GET method.
   * @return The response from the server.
   * @throws IOException If there is a problem fetching the response.
   */
  private String fetchUrl(String urlString, 
      Map<String, String> parameters, boolean isPost)
      throws IOException {

    // Send the tweet to Twitter.
    String message = null;
    try {
      if (isPost) {
        LOG.info("About to post. Parameters: " + parameters);
        message = oauthService.post(urlString, parameters);
      } else {
        message = oauthService.get(urlString, parameters);
      }
    } catch (OAuthServiceException e) {
      LOG.severe("HTTP message failed: " + e);
    }
    return message;
  }
}
