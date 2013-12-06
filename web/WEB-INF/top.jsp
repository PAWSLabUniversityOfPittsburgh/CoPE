<%@ page
	contentType="text/html; charset=utf-8" pageEncoding="utf-8" 
	language="java"
	import="java.io.*, java.util.*,edu.pitt.sis.paws.cope.*"
	errorPage=""
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Untitled Document</title>
<link rel='stylesheet' href="<%=request.getContextPath() + "/" +
	ClientDaemon.CONTEXT_ASSETS_PATH%>/KnowledgeTree.css" type="text/css"/>

<script language='javascript' type="text/javascript">
<!--
	var xmlhttp = false;
	var g_objWarningTimer;
	var g_dttmSessionExpires;

	function leftFrameHideUnhide(flag)
	{
		bgmd = Math.round(Math.random()*1000);
		url = new String("<%=request.getContextPath()%>/ajax_robot");
		url = url.concat("?BGMD=", bgmd, "&<%=AjaxRobot.REQUEST_HIDE_LEFT_FRAME%>=", flag);
//		alert(url);
		try
		{
			xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
			xmlhttp.open("GET", url, true);
			xmlhttp.send();
		}
		catch (e)
		{
			try
			{
				xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
				xmlhttp.open("GET", url, true);
				xmlhttp.send();
			}
			catch (E) { xmlhttp = false; }
		}
		
		if (!xmlhttp && typeof XMLHttpRequest!='undefined')
		{
			try
			{
				xmlhttp = new XMLHttpRequest();
				xmlhttp.open("GET", url, true);
				xmlhttp.send(null);
			}
			catch (e) { xmlhttp=false; }
		}
		
		if (!xmlhttp && window.createRequest)
		{
			try
			{
				xmlhttp = window.createRequest();
				xmlhttp.open("GET", url, true);
				xmlhttp.send(null);
			}
			catch (e) { xmlhttp=false; }
		}
	}
	
	function leftFrameChange(param)
	{
		if(param==-1)
		{
			document.getElementById('leftFrameControl').innerHTML =
				"\n\t<a href='#' onClick='leftFrameChange(1);'><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/right.gif' width='16' height='16' border='0' alt='[&gt;]' title='Show left frame' /></a>\n";
			top.document.getElementById('topFrameSet').cols = '0,*';
			top.document.getElementById('leftFrame').noResize = 'true';

			// send AJAX message to hide left frame
			leftFrameHideUnhide('1');
		}
		else if(param==1)
		{
			document.getElementById('leftFrameControl').innerHTML = 
				"\n\t<a href='#' onClick='leftFrameChange(-1);'><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/left.gif' width='16' height='16' border='0' alt='[&lt;]' title='Hide left frame' /></a>\n";
			top.document.getElementById('topFrameSet').cols = '250,*';
			top.document.getElementById('leftFrame').noResize = null;

			// send AJAX message to UN-hide left frame
			leftFrameHideUnhide('-1');
		}
	}
	

-->
</script>
<%!
	public ResourceMap res_map = null;
	public iNode current_node = null;
	public int user_id = 0;
	public int group_id = 0;
	public String up_dir_icon = null;
	public String up_dir_href = null;
	public String edit_icon = null;
	public String edit_href = null;
	public String add_icon = null;
	public String add_href = null;
	public String copy_icon = null;
	public String copy_href = null;
	public String del_icon = null;
	public String del_href = null;
	public String move_icon = null;
	public String move_href = null;
	public boolean hide_left_frame = false;
%>
<%
	res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
