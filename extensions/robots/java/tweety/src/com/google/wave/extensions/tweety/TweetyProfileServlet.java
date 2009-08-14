// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.tweety;

import com.google.wave.api.ParticipantProfile;
import com.google.wave.api.ProfileServlet;

import org.json.JSONException;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Servlet that handles profile related requests from Google Wave. This servlet
 * is registered at {@code /_wave/robot/profile}.
 * 
 * By default, the servlet responds with a JSON string that describes the Robot:
 * <pre>
 * {"profileUrl":"http://tweety-wave.appspot.com/about",
 *  "imageUrl":"http://tweety-wave.appspot.com/favicon.ico",
 *  "name":"Tweety the Twitbot"}
 * </pre>
 * 
 * This servlet can also take a query parameter {@code name}, that the caller
 * can supply to resolve a Twitter user's profile. For example, the call to
 * {@code http://tweety-wave.appspot.com/_wave/robot/profile?name=foo} returns:
 * <pre>
 * {"profileUrl":"http://twitter.com/foo",
 *  "imageUrl":"http://<the URL for the user's avatar>",
 *  "name":"<the display name of foo>"}
 * </pre>
 * 
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public class TweetyProfileServlet extends ProfileServlet {

  private static final Logger LOG = Logger.getLogger(TweetyProfileServlet.class.getName());
  
  @Override
  public String getRobotAvatarUrl() {
    return "http://tweety-wave.appspot.com/favicon.ico";
  }

  @Override
  public String getRobotName() {
    return "Tweety the Twitbot";
  }

  @Override
  public String getRobotProfilePageUrl() {
    return "http://tweety-wave.appspot.com/about";
  }

  @Override
  public ParticipantProfile getCustomProfile(String name) {
    try {
      TwitterService twitterService = new TwitterService();
      return twitterService.getProfile(name);
    } catch (IOException e) {
      LOG.warning("Problem retrieving profile for user " + name + " from Twitter. Cause: " +
          e.getMessage());
    } catch (JSONException e) {
      LOG.warning("Problem parsing the profile response from Twitter for user " + name +
          ". Cause: " + e.getMessage());
    }
    return null;
  }
}
