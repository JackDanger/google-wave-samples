// Copyright 2009 Google Inc. All Rights Reserved

package com.google.wave.extensions.blogbot;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.Range;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all incoming events from the wave Blogbot resides on.
 *
 * @author kimwhite@google.com (Kimberly White)
 */
public class BlogbotServlet extends AbstractRobotServlet {

  /**
   * The key of a flag to signify that the wave is the table of contents wave.
   */
  private static final String TOC_FLAG = "isTableOfContents";
  
  /**
   * The key of a wave id.
   */
  private static final String WAVE_ID = "waveID";
  
  /**
   * The key of a wavelet id.
   */
  private static final String WAVELET_ID = "waveletID";
  
  /**
   * The key of a blip id.
   */
  private static final String BLIP_ID = "blipID";
  
  /**
   * The key of a link annotation.
   */
  private static final String LINK_ANNOTATION_KEY = "link/wave";
  
  /**
   * The key of a wave title.
   */
  private static final String TITLE = "title";
  
  /**
   * The key of a blog title
   */
  private static final String BLOG_TITLE = "New Blog Entry";

  /**
   * A regular expression used in parsing the blip id.
   */
  private static final String LINE_SEP_REGEX = "\\n";
  
  /**
   * A regular expression used to extract the blip id.
   */
  private static final String FIELD_SEP_REGEX = "\\:";
  
  /**
   * The key of the address of the robot.
   */
  private static final String ROBOT = "blogbot-wave@appspot.com";
  
  /**
   * Receives and handles Wave events.
   * @param bundle contains all incoming wave events.
   */
  @Override
  public void processEvents(RobotMessageBundle bundle) {
    Wavelet wavelet = bundle.getWavelet(); 

    if (bundle.wasSelfAdded() && bundle.isNewWave() && !wavelet.hasDataDocument(TOC_FLAG)) {
      TableOfContents contents = new TableOfContents(wavelet);
      wavelet.appendDataDocument(TOC_FLAG, "true");
      wavelet.appendDataDocument(WAVE_ID, contents.getWaveId());
      wavelet.appendDataDocument(WAVELET_ID, contents.getWaveletId());
      wavelet.appendDataDocument(BLIP_ID, contents.getBlipId());
    }
    
    // Handle various events.
    for (Event e : bundle.getEvents()) {
      switch (e.getType()) {
        case FORM_BUTTON_CLICKED:
          // New post button pressed in TOC wave.
          newPost(bundle, wavelet);
          break;
        case BLIP_SUBMITTED:
          // Post title changed.
          if ("false".equals(wavelet.getDataDocument(TOC_FLAG))) {
            if (!wavelet.getDataDocument(TITLE).equals(wavelet.getTitle())) {
              updateTableOfContents(bundle, wavelet);
            }
          }
          break;
        case DOCUMENT_CHANGED:
          // Receives and parses the Table of Contents BlipID from Rusty.
          String newIds = wavelet.getDataDocument("_new_ids_");
          if (newIds != null && !newIds.isEmpty()) {
            for (String line : newIds.split(LINE_SEP_REGEX)) {
              if (line.startsWith("<p>" + wavelet.getDataDocument(BLIP_ID))) {
                String[] pollBlip = line.split(FIELD_SEP_REGEX);
                String participantPollBlipId = pollBlip[1];
                pollBlip = participantPollBlipId.split(" ");
                participantPollBlipId = pollBlip[2];
                wavelet.setDataDocument(BLIP_ID, participantPollBlipId);
              }
            }
            wavelet.setDataDocument("_new_ids_", "");
          }
          break;
      }
    }
  }
    
