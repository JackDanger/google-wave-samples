package com.google.wave.extensions.tweety.model;

/**
 * Model object that represents a Twitter tweet.
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public final class Tweet {

  /**
   * The id of the tweet.
   */
  private final String id;

  /**
   * The content of the tweet.
   */
  private final String text;

  /**
   * The screen name of the tweet's author.
   */
  private final String author;

  /**
   * The time when the tweet was submitted, since epoch.
   */
  private final long time;

  /**
   * Constructs a tweet object.
   *
   * @param id The id of the tweet.
   * @param text The content of the tweet.
   * @param author The tweet author's screen name.
   * @param time The time when the tweet was posted, since epoch.
   */
  public Tweet(String id, String text, String author, long time) {
    this.id = id;
    this.text = text;
    this.author = author;
    this.time = time;
  }

  /**
   * Returns the id the tweet.
   *
   * @return The tweet id.
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the text content of the tweet.
   *
   * @return The tweet content.
   */
  public String getText() {
    return text;
  }

  /**
   * Returns the screen name of the tweet author.
   *
   * @return The author's screen name.
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Returns the time when the tweet was created, since epoch.
   *
   * @return The time when the tweet was created.
   */
  public long getTime() {
    return time;
  }
}
