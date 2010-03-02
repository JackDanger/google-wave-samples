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

package robot;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthSubHandler extends HttpServlet {
  private static final Logger log = Logger.getLogger(AuthSubHandler.class.getName());
  @Inject private Util util;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    try {
      String token = AuthSubUtil.getTokenFromReply(request.getQueryString());
      String authSubToken = AuthSubUtil.exchangeForSessionToken(token, null);

      String user = request.getParameter("user");
      UserInfo userInfo = new UserInfo(user, authSubToken);
      util.persistUserInfo(userInfo);

      response.setContentType("text/html");
      response.getWriter().println(
          "<html><script>window.onload=function(){setTimeout('self.close()', 0);}</script>" +
          "<body>bye</body></html>");
    } catch (AuthenticationException e) {
      log.log(Level.WARNING, "AuthSubHandler exchangeForSessionToken() failed.", e);
    } catch (GeneralSecurityException e) {
      log.log(Level.WARNING, "AuthSubHandler exchangeForSessionToken() failed.", e);
    }

  }
}
