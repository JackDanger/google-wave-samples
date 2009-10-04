package robot;

import java.util.List;
import java.util.logging.Logger;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.ElementType;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.FormElement;
import com.google.wave.api.FormView;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;
import com.google.wave.api.Wavelet;

public class MainServlet extends AbstractRobotServlet {

  private static final Logger log = Logger.getLogger(MainServlet.class.getName());

  private final String BOT_NAME = "hello-worldy@appspot.com";
  private final String WELCOME = "Hello World!";  
  
  @Override
  public void processEvents(RobotMessageBundle robotMessageBundle) {
    Wavelet wavelet = robotMessageBundle.getWavelet();
    for (Event event : robotMessageBundle.getEvents()) {
      Blip currentBlip = event.getBlip();
      switch(event.getType()) {
        case WAVELET_SELF_ADDED:
          wavelet.setTitle(WELCOME);     
          addButton(event.getBlip());
          break;
        case BLIP_SUBMITTED:          
          if (!isRoot(currentBlip, wavelet)) {
            String text = currentBlip.getDocument().getText();
            wavelet.appendBlip().getDocument().append("[echo] " + text);             
          }                   
          break;
        case WAVELET_PARTICIPANTS_CHANGED:
          for (String participant : event.getAddedParticipants()) {            
            if (!participant.equals(BOT_NAME) && 
                !participant.equals(wavelet.getCreator())) {
              wavelet.appendBlip().getDocument().append("Hi " + participant + "!"); 
            }                        
          }          
          break;            
        case FORM_BUTTON_CLICKED:
          if (event.getButtonName().equals("my_button")) {
            String clicker = event.getModifiedBy();
            wavelet.appendBlip().getDocument().append(clicker + " has clicked the button!");
          }
          FormView formView = currentBlip.getDocument().getFormView();          
          if (formView.getFormElement("my_button") != null) {
            
          }          
          break;             
      }      
    }        
  }
  
  private void addButton(Blip blip) {
    FormElement button = new FormElement();
    button.setType(ElementType.BUTTON);
    button.setName("my_button");
    button.setValue("click me");    
    blip.getDocument().getFormView().append(button);    
  }
  
  private boolean isRoot(Blip blip, Wavelet wavelet) {
    return blip.getBlipId().equals(wavelet.getRootBlipId());
  }
}
