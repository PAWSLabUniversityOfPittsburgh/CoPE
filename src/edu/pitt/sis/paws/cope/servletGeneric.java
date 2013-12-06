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

public class servletGeneric extends HttpServlet 
{
	static final long serialVersionUID = -2L;

	protected ClientDaemon cd;

	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		// Get the Session manager
//System.out.println("servletGeneric: Starts check init");
		cd = ClientDaemon.getInstance();
	}
	
	protected void doCheckInit(HttpServletRequest req, HttpServletResponse res) 
		throws ServletException, IOException 
	{
//System.out.println("servletGeneric.doCheckInit session inited. = " + ClientDaemon.isSessionInited(req.getSession(false))); /// DEBUG
		if(!ClientDaemon.isSessionInited(req.getSession(false)))
		{
			this.forwardToURL(req,res, ClientDaemon.CONTEXT_AUTHURL);
		}
	}

	protected static int num_of_bits(long mask)
	{
		int nob = 0;
		int max = (int)Math.ceil(Math.log(mask)/Math.log(2));
		for(int i=1;i<max;i++)
			if((mask&i)>0)nob++;
		return nob;
	}
	
	public void forwardToIndex(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		RequestDispatcher disp;
//System.out.println("a2TeAL: svtGneric: forwardToIndex" + req);	
		disp = req.getRequestDispatcher(ClientDaemon.CONTEXT_INDEXURL);
		disp.forward(req, res);
	}
	
	public void forwardToHome(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		RequestDispatcher disp;
//System.out.println("a2TeAL: svtGneric: forwardToHome" + req);	
		disp = req.getRequestDispatcher(ClientDaemon.CONTEXT_HOMEURL);
		disp.forward(req, res);
	}

	public void redirectToHome(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		String destinationURL = res.encodeRedirectURL(
			 req.getContextPath() + ClientDaemon.CONTEXT_HOMEURL);
//System.out.println("servletGeneric.redirectToHome redirURL: " + destinationURL);
		res.sendRedirect(destinationURL);
	}

	public void redirectToURL(HttpServletRequest req, HttpServletResponse res,
		String URL) throws ServletException, IOException
	{
		res.sendRedirect(URL);
System.out.println("servletGeneric.redirectToURL redirURL: " + URL);
	}

	public void forwardToURL(HttpServletRequest req, HttpServletResponse res, 
		String URL) throws ServletException, IOException
	{
//System.out.println("a2TeAL: svtGneric: forwardToURL~ req " + req);	
//System.out.println("a2TeAL: svtGneric: forwardToURL~ res " + res);	
//System.out.println("a2TeAL: svtGneric: forwardToURL~ URL " + URL);	
		RequestDispatcher disp;
		disp = req.getRequestDispatcher(URL);
		disp.forward(req, res);
	}
	
	public void includeURL(HttpServletRequest req, HttpServletResponse res,
		String URL) throws ServletException, IOException
	{
		RequestDispatcher disp;
		disp = req.getRequestDispatcher(URL);
//System.out.println("Dispatcher null? " + (disp==null));		
		disp.include(req, res);
	}
	
/*	public boolean isAuthInfoNew(HttpSession session, String new_login,
		int new_id, long new_group)
	{
//System.out.println("a2TeAL: svtGeneric~ Passed login="+new_login);			
//System.out.println("a2TeAL: svtGeneric~ Passed id="+new_id);			
//System.out.println("a2TeAL: svtGeneric~ Passed group="+new_group);			

		String user_login = 
			(String)session.getAttribute(cd.SESSION_USER_LOGIN);
//System.out.println("a2TeAL: svtGeneric~ Session login="+user_login);			
		Integer user_id = 
			(Integer)session.getAttribute(cd.SESSION_USER_ID);
//System.out.println("a2TeAL: svtGeneric~ Session id="+user_id);			
		Long group_id = 
			(Long)session.getAttribute(cd.SESSION_GROUP_ID);
//System.out.println("a2TeAL: svtGeneric~ Session group="+group_id);			
		ResourceMap res_map = 
			(ResourceMap)session.getAttribute(cd.SESSION_RES_MAP);
		
		if(	(user_login == null || user_id == null || res_map == null) ||
			(!user_login.equals(new_login))	||
			(user_id.intValue() != new_id) 	||
			(group_id.longValue() != new_group)	||
			(res_map.getUserID() != new_id)		||
			(res_map.getGroupID() != new_group)	||
			(!res_map.getUserLogin().equals(new_login)) )
			return true;
//System.out.println("a2TeAL: svtGeneric~ comparison returning "+false);	
		return false;
	}/**/
	
//	public boolean isUserAuthenticated(HttpSession session, boolean check_rm_n_gr)
//	{
//		if(session.getAttribute(cd.SESSION_INITED)==null) return false;
/*		String user_login = (String)session.getAttribute(cd.SESSION_USER_LOGIN);
		Integer user_id = (Integer)session.getAttribute(cd.SESSION_USER_ID);
		Long group_id = (Long)session.getAttribute(cd.SESSION_GROUP_ID);
		ResourceMap res_map = (ResourceMap)session.getAttribute(cd.SESSION_RES_MAP);
		
		//check for nulls
		// actually resmap can be null
		if(check_rm_n_gr)
			if(user_login == null || user_id == null || res_map == null || group_id == null)
				return false;
		else
			if((user_login==null) || (user_id==null))
				return false;
		/**/	
			
			
		//check for existing user login and id
/*		Connection conn = null;
		try
		{
			conn = SQLManager.getConntection();
			String qry = "SELECT UserID, Name FROM ent_user " + 
				"WHERE UserID="+ user_id.toString() +
				" AND Name=" + user_login;
			ResultSet rs = SQLManager.executeStatement(conn, qry);
			if(!rs.next())
				return false;
		}
		catch (Exception e) { e.printStackTrace(System.err); }
		finally {SQLManager.freeConnection(conn);}/**/
	
//		return true;
//	}
}

