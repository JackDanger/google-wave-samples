// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.polly;

import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.FormView;
import com.google.wave.api.Range;
import com.google.wave.api.StyleType;
import com.google.wave.api.StyledText;
import com.google.wave.api.TextView;

import java.util.List;

/**
 * The Preview pane displays the poll as a recipient will see it once it is
 * distributed. The contents of form change dynamically as the user defines the
 * form in the Admin pane.
 * 
 * @author scovitz@google.com (Seth Covitz)
 */
public class PreviewPane {

  // Names used to get and set form element values.
  private static final String PREV_CHOICE_LABEL_SUFFIX = "_label";
  private static final String PREV_CHOICE_RADIO_SUFFIX = "_radio";
  private static final String PREV_CHOICE_PREFIX = "prev_choice";
  private static final String PREV_CHOICES_RADIOGROUP = "prev_choices_group";
  private static final String PREV_QUESTION_LABEL = "prev_question_label";
  private static final String PREV_SUBMIT_POLL_BUTTON = "submit_poll_button";
  
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
   * Constructs a PreviewPane from the current blip and poll metadata.
   * 
   * @param blip the blip within which the poll pane is present.
   * @param metadata the most recent metadata for the poll.
   */
  public PreviewPane(Blip blip, PollMetadata metadata) {
    this.blip = blip;
    this.metadata = metadata;
  }

  /**
   * Creates the PollPane by appending it to the end of a blip.
   * 
   * @param asPreview whether or not the PreviewPane is used in a preview or as
   *     the actual poll.
   */
  public void create(boolean asPreview) {
    TextView textView = blip.getDocument();

    // If being used as a preview, create a sub-heading for the pane.
    if (asPreview) {
      textView.append("\n");
      textView.appendStyledText(new StyledText("Preview", StyleType.HEADING2));
    }

    textView.append("\n\n");

    textView.appendElement(new FormElement(ElementType.LABEL, PREV_QUESTION_LABEL,
        metadata.getQuestion()));
    textView.append("\n\n");
    
    textView.appendElement(new FormElement(ElementType.RADIO_BUTTON_GROUP,
        PREV_CHOICES_RADIOGROUP));
    
    for (int i = 0; i < metadata.getChoices().size(); ++i) {
      textView.appendElement(new FormElement(
          ElementType.RADIO_BUTTON,
          PREV_CHOICES_RADIOGROUP,
          PREV_CHOICE_PREFIX + String.valueOf(i) + PREV_CHOICE_RADIO_SUFFIX));
      textView.appendElement(new FormElement(
          ElementType.LABEL,
          PREV_CHOICE_PREFIX + String.valueOf(i) + PREV_CHOICE_LABEL_SUFFIX,
          metadata.getChoices().get(i)));
      textView.append("\n");
    }
    
    textView.append("\n\n");
    
    textView.appendElement(new FormElement(ElementType.BUTTON, PREV_SUBMIT_POLL_BUTTON,
        "Submit Poll"));
  }

  /**
   * Sets/updates the poll question in the preview. 
   * 
   * @param question the new poll question.
   */
  public void setQuestion(String question) {
    FormView formView = blip.getDocument().getFormView();
    FormElement questionLabel = formView.getFormElement(PREV_QUESTION_LABEL);
    questionLabel.setValue(question);
    formView.replace(questionLabel);
  }

