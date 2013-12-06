/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/**
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.paws.cope;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.cope.ClientDaemon;

public class Show extends servletGeneric
{
	static final long serialVersionUID = -2L;
	
	// Constants
	public static final int SHOW_MODE_VIEW = 1;  // also when parameter is null
	public static final int SHOW_MODE_EDIT = 2;
	public static final int SHOW_MODE_ADD = 3;
	public static final int SHOW_MODE_COPY = 4;
	public static final int SHOW_MODE_DELETE = 5;
	public static final int SHOW_MODE_MOVE = 6;
	public static final int SHOW_MODE_ADD_RATING = 7;
	public static final String SHOW_FLAG_RELOAD = "reload";

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		doGet(req, res);
	}
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
//Calendar start = null;
//Calendar finish = null;
//long diff_mills;
//start = new GregorianCalendar();
		// check authentication
//		doCheckInit(req, res);
//System.out.println("Show.new");		
		if(!ClientDaemon.isSessionInited(req.getSession(false)))
		{
			this.forwardToURL(req,res, ClientDaemon.CONTEXT_AUTHURL);
			return;
		}

		
		req.setCharacterEncoding("utf-8");
		
		// deal with Node current id
		HttpSession session = req.getSession();
		iNode current_node = (iNode)session.getAttribute(
			ClientDaemon.SESSION_CURRENT_NODE);
		String node_id_s = req.getParameter(ClientDaemon.REQUEST_NODE_ID);
		String mode_s = req.getParameter(ClientDaemon.REQUEST_SHOW_MODE);
		
		
		//save CoPE Search parameters to the session
		String filter_author = req.getParameter(CoPESearchNode.COPESEARCH_FILTER_AUTHOR);
		String filter_year = req.getParameter(CoPESearchNode.COPESEARCH_FILTER_YEAR);
		String filter_title = req.getParameter(CoPESearchNode.COPESEARCH_FILTER_TITLE);
		String filter_rating = req.getParameter(CoPESearchNode.COPESEARCH_FILTER_RATING);
		
		if(filter_author != null && filter_author != "") session.setAttribute(CoPESearchNode.COPESEARCH_FILTER_AUTHOR, filter_author);
		if(filter_year != null && filter_year != "") session.setAttribute(CoPESearchNode.COPESEARCH_FILTER_YEAR, filter_year);
		if(filter_title != null && filter_title != "") session.setAttribute(CoPESearchNode.COPESEARCH_FILTER_TITLE, filter_title);
		if(filter_rating != null && filter_rating != "") session.setAttribute(CoPESearchNode.COPESEARCH_FILTER_RATING, filter_rating);
		
		
//Calendar start = null;
//Calendar finish = null;
//long diff_mills;
//start = new GregorianCalendar();
		ResourceMap res_map = (ResourceMap)session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
//finish = new GregorianCalendar();
//diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//System.out.println("\t Show.doGet load ResMap millisec passed " + diff_mills);

		// check the reload statement
		String reload = req.getParameter(SHOW_FLAG_RELOAD);
		if(reload != null)
		{
			// get user id and group id
			int user_id = ((Integer)session.getAttribute(
					ClientDaemon.SESSION_USER_ID)).intValue();
			int group_id = ((Integer)session.getAttribute(
					ClientDaemon.SESSION_GROUP_ID)).intValue();
//System.out.println("Show.doGet creating group_id = " + group_id);
			res_map = cd.createUserResourceMap(user_id,
				group_id);
			session.setAttribute(ClientDaemon.SESSION_RES_MAP, res_map);
		}


		int node_id;
		if(node_id_s != null) // new node is specified
		{
			node_id = Integer.parseInt(node_id_s);
			
			if((current_node == null) || (node_id != current_node.getId()) )
			{
				current_node = res_map.getNodes().findById(node_id);
				session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
			}
		}
		else if(current_node != null) // old node is specified
			node_id = current_node.getId();
		else // root node is meant
		{
System.out.println("[CoPE] Show (res_map==null) " + (res_map==null));
System.out.println("[CoPE] Show (res_map.getRootNode()==null) " + (res_map.getRootNode()==null));
			if(res_map.getRootNode().getChildren().size() == 1)
			{
				node_id = res_map.getRootNode().getChildren().get(0).getId();
				current_node = res_map.getRootNode().getChildren().get(0);
			}
			else
			{
				node_id = res_map.getRootNode().getId();
				current_node = res_map.getRootNode();
			}
			session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
//			node_id = res_map.getRootNode().getId();
//			current_node = res_map.getRootNode();
//			session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
		}
		
		if(current_node == null) // node was not found -- set root
		{
			node_id = res_map.getRootNode().getId();
			current_node = res_map.getRootNode();
			session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
		}
		
//System.out.println("current_module null? " + (current_module==null));		
		String title = current_node.getTitle();
		
		//String main_url = "";
		//main_url =  req.getContextPath() + "/content/jspMain";
		
		// Check for modes (view/edit/...)
		//	first delete old mode
		session.removeAttribute(ClientDaemon.SESSION_SHOW_MODE);
		int mode = 0;
		if(mode_s != null)
		{
			mode = Integer.parseInt(mode_s);
			// .. and load mode to the session
			switch(mode)
			{
				case Show.SHOW_MODE_EDIT:
					session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
						Show.SHOW_MODE_EDIT);
				break;
			      case Show.SHOW_MODE_ADD:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_ADD);
			      break;
			      case Show.SHOW_MODE_COPY:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_COPY);
			      break;
			      case Show.SHOW_MODE_DELETE:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_DELETE);
			      break;
			      case Show.SHOW_MODE_MOVE:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_MOVE);
			      break;
			      case Show.SHOW_MODE_ADD_RATING:
			            session.setAttribute(ClientDaemon.SESSION_SHOW_MODE,
			                  Show.SHOW_MODE_ADD_RATING);
			      break;
				
			}
		}
		
		
		
		// Check for "hide left frame flag"
		Object o_lframe = session.getAttribute(ClientDaemon.SESSION_HIDE_LEFT_FRAME);
		boolean hide_left_frame = (o_lframe != null);
//		session.removeAttribute(cd.SESSION_HIDE_LEFT_FRAME);
		
		// if there's a node pending and we're not editing it - recucle
		if( (res_map.getPendingNode()!= null) && (mode != Show.SHOW_MODE_EDIT) )
		{
			res_map.recycleNode(res_map.getPendingNode());
			session.setAttribute(ClientDaemon.SESSION_RES_MAP, res_map);
			
			if(current_node.getId() == 0)
			{
				current_node = current_node.getParent();
				session.setAttribute(ClientDaemon.SESSION_CURRENT_NODE, current_node);
			}
		}

		// Make sure the path to the node is expanded if necessary
		String expand = req.getParameter(ClientDaemon.REQUEST_EXPANDPATH);
		if(expand != null)
		{
			if(res_map.getNodes().findById(current_node.getId()).
				expandParents(false))
				session.setAttribute(ClientDaemon.SESSION_RES_MAP,res_map);
		}

//		ArrayList urlList = new ArrayList(1);// get from the query
//		urlList.add(title);
////		urlList.add(main_url);
//		req.setAttribute("show",urlList);
//		forwardToURL(req,res,"/content/jspShow");




//		String main = req.getContextPath() + "/content/jspMain";
		String left = req.getContextPath() + "/content/jspLeft" +
			((current_node!=null)?("#n" + current_node.getId()):"");
		String right = req.getContextPath() + "/content/jspRight" +
			((current_node!=null)?("#node" + current_node.getId()):"");
//		String right2 = "/content/jspRight" /*+
//			((current_node!=null)?("#node" + current_node.getId()):"")*/;
//		String top = req.getContextPath() + "/content/jspTop";
//		String bottom = req.getContextPath() + "/content/jspBottom";
		
