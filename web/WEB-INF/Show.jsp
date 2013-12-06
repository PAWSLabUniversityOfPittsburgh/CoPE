<%@ page 
	contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java"
	import="java.util.*,edu.pitt.sis.paws.cope.*"
	errorPage=""
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" 
	"http://www.w3.org/TR/html4/frameset.dtd">
<%!public ClientDaemon cd;
	public iNode current_node;
	public String title = "";
	public String main = "";
	public String left = "";
	public String right = "";
	public String top = "";
	public String bottom = "";%>

<%
	cd = ClientDaemon.getInstance();
	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
	ArrayList urlList = (ArrayList) request.getAttribute("show");
//System.out.println("urlList not null " + (urlList!=null));
//System.out.println("urlList size " + (urlList.size()));
	title = (String)urlList.get(0);
	main = request.getContextPath() + "/content/jspMain"; //(String)urlList.get(1);
	int node_id = 0;
	if(current_node!=null)
		node_id = current_node.getId();
	left = request.getContextPath() + "/content/jspLeft#n" + node_id;
System.out.println("left:" + left);
	right = request.getContextPath() + "/content/jspRight#node" + node_id;
	top = request.getContextPath() + "/content/jspTop";
	bottom = request.getContextPath() + "/content/jspBottom";
	
	// Handle the "frames" mask
//	String sess_frames = (String)session.getAttribute(ClientDaemon.SESSION_FRAMES);
//	String req_frames = request.getParameter(ClientDaemon.REQUEST_FRAMES);
//	String default_frames = "LTMB";
//	String frames = ClientDaemon.competeParameters(sess_frames, req_frames,
//		default_frames);
%>

<html>
<head>
<title><%=title%></title>
<link rel='stylesheet' href="<%=request.getContextPath() + "/" +
ClientDaemon.CONTEXT_ASSETS_PATH%>/KnowledgeTree.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; no-cache; charset=utf-8">

</head>

<!--
<frameset rows="*" cols="250,*" id=topframeset framespacing="0" frameborder="YES" border="2">
	<frame src="<%=left%>" id=left name="leftFrame"> 
	<frameset rows="20,*" cols="*" id=showmainframeset frameborder="NO" framespacing="0" border="2">	
		<frame src="<%=top%>" id=maintop name="maintopFrame" frameborder="no" marginheight="0" marginwidth="0" scrolling="no"> 
		<frame src="<%=main%>" id=main name="mainFrame" marginheight="0" marginwidth="0" scrolling="yes">
	</frameset>
</frameset>-->

<!--
<frameset rows="*" cols="250,*" id="topFrameSet" framespacing="0" frameborder="YES" border="4">
	<frame src="<%=left%>" id="leftFrame" name="leftFrame" scrolling="YES" marginheight="2" marginwidth="2">
	<frameset rows="22,*" framespacing="0" frameborder="YES" border="0">
		<frame src="<%=top%>" name="topFrame" scrolling="NO" marginheight="0" marginwidth="0">
			<frame src="<%=main%>" name="mainFrame" marginheight="4" marginwidth="4">
	</frameset>
</frameset> -->

<frameset rows="*" cols="250,*" id="topFrameSet" framespacing="0" frameborder="YES" border="4">
	<frame src="<%=left%>" id="leftFrame" name="leftFrame" scrolling="YES" marginheight="2" marginwidth="2">
	<frame src="<%=right%>" id="rightFrame" name="rightFrame" scrolling="NO" marginheight="0" marginwidth="0">
</frameset>

<noframes>
	<body>
	</body>
</noframes>
</html>
