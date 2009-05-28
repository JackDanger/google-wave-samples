// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.twitter.tweety.controller;

import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.FormView;
import com.google.wave.api.TextView;
import com.google.wave.extensions.twitter.tweety.model.TwitterWave;
import com.google.wave.extensions.twitter.tweety.util.Util;

/**
 * A controller for the login flow. It is responsible for rendering the login
 * form on a blip, and storing the credentials in Google App Engine data store.
 *
 * This controller renders the login form in the following format:
 * <pre>
 * Sign into Twitter
 * Twitter username:
 * [Input box to enter the username]
 * Twitter password:
 * [Input box to enter the password]
 * Twitter search:
 * [Input box to enter the search term]
 * [Button to login]
 * </pre>
 *
 * @author mprasetya@google.com (Marcel Prasetya)
 */
public class LoginController {

  /**
   * The element id of the login button.
   */
  private static final String LOGIN_BUTTON_ID = "login_button";

  /**
   * The caption of the login button.
   */
  private static final String LOGIN_BUTTON_CAPTION = "Sign into Twitter";

  /**
   * The element id of the password label.
   */
  private static final String PASSWORD_LABEL_ID = "password_label";
  
  /**
   * The caption of the password label.
   */
  private static final String PASSWORD_LABEL_CAPTION = "Twitter password:";

  /**
   * The element id of the search label.
   */
  private static final String SEARCH_LABEL_ID = "search_label";
  
  /**
   * The caption of the search label.
   */
  private static final String SEARCH_LABEL_CAPTION = "Twitter search:";
  
  /**
   * The element id of the search input box.
   */
  private static final String SEARCH_INPUT_ID = "search_input";

  /**
   * The element id of the username label.
   */
  private static final String USERNAME_LABEL_ID = "username_label";
  
  /**
   * The caption of the username label.
   */
  private static final String USERNAME_LABEL_CAPTION = "Twitter username:";
  
  /**
   * The element id of the username input box.
   */
  private static final String USERNAME_INPUT_ID = "username_input";

  /**
   * The blip to render the login form.
   */
  private Blip blip;
  
  /**
   * Constructs a login controller based on the given {@link Blip}.
   *
   * @param blip A blip in a Twitter Wave.
   */
  public LoginController(Blip blip) {
    this.blip = blip;
  }

  /**
   * Renders the login form.
   */
  public void renderForm() {
    TextView document = blip.getDocument();
    
    // Append the username input box.
    document.append("\n\n");
    document.appendElement(new FormElement(
        ElementType.LABEL,
        USERNAME_LABEL_ID,
        USERNAME_LABEL_CAPTION));
    document.appendElement(new FormElement(
        ElementType.INPUT,
        USERNAME_INPUT_ID,
        ""));
    
    // Append the password input box.
    document.appendElement(new FormElement(
        ElementType.LABEL,
        PASSWORD_LABEL_ID,
        PASSWORD_LABEL_CAPTION));
    document.appendElement(new FormElement(
        ElementType.PASSWORD,
        "",
        ""));

    // Append the search input box.
    document.appendElement(new FormElement(
        ElementType.LABEL,
        SEARCH_LABEL_ID,
        SEARCH_LABEL_CAPTION));    
    document.appendElement(new FormElement(
        ElementType.INPUT,
        SEARCH_INPUT_ID,
        ""));
    
    // Append the login button.
    document.append("\n");
    document.appendElement(new FormElement(
        ElementType.BUTTON,
        LOGIN_BUTTON_ID,
        LOGIN_BUTTON_CAPTION));
  }
  
  /**
   * Checks whether the login button is clicked or not.
   *
   * @return A {@code true} if the login button is clicked.
   */
  public boolean isButtonClicked() {
    return Util.isButtonClicked(blip, LOGIN_BUTTON_ID);
  }
  
  /**
   * Populates the given {@link TwitterWave} object based on the login form.
   *
   * @param twitterWave The Twitter Wave object that holds the metadata, such
   *     as username, password, and search term.
   */
  public void populateTwitterWave(TwitterWave twitterWave) {
    // Get the username.
    FormView formView = blip.getDocument().getFormView(); 
    String username = formView.getFormElement(USERNAME_INPUT_ID).getValue();
    
    // Get the password.
    String password = "";
    for (FormElement element : formView.getFormElements()) {
      if (element.getType() == ElementType.PASSWORD) {
        password = element.getValue();
      }
    }
    
    // Get the search query.
    String searchQuery = formView.getFormElement(SEARCH_INPUT_ID).getValue();
    
    // Store username and password in TwitterWave.
    twitterWave.setUsername(username);
    twitterWave.setPassword(password);
    twitterWave.setSearchQuery(searchQuery);
    twitterWave.setInSearchMode(!Util.isEmpty(searchQuery));
  }
}
