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
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.*;

/** External Object is an entity that a Node may emulate, for example a Paper
 * a Summary. This object is attached to Node object and has it's own
 * authorship and view and edit methods
 */
public abstract class Resource extends Item2 implements iResource
{
	// CONSTANTS
	protected static String URI_PREFIX = "activity";
	// Rating labels
//	private static final String RESOURCE_RATING_LABELS = 
//		"[\"<span style='color:red;'>Highly Negative</span>','<span style='color:orange;'>Negative</span>','Neutral','<span style='color:olive;'>Positive</span>','<span style='color:green;'>Highly Positive</span>\"]";
//	private static final String[] RESOURCE_RATING_LABELS_ARRAY = {
//		"<span style='color:red;'>Highly Negative</span>",
//		"<span style='color:orange;'>Negative</span>",
//		"Neutral",
//		"<span style='color:olive;'>Positive</span>",
//		"<span style='color:green;'>Highly Positive</span>"};

//	private static final String RESOURCE_RATING_LABELS = 
//		"['<span style=\"color:red;\">Highly Negative</span>','<span style=\"color:orange;\">Negative</span>','Neutral','<span style=\"color:olive;\">Positive</span>','<span style=\"color:green;\">Highly Positive</span>']";
//	
//	private static final String[] RESOURCE_RATING_LABELS_ARRAY = {
//		"<span style=\"color:red;\">Highly Negative</span>",
//		"<span style=\"color:orange;\">Negative</span>",
//		"Neutral",
//		"<span style=\"color:olive;\">Positive</span>",
//		"<span style=\"color:green;\">Highly Positive</span>"};

	/** A collection of the Nodes that the object is attached to*/
	protected ItemVector<iNode> owners;
	/** Object's creator */
	protected User creator;

	/**
	 * Flag defines whether node is attached to an onject stored outside KT's database
	 */
	protected boolean stored_in_db;
	
	/** Constructor for cloning - creator will be set to null
	* @param _id - object Id
	* @param _title - object Title
	*/
	public Resource(int _id, String _title, String _uri)
	{
		super(_id, _title, _uri);

		if(this.getURI() == null || this.getURI().equals("") )
			setURI(URI_PREFIX + this.getId());
//		else
//			System.out.println("Resource nexternal URI " + this);
		
		owners = new ItemVector<iNode>();
		creator = null;
		
		stored_in_db = NodeUntyped.IS_STORED_IN_DB;
	}
	
	/**Main constructor for ExternalNodeObject
	 * @param _id - object Id
	 * @param _title - object Title
	 * @param _user - creator
	 */
	public Resource(int _id, String _title, String _uri, User _user)
	{
		super(_id, _title, _uri);

		if(this.getURI() == null || this.getURI().equals("") )
			setURI(URI_PREFIX + this.getId());
//		else
//			System.out.println("Resource nexternal URI " + this);

		owners = new ItemVector<iNode>();
		creator = _user;
		stored_in_db = NodeUntyped.IS_STORED_IN_DB;
	}

	public void setId(int _id)
	{
		super.setId(_id);
		setURI(URI_PREFIX + this.getId());
	}
	
	/** Getter for the owners' collection
	* @return the owners' collection */
	public ItemVector<iNode> getOwners() { return owners; }
	
	/** Method checks whether the user specified by a user_is is the author
	 * of this external object, true is returned if yes, false - if not
	 * @param _user_id - user id to be checked */
	public boolean isCreatedBy(int _user_id)
	{
//if(this.creator == null) System.out.println("Node.isAuthoredBy creator is null");
		if( (this.creator != null) && (this.creator.getId() == _user_id) )
			return true;
		else
			return false;
	}
	
	/** Method checks whether the specified user is the author
	 * of this external object, true is returned if yes, false - if not
	 * @param _user - user to be checked */
	public boolean isCreatedBy(User _user)
	{
		if( (this.creator != null) && (_user != null) &&
			(this.creator.getId() == _user.getId()) )
			return true;
		else
			return false;
	}
	
	/** Returns the creator of the external object
	* @return the creator of the external object */
	public User getCreator() { return creator; }

	public String getCreatorAdderNames()
	{
		return "";
	}
	
	/** Sets the creator of the external object
	* @param _user - the creator of the external object */
	public void setCreator(User _user) {creator = _user; }
	
	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		out.println("</head>");
		out.println("<body>");
	}
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		out.println("</head>");
		out.println("<body>");
	}
		
	/** Method determines whether the rating of the item should be shown before item itself or after
	 * @return true if ration sjould be showm before, false otherwise
	 * @since 1.5
	 */
	public boolean isRatingShownBefore() { return true /* before */; }

	/** Method returns 5 rating labels for the item that might vary from item type to item type.
	 * @return 5 rating labels for 5 stars
	 * @since 1.5
	 */
	public String getRatingLabels()
	{
		String RESOURCE_RATING_LABELS = 
			"['<span style=\"color:red;\">Highly Negative</span>','<span style=\"color:orange;\">Negative</span>','Neutral','<span style=\"color:olive;\">Positive</span>','<span style=\"color:green;\">Highly Positive</span>']";
		return RESOURCE_RATING_LABELS;
	}

	public String[] getRatingLabelsArray()
	{
		String[] RESOURCE_RATING_LABELS_ARRAY = {
			"<span style=\"color:red;\">Highly Negative</span>",
			"<span style=\"color:orange;\">Negative</span>",
			"Neutral",
			"<span style=\"color:olive;\">Positive</span>",
			"<span style=\"color:green;\">Highly Positive</span>"};
		return RESOURCE_RATING_LABELS_ARRAY;
	}

	public static String addIdentityToURL(String url, HttpServletRequest request)
	{
		int user_id;
		int group_id;
		
		String result = "";
		
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap) session.
				getAttribute(ClientDaemon.SESSION_RES_MAP);
		user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		
		User user_ = res_map.users.findById(user_id);
		User group_ = res_map.users.findById(group_id);
		String sess_ = session.getId().substring(session.getId().length()-5);
		
		result =  url+((url.indexOf("?")!=-1)?"&":"?") + "usr=" + user_.getLogin() + "&grp=" +
			group_.getLogin() + "&sid=" + sess_;// + "&ums=" + ClientDaemon.CONTEXT_UMS;
		
		// replace macro-parameters
//System.out.println("Before replace url="+result);
//		String patternStr = "__usr__";
//		String replacementStr = user_.getLogin();
//		Pattern pattern = Pattern.compile(patternStr);
//		Matcher matcher = pattern.matcher(result);
//        String output = matcher.replaceAll(replacementStr);
//        
////		result.replaceAll("||usr||", user_.getLogin()); // user identity
//System.out.println("After replace url="+output);
//System.out.println("After replace url="+result.replaceAll("__usr__", user_.getLogin()));
		result = result.replaceAll("__usr__", user_.getLogin()); // user identity
		return result;
	}

	public boolean isStoredInDB() { return stored_in_db; }

	public void setStoredInDB(boolean _is_stored) { stored_in_db = _is_stored; }
}	
