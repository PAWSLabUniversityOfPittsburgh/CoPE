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
import javax.servlet.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.utils.SQLManager;

public class doEdit extends servletGeneric
{
	static final long serialVersionUID = -2L;

//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
//	{
//		doPost(request, response);
//	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		request.setCharacterEncoding("utf-8");
		
		// first get the id of the node being edited
		String id_s = (String)request.getParameter(iNode.NODE_FRMFIELD_ID);
		if(id_s == null)
		{
			System.out.println("doEdit.doPost: ERROR Id not specified!");
			return;
		}
		
		int id = Integer.parseInt(id_s);
//System.out.println("doEdit.doPost node id = " + id);				
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap)session.getAttribute(
			ClientDaemon.SESSION_RES_MAP);
		iNode edited_node = res_map.getNodes().findById(id);
		if(edited_node == null)
		{
			System.out.println("doEdit.doPost: ERROR Id not found!");
			return;
		}
//System.out.println("doEdit.doPost NodeID="+id);

		String url = null;
		Connection conn = null;
		try
		{
			conn = cd.getSQLManager().getConnection();
			
//String new_summary = request.getParameter(Summary.SUMMARY_FRMFIELD_SUMMARY);
//System.out.println("doEdit: new_summary='" + new_summary + "'");

			// 0. update own properties
			int changes = edited_node.updateObject(request);
			
			// 1. if the node is being edited
			if(id != 0)
				edited_node.saveToDB(conn, request, null, changes);
			
			// 2. if it is a new node being added
			else
			{
//System.out.println("doEdit.doPost saving blank node");
				id = edited_node.addToDB(conn, request, null);
			}
			
			url = request.getContextPath() + 
				"/content/Show?" +  ClientDaemon.REQUEST_NODE_ID +
				"=" + id;
		}//end -- try
		catch (Exception e) { e.printStackTrace(System.err); }
		finally
		{
			SQLManager.recycleObjects(conn, null, null);

			PrintWriter out = response.getWriter();
			out.println("<script>");
			out.println("	if(parent != null)");
			out.println("		parent.location = '" + url + "';");
			out.println("</script>");
			out.println("<body>");
			out.println("</body>");

		}
	}
}
