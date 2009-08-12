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

import com.google.wave.api.*;

/* Wave robot servlet that handles wave events.  Introduces itself when it's
 * added, and responds to new blips with regex processing.
 * @author eford1@gmail.com (Elizabeth Ford)
 */
public class RegexeyServlet extends AbstractRobotServlet {

  @Override
  public void processEvents(RobotMessageBundle bundle) {
    Wavelet wavelet = bundle.getWavelet();

    if (bundle.wasSelfAdded()) {
      Blip blip = wavelet.appendBlip();
      TextView textView = blip.getDocument();
      textView.append("Hi, I'm Regexey!  Right now, I only do find and " +
          "replace.  To try it, just make a new blip with your search regex, " +
          "your replace regex, and the text you want to do a find-and-replace" +
          " on (in that order).  Separate them with newlines.");
    }

    for (Event e: bundle.getEvents()) {
      if (e.getType() == EventType.BLIP_SUBMITTED) {
        //get the blip that has the regexes in it
        Blip blip = e.getBlip();

        //convert the blip to a string
        TextView textView = blip.getDocument();
        String text = textView.getText();
        String newText = RegexeyUtil.process(text);

        //create a new child of the root blip
        Blip newBlip = blip.createChild();
        TextView newView = newBlip.getDocument();

        //put the new text in the new blip
        newView.append(newText);
      }
    }
  }
}
