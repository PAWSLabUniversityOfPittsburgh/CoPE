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
import java.util.regex.*;
import java.sql.*;

import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.*;
import edu.pitt.sis.paws.core.utils.SQLManager;

/**
 * ResourceMap class is the central storage of the structural data of the
 * client application (node tree, concepts, etc). ResourceMap is responsible
 * for managing these structures and communicating any changes to the portal.
 * ResourceMap should be stored as a session object and exist as a single
 * instance per user/portal connection.
 */

public class ResourceMap
{
	private ItemVector<iNode> nodes;
//	protected Item2Vector<iNode> root_nodes;
	protected Item2Vector<User> users;
	protected Vector<Right> rights;
	protected ItemVector<iConcept> concepts;

	/** All ratings in the current view of the portal
	 * @since 1.5
	 */
	protected Vector<Rating> ratings;

	//CoPE
	protected Item2Vector<Paper> papers;
	protected Item2Vector<iExternalResource> activities;
	protected Item2Vector<Summary> summaries;
	protected Item2Vector<Author> authors;
	

	/** Rights that apply to all users */
	protected Vector<Right> globally_accessible_rights;
	/** Rights that apply to all nodes */
	protected Vector<Right> globally_defined_rights;
	/** Rights that apply to users and nodes those users are authors of. */
	protected Vector<Right> authoritative_rights;
	/** This is a count for virtual nodes that do not exist in the database
	 * but represent some database records, the id's of such nodes are 
	 * negative. */
	protected int top_virtual_node;
	/** collction of the nodes that aren't stored in the DB, but wrap some
	 * objects from DB */
	private ItemVector<iNode> virtual_nodes;

	/** the id of the root node */
	protected final int root_node_id = 2;
	/** root node of the portal */
	protected iNode root_node;

	/** the id of the node that serves as a trash bin */
	protected int bin_node_id;
	/** the the node that serves as a trash bin */
	protected iNode bin_node;
	
	/** the id of the node that serves as a trash bin */
	protected int copesearch_node_id;
	/** the the node that serves as a trash bin */
	protected iNode copesearch_node;
	
	/** Node that is temporatily added in the "add new" mode before being sumbitted to the DB*/
	protected iNode pending_node;
	
	/** Constructor. Resets all the collections to an empty initial state. */
	public ResourceMap(int _top_virtual_node/*, int _bin_node_id*/)
	{
		nodes = new ItemVector<iNode>();
//		root_nodes = new Item2Vector<iNode>();
//		bin_node_id = _bin_node_id;
		bin_node = null;
		users = new Item2Vector<User>();
		rights = new Vector<Right>();
		concepts = new ItemVector<iConcept>();

		papers = new Item2Vector<Paper>();
		summaries = new Item2Vector<Summary>();
		activities = new Item2Vector<iExternalResource>();
		authors = new Item2Vector<Author>(); 

		globally_accessible_rights = new Vector<Right>();
		globally_defined_rights = new Vector<Right>();
		authoritative_rights = new Vector<Right>();

		top_virtual_node = _top_virtual_node;
		virtual_nodes = new ItemVector<iNode>();
		
		ratings = new Vector<Rating>();
		
		pending_node = null;
	}

	/** Constructor. Resets all the collections to an empty initial state. */
	public ResourceMap(int _top_virtual_node, int _bin_node_id)
	{
		nodes = new ItemVector<iNode>();
//		root_nodes = new Item2Vector<iNode>();
		bin_node_id = _bin_node_id;
		bin_node = null;
		users = new Item2Vector<User>();
		rights = new Vector<Right>();
		concepts = new ItemVector<iConcept>();

		papers = new Item2Vector<Paper>();
		summaries = new Item2Vector<Summary>();
		activities = new Item2Vector<iExternalResource>();
		authors = new Item2Vector<Author>(); 

		globally_accessible_rights = new Vector<Right>();
		globally_defined_rights = new Vector<Right>();
		authoritative_rights = new Vector<Right>();

		top_virtual_node = _top_virtual_node;
		virtual_nodes = new ItemVector<iNode>();
		
		ratings = new Vector<Rating>();

		pending_node = null;
	}
	
	public static ResourceMap createUserResourceMap(int _user_id, int _group_id, SQLManager sqlManager)
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
		System.out.println("~~~ [CoPE] logged user " + new_user.getLogin() + "  into group " + new_group.getLogin());
		System.out.println("~~~ [CoPE] started in " + diff_mills + "ms ---- ");

		return user_map;
		
		
	}// end of -- createUserResourceMap
	
	
	/** Resets a <em>root nodes</em> collection. Erases the previous content
	 * and selects those nodes fron <em>nodes</em> collection that have no
	 * parent (<em>parent</em> set to <em>null</em>). */
	private void resetRootNode()
	{
		root_node = nodes.findById(root_node_id);
//		iNode my_profile = nodes.findById(-1);
//		if(my_profile != null)
//		{
//			my_profile.setParent(root_node);
//			root_node.getChildren().add(0, my_profile);
//		}
	}
	
	/** This method is called by a client copy of <em>ResourceMap</em>.
	 * There is a chance that a scope of visibility for a user doesn't
	 * contain root nodes. Then the paths to the <em>root nodes</em> are
	 * <quote>restored</quote>.
	 * @param _resmap - a server copy of <em>ResourceMap</em> */
	private void restorePathsToOriginalRootNodes(ResourceMap _resmap, User _user)
	{
//System.out.println("ResourceMap.restorePathsToOriginalRootNodes start...");
//System.out.println("ResourceMap.restorePathsToOriginalRootNodes before scanning " + _resmap.getNodes().size() + " other nodes");
		for(int i=0; i<_resmap.getNodes().size(); i++)
		{// for all nodes in other ResourceMap
			iNode other_node = _resmap.getNodes().get(i);
//System.out.println("ResourceMap.restorePathsToOriginalRootNodes scanning other node # " + i + "(" + other_node.getId() + "," + other_node.getTitle() + ")");
			if(other_node.getNodeType()==iNode.NODE_TYPE_I_MYPROFILE)
				continue;
//			if( (other_node.getParent() != null) && (other_node.getParent().getNodeType()==iNode.NODE_TYPE_I_ALL))
//				continue;
			iNode this_equiv = nodes.findById(other_node.getId());
//if(this_equiv==null)
//{
//	System.out.println("=================================");
//	System.out.println("=================================");
//	System.out.println("=================================");
//	System.out.println("=================================");
//	System.out.println("=================================");
//	System.out.println("=================================");
//	System.out.println("=================================");
//	_resmap.getNodes().setSorted(Item2Vector.SORTING_ID);
//	for(int ii=0; ii<_resmap.getNodes().size(); ii++)
//	{
//		System.out.println("Node " + ii + " #" + _resmap.getNodes().get(ii).getId() + " " + _resmap.getNodes().get(ii).getTitle());
//	}
//}
//System.out.println("ResourceMap.restorePathsToOriginalRootNodes scanning other node id # " + other_node.getId() + " this_equiv="+this_equiv);
			while(this_equiv.getParent() != null)
			{
//System.out.println("ResourceMap.restorePathsToOriginalRootNodes equivalent node id " + this_equiv.getId() + " cycle# "  + i);
				if( _resmap.getNodes().findById(
						this_equiv.getParent().getId()) == null )
				{
					iNode new_parent = this_equiv.getParent().clone(_user, false /*set external object*/);
					_resmap.getNodes().add(new_parent);
//System.out.println("ResourceMap.restorePathsToOriginalRootNodes adding other node id " + new_parent.getId());
//	NO LINKS HERE				new_parent.getChildren().add(other_node);
//	NO LINKS HERE				other_node.setParent(new_parent);
				}
				this_equiv = this_equiv.getParent();
			}
		}// -- end for all nodes in other ResourceMap
//System.out.println("ResourceMap.restorePathsToOriginalRootNodes end.");
	}// -- end restorePathsToOriginalRootNodes

	public void loadData(SQLManager sqlm) throws Exception
	{
		Calendar start = null;
		Calendar finish = null;
		long diff_mills;
		
		start = new GregorianCalendar();
		
		UserLoadingThread ult = new UserLoadingThread(sqlm, this);
		UserLinkLoadingThread ullt = new UserLinkLoadingThread(sqlm, this);
		AuthorLoadingThread alt = new AuthorLoadingThread(sqlm, this);
		PaperLoadingThread plt = new PaperLoadingThread(sqlm, this);
		PaperAuthorLinkLoadingThread palt = new PaperAuthorLinkLoadingThread(sqlm, this);
		SummaryLoadingThread slt = new SummaryLoadingThread(sqlm, this);
		ActivityLoadingThread aclt = new ActivityLoadingThread(sqlm, this);
		NodeLoadingThread nlt = new NodeLoadingThread(sqlm, this);
		NodeLinkLoadingThread nllt = new NodeLinkLoadingThread(sqlm, this);
		ConceptLoadingThread clt = new ConceptLoadingThread(sqlm, this);
		ConceptLinkLoadingThread cllt = new ConceptLinkLoadingThread(sqlm, this);
		RightLoadingThread rlt = new RightLoadingThread(sqlm, this);
		UserConceptLinkLoadingThread ucllt = new UserConceptLinkLoadingThread(sqlm, this);
		NodeConceptLinkLoadingThread ncllt = new NodeConceptLinkLoadingThread(sqlm, this);
		RatingLoadingThread ralt = new RatingLoadingThread(sqlm, this);
		
		// set prerequisites
		ullt.getPrerequisites().add(ult);
		
		plt.getPrerequisites().add(ult);
		
		palt.getPrerequisites().add(plt);
		palt.getPrerequisites().add(alt);
		
		slt.getPrerequisites().add(plt);
//		slt.getPrerequisites().add(ult);
		
		aclt.getPrerequisites().add(ult);
		
//		nlt.getPrerequisites().add(ult);
		nlt.getPrerequisites().add(aclt);
//		nlt.getPrerequisites().add(plt);
		nlt.getPrerequisites().add(slt);
		
		nllt.getPrerequisites().add(nlt);
		
//		rlt.getPrerequisites().add(ult);
		rlt.getPrerequisites().add(nlt);
		
		cllt.getPrerequisites().add(clt);
		
		ucllt.getPrerequisites().add(ult);
		ucllt.getPrerequisites().add(clt);
		
//		ralt.getPrerequisites().add(ult);
		ralt.getPrerequisites().add(nlt);
		
//		ncllt.getPrerequisites().add(ult);
		ncllt.getPrerequisites().add(clt);
//		ncllt.getPrerequisites().add(nlt);
		ncllt.getPrerequisites().add(nllt);
		ncllt.getPrerequisites().add(rlt);
		ncllt.getPrerequisites().add(ralt);
		
		// start threads
		ult.start();
		ullt.start();
		alt.start();
		plt.start(); 
		palt.start();
		slt.start();
		aclt.start();
		nlt.start();
		nllt.start();
		clt.start();
		cllt.start();
		rlt.start();
		ucllt.start();
		ncllt.start();
		ralt.start();
		
		// wait for threads
//		ult.join();
		ullt.join();
//		alt.join();
//		plt.join(); 
		palt.join();
//		slt.join();
//		aclt.join();
//		nlt.join();
//		nllt.join();
//		clt.join();
		cllt.join();
//		rlt.join();
		ucllt.join();
		ncllt.join();
//		ralt.join();
		
		System.out.println("~~~ [CoPE] ResourceMap.loadMap virtual nodes#" + top_virtual_node);
		System.out.println("~~~ [CoPE] ResourceMap.loadMap nodes#" + nodes.size());
				
		finish = new GregorianCalendar();
		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
		System.out.println("~~~ [CoPE] ResourceMap loaded in " + diff_mills + "ms ---- ");

	}
	
	
	/** Method loads content from a database */
	public void loadDataOld(Connection conn) throws Exception
	{
		Calendar start = null;
		Calendar finish = null;
		long diff_mills;
		
		start = new GregorianCalendar();

		String qry = "";
		ResultSet rs = null;
		int count = 0;

		// Load Users
		qry = "SELECT * FROM ent_user WHERE (Name<>'MACRO');";
//		+	"OR (Name IS NULL);";
		PreparedStatement statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		while(rs.next())
		{
			int user_id = rs.getInt("UserID");
			String user_name = rs.getString("Name");
			String user_login = rs.getString("Login");
			String user_uri = rs.getString("URI");
			boolean is_group = (rs.getInt("isGroup")!=0)?true:false;

			users.add( new User(user_id,user_name,user_uri,user_login, is_group));
//			users.add( (is_group) ? (new User(user_id,user_name,user_login)) :
//				(new User(user_id,user_name,user_login)) );;
			count++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
		
System.out.println("~~~ [CoPE] ResourceMap.loadMap users#" + count); /// DEBUG

		// Load Group-User Links
		qry = "SELECT * FROM rel_user_user;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			int parent_user_id = rs.getInt("ParentUserID");
			int child_user_id = rs.getInt("ChildUserID");
			User parent_user = users.findById(parent_user_id);
			User child_user = users.findById(child_user_id);
			parent_user.getSubordinates().add(child_user);
			child_user.getSuperordinates().add(parent_user);
			count++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap group-user links#" + count); /// DEBUG

		// Load all the external objects (secondary to Nodes in general)

		// Authors of Papers
		qry = "SELECT * FROM ent_cope_author;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// node data
			int author_id = rs.getInt("AuthorID");
			String last_name = rs.getString("LastName");
			String given_names = rs.getString("GivenNames");
			
			String author_uri = "";//"author" + author_id;
			
			authors.add(new Author(author_id, last_name, author_uri, given_names));
			count++;
		}// end -- load Papers
		rs.close();
		rs = null;
		statement .close();
		statement = null;
		System.out.println("~~~ [CoPE] ResourceMap.loadMap authors#" + count); /// DEBUG

		//	Papers
		qry = "SELECT p.PaperID, p.Title AS pTitle, p.Year, p.URL AS pURL," +
			" p.BiblioInfo, p.Authors, n.* " + 
			"FROM ent_cope_paper p LEFT OUTER JOIN ent_node n" + 
			" ON (p.PaperID = n.ExtID) WHERE n.ItemTypeID=6 " +
			"GROUP BY p.PaperID, p.Title, p.Year, p.URL, p.BiblioInfo, " +
			"p.Authors;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// node data
//			int node_id = rs.getInt("NodeID");
			int user_id = rs.getInt("UserID");
			String node_url = rs.getString("URL");
//			String node_descr = "";
//			boolean folder_flag = false;
			User user = users.findById(user_id);
			// paper data
			int paper_id = rs.getInt("PaperID");
			String paper_title = rs.getString("pTitle");
//if(paper_title.equals("MVY1")) System.out.println("MVY1 " + user.getTitle());
	
			int paper_year = rs.getInt("Year");
			String paper_biblioinfo = rs.getString("BiblioInfo");
			
			String paper_uri = "";//"paper" + paper_id;

			String paper_authors = rs.getString("Authors");

			papers.addNew( new Paper(paper_id, paper_title, paper_uri, paper_year,
				paper_biblioinfo, paper_authors, node_url, user) );
			count++;
		}// end -- load Papers
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap papers#" + count); /// DEBUG

		// Author - Paper links
		qry = "SELECT * FROM rel_cope_paper_author ORDER BY PaperID, Idx;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// node data
			int author_id = rs.getInt("AuthorID");
			int paper_id = rs.getInt("PaperID");
			
			Author author = authors.findById(author_id);
			Paper paper = papers.findById(paper_id);
			
			if(author == null || paper == null)
			{
				System.out.print("ResourceMap.loadMap error in Author-Paper link ");
				if(paper == null)System.out.print("paper id " + paper_id + " not found ");
				if(author == null)System.out.print("author id " + author_id + " not found ");
				System.out.println("");
			}
			else
			{
				paper.getAuthorss().add(author);
				author.getPapers().add(paper);
				count++;
			}
		}// end -- load Papers
		rs.close();
		rs = null;
		statement .close();
		statement = null;
		System.out.println("~~~ [CoPE] ResourceMap.loadMap author-paper links#" + count); /// DEBUG

		//	Summaries
		qry = "SELECT * FROM ent_cope_summary;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			int summary_id = rs.getInt("SummaryID");
			int paper_id = rs.getInt("PaperID");
			String summary_text = rs.getString("SummaryText");
			int user_id = rs.getInt("UserID");
			User user = users.findById(user_id);
