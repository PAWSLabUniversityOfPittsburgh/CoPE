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
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.utils.SQLManager;

public class doAuthenticate extends servletGeneric
{
	
	static final long serialVersionUID = -2L;

//	private Connection conn = null;

//	public void init(ServletConfig config) throws ServletException
//	{
//		super.init(config);
//		try{ conn = cd.getConnection(); }
//		catch (Exception e) {e.printStackTrace(System.out); }
//	}

	public void destroy()
	{
//		try{ if(conn != null ) conn.close(); }
//		catch (Exception e) {e.printStackTrace(System.out); }
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
//System.out.println("doAuthenticate.doPost starting...");
//System.out.println("doAuthenticate.doPost done.");
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
//System.out.println("doAuthenticate.doGet starting...");
		// Check  for logoff option
		String logoff = req.getParameter("logoff");
		if(logoff != null)
		{
			req.getSession().invalidate();
			forwardToIndex(req, res);
			return;
		}
		// Get the authentication parameters
		String user_login = req.getRemoteUser();
		// Get the SesionManager
		cd = ClientDaemon.getInstance();
		// Check the user login
//		boolean authenticated = false;
		String qry = null;
//		String qry2 = null;
		ResultSet rs = null;
		Connection conn = null;
		try
		{
			conn = cd.getSQLManager().getConnection();
			// Initialize session and resource map
			// Find and put user id
			qry = "SELECT UserID FROM ent_user WHERE Login='" +
				user_login + "'";
			PreparedStatement statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			int user_id = 0;
			if(rs.next())
			{
				user_id = rs.getInt("UserID");
			}
			rs.close();
			rs = null;
			statement .close();
			statement = null;

			// Set user id session parameter
			HttpSession session = req.getSession();
			session.setAttribute(ClientDaemon.SESSION_USER_ID,
				new Integer(user_id));

			// Select the groups
			Vector<Integer> group_ids = new Vector<Integer>();
			Vector<String> group_names = new Vector<String>();
			qry = "SELECT uu.ParentUserID, u.Name FROM rel_user_user uu" +
				" LEFT JOIN ent_user u ON(u.UserID=uu.ParentUserID) " +
				"WHERE uu.ChildUserID = " + user_id;
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				group_ids.add(new Integer(rs.getInt("ParentUserID")));
				group_names.add(new String(rs.getString("Name")));
			}
			rs.close();
			rs = null;
			statement .close();
			statement = null;

			String forward = "";
			switch (group_ids.size())
			{
				case 0:
				{// No group associated
					forward = ClientDaemon.CONTEXT_ERRORURL + 
						"?msg=You are not a member of any user" +
						" group! Please contact administrator.";
					forwardToURL(req, res, forward);
				}
				break;
				case 1:
				{// One group associated
					session.setAttribute(ClientDaemon.SESSION_GROUP_ID, group_ids.get(0));
//System.out.println("doAuthenticate.doGet One group only");
//					forwardToURL(req, res, ClientDaemon.CONTEXT_GROUPURL_VAL);
					PrintWriter out = res.getWriter();
					out.println("<html>");
					out.println("<head><title>CoPE - Collaborative Paper Exchange. Select User Group</title></head><body>");
					out.println("<CENTER><img src='" + req.getContextPath()+ "/assets/CoPELogo2.gif' alt='Knowledge Tree Logo' align='middle'></CENTER>");
					out.println("<CENTER>");
					out.println("<FORM NAME='group' ACTION='" + req.getContextPath() + ClientDaemon.CONTEXT_GROUPURL + "' METHOD='POST'>");
//					out.println("	<table border='0' cellspacing='2' cellpadding='2' align='center'>");
//					out.println("		<tr> ");
//					out.println("			<td width='80'>User group</td>");
//					out.println("			<td width='190'>");
					out.println("				<select name='" +  ClientDaemon.REQUEST_USER_GROUP + "'>");
					out.println("\t\t\t\t\t<option value='"+group_ids.get(0)+"' selected>"+group_names.get(0)+"</option>");
					out.println("				</select>");
//					out.println("			</td> ");
//					out.println("		</tr>");
//					out.println("		<tr> ");
//					out.println("		  <td colspan='2'>");
					out.println("				<div><input type='Submit' value='Submit'/></div>");
//					out.println("		  </td>");
//					out.println("		</tr>");
//					out.println("	</table>");
					out.println("</FORM>");
					out.println("</CENTER>");

//					out.println("<script language='javascript'>");
//					out.println("document.group.submit();");
//					out.println("</script>");
					out.println("<script language='javascript'>");
					out.println("document.getElementById('" + ClientDaemon.REQUEST_USER_GROUP + "').focus();");
					out.println("</script>");

					out.println("</body></html>");
					out.close();
					
				}
				break;
				default:
				{// Multiple groups associated
//System.out.println("doAuthenticate.doGet Multiple groups");
					PrintWriter out = res.getWriter();
					out.println("<html>");
					out.println("<head><title>CoPE - Collaborative Paper Exchange. Select User Group</title></head>");
					out.println("<body>");
					out.println("<CENTER>");


//					out.println("<H2 align='center'>User group</H2>");
//					out.println("<H4 align='center'>Please choose the user group you want to login into</H4>");
					out.println("<CENTER><img src='" + req.getContextPath()+ "/assets/CoPELogo2.gif' alt='Knowledge Tree Logo' align='middle'></CENTER>");
					out.println("<FORM ACTION='" + req.getContextPath() + ClientDaemon.CONTEXT_GROUPURL + "' METHOD='POST'>");
//					out.println("	<table border='0' cellspacing='2' cellpadding='2' align='center'>");
//					out.println("		<tr> ");
//					out.println("			<td width='80'>User group</td>");
//					out.println("			<td width='190'>");
					out.println("				<select name='" +  ClientDaemon.REQUEST_USER_GROUP + "' id='" +  ClientDaemon.REQUEST_USER_GROUP + "'>");
					for(int i=0;i<group_ids.size();i++)
					{
						out.println("\t\t\t\t\t<option value='"+group_ids.get(i)+"'>"+group_names.get(i)+"</option>");
					}
					out.println("				</select>");
//					out.println("			</td> ");
//					out.println("		</tr>");
//					out.println("		<tr> ");
//					out.println("		  <td colspan='2'>");
					out.println("				<div><input type='Submit' value='Submit' id='Submit'/><div/>");
//					out.println("				<%");
//					out.println("					ArrayList credList = (ArrayList) request.getAttribute('creds');");
//					out.println("				%>");
//					out.println("				<input name='user_id' type='hidden' value='<%=credList.get(0)%>'>	");
//					out.println("				<input name='user_login' type='hidden' value='<%=credList.get(1)%>'>");
//					out.println("		  </td>");
//					out.println("		</tr>");
//					out.println("	</table>");
					out.println("</FORM>");
					out.println("</CENTER>");
					out.println("<script language='javascript'>");
					out.println("document.getElementById('Submit').focus();");
					out.println("</script>");

					out.println("</body></html>");
					out.close();

/*					ArrayList credList = new ArrayList(2);
					credList.add(new Integer(user_id));
					credList.add(user_login);

					ArrayList groupList = new ArrayList(num_of_bits(user_groups)*2);
					rs = ClientDaemon.executeStatement(conn, qry2);
					while(rs.next())
					{
						groupList.add(new Integer(rs.getInt("UserGroupID")));
						groupList.add(rs.getString("Name"));
					}
					req.setAttribute("groups", groupList);
					req.setAttribute("creds", credList);
					forwardToURL(req, res, "/jspGroup");
					/**/
				}
				break;
			}
		}//end -- try
		catch (Exception e) { e.printStackTrace(System.err); }
		finally {SQLManager.recycleObjects(conn, null, null);}/**/

/*		boolean authenticated = true; // delete
		PrintWriter out = res.getWriter();
		out.println("<html>");

		out.println("<head><title>Authentication</title></head>");
		out.println("<body>");
		String msg = (authenticated)?"is authenticated":"<p style='color:red'>failed to authenticate</p>";
		out.println("<p>The user " + msg + ".</p>");
		out.println("</body></html>");
		out.close();
/**/
//System.out.println("doAuthenticate.doGet done.");
	}// -- end doGet
}
