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
import java.sql.*;

import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.utils.SQLManager;

//import edu.pitt.sis.paws.core.*;

public class ExternalResourceVisual extends ExternalResource
{
	
	/**Constructor of the ExternalResourceVisual - for using addToDB only */
	public ExternalResourceVisual()
	{
		super(0,"","","","");
//		icon_url = "";
	}

	// Constructors & cloners

	/** Main constructor of the ExternalResourceVisual
	 * @param _id - ExternalResourceVisual id
	 * @param _title - ExternalResourceVisual title
	 * @param _url - url of the ExternalResourceVisual
	 * @param _user - creator of the ExternalResourceVisual
	 */
	public ExternalResourceVisual(int _id, String _title, String _uri, User _user, String _url, String _ext_id)
	{
	      super(_id, _title, _uri, _user, ((_url==null)?"":_url), _ext_id);
//		icon_url = _icon_url;
	}
	
	/** Constructor for cloning (creator will be set to null)
	 * @param _id - ExternalResourceVisual id
	 * @param _title - ExternalResourceVisual title
	 * @param _url - url of the ExternalResourceVisual
	 */
	public ExternalResourceVisual(int _id, String _title, String _uri, String _url, String _ext_id)
	{
		super(_id, _title, _uri, ((_url==null)?"":_url), _ext_id);
//		icon_url = _icon_url;
	}

	/** This cloner should be used in the interest of the Node object when
 	 * the latter emulates ExternalResourceVisual and is being cloned itself.
	 * @return copy of the ExternalResourceVisual object */
	public ExternalResourceVisual clone() 
	{
		ExternalResourceVisual copy = null;
		try
		{ 
			copy = new ExternalResourceVisual(this.getId(), new String(this.getTitle()), new String(this.getURI()),
				new String(this.url), new String(this.ext_id)); 
		}
		catch (Exception e) { e.printStackTrace(System.out); }
		return copy;
	}
	
	// IMPLEMENTATION OF HTML Visualization
	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException
	{
		String activity_url = Resource.addIdentityToURL(this.url, request);
//System.out.println("ExternalResourceVisual.showView starting...");	
//		System.out.println("ExternalResourceVisual.showView forwarding to URL='" + this.url + "'");	
//System.out.println("ExternalResourceVisual.showView forwarding to URL='" + activity_url + "'");	
		out.println("<script type='text/javascript'>");
		out.println("<!--");
		out.println("	window.location = '" + activity_url + "';");
		out.println("-->");
		out.println("</script>");
	}

	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		out.println("<script type='text/javascript'>");
		out.println("function checkNumeric(node)");
		out.println("{");
		out.println("	var val = node.value;");
		out.println("	for(var i=0;i<val.length;i++)");
		out.println("		if(val.charAt(i)<'0' || val.charAt(i)>'9')");
		out.println("		{");
		out.println("			if(i==(val.length-1))");
		out.println("			{");
		out.println("				node.value = val.substr(0,i);");
		out.println("			}");
		out.println("			else");
		out.println("			{");
		out.println("				node.value = val.substr(0,i)+val.substr(i+1,val.length-i-1);");
		out.println("			}");
		out.println("			alert('Only numbers are allowed for this parameter!');");
		out.println("		}");
		out.println("}");
		out.println("</script>");
		out.println("</head>");
		out.println("<body>");
	}

	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