//System.out.println("\t paper iddd = " + paper_id); //DEBUG
			Paper paper = papers.findById(paper_id);
			
			String summary_uri = "";//"summary" + summary_id; 
			
//if(paper == null)
//{
//	for(int i=0; i<papers.size(); i++)
//		System.out.println("\t\t" + papers.get(i));
//}
			Summary summary = new Summary(summary_id, user, summary_uri, summary_text, paper);
			this.summaries.add(summary);
			paper.getSummaries().addNew(summary);
			count++;
		}// end -- load Papers
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap summaries#" + count); /// DEBUG

		 //    Activities
		String activity_types = "";
		for(int i=0; i<iNode.NODE_TYPES_I_ACTIVITIES.length; i++)
		      activity_types+= ((activity_types.length()>0)?",":"") + 
				iNode.NODE_TYPES_I_ACTIVITIES[i];
		qry = "SELECT a.ActivityID, a.ExtID AS aExtID, a.Title AS aTitle, a.URI " + 
			"AS aProperties, a.URL AS aURL, n.* " + 
			"FROM ent_activity a LEFT OUTER JOIN ent_node n" + 
			" ON (a.ActivityID = n.ExtID) WHERE n.ItemTypeID IN (" +
			activity_types + ")" +
			"GROUP BY a.ActivityID, a.Title, a.URI, a.URL;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		// portal parameters for activity URLs
//		User user_ = users.findById(_user_id);
//		User group_ = users.findById(_group_id);
//		String sess_ = session_id.substring(session_id.length()-5);
//System.out.println("ResourceMap.loadMap additional parameters user_id=" + _user_id + 
//	" (user_==null)=" + (user_==null) + ((user_==null)?"":" user.login=" + user_.getLogin()) +
//	" group_id=" + _group_id + " (group_==null)=" + (group_==null) + ((group_==null)?"":" group.login=" + group_.getLogin()) +
//	" session.id=" + sess_ ); /// DEBUG
		while(rs.next())
		{
			// node data
//			int node_id = rs.getInt("NodeID");
			int user_id = rs.getInt("UserID");
//			String node_url = rs.getString("URL");
//			String node_descr = "";
			int node_item_type = rs.getInt("ItemTypeID");
//			boolean folder_flag = false;
			User user = users.findById(user_id);
			// Activity data
			int activity_id = rs.getInt("ActivityID");
			String activity_ext_id = rs.getString("aExtID");
			String activity_title = rs.getString("aTitle");
//			String activity_props = rs.getString("aExtID");
			String activity_url = rs.getString("aURL");
			
			String activity_uri = "";//"activity" + activity_id;

//			activity_url += ((activity_url.indexOf("?")!=-1)?"&":"?") + "usr=" + user_.getLogin() + "&grp=" +
//				group_.getLogin() + "&sid=" + sess_;
//System.out.println(activity_url);

//			String icon_small = "";
//			String icon_large = "";
//			switch (node_item_type) 
//			{
//				case iNode.NODE_TYPE_I_QUIZ:
//				case iNode.NODE_TYPE_I_SYS_QUIZGUIDE:
//				{
//					icon_small = "quiz_small.gif";
//					icon_large = "quiz_large.gif";
//				}
//				break;
//				case iNode.NODE_TYPE_I_SYS_NAVEX:
//				case iNode.NODE_TYPE_I_DISSECTION:
//				{
//					icon_small = "dissection_small.gif";
//					icon_large = "dissection_large.gif";
//				}
//				case iNode.NODE_TYPE_I_WADEIN:
//				case iNode.NODE_TYPE_I_SYS_WADEIN:
//				{
//					icon_small = "wadein_small.gif";
//					icon_large = "wadein_large.gif";
//				}
//				break;
//				case iNode.NODE_TYPE_I_CODEEXAMPLE:
//				{
//					icon_small = "code_example_small.gif";
//					icon_large = "code_example_large.gif";
//				}
//				break;
//				case iNode.NODE_TYPE_I_KARELROBOT:
//				{
//					icon_small = "karel_small.gif";
//					icon_large = "karel_large.gif";
//				}
//				break;
//			}

			
			switch (node_item_type) 
			{
				case iNode.NODE_TYPE_I_QUIZ:
				case iNode.NODE_TYPE_I_DISSECTION:
				case iNode.NODE_TYPE_I_WADEIN:
				case iNode.NODE_TYPE_I_CODEEXAMPLE:
				case iNode.NODE_TYPE_I_LINK:
				{
					activities.addNew( new ExternalResourceVisual(activity_id, activity_title, activity_uri,
							user, activity_url, activity_ext_id) );
				}
				break;
				case iNode.NODE_TYPE_I_KARELROBOT:
				{
				      activities.addNew( new ExternalResourceDownloadable(activity_id, activity_title, activity_uri,
				    		  user, activity_url, activity_ext_id) );
				}
				break;
				case iNode.NODE_TYPE_I_SYS_QUIZGUIDE:
				case iNode.NODE_TYPE_I_SYS_NAVEX:
				case iNode.NODE_TYPE_I_SYS_WADEIN:
				case iNode.NODE_TYPE_I_LINK_POPUP:
				{
//System.out.println(" loading activity id = "  + activity_id);
//System.out.println("\tactivity user " + user);
//System.out.println("\tactivity user id " + user.getId());

					activities.addNew( new ExternalResourcePopup(activity_id, activity_title, activity_uri,
							user, activity_url, activity_ext_id) );
				}
				break;
			}
					
			count++;
		}// end -- load Activities
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap activities#" + count); /// DEBUG
		
		// Load Nodes
		qry = "SELECT * FROM ent_node ;"; //WHERE (Description<>'MACRO') OR (Description IS NULL)
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			int node_id = rs.getInt("NodeID");
			String node_title = rs.getString("Title");
			String node_descr = rs.getString("Description");
			int user_id = rs.getInt("UserID");
			int node_type = rs.getInt("ItemTypeID");
			String node_url = rs.getString("URL");
			String node_params = rs.getString("URI");
			String external_id = rs.getString("ExtID");
			int folder_flag_i = rs.getInt("FolderFlag");

			User user = users.findById(user_id);
			node_descr = (node_descr==null) ? "" : node_descr ;
			node_url = (node_url==null) ? "" : node_url ;
			node_params = (node_params==null) ? "" : node_params ;
			boolean folder_flag = (folder_flag_i == 1)?true:false;

			// decode the type of the node

			switch (node_type)
			{
				case iNode.NODE_TYPE_I_ALL: // Root Node
				{
					iNode node = new NodeUntyped(node_id, node_title,
							node_type, node_descr, node_url, folder_flag,
							node_params, user, null);
					this.root_node = node;
					globally_defined_rights = root_node.getRights();
//System.out.println("Root node assigned " + node);
					nodes.add( node );
				}
				break;
				case iNode.NODE_TYPE_I_FOLDER: // Folder or
				case iNode.NODE_TYPE_I_UNTYPDOC: // Untyped document
				case iNode.NODE_TYPE_I_TOPIC_FOLDER: // Topic Folder
				case iNode.NODE_TYPE_I_BIN: // Recycled bin
				{
					iNode node = new NodeUntyped(node_id, node_title,
						node_type, node_descr, node_url, folder_flag,
						node_params, user, null);
					nodes.add( node );
					if(node.getNodeType() == iNode.NODE_TYPE_I_BIN)
					{
						bin_node_id = node.getId();
						bin_node = node;
//System.out.println("--- ResourceMap.loadData bin node found, getBinNode==null: " (this.getBinNode()==null));
					}
				}
				break;
				case iNode.NODE_TYPE_I_PAPER: // Paper
				{
					int paper_id = 0;
					try
					{
						paper_id = Integer.parseInt(external_id);
						iResource paper = papers.findById(paper_id);
						nodes.add( new NodeUntyped(node_id, node_title,
							node_type, node_descr, node_url, folder_flag,
							node_params, user,  paper) );
					}
					catch(Exception e)
					{
						System.out.println("ResourceMap.loadMap Couldn't get paper id for params="+node_params);
					}
				}
				break;
				case iNode.NODE_TYPE_I_SUMMARY: // Summary
				{
					int summary_id = 0;
					try
					{
						summary_id = Integer.parseInt(external_id);
						iResource summary = summaries.findById(summary_id);
						nodes.add( new NodeUntyped(node_id, node_title,
							node_type, node_descr, node_url, folder_flag,
							node_params, user,  summary) );
					}
					catch(Exception e)
					{
						System.out.println("ResourceMap.loadMap Couldn't get summary id for params="+node_params);
					}
				}
				break;
				case iNode.NODE_TYPE_I_QUIZ: // Quiz
				case iNode.NODE_TYPE_I_DISSECTION: // Dissection
				case iNode.NODE_TYPE_I_WADEIN: // Wadein
				case iNode.NODE_TYPE_I_CODEEXAMPLE: // Code Example
				case iNode.NODE_TYPE_I_KARELROBOT: // Karel Robot file
				case iNode.NODE_TYPE_I_SYS_QUIZGUIDE: //System QuizGUIDE
				case iNode.NODE_TYPE_I_SYS_NAVEX: // System NavEx
				case iNode.NODE_TYPE_I_SYS_WADEIN: // System WADEIn II
				case iNode.NODE_TYPE_I_LINK: // Link
				case iNode.NODE_TYPE_I_LINK_POPUP: // System WADEIn II
			      {
			            int activity_id = 0;
			            try
			            {
			                  activity_id = Integer.parseInt(external_id);
			                  iResource activity = activities.findById(activity_id);
//System.out.println("activity_id="+activity_id+"activity:"+activity);
			                  nodes.add( new NodeUntyped(node_id, node_title,
			                        node_type, node_descr, node_url, folder_flag,
			                        node_params, user,  activity) );
//if(node_type == iNode.NODE_TYPE_I_SYS_QUIZGUIDE)
//	System.out.println("Attaching act tot node: activity_id=" + activity_id + " node id = " + node_id + 
//			" (activity==null) " + (activity==null) + " (activity.getCreator()==null) " + (activity.getCreator()==null));
			            }
			            catch(Exception e)
			            {
			                  System.out.println("ResourceMap.loadMap Couldn't get activity id for params="+node_params);
			                  e.printStackTrace(System.out);
			            }
			      }
			      case 0:
			      {

			      }
			      break;
			}
			count++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap nodes#" + count);  /// DEBUG

		// Load properties
		//	Load comments
