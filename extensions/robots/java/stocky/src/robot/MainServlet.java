package robot;

import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.Wavelet;
import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.StyleType;
import com.google.wave.api.Range;

import java.util.logging.Logger;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainServlet extends AbstractRobotServlet {

  private static final Logger log =
      Logger.getLogger(MainServlet.class.getName());

  private final String BOT_NAME = "stocky-wave@appspot.com";
  private final String INSTRUCTION =  
      "Get stock price by prefixing your stock symbol with \"$\".";

  @Override
  public void processEvents(RobotMessageBundle robotMessageBundle) {     
    
    Wavelet wavelet = robotMessageBundle.getWavelet();
    
    if (robotMessageBundle.wasParticipantAddedToWave(BOT_NAME)) {
      wavelet.setTitle(INSTRUCTION);
    }         

    for (Event event : robotMessageBundle.getBlipSubmittedEvents()) {
      Blip blip = event.getBlip();
      String text = null;

      if (blip.getBlipId().equals(wavelet.getRootBlipId())) {
	      // this is the root blip, use getTitle() to get its content
        
      } else {      
        text = blip.getDocument().getText();

        String replacement = process(text, blip);
       
        blip.getDocument().replace(replacement);
        //Util.boldenPrice(replacement, blip);
      }
    }    
  }

  public String process(String text, Blip blip) {

    // test for pattern
    Pattern p = null;
    Matcher m = null;
    String re ="\\$([a-zA-Z]+)";
    String whole = null;
    String symbol = null;
    StringBuffer sb = new StringBuffer();
    double price = -1;

    p = Pattern.compile(re, Pattern.DOTALL);
    m = p.matcher(text);

    boolean hasStock = false;

    while (m.find()) {
      hasStock = true;

      whole = m.group(0);
      symbol = m.group(1);
      price = Util.getStockPrice(symbol);    

      m.appendReplacement(sb, symbol + " (" + price + ")");      
    }       
    m.appendTail(sb);
    
    String replacement = sb.toString();
    
    if (hasStock) {
      BlipEntry entry = Util.contains(blip);

      if (entry == null) {
        entry = new BlipEntry(blip);
      }

      entry.setText(replacement);
      
      Util.save(entry);
    }

    log.warning("replacement: " + replacement);
      
    return replacement;
  }


}
