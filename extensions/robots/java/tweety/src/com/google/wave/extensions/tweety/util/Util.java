package com.google.wave.extensions.tweety.util;

import com.google.wave.api.Event;
import com.google.wave.api.EventType;

import java.util.List;

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
   * @param events A list of events received from Google Wave that needs to be
   *     checked whether it contains form button clicked event or not.
   * @param buttonId The id of the button that we want to check.
   * @return true If the user just clicked on the button.
   */
  public static boolean isButtonClicked(List<Event> events, String buttonId) {
    for (Event event : events) {
      if (event.getType() == EventType.FORM_BUTTON_CLICKED &&
          buttonId.equals(event.getButtonName())) {
        return true;
      }
    }
    return false;
  }
}