//System.out.println("Paper.showEdit starting...");	
		HttpSession session = request.getSession();
		iNode	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
		int node_id = current_node.getId();
		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/doEdit' target='_top'>");
		out.println("<!-- ID field -->");
		out.println("<input name='" + iNode.NODE_FRMFIELD_ID + "' type='hidden' value='" + node_id + "'>");

		out.println("<!-- Title field -->");
		out.println("<div style='font-family:\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Title</div>");
		out.println("<div style='padding:0px 0px 10px 15px;' title='Title'><input id='" + iNode.NODE_FRMFIELD_TITLE + "' name='" + iNode.NODE_FRMFIELD_TITLE + "' type='text' value='" + this.getTitle() + "' size='70' maxlength='255'></div>");

		out.println("<!-- URL field -->");
		out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>URL</div>");
		out.println("<div style='padding:0px 0px 10px 15px;' title='URL'><input id='" + iNode.NODE_FRMFIELD_URL + "' name='" + iNode.NODE_FRMFIELD_URL + "' type='text' value='" + this.url + "' size='70' maxlength='255'></div>");

		out.println("<p>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit()");
		out.println("{");
		out.println("	var error_msg = 'Fields: \\n';");
		out.println("	var error = false");
		out.println("	");
		out.println("	if( document.edit." + iNode.NODE_FRMFIELD_TITLE + ".value.length == 0 )");
		out.println("	{");
		out.println("		error_msg += ' * Title; \\n';");
		out.println("		error = true;");
		out.println("	}");
		out.println("	error_msg += ' should not contail an empty string';");
		out.println("	");
		out.println("	if(error) alert(error_msg );");
		out.println("	else");
		out.println("		document.edit.submit();");
		out.println("}");
		out.println("</script>");

		String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";

		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</p>");
		out.println("</form>");
	}
	
	public int updateObject(HttpServletRequest request) throws Exception
	{
		int changes = iNode.NODE_CHANGE_NONE;
		
		String new_title = request.getParameter(iNode.NODE_FRMFIELD_TITLE);
		String new_url = request.getParameter(iNode.NODE_FRMFIELD_URL);
		
		// Title
		if(!this.getTitle().equals(new_title))
		{
			this.setTitle(new_title);
			changes |= iNode.NODE_CHANGE_TITLE;

			for(int i=0; i<this.getOwners().size(); i++)
				this.getOwners().get(i).setTitle(new_title);
		}
		
		// URL 
		if(!this.getURL().equals(new_url))
		{
			this.setURL(new_url);
			changes |= iNode.NODE_CHANGE_URL;

			// change all owners
			for(int i=0; i<this.getOwners().size(); i++)
				this.getOwners().get(i).setURL(new_url);
		}
		
		return changes;
	}
	
	// IMPLEMENTATION OF DBStored interface
	public void saveToDB(Connection conn, HttpServletRequest request, iNode node, int changes)
		throws Exception
	{
		// if not stored in DB - exit
		if(!stored_in_db) return;

		String qryN = "";
		String qryQ = "";

		// Process Title
		if((changes & iNode.NODE_CHANGE_TITLE) > 0)
		{
			String title_dequoted = SQLManager.stringUnquote(this.getTitle());
			
			qryN += ((qryN.length() > 0)?" ,":"") + "Title='" +
				title_dequoted + "'";
			qryQ += ((qryQ.length() > 0)?" ,":"") + "Title='" +
				title_dequoted + "'";
		}
		
		// Process URL
		if((changes & iNode.NODE_CHANGE_URL) > 0)
		{
			String url_dequoted = SQLManager.stringUnquote(this.getURL());
			
			qryN += ((qryN.length() > 0)?" ,":"") + "URL='" +
				url_dequoted + "'";
			qryQ += ((qryQ.length() > 0)?" ,":"") + "URL='" +
				url_dequoted + "'";
		}

		if(!qryN.equals(""))
		{// Save changes into Node if any
			qryN += ((qryN.length() > 0)?" ,":"") + "DateModified=NOW()";		

//			for(int i=0; i<owners.size(); i++)
//				where_in += (((where_in.length()>0)?",":"")+owners.get(i).getId());
//			String big_qryN = "UPDATE ent_node SET " + qryN + 
//				" WHERE NodeID IN(" + where_in + ");";

			String big_qryN = "UPDATE ent_node SET " + qryN + 
					" WHERE ExtID=" + this.getId() + " AND ItemTypeID=" + 
					this.getOwners().get(0).getNodeType() + ";";

			big_qryN = SQLManager.stringUnbreak(big_qryN);

			Statement stmt = conn.createStatement();
			stmt.executeUpdate( big_qryN);
			stmt.close();
		}// -- end -- Save changes into Node if any
			
		if(!qryQ.equals(""))
		{// Save changes into Paper if any
			qryQ += ((qryQ.length() > 0)?" ,":"") + "DateModified=NOW()";		

			String big_qryQ = "UPDATE ent_activity SET " + qryQ + 
				" WHERE ActivityID=" + this.getId() + ";";

			big_qryQ = SQLManager.stringUnbreak(big_qryQ);
			
			Statement stmt = conn.createStatement();
			stmt.executeUpdate( big_qryQ);
			stmt.close();
		}// -- end -- Save changes into Paper if any
	}

	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{
		int activity_id = 0;

		HttpSession session = request.getSession();
		ResourceMap resmap = ((ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP));

		if(this.isStoredInDB())
		{
			int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();

			String qry = "INSERT INTO ent_activity (Title,"+
				" URI, URL, UserID, " + 
				"DateCreated, DateModified, DateAltered) " +
				"VALUES ('" + this.getTitle() + "', 1, " +
				"'" + this.getURL() + "'," + user_id + ",NOW(),NOW(),NOW());";
			
			Statement stmt = conn.createStatement();
			stmt.executeUpdate( qry);
			stmt.close();
			
			qry = "SELECT MAX(LAST_INSERT_ID(ActivityID)) AS LastID FROM ent_activity WHERE UserID=" + user_id + ";";
			
			PreparedStatement statement = conn.prepareStatement(qry);
			ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				activity_id = rs.getInt("LastID");
			}
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			
			// Connect to Node
			qry = "UPDATE ent_node SET ExtID=" + activity_id + " WHERE NodeID=" +
					node.getId() + ";";
			Statement stmt1 = conn.createStatement();
			stmt1.executeUpdate( qry);
			stmt1.close();
			
			// Mockup URL
			qry = "UPDATE ent_activity SET ExtID=" + activity_id + " WHERE ActivityID=" +
					activity_id + ";";

			Statement stmt2 = conn.createStatement();
			stmt2.executeUpdate( qry);
			stmt2.close();
		}
		
		this.setId(activity_id);

		resmap.getActivities().remove(this);
		resmap.getActivities().add(this);
		
		session.setAttribute(ClientDaemon.SESSION_RES_MAP, resmap);
		
		return activity_id;
	}
	
//	public String getIconURL() { return icon_url; }
}
