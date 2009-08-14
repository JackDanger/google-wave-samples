// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.tweety.controller;

import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.Event;
import com.google.wave.api.FormElement;
import com.google.wave.api.StyleType;
import com.google.wave.api.StyledText;
import com.google.wave.api.TextView;
import com.google.wave.extensions.tweety.TwitterService;
import com.google.wave.extensions.tweety.model.Tweet;
import com.google.wave.extensions.tweety.model.TwitterWave;
import com.google.wave.extensions.tweety.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.cache.CacheException;

/**
 * A controller for Twitter Search mode. It is responsible for rendering the
 * search form on a blip, and fetching search results from Twitter.
 * 
 * This controller renders the search form in the following format:
 * <pre>
 * [username]'s Twitter Search
 * [Input box to enter the search term]
 * [Button to execute the search]
 * </pre>
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 * @author kimwhite@google.com (Kimberly White)
 */
public class SearchController implements FetchController {

  /**
   * The element id of the search input box.
   */
  private static final String SEARCH_INPUT_ID = "search_input";

  /**
   * The element id of the search button.
   */
  private static final String SEARCH_BUTTON_ID = "search_button";
  
  /**
   * The caption of the search button.
   */
  private static final String SEARCH_BUTTON_CAPTION = "Search Twitter";

  
  /**
   * The blip to render the search form.
   */
  private Blip blip;
  
  /**
   * The Twitter Wave where this controller resides.
   */
  private TwitterWave twitterWave;
  
  /**
   * A list of events received from Google Wave.
   */
  private List<Event> events;
  
  /**
   * Helper class to post to and fetch data from Twitter.
   */
  private TwitterService twitterService;
  
  /**
   * Constructs a search controller given a {@link Blip} and a
   * {@link TwitterWave}
   * 
   * @param service Posts to and fetches data from Twitter.
   * @param blip The blip to render the search form.
   * @param twitterWave The Twitter Wave where this controller resides.
   * @param events A list of events received from Google Wave.
   */
  public SearchController(TwitterService service, Blip blip, TwitterWave twitterWave, List<Event> events) {
   twitterService = service;
   this.blip = blip;
   this.twitterWave = twitterWave;
   this.events = events;
  }

  @Override
  public void renderForm() {
    // Clear out everything.
    TextView document = blip.getDocument();
    document.delete();

    // TODO: Decide if want to use former code and, if so, how to get username.
//    // Former code:
//    String username = twitterWave.getUsername();
//    blip.getWavelet().setTitle(
//        new StyledText((username + (username.endsWith("s") ? "'" : "'s") + " Twitter Search"),
//            StyleType.HEADING3));
    
    // Set the new title.
    blip.getWavelet().setTitle(new StyledText("Twitter Search", StyleType.HEADING3));

    // Insert the search input box.
    document.append("\n");
    document.appendElement(new FormElement(
        ElementType.INPUT,
        SEARCH_INPUT_ID,
        twitterWave.getSearchQuery()));

    // Append the search button.
    document.append("\n");
    document.appendElement(new FormElement(
        ElementType.BUTTON,
        SEARCH_BUTTON_ID,
        SEARCH_BUTTON_CAPTION));
  }

  @Override
  public boolean isButtonClicked() {
    return Util.isButtonClicked(events, SEARCH_BUTTON_ID);
  }

  @Override
  public List<Tweet> fetch()
      throws IOException, JSONException, ParseException, CacheException {
    // Return an empty list if the search term is empty.
    if (Util.isEmpty(twitterWave.getSearchQuery())) {
      return new ArrayList<Tweet>();
    }

    // Handle search mode.
    List<Tweet> tweets = twitterService.search(
        twitterWave.getSearchQuery(),
        twitterWave.getLatestTweetId(),
        twitterWave.getWaveId());

    // Update the latest tweet id.
    if (!tweets.isEmpty()) {
      twitterWave.setLatestTweetId(tweets.get(0).getId());
    }

    return tweets;
  }

  @Override
  public List<Tweet> execute() throws IOException, JSONException, ParseException, CacheException {
    // Extract the search term.
    String query = blip.getDocument().getFormView().getFormElement(SEARCH_INPUT_ID).getValue();

    // Perform search for the new search term.
    if (!twitterWave.getSearchQuery().equals(query)) {
      // Delete all blips.
      for (Blip blip : this.blip.getChildren()) {
        blip.delete();
      }

      // Reset the metadata.
      twitterWave.setSearchQuery(query);
      twitterWave.setLatestTweetId(null);

      // Fetch new search results.
      return fetch();
    }
    return new ArrayList<Tweet>();
  }
}
