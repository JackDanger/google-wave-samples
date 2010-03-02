package robot;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.projecthosting.IssueCommentsEntry;
import com.google.gdata.data.projecthosting.IssuesEntry;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Annotation;
import com.google.wave.api.Annotations;
import com.google.wave.api.Blip;
import com.google.wave.api.BlipContentRefs;
import com.google.wave.api.ElementType;
import com.google.wave.api.FormElement;
import com.google.wave.api.Gadget;
import com.google.wave.api.Line;
import com.google.wave.api.Restriction;
import com.google.wave.api.Tags;
import com.google.wave.api.Wavelet;
import com.google.wave.api.event.AnnotatedTextChangedEvent;
import com.google.wave.api.event.BlipSubmittedEvent;
import com.google.wave.api.event.DocumentChangedEvent;
import com.google.wave.api.event.FormButtonClickedEvent;
import com.google.wave.api.event.GadgetStateChangedEvent;
import com.google.wave.api.event.WaveletBlipCreatedEvent;
import com.google.wave.api.event.WaveletBlipRemovedEvent;
import com.google.wave.api.event.WaveletCreatedEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;

@Singleton
public class Buggy extends AbstractRobot {
  private Injector injector = null;
  private Util util = null;
  private ProjectHostingHelper projectHostingHelper = null;

  private static final Logger LOG = Logger.getLogger(Buggy.class.getName());

  private static String SANDBOX_DOMAIN = "wavesandbox.com";
  private static String PREVIEW_DOMAIN = "googlewave.com";

  private String domain = null;
  
  private static String PREVIEW_RPC_URL = "http://gmodules.com/api/rpc";
  private static String SANDBOX_RPC_URL = "http://sandbox.gmodules.com/api/rpc";

  private static final boolean DEBUG_MODE = false;
  private static final String DEFAULT_ISSUE_TITLE = "DEFAULT ISSUE TITLE";
  private static final String HIGHLIGHT_ANNOTATION_NAME = "buggy-wave";
  private static final String NEW_IDS_DOC_NAME = "_new_ids_";
  private static final String ISSUE_LINK_ACHOR_TEXT = "[ issue link ]";

  private static String OAUTH_TOKEN = null;
  private static String OAUTH_KEY = null;
  private static String OAUTH_SECRET = null;
  private static String SECURITY_TOKEN = null;

  @Inject
  public Buggy(Injector injector, Util util, ProjectHostingHelper projectHostingHelper) {
    this.injector = injector;
    this.util = util;
    this.projectHostingHelper = projectHostingHelper;
    this.domain = SANDBOX_DOMAIN;
    
    OAUTH_TOKEN = System.getProperty("OAUTH_TOKEN");
    OAUTH_KEY = System.getProperty("OAUTH_KEY");
    OAUTH_SECRET = System.getProperty("OAUTH_SECRET");
    SECURITY_TOKEN = System.getProperty("SECURITY_TOKEN");    
    
    initOauth();
  }

  public void initOauth() {
    setupVerificationToken(OAUTH_TOKEN, SECURITY_TOKEN);   
    setupOAuth(OAUTH_KEY, OAUTH_SECRET, getRpcServerUrl());
    setAllowUnsignedRequests(true);
  }

  public String getRpcServerUrl() {
    if (this.domain.equals(SANDBOX_DOMAIN)) {
      return SANDBOX_RPC_URL;
    }
    if (this.domain.equals(PREVIEW_DOMAIN)) {
      return PREVIEW_RPC_URL;
    }
    return null;
  }

  private void appendInput(Blip blip, String name, String value) {
    FormElement input = new FormElement(ElementType.INPUT);
    input.setDefaultValue(value);
    input.setValue(value);
    input.setName(name);
    blip.append(input);
  }

  private void appendButton(Blip blip, String name, String value) {
    FormElement button = new FormElement(ElementType.BUTTON);
    button.setDefaultValue(value);
    button.setValue(value);
    button.setName(name);
    blip.append(button);
  }

  private void processComments(final BlipSubmittedEvent event) {
    Wavelet wavelet = event.getWavelet();
    Blip blip = event.getBlip();

    if (blip.getBlipId().equals(wavelet.getRootBlipId())) {
      // This is triggered from root blip, skip comment processing.
      return;
    }

    String user = event.getModifiedBy();
    UserInfo userInfo = util.getUserInfo(user);
    if (userInfo == null) {
      appendAuthSubGadget(blip, user);
    } else {
      submitNewComment(blip, user, userInfo.getAuthSubToken());
    }
  }