//		// Handle the "frames" mask
//		String sess_frames = (String)session.getAttribute(cd.SESSION_FRAMES);
//		String req_frames = request.getParameter(cd.REQUEST_FRAMES);
//		String default_frames = "LTMB";
//		String frames = ClientDaemon.competeParameters(sess_frames, req_frames,
//			default_frames);

		// Print the frame set
		// if left frame hidden - forward to right frameset and exit
//		if(hide_left_frame)
//		{
//			this.forwardToURL(req, res, right2);
//System.out.println("Show.doGet redirURL: " + right2);
//			return;
//		}
		
		PrintWriter out = res.getWriter();
//		res.setContentType("text/html;no-cache;charset=utf-8");
		out.println("<html><head>");
		out.println("<meta http-equiv='Content-Type' content='text/html; no-cache; charset=utf-8'>");
		out.println("<title> CoPE - " + title + "</title>");
		out.println("<link rel='stylesheet' href='" + req.getContextPath() +
			"/" + ClientDaemon.CONTEXT_ASSETS_PATH + "/KnowledgeTree.css' type='text/css'/>");
		out.println("<link rel='icon' href='" + req.getContextPath() +
			"/" + ClientDaemon.CONTEXT_ASSETS_PATH + "/favicon.ico' type='image/x-icon'>");
				out.println("<link rel='shortcut icon' href='" + req.getContextPath() +
			"/" + ClientDaemon.CONTEXT_ASSETS_PATH + "/favicon.ico' type='image/x-icon'> ");
		out.println("</head>");
		out.println("");
		out.println("");
		out.println("");

		out.println("<script language='javascript' type='text/javascript'>");
		out.println("<!--		");
		out.println("function restartSession()");
		out.println("{");
		out.println("	var message = \"Are you still there?\\n\\nYour current session is about to expire. Your current session will expire in 2 minutes, at \" + getTimeSessionExpires() + \".\\n\\nIf you would like to continue your work session, please select the Ok button to renew your session.\\n\\nIf you would like to log off now, please select the Cancel button.\";");
		out.println("	if ( confirm(message) )");
		out.println("	{");
		out.println("		var now = new Date();");
		out.println("		var action = (g_dttmSessionExpires > now);");
		out.println("");
		out.println("		if (action == true)");
		out.println("		{");
		out.println("			window.top.location.replace(window.top.location.href);");
		out.println("		}");
		out.println("		else");
		out.println("		{");
		out.println("			window.top.location.replace(\"" + req.getContextPath() + "/logoff.jsp\");");
		out.println("		}");
		out.println("	}");
		out.println("	else");
		out.println("	{");
		out.println("		var now = new Date();");
		out.println("		var action = (g_dttmSessionExpires > now);");
		out.println("");
		out.println("		if (action == true)");
		out.println("		{");
		out.println("			window.top.location.replace(\"" + req.getContextPath() + "/logoff.jsp\");");
		out.println("		}");
		out.println("		else");
		out.println("		{");
		out.println("			window.top.location.replace(\"" + req.getContextPath() + "/logoff.jsp\");");
		out.println("		}");
		out.println("	}");
		out.println("}");

		out.println("function startSessionTimer()");
		out.println("{");
		out.println("	g_objWarningTimer = setTimeout('restartSession();', 10680000);");//2580000-45 minutes
		out.println("}");

		out.println("function getTimeSessionExpires()");
		out.println("{");
		out.println("	g_dttmSessionExpires = new Date(Date.parse(Date()) + 120000);");
		out.println("	var dttmSessionExpiresHour = g_dttmSessionExpires.getHours();");
		out.println("	var dttmSessionExpiresMinute = g_dttmSessionExpires.getMinutes();");
		out.println("	var strMeridianIndicator = 'AM';");
		out.println("	if (dttmSessionExpiresHour >= 12)");
		out.println("	{");
		out.println("		strMeridianIndicator = 'PM';");
		out.println("	}");
		out.println("	if (dttmSessionExpiresHour > 12)");
		out.println("	{");
		out.println("		dttmSessionExpiresHour = (dttmSessionExpiresHour - 12);");
		out.println("	}");
		out.println("	if (dttmSessionExpiresMinute < 10)");
		out.println("	{");
		out.println("		dttmSessionExpiresMinute = '0' + dttmSessionExpiresMinute;");
		out.println("	}");
		out.println("	var strDisplayTime = dttmSessionExpiresHour + ':' + dttmSessionExpiresMinute + ' ' + strMeridianIndicator;");
		out.println("	return strDisplayTime");
		out.println("}");
		out.println("");
		out.println("window.onload = pageInit;");
		out.println("");
		out.println("function pageInit()");
		out.println("{");
		out.println("	try");
		out.println("	{");
		out.println("		startSessionTimer();");
		out.println("	}");
		out.println("	catch (exc){}");
		out.println("}");
		out.println("-->");
		out.println("</script>");		
	

		out.println("<frameset rows='*' cols='" + ((hide_left_frame)?"0":"250") + ",*' id='topFrameSet' framespacing='0' frameborder='YES' border='4'>");
		out.println("	<frame src='" + left + "' id='leftFrame' name='leftFrame' scrolling='YES' marginheight='2' marginwidth='2'>");
		out.println("	<frame src='" + right + "' id='rightFrame' name='rightFrame' scrolling='YES' marginheight='0' marginwidth='0'>");
//		out.println("	<frameset rows='22,*' framespacing='0' frameborder='YES' border='0'>");
//		out.println("		<frame src='" + top + "' name='topFrame' scrolling='NO' marginheight='0' marginwidth='0'>");
//		out.println("		<frame src='" + main + "' name='mainFrame' marginheight='4' marginwidth='4'>");
//		out.println("	</frameset>");
		out.println("</frameset>");
		
		out.println("<noframes>");
		out.println("	<body>");
		out.println("	</body>");
		out.println("</noframes>");
		out.println("</html>");

//finish = new GregorianCalendar();
//diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//System.out.println("\t[CoPE] Show.doGet millisec passed " + diff_mills);

	}
}

