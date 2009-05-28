// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.twitter.tweety;

import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.wave.api.ParticipantProfile;
import com.google.wave.extensions.twitter.tweety.model.Tweet;
import com.google.wave.extensions.twitter.tweety.util.Util;
import com.google.wave.extensions.twitter.tweety.util.WaveSubmittedTweetsCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.cache.CacheException;

/**
 * Helper class to post and fetch data to and from Twitter.
 *
 * @author mprasetya@google.com (Marcel Prasetya)
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
   * The constant to set in {@code HttpUrlConnection} object to use HTTP POST
   * method.
   */
  private static final String HTTP_POST = "POST";
  
  
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

  /**
   * Posts a tweet to Twitter.
   *
   * @param username The username to post this tweet on behalf of.
   * @param password The password to authenticate the request against Twitter.
   * @param text The content of the tweet.
   * @param inReplyToTweetId The id of an existing tweet that the tweet is in
   *     reply to.
   * @param waveId The id of the wave that generate this tweet.
   * @return A {@link Tweet} object that represents the response from Twitter.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *     response.
   * @throws CacheException If there is a problem saving data into Google App
   *     Engine Memcache.
   */
  public static Tweet tweet(
      String username,
      String password,
      String text,
      String inReplyToTweetId,
      String waveId) throws IOException, JSONException, ParseException, CacheException {
    // Set up query parameters for posting a tweet.
    String queryParameters = "status=" + URLEncoder.encode(text, "UTF-8");
    if (!Util.isEmpty(inReplyToTweetId)) {
      queryParameters += "&in_reply_to_status_id=" + URLEncoder.encode(inReplyToTweetId, "UTF-8");
    }

    // Send the tweet to Twitter.
    Tweet tweet = parseTweet(
        new JSONObject(fetchUrl(UPDATE_URL, queryParameters, true, username,password)),
        false);

    // Keep track of tweet that was originated from Wave.
    new WaveSubmittedTweetsCache().add(waveId, tweet.getId());

    return tweet;
  }

  /**
   * Performs a Twitter search.
   * 
   * @param username The username to perform this search on behalf of.
   * @param password The password to authenticate the request against Twitter.
   * @param searchQuery The search query string.
   * @param sinceTweetId Returns tweets with ids greater than the given id.
   * @param waveId The id of the wave that triggers the search.
   * @return A list of {@link Tweet} that match the search term.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *     response.
   * @throws CacheException If there is a problem fetching data from Google
   *     App Engine Memcache.
   */
  public static List<Tweet> search(
      String username,
      String password,
      String searchQuery,
      String sinceTweetId,
      String waveId) throws IOException, JSONException, ParseException, CacheException {
    // Limit result to English and 10 entries.
    String queryParameters = "lang=en";
    queryParameters += "&rpp=10";

    // Set the starting tweet id.
    if (!Util.isEmpty(sinceTweetId)) {
      queryParameters += "&since_id=" + sinceTweetId;
    }

    // Set the search query.
    queryParameters += "&q=" + URLEncoder.encode(searchQuery, "UTF-8");

    // Send the search request to Twitter.
    String jsonString = fetchUrl(SEARCH_URL, queryParameters, false, username, password);
    return parseTweets(waveId, jsonString, true);
  }

  /**
   * Fetch a timeline from Twitter for the authenticated user.
   * 
   * @param username The username that we want to fetch the tweets of.
   * @param password The password to authenticate the request against Twitter.
   * @param sinceTweetId Returns tweets with ids greater than the given id.
   * @param waveId The id of the wave that triggers the fetch.
   * @return A list of {@link Tweet}.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *     response.
   * @throws CacheException If there is a problem fetching data from Google App
   *     Engine Memcache.
   */
  public static List<Tweet> fetchTimeline(
      String username,
      String password,
      String sinceTweetId,
      String waveId) throws IOException, JSONException, ParseException, CacheException {
    String queryParameters = null;
    if (!Util.isEmpty(sinceTweetId)) {
      queryParameters = "since_id=" + sinceTweetId;
    }

    // Fetch the tweets from Twitter.
    String jsonString = fetchUrl(FETCH_URL, queryParameters, false, username, password);
    return parseTweets(
        waveId,
        jsonString,
        false);
  }

  /**
   * Fetches the profile of the given screen name.
   *
   * @param screenName The screen name to query.
   * @return A {@link ParticipantProfile} of the given screen name.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   */
  public static ParticipantProfile getProfile(String screenName)
      throws IOException, JSONException {
    String queryParameters = "screen_name=" + screenName;
    JSONObject profile = new JSONObject(fetchUrl(PROFILE_URL, queryParameters, false, null, null));
    return new ParticipantProfile(
        profile.getString(PROFILE_NAME),
        profile.getString(PROFILE_IMAGE_URL),
        "http://www.twitter.com/" + screenName);
  }

  /**
   * Helper method to set HTTP Basic authorization header in the request.
   *
   * @param connection The {@link HttpURLConnection} object to set the
   *     authorization header in.
   * @param username The Twitter username to set in the authorization header.
   * @param password the Twitter password to set in the authorization header.
   */
  private static void setAuthorizationHeader(
      HttpURLConnection connection,
      String username,
      String password) {
    String authString = "Basic " + Base64.encode((username + ":" + password).getBytes());
    connection.setRequestProperty("Authorization", authString);
  }

  /**
   * Helper method to parse the JSON response from Twitter into a list of
   * {@link Tweet} objects. This method will exclude all tweets that were
   * generated from Wave (by calling {@link #tweet(String, String, String,
   * String, String)} in the result.
   *
   * @param waveId The wave that triggers the call to Twitter.
   * @param jsonResponse The JSON response from Twitter.
   * @param isSearch Flag that determines whether this is a search API call or
   *     a timeline API call.
   * @return A list of {@link Tweet}.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *     response.
   * @throws CacheException If there is a problem fetching data from Google
   *     App Engine Memcache.
   */
  private static List<Tweet> parseTweets(String waveId, String jsonResponse, boolean isSearch)
      throws JSONException, ParseException, CacheException {
    List<Tweet> tweets = new ArrayList<Tweet>();
    JSONArray tweetsAsJson = isSearch ?
        new JSONObject(jsonResponse).getJSONArray(RESULTS) :
        new JSONArray(jsonResponse);

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
   * Helper method to parse a tweet's JSON representation.
   *
   * @param tweetAsJson A JSON representation of a tweet.
   * @param isSearch Flag that determines whether this is a search tweet or
   *     a timeline tweet.
   * @return A list of {@link Tweet}.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *     response.
   */
  private static Tweet parseTweet(JSONObject tweetAsJson, boolean isSearch)
      throws JSONException, ParseException {
    String id = tweetAsJson.getString(TWEET_RESPONSE_ID);
    String text = tweetAsJson.getString(TWEET_RESPONSE_TEXT);
    String author = isSearch ?
        tweetAsJson.getString(SEARCH_TWEET_RESPONSE_AUTHOR) :
        tweetAsJson.getJSONObject(TWEET_RESPONSE_USER).getString(TWEET_RESPONSE_AUTHOR);
    long createdAt = convertTwitterDateTimeIntoLong(
        tweetAsJson.getString(TWEET_RESPONSE_CREATED_AT), isSearch);
    return new Tweet(id, text, author, createdAt);
  }

  /**
   * Helper method to convert tweet's creation time into a long.
   *
   * @param dateTime Tweet's creation time.
   * @param isSearch Flag that determines whether this is a search tweet or
   *     a timeline tweet.
   * @return The tweet's creation time, since epoch.
   * @throws ParseException If there is a problem parsing the date.
   */
  private static long convertTwitterDateTimeIntoLong(String dateTime, boolean isSearch)
      throws ParseException {
    return isSearch ?
        SEARCH_TWEET_DATE_FORMATTER.parse(dateTime).getTime() :
        TWEET_DATE_FORMATTER.parse(dateTime).getTime();
  }

  /**
   * Helper method to fetch a URL.
   *
   * @param urlString The URL to fetch.
   * @param parameters The query parameters to pass in.
   * @param isPost Whether the fetcher should use POST or GET method.
   * @param username The username to be set in the authorization header.
   * @param password the password to be set in the authorization header.
   * @return The response from the server.
   * @throws IOException If there is a problem fetching the response.
   */
  private static String fetchUrl(
      String urlString,
      String parameters,
      boolean isPost,
      String username,
      String password) throws IOException {
    // Initialize query parameters.
    if (!isPost && !Util.isEmpty(parameters)) {
      urlString += "?" + parameters;
    }

    // Initialize connection.
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setUseCaches(false);
    
    // Set authorization header.
    if (!Util.isEmpty(username) && !Util.isEmpty(password)) {
      setAuthorizationHeader(connection, username, password);
    }

    // Handle POST request.
    if (isPost) {
      connection.setRequestMethod(HTTP_POST);

      // Set query parameters.
      if (!Util.isEmpty(parameters)) {
        connection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(parameters);
        writer.close();
      }
    }

    // Read the response.
    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder result = new StringBuilder();
    try {
      String inputLine;
      while ((inputLine = reader.readLine()) != null) {
        result.append(inputLine);
      }
    } finally {
      reader.close();
    }
    connection.disconnect();
    return result.toString();
  }
}
