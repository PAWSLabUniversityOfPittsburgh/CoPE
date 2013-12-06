<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java" 
	import="edu.pitt.sis.paws.cope.*, java.util.*" 
	errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>CoPE - Collaborative Paper Exchange system. Authentication page</title>
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
	String msg = null;
%>
<%
	request.getParameter("msg");
	msg = (msg==null)?"Incorrect username and/or password. Please try again":msg;
//	ClientDaemon cd = (ClientDaemon)ClientDaemon.getInstance(application);
	assets_path = ClientDaemon.CONTEXT_ASSETS_PATH;
	Enumeration enu = session.getAttributeNames();
	for(;enu.hasMoreElements();)
		session.removeAttribute((String)enu.nextElement());
//		session.removeAttribute(ClientDaemon.SESSION_INITED);
%>
<CENTER><img src="<%= request.getContextPath()%>/assets/CoPELogo2.gif" alt="CoPE Logo" align="middle"></CENTER>
<p style="color:red" align="center"><%=msg%></p>
<!-- /eLearning/servlet/access -->
<FORM ACTION="j_security_check" METHOD="POST">
	<table border="0" cellspacing="2" cellpadding="2" align="center">
		<tr> 
			<td width="50" style="color:red;">Login</td>
			<td width="150"><input name="j_username" type="text" value="" size="25" maxlength="15"></td>
		</tr>
		<tr> 
			<td width="50" style="color:red;">Password</td>
			<td width="150"><input name="j_password" type="password" value="" size="25" maxlength="15"></td>
		</tr>
		<tr> 
			<td colspan="2"><input type="Submit" value="Login"></td>
		</tr>
	</table>
</FORM>


</body>
</html>


