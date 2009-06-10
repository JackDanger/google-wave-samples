// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.polly;

import com.google.wave.api.ProfileServlet;

/**
 * A servlet that is used to fetch the profile information for Polly.
 * 
 * @author scovitz@google.com (Seth Covitz)
 */
public class PollyProfileServlet extends ProfileServlet {

  /* (non-Javadoc)
   * @see com.google.wave.api.ProfileServlet#getRobotAvatarUrl()
   */
  @Override
  public String getRobotAvatarUrl() {
    return "http://polly-wave.appspot.com/_wave/polly.jpg";
  }

  /* (non-Javadoc)
   * @see com.google.wave.api.ProfileServlet#getRobotName()
   */
  @Override
  public String getRobotName() {
    return "Polly the Pollster";
  }

  /* (non-Javadoc)
   * @see com.google.wave.api.ProfileServlet#getRobotProfilePageUrl()
   */
  @Override
  public String getRobotProfilePageUrl() {
    return "http://polly-wave.appspot.com/about";
  }
}
