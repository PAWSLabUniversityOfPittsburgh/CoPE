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

import java.util.*;
import java.sql.*;

//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
//import javax.sql.DataSource;

import edu.pitt.sis.paws.core.utils.SQLManager;


/**
 * ClientDaemon is a singleton encapsulating constants variables and methods
 * central to the client application in general.
 */
public class ClientDaemon
{
	private static ClientDaemon instance = new ClientDaemon();

	// CONSTANTS
	// 		CONTEXT PARAMETERS
	public static final String CONTEXT_HOMEURL = "/content/Show";
	public static final String CONTEXT_INDEXURL = "/index.jsp";
	public static final String CONTEXT_AUTHURL = "/content/doAuthenticate";
	public static final String CONTEXT_GROUPURL = "/content/doGroup";
	public static final String CONTEXT_ERRORURL = "/relogin.jsp";
	public static final String CONTEXT_ASSETS_PATH = "/assets";
	public static final String CONTEXT_UMS = "http://localhost:8080/cbum/um"; //"http://kt1.exp.sis.pitt.edu:8080/cbum/um";
	//		SESSION CONSTANTS
	public static final String SESSION_USER_ID = "user_id";
	public static final String SESSION_RES_MAP = "res_map";
	public static final String SESSION_GROUP_ID = "group_id";
	public static final String SESSION_CURRENT_NODE = "current_node";
	public static final String SESSION_INITED = "SESSION_INITED";

	// 		REQUEST PARAMETERS
	public static final String REQUEST_NODE_ID = "id";
	/** Flag requesting a check for the node to be visible (all its parents
	 * expanded). */
	public static final String REQUEST_EXPANDPATH = "exppath";
	public static final String REQUEST_FRAMES = "frames";
	public static final String REQUEST_USER_GROUP = "user_group";
	/** Parameter for the left "tree" frame to supply the id of the node being 
	 * recently expanded
	 */
	public static final String REQUEST_EXPAND = "expand";
	/** Parameter for the left "tree" frame to supply the id of the node being 
	 * recently collapsed
	 */
	public static final String REQUEST_COLLAPSE = "collapse";
	
//	private static ResourceMap res_map = new ResourceMap();
	
	/** Mask defining what frames to show - ltmb - left, top, main, bottom 
	 * Left:
	 * 	' ' - not loaded, [<] icon in top frame is NOT shown,
	 *     l  - minimized (hidden) but loaded, [<] icon in top frame is shown,
	 *     L  - shown, [<] icon in top frame is shown.
	 * Bottom:
	 * 	' ' - not loaded,
	 *     b  - minimized (only top bar of it is shown),
	 *     B  - maximized (some of the content is shown, default - 80px).
	 * Top:
	 * 	' ' - not shown - NEVER happens,
	 *     t  - minimized - NEVER happens,
	 *     T  - shown - default and only.
	 * Main:
	 * 	' ' - not shown - NEVER happens,
	 *     m  - minimized - the Bottom frame is maximized to the whole area,
	 *     M  - shown.
	 */
	public static final String SESSION_FRAMES = "frames";
	/** Token stored in session to specify the special modes of node
	 * handling e.g. editing mode. Used along with REQUEST_MODE.
	 * For mode specification refer to the Show class definition.
	 */
	public static final String SESSION_SHOW_MODE = "mode";
	/** Flag to determine whether left frame is shown or not. If value is not
	 * null then do not show.
	 */
	public static final String SESSION_HIDE_LEFT_FRAME = "lhide";

	// REQUEST PARAMETERS
	/** Parameter is used with Show servlet to specify the special conditions
	 * e.g. editing. For mode specification refer to the Show class definition
	 */
	public static final String REQUEST_SHOW_MODE = "mode";

//	private DataSource database_ds;	

	private SQLManager sqlManager;
	
	private ClientDaemon()
	{
		sqlManager = new SQLManager("java:comp/env/jdbc/portal");
	}

	public static ClientDaemon getInstance() { return instance; }
	
	
	public SQLManager getSQLManager() { return sqlManager; }
	
