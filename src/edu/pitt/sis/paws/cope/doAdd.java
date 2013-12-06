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

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.pitt.sis.paws.core.utils.SQLManager;

public class doAdd extends servletGeneric
{
	static final long serialVersionUID = -2L;
	
	public void doGet(HttpServletRequest request, 
		HttpServletResponse response) throws ServletException, IOException
	{
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, 
		HttpServletResponse response) throws ServletException, IOException
	{
		// first get the id of the node being edited
		String id_s = (String)request.getParameter(iNode.NODE_FRMFIELD_ID);
		if(id_s == null)
		{
			System.out.println("doEdit.doPost: ERROR Id not specified!");
			return;
		}
		int id = Integer.parseInt(id_s);
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap)session.getAttribute(
			ClientDaemon.SESSION_RES_MAP);
		iNode edited_node = res_map.getNodes().findById(id);
		if(edited_node == null)
		{
			System.out.println("doEdit.doPost: ERROR Id not found!");
			return;
		}
		// get the type of a newly added node
		String node_type_s = (String)request.getParameter(
			iNode.NODE_FRMFIELD_NODETYPE);
		if(node_type_s == null)
		{
			System.out.println(
				"doEdit.doPost: ERROR Node Type not specified!");
			return;
		}
		
		// Get User ID
		
		// Create stub node in database
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		User user = res_map.getUsers().findById(user_id);
		int node_type = Integer.parseInt(node_type_s);
		int folder_flag_i = ((node_type == iNode.NODE_TYPE_I_FOLDER) ||
			(node_type == iNode.NODE_TYPE_I_TOPIC_FOLDER) || 
			(node_type == iNode.NODE_TYPE_I_COURSE) )?1:0;
		boolean folder_flag = (folder_flag_i == 1)?true:false;
		int node_id = 0;

