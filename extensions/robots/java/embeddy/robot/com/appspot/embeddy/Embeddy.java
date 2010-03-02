package robot;

import java.util.logging.Logger;

import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Blip;
import com.google.wave.api.Gadget;
import com.google.wave.api.event.GadgetStateChangedEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;

public class Embeddy extends AbstractRobot {
  
  private static final Logger LOG = Logger.getLogger(Embeddy.class.getName());
  
  private static final String ROBOT_ID = "embeddy";
  
  private String gadgetUrl = String.format("http://%s.appspot.com/gadget/content.xml", ROBOT_ID);
  
  @Override
  public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {    
    Gadget gadget = new Gadget(gadgetUrl);
    event.getWavelet().getRootBlip().append(gadget);    
    String id = event.getWavelet().getDomain() + "!" + event.getWavelet().getWaveId().getId();    
    gadget.setProperty("id", id);
  }  
  
  @Override
  public void onGadgetStateChanged(GadgetStateChangedEvent event) {
    Blip blip = event.getBlip();

    Gadget gadget = Gadget.class.cast(blip.at(event.getIndex()).value());
    if (!gadget.getUrl().startsWith(gadgetUrl)) {
      return;
    }
    
    if (gadget.getProperty("is-closed").equals("true")) {
      blip.at(event.getIndex()).delete();
    }
  }  
  
  @Override
  protected String getRobotName() {
    return this.getClass().getSimpleName();
  }
}
