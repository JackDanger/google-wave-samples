package robot;

import java.util.logging.Logger;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.wave.api.Blip;
import com.google.wave.api.Gadget;
import com.google.wave.api.GadgetView;
import com.google.wave.api.Wavelet;
import com.google.wave.api.StyleType;
import com.google.wave.api.Range;

import org.json.JSONObject;
import org.json.JSONArray;

import javax.jdo.JDOHelper;
import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public class Util {

  private static final Logger log =
      Logger.getLogger(Util.class.getName());

  private static PersistenceManagerFactory pmf = null;
  
  public static PersistenceManagerFactory getPersistenceManagerFactory() {
    if (pmf == null) {
      pmf = JDOHelper.getPersistenceManagerFactory("transactions-optional"); 
    }
    return pmf;
  }

  public static void save(BlipEntry entry) {    
    
    PersistenceManager pm = 
        Util.getPersistenceManagerFactory().getPersistenceManager();

    pm.makePersistent(entry);

    pm.close();
  }

  // TODO
  public static BlipEntry contains(Blip blip) {

    String blipId = blip.getBlipId();
    Wavelet wavelet = blip.getWavelet();
    String waveletId = wavelet.getWaveletId();
    String waveId = wavelet.getWaveId();

    BlipEntry entry = null;

    PersistenceManager pm = 
        Util.getPersistenceManagerFactory().getPersistenceManager();

    String filters = 
        "waveId == waveId_ && waveletId == waveletId_ && blipId == blipId_";
    Query query = pm.newQuery(BlipEntry.class, filters);
    query.declareParameters("String waveId_, String waveletId_, String blipId_");
    List<BlipEntry> list = (List<BlipEntry>) 
        query.executeWithArray(new Object[] {waveId, waveletId, blipId});
    
    if (list.size() > 0) {
      entry = list.get(0);
      entry = pm.detachCopy(entry);
    }    
    
    pm.close();

    return entry;
  }
  
  public static double getStockPrice(String symbol) {

    double price = -1.0;

    String url = "http://hgh.appspot.com/q?s=" + symbol;

    try {
      String data = fetchUrl(url);

      price = Double.parseDouble(data);

    } catch (Exception e) {
      log.warning(e.getMessage());
      e.printStackTrace();
    }   
    
    return price;
  }

  public static String fetchUrl(String url) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) 
        new URL(url).openConnection();

    BufferedReader reader = 
        new BufferedReader(new InputStreamReader(connection.getInputStream()));
    StringBuilder result = new StringBuilder();
    try {
      String inputLine;
      while ((inputLine = reader.readLine()) != null) {
        result.append(inputLine);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return result.toString();
  }

  public static void boldenPrice(String text, Blip blip) {
    int parenOpen = text.indexOf("(");
    int parenClose = text.indexOf(")");
    blip.getDocument().setAnnotation(
        new Range(parenOpen, parenClose + 1),
        StyleType.BOLD.toString(),
        StyleType.BOLD.toString());
  }

  public Gadget addGadget(Wavelet wavelet, String url) {
    GadgetView gv = wavelet.appendBlip().getDocument().getGadgetView();
    Gadget g = new Gadget(url);
    gv.append(g);
    return g;
  }
}