/*		qry = "Select * FROM ent_comment;";
		rs = ad.executeStatement(conn, qry);
		while(rs.next())
		{
//System.out.println("ResourceMap.loadMap there exist Node Comments");
			int comment_id = rs.getInt("CommentID");
			int node_id = rs.getInt("NodeID");
			int user_id = rs.getInt("UserID");
			String comment_text = rs.getString("CommentText");

			User user = users.findById(user_id);

			if( user == null )
			{
				System.out.println("ResourceMap.loadMap getting Comments user # " + user_id + " was not found");
				continue;
			}

			iNode node = nodes.findById(node_id);
			if( node == null )
			{
				System.out.println("ResourceMap.loadMap getting Comments node # " + node_id + " was not found");
				continue;
			}

			if( (node != null) && (user != null) )
			{
				node.getComment().getValues().add( new NodeCommentValue(
					comment_id, comment_text, user, node.getComment()) );
			}
		}/**/

		// Load Node-Node Links
		qry = "SELECT * FROM rel_node_node ORDER BY OrderRank;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			int parent_node_id = rs.getInt("ParentNodeID");
			int child_node_id = rs.getInt("ChildNodeID");
			double weight = rs.getInt("Weight");
if(weight != 1.0) System.out.println("ResourceMap.loadData parent_id=" + parent_node_id + " child_id=" + child_node_id + " weight="+weight);
			iNode parent_node = nodes.findById(parent_node_id);
			iNode child_node = nodes.findById(child_node_id);
			parent_node.getChildren().add(child_node, weight);
			child_node.setParent(parent_node);
			count++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap node-node links#" + count); /// DEBUG

		// [Re]calculate Root nodes
		resetRootNode();

		// Load rights
		qry = "SELECT * FROM ent_right;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// read data
			int user_id = rs.getInt("UserID");
			int right_type = rs.getInt("RightTypeID");
			int parent_type = rs.getInt("ParentTypeID");
			int child_type = rs.getInt("ChildTypeID");
			int node_id = rs.getInt("NodeID");
			int quantity = rs.getInt("Quantity");
			String desc = rs.getString("Description");
			int ownerFlag = rs.getInt("OwnerFlag");
			// process fata
			int user_macro = ( (user_id != Right.USER_ALL) &&
				(user_id != Right.USER_AUTHOR) ) ?
				((ownerFlag==1)? Right.USER_AUTHOR : 0) : user_id;

//			int node_macro = (node_id != Right.NODE_ALL) ? 0 : node_id;
			User user = (user_macro == Right.USER_ALL ) ? null : users.findById(user_id);
			iNode node = /*(node_macro != 0 ) ? null :*/ nodes.findById(node_id);

			Right right = new Right(user, user_macro, right_type, node,
				/*node_macro*/node_id, parent_type, child_type, quantity,
				desc, (ownerFlag==1));
			rights.add(right);

			if(user_macro == Right.USER_ALL)
				globally_accessible_rights.add(right);
			if(user_macro == Right.USER_AUTHOR)
				authoritative_rights.add(right);
//			if(node_macro == Right.NODE_ALL)
//				globally_defined_rights.add(right);

			// Add rights to target elements - user and node
			if(node != null) node.getRights().add(right);
			else
			{
				for(int i=0; i>nodes.size(); i++)
					nodes.get(i).getRights().add(right);
			}

			if(user != null) user.getRights().add(right);
			else
			{
				for(int i=0; i>users.size(); i++)
					users.get(i).getRights().add(right);
			}
			count++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap rights#" + count); /// DEBUG

		// Load Concepts
		qry = "SELECT * FROM ent_concept ;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// read data
			int concept_id = rs.getInt("ConceptID");
			String title = rs.getString("Name");

			concepts.add( new Concept(concept_id, title) );
			count++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap concepts#" + count); /// DEBUG

		// Load Concept-Concept Links
		qry = "SELECT * FROM rel_concept_concept;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// read data
			int parent_id = rs.getInt("ParentConceptID");
			int child_id = rs.getInt("ChildConceptID");

			Concept parent = (Concept)concepts.findById(parent_id);
			Concept child = (Concept)concepts.findById(child_id);

			parent.getChildren().add( child );
			child.setParent(parent);

			count++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap concept-concept links#" + count);  /// DEBUG
		
		// Load User-Concept Links
		qry = "SELECT * FROM rel_user_concept;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// read data
			int user_id = rs.getInt("UserID");
			int concept_id = rs.getInt("ConceptID");
			User user = users.findById(user_id);
			iConcept concept = concepts.findById(concept_id);
			user.getConcepts().add(concept);
			count++;
		}		
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap user-concept links#" + count);  /// DEBUG
		
		// Load Node-Concept Links
		qry = "SELECT * FROM rel_node_concept;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// read data
			int node_id = rs.getInt("NodeID");
			int concept_id = rs.getInt("ConceptID");
			int type = rs.getInt("RelationType");
			int user_id = rs.getInt("UserID");
			iNode node = nodes.findById(node_id);
			iConcept concept = concepts.findById(concept_id);
			User user = users.findById(user_id);
			switch (type)
			{
				case iConcept.CONCEPT_NODE_REL_ONTOLOGY:
				{
					node.getOntologies().add(concept);
//System.out.println("ResourceMap.loadMap ontology loaded"); // DEBUG
				}
				break;
				case iConcept.CONCEPT_NODE_REL_CONCEPT:
				{
					node.getConcepts().addNewValue(concept, user);
//System.out.println("ResourceMap.loadMap concept indexing node=" + node_id + " concept=" + concept_id + " type=" + type + " user=" + user_id);;
					// Work with NodeConcepts external object
					iNode indexing_node = node.findChildOfTypeByUser(
						iNode.NODE_TYPE_I_CONCEPTS, user_id);
					NodeConcepts nc = null;
					
					String node_concepts_uri = "";//"NodeConcepts" + node.getId();
					
					if(indexing_node == null)
					{
						// create NodeConcepts external object
						nc = new NodeConcepts( node.getId(),
							"Concepts", node_concepts_uri, user);
						indexing_node = new NodeUntyped(						
							(-(++top_virtual_node)), "Concepts",
							iNode.NODE_TYPE_I_CONCEPTS, "",
							"", false, "", user, nc);
						node.getChildren().add(indexing_node);
						nodes.add(indexing_node);
						indexing_node.setParent(node);
						virtual_nodes.add(indexing_node);
//System.out.println("ResourceMap.loadMap indexing_node (indexing_node.getExternalObject()==null) = " + (indexing_node.getExternalObject()==null)); /// DEBYG
					}
					else nc = (NodeConcepts)indexing_node.getExternalObject();
					nc.getConcepts().addNew(concept);
				}
				break;
			}
			count ++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap concept-node links#" + count);

		// Load Ratings
		qry = "SELECT * FROM ent_node_rating;";
		statement = conn.prepareStatement(qry);
		rs = statement.executeQuery();
		count = 0;
		while(rs.next())
		{
			// read data
			int node_id = rs.getInt("NodeID");
			int user_id = rs.getInt("UserID");
			int rating_val = rs.getInt("Rating");
			int i_anonymous = rs.getInt("Anonymous");
			String comment = rs.getString("Comment");

			iNode node = nodes.findById(node_id);
			User user = users.findById(user_id);
			boolean anonymous = (i_anonymous==0)?false:true;
			
			Rating rating = new Rating((float)rating_val, anonymous, 
				comment, user, node);
			
			// add to rating collection
			ratings.add(rating);
			// add to the user
			user.getRatings().add(rating);
			// add to the node
			node.setPersonalRating(rating);
			// add to all user groups
			for(int i=0; i<user.getSuperordinates().size(); i++)
			{
				User group = user.getSuperordinates().get(i);
				group.getRatings().add(rating);
			}
			count ++;
		}
		rs.close();
		rs = null;
		statement .close();
		statement = null;
System.out.println("~~~ [CoPE] ResourceMap.loadMap ratings#" + count);


System.out.println("~~~ [CoPE] ResourceMap.loadMap virtual nodes#" + top_virtual_node);
System.out.println("~~~ [CoPE] ResourceMap.loadMap nodes#" + nodes.size());
		
		finish = new GregorianCalendar();
		diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
		System.out.println("~~~ [CoPE] ResourceMap loaded in " + diff_mills + "ms ---- ");

	}// - end of - loadMap

	/** Getter for <i>nodes</i> collection.
	 */
	public ItemVector<iNode> getNodes() { return nodes; }
