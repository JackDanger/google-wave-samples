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

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UpdateIssues extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdateIssues.class.getName());

  @Inject
  private Buggy robot;
  @Inject
  private PersistenceManagerFactory pmf;

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      Query query = pm.newQuery(IssueJdoEntry.class);
      List<IssueJdoEntry> list = (List<IssueJdoEntry>) query.execute();
      Queue queue = QueueFactory.getDefaultQueue();
      for (IssueJdoEntry issueJdoEntry : list) {
        String projectName = issueJdoEntry.getProjectName();
        String robotAddress = robot.getRobotName() + "@appspot.com";

        String domain = issueJdoEntry.getDomain();
        String waveId = issueJdoEntry.getWaveId();
        String waveletId = issueJdoEntry.getWaveletId();
        log.info("wave id = " + waveId);
        log.info("wavelet id = " + waveletId);

        JSONObject json = new JSONObject();
        json.put("domain", domain);
        json.put("waveId", waveId);
        json.put("waveletId", waveletId);
        json.put("robotAddress", robotAddress);
        json.put("projectName", projectName);

        TaskOptions taskOptions = TaskOptions.Builder.url("/updateIssueTask").method(Method.POST)
            .payload(json.toString());

        queue.add(taskOptions);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    } finally {
      pm.close();
    }
  }
}