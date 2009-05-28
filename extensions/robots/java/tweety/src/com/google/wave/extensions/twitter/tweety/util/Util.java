// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.twitter.tweety.util;

import com.google.wave.api.Blip;
import com.google.wave.api.FormElement;

/**
 * Utility class.
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public class Util {

  /**
   * Checks whether the given input string is empty or not.
   *
   * @param string
   * @return true If the input string is null, empty, or contains only
   *     whitespaces.
   */
  public static boolean isEmpty(String string) {
    return string == null || string.trim().isEmpty();
  }
  
  /**
   * Checks whether a button in a blip was clicked or not. This method will
   * reset the state of the button to be "unclicked" at the end of the method
   * call.
   * 
   * @param blip The blip where the button resides.
   * @param buttonId The id of the button that we want to check.
   * @return true If the user just clicked on the button.
   */
  public static boolean isButtonClicked(Blip blip, String buttonId) {
    if (blip != null && blip.getDocument().getElements() != null) {
      FormElement button = blip.getDocument().getFormView().getFormElement(buttonId);
      if (button != null && "clicked".equals(button.getValue())) {
        button.setValue(button.getDefaultValue());
        blip.getDocument().getFormView().replace(button);
        return true;
      }
    }
    return false;
  }
}
