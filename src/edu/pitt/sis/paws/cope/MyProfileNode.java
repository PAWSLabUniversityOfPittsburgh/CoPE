/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/** 
 * This class handles My Profile page
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.paws.cope;

import java.io.*;
import java.sql.*;
import java.util.*;
//import java.util.regex.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import edu.pitt.sis.paws.core.*;
import edu.pitt.sis.paws.core.utils.SQLManager;

public class MyProfileNode extends Node 
{
	// Constants
	protected static final String MYPROFILE_OLD_LOGIN = "fld_old_login";
	protected static final String MYPROFILE_NEW_LOGIN = "fld_new_login";
	protected static final String MYPROFILE_OLD_PASS = "fld_old_pass";
	protected static final String MYPROFILE_NEW_PASS = "fld_new_pass";
	protected static final String MYPROFILE_SESS_RESULT = "myprofile_result";
	protected static final String MYPROFILE_PREF_CHANGED = "frm_pref_changed";

	protected User user;
	
	public MyProfileNode(int _id, User _user)
	{
		super(_id, "My Profile", NODE_TYPE_I_MYPROFILE, ""/*descr*/, 
			""/*url*/, false/*folderflag*/, null/*external*/);
		user = _user;
	}

	public iNode clone(User _user, boolean _set_xtrnal_obj) { return null; }	// Cannot be cloned

	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		if( ((this.node_type != iNode.NODE_TYPE_I_FOLDER)&&(this.node_type != iNode.NODE_TYPE_I_NONE)&&(this.xtrnl_object != null)) )
		{
			xtrnl_object.showEdit(out, request, cancel_to_url);
			return;			
		}
		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/doEdit' target='_top'>");
		out.println("<!-- ID field -->");
		out.println("<input name='" + NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>");
		out.println("<!-- Preference Changed field -->");
		out.println("<input name='" + MYPROFILE_PREF_CHANGED + "' type='hidden' value=''>");

		out.println("<!-- Current Login -->");
//		out.println("<div class='pt_main_subheader_editing_name'>Current Login</div>");
		out.println("<div class='pt_main_subheader_editing_value' title='The Login you currently have'><input name='" + MYPROFILE_OLD_LOGIN + "' type='hidden' value=\"" + user.getLogin() + "\" size='25' maxlength='15'></div>");
		out.println("<!-- Current Password -->");
		out.println("<div class='pt_main_subheader_editing_name'>Current Password</div>");
		out.println("<div class='pt_main_subheader_editing_value' title='The Password you currently have'><input name='" + MYPROFILE_OLD_PASS + "' type='password' value=\"\" size='25' maxlength='15'></div>");

//		out.println("<!-- New Login -->");
//		out.println("<div class='pt_main_subheader_editing_name'>New Login</div>");
//		out.println("<div class='pt_main_subheader_editing_value' title='The Login you want to have'><input name='" + MYPROFILE_NEW_LOGIN + "' type='text' value=\"\" size='25' maxlength='15'></div>");
		out.println("<!-- New Password -->");
		out.println("<div class='pt_main_subheader_editing_name'>New Password</div>");
		out.println("<div class='pt_main_subheader_editing_value' title='The Password you want to have'><input name='" + MYPROFILE_NEW_PASS + "' type='password' value=\"\" size='25' maxlength='15'></div>");

		out.println("<!-- Preferences -->");
		out.println("<div class='pt_main_subheader_editing_name'>Preferences</div>");
		// Get all available ontologies
		HttpSession session = request.getSession();
		ResourceMap resmap = (ResourceMap)session.getAttribute(
			ClientDaemon.SESSION_RES_MAP);
		ItemVector<iConcept> all_ontologies = resmap.getAllOntologies();
		out.println("<div style='padding:0px 5px 15px 5px'>");
		for(int i=0; i < all_ontologies.size(); i++)
			all_ontologies.get(i).outputTreeConcept(out, request, user.getConcepts(), 0, true, " onClick='change();'");
		out.println("</div>");


		out.println("<div>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:document.edit.submit();'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";
		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</div>");
		out.println("</form>");
	}
	
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		out.println("<script type='text/javascript' >");
		out.println("<!--");
//		out.println("function mySubmit()");
//		out.println("{");
//		out.println("	var error_msg = 'Fields:\\n';");
//		out.println("	var error = false");
//		out.println("	");
//		out.println("	if( document.edit.fld_old_login.value.length == 0 )");
//		out.println("	{");
//		out.println("		error_msg += ' * Old Login\\n';");
//		out.println("		error = true;");
//		out.println("	}");
//		out.println("	if( document.edit.fld_old_pass.value.length == 0 )");
//		out.println("	{");
//		out.println("		error_msg += '  * Old Password\\n';");
//		out.println("		error = true;");
//		out.println("	}");
//		out.println("	if( document.edit.fld_new_login.value.length == 0 )");
//		out.println("	{");
//		out.println("		error_msg += '  * New Login\\n';");
//		out.println("		error = true;");
//		out.println("	}");
//		out.println("	if( document.edit.fld_new_pass.value.length == 0 )");
//		out.println("	{");
//		out.println("		error_msg += '  * New Password\\n';");
//		out.println("		error = true;");
//		out.println("	}");
//		out.println("	error_msg += 'should not be empty!';");
//		out.println("	");
//		out.println("	if(error) alert(error_msg );");
//		out.println("	else");
//		out.println("	{");
//		out.println("		document.edit.submit();");
//		out.println("	}");	
//		out.println("}");
		out.println("	function flip_dir_icon(node)");
		out.println("	{");
		out.println("		node.childNodes[0].style.display = (node.childNodes[0].style.display == 'none') ? 'inline' : 'none';");
		out.println("		node.childNodes[1].style.display = (node.childNodes[1].style.display == 'none') ? 'inline' : 'none';");
		out.println("		node.parentNode.nextSibling.style.display = (node.parentNode.nextSibling.style.display == 'none') ? 'block' : 'none';");
		out.println("	}");
		out.println("	function change()");
		out.println("	{");
		out.println("		document.edit." +  MYPROFILE_PREF_CHANGED + ".value='changed';");
//		out.println("		alert(document.edit." +  MYPROFILE_PREF_CHANGED + ".value);");
		out.println("	}");
		out.println("-->");
		out.println("</script>");
		out.println("</head>");
		out.println("<body>");
	}

	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException
	{
		HttpSession session = request.getSession();
		String message = (String)session.getAttribute(
			MYPROFILE_SESS_RESULT);
		if(message == null)
		{
			out.println("<div class='pt_main_subheader'>Name</div>");
			out.println("<p>" + user.getTitle() + "</p>");
			out.println("<div class='pt_main_subheader'>Login</div>");
			out.println("<p>" + user.getLogin() + "</p>");
			out.println("<div class='pt_main_subheader'>Preferences" +
				"</div>");
			if(user.getConcepts().size()==0)
				out.println("<div>No concepts added</div>");
			for(int i=0; i<user.getConcepts().size(); i++)
				out.println("<div class='pt_concept'>" + 
				user.getConcepts().get(i).getTitle() + "</div>");
		}
		else
		{
			url = request.getContextPath() + 
				"/content/jspMain"; // +  AppDaemon.REQUEST_NODE_ID +
			out.println("<p>" + message + "</p>");
			session.removeAttribute(MYPROFILE_SESS_RESULT);
			out.println("<div>");
			out.println("<a class='pt_main_edit_button' href='" + url + 
				"'>Continue</a>");
			out.println("</div>");
		}
	}
	
	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		HttpSession session = request.getSession();
		String message = (String)session.getAttribute(
			MYPROFILE_SESS_RESULT);
		if(message != null)
		{
			url = request.getContextPath() + 
				"/content/jspMain"; // +  AppDaemon.REQUEST_NODE_ID +
//				"=" + this.getId();
			out.println("<meta http-equiv='refresh' content='5;URL=" +
				url + "'>");
			out.println("</head>");
			out.println("<body style='padding:5px 5px 5px 5px;'>");
		}
		else
		{

			out.println("</head>");
			out.println("<body>");
		}
		
	}

	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{// Doesn't add anything new
		return 0;
	}

	public int updateObject(HttpServletRequest request) throws Exception
	{
		int changes = iNode.NODE_CHANGE_NONE;
		
		// all the changing of the My_Profile object is in saveToDB method
		
		return changes;
	}
	
	public void saveToDB(Connection conn, HttpServletRequest request, iNode node, int changes)
		throws Exception
	{
		String frm_old_login = request.getParameter(MYPROFILE_OLD_LOGIN);
//		String frm_new_login = request.getParameter(MYPROFILE_NEW_LOGIN);
		String frm_old_pass = request.getParameter(MYPROFILE_OLD_PASS);
		String frm_new_pass = request.getParameter(MYPROFILE_NEW_PASS);
		String qry = "";
		String message = "";
		ResultSet rs;
		
		// Work with login/password
		if(frm_old_login != null && !frm_old_login.equals("") )
		{
			qry = "SELECT * FROM ent_user WHERE Login='" + frm_old_login +
				"'" + " AND Pass=MD5('" + frm_old_pass + "');";
			PreparedStatement statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();			
			if(rs.next())
			{// Current login/pass match
				qry = "UPDATE ent_user SET" + /*" Login='" + frm_new_login + 
					"', " + */" Pass=MD5('" + frm_new_pass + "') " + 
					"WHERE Login='" + frm_old_login + "'" +
					" AND Pass=MD5('" + frm_old_pass + "');";
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(qry);
				stmt.close();
				
//				qry = "UPDATE seq_role SET Login='" + frm_new_login + 
//					"' WHERE Login='" + frm_old_login + "';";
//				SQLManager.executeUpdate(conn, qry);			
				message += "<span style='color:green;'>" +
					"Password has been successfully modified." +
					"</span></ br>";
//				user.setLogin(frm_new_login);

			}
			else
			{// Current login/pass do not match
				message += "<span style='color:red;font-weight:bold'>" + 
					"Specified Login/Password sequence not found!" +
					"</span></ br>";
			}
			statement.close();
			statement = null;
		}

		// Check whether preferences changed
		String frm_pref_changed = request.getParameter(MYPROFILE_PREF_CHANGED);
//System.out.println("MyProfileNode.saveToDB frm_pref_changed=" + frm_pref_changed); /// DEBUG
		if((frm_pref_changed != null) && 
			(frm_pref_changed.equals("changed")))
		{
			// Getting session, resource map and a user
			HttpSession session = request.getSession();
			ResourceMap res_map = (ResourceMap) session.
				getAttribute(ClientDaemon.SESSION_RES_MAP);
			int user_id = ((Integer)session.getAttribute(
				ClientDaemon.SESSION_USER_ID)).intValue();
			// Getting the parameters
			Enumeration names = request.getParameterNames();
			ItemVector<iConcept> new_concepts = 
				res_map.getConceptsByParameterEnumeration(names);
//System.out.println("MyProfileNode.saveToDB concepts submited # " + new_concepts.size()); /// DEBUG
			// delete previously added concepts -- logically
			String concept_id_list = "";
			for(int i=0;i<this.user.getConcepts().size();i++)
				concept_id_list += (((concept_id_list.length()>0)?",":"") +
					this.user.getConcepts().get(i).getId());
			this.user.getConcepts().clear();
			// delete previously added concepts -- from DB & add new concepts -- to DB
			rs = null;
			qry = "DELETE FROM rel_user_concept WHERE" +
				" UserID=" + user_id + ";";
			try
			{
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(qry);
				stmt.close();
				for(int i=0; i<new_concepts.size(); i++)
				{
					qry = "INSERT INTO rel_user_concept (UserID, " +
						"ConceptID, DateCreated)" +
						"VALUES (" + user_id + "," + 
						new_concepts.get(i).getId() + ", NOW())";
					Statement stmt1 = conn.createStatement();
					stmt1.executeUpdate(qry);
					stmt1.close();
				}
				
			}
			catch (Exception e) { e.printStackTrace(System.err); }
//			finally { AppDaemon.freeConnection(conn); }
			// add new concepts -- logically
			user.getConcepts().addAll(new_concepts);
	
		}

		HttpSession session = request.getSession();
		if(message != null && !message.equals(""))
			session.setAttribute(MYPROFILE_SESS_RESULT, message);
	}	
	
	public String toString() { return "My Profile"; };
	
}