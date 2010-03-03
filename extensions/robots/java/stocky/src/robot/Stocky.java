package robot;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Annotation;
import com.google.wave.api.Annotations;
import com.google.wave.api.Blip;
import com.google.wave.api.BlipContentRefs;
import com.google.wave.api.Wavelet;
import com.google.wave.api.event.DocumentChangedEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;

@Singleton
public class Stocky extends AbstractRobot {
  private static final Logger LOG = Logger.getLogger(Stocky.class.getName());

  private Injector injector = null;
  private Util util = null;

  private static String SANDBOX_DOMAIN = "wavesandbox.com";
  private static String PREVIEW_DOMAIN = "googlewave.com";

  private static String PREVIEW_RPC_URL = "http://gmodules.com/api/rpc";
  private static String SANDBOX_RPC_URL = "http://sandbox.gmodules.com/api/rpc";

  private static String OAUTH_TOKEN = null;
  private static String OAUTH_KEY = null;
  private static String OAUTH_SECRET = null;
  private static String SECURITY_TOKEN = null;

  private static final boolean WHITELIST_ONLY = false;
  private static final boolean DEBUG_ON = true;
  private static List<String> whitelist = new ArrayList<String>();
  static {
    whitelist.add("austin@wavesandbox.com");
  }
  
  private String domain = SANDBOX_DOMAIN;
  
  @Inject
  public Stocky(Injector injector, Util util) {
    this.injector = injector;
    this.util = util;
    
    OAUTH_TOKEN = System.getProperty("OAUTH_TOKEN");
    OAUTH_KEY = System.getProperty("OAUTH_KEY");
    OAUTH_SECRET = System.getProperty("OAUTH_SECRET");
    SECURITY_TOKEN = System.getProperty("SECURITY_TOKEN");    
    
    initOauth();
  }

  public void initOauth() {
    setupVerificationToken(OAUTH_TOKEN, SECURITY_TOKEN);

    if (domain.equals(SANDBOX_DOMAIN)) {
      setupOAuth("google.com:" + OAUTH_KEY, OAUTH_SECRET, SANDBOX_RPC_URL);
    }
    if (domain.equals(PREVIEW_DOMAIN)) {
      setupOAuth("google.com:" + OAUTH_KEY, OAUTH_SECRET, PREVIEW_RPC_URL);
    }

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

  @Override
  public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
  }

  @Override
  public void onDocumentChanged(DocumentChangedEvent event) {
    if (WHITELIST_ONLY && !whitelist.contains(event.getModifiedBy())) {
      return; // Ignore non-whitelisted actions
    }

    Blip blip = event.getBlip();

    Annotations annotations = event.getBlip().getAnnotations();
    for (Annotation annotation : annotations.asList()) {
      String name = annotation.getName();
      String value = annotation.getValue();
      if (name.equals("stocky") && value.equals("_new_")) {
        int startIndex = annotation.getRange().getStart();

        BlipContentRefs blipContentRefs = blip.range(annotation.getRange().getStart(), annotation
            .getRange().getEnd());
        blipContentRefs.annotate("stocky", "_done_");

        String annotatedContent = blipContentRefs.value().getText();
        LOG.info("annotatedContent = " + annotatedContent);
        
        //String symbol = annotatedContent.substring(1, annotatedContent.length() - 1).toUpperCase();
        String symbol = annotatedContent.toUpperCase();
        LOG.info("stocky finds new symbol: " + symbol);

        // Replace the content with new and delete the "stocky" annotation.
        String price = String.format("$%.2f", util.getStockPrice(symbol));
        String newContent = String.format("%s[%s] ", symbol, price);
        blipContentRefs = blipContentRefs.replace(newContent);

        BlipContentRefs symbolContentRef = blip.range(startIndex, startIndex + symbol.length());
        symbolContentRef.annotate("style/color", "rgb(50,205,50)");
        symbolContentRef.annotate("style/fontWeight", "bold");

        BlipContentRefs priceContentRef = blip.range(startIndex + symbol.length() + 1, startIndex
            + symbol.length() + 1 + price.length());
        priceContentRef.annotate("style/color", "rgb(50,205,50)");
        priceContentRef.annotate("style/fontSize", "0.8em");
        priceContentRef.annotate("style/color", "rgb(255,0,0)");
      }
    }
  }

  private void debug(Wavelet wavelet, String msg) {
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

  private String getServerName() {
    @RequestScoped
    class ServletHelper {
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

    ServletHelper servletHelper = injector.getInstance(ServletHelper.class);
    return servletHelper.getRequest().getServerName();
  }
}