		String title = "";
		switch (node_type) 
		{
			case iNode.NODE_TYPE_I_FOLDER:
			case iNode.NODE_TYPE_I_UNTYPDOC:
				title = "New folder/document";
			break;
			case iNode.NODE_TYPE_I_PAPER:
				title = "New paper";
			break;
			case iNode.NODE_TYPE_I_SUMMARY:
				title = "Summary";
			break;
			case iNode.NODE_TYPE_I_CONCEPTS:
				title = "Concepts";
			break;
			case iNode.NODE_TYPE_I_TOPIC_FOLDER:
				title = "Topic";
			break;
			case iNode.NODE_TYPE_I_QUIZ:
				title = "Quiz";
			break;
			case iNode.NODE_TYPE_I_DISSECTION:
				title = "Dissection";
			break;
			case iNode.NODE_TYPE_I_WADEIN:
				title = "WADEIn";
			break;
			case iNode.NODE_TYPE_I_CODEEXAMPLE:
				title = "Code Example";
			break;
			case iNode.NODE_TYPE_I_KARELROBOT:
				title = "Karel Robot";
			break;
			case iNode.NODE_TYPE_I_SYS_QUIZGUIDE:
				title = "QuizGUIDE:  Adaptive Quizzes";
			break;
			case iNode.NODE_TYPE_I_SYS_NAVEX:
				title = "NavEx: Navigation to Adaptive Examples";
			break;
			case iNode.NODE_TYPE_I_SYS_WADEIN:
				title = "WADEIn II - C Expressions";
			break;
			case iNode.NODE_TYPE_I_COURSE:
				title = "Course";
			break;
			case iNode.NODE_TYPE_I_LINK:
				title = "Link";
			break;
			case iNode.NODE_TYPE_I_LINK_POPUP:
				title = "Popup Link";
			break;
		}
		
//		ResultSet rs = null;
		String url = null;
//		String qry = "INSERT INTO ent_node (Title, UserID, DateCreated, " +
//			"DateModified, DateAltered, ItemTypeID, FolderFlag) VALUES ('" + 
//			title + "', " + user_id + ", NOW(), NOW(), NOW(), " + 
//			node_type + ", " + folder_flag_i + ");";

//System.out.println("doAdd.doPost new stub node query \n\t " + qry);
		Connection conn = null;
		try
		{
			conn = cd.getSQLManager().getConnection();
			// insert stub node
//			if(node_type != iNode.NODE_TYPE_I_CONCEPTS)
//			{
//				AppDaemon.executeUpdate(conn, qry);
//				
//				// get last inserted id
//				qry = "SELECT MAX(LAST_INSERT_ID(NodeID)) AS LastID FROM ent_node WHERE UserID=" + user_id + ";";
//				PreparedStatement statement = conn.prepareStatement(qry);
//				rs = AppDaemon.executeStatement(statement);
//				while(rs.next())
//				{
//					node_id = rs.getInt("LastID");
//				}
//				rs.close();
//				rs = null;
//				statement .close();
//				statement = null;
//	
//				// connect new node to the parent
//				qry = "INSERT INTO rel_node_node (ParentNodeID, ChildNodeID, Weight, OrderRank)" +
//					" VALUES (" + edited_node.getId() + ", " + node_id + ", 1, " + (edited_node.getChildren().size()+1) + ");";
//				AppDaemon.executeUpdate(conn, qry);
//			}
			
			iNode new_node = null;
			// create actual node object and external object if necessary
			switch (node_type) 
			{
				case iNode.NODE_TYPE_I_FOLDER:
				case iNode.NODE_TYPE_I_UNTYPDOC:
				case iNode.NODE_TYPE_I_TOPIC_FOLDER:
				{
					new_node = new NodeUntyped(node_id, title, 
							node_type, "", "", folder_flag, "", user, null);
				}
				break;
				case iNode.NODE_TYPE_I_QUIZ:
				case iNode.NODE_TYPE_I_DISSECTION:
				case iNode.NODE_TYPE_I_WADEIN:
				case iNode.NODE_TYPE_I_CODEEXAMPLE:
				case iNode.NODE_TYPE_I_LINK:
				{
					ExternalResourceVisual erv = new ExternalResourceVisual(0, title, "",
							user, "", "");
					new_node = new NodeUntyped(node_id, title, 
							node_type, "", "", folder_flag, "", user, erv);
				}
				break;
				case iNode.NODE_TYPE_I_KARELROBOT:
				{
					ExternalResourceDownloadable erd = new ExternalResourceDownloadable(
							0, title, "", user, "", "");
					new_node = new NodeUntyped(node_id, title, 
							node_type, "", "", folder_flag, "",	user, erd);
				}
				case iNode.NODE_TYPE_I_SYS_QUIZGUIDE: //System QuizGUIDE
				case iNode.NODE_TYPE_I_SYS_NAVEX: // System NavEx
				case iNode.NODE_TYPE_I_SYS_WADEIN: // System WADEIn II
				case iNode.NODE_TYPE_I_LINK_POPUP:
				{
					// create CodeExample object
					ExternalResourcePopup erp = new ExternalResourcePopup(0, title, "",
							user, "", "");
					new_node = new NodeUntyped(node_id, title, 
							node_type, "", "", folder_flag, "", user, erp);
				}
				break;
				case iNode.NODE_TYPE_I_PAPER:
				{
					Paper p = new Paper(0 /*paper_id*/, title, "",
							new GregorianCalendar().get(Calendar.YEAR),
							"", "", "", user);
					new_node = new NodeUntyped(node_id, title, 
							node_type, "", "", folder_flag, "",
							user, p);
				}
				break;
				case iNode.NODE_TYPE_I_SUMMARY:
				{
					Paper p = (Paper)edited_node.getExternalObject();
					Summary s = new Summary(0/*summary_id*/, user, "", "Empty Summary", p);
					p.getSummaries().add(s);
					
					new_node = new NodeUntyped(node_id, title, 
							node_type, "", "", folder_flag, "",
							user, s);
				}
				break;
				case iNode.NODE_TYPE_I_CONCEPTS:
				{
					int ed_node_id = edited_node.getId();
					NodeConcepts nc = new NodeConcepts(ed_node_id, 
								title, "", user);
					new_node = new NodeUntyped(
							-res_map.getNextVirtualNodeId(), title, 
							node_type, "", "", folder_flag, "",
							user, nc);
				}
				break;
			}
			// add new node to the resource map and rewrite resource
			// map into session
			res_map.getNodes().add(new_node);
			// add node into "pending"
			res_map.setPendingNode(new_node);
			
//System.out.println(" nodeeeee " + edited_node.getTitle());
			edited_node.getChildren().add(new_node);
			new_node.setParent(edited_node);
			
			// apply rights to the node
			
			// url
			url = request.getContextPath() + 
				"/content/Show?" +  ClientDaemon.REQUEST_NODE_ID +
				"=" + new_node.getId() + "&" + 
				ClientDaemon.REQUEST_SHOW_MODE + "=" +
				Show.SHOW_MODE_EDIT;
		}//end -- try
		catch (Exception e) { e.printStackTrace(System.err); }
		finally
		{
			SQLManager.recycleObjects(conn, null, null);

/*System.out.println("doAdd.doPost : url = " + url);
			this.redirectToURL(request, response, url);
System.out.println("After forward");/**/
			PrintWriter out = response.getWriter();
			out.println("<script>");
			out.println("	if(parent != null)");
			out.println("		parent.location = '" + url + "';");
			out.println("</script>");
			out.println("<body>");
			out.println("</body>");

//			edited_node.showView(out, request);/**/
		}
	}
	
}
