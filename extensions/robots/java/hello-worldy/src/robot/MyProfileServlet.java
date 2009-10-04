package robot;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.wave.api.AbstractRobotServlet;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.Wavelet;
import com.google.wave.api.ProfileServlet;

public class MyProfileServlet extends ProfileServlet {

  private static final Logger log =
      Logger.getLogger(MyProfileServlet.class.getName());

  public String getRobotName() {
	return "Hello-Worldy";    
  }
  
  public String getRobotAvatarUrl() {	  
	  return "http://hello-worldy.appspot.com/images/robot.jpg";
  }
  
}
