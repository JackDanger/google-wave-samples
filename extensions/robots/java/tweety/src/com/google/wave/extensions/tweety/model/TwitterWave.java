// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.tweety.model;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Model object that represents a Twitter Wave. It holds metadata such as the
 * wave id, wavelet id, the wavelet creator, and whether this wave is in 
 * Twitter Search mode or normal Timeline mode.
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 * @author kimwhite@google.com (Kimberly White)
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class TwitterWave {

  /**
   * A prefix that we prepend to the primary key, the wave id, since Google
   * App Engine doesn't support primary key that starts with number.
   */
  private static final String TWEETY_WAVE_KEY_PREFIX = "TWEETY_";

  /**
   * The wave id that serves as the primary key.
   */
  @PrimaryKey
  @Persistent
  private String waveId;

  /**
   * The id of the wavelet creator.
   */
  @Persistent
  private String creator;
  
  /**
   * The wavelet id where the robot resides as a participant.
   */
  @Persistent
  private String waveletId;

  /**
   * The id of the last tweet that was submitted from Google Wave for this
   * particular Twitter Wave, either via the Update box, or from reply.
   */
  @Persistent
  private String latestTweetId;

  /**
   * The search term that we use to query Twitter when in search mode. This will
   * be {@code null} if we are in timeline mode.
   */
  @Persistent
  private String searchQuery;

  /**
   * The flag that denotes whether the Twitter Wave is in search or timeline
   * mode.
   */
  @Persistent
  private boolean inSearchMode;

  /**
   * Helper method to fetch a {@link TwitterWave} object from the data store
   * for the given Wave.
   *
   * @param manager The persistence manager used to fetch a TwitterWave object.
   * @param waveId The wave id.
   * @param waveletId The wavelet id.
   * @param waveletCreator The wavelet creator id.
   * @return The {@link TwitterWave} object associated with the given Wave.
   */
  @SuppressWarnings("unchecked")
  public static TwitterWave getTwitterWave(
      PersistenceManager manager,
      String waveId,
      String waveletId,
      String waveletCreator) {
    Query query = manager.newQuery(TwitterWave.class);
    query.setFilter("waveId == waveIdParam");
    query.declareParameters("String waveIdParam");
    String waveIdParam = TwitterWave.TWEETY_WAVE_KEY_PREFIX + waveId;
    List<TwitterWave> twitterWaves = (List<TwitterWave>) query.execute(waveIdParam);

    if (!twitterWaves.isEmpty() && twitterWaves.get(0) != null) {
      return twitterWaves.get(0);
    }

    return new TwitterWave(waveId, waveletId, null, waveletCreator);
  }

  /**
   * Construct a TwitterWave object.
   *
   * @param waveId The wave id.
   * @param waveletId The wavelet id.
   * @param latestTweetId The id of the latest tweet that was submitted from the
   *     given wave.
   * @param waveletCreator The wavelet creator id.
   */
  public TwitterWave(String waveId, String waveletId, String latestTweetId, String waveletCreator) {
    setWaveId(waveId);
    this.creator = waveletCreator;
    this.waveletId = waveletId;
    this.latestTweetId = latestTweetId;
  }

  /**
   * Returns wave id of this Twitter Wave.
   *
   * @return The wave id.
   */
  public String getWaveId() {
    return waveId.replace(TWEETY_WAVE_KEY_PREFIX, "");
  }

  /**
   * Sets the wave id that this Twitter Wave is associated with.
   *
   * @param waveId The wave id.
   */
  public void setWaveId(String waveId) {
    this.waveId = TWEETY_WAVE_KEY_PREFIX + waveId;
  }

  /**
   * Returns the id of the wavelet where Tweety is a participant of.
   *
   * @return The wavelet id.
   */
  public String getWaveletId() {
    return waveletId;
  }

  /**
   * Sets the id of the wavelet where Tweety is a participant of.
   *
   * @param waveletId The wavelet id.
   */
  public void setWaveletId(String waveletId) {
    this.waveletId = waveletId;
  }

  /**
   * Returns the id of the latest tweet that was submitted from this Twitter
   * Wave.
   *
   * @return The latest tweet id.
   */
  public String getLatestTweetId() {
    return latestTweetId;
  }

  /**
   * Sets the id of the latest tweet that was submitted from this Twitter Wave.
   *
   * @param latestTweetId The latest tweet id.
   */
  public void setLatestTweetId(String latestTweetId) {
    this.latestTweetId = latestTweetId;
  }

  /**
   * Returns the search term that is associated with this Twitter Wave, if this
   * Twitter Wave is in search mode.
   *
   * @return The search term.
   */
  public String getSearchQuery() {
    return searchQuery;
  }

  /**
   * Sets the search term that is associated with this Twitter Wave, if this
   * Twitter Wave is in search mode.
   *
   * @param searchQuery The search term that is associated with this Twitter
   *     Wave.
   */
  public void setSearchQuery(String searchQuery) {
    this.searchQuery = searchQuery;
  }

  /**
   * Returns a boolean flag that determines whether this Twitter Wave is in
   * search mode or timeline mode.
   *
   * @return {@code true} if this Twitter Wave is in search mode.
   */
  public boolean isInSearchMode() {
    return inSearchMode;
  }

  /**
   * Sets a boolean flag that determines whether this Twitter Wave is in search
   * mode or timeline mode.
   *
   * @param inSearchMode The boolean flag to set.
   */
  public void setInSearchMode(boolean inSearchMode) {
    this.inSearchMode = inSearchMode;
  }

  /**
   * Returns the wavelet creator id.
   * 
   * @return the wavelet creator.
   */
  public String getCreator() {
    return creator;
  }
}
