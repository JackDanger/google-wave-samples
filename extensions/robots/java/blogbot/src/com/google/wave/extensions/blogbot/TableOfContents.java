// Copyright 2009 Google Inc. All Rights Reserved

package com.google.wave.extensions.blogbot;

import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

/**
 * Sets up and stores information regarding the Table of Contents wave.
 *
 * @author kimwhite@google.com (Kimberly White)
 */
public class TableOfContents {

  /**
   * The title of the table of contents wave.
   */
  private static final String CONTENTS_TITLE = "Table of Contents";
  
  /**
   * The caption of the button element in the table of contents wave.
   */
  private static final String LOGIN_BUTTON_CAPTION = "New Post";
  
  /**
   * The id of the button element.
   */
  private static final String LOGIN_BUTTON_ID = "login_button";
  
  /**
   * The id of the table of contents blip containing links to other waves.
   */
  private String blipId;
  
  /**
   * The id of the table of contents wave.
   */
  private String waveId;
  
  /**
   * The id of the table of contents wavelet.
   */
  private String waveletId;
    
  /**
   * Constructs a new table of contents wave.
   * @param wavelet The wavelet containing the table of contents.
   */
  public TableOfContents(Wavelet wavelet) {
    // Set IDs.
    setWaveId(wavelet.getWaveId());
    setWaveletId(wavelet.getWaveletId());
    
    // Setup wavelet.
    wavelet.setTitle(CONTENTS_TITLE);
    TextView contentsDocument = wavelet.getRootBlip().getDocument();
    contentsDocument.append("\n\n(Write Introduction Here)");
    
    // Create New Post button in new blip.
    Blip blip = wavelet.appendBlip();
    contentsDocument = blip.getDocument();
    contentsDocument.appendElement(new FormElement(ElementType.BUTTON, LOGIN_BUTTON_ID,
        LOGIN_BUTTON_CAPTION));
    
    // Create new blip for TOC and save blip ID.
    String blipKey = String.valueOf(Math.random());
    blip = wavelet.appendBlip(blipKey);
    setBlipId(blipKey); 
  }

  public void setBlipId(String blipId) {
    this.blipId = blipId;
  }

  public String getBlipId() {
    return blipId;
  }

  public void setWaveId(String waveId) {
    this.waveId = waveId;
  }

  public String getWaveId() {
    return waveId;
  }

  public void setWaveletId(String waveletId) {
    this.waveletId = waveletId;
  }

  public String getWaveletId() {
    return waveletId;
  }
}
