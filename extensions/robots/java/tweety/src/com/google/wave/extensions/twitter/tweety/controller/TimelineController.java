// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.twitter.tweety.controller;

import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.StyleType;
import com.google.wave.api.StyledText;
import com.google.wave.api.TextView;
import com.google.wave.extensions.twitter.tweety.TwitterService;
import com.google.wave.extensions.twitter.tweety.model.Tweet;
import com.google.wave.extensions.twitter.tweety.model.TwitterWave;
import com.google.wave.extensions.twitter.tweety.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.cache.CacheException;

/**
 * A controller for Twitter Timeline mode. It is responsible for rendering the
 * update form on a blip, and fetching timeline tweets from Twitter.
 *
 * This controller renders the update form in the following format:
 * <pre>
 * What are you doing [username]?
 * [Input box to enter the tweet]
 * [Button to post the tweet]
 * </pre>
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public class TimelineController implements FetchController {

  /**
   * The element id of the update input box.
   */
  private static final String UPDATE_INPUT_ID = "update_input";

  /**
   * The element id of the update button.
   */
  private static final String UPDATE_BUTTON_ID = "update_button";


  /**
   * The blip to render the update form.
   */
  private Blip blip;

  /**
   * The Twitter Wave where this controller resides.
   */
  private TwitterWave twitterWave;

  /**
   * Constructs a timeline controller given a {@link Blip} and a
   * {@link TwitterWave}
   *
   * @param blip The blip to render the update form.
   * @param twitterWave The Twitter Wave where this controller resides.
   */
  public TimelineController(Blip blip, TwitterWave twitterWave) {
   this.blip = blip;
   this.twitterWave = twitterWave;
  }

  @Override
  public void renderForm() {
    // Clear out everything.
    TextView document = blip.getDocument();
    document.delete();

    // Set the title.
    blip.getWavelet().setTitle(
        new StyledText("What are you doing " + twitterWave.getUsername() + "?",
            StyleType.HEADING3));

    // Insert the update form.
    document.append("\n");
    document.appendElement(new FormElement(
        ElementType.INPUT,
        UPDATE_INPUT_ID,
        ""));

    // Insert the update button.
    document.append("\n");
    document.appendElement(new FormElement(
        ElementType.BUTTON,
        UPDATE_BUTTON_ID,
        "Update"));
  }

  @Override
  public boolean isButtonClicked() {
    return Util.isButtonClicked(blip, UPDATE_BUTTON_ID);
  }

  @Override
  public List<Tweet> fetch()
      throws IOException, JSONException, ParseException, CacheException {
    // Handle timeline mode, fetch tweets from Twitter.
    List<Tweet> tweets = TwitterService.fetchTimeline(
        twitterWave.getUsername(),
        twitterWave.getPassword(),
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
    // Extract the tweet from the update form.
    String tweet = blip.getDocument().getFormView().getFormElement(UPDATE_INPUT_ID).getValue();

    // Tweet the content of the update form.
    List<Tweet> tweets = new ArrayList<Tweet>();
    if (!Util.isEmpty(tweet)) {
      tweets.add(TwitterService.tweet(
          twitterWave.getUsername(),
          twitterWave.getPassword(),
          tweet,
          null,
          twitterWave.getWaveId()));
    }

    // Reset the update box.
    renderForm();
    return tweets;
  }
}
