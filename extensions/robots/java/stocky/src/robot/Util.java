package robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Util {
  private static final Logger LOG = Logger.getLogger(Util.class.getName());

  private PersistenceManagerFactory pmf = null;

  @Inject
  public Util(PersistenceManagerFactory pmf) {
    this.pmf = pmf;
  }

  public PersistenceManager getPm() {
    return pmf.getPersistenceManager();
  }

  public BlipEntry save(BlipEntry entry) {
    PersistenceManager pm = getPm();
    try {
      entry = pm.makePersistent(entry);
      entry = pm.detachCopy(entry);
    } finally {
      pm.close();
    }

    return entry;
  }

  public BlipEntry getBlipEntry(String domain, String waveId) {
    PersistenceManager pm = getPm();
    BlipEntry entry = null;

    try {
      Query query = pm.newQuery(BlipEntry.class);
      query.declareParameters("String domain_, String waveId_");
      String filters = "domain == domain_ && waveId == waveId_";
      query.setFilter(filters);
      List<BlipEntry> entries = (List<BlipEntry>) query.execute(domain, waveId);
      if (entries.size() > 0) {
        entry = pm.detachCopy(entries.get(0));
      }
    } finally {
      pm.close();
    }

    return entry;
  }

  public double getStockPrice(String symbol) {
    double price = -1.0;

    String url = "http://hgh.appspot.com/q?s=" + symbol;
    try {
      String data = fetchUrl(url);
      price = Double.parseDouble(data);
    } catch (Exception e) {
      LOG.warning(e.getMessage());
      e.printStackTrace();
    }
    return price;
  }

  public boolean isValidSymbol(String symbol) {
    String re = "[a-zA-Z]+";
    Pattern p = Pattern.compile(re, Pattern.DOTALL);
    Matcher m = p.matcher(symbol);
    return m.matches();
  }

  public String fetchUrl(String url) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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

  public String getPostBody(HttpServletRequest req) throws IOException {
    InputStream is = req.getInputStream();

    StringBuffer body = new StringBuffer();
    String line = null;
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    while ((line = br.readLine()) != null) {
      body.append(line);
      body.append("\n");
    }
    return body.toString();
  }
}
