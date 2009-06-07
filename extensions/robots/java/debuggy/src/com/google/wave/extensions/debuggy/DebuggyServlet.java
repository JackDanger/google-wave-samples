package com.google.wave.extensions.debuggy;

import com.google.wave.api.*;
import java.util.List;

@SuppressWarnings("serial")
public class DebuggyServlet extends AbstractRobotServlet {

	
	@Override
	public void processEvents(RobotMessageBundle events) {
		Wavelet wavelet = events.getWavelet();
		TextView textView = wavelet.appendBlip().getDocument();
		
		addLine(textView, "Version", "7");
		
		List<Event> eventsArray = (List<Event>) events.getEvents();
		textView.appendStyledText(createHeader("Events: "));
		for (Event e : eventsArray) {
			textView.append(e.getType().toString());
		}
		textView.append(createNewLine());
		
		addLine(textView, "Wavelet Id", wavelet.getWaveletId());
		addLine(textView, "Wave Id", wavelet.getWaveId());
		
		List<String> participantsList = (List<String>) wavelet.getParticipants();
		textView.appendStyledText(createHeader("Participants"));
		for (String p : participantsList) {
			textView.append(p);
		}
		textView.append(createNewLine());
		
		addLine(textView, "Title", wavelet.getTitle());
		
		textView.appendStyledText(createHeader("Root Blip"));
		addBlip(textView, wavelet.getRootBlip());
		textView.appendStyledText(createRule());
		
		List<Blip> blipsList = (List<Blip>) wavelet.getRootBlip().getChildren();
		textView.appendStyledText(createHeader("Child Blips"));
		for (Blip b : blipsList) {
			addBlip(textView, b);
			textView.appendStyledText(createRule());
		}
	}
	
	public void addBlip(TextView textView, Blip blip) {
		textView.append("Id:" + blip.getBlipId() + "|Creator:" + blip.getCreator() + "|");
		
		String creator = blip.getCreator();
		
		if (!creator.equals("debuggybot@appspot.com")) {
			addLine(textView, "Blip Text", blip.getDocument().getText());
		}
	}
	
	public void addLine(TextView textView, String title, String content) {
		textView.appendStyledText(createHeader(title + ": "));
		textView.append(content);
		textView.append(createNewLine());
	}
	
	public StyledText createRule() {
		return new StyledText("-----------------", StyleType.BOLD);
	}
	
	public StyledText createHeader(String header) {
		return new StyledText(header, StyleType.BOLD);
	}
	
	public String createNewLine() {
		return "\n";
	}
	
}
