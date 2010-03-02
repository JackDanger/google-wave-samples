<% String domain = request.getServerName();
	int index = domain.indexOf(".");
	String appId = domain.substring(0, index);
	String projectId = request.getParameter("id");
  if (projectId == null) {
    throw new IllegalArgumentException("Missing required param: id");
  }	
	
  String robotAddress = appId + "+" + projectId + "@" + domain.substring(index + 1, domain.length());
  String profileImageUrl = "http://" + domain + "/images/profile.jpg";
  String robotName = "Buggy for " + projectId;
  String description = "This robot allows you to create and follow issue with Google Issue Tracker.";
  String triggerText = "Create a new issue";%>  
<extension 
    name="<%=robotName%>"
    thumbnailUrl="<%=profileImageUrl%>"
    description="<%=description%>"> 
  <author name="Austin Chau"/>
  
  <menuHook location="TOOLBAR" text="<%=triggerText%>"
      iconUrl="<%=profileImageUrl%>">    
    <addParticipants> 
      <participant id="<%=robotAddress%>" />
    </addParticipants>
    <annotateSelection key="buggy-wave" value="new"/>      
  </menuHook>     
  
</extension>