package com.google.wave.extensions.tweety.controller;

import com.google.wave.extensions.tweety.model.Tweet;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.cache.CacheException;

/**
 * A controller that is responsible for rendering form and fetching data from
 * Twitter.
 * 
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public interface FetchController {

  /**
   * Renders control form.
   */
  void renderForm();

  /**
   * Checks whether the trigger button in the control form is being clicked or
   * not.
   *
   * @return {@code true} if the trigger button in the control form is being
   *     clicked.
   */
  boolean isButtonClicked();

  /**
   * Fetches a list of tweet from Twitter.
   * 
   * @return A list of {@link Tweet}.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *     response.
   * @throws CacheException If there is a problem fetching data from Google
   *     App Engine Memcache.
   */
  List<Tweet> fetch() throws IOException, JSONException, ParseException,
      CacheException;
  
  /**
   * Performs a fetch, triggered by user's interaction with the control form,
   * and return a list of {@link Tweet} based on that input.
   * 
   * @return A list of {@link Tweet}.
   * @throws IOException If there is a problem connecting to Twitter.
   * @throws JSONException If there is a problem parsing the JSON response.
   * @throws ParseException If there is a problem parsing the date in the JSON
   *     response.
   * @throws CacheException If there is a problem fetching data from Google
   *     App Engine Memcache.
   */
  List<Tweet> execute() throws IOException, JSONException, ParseException,
      CacheException;
}
