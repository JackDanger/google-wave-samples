/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.wave.extensions.regexey;

import java.util.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the regex find and replace functionality of the Regexey robot.
 *
 * @author eford1@gmail.com (Elizabeth Ford)
 */
class RegexeyUtil {

  private static final Logger LOG =
      Logger.getLogger("com.google.wave.extensions.regexey.RegexeyUtil");

  /**
   * Processing function.
   *
   * @param text from the blip you want to process.  Must have at least
   *    three lines (search line, replace line, text to do find and replace on)
   * @returns Formatted text for the response blip.
   */
  static String process(String text){
    //parse the string into the 2 regexes
    String delims = "\n";
    String[] tokens = text.split(delims);
    String output;
    if(tokens.length < 3) {
      return "I'm sorry, but that was not a valid entry.";
    }
    Pattern pattern;
    try {
      pattern = Pattern.compile(tokens[0]);
      Matcher matcher = pattern.matcher(tokens[2]);
      output = matcher.replaceAll(tokens[1]);
      return "Replaced \"" + tokens[0] + "\" with \"" + tokens[1] +
          "\" in \"" + tokens[2] + "\"\n\n" + output;
    } catch (Exception exception) {
      LOG.warning("Bad regex? " + exception.toString());
      return "I'm sorry, but that may not have been a valid entry.";
    }
  }
}