  /**
   * Constructs a new blog or post wave.
   * @param bundle contains all incoming wave events.
   * @param wavelet the Table of Contents wavelet.
   */
  public void newPost(RobotMessageBundle bundle, Wavelet wavelet) {    
    // Verify wavelet has valid values
    validateWavelet(wavelet);
    
    String waveId = wavelet.getDataDocument(WAVE_ID);
    String waveletId = wavelet.getDataDocument(WAVELET_ID);
    String blipId = wavelet.getDataDocument(BLIP_ID);
    String annotationWriteBack = String.valueOf(Math.random());
    List<String> participants = new ArrayList<String>();
    
    // Adds the blog owner, robot, and all participants of TOC
    // as participants on the new blog wave.
    participants.add(ROBOT);
    participants.add(wavelet.getCreator());
    participants.addAll(wavelet.getParticipants());
    Wavelet newWavelet = bundle.createWavelet(participants, annotationWriteBack);
    
    // Sets initial wave information.
    newWavelet.setTitle(BLOG_TITLE);
    newWavelet.appendDataDocument(TITLE, BLOG_TITLE);
    
    // Give the new wave the Table Of Contents data.
    passTableOfContentsData(newWavelet, waveId, waveletId, blipId);  
  }
  
  /**
   * Gives the new wave the Table of Contents ID data.
   * @param newWavelet the new blog post wavelet.
   * @param waveId the Table of Contents wave ID.
   * @param waveletId the Table of Contents wavelet ID.
   * @param blipId the Table of Contents blip ID.
   */
  public void passTableOfContentsData(Wavelet newWavelet, String waveId, 
      String waveletId, String blipId) {
    newWavelet.appendDataDocument(WAVE_ID, waveId);
    newWavelet.appendDataDocument(WAVELET_ID, waveletId);
    newWavelet.appendDataDocument(BLIP_ID, blipId);
    newWavelet.appendDataDocument(TOC_FLAG, "false");
  }
  
  /**
   * Updates table of contents with a link to the new wave.
   * @param bundle contains all incoming wave events.
   * @param postWavelet the blog post wavelet.
   */
  public void updateTableOfContents(RobotMessageBundle bundle, Wavelet postWavelet) {
    // Verify wavelet has valid values
    validateWavelet(postWavelet);
    
    // Retrieve information stored in post wavelet's data document.
    String waveId = postWavelet.getDataDocument(WAVE_ID);
    String waveletId = postWavelet.getDataDocument(WAVELET_ID);
    String blipId = postWavelet.getDataDocument(BLIP_ID);
    String postTitle = postWavelet.getTitle();
    
    // Get access to table of contents blip by using the stored information.
    Blip blip = bundle.getBlip(waveId, waveletId, blipId);
    TextView contentsDocument = blip.getDocument();
    Wavelet contentsWavelet = blip.getWavelet();
        
    // Add and linkify the title of the new blog post.
    String postWaveId = postWavelet.getWaveId();
    contentsDocument.insert(0, "\n" + postTitle);
    int upperIndex = postTitle.length() + 1; 
    linkify(0, upperIndex, postWaveId, contentsDocument);
    
    // Reset title attributes.
    postWavelet.setDataDocument(TITLE, postTitle);  
  }
  
    /**
     * Creates the link to the blog wave in Table of Contents.
     * @param lowerIndex the beginning of the text to linkify.
     * @param upperIndex the end of the text to linkify.
     * @param postWaveID the blog post wave ID to link to.
     * @param contentsDocument the Table of Contents wave document.
     */
  public void linkify(int lowerIndex, int upperIndex, String postWaveID, 
      TextView contentsDocument) {
    contentsDocument.setAnnotation(new Range(lowerIndex, upperIndex),
        LINK_ANNOTATION_KEY, postWaveID);
  }
  
  private void validateWavelet(Wavelet wavelet) {
    // Check if data document values are null
    if (wavelet.getDataDocument(WAVE_ID) == null || 
      wavelet.getDataDocument(WAVELET_ID) == null ||
      wavelet.getDataDocument(BLIP_ID) == null) {
      throw new IllegalStateException("Wavelet " + wavelet.getWaveletId() + 
          " not intialized correctly");
    }
  }
}
