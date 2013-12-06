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

public class doDelete extends servletGeneric
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
			System.out.println("doDelete.doPost: ERROR Id not specified!");
			return;
		}
		int id = Integer.parseInt(id_s);
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap)session.getAttribute(
			ClientDaemon.SESSION_RES_MAP);
//System.out.println("@@@ bin_node_id " + res_map.getBinNodeId());
		iNode deleted_node = res_map.getNodes().findById(id);
		if(deleted_node == null)
		{
			System.out.println("doDelete.doPost: ERROR Id not found!");
			return;
		}

		String url = ""; // return URL
		Connection conn = null;
		try
		{
			conn = cd.getSQLManager().getConnection();
			
			// Remove connection to parent if exists
			String qry_rem_parent = "";
			iNode parent = deleted_node.getParent();
			if( parent != null)
			{
				qry_rem_parent = "DELETE FROM rel_node_node WHERE " +
					"ParentNodeID=" + deleted_node.getParent().getId() +
					" AND ChildNodeID=" + deleted_node.getId()  + ";";
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(qry_rem_parent);
				stmt.close();
				stmt = null;
			}

			// decide - delete node or connect to bin
			boolean disconnect = true;
			if((deleted_node.getExternalObject() != null) &&
				(deleted_node.getExternalObject().getOwners().size() > 1))
			{
				disconnect = false;
System.out.println("~~~ Delete node of the external object ");
			}

			if(disconnect) // connect node to bin
			{
				// Connect to the bin node
//System.out.println("bin_node_id: " + (res_map.getBinNodeId()));
				String qry = "INSERT INTO rel_node_node (ParentNodeID," +
					"ChildNodeID,Weight,OrderRank) VALUES(" +
					res_map.getBinNodeId() + "," +
					deleted_node.getId() + ",1,1);";
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(qry);
				stmt.close();
				stmt = null;
			}
			else // delete node
			{
				qry_rem_parent = "DELETE FROM rel_node_node WHERE " +
					" ChildNodeID=" + deleted_node.getId()  + ";";
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(qry_rem_parent);
				stmt.close();
			}


			url = request.getContextPath() +
				"/content/Show" +
				((parent != null)?
					"?" +  ClientDaemon.REQUEST_NODE_ID +
					"=" + parent.getId()
					:""
				);

			// delete node in the res_map
			res_map.sendNodeToBin(deleted_node, disconnect);

		}//end -- try
		catch (Exception e) { e.printStackTrace(System.err); }
		finally
		{
			SQLManager.recycleObjects(conn, null, null);

/*System.out.println("doAdd.doPost : url = " + url);
			this.redirectToURL(request, response, url);
System.out.println("After forward");/**/
			PrintWriter out = response.getWriter();
			out.println("<script language='javascript'>");
			out.println("//	if(parent != null)");
			out.println("		top.location = '" + url + "';");
			out.println("</script>");
			out.println("<body>");
			out.println("</body>");

//			edited_node.showView(out, request);/**/
		}
	}
}
