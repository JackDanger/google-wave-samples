package robot;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetUserInfo extends HttpServlet {
  private static final Logger log = Logger.getLogger(GetUserInfo.class.getName());
  @Inject private Util util;

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String callback = req.getParameter("callback");
      String name = req.getParameter("user");
      boolean hasUserInfo = util.getUserInfo(name) != null;
      resp.setContentType("text/javascript");
      resp.getWriter().println(String.format(callback + "(%b)", hasUserInfo));
    } catch (Exception e) {
      e.printStackTrace();
      log.warning(e.getMessage());
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }
}