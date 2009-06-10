// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.polly;

import com.google.wave.api.Blip;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.StyleType;
import com.google.wave.api.StyledText;
import com.google.wave.api.Wavelet;

/**
 * The Poll wavelet is distributed to each poll recipient and allows them to
 * vote/respond to the poll question.
 * 
 * @author scovitz@google.com (Seth Covitz)
 */
public class PollWavelet {

  /**
   * A reference to the wavelet that received the event or represents the
   * poll.
   */
  private Wavelet wavelet;
  
  /**
   * The persistent state of the poll. 
   */
  private PollMetadata metadata;
 
  /**
   * A convenient wrapper to query/manipulate a virtual pane in the
   * poll. The PreviewPane is reused from the AdminWavelet such that the
   * poll is identical to what the user saw in the preview.
   */
  private PreviewPane pollPane;

  /**
   * Constructs a PollWavelet in the current robot context. This constructor
   * is called when the PollWavelet receives an event.
   * 
   * @param context the context within which the robot is currently running.
   */
  public PollWavelet(RobotMessageBundle context) {
    this.wavelet = context.getWavelet();
    this.metadata = new PollMetadata(context);
    
    Blip rootBlip = wavelet.getRootBlip();
    this.pollPane = new PreviewPane(rootBlip, metadata);
  }

  /**
   * Constructs a PollWavelet given the current robot context and a newly
   * created Wavelet. New PollMetadata is created for each poll.
   * 
   * @param context the context within which the robot is currently running.
   * @param wavelet a new wavelet to be initialized as a poll.
   */
  public PollWavelet(RobotMessageBundle context, Wavelet wavelet) {
    this.wavelet = wavelet;
    this.metadata = new PollMetadata(context);
    this.pollPane = new PreviewPane(wavelet.getRootBlip(), metadata);
  }

  /**
   * Determines whether the wavelet that received an event has been initialized
   * by Polly as a Poll Wavelet.
   * 
   * @param context the context within which the robot is currently running.
   * @return true if the wavelet is a poll wavelet, false otherwise.
   */
  public static boolean isPollWavelet(RobotMessageBundle context) {
    return context.getWavelet().getRootBlip().getDocument().hasAnnotation("poll-wavelet");
  }

  /**
   * Creates the poll form. The poll form consists of a single poll pane that
   * allows the recipient to vote on the poll and submit their vote when done.
   */
  public void create(Wavelet resultsWavelet, String recipient) {
    metadata.setRecipients(recipient);
    metadata.setResultsWaveletId(resultsWavelet.getWaveId() + " " +
        resultsWavelet.getWaveletId());

    wavelet.setTitle(new StyledText(metadata.getTitle() + " -- Poll", StyleType.HEADING2));
    pollPane.create(false);
    wavelet.getRootBlip().getDocument().setAnnotation("poll-wavelet", "");
    
    metadata.saveMetadata(wavelet);
  }

  /**
   * A handler for PollWavelet events.
   */
  public void handleEvents() {
    if (pollPane.isSubmitPollButtonPressed()) {
      submitPoll();
    }
  }

  /**
   * Submits the user's vote by writing the vote to the results wavelet's (aka
   * Admin Wavelet) data document.
   */
  private void submitPoll() {
    metadata.writeVoteToResults(pollPane.getVote());
  }
}
