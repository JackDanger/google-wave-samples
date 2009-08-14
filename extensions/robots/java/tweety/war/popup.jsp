<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="application/xml"%>
<%@ page import="com.google.wave.api.oauth.impl.SingletonPersistenceManagerFactory" %>
<%@ page import="com.google.wave.api.oauth.impl.OAuthUser" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="javax.jdo.PersistenceManagerFactory" %>

<Module>
  <ModulePrefs title="LoginGadget" height="250" width="400">
    <Require feature="dynamic-height"/>
  </ModulePrefs>
<Content type="html">
<![CDATA[<html><head>
<script type="text/javascript" src="http://wave-api.appspot.com/public/wave.js">
</script>
<script type="text/javascript">
// Copyright 2009 Google Inc.  All Rights Reserved.

/**
 * @fileoverview Wave Gadget for handling the OAuth login popup window.
 * @author elizabethford@google.com (Elizabeth Ford)
 */
    <%
    PersistenceManagerFactory pmf = SingletonPersistenceManagerFactory.get();
    String userRecordKey = request.getParameter("key");
    PersistenceManager pm = pmf.getPersistenceManager();
    OAuthUser userProfile = null;
    try {
      userProfile = pm.getObjectById(OAuthUser.class, userRecordKey);
    } catch (Exception e) {
      out.write(e.getMessage());
    } finally {
      pm.close();
    }
    %>

  gadgets.util.registerOnLoadHandler(popup);
  var timer;
  var authWindow;

  /**
   * Pops up a window and polls it to see when it's closed.
   */
  function popup() {
    var url = '<%=userProfile.getAuthUrl() %>';
    authWindow = window.open(url, 'Login Page', 'location=yes,width=800,' +
      'height=550,resizable=1,scrollbars=1,status=1');
    timer = setInterval('polling()',2000);
  }

  /**
   * Changes the gadget state (so the robot can check to see if the popup window
   * is closed).
   */
  function changeState() {
    var delta = {};
    delta['popupClosed'] = 'true';
    wave.getState().submitDelta(delta);
  }

  /**
   * Polling function to ask the popup window if it's closed.
   */
  function polling() {
    if (authWindow && authWindow.closed) {
      clearInterval(timer);
      changeState();
      //at this point, we know it's closed.
    }
  }

</script>
</head>
<body>

  <div id='wrapper'> <center>
    <h1><img src='http://tweety-wave.appspot.com/key.png' height='100px'
    style='float: left; padding: 3px; margin-right: 50px'/>  Authenticating... </h1>
    <p> You have to log in to use this service. <br />
    Please make sure you are not blocking the login popup.  :-)</p>
    </center>
   </div>

</body>
</html>
]]></Content>
</Module>