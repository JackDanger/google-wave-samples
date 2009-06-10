// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.polly;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.RobotMessageBundle;

/**
 * Polly is a robot that acts as an interactive form builder to help Wave users
 * construct, distribute and collect polls.
 * 
 * @author scovitz@google.com (Seth Covitz)
 */
public class PollyServlet extends AbstractRobotServlet {

  /**
   * The main event handler for Polly. It constructs an object that knows how
   * to operate on the different wavelet 'types' that Polly has created.
   * 
   * @param context the robot context while handling the events.
   */
  @Override
  public void processEvents(RobotMessageBundle context) {

    // Poll Administration Flow
    if (isNewPollyWave(context) || AdminWavelet.isAdminWavelet(context)) {
      AdminWavelet adminWavelet = new AdminWavelet(context);
      adminWavelet.handleEvents();
      return;
    }
    
    // Poll Recipient Flow
    if (PollWavelet.isPollWavelet(context)) {
      PollWavelet pollWavelet = new PollWavelet(context);
      pollWavelet.handleEvents();
      return;
    }
  }
  
  /**
   * Check to see if a new Polly wave has been created. Specifically, we check
   * if the robot was just added as a participant, and the title is blank.
   * 
   * @param context the robot context while handling the events. 
   * @return true if this is a new Polly wave, false otherwise.
   */
  private boolean isNewPollyWave(RobotMessageBundle context) {
    return context.wasSelfAdded() && context.getWavelet().getTitle().isEmpty();
  }
}
