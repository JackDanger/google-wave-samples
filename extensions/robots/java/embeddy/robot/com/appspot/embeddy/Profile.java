package com.appspot.embeddy;

import com.google.wave.api.ProfileServlet;

@SuppressWarnings("serial")
public class Profile extends ProfileServlet {
  @Override
  public String getRobotAvatarUrl() {
    return "http://embeddy.appspot.com/robot/avatar.png";
  }

  @Override
  public String getRobotName() { return "Embeddy"; }

  @Override
  public String getRobotProfilePageUrl() {
    return "http://embeddy.appspot.com/";
  }
}