//	/** Getter for <i>root_nodes</i> collection.
//	 */
//	public Item2Vector<iNode> getRootNodes() { return root_nodes; }
	/** Getter for <i>users</i> collection.
	 */
	public Item2Vector<User> getUsers() { return users; }
	/** Getter for <i>rights</i> collection.
	 */
	public Vector<Right> getRights() { return rights; }
	/** Getter for <i>globally_defined_rights</i> collection.
	 */
	public Vector<Right> getGloballyDefinedRights()
		{ return globally_defined_rights; }
	/** Getter for <i>globally_accessible_rights</i> collection.
	 */
	public Vector<Right> getGloballyAccessibleRights()
		{ return globally_accessible_rights; }
	public Item2Vector<Paper> getPapers() { return papers; }
	public Item2Vector<Summary> getSummaries() { return summaries; }
	
	public ItemVector<iNode> getVirtualNodes() { return virtual_nodes; }

	/** This function returns a list of rights that are applicable to a user
	 * (identified by a user id) as a member of a group (indentified by a
	 * group id). Rights applicable to ALL users are included.
	 * @param user_id - id of the user
	 * @param group_id - id of the group
	 */
	public ArrayList<Right> getUserRights(int user_id, int group_id)
	{
		ArrayList<Right> selected_rights = new ArrayList<Right>();
		for(int i=0; i<rights.size(); i++)
		{
			if(rights.get(i).getUser() == null)
			{
//if((rights.get(i).getOwnerFlag()))
//	System.out.println("inspecting right owner user_id=" + user_id);
				// add rights applicable to all
				if(rights.get(i).getUserMacro() == Right.USER_ALL)
					selected_rights.add(rights.get(i));
				// add authoritative rights
//				if( (rights.get(i).getUserMacro() == Right.USER_AUTHOR)
//					&&
//					( (rights.get(i).) )||
//						()
//					)
//				)
//					selected_rights.add(rights.get(i));
			}
			else if( (rights.get(i).getUser().getId() == user_id) ||
				(rights.get(i).getUser().getId() == group_id) )
			{
				// add targeted rights
				selected_rights.add(rights.get(i));
//if((rights.get(i).getOwnerFlag()))
//	System.out.println("copying right owner user_id=" + user_id);
			}
		}
		return selected_rights;
	}// end -- getUserRights

	/** The function loads nodes from calling server <i>ResourceMap</i> to a
	 * client <i>ResourceMap</i>. Loaded nodes would comprise a client set of
	 * viewable nodes and are regulated by a specified rights set.
	 * @param _resmap - client <i>ResourceMap</i> nodes should be loaded into.
	 * @param _rights - rights that would identify a viewable nodes set
	 */
	public void copyViewableNodes(ArrayList<Right> _rights,
		ResourceMap _resmap, User new_user, User new_group,
		boolean can_see_authors)
	{
		// all the concepts
		ItemVector<iConcept> copy_concepts = new ItemVector<iConcept>();

//System.out.println("ResourceMap.copyViewableNodes _rights.# " + _rights.size());
		// look for a right applicable to all nodes
		boolean hasTotalRights = false;
		for(int i=0; i<_rights.size(); i++)
		{
//System.out.println("ResourceMap.copyViewableNodes right# " + i + " Node " + _rights.get(i).getNode() + " Node.macro " + _rights.get(i).getNodeMacro());

			if( (_rights.get(i).getNode() == null) &&
				_rights.get(i).getNodeMacro() == Right.NODE_ALL )
			{
				hasTotalRights = true;
				break;
			}
		}
//System.out.println("ResourceMap.copyViewableNodes hasTotalRights " + hasTotalRights);

//System.out.println("ResourceMap.copyViewableNodes before copying # concepts copied="+copy_concepts.size());

		if(hasTotalRights) // copy all the nodes
		{
			copy_concepts = root_node.copyToResourceMap(
				_resmap, new_user, copy_concepts, can_see_authors);
		}
//		if(hasTotalRights) // copy all the nodes
//		{
//			for(int i=0; i<root_nodes.size(); i++)
//			{
//				copy_concepts = root_nodes.get(i).copyToResourceMap(
//					_resmap, new_user, copy_concepts, can_see_authors);
//			}
//		}
		else // copy only nodes that are in rights
		{
			for(int i=0; i<_rights.size(); i++)
			{
				if(_rights.get(i).getNode() != null)
					copy_concepts = _rights.get(i).getNode().
						copyToResourceMap(_resmap, new_user,
							copy_concepts, can_see_authors);
			}
		}
		// array of all concepts is compiled - ontologies are in place
		_resmap.setConcepts(copy_concepts);
System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes total # concepts copied="+copy_concepts.size());
		// Restore paths to 'original' root nodes
		restorePathsToOriginalRootNodes(_resmap, new_user);
		// Restore links between current user & concepts (not ontologies)
		User old_user = this.users.findById(new_user.getId());
//System.out.println("ResourceMap.copyViewableNodes (old_user==null):" + (old_user==null));
//System.out.println("ResourceMap.copyViewableNodes (old_user==null):" + (old_user==null));
		for(int i=0; i<old_user.getConcepts().size(); i++)
		{
			int old_concept_id = old_user.getConcepts().get(i).getId();
			iConcept new_concept = _resmap.getConcepts().
				findById(old_concept_id);
			if(new_concept != null)
				new_user.getConcepts().add(new_concept);
		}
		// restore links between nodes & conceps (not ontologies)
		iNode old_node = null;
		iConcept new_concept = null;
		for(int i=0; i<_resmap.getNodes().size(); i++)
		{
			old_node = this.nodes.findById(_resmap.getNodes().get(i).getId());
			if(old_node !=null)
			{
				for(int k=0; k<old_node.getConcepts().getValues().size(); k++)
				{
					for(int j=0;j<((NodeConceptsValue)old_node.getConcepts().getValues().get(k)).getConcepts().size(); j++)
					{
						new_concept = copy_concepts.findById(((NodeConceptsValue)old_node.getConcepts().getValues().get(k)).getConcepts().get(j).getId());
						if(new_concept != null)
						{
//	System.out.println("ResourceMap.copyViewableNodes indexing concept copied " + new_concept + " of the node " + _resmap.getNodes().get(i));
							_resmap.getNodes().get(i).getConcepts().addNewValue(new_concept, ((NodeConceptsValue)old_node.getConcepts().getValues().get(k)).getUser());
						}
					}
				}
			}
		}
		// Restore parent-child links in a copy
		copyParentChildNodeLinks(_resmap);
		// Restore the bin node if copied
		_resmap.resetBinNode();
		// Copy ALL authors
		_resmap.getAuthors().addAll(this.getAuthors());
		
		// Restore links between nodes and external objects cycle 1
		for(int i=0; i<_resmap.getNodes().size(); i++)
		{// cycle 1 - start
			switch (_resmap.getNodes().get(i).getNodeType())
			{
				// Reconnect Papers
				case iNode.NODE_TYPE_I_PAPER:
				{
					int new_node_id = _resmap.getNodes().get(i).getId();
					Paper old_paper = (Paper)this.getNodes().findById(new_node_id).getExternalObject();
					Paper new_paper = _resmap.getPapers().findById(old_paper.getId());
					if(new_paper == null)
					{
						new_paper = old_paper.clone();
						_resmap.getPapers().add(new_paper);
					}
					
					if(old_paper.isCreatedBy(new_user))
						new_paper.setCreator(new_user);
					else if(can_see_authors)
					{
						User other_user = _resmap.getUsers().findById(old_paper.getCreator().getId());
						new_paper.setCreator(other_user);
					}
					
//					if(!_resmap.getPapers().addNew(new_paper))
//						new_paper = _resmap.getPapers().findById(old_paper.getId());
					
					_resmap.getNodes().get(i).setExternalObject(new_paper);
					
					// connect authors
					for(int j=0; j<old_paper.getAuthorss().size(); j++)
					{
						int old_author_id = old_paper.getAuthorss().get(j).getId();
						Author new_author = _resmap.getAuthors().findById(old_author_id);
						
						new_paper.getAuthorss().addNew(new_author);
						new_author.getPapers().addNew(new_paper);
					}
					
					// handle summaries (but not nodes for them)
					for(int j=0; j<old_paper.getSummaries().size(); j++)
					{
						Summary old_summary = old_paper.getSummaries().get(j);
						Summary new_summary = _resmap.getSummaries().findById(old_summary.getId());
						if(new_summary == null)
						{
							new_summary = old_summary.clone();
							if(old_summary.isCreatedBy(new_user))
								new_summary.setCreator(new_user);
							else if(can_see_authors)
							{
								User other_user = _resmap.getUsers().findById(old_summary.getCreator().getId());
								new_summary.setCreator(other_user);
							}
							
							_resmap.getSummaries().add(new_summary);
						}
						new_paper.getSummaries().addNew(new_summary);
						new_summary.setPaper(new_paper);
					}
				}
				break;
				case iNode.NODE_TYPE_I_QUIZ:		//Quiz
				case iNode.NODE_TYPE_I_DISSECTION:	//Dissection
				case iNode.NODE_TYPE_I_WADEIN:	//WADEIN
				case iNode.NODE_TYPE_I_CODEEXAMPLE: // Code Example
				case iNode.NODE_TYPE_I_KARELROBOT: // Karel Robot
				case iNode.NODE_TYPE_I_SYS_QUIZGUIDE: //System QuizGUIDE
				case iNode.NODE_TYPE_I_SYS_NAVEX: // System NavEx
				case iNode.NODE_TYPE_I_SYS_WADEIN: // System WADEIn II
//				case iNode.NODE_TYPE_I_LINK: // Link
//				case iNode.NODE_TYPE_I_LINK_POPUP: // Popup Link
				{
					int new_node_id = _resmap.getNodes().get(i).getId();
					iExternalResource old_activity = (iExternalResource)this.getNodes().findById(new_node_id).getExternalObject();
//System.out.println("ResourceMap.copyviewableNodes (old_activity==null):"+(old_activity==null) + " new_node_id="+new_node_id);

					// THIS IS SHORTCUT TO ELIMNATE CONFLICTS B/W KT & CoPE
					iExternalResource new_activity = null;
					if(old_activity != null)
					{
						new_activity = old_activity.clone();
					// end of -- THIS IS SHORTCUT TO ELIMNATE CONFLICTS B/W KT & CoPE

						if(old_activity.isCreatedBy(new_user))
							new_activity.setCreator(new_user);
						else if(can_see_authors)
						{
	//System.out.println("activity of node " + new_node_id + " for activity id = "  + old_activity.getId());
	//System.out.println("\t(old_activity == null) " + (old_activity == null));
	//System.out.println("\t(old_activity.getCreator() == null) " + (old_activity.getCreator() == null));
	
							User other_user = _resmap.getUsers().findById(old_activity.getCreator().getId());
							new_activity.setCreator(other_user);
						}
						
						if(!_resmap.getActivities().addNew(new_activity))
							new_activity = _resmap.getActivities().findById(old_activity.getId());
						_resmap.getNodes().get(i).setExternalObject(new_activity);
					}
				}
			      break;
			      case iNode.NODE_TYPE_I_CONCEPTS:
			      {
					iNode _new_node = _resmap.getNodes().get(i);
					iNode _old_node = this.getNodes().findById(_new_node.getId());
//System.out.println("ResourceMap.copyViewableNodes trying to clone new_node.id=" + _new_node.getId());
//System.out.println("ResourceMap.copyViewableNodes trying to clone old_node.id=" + _old_node.getId());
					
					NodeConcepts nc_old = (NodeConcepts)_old_node.getExternalObject();
//System.out.println("ResourceMap.copyViewableNodes (_old_node.getExternalObject()==null) = " + (_old_node.getExternalObject()==null));
					NodeConcepts nc_new = nc_old.clone();
					if(nc_old.isCreatedBy(new_user))
						nc_new.setCreator(new_user);
					else if(can_see_authors)
					{
						User other_user = _resmap.getUsers().findById(nc_old.getCreator().getId());
						nc_new.setCreator(other_user);
					}

					
					for(int j=0; j<nc_old.getConcepts().size(); j++)
					{
						iConcept _old_concept = nc_old.getConcepts().get(j);
						iConcept _new_concept = _resmap.getConcepts().findById(_old_concept.getId());
						if(new_concept != null)
						{
							nc_new.getConcepts().add(_new_concept);
//System.out.println("ResourceMap.copyViewableNodes an indexing concept.id=" + _old_concept.getId() + "copied");
						}
						
//else System.out.println("ResourceMap.copyViewableNodes an indexing concept.id=" + _old_concept.getId() + " --NOT-- copied");
					}
					_new_node.setExternalObject(nc_new);
				}
				break;
				case 0:
				{

				}
				break;
			}

		}// cycle 1 - end
		
System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes papers#" + _resmap.getPapers().size());
System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes activities#" + _resmap.getActivities().size());
		// Restore links between nodes and external objects cycle 2
		for(int i=0; i<_resmap.getNodes().size(); i++)
		{// cycle 2 - start
			switch (_resmap.getNodes().get(i).getNodeType())
			{
				// Reconnect Summaries
				case iNode.NODE_TYPE_I_SUMMARY:
				{
					int new_node_id = _resmap.getNodes().get(i).getId();
//System.out.println("ResourceMap.copyViewableNodes new_node_id=" + new_node_id);
					Summary old_summary = (Summary)this.getNodes().findById(new_node_id).getExternalObject();
//System.out.println("ResourceMap.copyViewableNodes old_summary.id=" + old_summary.getId());
					Summary new_summary = _resmap.getSummaries().findById(old_summary.getId());
//System.out.println("ResourceMap.copyViewableNodes new_summary==null" + (new_summary==null));
					if(new_summary == null)
					{
System.out.println("ResourceMap.copyViewableNodes STRANGE, SUMMARY NOT ADDED (summary_id=" + old_summary.getId() + ")");
						new_summary = old_summary.clone();
						if(old_summary.isCreatedBy(new_user))
							new_summary.setCreator(new_user);
						_resmap.getSummaries().add(new_summary);
					}
					_resmap.getNodes().get(i).setExternalObject(new_summary);
				}
				break;
			}
		}// cycle 2 - end
System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes summaries#" + summaries.size());

		// copy personal ratings
		User _old_user = users.findById(new_user.getId());
		for(int i=0; i<_old_user.getRatings().size(); i++)
		{
			Rating old_rating = _old_user.getRatings().get(i);
			iNode new_node = _resmap.getNodes().findById(
				old_rating.getNode().getId());
			if(new_node != null)
			{
				// make a copy of the rating
				Rating new_rating = new Rating(
					old_rating.getRatingValue(), 
					old_rating.getAnonymous(), old_rating.getComment(),
					new_user, new_node);
				// add rating to the new user and the new node
				_resmap.getRatings().add(new_rating);
				new_user.getRatings().add(new_rating);
				new_node.setPersonalRating(new_rating);
			}
		}
System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes personal ratings#" + new_user.getRatings().size());

		// copy group ratings
		User _old_group = users.findById(new_group.getId());
		int count = 0;
		for(int i=0; i<_old_group.getRatings().size(); i++)
		{
			Rating old_rating = _old_group.getRatings().get(i);
//System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes group ratings: group_rating.user_id=" + old_rating.getUser().getId());
//System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes group ratings: user.user_id=" + new_user.getId());
			
			if(old_rating.getUser().getId() == new_user.getId())
				continue;
			
			iNode new_node = _resmap.getNodes().findById(
				old_rating.getNode().getId());
			if(new_node != null)
			{// if rated node found
				count++;
				User new_group_rater = null;
				if(can_see_authors)
				{
					int old_rating_author_id = old_rating.getUser().getId();
					new_group_rater = _resmap.getUsers().findById(old_rating_author_id);
					if(new_group_rater == null)				
						System.out.println("ResourceMap.copyViewableNodes cannot find a group rater with id=" + old_rating_author_id + "for node " + new_node);
				}
				
				// make a copy of the rating - with a null or specified author
				Rating new_rating = new Rating(
					old_rating.getRatingValue(), 
					old_rating.getAnonymous(), old_rating.getComment(),
					new_group_rater, new_node);
				// add rating to the new user and the new node
				_resmap.getRatings().add(new_rating);
				new_group.getRatings().add(new_rating);
				if(new_node.getGroupRating() == null)
					new_node.setGroupRating(new Rating(new_node));
				new_node.getGroupRating().addRating(new_rating);
			}// end -- if rated node found
			else
				System.out.println("ResourceMap.copyViewableNodes cannot find a rated node " +  old_rating.getNode());
		}
System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes group ratings#" + count);

		// Add CoPE Search
		iNode cope_node = _resmap.getNodes().findByTitle("CoPE (Collaborative Paper Exchange)");
		if(cope_node!=null)
		{
			iNode cope_search_nd = new CoPESearchNode(-_resmap.getNextVirtualNodeId());
			_resmap.getNodes().add(cope_search_nd);
			cope_node.getChildren().add(0, cope_search_nd);
			cope_search_nd.setParent(cope_node);
			_resmap.setCopeSearchNodeId(cope_search_nd.getId());
			_resmap.resetCopeSearchNodeId();
		}
		

		// Recalculate Root nodes Vector
		_resmap.resetRootNode();
		
//		// Add nodes to My Documents
//		count = 0;
//		for(int i=0; i<_resmap.getNodes().size(); i++)
//		{
//			// if own copy to My Documents
//			if( (_resmap.getNodes().get(i).getNodeType() != iNode.NODE_TYPE_I_ALL) &&
//					(_resmap.getNodes().get(i).getNodeType() != iNode.NODE_TYPE_I_BIN) &&
//					(_resmap.getNodes().get(i).getNodeType() != iNode.NODE_TYPE_I_MYPROFILE) &&
//					(_resmap.getNodes().get(i).getNodeType() != iNode.NODE_TYPE_I_SUMMARY) &&
//					(_resmap.getNodes().get(i).getNodeType() != iNode.NODE_TYPE_I_CONCEPTS) &&
//					(_resmap.getNodes().get(i).getFolderFlag() == false) &&
//					(_resmap.getNodes().get(i).getUser() != null) && 
//					(_resmap.getNodes().get(i).getUser().getId() == new_user.getId()) )
//			{
//				iNode new_my_doc = _resmap.getNodes().get(i).clone(new_user, true);
//				new_my_doc.setId(-_resmap.getNextVirtualNodeId());
//				
//				_resmap.getRootNode().getChildren().get(1).getChildren().add(new_my_doc);
//				new_my_doc.setParent(_resmap.getRootNode().getChildren().get(1));
//				count ++;
//			}
//		}
//System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes my documets#" + count);
		
		// Expand nodes that hold 'foreign' hierarchies
		
		
System.out.println("~~~ [CoPE] ResourceMap.copyViewableNodes nodes#" + nodes.size());
	}// end -- copyViewableNodes

	/** The function loads parent-child links from a calling server
	 * <i>ResourceMap</i> to a specified client <i>ResourceMap</i>.
	 * Prior to invocation of this function nodes in client <i>ResourceMap</i>
	 * contain no parent-child links.
	 * @param _resmap - target resource map, that nodes should be added to
	 */
	private void copyParentChildNodeLinks(ResourceMap _resmap)
	{
		for(int i=0; i<_resmap.getNodes().size(); i++)
		{
			iNode other_node = _resmap.getNodes().get(i);
			if(other_node.getNodeType()==iNode.NODE_TYPE_I_MYPROFILE)
				continue;
//			if( (other_node.getParent() != null) && (other_node.getParent().getNodeType()==iNode.NODE_TYPE_I_ALL))
//				continue;
			iNode this_node_parent =
				nodes.findById(other_node.getId()).getParent();
			if(this_node_parent != null)
			{
//if(this_node_parent.getId()==3058)
//{
//	System.out.println("this_node_parent=" + this_node_parent + " has children " + this_node_parent.getChildren().size() + " looking for " + other_node);
//	for(Iterator iter=this_node_parent.getChildren().iterator();iter.hasNext();)
//		System.out.println("\t" + iter.next());
//}
				int parent_child_index = this_node_parent.getChildren().findIndexById(other_node.getId());
				double parent_child_weight = this_node_parent.getChildren().getWeight(parent_child_index);
				iNode other_node_potential_parent =
					_resmap.getNodes().findById(this_node_parent.getId());
				if(other_node_potential_parent != null)
				{
					other_node.setParent(other_node_potential_parent);
					other_node_potential_parent.getChildren().
						addNew(other_node, parent_child_weight);
				}
			}
		}
	}// -- end copyParentChildNodeLinks

	/** The function copies a specified set of rights from a calling server
	 * <i>ResourceMap</i> to a specified client <i>ResourceMap</i>. All
	 * necessary links to and from client nodes are set. Globally defined and
	 * globally accessible nodes are also set for a client <i>ResourceMap</i>.
	 * @param _resmap - client <i>ResourceMap</i>,  nodes are copied into.
	 * @param _rights - rights that would identify a viewable nodes set
	 */
	public void applyRights(ArrayList<Right> _rights, ResourceMap _resmap)
	{
//System.out.println("ResourceMap.applyRights no of rights =" + _rights.size());
		for(int i=0; i<_rights.size(); i++)
		{
			Right new_right = _rights.get(i).clone();
//if((new_right.getOwnerFlag())) System.out.println("owner user_id=" + new_right.getUser());
			iNode target_node = _rights.get(i).getNode();
			User new_user = null;
			if(target_node != null)
			{// apply for individual
				iNode other_node = _resmap.getNodes().findById(
					target_node.getId());
				if(other_node != null)
					new_right.setNode(other_node);
				else
					System.out.println("ResourceMap.applyRights " +
						"missing target node");
				other_node.getRights().add(new_right);
			}
			else
			{// apply for all nodes
//				for(int j=0; j<_resmap.getNodes().size(); j++)
//					_resmap.getNodes().get(j).getRights().
//						add(new_right);
				// add a globally defined right to a client resmap
				_resmap.getGloballyDefinedRights().add(new_right);
			}
			if(_rights.get(i).getUser() == null)
				_resmap.getGloballyAccessibleRights().add(new_right);
			else
			{
				new_user = _resmap.getUsers().findById(_rights.get(i).
					getUser().getId());
				if(new_user == null)
				{
					System.out.println("ResourceMap.applyRights new_user null (should be " + _rights.get(i).getUser().getId() + ")");
				}
				else
				{
					new_user.getRights().add(new_right);
					new_right.setUser(new_user);
				}
			}

			_resmap.getRights().add(new_right);
		}
	}// -- end applyRights

	public static void displayFolderView(ItemVector<iNode> folder_list,
		JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException
	{
//System.out.println("ResourceMap.displayFolderView entering...");
		// get the user_id
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		ResourceMap res_map = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
//System.out.println("ResourceMap.displayFolderView activities " + res_map.getActivities().size());/// DEBUG
		User user = res_map.getUsers().findById(user_id);
		User group = res_map.getUsers().findById(group_id);
		
		// Show 'Folders' caption
		out.println("<div class='pt_main_subheader'>Folders/Documents</div>");
		// compile subfolders and subitems separately
		
//		Item2Vector<iNode> folder_list_d = new Item2Vector<iNode>();//docs
//		Item2Vector<iNode> folder_list_f = new Item2Vector<iNode>();//folders
//		for(int i=0; i<folder_list.size(); i++)
//		{
//			if(folder_list.get(i).getFolderFlag() 
//				|| folder_list.get(i).getNodeType() == iNode.NODE_TYPE_I_MYPROFILE)
//				folder_list_f.add(folder_list.get(i));
//			else
//				folder_list_d.add(folder_list.get(i));
//		}

		// Show folders
		String icon = "";
		String alt = "";
	
		for(int i=0; i<folder_list.size(); i++)
		{
			iNode child = folder_list.get(i);
			
			icon = iNode.NODE_TYPE_ICONS_LARGE[child.getNodeType()];
			alt = "&curren;";
			
			// html for rating
			String rating_html = "";
			String rating_icon = "stars_0.gif";
			
//float own = 2.0f;			
//float other1 = 2.3f;			
//float other2 = 2.75f;			
//System.out.println("#ResourceMap.displayFolderView own=" + own + " (int)own=" + (int)Math.round(own)); /// DEBUG
//System.out.println("#ResourceMap.displayFolderView other1=" + other1 + " (rnd)other1=" + (int)Math.round(other1)); /// DEBUG
//System.out.println("#ResourceMap.displayFolderView other2=" + other2 + " (rnd)other2=" + (int)Math.round(other2)); /// DEBUG
//System.out.println("#ResourceMap.displayFolderView other1=" + other1 + " (*)other1=" + ((float)Math.round((float)other1*2))/2); /// DEBUG
//int other2_hat = 0;

			if(show_ratings)
			{
				float value = 0;
//				boolean personal = false;
				if(child.getPersonalRating() != null) // PERSONAL RATING
				{
					value = child.getPersonalRating().getRatingValue();

					rating_icon = "stars_own" + (int)value + ".gif";

//					rating_icon = "stars_own" + (int)value + ".gif";
//					personal = true;
//System.out.println("#ResourceMap.displayFolderView own rating"); /// DEBUG
				}
				else if(child.getGroupRating() != null) // GROUP RATING
				{
					value = child.getGroupRating().getRatingValue();

					if(((float)Math.round((float)value*2))/2 == Math.ceil(((float)Math.round((float)value*2))/2))
						rating_icon = "stars_other" + (int)value + ".gif";
					else
						rating_icon = "stars_other" + ((float)Math.round((float)value*2))/2 + ".gif";
//System.out.println("#ResourceMap.displayFolderView other rating"); /// DEBUG
				}
				
				rating_html = 
					"<img src='" + request.getContextPath() +
					ClientDaemon.CONTEXT_ASSETS_PATH +	"/" + 
					rating_icon + "'/>";
				
			}
			else
				rating_html = "";

			String div_style = "pt_main_folder_document";
			float match = child.getProfileMatch(user);
			if(match >= .5)
				div_style = "pt_main_folder_document3";
			else if(match >= .1)
				div_style = "pt_main_folder_document2";
//			out.println("<div style='padding-left:5px; " +
//				"border-bottom: 1px dotted gray;'>" +
//System.out.println("#ResourceMap.displayFolderView title=" + child.getTitle() + " match="  + match  + " div_style=" + div_style); /// DEBUG
			boolean can_see_author = child.canSeeAuthor(user, group, res_map);
			String display_title = child.getTitle() + 
				((can_see_author)? "&nbsp;<sup>[" + child.getCreatorAdderNames() + 
						"]</sup>": "");
			
			out.println("<div class='" + div_style + "'>" +
				rating_html + "&nbsp;" + 
				"<img src='" + request.getContextPath() +
				ClientDaemon.CONTEXT_ASSETS_PATH +
				"/" + icon + "' border='0' alt='" + alt +
				"' /><a" + ((child.isCreatedBy(user_id))?" style='text-decoration:underline;color:#006600;'":"") +
				" href='" + request.getContextPath() +
				"/content/Show?" +  ClientDaemon.REQUEST_NODE_ID +
				"=" + child.getId() + "' target='_top'>" +
				"&nbsp;" + display_title + "</a></div>");
		}

	}// - end - displayFolderView

	public static void displayFolderView(ItemVector<iNode> folder_list,
		PrintWriter out, HttpServletRequest request) throws IOException
	{
	}

	public boolean isAllowedWhatWhoFor(int _right_type, int _user_id,
		iNode _node)
	{
		boolean result = false;

		// First check the trail to the root
		iNode current_node = _node;
		boolean ini_authorship = false;
		if(_node.getUser() != null)
			ini_authorship = (_node.getUser().getId() == _user_id);
//System.out.println("ResourceMap.isAllowedWhatWhoFor before trail, current_node " + current_node);
		while( (result == false) && (current_node != null) )
		{
//System.out.println("ResourceMap.isAllowedWhatWhoFor trail node #" + current_node.getId());
			result = current_node.isAllowedWhatWho(_right_type, _user_id,
				ini_authorship);
			current_node = current_node.getParent();
		}
//System.out.println("ResourceMap.isAllowedWhatWhoFor before globally defined");
		// Second check globally defined rights
		for(int i=0; i<this.globally_defined_rights.size(); i++)
		{
			if( this.globally_defined_rights.get(i).
				isAllowedWhatWhoFor(_right_type, _user_id,
				_node.getId(), ini_authorship) )
			{
				result = true;
				break;
			}
		}
//System.out.println("ResourceMap.isAllowedWhatWhoFor before globally accessible");
		// Third check globally accessible rights
		for(int i=0; i<this.globally_accessible_rights.size(); i++)
		{
			if( this.globally_accessible_rights.get(i).
				isAllowedWhatWhoFor(_right_type, _user_id,
				_node.getId(), ini_authorship) )
			{
				result = true;
				break;
			}
		}
		return result;
	}// end of - isAllowedWhatWhoFor


/*	public boolean isAllowedWhatWhoForFrom(int _right_type, int _user_id,
		iNode _node, int _parent_node_type)
	{
		boolean result = false;
//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check trail to root");
		// First check the trail to the root
		iNode current_node = _node;
//System.out.println("ResourceMap.isAllowedWhatWhoForTo before trail, current_node " + current_node);
		while( (result == false) && (current_node != null) )
		{
//System.out.println("\tChecking node # " + current_node.getId());
//System.out.println("ResourceMap.isAllowedWhatWhoForTo trail node #" + current_node.getId());
			result = current_node.isAllowedWhatWhoFrom(_right_type,
				_user_id, _parent_node_type);
			current_node = current_node.getParent();
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally defined rights");
		// Second check globally defined rights
		for(int i=0; i<this.globally_defined_rights.size(); i++)
		{
//System.out.println("\tChecking globally defined right # " + i);
			if( this.globally_defined_rights.get(i).
				isAllowedWhatWhoForFrom(_right_type, _user_id,
				_node.getId(), _parent_node_type) )
			{
				result = true;
				break;
			}
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally accessible rights");
		// Third check globally accessible rights
		for(int i=0; i<this.globally_accessible_rights.size(); i++)
		{
//System.out.println("\tChecking globally accessible right # " + i);
			if( this.globally_accessible_rights.get(i).
				isAllowedWhatWhoForFrom(_right_type, _user_id,
				_node.getId(), _parent_node_type) )
			{
				result = true;
				break;
			}
		}
		return result;
	}// - end - isAllowedWhatWhoForFrom/**/

	public boolean isAllowedWhatWhoForFromTo(int _right_type, int _user_id,
		iNode _node, int _parent_node_type, int _child_node_type)
	{
		boolean result = false;
//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check trail to root");
		// First check the trail to the root
		iNode current_node = _node;
		boolean ini_authorship = _node.isCreatedBy(_user_id);

//System.out.println("ResourceMap.isAllowedWhatWhoForTo before trail, current_node " + current_node);
//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo ini_authorship=" + ini_authorship);
		while( (result == false) && (current_node != null) )
		{
//System.out.println("\tChecking node # " + current_node.getId());
//System.out.println("ResourceMap.isAllowedWhatWhoForTo trail node #" + current_node.getId());
			result = current_node.isAllowedWhatWhoFromTo(_right_type,
				_user_id, _parent_node_type,_child_node_type,
				ini_authorship);
			current_node = current_node.getParent();
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally defined rights");
		// Second check globally defined rights
		for(int i=0; i<this.globally_defined_rights.size(); i++)
		{
//System.out.println("\tChecking globally defined right # " + i);
			if( this.globally_defined_rights.get(i).
				isAllowedWhatWhoForFromTo(_right_type, _user_id,
				_node.getId(), _parent_node_type, _child_node_type,
				ini_authorship) )
			{
				result = true;
				break;
			}
		}
		return result;
	}// end of - isAllowedWhatWhoForFromTo

	public boolean isAllowedWhatWhoForFromTo_DownInhibitory(int _right_type, int _user_id,
		iNode _node, int _parent_node_type, int _child_node_type)
	{
		boolean result = false;
//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check trail to root");
		// First check the trail to the root
		iNode current_node = _node;
		boolean ini_authorship = _node.isCreatedBy(_user_id);

//System.out.println("ResourceMap.isAllowedWhatWhoForTo before trail, current_node " + current_node);
//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo ini_authorship=" + ini_authorship);

		result = current_node.isAllowedWhatWhoFromTo_DownInhibitory(_right_type,
			_user_id, _parent_node_type,_child_node_type,
			ini_authorship, globally_defined_rights,
			globally_accessible_rights);


//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo result = " + result);
		return result;
	}// end of - isAllowedWhatWhoForFromTo


	public boolean isAllowedWhatWho2ForFromToQuant(int _right_type, int _user_id, int _group_id,
		iNode _node, int _parent_node_type, int _child_node_type)
	{
		boolean result = false;
//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check trail to root");
		// First check the trail to the root
		iNode current_node = _node;
		int children_type_count = _node.getChildCountOfTypeByUser(_child_node_type, _user_id);
//System.out.print("ResourceMap.isAllowedWhatWhoForToQuant right=" + _right_type + " user=" + _user_id + " group=" + _group_id +
//" parentT=" + _parent_node_type + " childT=" + _child_node_type + " children#="+children_type_count);
		boolean ini_authorship = false;
		if(_node.getUser() != null)
			ini_authorship = (_node.getUser().getId() == _user_id);
//System.out.println("ResourceMap.isAllowedWhatWhoForTo before trail, current_node " + current_node);
//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo ini_authorship=" + ini_authorship);
		while( (result == false) && (current_node != null) )
		{
//System.out.println("\tChecking node # " + current_node.getId());
//System.out.println("ResourceMap.isAllowedWhatWhoForTo trail node #" + current_node.getId());
			result =
				(current_node.isAllowedWhatWhoFromToQuant(_right_type,
				_user_id, _parent_node_type,_child_node_type,
				ini_authorship, children_type_count))
				||
				(current_node.isAllowedWhatWhoFromToQuant(_right_type,
				_group_id, _parent_node_type,_child_node_type,
				ini_authorship, children_type_count)) ;
			current_node = current_node.getParent();
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally defined rights");
		// Second check globally defined rights
		for(int i=0; i<this.globally_defined_rights.size(); i++)
		{
//System.out.println("\tChecking globally defined right # " + i);
			if( this.globally_defined_rights.get(i).
				isAllowedWhatWhoForFromToQuant(_right_type, _user_id,
				_node.getId(), _parent_node_type, _child_node_type,
				ini_authorship, children_type_count)
				||
				this.globally_defined_rights.get(i).
				isAllowedWhatWhoForFromToQuant(_right_type, _group_id,
				_node.getId(), _parent_node_type, _child_node_type,
				ini_authorship, children_type_count)  )
			{
				result = true;
				break;
			}
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForTOOO Check globally accessible rights");
		// Third check globally accessible rights
		for(int i=0; i<this.globally_accessible_rights.size(); i++)
		{
//System.out.println("\tChecking globally accessible right # " + i);
			if( this.globally_accessible_rights.get(i).
				isAllowedWhatWhoForFromToQuant(_right_type, _user_id,
				_node.getId(), _parent_node_type, _child_node_type,
				ini_authorship, children_type_count)
				||
				this.globally_accessible_rights.get(i).
				isAllowedWhatWhoForFromToQuant(_right_type, _group_id,
				_node.getId(), _parent_node_type, _child_node_type,
				ini_authorship, children_type_count)  )
			{
				result = true;
				break;
			}
		}

//System.out.println("ResourceMap.isAllowedWhatWhoForFromTo result = " + result);
//System.out.println(" " + result);
		return result;
	}// end of - isAllowedWhatWhoForFromToQuant


	public void outputNodeTree(JspWriter out, HttpServletRequest req,
		iNode current_node, int user_id, int group_id, int show_mode, boolean track_opens)
		throws IOException
	{
//Calendar start = null;
//Calendar finish = null;
//long diff_mills;
//start = new GregorianCalendar();
		User user = null;
		User group = null;
		if(user_id > 0) user = getUsers().findById(user_id);
		if(group_id > 0) group = getUsers().findById(group_id);
		for(int i=0;i<root_node.getChildren().size();i++)
		{
			root_node.getChildren().get(i).outputTree( out, req, current_node,
				0, show_mode, user, group, this, "");
		}
		
//			root_nodes.get(i).outputTreeNode(out, req, current_node, 0, 
//			show_mode, user, track_opens);
//finish = new GregorianCalendar();
//diff_mills = finish.getTimeInMillis() - start.getTimeInMillis();
//System.out.println("\t[CoPE] ResourceMap.outputNodeTree millisec passed " + diff_mills);
	}

	public ItemVector<iConcept> getConcepts() { return concepts; }
	public Item2Vector<iExternalResource> getActivities() { return activities; }
	public void setConcepts(ItemVector<iConcept> _concepts) { concepts = _concepts; }

	public ItemVector<iConcept> getConceptsByParameterEnumeration(Enumeration e)
	{
		ItemVector<iConcept> result = new ItemVector<iConcept>();
		while(e.hasMoreElements())
		{
			String val = (String)e.nextElement();
			Pattern p = Pattern.compile("concept[0-9]+");
			Matcher m = p.matcher("");
			m.reset(val);
			if(m.matches())
			{
//System.out.print("ResourceMap.getNodesByParameterEnumeration parameter: " + val);
				int concept_id = Integer.parseInt(val.substring(7));
				iConcept concept = this.concepts.findById(concept_id);
				if(concept != null)
				{
					result.add(concept);
//System.out.println("  " + concept);
				}
				else
					System.out.print("ResourceMap.getNodesByParameterEnumeration ERROR! Sumbitted concept not found: " + val);
			}
		}
		return result;
	}
	
	public int getNextVirtualNodeId() { return ++top_virtual_node; } 
	public int getTopVirtualNode() { return top_virtual_node; } 
	public ItemVector<iConcept> getAllOntologies()
	{
		ItemVector<iConcept> result = new ItemVector<iConcept>();
		for(int i=0; i<nodes.size(); i++)
			for(int j=0; j<nodes.get(i).getOntologies().size(); j++)
				result.addNew(nodes.get(i).getOntologies().get(j));
		return result;
	}
	
	public boolean sendNodeToBin(iNode node_to_bin, boolean put_to_bin)
	{
		boolean result = false;
		if(node_to_bin == null)
			return result;
		iNode parent = node_to_bin.getParent();
		if(parent != null) // if has parent
		{
//System.out.println("~~ parent " + parent.getTitle());
			int index = parent.getChildren().findIndexById(node_to_bin.getId());
//System.out.println("~~ child children " + parent.getChildren().size());
//System.out.println("~~ no of index " + index);
			parent.getChildren().removeElementAt(index);
//			result = (deleted!=null)?true:false;
			result = true;
		}
//System.out.println("~~ bin_node==null " + (bin_node==null));
		if((bin_node != null)&&(put_to_bin))
		{
			node_to_bin.setParent(bin_node);
			bin_node.getChildren().add(node_to_bin);
		}
		return result;
	}
	
	public iNode getRootNode() {return root_node; }
	
	public int getBinNodeId() {return bin_node_id; }
	public void resetBinNode()
	{
		if(bin_node_id != 0)
			bin_node = nodes.findById(bin_node_id);
//System.out.println("!! bin_node==null " + (bin_node==null));
	}

	public void resetCopeSearchNodeId()
	{
		if(copesearch_node_id != 0)
			copesearch_node = nodes.findById(copesearch_node_id);
//System.out.println("!! copesearch_node==null " + (copesearch_node==null));
	}

	public void setCopeSearchNodeId(int _cope_search_node_id)
	{
		copesearch_node_id = _cope_search_node_id;
	}
	public int getCopeSearchNodeId() {return copesearch_node_id; }
	
	/** Method returns a vector of all ratings in the current view of the portal
	 * @return a vector of all ratings in the current view of the portal
	 * @since 1.5
	 */
	public Vector<Rating> getRatings() { return ratings; }
	
	public void setPendingNode(iNode _pending_node)
	{
		pending_node = _pending_node;
//System.out.println("ResourceMap.setPendingNode pending_node=" + pending_node);
	}

	public iNode getPendingNode() { return pending_node; }
	
	/** deleting a pending node */
	public void recycleNode(iNode _pending_node)
	{
//System.out.println("ResourceMap.recycleNode pending_node=" + pending_node);
		if(_pending_node==null && pending_node == null)
		{
			System.out.println("!!! ResourceMap.recycleNode trying to recycle a null node.");
			return;
		}
		
		if(pending_node != null)
			_pending_node = pending_node;
		
		if(_pending_node.getId() != 0)
		{
			System.out.println("!!! ResourceMap.recycleNode trying to recycle a non-pending node. Use deletion instead.");
			return;
		}
		
		if(_pending_node.getChildren().size() != 0)
		{
			System.out.println("!!! ResourceMap.recycleNode trying to recycle a node with children.");
			return;
		}
		
		// logically delete the node
		// 1. from nodes collection
		nodes.remove(_pending_node);
		// 2. from its parent
		if(_pending_node.getParent() != null)
			_pending_node.getParent().getChildren().remove(_pending_node);
		// 3. from pending node pointer
		pending_node = null;
		
		// deal with external objects
		switch(_pending_node.getNodeType())
		{
			case iNode.NODE_TYPE_I_FOLDER:
			case iNode.NODE_TYPE_I_UNTYPDOC:
			case iNode.NODE_TYPE_I_TOPIC_FOLDER:
			{// no special treatment is necessary
				;
			}
			break;
			
			case iNode.NODE_TYPE_I_BIN:
			case iNode.NODE_TYPE_I_COPE_SEARCH:
			case iNode.NODE_TYPE_I_MYPROFILE:
			{ // looks crazy
				System.out.println("!!! ResourceMap.recycleNode trying to recycle a node of this type is alerting (" + iNode.NODE_TYPES_S_ALL[_pending_node.getNodeType()-1] + ")");
			}
			break;
			
			case iNode.NODE_TYPE_I_PAPER:
			{
				if(_pending_node.getExternalObject() == null)
				{
					System.out.println("!!! ResourceMap.recycleNode trying to recycle a null external object if a node.");
				}
				else
				{
					papers.remove(_pending_node.getExternalObject());
					_pending_node.getExternalObject().getOwners().removeAllElements();
					_pending_node.setExternalObject(null);
				}
			}
			break;
			case iNode.NODE_TYPE_I_SUMMARY:
			{
				if(_pending_node.getExternalObject() == null)
				{
					System.out.println("!!! ResourceMap.recycleNode trying to recycle a null external object if a node.");
				}
				else
				{
					summaries.remove(_pending_node.getExternalObject());
					_pending_node.getExternalObject().getOwners().removeAllElements();
					_pending_node.setExternalObject(null);
				}
			}
			break;
			
			case iNode.NODE_TYPE_I_QUIZ:
			case iNode.NODE_TYPE_I_DISSECTION:
			case iNode.NODE_TYPE_I_WADEIN:
			case iNode.NODE_TYPE_I_CODEEXAMPLE:
			case iNode.NODE_TYPE_I_KARELROBOT:
			case iNode.NODE_TYPE_I_SYS_QUIZGUIDE:
			case iNode.NODE_TYPE_I_SYS_NAVEX:
			case iNode.NODE_TYPE_I_SYS_WADEIN:
			{// these are activities
				if(_pending_node.getExternalObject() == null)
				{
					System.out.println("!!! ResourceMap.recycleNode trying to recycle a null external object if a node.");
				}
				else
				{
					activities.remove(_pending_node.getExternalObject());
					_pending_node.getExternalObject().getOwners().removeAllElements();
					_pending_node.setExternalObject(null);
				}
			}
			break;
			
			case iNode.NODE_TYPE_I_CONCEPTS:
			{ // not sure what TODO with it yet 
				;
			}
			break;
			
		}// end of -- switch
		_pending_node = null;
	}// end of -- recyclePendingNode
	
	public Item2Vector<Author> getAuthors() { return authors; }
}

abstract class LoadingThread extends Thread
{
	protected SQLManager sqlm;
	protected ResourceMap resmap;
	protected ArrayList<Thread> prereqs;
	
	public LoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		sqlm = _sqlm;
		resmap = _resmap;
		prereqs = new ArrayList<Thread>();
	}
	
	public ArrayList<Thread> getPrerequisites() { return prereqs; }
}

class UserLoadingThread extends LoadingThread
{
	public UserLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM ent_user WHERE (Name<>'MACRO');";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				int user_id = rs.getInt("UserID");
				String user_name = rs.getString("Name");
				String user_login = rs.getString("Login");
				String user_uri = rs.getString("URI");
				boolean is_group = (rs.getInt("isGroup")!=0)?true:false;

				resmap.getUsers().add( new User(user_id,user_name,user_uri,user_login, is_group));
				count++;
			}
			rs.close();
			rs = null;
			statement.close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap users#" + count); /// DEBUG
			
		}
	}
}