//System.out.println("jspTop.doGet (res_map==null)=" + (res_map==null));	
	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
	user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
	group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
	
	// get a copy of current node from a map
	iNode c_node = null;
	if(current_node != null)
		c_node = res_map.getNodes().findById(current_node.getId());
	// deal with 'up-dir' icon
	boolean no_way_up = (c_node == null || c_node.getId()==1)?true:false;
	
	int parent_node_id = 0;
	if( (c_node != null) && (c_node.getParent() != null) ) 
		parent_node_id = c_node.getParent().getId();
	
	// DECIDE MODES
	boolean can_edit = false;
	boolean is_in_edit_mode = false;

	boolean can_add = false;
	boolean is_in_add_mode = false;

	boolean can_del = false;
	boolean is_in_del_mode = false;

	boolean can_copy = false;
	boolean is_in_copy_mode = false;

	boolean can_move = false;
	boolean is_in_move_mode = false;
	
	boolean can_rate = false;

	Integer show_mode_i = (Integer)session.getAttribute(ClientDaemon.SESSION_SHOW_MODE);
	int show_mode = (show_mode_i == null) ? Show.SHOW_MODE_VIEW : 
		show_mode_i.intValue();
	is_in_edit_mode = (show_mode == Show.SHOW_MODE_EDIT);
	is_in_del_mode = (show_mode == Show.SHOW_MODE_DELETE);
	is_in_add_mode = (show_mode == Show.SHOW_MODE_ADD);
	is_in_copy_mode = (show_mode == Show.SHOW_MODE_COPY);
	is_in_move_mode = (show_mode == Show.SHOW_MODE_MOVE);
	
	// decide whether to show up dir by type, so far - default
	if( !no_way_up && (!is_in_edit_mode) && (!is_in_add_mode) && 
		(!is_in_copy_mode) && (!is_in_del_mode) && (!is_in_move_mode))
	{
		up_dir_icon = "up_dir_enable.gif";
		up_dir_href = " href='" + request.getContextPath() + "/content" + 
	"/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + parent_node_id + "' " +
	"target='_top'";
	}
	else
	{
		up_dir_icon = "up_dir_disable.gif";
		up_dir_href = "";
	}

	// Deciding CAN_EDIT
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
		int parent_n_type = (c_node.getParent() == null) ? 
	iNode.NODE_TYPE_I_ALL : c_node.getParent().getNodeType();
		
		if( (c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_COPE_SEARCH) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN)	)
	can_edit = (
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_EDIT, user_id, c_node, 
			parent_n_type, c_node.getNodeType())
		||
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_EDIT, group_id, c_node, 
			parent_n_type, c_node.getNodeType())
		);
		else
	can_edit = true; // my profile can always be edited
//System.out.println("top.jsp Check for 'can edit' ---- over");
	}
	

	// Deciding CAN_DEL
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
		int parent_n_type = (c_node.getParent() == null) ? 
	iNode.NODE_TYPE_I_ALL : c_node.getParent().getNodeType();
		
		if( (c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_COPE_SEARCH) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN) )
	can_del = 
		(		
			(
			res_map.isAllowedWhatWhoForFromTo(
		Right.RIGHT_TYPE_DELETE, user_id, c_node, 
		parent_n_type, c_node.getNodeType())
			||
			res_map.isAllowedWhatWhoForFromTo(
		Right.RIGHT_TYPE_DELETE, group_id, c_node, 
		parent_n_type, c_node.getNodeType())
			)
		) && //(((Node)c_node).getChildren().size()==0)
		// downwards
		(		
			(
			res_map.isAllowedWhatWhoForFromTo_DownInhibitory(
		Right.RIGHT_TYPE_DELETE, user_id, c_node, 
		parent_n_type, c_node.getNodeType())
			||
			res_map.isAllowedWhatWhoForFromTo_DownInhibitory(
		Right.RIGHT_TYPE_DELETE, group_id, c_node, 
		parent_n_type, c_node.getNodeType())
			)
		)
		;
