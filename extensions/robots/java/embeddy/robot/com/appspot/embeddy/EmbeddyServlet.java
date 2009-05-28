package com.appspot.embeddy;

import com.google.wave.api.*;

@SuppressWarnings("serial")
public class EmbeddyServlet extends AbstractRobotServlet {
  @Override
  public void processEvents(RobotMessageBundle events) {
    if (events.wasParticipantAddedToWave("embeddy@appspot.com")) {
      Wavelet wavelet = events.getWavelet();
      Gadget gadget =
        new Gadget("http://embeddy.appspot.com/gadget/content.xml");
      gadget.setField("id-defined", "'" + wavelet.getWaveId() + "'");
      TextView text = wavelet.appendBlip().getDocument();
      text.append("\n\n\n");
      text.getGadgetView().append(gadget);
    }
  }
}
