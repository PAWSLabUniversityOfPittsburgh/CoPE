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

import edu.pitt.sis.paws.cope.ResourceMap;

public class doGroup extends servletGeneric 
{
	static final long serialVersionUID = -2L;

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}

	public void doGet(HttpServletRequest request, 
		HttpServletResponse response) throws ServletException, IOException
	{
		System.out.println("doGroup.doGet Warning, using get method!");
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, 
		HttpServletResponse response) throws ServletException, IOException
	{
//System.out.println("doGroup.doPost starting...");
		// init the servlet

		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).
			intValue();

		String user_group_s = request.getParameter(ClientDaemon.REQUEST_USER_GROUP);
		int user_group = 0;
//System.out.println("doGroup.doPost user_group_s = " + user_group_s);
		if(user_group_s == null)
		{
			user_group = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		}
		else
			user_group = Integer.parseInt(user_group_s);
		
//		ResourceMap res_map = cd.createUserResourceMap(user_id, user_group);
		ResourceMap res_map = ResourceMap.createUserResourceMap(user_id, user_group, cd.getSQLManager());

//System.out.println("doGroup.doPost res_map.nodes.# " + res_map.getNodes().size());

		// record group and map into session and set session inited flag
		session.setAttribute(ClientDaemon.SESSION_GROUP_ID, new Integer(user_group));
		session.setAttribute(ClientDaemon.SESSION_RES_MAP , res_map);
		session.setAttribute(ClientDaemon.SESSION_INITED, "TRUE");

		redirectToHome(request, response);
//System.out.println("doGroup.doPost done.");
	}
}