class UserLinkLoadingThread extends LoadingThread
{
	public UserLinkLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();

			conn = sqlm.getConnection();
			qry = "SELECT * FROM rel_user_user;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				int parent_user_id = rs.getInt("ParentUserID");
				int child_user_id = rs.getInt("ChildUserID");
				User parent_user = resmap.getUsers().findById(parent_user_id);
				User child_user = resmap.getUsers().findById(child_user_id);
				parent_user.getSubordinates().add(child_user);
				child_user.getSuperordinates().add(parent_user);
				count++;
			}
			rs.close();
			rs = null;
			statement.close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap group-user links#" + count); /// DEBUG
		}
	}
}


class AuthorLoadingThread extends LoadingThread
{
	public AuthorLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM ent_cope_author;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				// node data
				int author_id = rs.getInt("AuthorID");
				String last_name = rs.getString("LastName");
				String given_names = rs.getString("GivenNames");
				
				String author_uri = "";//"author" + author_id;
				
				resmap.getAuthors().add(new Author(author_id, last_name, author_uri, given_names));
				count++;
			}// end -- load Papers

			rs.close();
			rs = null;
			statement.close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap authors#" + count); /// DEBUG
		}
	}
}

class PaperLoadingThread extends LoadingThread
{
	public PaperLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT p.PaperID, p.Title AS pTitle, p.Year, p.URL AS pURL," +
				" p.BiblioInfo, p.Authors, n.* " + 
				"FROM ent_cope_paper p LEFT OUTER JOIN ent_node n" + 
				" ON (p.PaperID = n.ExtID) WHERE n.ItemTypeID=6 " +
				"GROUP BY p.PaperID, p.Title, p.Year, p.URL, p.BiblioInfo, " +
				"p.Authors;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				// node data
				int user_id = rs.getInt("UserID");
				String node_url = rs.getString("URL");
				User user = resmap.getUsers().findById(user_id);
				// paper data
				int paper_id = rs.getInt("PaperID");
				String paper_title = rs.getString("pTitle");
				int paper_year = rs.getInt("Year");
				String paper_biblioinfo = rs.getString("BiblioInfo");
				String paper_uri = "";//"paper" + paper_id;
				String paper_authors = rs.getString("Authors");
				resmap.getPapers().addNew( new Paper(paper_id, paper_title, paper_uri, paper_year,
					paper_biblioinfo, paper_authors, node_url, user) );
				count++;
			}// end -- load Papers
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap papers#" + count); /// DEBUG
		}
	}
}

