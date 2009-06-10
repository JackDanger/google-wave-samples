// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.polly;

import com.google.wave.api.Blip;
import com.google.wave.api.Element;
import com.google.wave.api.ElementType;
import com.google.wave.api.Image;
import com.google.wave.api.StyleType;
import com.google.wave.api.StyledText;
import com.google.wave.api.TextView;

import java.util.List;

/**
 * The Results pane displays two graphs depicting the distribution of votes and
 * the participation amongst recipients in the poll. It will appear once the
 * poll has been distributed and will update as recipients vote.
 * 
 * @author scovitz@google.com (Seth Covitz)
 */
public class ResultsPane {

  private static final int DISTRIBUTION_HEIGHT = 150;

  private static final int DISTRIBUTION_WIDTH = 400;

  private static final int PARTICIPATION_HEIGHT = 150;

  private static final int PARTICIPATION_WIDTH = 360;

  /**
   * The blip that represents the current state of the preview form.
   */
  private Blip blip;
  
  /**
   * The metadata that represents the current state of the form if there has
   * been no change or the previous state of the form if it has been modified. 
   */
  private PollMetadata metadata;

  /**
   * Constructs a ResultsPane from the current blip and poll metadata.
   * 
   * @param blip the blip within which the poll pane is present.
   * @param metadata the most recent metadata for the poll.
   */
  public ResultsPane(Blip blip, PollMetadata metadata) {
    this.blip = blip;
    this.metadata = metadata;
  }

  /**
   * Creates the ResultsPane by appending it to the end of a blip.
   */
  public void create() {
    TextView textView = blip.getDocument();

    textView.append("\n");

    textView.appendStyledText(new StyledText("Results", StyleType.HEADING2));

    textView.append("\n\n");
    
    textView.appendElement(new Image(getParticipationChartUrl(),
        PARTICIPATION_WIDTH, PARTICIPATION_HEIGHT, ""));
    
    textView.appendElement(new Image(getDistrtibutionChartUrl(),
        DISTRIBUTION_WIDTH, DISTRIBUTION_HEIGHT, ""));

    textView.append("\n");
  }

  /**
   * Returns whether the ResultsPane is visible.
   * 
   * @return true if the ResultsPane has been created, false otherwise.
   */
  public boolean isVisible() {
    return blip.getDocument().getText().contains("Results");
  }

  /**
   * Updates the results charts by recalculating the Chart API urls and
   * replacing the images.
   */
  public void update() {
    Image distributionChart = new Image(getDistrtibutionChartUrl(),
        DISTRIBUTION_WIDTH, DISTRIBUTION_HEIGHT, "");
    Image participationChart = new Image(getParticipationChartUrl(),
        PARTICIPATION_WIDTH, PARTICIPATION_HEIGHT, "");
    
    TextView textView = blip.getDocument();
    for (Element element : textView.getElements(ElementType.IMAGE)) {
      Image image = (Image)element;
      if (image.getUrl().contains("Poll%20Distribution")) {
        textView.replaceElement(textView.getPosition(image), distributionChart);
      } else if (image.getUrl().contains("Poll%20Participation")) {
        textView.replaceElement(textView.getPosition(image), participationChart);
      }
    }
  }

  /**
   * Builds the distribution chart url from the list of poll choices and
   * received votes.
   * 
   * @return the distribution chart url.
   */
  private String getDistrtibutionChartUrl() {
    StringBuilder distribution = new StringBuilder();
    distribution.append("http://chart.apis.google.com/chart?chtt=Poll%20Distribution&");
    distribution.append("cht=p3&chs=400x150&chd=t:");
    
    String data = "";
    String labels = "";
    List<String> choices = metadata.getChoices();
    List<Integer> counts = metadata.getChoiceCounts();
    for (int i = 0; i < metadata.getChoices().size(); ++i) {
      data += "," + counts.get(i);
      labels += "|" + choices.get(i) + " (" + counts.get(i) + ")";
    }
    data = data.substring(1);
    labels = labels.substring(1);
    
    distribution.append(data);
    distribution.append("&chl=");
    distribution.append(labels);
    
    return distribution.toString();
  }

  /**
   * Builds the participation chart url from the list of recipients and
   * received votes.
   * 
   * @return the participation chart url.
   */
  private String getParticipationChartUrl() {
    StringBuilder participation = new StringBuilder();
    participation.append("http://chart.apis.google.com/chart?chtt=Poll%20Participation&cht=gom&");
    participation.append("chs=360x150&chco=FFB000,FFA800,FFA000,FF9800,FF9000,FF8800,FF8000&");
    participation.append("chd=t:");
    
    int numRecipients = metadata.getRecipientsAsList().size();
    int numParticipants = metadata.getVoters().size();

    String data = String.valueOf(numParticipants * 100 / numRecipients);
    String label = data + "% done (" + String.valueOf(numParticipants) + " of " +
        String.valueOf(numRecipients) + ")";
    
    participation.append(data);
    participation.append("&chl=");
    participation.append(label);
    
    return participation.toString();
  }
}