  /**
   * Dynamically updates the list of choices in the preview. In order to do
   * this as the user types, added and removed choices must be detected and
   * handled appropriately.
   * 
   * @param choices the new list of choices.
   */
  public void setChoices(List<String> choices) {
    TextView textView = blip.getDocument();
    FormView formView = textView.getFormView();
    
    // Count existing choice elements
    int existingCount = 0;
    for (FormElement formElement : formView.getFormElements()) {
      if (formElement.getType() == ElementType.LABEL &&
          formElement.getName().startsWith(PREV_CHOICE_PREFIX) &&
          formElement.getName().endsWith(PREV_CHOICE_LABEL_SUFFIX)) {
        ++existingCount;
      }
    }
    
    // Prune extra choices
    if (existingCount > choices.size()) {
      for (int i = existingCount - 1; i >= choices.size(); --i) {
        String choiceLabel = PREV_CHOICE_PREFIX + String.valueOf(i) + PREV_CHOICE_LABEL_SUFFIX;
        int position = textView.getPosition(formView.getFormElement(choiceLabel));

        // Delete the carriage return following the label.
        textView.delete(new Range(position + 1, position + 2));
        // Delete the label.
        formView.delete(choiceLabel);
        // Delete the radio button.
        formView.delete(PREV_CHOICE_PREFIX + String.valueOf(i) + PREV_CHOICE_RADIO_SUFFIX);
      }
      existingCount = choices.size();
    }
    
    // Replace existing labels.
    for (int i = 0; i < existingCount; ++i) {
      String choiceLabel = PREV_CHOICE_PREFIX + String.valueOf(i) + PREV_CHOICE_LABEL_SUFFIX;
      FormElement label = formView.getFormElement(choiceLabel);
      if (!choices.get(i).equals(metadata.getChoices().get(i))) {
        label.setValue(choices.get(i));
        formView.replace(label);
      }
    }
    
    // Add new labels.
    if (existingCount < choices.size()) {
      // Get the position of the last label.
      int lastLabelPosition;
      if (existingCount == 0) {
        lastLabelPosition = textView.getPosition(formView.getFormElement(PREV_CHOICES_RADIOGROUP));
      } else {
        String choiceLabel = PREV_CHOICE_PREFIX + String.valueOf(existingCount - 1) +
            PREV_CHOICE_LABEL_SUFFIX;
        lastLabelPosition = textView.getPosition(formView.getFormElement(choiceLabel)) + 1;
      }
      
      for (int i = existingCount; i < choices.size(); ++i) {
        FormElement radioButton = new FormElement(
            ElementType.RADIO_BUTTON,
            PREV_CHOICES_RADIOGROUP,
            PREV_CHOICE_PREFIX + String.valueOf(i) + PREV_CHOICE_RADIO_SUFFIX);
        FormElement label = new FormElement(
            ElementType.LABEL,
            PREV_CHOICE_PREFIX + String.valueOf(i) + PREV_CHOICE_LABEL_SUFFIX,
            choices.get(i));
        textView.insertElement(lastLabelPosition + 1, radioButton);
        textView.insertElement(lastLabelPosition + 2, label);
        textView.insert(lastLabelPosition + 3, "\n");
        lastLabelPosition = lastLabelPosition + 3;
      }
    }
  }

  /**
   * Checks the to see if the 'submit poll' button is currently clicked. If
   * so, the button is reset into the unclicked/default state.
   * 
   * @return true if the button was clicked, false otherwise.
   */
  public boolean isSubmitPollButtonPressed() {
    FormView formView = blip.getDocument().getFormView();
    FormElement submitPoll = formView.getFormElement(PREV_SUBMIT_POLL_BUTTON);
    
    boolean isPressed = "clicked".equals(submitPoll.getValue());
    if (isPressed) {
      submitPoll.setValue(submitPoll.getDefaultValue());
      formView.replace(submitPoll);
    }
    
    return isPressed;
  }

  /**
   * Returns the currently selected choice in the poll.
   * 
   * @return the currently selected choice in the poll.
   */
  public String getVote() {
    FormView formView = blip.getDocument().getFormView();
    String selectedRadio = formView.getFormElement(PREV_CHOICES_RADIOGROUP).getValue();
    if (selectedRadio != null && !selectedRadio.isEmpty()) {
      // Derive the choice from the radio button's name.
      return selectedRadio.substring(PREV_CHOICE_PREFIX.length(),
          PREV_CHOICE_PREFIX.length() + 1);
    } else {
      return "";
    }
  }
}
