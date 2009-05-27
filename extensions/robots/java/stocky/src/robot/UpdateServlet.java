// Copyright 2009 Google Inc. All Rights Reserved.

package robot;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Blip;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.Wavelet;
import com.google.wave.api.StyleType;
import com.google.wave.api.Range;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.Query;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class UpdateServlet extends AbstractRobotServlet {

  private static final Logger log =
      Logger.getLogger(UpdateServlet.class.getName());

  @SuppressWarnings("unchecked")
  @Override
  public void processEvents(RobotMessageBundle robotMessageBundle) {
    log.warning("cron job start!");

    PersistenceManager pm = 
        Util.getPersistenceManagerFactory().getPersistenceManager();

    List<BlipEntry> entries = 
        (List<BlipEntry>) pm.newQuery(BlipEntry.class).execute();
    
    for (BlipEntry entry : entries) {
      String waveId = entry.getWaveId();
      String waveletId = entry.getWaveletId();
      String blipId = entry.getBlipId();
      String text = entry.getText();
            
      Blip blip = robotMessageBundle.getBlip(waveId, waveletId, blipId);

      log.warning("text: " + text);
      
      String replacement = process(text);

      blip.getDocument().replace(replacement);
      //Util.boldenPrice(replacement, blip);
    }

    pm.close();
    log.warning("cron job end!");
  }

  public String process(String text) {

    // test for pattern
    Pattern p = null;
    Matcher m = null;
    String re ="([a-zA-Z]+) \\(([0-9.]+)\\)";
    String whole = null;
    String symbol = null;
    StringBuffer sb = new StringBuffer();
    double price = -1;

    p = Pattern.compile(re, Pattern.DOTALL);
    m = p.matcher(text);

    while (m.find()) {
      whole = m.group(0);
      symbol = m.group(1);
      price = Double.parseDouble(m.group(2));     
      
      double newPrice = Util.getStockPrice(symbol);

      m.appendReplacement(sb, symbol + " (" + newPrice + ")");
    }       
    m.appendTail(sb);
    
    log.warning("new text: " + sb.toString());

    return sb.toString();
  }

}