package com.google.wave.extensions.hellowave;

import com.google.wave.api.*;
import java.util.List;

@SuppressWarnings("serial")
public class HelloWaveServlet extends AbstractRobotServlet {

	
	@Override
	public void processEvents(RobotMessageBundle events) {
		Wavelet wavelet = events.getWavelet();
		
		Blip rootBlip = events.getEvents().get(0).getBlip();
		makeCartoonBlip(rootBlip);
	}
	
	public void makeCartoonBlip(Blip blip) {
		String creator = blip.getCreator();
		List<String> contributors = blip.getContributors();
		if (!contributors.contains("rusty@gwave.com")) {
			
			String text = blip.getDocument().getText();
			Image image = new Image();
			image.setUrl(makeCartoonUrl(text, creator));
			
			blip.getDocument().delete();
			blip.getDocument().appendElement(image);
		} 
	}
	
	public String makeCartoonUrl(String text, String creator) {
		String base = "http://www.google.com/chart?chst=d_bubble_texts_big&chld=bb|" + makeColor(creator) + "|FFFFFF";
		base += "|" + text.replace(" ", "+");
		
		return base;
	}
	
	public String makeColor(String name) {
		String hex[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
		String color = "";
		for (int i = 0; i < Math.min(name.length(), 6); i++) {
			char code = name.charAt(i);
			int scaled = code % 16;
			color += hex[scaled];
		}
		return color;
	}
	
}
