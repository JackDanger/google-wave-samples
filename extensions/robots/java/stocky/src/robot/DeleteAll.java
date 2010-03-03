package robot;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DeleteAll extends HttpServlet {
  private static final Logger log = Logger.getLogger(DeleteAll.class.getName());
  @Inject
  private PersistenceManagerFactory pmf = null;

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      PersistenceManager pm = pmf.getPersistenceManager();
      Query query = pm.newQuery(BlipEntry.class);
      List<BlipEntry> list = (List<BlipEntry>) query.execute();
      pm.deletePersistentAll(list);
    } catch (Exception e) {
      e.printStackTrace();
      log.warning(e.getMessage());
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }

}
