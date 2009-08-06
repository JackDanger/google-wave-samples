package com.google.wave.extensions.blogbot;

import com.google.wave.api.ProfileServlet;

/**
 * Servlet that handles profile related requests from Google Wave. This servlet
 * is registered at {@code /_wave/robot/profile}.
 * 
 * By default, the servlet responds with a JSON string that describes the Robot:
 * <pre>
 * {"profileUrl":"http://blogbot-wave.appspot.com/about",
 *  "imageUrl":"http://blogbot-wave.appspot.com/favicon.ico",
 *  "name":"Blogbot"}
 * </pre>
 * 
 * @author kimwhite@google.com (Kimberly White)
 */
public class BlogbotProfileServlet extends ProfileServlet {
  /**
   * Returns the url of Blogbot's avatar.
   */
  @Override
  public String getRobotAvatarUrl() {
    return "http://blogbot-wave.appspot.com/favicon.ico";
  }
  
  /**
   * Returns Blogbot's name.
   */
  @Override
  public String getRobotName() {
    return "Blogbot";
  }
  
  /**
   * Returns the url of Blogbot's profile page.
   */
  @Override
  public String getRobotProfilePageUrl() {
    return "http://blogbot-wave.appspot.com/about";
  }  
}