  public void submitNewComment(final Blip blip, String user, String authSubToken) {
    // Check if the content of the blip starts with "@comment"
    IssueJdoEntry issueJdoEntry = util.getIssueJdoEntry(blip.getWavelet().getWaveId().getId());
    if (issueJdoEntry != null) {
      String content = blip.getContent();

      // Login using the authsub token.
      projectHostingHelper.login(authSubToken);
      IssueCommentsEntry commentEntry = projectHostingHelper.createComment(issueJdoEntry
          .getProjectName(), issueJdoEntry.getIssueId(), user, content);
      issueJdoEntry.setLocalCommentsCount(issueJdoEntry.getLocalCommentsCount() + 1);
      util.persistJdo(issueJdoEntry);

      int commentId = projectHostingHelper.getCommentEntryId(commentEntry.getId(), issueJdoEntry
          .getProjectName(), issueJdoEntry.getIssueId() + "");
    }
  }

  @Override
  public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
    if (event.getWavelet().getRootBlip().getContent().trim().equals("\n")) {
      // Check if project name is specified in robot address as
      // robotname+project@appspot.com
      if (getProjectName(event) == null) {
        event.getWavelet().setTitle(
            "You must provide the project name to the robot address"
                + "(i.e. robotname+project@appspot.com)");
        return;
      }
      event.getWavelet().setTitle(DEFAULT_ISSUE_TITLE);
      event.getWavelet().getRootBlip().append("\nYour description here.\n\n");

      appendButton(event.getWavelet().getRootBlip(), "createIssues", "Create Issue");
    }
  }

  @Override
  public void onAnnotatedTextChanged(AnnotatedTextChangedEvent event) {
  }

  @Override
  public void onBlipSubmitted(BlipSubmittedEvent event) {
    this.processComments(event);
  }

  @Override
  public void onFormButtonClicked(FormButtonClickedEvent event) {
    Wavelet wavelet = event.getWavelet();
    Blip blip = event.getBlip();
    if (event.getButtonName().equals("createIssue")) {
      String project = wavelet.getDataDocuments().get("issue-tracker-project");
      String title = wavelet.getTitle();
      String content = blip.getContent().replace(title, "");
      int issueId = submitNewIssue(project, event.getModifiedBy(), title, content, wavelet
          .getTags());
      if (issueId > 0) {
        util.persistJdo(new IssueJdoEntry(wavelet.getDomain(), blip, project, issueId));
        // Create a new blip with issue creation info.
        String issueWebLink = projectHostingHelper.getWebIssueEntryUrl(project, issueId);
        wavelet.setTitle(wavelet.getTitle() + " - " + issueWebLink);
        blip.all(ElementType.BUTTON, Restriction.of("name", "createIssue")).delete();
      } else {
        wavelet.reply("\nsubmitNewIssue failed()");
      }
    }
  }

  @Override
  public void onGadgetStateChanged(GadgetStateChangedEvent event) {
    handleAuthSubCompleted(event);
  }

  @Override
  public void onWaveletBlipCreated(WaveletBlipCreatedEvent event) {
  }

  @Override
  public void onWaveletBlipRemoved(WaveletBlipRemovedEvent event) {
    // Blip newBlip = event.getWavelet().appendBlip();
    // newBlip.getDocument().delete();
  }

  @Override
  public void onDocumentChanged(DocumentChangedEvent event) {
    Blip blip = event.getBlip();
    Wavelet wavelet = event.getWavelet();

    if (wavelet.getDataDocuments().contains("issue-tracker-project")) {
      return;
    }

    Annotations annotations = event.getBlip().getAnnotations();
    Iterator<Annotation> iterator = annotations.iterator();
    while (iterator.hasNext()) {
      Annotation annotation = iterator.next();
      String name = annotation.getName();
      String value = annotation.getValue();
      if (name.equals(this.HIGHLIGHT_ANNOTATION_NAME) && value.equals("new")) {

        // Check for authsub token first
        String user = event.getModifiedBy();
        UserInfo userInfo = util.getUserInfo(user);
        if (userInfo == null) {
          appendAuthSubGadget(blip, user);
        } else {
          BlipContentRefs blipContentRefs = blip.range(annotation.getRange().getStart(), annotation
              .getRange().getEnd());
          blipContentRefs.annotate(this.HIGHLIGHT_ANNOTATION_NAME, "done");

          Set<String> participants = new HashSet<String>();
          participants.add(event.getModifiedBy());
          
          Wavelet newWavelet = null;          
          try {
            newWavelet = this.newWave(this.getDomain(), participants, "test", event.getBundle().getProxyingFor(), this.getRpcServerUrl());
            newWavelet.setTitle(DEFAULT_ISSUE_TITLE);

            newWavelet.getDataDocuments().set("issue-tracker-project",
                event.getBundle().getProxyingFor());
            newWavelet.getRootBlip().append(new Line());
            newWavelet.getRootBlip().append(blipContentRefs.value());
            newWavelet.getRootBlip().append(new Line());
            newWavelet.getRootBlip().append(new Line());
            this.appendButton(newWavelet.getRootBlip(), "createIssue", "createIssue");

            // Linkify to new wave
            blipContentRefs.annotate("link/wave", this.domain + "!"
                + newWavelet.getWaveId().getId());
          
            this.submit(newWavelet, this.getRpcServerUrl());
          } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
          }            
          

          break;
        }
      }
    }
  }

  private String getDomain() {
    return this.domain;
  }

  private int submitNewIssue(String project, String author, String title, String content, Tags tags) {
    int issueId = -1;

    UserInfo userInfo = util.getUserInfo(author);
    String authSubToken = userInfo.getAuthSubToken();

    IssuesEntry entry = null;

    try {
      projectHostingHelper.login(authSubToken);
      entry = projectHostingHelper.createIssue(project, title, content, tags.iterator(), author);
    } catch (ServiceException e) {
      LOG.log(Level.WARNING, "ProjectHosting newIssue() failed.", e);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "ProjectHosting newIssue() failed.", e);
    } finally {
      if (entry != null) {
        // Save the issue as IssueJdoEntry, to maintain comment counts for syncing.
        issueId = Integer.parseInt(entry.getId().replaceAll(
            "http://code.google.com/feeds/issues/p/" + project + "/issues/full/", ""));
      }
    }
    return issueId;
  }

  private void appendAuthSubGadget(final Blip blip, final String user) {
    if (blip.first(ElementType.GADGET, Restriction.of("name", "authsubGadget")).values().size() > 0) {
      return;
    }

    String next = "http://" + this.getServerName() + "/authSubHandler?user=" + user;
    String scope = "http://code.google.com/feeds/issues";
    boolean secure = false;
    boolean session = true;
    String authSubUrl = this.projectHostingHelper.getAuthSubUrl(next, scope, secure, session);
    String authSubGadgetUrl = "http://" + this.getServerName() + "/authsub.xml";

    Gadget g = appendGadget(blip, authSubGadgetUrl);
    g.setProperty("name", "authsubGadget");
    g.setProperty("authSubUrl", authSubUrl);
    g.setProperty("user", user);
    g.setProperty("getUserInfoUrl", "http://" + this.getServerName() + "/getUserInfo");
  }

  /**
   * This method responds to a state update from the AuthSub gadget with state
   * key = "authSubCompleted".
   * 
   * @param event
   */
  private void handleAuthSubCompleted(final GadgetStateChangedEvent event) {
    Blip blip = event.getBlip();

    Gadget gadget = Gadget.class.cast(blip.at(event.getIndex()).value());
    if (!gadget.getUrl().startsWith("http://" + this.getServerName() + "/authsub.xml")) {
      return;
    }
    String user = gadget.getProperty("authSubCompleted");
    if (user != null) {
      blip.at(event.getIndex()).delete();

      UserInfo userInfo = util.getUserInfo(user);
      if (userInfo != null) {
        if (blip.getBlipId().equals(event.getWavelet().getRootBlipId())) {
          // This is resulted from root blip, thus it is for submit new issue.
          //submitNewIssue(event);
        }
      } else {
        // TODO(austinchau) Handle error when gadget state update but no authsub
        // token in server.
      }
    }
  }

  private Gadget appendGadget(final Blip blip, String url) {
    String cacheBuster = new Date().getTime() / 17 + "";
    url += "?z=" + cacheBuster;
    if (DEBUG_MODE) {
      url += "&__debugconsole__";
    }
    Gadget gadget = new Gadget(url);
    blip.append(gadget);
    return gadget;
  }

  public Wavelet createNewWavelet(final Wavelet wavelet, final Set<String> participants,
      final String proxyFor) {
    Wavelet newWavelet = null;
    try {
      newWavelet = this.newWave(wavelet.getDomain(), participants, "new_wave", proxyFor,
          getRpcServerUrl());
      newWavelet.submitWith(wavelet);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }
    return newWavelet;
  }

  @Override
  public void onWaveletCreated(WaveletCreatedEvent event) {
  }

  private void saveBlip(Blip blip) {
    util.persistJdo(blip);
  }

  private String getProjectName(final WaveletSelfAddedEvent event) {
    return event.getBundle().getProxyingFor();
  }

  private String getServerName() {
    ServletHelper servletHelper = injector.getInstance(ServletHelper.class);
    return servletHelper.getRequest().getServerName();
  }

  public String getRobotAvatarUrl() {
    return "http://" + getServerName() + "/images/profile.jpg";
  }

  public String getRobotProfilePageUrl() {
    return "http://" + getServerName() + "/_wave/robot/profile";
  }

  @Override
  public String getRobotName() {
    return getServerName().replace(".appspot.com", "");
  }

  @RequestScoped
  private static class ServletHelper {
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    @Inject
    public ServletHelper(HttpServletRequest request, HttpServletResponse response) {
      this.request = request;
      this.response = response;
    }

    public HttpServletRequest getRequest() {
      return this.request;
    }

    public HttpServletResponse getResponse() {
      return response;
    }
  }
}