class PaperAuthorLinkLoadingThread extends LoadingThread
{
	public PaperAuthorLinkLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM rel_cope_paper_author ORDER BY PaperID, Idx;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				// node data
				int author_id = rs.getInt("AuthorID");
				int paper_id = rs.getInt("PaperID");
				
				Author author = resmap.getAuthors().findById(author_id);
				Paper paper = resmap.getPapers().findById(paper_id);
				
				if(author == null || paper == null)
				{
					System.out.print("ResourceMap.loadMap error in Author-Paper link ");
					if(paper == null)System.out.print("paper id " + paper_id + " not found ");
					if(author == null)System.out.print("author id " + author_id + " not found ");
					System.out.println("");
				}
				else
				{
					paper.getAuthorss().add(author);
					author.getPapers().add(paper);
					count++;
				}
			}// end -- load Papers
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap author-paper links#" + count); /// DEBUG
		}
	}
}

class SummaryLoadingThread extends LoadingThread
{
	public SummaryLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM ent_cope_summary;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				int summary_id = rs.getInt("SummaryID");
				int paper_id = rs.getInt("PaperID");
				String summary_text = rs.getString("SummaryText");
				int user_id = rs.getInt("UserID");
				User user = resmap.getUsers().findById(user_id);
				Paper paper = resmap.getPapers().findById(paper_id);
				
