<%@ page 
	contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java"
	import="edu.pitt.sis.paws.cope.*"
	errorPage=""
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">

<html> <!-- utf-8 -->
<head>
<link rel='stylesheet' href="<%=request.getContextPath() + "/" +
ClientDaemon.CONTEXT_ASSETS_PATH%>/KnowledgeTree.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; no-cache; charset=utf-8">
<title>Main Frame</title>
<body>
<%!public ClientDaemon cd;
	public ResourceMap res_map;
	public iNode current_node;
	public int show_mode;
	public int user_id;
	public int group_id;
	public boolean show_ratings;%>
<%
	//	cd = ClientDaemon.getInstance();
	res_map = (ResourceMap) session.
	getAttribute(ClientDaemon.SESSION_RES_MAP);
//System.out.println("jspMain.doGet (res_map==null)=" + (res_map==null));	
	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
	// Show mode
	Integer show_mode_i = (Integer)session.getAttribute(ClientDaemon.SESSION_SHOW_MODE);
	show_mode = (show_mode_i == null) ? Show.SHOW_MODE_VIEW : 
		show_mode_i.intValue();
	user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
	group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();

	show_ratings = false;
	iNode c_node = null;
	if(current_node != null)
	{
		c_node = res_map.getNodes().findById(current_node.getId());

		if((c_node.getNodeType() != iNode.NODE_TYPE_I_BIN) && (c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE))
	show_ratings = (
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_VIEW_RATING, user_id, c_node, 
			iNode.NODE_TYPE_I_ALL, c_node.getNodeType())
		||
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_RATE, user_id, c_node, 
			iNode.NODE_TYPE_I_ALL, c_node.getNodeType())
		||	
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_VIEW_RATING, group_id, c_node, 
			iNode.NODE_TYPE_I_ALL, c_node.getNodeType())
		||
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_RATE, group_id, c_node, 
			iNode.NODE_TYPE_I_ALL, c_node.getNodeType())
		);
	}
//	can_rate = false;
//	iNode c_node = current_node;
//	
//	int show_mode = (show_mode_i == null) ? Show.SHOW_MODE_VIEW : 
//		show_mode_i.intValue();
//	is_in_view_mode = (show_mode == Show.SHOW_MODE_VIEW);
//	
//	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
//		(is_in_view_mode))
//	{// inspect all the rights
//		int parent_n_type = (c_node.getParent() == null) ? 
//	iNode.NODE_TYPE_I_ALL : c_node.getParent().getNodeType();
//		
//		if( (c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
//	(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN)	)
//	can_rate = (
//		res_map.isAllowedWhatWhoForFromTo(
//			Right.RIGHT_TYPE_RATE, user_id, c_node, 
//			parent_n_type, c_node.getNodeType())
//		||
//		res_map.isAllowedWhatWhoForFromTo(
//			Right.RIGHT_TYPE_RATE, group_id, c_node, 
//			parent_n_type, c_node.getNodeType())
//		);
//		else
//	can_rate = true; // my profile & bin can always be edited

//System.out.println("top.jsp Check for 'can edit' ---- over");

	
//	session.removeAttribute(cd.SESSION_SHOW_MODE);

	if((current_node != null) && (show_mode == Show.SHOW_MODE_VIEW))
		current_node.showViewHeader(out, request);
	if((current_node != null) && (show_mode == Show.SHOW_MODE_EDIT))
		current_node.showEditHeader(out, request);

	// display subfolders of such exist
	//	first find a current node node in res_map (just in case)
	
	// This is the url to go to if a Cancel button will be clicked
	String cancel_to_url = request.getContextPath() + "/content" + 
		"/Show" +
		((c_node != null)?
	("?" + ClientDaemon.REQUEST_NODE_ID + "=" + c_node.getId()):""
		);
	
//System.out.println("main.jsp : cancel_to_url = " + cancel_to_url);
	if(c_node != null)
	{// current node exists
		// show ratings ?
//System.out.println("main.jsp : (res_map!=null)=" + (res_map!=null));


		// check the mode
		switch(show_mode)
		{
	case Show.SHOW_MODE_VIEW:
		c_node.showView(out, request, show_ratings);
	break;
	case Show.SHOW_MODE_EDIT:
		c_node.showEdit(out, request, cancel_to_url);
	break;
	case Show.SHOW_MODE_ADD:
		c_node.showAdd(out, request, cancel_to_url, res_map);
	break;
	case Show.SHOW_MODE_COPY:
		c_node.showCopy(out, request, cancel_to_url, res_map);
	break;
	case Show.SHOW_MODE_DELETE:
		c_node.showDelete(out, request, cancel_to_url);
	break;
		}
	}
	else
	{// no current node defined
		ResourceMap.displayFolderView(
	res_map.getRootNode().getChildren(), out, request, false);
	}
	
	// up-dir icon
//System.out.println("Main.JSP (current_node != null) " + (current_node != null));		
//System.out.println("Main.JSP (c_node != null) " + (c_node != null));		
//	boolean no_way_up = false;
%>
</body>
</html>
