package robot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gdata.client.Query;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.client.projecthosting.ProjectHostingService;
import com.google.gdata.data.HtmlTextConstruct;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.projecthosting.IssueCommentsEntry;
import com.google.gdata.data.projecthosting.IssueCommentsFeed;
import com.google.gdata.data.projecthosting.IssuesEntry;
import com.google.gdata.data.projecthosting.IssuesFeed;
import com.google.gdata.data.projecthosting.Label;
import com.google.gdata.data.projecthosting.Owner;
import com.google.gdata.data.projecthosting.Status;
import com.google.gdata.data.projecthosting.Username;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class ProjectHostingHelper {
  private static final Logger log = Logger.getLogger(ProjectHostingHelper.class.getName());
  private ProjectHostingService service = null;

  public ProjectHostingHelper() {
    this.service = new ProjectHostingService("robot-test");
  }

  public String getAuthSubUrl(String next, String scope, boolean secure, boolean session) {
    String url = null;
    url = AuthSubUtil.getRequestUrl(next, scope, secure, session);
    return url;
  }

  public boolean login(String username, String password) {
    boolean success = true;
    try {
      this.service.setUserCredentials(username, password);
    } catch (AuthenticationException e) {
      log.log(Level.SEVERE, "ProjectHostingHelper constructor failed", e);
      success = false;
    }
    return success;
  }

  public void login(String authSubToken) {
    this.service.setAuthSubToken(authSubToken);
  }

  public IssuesEntry createIssue(String project, String title, String content,
      Iterator<String> labels, String user) throws ServiceException, IOException {
    IssuesEntry entry = new IssuesEntry();
    entry.setTitle(new PlainTextConstruct(title));
    entry.setContent(new HtmlTextConstruct(content));
    entry.setStatus(new Status("New"));
    Owner owner = new Owner();
    owner.setUsername(new Username(user));

    Person author = new Person();
    author.setName(user);
    entry.getAuthors().add(author);

    while (labels.hasNext()) {
      String label = labels.next();
      if (!label.trim().equals("")) {
        entry.addLabel(new Label(label));
      }
    }

    URL postUrl = new URL("http://code.google.com/feeds/issues/p/" + project + "/issues/full");
    return service.insert(postUrl, entry);
  }

  public IssueCommentsFeed getComments(String project, int issueId, int startIndex) {
    IssueCommentsFeed resultFeed = null;
    try {
      URL feedUrl = new URL("http://code.google.com/feeds/issues/p/" + project + "/issues/"
          + issueId + "/comments/full");
      ;
      Query query = new Query(feedUrl);
      query.setStartIndex(startIndex);
      resultFeed = service.getFeed(query, IssueCommentsFeed.class);
    } catch (MalformedURLException e) {
      log.log(Level.SEVERE, "getComments() failed", e);
    } catch (IOException e) {
      log.log(Level.SEVERE, "getComments() failed", e);
    } catch (ServiceException e) {
      log.log(Level.SEVERE, "getComments() failed", e);
    }
    return resultFeed;
  }

  public IssuesEntry getIssuesEntry(String project, int issueId) {
    IssuesEntry issuesEntry = null;
    URL feedUrl;
    try {
      feedUrl = new URL("http://code.google.com/feeds/issues/p/" + project + "/issues/full?id="
          + issueId);
      IssuesFeed issuesFeed = service.getFeed(feedUrl, IssuesFeed.class);
      issuesEntry = issuesFeed.getEntries().get(0);
    } catch (MalformedURLException e) {
      log.log(Level.SEVERE, "getIssuesEntry() failed", e);
    } catch (IOException e) {
      log.log(Level.SEVERE, "getIssuesEntry() failed", e);
      e.printStackTrace();
    } catch (ServiceException e) {
      log.log(Level.SEVERE, "getIssuesEntry() failed", e);
      e.printStackTrace();
    }
    return issuesEntry;
  }

  public IssueCommentsEntry createComment(String project, int issueId, String name, String content) {
    IssueCommentsEntry entry = new IssueCommentsEntry();
    entry.setContent(new HtmlTextConstruct(content));

    Person author = new Person();
    author.setName(name);
    entry.getAuthors().add(author);

    URL postUrl;
    try {
      postUrl = new URL("http://code.google.com/feeds/issues/p/" + project + "/issues/" + issueId
          + "/comments/full");
      entry = service.insert(postUrl, entry);
    } catch (IOException e) {
      log.log(Level.SEVERE, "submitComment() failed", e);
    } catch (ServiceException e) {
      log.log(Level.SEVERE, "submitComment() failed", e);
    }
    return entry;
  }

  public List<IssuesEntry> getAllIssueEntries(String project) throws ServiceException, IOException {
    URL feedUrl = new URL("http://code.google.com/feeds/issues/p/" + project + "/issues/full");
    IssuesFeed resultFeed = service.getFeed(feedUrl, IssuesFeed.class);
    return resultFeed.getEntries();
  }

  public String getWebIssueEntryUrl(String project, int id) {
    return "http://code.google.com/p/" + project + "/issues/detail?id=" + id;
  }

  public int getCommentEntryId(String id, String projectName, String issueId) {
    return Integer.parseInt(id.replaceAll("http://code.google.com/feeds/issues/p/" + projectName
        + "/issues/" + issueId + "/comments/full/", ""));
  }
}
