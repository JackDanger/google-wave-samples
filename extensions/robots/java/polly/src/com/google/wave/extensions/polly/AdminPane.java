// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.polly;

import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.FormView;
import com.google.wave.api.Range;
import com.google.wave.api.StyleType;
import com.google.wave.api.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * The Admin pane displays the form that allows the user to define the poll,
 * its title, question, choices and recipients.
 * 
 * @author scovitz@google.com (Seth Covitz)
 */
public class AdminPane {
  
  // Names used to get and set form element values.
  private static final String CHOICES_INPUT = "choices_input";
  private static final String CHOICES_LABEL = "choices_label";
  private static final String RECIPIENTS_LABEL = "recipients_label";
  private static final String RECIPIENTS_INPUT = "recipients_input";
  private static final String QUESTION_INPUT = "question_input";
  private static final String QUESTION_LABEL = "question_label";
  private static final String TITLE_INPUT = "title_input";
  private static final String TITLE_LABEL = "title_label";
  private static final String DISTRIBUTE_POLL_BUTTON = "distribute_poll_button";

  /**
   * The metadata that represents the current state of the form if there has
   * been no change or the previous state of the form if it has been modified. 
   */
  private PollMetadata metadata;

  /**
   * The blip that represents the current state of the admin form.
   */
  private Blip blip;
  
  /**
   * The current title of the poll.
   */
  private String title;
  
  /**
   * The current question text for the poll.
   */
  private String question;
  
  /**
   * The current list of choices for the poll. 
   */
  private List<String> choices;
  
  /**
   * The current list of recipients for the poll.
   */
  private String recipients;
  
  /**
   * Constructs an AdminPane from the current blip and poll metadata.
   * 
   * @param blip the blip within which the admin pane is present.
   * @param metadata the most recent metadata for the poll.
   */
  public AdminPane(Blip blip, PollMetadata metadata) {
    this.blip = blip;
    this.metadata = metadata;
    
    // Parse the blip to derive the current state of the form metadata.
    parseBlip();
  }

  /**
   * Creates the AdminPane from an empty blip.
   */
  public void create() {
    TextView textView = blip.getDocument();

    // Set the 'effective' author to be Polly.
    textView.setAuthor(AdminWavelet.POLLY);
    
    textView.append("\n\n");

    textView.appendElement(new FormElement(ElementType.LABEL, TITLE_LABEL,
        "Enter the title of your poll here:"));

    textView.appendElement(new FormElement(ElementType.INPUT, TITLE_INPUT,
        metadata.getTitle()));
    
    textView.append("\n");

    textView.appendElement(new FormElement(ElementType.LABEL, QUESTION_LABEL,
        "Question: Enter the text of your question here:"));

    textView.appendElement(new FormElement(ElementType.INPUT, QUESTION_INPUT,
        metadata.getQuestion()));
    
    textView.append("\n");
    
    textView.appendElement(new FormElement(ElementType.LABEL, CHOICES_LABEL,
        "Choices:"));
    
    FormElement textArea = new FormElement(ElementType.TEXTAREA, CHOICES_INPUT);
    textView.appendElement(textArea);
    
    // Style the textarea to initially be bulleted.
    int textAreaPosition = textView.getPosition(textArea);
    textView.setStyle(new Range(textAreaPosition, textAreaPosition + 1), StyleType.BULLETED);
    
    textView.append("\n");

    textView.appendElement(new FormElement(ElementType.LABEL, RECIPIENTS_LABEL,
        "Recipients (comma separated list of participants):"));

    textView.appendElement(new FormElement(ElementType.INPUT, RECIPIENTS_INPUT,
        metadata.getRecipients()));
    
    textView.append("\n");

    textView.appendElement(new FormElement(ElementType.BUTTON, DISTRIBUTE_POLL_BUTTON,
        "Distribute Poll"));

    // Create an annotation over the document so that we can recognize it. No
    // value is set since we are only concerned with the existence of the
    // annotation.
    textView.setAnnotation("poll-admin", "");
    
    // Parse the form to retrieve the inital values.
    parseBlip();
  }

  /**
   * Parse the form elements in the blip to derive their current values.
   */
  private void parseBlip() {
    try {
      FormView formView = blip.getDocument().getFormView();
      title = formView.getFormElement(TITLE_INPUT).getValue();
      question = formView.getFormElement(QUESTION_INPUT).getValue();

      String choiceString = formView.getFormElement(CHOICES_INPUT).getValue();
      choices = new ArrayList<String>();
      if (!choiceString.isEmpty()) {
        for (String line : choiceString.split("\\n")) {
          choices.add(line.replace("* ", ""));
        }
      }
      
      recipients = formView.getFormElement(RECIPIENTS_INPUT).getValue();
    } catch(NullPointerException npx) {
      // Form will not be valid until it has been fully created.
    }
  }

  /**
   * Returns the current title of the poll.
   * 
   * @return the current title of the poll.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Checks the current value of the title and the previous value to see if it
   * has changed.
   * 
   * @return true if the title has changed, false otherwise.
   */
  public boolean hasTitleChanged() {
    return !title.equals(metadata.getTitle());
  }
  
  /**
   * Returns the current question for the poll.
   * 
   * @return the current question for the poll.
   */
  public String getQuestion() {
    return question;
  }
  
  /**
   * Checks the current value of the question and the previous value to see if
   * it has changed.
   * 
   * @return true if the question has changed, false otherwise.
   */
  public boolean hasQuestionChanged() {
    return !question.equals(metadata.getQuestion());
  }
  
  /**
   * Returns the current list of choices for the poll.
   * 
   * @return the current list of choices for the poll.
   */
  public List<String> getChoices() {
    return choices;
  }
  
  /**
   * Checks the current list of choices and the previous list to see if it has
   * changed.
   * 
   * @return true if the choices have changed, false otherwise.
   */
  public boolean haveChoicesChanged() {
    if (choices.size() != metadata.getChoices().size()) {
      return true;
    }
    
    for (int i = 0; i < choices.size(); ++i) {
      if (!choices.get(i).equals(metadata.getChoices().get(i))) {
        return true;
      }
    }
      
    return false;
  }
  
  /**
   * Returns the current list of recipients for the poll.
   * 
   * @return the current list of recipients for the poll.
   */
  public String getRecipients() {
    return recipients;
  }
  
  /**
   * Checks the current list of recipients and the previous list to see if it
   * has changed.
   * 
   * @return true if the recipients have changed, false otherwise.
   */
  public boolean haveRecipientsChanged() {
    return !recipients.equals(metadata.getRecipients());
  }
  
  /**
   * Checks the admin pane to see if any of the poll data has changed since it
   * was last saved.
   * 
   * @return true if the data has changed, false otherwise.
   */
  public boolean hasChanged() {
    return hasTitleChanged() || hasQuestionChanged() || haveChoicesChanged() ||
        haveRecipientsChanged() || metadata.hasNewVotes();
  }

  /**
   * Checks the to see if the 'distribute poll' button is currently clicked. If
   * so, the button is reset into the unclicked/default state.
   * 
   * @return true if the button was clicked, false otherwise.
   */
  public boolean isDistributePollButtonPressed() {
    FormView formView = blip.getDocument().getFormView();
    FormElement distributePoll = formView.getFormElement(DISTRIBUTE_POLL_BUTTON);
    
    boolean isPressed = "clicked".equals(distributePoll.getValue());
    if (isPressed) {
      distributePoll.setValue(distributePoll.getDefaultValue());
      formView.replace(distributePoll);
    }
    
    return isPressed;
  }
}
