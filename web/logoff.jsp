<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java" 
	import="edu.pitt.sis.paws.cope.*" 
	errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>CoPE - Collaborative Paper Exchange. Log-off page</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="icon" href="<%= request.getContextPath()%>/assets/favicon.ico" type="image/x-icon">
<link rel="shortcut icon" href="<%= request.getContextPath()%>/assets/favicon.ico" type="image/x-icon"> 
</head>

<script language='javascript'>
	if (top != self) 
		top.location.href = self.location.href;
</script>

<body>
<%!
	String assets_path = null;
%>
<%
	// invalidate session
	session.removeAttribute(ClientDaemon.SESSION_INITED);
	session.invalidate();
	
	// forward to relogin
//	ClientDaemon cd = (ClientDaemon)ClientDaemon.getInstance(application);
	RequestDispatcher disp;
	disp = request.getRequestDispatcher(ClientDaemon.CONTEXT_INDEXURL);
	disp.forward(request, response);
%>
</body>
</html>


