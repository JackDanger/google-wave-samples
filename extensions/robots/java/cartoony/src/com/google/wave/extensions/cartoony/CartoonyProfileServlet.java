// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.wave.extensions.cartoony;

import com.google.wave.api.ProfileServlet;

/**
* A servlet that is used to fetch the profile information for Cartoony.
* 
*/
public class CartoonyProfileServlet extends ProfileServlet {

@Override
public String getRobotAvatarUrl() {
	return "http://www.google.com/chart?chst=d_bubble_texts_big&chld=bb|01D5C1|FFFFFF|:-)";
}

@Override
public String getRobotName() {
	return "Cartoony";
}

@Override
public String getRobotProfilePageUrl() {
	return "http://www.cartoons.com";
}
}