				String summary_uri = "";//"summary" + summary_id; 
				
				Summary summary = new Summary(summary_id, user, summary_uri, summary_text, paper);
				resmap.getSummaries().add(summary);
				paper.getSummaries().addNew(summary);
				count++;
			}// end -- load Papers
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap summaries#" + count); /// DEBUG
		}
	}
}

class ActivityLoadingThread extends LoadingThread
{
	public ActivityLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			String activity_types = "";
			for(int i=0; i<iNode.NODE_TYPES_I_ACTIVITIES.length; i++)
			      activity_types+= ((activity_types.length()>0)?",":"") + 
					iNode.NODE_TYPES_I_ACTIVITIES[i];
			qry = "SELECT a.ActivityID, a.ExtID AS aExtID, a.Title AS aTitle, a.URI " + 
				"AS aProperties, a.URL AS aURL, n.* " + 
				"FROM ent_activity a LEFT OUTER JOIN ent_node n" + 
				" ON (a.ActivityID = n.ExtID) WHERE n.ItemTypeID IN (" +
				activity_types + ")" +
				"GROUP BY a.ActivityID, a.Title, a.URI, a.URL;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();

			while(rs.next())
			{
				// node data
				int user_id = rs.getInt("UserID");
				int node_item_type = rs.getInt("ItemTypeID");
				User user = resmap.getUsers().findById(user_id);
				// Activity data
				int activity_id = rs.getInt("ActivityID");
				String activity_ext_id = rs.getString("aExtID");
				String activity_title = rs.getString("aTitle");
				String activity_url = rs.getString("aURL");
				
				String activity_uri = "";//"activity" + activity_id;
				
				switch (node_item_type) 
				{
					case iNode.NODE_TYPE_I_QUIZ:
					case iNode.NODE_TYPE_I_DISSECTION:
					case iNode.NODE_TYPE_I_WADEIN:
					case iNode.NODE_TYPE_I_CODEEXAMPLE:
					case iNode.NODE_TYPE_I_LINK:
					{
						resmap.getActivities().addNew( new ExternalResourceVisual(activity_id, activity_title, activity_uri,
								user, activity_url, activity_ext_id) );
					}
					break;
					case iNode.NODE_TYPE_I_KARELROBOT:
					{
						resmap.getActivities().addNew( new ExternalResourceDownloadable(activity_id, activity_title, activity_uri,
					    		  user, activity_url, activity_ext_id) );
					}
					break;
					case iNode.NODE_TYPE_I_SYS_QUIZGUIDE:
					case iNode.NODE_TYPE_I_SYS_NAVEX:
					case iNode.NODE_TYPE_I_SYS_WADEIN:
					case iNode.NODE_TYPE_I_LINK_POPUP:
					{
						resmap.getActivities().addNew( new ExternalResourcePopup(activity_id, activity_title, activity_uri,
								user, activity_url, activity_ext_id) );
					}
					break;
				}
						
				count++;
			}// end -- load Activities
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap activities#" + count); /// DEBUG
		}
	}
}

class NodeLoadingThread extends LoadingThread
{
	public NodeLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM ent_node ;"; //WHERE (Description<>'MACRO') OR (Description IS NULL)
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				int node_id = rs.getInt("NodeID");
				String node_title = rs.getString("Title");
				String node_descr = rs.getString("Description");
				int user_id = rs.getInt("UserID");
				int node_type = rs.getInt("ItemTypeID");
				String node_url = rs.getString("URL");
				String node_params = rs.getString("URI");
				String external_id = rs.getString("ExtID");
				int folder_flag_i = rs.getInt("FolderFlag");

