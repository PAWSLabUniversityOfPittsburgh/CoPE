<!--
Disclaimer:
	Java code contained in this file is created as part of educational
	research and development. You assume full responsibility and risk of
	loss resulting from compiling and running this code.
Author: Michael V. Yudelson (C) 2005
Affiliation: University of Pittsburgh, School of Information Sciences
	URL: http://www.yudelson.org
	Email: myudelson@gmail.com
-->
 <%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java" 
	import="edu.pitt.sis.paws.cope.*" 
	errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>CoPE - Collaborative Paper Exchange system</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="icon" href="<%= request.getContextPath()%>/assets/favicon.ico" type="image/x-icon">
<link rel="shortcut icon" href="<%= request.getContextPath()%>/assets/favicon.ico" type="image/x-icon"> 
</head>

<body>
<%!
	String assets_path = null;
//	ClientDaemon cd = null;
%>
<%
	//	ClientDaemon cd = ClientDaemon.getInstance();
	assets_path = ClientDaemon.CONTEXT_ASSETS_PATH;
	// invalidate session
	session.removeAttribute(ClientDaemon.SESSION_INITED);
	session.invalidate();
%>
<CENTER><img src="<%= request.getContextPath()%>/assets/CoPELogo2.gif" alt="CoPE Logo" align="middle"></CENTER>
<H2 align="center"><a href="<%=request.getContextPath()%>/content/Show">Login</a></H2>
<!-- /eLearning/servlet/access -->
</body>
</html>


