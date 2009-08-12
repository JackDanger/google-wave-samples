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

import com.google.wave.api.ProfileServlet;

/**
 * Servlet that handles profile related requests from Google Wave. This servlet
 * is registered at {@code /_wave/robot/profile}.
 * 
 * By default, the servlet responds with a JSON string that describes the Robot:
 * <pre>
 * {"profileUrl":"http://regexey.appspot.com/about",
 *  "imageUrl":"http://regexey.appspot.com/regexey.ico",
 *  "name":"Regexey"}
 * </pre>
 * 
 * @author elizabethford@google.com (Elizabeth Ford)
 */
public class RegexeyProfileServlet extends ProfileServlet {
  /**
   * Returns the url of the robot's avatar.
   * 
   * @return the url of the robot's avatar.
   */
  @Override
  public String getRobotAvatarUrl() {
    return "http://regexey.appspot.com/regexey.ico";
  }
  
  /**
   * Returns the robot's name.
   * 
   * @return the robot's name.
   */
  @Override
  public String getRobotName() {
    return "Regexey";
  }
  
  /**
   * Returns the url of the robot's profile page.
   * 
   * @return the url of the robot's profile page.
   */
  @Override
  public String getRobotProfilePageUrl() {
    return "http://regexey.appspot.com/about";
  }  
}