//System.out.println("top.jsp Check for 'can edit' ---- over");
	}
	

	// Deciding CAN_ADD
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		/*(c_node.getFolderFlag()) && */
		(!is_in_add_mode) && (!is_in_edit_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
		can_add = false;
//		System.out.println("top No of Type="+iNode.NODE_TYPES_I_DEFINITIVE.length);
		for(int i=0; i<iNode.NODE_TYPES_I_DEFINITIVE.length;i++)
		{
	if(
		(c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
		(c_node.getNodeType() != iNode.NODE_TYPE_I_COPE_SEARCH) &&
		(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN) &&
		res_map.isAllowedWhatWho2ForFromToQuant(Right.RIGHT_TYPE_ADD,
			user_id, group_id, c_node, c_node.getNodeType(), 
			iNode.NODE_TYPES_I_DEFINITIVE[i])
	)
		can_add = true;
		}/**/
	}
	
	// Deciding CAN_COPY
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		/*(c_node.getFolderFlag()) && */
		(!is_in_add_mode) && (!is_in_edit_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
	
		for(int i=0; i<iNode.NODE_TYPES_I_DEFINITIVE.length;i++)
		{
	if(	
		(c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
		(c_node.getNodeType() != iNode.NODE_TYPE_I_COPE_SEARCH) &&
		(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN) &&
		res_map.isAllowedWhatWho2ForFromToQuant(Right.RIGHT_TYPE_COPY,
			user_id, group_id, c_node, c_node.getNodeType(), 
			iNode.NODE_TYPES_I_DEFINITIVE[i])
	)
		can_copy = true;
		}/**/
	}	
	
	// Deciding CAN_MOVE = CAN_DEL + CAN_COPY
	can_move = can_del && can_copy;

	// Deciding CAN_RATE
	if( (c_node != null) && /*(c_node.getRights().size() > 0) && */
		(!is_in_edit_mode) && (!is_in_add_mode) && (!is_in_copy_mode)
		&& (!is_in_del_mode) && (!is_in_move_mode))
	{// inspect all the rights
		int parent_n_type = (c_node.getParent() == null) ? 
	iNode.NODE_TYPE_I_ALL : c_node.getParent().getNodeType();
		
		if( (c_node.getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_COPE_SEARCH) &&
	(c_node.getNodeType() != iNode.NODE_TYPE_I_BIN)	)
	can_rate = (
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_RATE, user_id, c_node, 
			parent_n_type, c_node.getNodeType())
		||
		res_map.isAllowedWhatWhoForFromTo(
			Right.RIGHT_TYPE_RATE, group_id, c_node, 
			parent_n_type, c_node.getNodeType())
		);
		else
	can_rate = true; // my profile & bin can always be edited
//System.out.println("top.jsp Check for 'can edit' ---- over");
	}

	
	String base_href = "";
	if(c_node != null)
		base_href = " href='" + request.getContextPath() + "/content" + 
	"/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + c_node.getId() + "&" + 
	ClientDaemon.REQUEST_SHOW_MODE + "=";
	
	if( can_edit)
	{
		edit_icon = "edit_enable.gif";
		edit_href = base_href + Show.SHOW_MODE_EDIT + "' target='_top'";
	}
	else
	{
		edit_icon = "edit_disable.gif";
		edit_href = "";
	}

	if( can_add )
	{
		add_icon = "add_enable.gif";
		add_href = base_href + Show.SHOW_MODE_ADD + "' target='_top'";
	}
	else
	{
		add_icon = "add_disable.gif";
		add_href = "";
	}

	if( can_copy )
	{
		copy_icon = "copy_enable.gif";
		copy_href = base_href + Show.SHOW_MODE_COPY + "' target='_top'";
	}
	else
	{
		copy_icon = "copy_disable.gif";
		copy_href = "";
	}

	if( can_del )
	{
		del_icon = "delete_enable.gif";
		del_href = base_href + Show.SHOW_MODE_DELETE + "' target='_top'";
	}
	else
	{
		del_icon = "delete_disable.gif";
		del_href = "";
	}
	
	if( can_move )
	{
		move_icon = "move_enable.gif";
		move_href = base_href + Show.SHOW_MODE_MOVE + "' target='_top'";
	}
	else
	{
		move_icon = "move_disable.gif";
		move_href = "";
	}
	
	Object o_lframe = session.getAttribute(ClientDaemon.SESSION_HIDE_LEFT_FRAME);
	hide_left_frame = (o_lframe != null);
%>
</head>

<body > <!-- bgcolor="#CCFF66" -->
<table width="100%" border="0" cellpadding="0" cellspacing="0" style="padding-left:2px;padding-top:1px;background-color:#E8EEF7; border-bottom:1px solid #3366CC">

  <tr>
    <!-- Show/Hide left frame button E8EEF7  C3D9FF -->
    <td id="leftFrameControl">
	<a href='#' onClick='leftFrameChange(<%=(hide_left_frame)?"1":"-1"%>);'><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/<%=(hide_left_frame)?"right":"left"%>.gif' width='16' height='16' border='0' alt='[&lt;]' title='Hide left frame' /></a>
    </td>
    <!-- Spacer -->
    <td>
     <img src="<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/spacer16x8.gif" alt="  " width="8" height="16" border="0" />
    </td>
    <!-- Reload Button -->
    <td>
     <a href='<%=request.getContextPath() + "/content/Show?" + ((c_node != null)?(ClientDaemon.REQUEST_NODE_ID + "=" +c_node.getId() + "&"):"") + Show.SHOW_FLAG_RELOAD + "=1"%>' target = "_top"><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/reload.gif' width='16' height='16' border='0' alt='[Reload]' title='Reload' /></a>
    </td>
    <!-- Divider -->
    <td>
	<img src="<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/divider.gif" alt="  " width="18" height="16" border="0" />
    </td>
    <!-- Up-dir button -->
    <td>
	<a<%=up_dir_href%>><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/" + up_dir_icon%>' width='16' height='16' border='0' alt='[Up]' title='Up' /></a>
    </td>
    <!-- Divider -->
    <td>
	<img src="<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/divider.gif" alt="  " width="18" height="16" border="0" />
    </td>

    <!-- Add button -->
    <td>
	<a<%=add_href%>><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/" + add_icon%>' width='16' height='16' border='0' alt='[Add]' title='Add new document/folder' /></a>
    </td>
    <!-- Spacer -->
    <td>
	<img src="<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/spacer16x8.gif" alt="  " width="8" height="16" border="0" />
    </td>

    <!-- Delete button -->
    <td>
	<a<%=del_href%>><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/" + del_icon%>' width='16' height='16' border='0' alt='[Delete]' title='Delete document/folder' /></a>
    </td>
    <!-- Spacer -->
    <td>
	<img src="<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/spacer16x8.gif" alt="  " width="8" height="16" border="0" />
    </td>

    <!-- Edit button -->
    <td>
	<a<%=edit_href%>><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/" + edit_icon%>' width='16' height='16' border='0' alt='[Edit]' title='Edit document/folder' /></a>
    </td>
    <!-- Spacer -->
    <td>
	<img src="<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/spacer16x8.gif" alt="  " width="8" height="16" border="0" />
    </td>

    <!-- Copy button -->
    <td>
	<a<%=copy_href%>><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/" + copy_icon%>' width='16' height='16' border='0' alt='[Copy]' title='Copy document/folder' /></a>
    </td>
    <!-- Spacer -->
    <td>
	<img src="<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/spacer16x8.gif" alt="  " width="8" height="16" border="0" />
    </td>
    
    <!-- Move button -->
    <td>
	<a<%=move_href%>><img src='<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/" + move_icon%>' width='16' height='16' border='0' alt='[Move]' title='Move document/folder' /></a>
    </td>
    
    <!-- Stretcher -->
    <td width="100%">&nbsp;</td>
    <!-- Logoff button -->
    <td>
	<a href='<%=request.getContextPath() + "/content/doAuthenticate?logoff=1"%>' target="_top">Logoff</a>
    </td>
    <!-- Spacer -->
    <td>
	<img src="<%=request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH%>/spacer16x4.gif" alt="  " width="8" height="16" border="0" />
    </td>
  </tr>
</table>
</body>
</html>
