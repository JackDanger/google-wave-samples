<% String domain = request.getServerName();
	int index = domain.indexOf(".");
	String appId = domain.substring(0, index);
	String robotAddress = appId + "@" + domain.substring(index + 1, domain.length());%>
<extension 
    name="<%=appId%>"
    thumbnailUrl="http://<%=domain%>/images/profile.jpg"
    description="This robot retrieves the current stock price of a stock symbol when you highlight a stock symbol"> 
  <author name="Austin Chau"/>
  <menuHook location="TOOLBAR" text="Stockify it!"
      iconUrl="http://<%=domain%>/images/profile.jpg">    
    <addParticipants> 
    	<participant id="<%=robotAddress%>" />
    </addParticipants>
    <annotateSelection key="stocky" value="_new_"/>      
  </menuHook>
</extension>