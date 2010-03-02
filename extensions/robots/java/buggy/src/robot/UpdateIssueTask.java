package robot;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.data.TextContent;
import com.google.gdata.data.projecthosting.IssueCommentsEntry;
import com.google.gdata.data.projecthosting.IssueCommentsFeed;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

@Singleton
public class UpdateIssueTask extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(UpdateIssueTask.class.getName());
  @Inject
  private Buggy robot;
  @Inject
  private Util util;
  @Inject
  private ProjectHostingHelper projectHostingHelper;

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String json = util.getPostBody(req);
    JSONObject jsonObj;
    try {
      jsonObj = new JSONObject(json);
      String domain = jsonObj.getString("domain");
      String waveId = jsonObj.getString("waveId");
      String waveletId = jsonObj.getString("waveletId");
      String robotAddress = jsonObj.getString("robotAddress");
      String projectName = jsonObj.getString("projectName");
      LOG.info("wave id = " + waveId);
      LOG.info("wavelet id = " + waveletId);
      LOG.info("robot address = " + robotAddress);
      LOG.info("project name = " + projectName);

      IssueJdoEntry issueJdoEntry = util.getIssueJdoEntry(waveId);

      Wavelet wavelet = robot.fetchWavelet(new WaveId(domain, waveId), new WaveletId(domain,
          waveletId), projectName, robot.getRpcServerUrl());
      LOG.info("fetched wavelet id: " + wavelet.getWaveId().getId());
      
      
      IssueCommentsFeed issueCommentsFeed = projectHostingHelper.getComments(issueJdoEntry
          .getProjectName(), issueJdoEntry.getIssueId(), issueJdoEntry.getLocalCommentsCount() + 1);

      if (issueCommentsFeed.getTotalResults() != issueJdoEntry.getLocalCommentsCount()) {
        // Update local comments count.
        issueJdoEntry.setLocalCommentsCount(issueCommentsFeed.getTotalResults());

        util.persistJdo(issueJdoEntry);

        List<IssueCommentsEntry> comments = issueCommentsFeed.getEntries();
        for (IssueCommentsEntry entry : comments) {
          int commentId = projectHostingHelper.getCommentEntryId(entry.getId(), issueJdoEntry
              .getProjectName(), issueJdoEntry.getIssueId() + "");
          TextContent textContent = (TextContent) entry.getContent();
          String content = null;
          if (textContent != null && textContent.getContent() != null) {
            HtmlTextConstruct htmlConstruct = (HtmlTextConstruct) textContent.getContent();
            content = htmlConstruct.getHtml();
          }
          String author = entry.getAuthors().get(0).getName();

          Blip newBlip = wavelet.reply("\n");
          newBlip.append("Comment #" + commentId + " from " + author);
          newBlip.append("\n\n" + content);
          robot.submit(wavelet, robot.getRpcServerUrl());
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}