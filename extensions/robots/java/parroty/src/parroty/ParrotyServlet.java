package parroty;

import com.google.wave.api.*;
import com.google.wave.api.event.*;

public class ParrotyServlet extends AbstractRobot {

  @Override
  protected String getRobotName() {
    return "Parroty";
  }

  @Override
  protected String getRobotAvatarUrl() {
    return "http://code.google.com/apis/wave/extensions/robots/images/robot_avatar.png";
  }

  @Override
  protected String getRobotProfilePageUrl() {
    return "http://code.google.com/apis/wave/extensions/robots/java-tutorial.html";
  }

  @Override
  public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
    event.getWavelet().reply("\nHi everybody!");
  }

  @Override
  public void onWaveletParticipantsChanged(WaveletParticipantsChangedEvent event) {
    for (String newParticipant: event.getParticipantsAdded()) {
      event.getWavelet().reply("\nHi : " + newParticipant);
    }
  }
}