	public static boolean isSessionInited(HttpSession session)
	{
//System.out.println("AppDaemon.isSessionInited (session==null):" + (session==null) + ((session != null)?" "+session.getAttribute(SESSION_INITED)+ " isNew?=" + session.isNew() :""));	
		if((session == null) || (session.getAttribute(SESSION_INITED) == null))
			return false;
		else
			return true;
	}

	public static void setSessionInited(HttpSession session)
	{
		session.setAttribute(SESSION_INITED,"TRUE");
	}
	
	/** This method should be applied to the server resource map to create a
	 * stapshot for a specific user
	 * @param _user_id - user id
	 * @param _group_id - group id
	 */
	public ResourceMap createUserResourceMap(int _user_id, int _group_id)
	{
		Calendar start = null;
		Calendar finish = null;
		long diff_mills;
		
		start = new GregorianCalendar();
		
		// Load server resource map
		ResourceMap svr_map = new ResourceMap(0);
		Connection conn = null;
		try
		{
			conn = sqlManager.getConnection();
			
			svr_map.loadData(sqlManager);//, _user_id, _group_id);
//			svr_map.loadDataOld(conn);//, _user_id, _group_id);
		}
		catch (Exception e) { e.printStackTrace(System.out); 
//System.out.println("ClientDaemon.initIt exception");			
		}
		finally { SQLManager.recycleObjects(conn, null, null); }
		
		// Now copy stuff to user
		ResourceMap user_map =	new ResourceMap(svr_map.getTopVirtualNode(),
			svr_map.getBinNodeId());
		//create a user
		User old_user = svr_map.getUsers().findById(_user_id);
		User new_user = new User(_user_id, old_user.getTitle(), 
				old_user.getURI(), old_user.getLogin(), false);
//System.out.println("ClientDaemon.createUserResourceMap new_user # " + new_user.getId());
		//create a group
		User old_group = svr_map.getUsers().findById(_group_id);
		User new_group = new User(_group_id, old_group.getTitle(), old_user.getURI(), old_group.getLogin(), true);
//System.out.println("ClientDaemon.createUserResourceMap new_group # " + new_group.getId());

		ArrayList<Right> user_rights = svr_map.getUserRights(_user_id,
			_group_id);
		// Determine whether user can see node authors' names
		boolean can_see_authors = false;
		for(int i=0; i<user_rights.size(); i++)
		{
			if((user_rights.get(i).getRightType() == Right.RIGHT_TYPE_ALL)
				|| (user_rights.get(i).getRightType() == Right.RIGHT_TYPE_VIEW_AUTHOR) )
			{
				can_see_authors = true;
				break;
			}
		}
		
		user_map.getUsers().add(new_user);
		user_map.getUsers().add(new_group);
//System.out.println("ClientDaemon.createUserResourceMap # of U " + user_map.getUsers().size());
		if(can_see_authors) // can see others - load all users and groups
		{
			User clone = null;
			for(int i=0; i<svr_map.getUsers().size(); i++)
			{
				if(svr_map.getUsers().get(i).getId() != new_user.getId()
					&& svr_map.getUsers().get(i).getId() != new_user.getId() )
				{
					clone = (User)svr_map.getUsers().get(i).clone();
					user_map.getUsers().add(clone);
				}
			}
		}
			
//System.out.println("ClientDaemon.createUserResourceMap user_id: " + _user_id);			
//System.out.println("ClientDaemon.createUserResourceMap group_id: " + _group_id);			
//System.out.println("ClientDaemon.createUserResourceMap user_rights #: " + user_rights.size());			
		svr_map.copyViewableNodes(user_rights, user_map, new_user, new_group, can_see_authors);
//System.out.println("ClientDaemon.createUserResourceMap user_nodes #: " + user_map.getNodes().size());			
		svr_map.applyRights(user_rights, user_map);

		finish = new GregorianCalendar();
		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
		System.out.println("~~~ [CoPE] started in " + diff_mills + "ms ---- ");

		return user_map;
		
		
	}
	/** This function handles String parameters that can be passed via
	 * session or request. In general request parameter has priority.
	 */
	public static String competeParameters(String parSession, String parRequest,
		String parDefault)
	{
		if(parRequest != null) return parRequest;
		else if (parRequest != null) return parSession;
		else return parDefault;
	}
}