				User user = resmap.getUsers().findById(user_id);
				node_descr = (node_descr==null) ? "" : node_descr ;
				node_url = (node_url==null) ? "" : node_url ;
				node_params = (node_params==null) ? "" : node_params ;
				boolean folder_flag = (folder_flag_i == 1)?true:false;

				// decode the type of the node
				switch (node_type)
				{
					case iNode.NODE_TYPE_I_ALL: // Root Node
					{
						iNode node = new NodeUntyped(node_id, node_title,
								node_type, node_descr, node_url, folder_flag,
								node_params, user, null);
						resmap.root_node = node;
						resmap.globally_defined_rights = resmap.root_node.getRights();
						resmap.getNodes().add( node );
					}
					break;
					case iNode.NODE_TYPE_I_FOLDER: // Folder or
					case iNode.NODE_TYPE_I_UNTYPDOC: // Untyped document
					case iNode.NODE_TYPE_I_TOPIC_FOLDER: // Topic Folder
					case iNode.NODE_TYPE_I_BIN: // Recycled bin
					case iNode.NODE_TYPE_I_LINK: // Link
					case iNode.NODE_TYPE_I_LINK_POPUP: // System WADEIn II
					case iNode.NODE_TYPE_I_COURSE: // System WADEIn II
					case iNode.NODE_TYPE_I_ADD1: // System WADEIn II
					case iNode.NODE_TYPE_I_ADD2: // System WADEIn II
					{
						iNode node = new NodeUntyped(node_id, node_title,
							node_type, node_descr, node_url, folder_flag,
							node_params, user, null);
						resmap.getNodes().add( node );
						if(node.getNodeType() == iNode.NODE_TYPE_I_BIN)
						{
							resmap.bin_node_id = node.getId();
							resmap.bin_node = node;
						}
					}
					break;
					case iNode.NODE_TYPE_I_PAPER: // Paper
					{
						int paper_id = 0;
						try
						{
							paper_id = Integer.parseInt(external_id);
							iResource paper = resmap.getPapers().findById(paper_id);
							resmap.getNodes().add( new NodeUntyped(node_id, node_title,
								node_type, node_descr, node_url, folder_flag,
								node_params, user,  paper) );
						}
						catch(Exception e)
						{
							System.out.println("ResourceMap.loadMap Couldn't get paper id for params="+node_params);
						}
					}
					break;
					case iNode.NODE_TYPE_I_SUMMARY: // Summary
					{
						int summary_id = 0;
						try
						{
							summary_id = Integer.parseInt(external_id);
							iResource summary = resmap.getSummaries().findById(summary_id);
							resmap.getNodes().add( new NodeUntyped(node_id, node_title,
								node_type, node_descr, node_url, folder_flag,
								node_params, user,  summary) );
						}
						catch(Exception e)
						{
							System.out.println("ResourceMap.loadMap Couldn't get summary id for params="+node_params);
						}
					}
					break;
					case iNode.NODE_TYPE_I_QUIZ: // Quiz
					case iNode.NODE_TYPE_I_DISSECTION: // Dissection
					case iNode.NODE_TYPE_I_WADEIN: // Wadein
					case iNode.NODE_TYPE_I_CODEEXAMPLE: // Code Example
					case iNode.NODE_TYPE_I_KARELROBOT: // Karel Robot file
					case iNode.NODE_TYPE_I_SYS_QUIZGUIDE: //System QuizGUIDE
					case iNode.NODE_TYPE_I_SYS_NAVEX: // System NavEx
					case iNode.NODE_TYPE_I_SYS_WADEIN: // System WADEIn II
				      {
				            int activity_id = 0;
				            try
				            {
								// THIS IS SHORTCUT TO ELIMNATE CONFLICTS B/W KT & CoPE
								if(external_id != null && external_id.length() > 0)
									activity_id = Integer.parseInt(external_id);
								// end of -- THIS IS SHORTCUT TO ELIMNATE CONFLICTS B/W KT & CoPE
				                  
				                  iResource activity = resmap.getActivities().findById(activity_id);
				                  resmap.getNodes().add( new NodeUntyped(node_id, node_title,
				                        node_type, node_descr, node_url, folder_flag,
				                        node_params, user,  activity) );
				            }
				            catch(Exception e)
				            {
				                  System.out.println("ResourceMap.loadMap Couldn't get activity id for params="+node_params);
				                  e.printStackTrace(System.out);
				            }
				      }
				      case 0:
				      {

				      }
				      break;
				}
				count++;
			}
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap nodes#" + count);  /// DEBUG
		}
	}
}

class NodeLinkLoadingThread extends LoadingThread
{
	public NodeLinkLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM rel_node_node ORDER BY OrderRank;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				int parent_node_id = rs.getInt("ParentNodeID");
				int child_node_id = rs.getInt("ChildNodeID");
				double weight = rs.getInt("Weight");
				iNode parent_node = resmap.getNodes().findById(parent_node_id);
				iNode child_node = resmap.getNodes().findById(child_node_id);
//if(parent_node_id == 3058)
//{
//	System.out.println("parent.id=" + parent_node_id + " child.id=" + child_node_id + " " + parent_node + " " + child_node);
//}
//System.out.println("(parent_node==null):"+(parent_node==null) + " parent_node_id:" + parent_node_id);				
				

				// THIS IS SHORTCUT TO ELIMNATE CONFLICTS B/W KT & CoPE
				if(child_node != null)
				{
					parent_node.getChildren().add(child_node, weight);
					child_node.setParent(parent_node);
				}	
				// end of -- THIS IS SHORTCUT TO ELIMNATE CONFLICTS B/W KT & CoPE

				count++;
			}
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap node-node links#" + count); /// DEBUG
		}
	}
}

class RightLoadingThread extends LoadingThread
{
	public RightLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM ent_right;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				// read data
				int user_id = rs.getInt("UserID");
				int right_type = rs.getInt("RightTypeID");
				int parent_type = rs.getInt("ParentTypeID");
				int child_type = rs.getInt("ChildTypeID");
				int node_id = rs.getInt("NodeID");
				int quantity = rs.getInt("Quantity");
				String desc = rs.getString("Description");
				int ownerFlag = rs.getInt("OwnerFlag");
				// process fata
				int user_macro = ( (user_id != Right.USER_ALL) &&
					(user_id != Right.USER_AUTHOR) ) ?
					((ownerFlag==1)? Right.USER_AUTHOR : 0) : user_id;

				User user = (user_macro == Right.USER_ALL ) ? null : resmap.getUsers().findById(user_id);
				iNode node = resmap.getNodes().findById(node_id);

				Right right = new Right(user, user_macro, right_type, node,
					node_id, parent_type, child_type, quantity,
					desc, (ownerFlag==1));
				resmap.getRights().add(right);

				if(user_macro == Right.USER_ALL)
					resmap.globally_accessible_rights.add(right);
				if(user_macro == Right.USER_AUTHOR)
					resmap.authoritative_rights.add(right);

				// Add rights to target elements - user and node
				if(node != null) node.getRights().add(right);
				else
				{
					System.out.println("CANNNOTTT BEEEEE!!!!");
					for(int i=0; i>resmap.getNodes().size(); i++)
						resmap.getNodes().get(i).getRights().add(right);
				}

				if(user != null) user.getRights().add(right);
//				else
//				{
//					for(int i=0; i>resmap.getUsers().size(); i++)
//						resmap.getUsers().get(i).getRights().add(right);
//				}
				count++;
			}
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap rights#" + count); /// DEBUG
		}
	}
}

class ConceptLoadingThread extends LoadingThread
{
	public ConceptLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM ent_concept ;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				// read data
				int concept_id = rs.getInt("ConceptID");
				String title = rs.getString("Name");

				resmap.getConcepts().add( new Concept(concept_id, title) );
				count++;
			}
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap concepts#" + count); /// DEBUG
		}
	}
}

class ConceptLinkLoadingThread extends LoadingThread
{
	public ConceptLinkLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM rel_concept_concept;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				// read data
				int parent_id = rs.getInt("ParentConceptID");
				int child_id = rs.getInt("ChildConceptID");

				Concept parent = (Concept)resmap.getConcepts().findById(parent_id);
				Concept child = (Concept)resmap.getConcepts().findById(child_id);

				parent.getChildren().add( child );
				child.setParent(parent);

				count++;
			}
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap concept-concept links#" + count);  /// DEBUG
		}
	}
}

class UserConceptLinkLoadingThread extends LoadingThread
{
	public UserConceptLinkLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM rel_user_concept;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				// read data
				int user_id = rs.getInt("UserID");
				int concept_id = rs.getInt("ConceptID");
				User user = resmap.getUsers().findById(user_id);
				iConcept concept = resmap.getConcepts().findById(concept_id);
				user.getConcepts().add(concept);
				count++;
			}		
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap user-concept links#" + count);  /// DEBUG
		}
	}
}

class NodeConceptLinkLoadingThread extends LoadingThread
{
	public NodeConceptLinkLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM rel_node_concept;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			count = 0;
			while(rs.next())
			{
				// read data
				int node_id = rs.getInt("NodeID");
				int concept_id = rs.getInt("ConceptID");
				int type = rs.getInt("RelationType");
				int user_id = rs.getInt("UserID");
				iNode node = resmap.getNodes().findById(node_id);
				iConcept concept = resmap.getConcepts().findById(concept_id);
				User user = resmap.getUsers().findById(user_id);
				switch (type)
				{
					case iConcept.CONCEPT_NODE_REL_ONTOLOGY:
					{
						node.getOntologies().add(concept);
					}
					break;
					case iConcept.CONCEPT_NODE_REL_CONCEPT:
					{
						node.getConcepts().addNewValue(concept, user);
						// Work with NodeConcepts external object
						iNode indexing_node = node.findChildOfTypeByUser(
							iNode.NODE_TYPE_I_CONCEPTS, user_id);
						NodeConcepts nc = null;
						
						String node_concepts_uri = "";//"NodeConcepts" + node.getId();
						
						if(indexing_node == null)
						{
							// create NodeConcepts external object
							nc = new NodeConcepts( node.getId(),
								"Concepts", node_concepts_uri, user);
							indexing_node = new NodeUntyped(						
								(-(++resmap.top_virtual_node)), "Concepts",
								iNode.NODE_TYPE_I_CONCEPTS, "",
								"", false, "", user, nc);
							node.getChildren().add(indexing_node);
							resmap.getNodes().add(indexing_node);
							indexing_node.setParent(node);
							resmap.getVirtualNodes().add(indexing_node);
						}
						else nc = (NodeConcepts)indexing_node.getExternalObject();
						nc.getConcepts().addNew(concept);
					}
					break;
				}
				count ++;
			}
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap concept-node links#" + count);
		}
	}
}

class RatingLoadingThread extends LoadingThread
{
	public RatingLoadingThread(SQLManager _sqlm, ResourceMap _resmap)
	{
		super(_sqlm, _resmap);
	}
	
	public void run()
	{
		String qry = "";
		ResultSet rs = null;
		PreparedStatement statement = null;
		Connection conn = null;
		int count = 0;
		
		try
		{
			for(int i=0; i<prereqs.size(); i++)
				prereqs.get(i).join();
			
			conn = sqlm.getConnection();
			qry = "SELECT * FROM ent_node_rating;";
			statement = conn.prepareStatement(qry);
			rs = statement.executeQuery();
			while(rs.next())
			{
				// read data
				int node_id = rs.getInt("NodeID");
				int user_id = rs.getInt("UserID");
				int rating_val = rs.getInt("Rating");
				int i_anonymous = rs.getInt("Anonymous");
				String comment = rs.getString("Comment");

				iNode node = resmap.getNodes().findById(node_id);
				User user = resmap.getUsers().findById(user_id);
				boolean anonymous = (i_anonymous==0)?false:true;
				
				Rating rating = new Rating((float)rating_val, anonymous, 
					comment, user, node);
				
				// add to rating collection
				resmap.getRatings().add(rating);
				// add to the user
				user.getRatings().add(rating);
				// add to the node
				node.setPersonalRating(rating);
				// add to all user groups
				for(int i=0; i<user.getSuperordinates().size(); i++)
				{
					User group = user.getSuperordinates().get(i);
					group.getRatings().add(rating);
				}
				count ++;
			}
			
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			SQLManager.recycleObjects(conn, null, null);
			conn = null;
		}
		catch(Exception e){ e.printStackTrace(System.out); }
		finally
		{
			System.out.println("~~~ [CoPE] ResourceMap.loadMap ratings#" + count);
		}
	}
}/**/