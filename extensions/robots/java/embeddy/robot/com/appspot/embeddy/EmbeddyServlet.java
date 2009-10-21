package com.appspot.embeddy;

import com.google.wave.api.*;

@SuppressWarnings("serial")
public class EmbeddyServlet extends AbstractRobotServlet {
  @Override
  public void processEvents(RobotMessageBundle events) {
    Gadget gadget;
    Wavelet wavelet = events.getWavelet();
    GadgetView gadgets = wavelet.getRootBlip().getDocument().getGadgetView();

    for (Event event : events.getEvents()) {
      switch (event.getType()) {
        case WAVELET_SELF_ADDED:
        gadget = new Gadget("http://embeddy.appspot.com/gadget/content.xml");
        gadgets.append(gadget);
        gadget.setField("id", "'" + wavelet.getWaveId() + "'");
        break;

        case DOCUMENT_CHANGED:
        gadget =
          gadgets.getGadget("http://embeddy.appspot.com/gadget/content.xml");

        if (gadget != null && "true".equals(gadget.getField("is-closed"))) {
          gadgets.delete(gadget);
        }
      }
    }
  }
}
