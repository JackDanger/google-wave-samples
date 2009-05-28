// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.twitter.tweety.util;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

/**
 * Helper class to store a list of tweets that were submitted from Google Wave.
 * This class uses Google App Engine Memcache as the storage.
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public class WaveSubmittedTweetsCache {

  /**
   * An instance of {@link Cache}, which is an interface to communicate with
   * Google App Engine Memcache.
   */
  private Cache cache;

  /**
   * Constructs an instance of the cache.
   * @throws CacheException If there is a problem connecting to Google App
   *     Engine Memcache.
   */
  public WaveSubmittedTweetsCache() throws CacheException {
    CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
    this.cache = cacheFactory.createCache(Collections.emptyMap());
  }

  /**
   * Adds a tweet id to a list of known tweet ids for the given wave.
   *
   * @param waveId The wave id.
   * @param tweetId The tweet id.
   * @throws CacheException If there is a problem accessing the cache.
   */
  @SuppressWarnings("unchecked")
  public void add(String waveId, String tweetId) throws CacheException {
    SortedSet<String> knownTweetIds = (SortedSet<String>) cache.get(waveId);
    if (knownTweetIds == null) {
      knownTweetIds = new TreeSet<String>();
    }
    knownTweetIds.add(tweetId);
    cache.put(waveId, knownTweetIds);
  }

  /**
   * Checks whether the given tweet id was submitted from the given wave.
   *
   * @param waveId The wave id.
   * @param tweetId The tweet id.
   * @return true if the given tweet was submitted from the given wave.
   * @throws CacheException If there is a problem accessing the cache.
   */
  @SuppressWarnings("unchecked")
  public boolean contains(String waveId, String tweetId) throws CacheException {
    SortedSet<String> knownTweetIds = (SortedSet<String>) cache.get(waveId);
    return knownTweetIds != null && knownTweetIds.contains(tweetId);
  }

  /**
   * Deletes all tweet ids that are less than the given tweet id to reduce the
   * size of the cache entry.
   *
   * @param waveId The id of the wave that we want to reduce the cache entry.
   * @param tweetId Keeps tweet ids that are greater than or equal to the given
   *     tweet id.
   * @throws CacheException If there is a problem accessing the cache.
   */
  @SuppressWarnings("unchecked")
  public void purge(String waveId, String tweetId) throws CacheException {
    SortedSet<String> knownTweetIds = (SortedSet<String>) cache.get(waveId);
    if (knownTweetIds != null) {
      SortedSet<String> newerIdsToBeKept = knownTweetIds.tailSet(tweetId);
      if (!newerIdsToBeKept.isEmpty()) {
        SortedSet<String> newEntry = new TreeSet<String>();
        newEntry.addAll(newerIdsToBeKept);
        cache.put(waveId, new TreeSet<String>(newEntry));
      }
    }
  }

  /**
   * Returns the last known submitted tweet for the given wave.
   *
   * @param waveId The wave to check.
   * @return The last tweet that was submitted from the given wave, or
   *     {@code null} if the wave hasn't submitted any tweets.
   * @throws CacheException If there is a problem fetching data from Google
   *     App Engine Memcache.
   */
  @SuppressWarnings("unchecked")
  public String last(String waveId) throws CacheException {
    SortedSet<String> knownTweetIds = (SortedSet<String>) cache.get(waveId);
    if (knownTweetIds != null && !knownTweetIds.isEmpty()) {
      return knownTweetIds.last();
    }
    return null;
  }
}
