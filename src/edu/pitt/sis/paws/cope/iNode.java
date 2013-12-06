/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/** Interface in intended to wrap all of the entities that can be used as a node
 * in a portal tree: folders, untyped nodes, and nodes with special types.
 * @author Michael V. Yudelson
 */
 
package edu.pitt.sis.paws.cope;

import java.util.*;
import java.io.*;
import java.sql.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.*;

public interface iNode extends //iHierarchicalItem<iNode>, 
	iHTMLHierarchicalItem<iNode>, iDBStored, iHTMLRepresentable, iAuthored
{
	// CONSTANTS
	// 	String values
	public static final String NODE_TYPE_S_ALL = "Macro: All (all of the nodes)"; 
	public static final String NODE_TYPE_S_NONE = "Macro: None (none of the nodes)"; 
	public static final String NODE_TYPE_S_AUTHORED = "Macro: Authored (nodes that were authored by a user)"; 
	public static final String NODE_TYPE_S_FOLDER = "Folder";
	public static final String NODE_TYPE_S_UNTYPDOC = "Untyped document";
	public static final String NODE_TYPE_S_PAPER = "Paper";
	public static final String NODE_TYPE_S_SUMMARY = "Summary";
	public static final String NODE_TYPE_S_CONCEPTS = "Concepts";
	public static final String NODE_TYPE_S_MYPROFILE = "My Profile";
	public static final String NODE_TYPE_S_TOPIC_FOLDER = "Topic folder";
	public static final String NODE_TYPE_S_QUIZ = "Quiz";
	public static final String NODE_TYPE_S_COPE_SEARCH = "CoPE Search";
	public static final String NODE_TYPE_S_BIN = "Bin";
	public static final String NODE_TYPE_S_DISSECTION = "Dissection";
	public static final String NODE_TYPE_S_WADEIN = "Expression WADEIn";
	public static final String NODE_TYPE_S_CODEEXAMPLE = "Code Example";
	public static final String NODE_TYPE_S_KARELROBOT = "Karel Robot";
	public static final String NODE_TYPE_S_SYS_QUIZGUIDE = "QuizGUIDE";
	public static final String NODE_TYPE_S_SYS_NAVEX = "NavEx";
	public static final String NODE_TYPE_S_SYS_WADEIN = "WADEIn II";
	public static final String NODE_TYPE_S_COURSE = "Course";
	public static final String NODE_TYPE_S_LINK = "Link";
	public static final String NODE_TYPE_S_LINK_POPUP = "Link (pop-up)";
	// 	Integer values
	public static final int NODE_TYPE_I_ALL = 1; 
	public static final int NODE_TYPE_I_NONE = 2; 
	public static final int NODE_TYPE_I_AUTHORED = 3; 
	public static final int NODE_TYPE_I_FOLDER = 4;
	public static final int NODE_TYPE_I_UNTYPDOC = 5;
	public static final int NODE_TYPE_I_PAPER = 6;
	public static final int NODE_TYPE_I_SUMMARY = 7;
	public static final int NODE_TYPE_I_CONCEPTS = 8;
	public static final int NODE_TYPE_I_MYPROFILE = 9;
	public static final int NODE_TYPE_I_TOPIC_FOLDER = 10;
	public static final int NODE_TYPE_I_QUIZ = 11;
	public static final int NODE_TYPE_I_COPE_SEARCH = 12;
	public static final int NODE_TYPE_I_BIN = 13;
	public static final int NODE_TYPE_I_DISSECTION = 14;
	public static final int NODE_TYPE_I_WADEIN = 15;
	public static final int NODE_TYPE_I_CODEEXAMPLE = 16;
	public static final int NODE_TYPE_I_KARELROBOT = 17;
	public static final int NODE_TYPE_I_SYS_QUIZGUIDE = 18;
	public static final int NODE_TYPE_I_SYS_NAVEX = 19;
	public static final int NODE_TYPE_I_SYS_WADEIN = 20;
	public static final int NODE_TYPE_I_COURSE = 21;
	public static final int NODE_TYPE_I_LINK = 22;
	public static final int NODE_TYPE_I_LINK_POPUP = 23;
	public static final int NODE_TYPE_I_ADD1 = 24;
	public static final int NODE_TYPE_I_ADD2 = 25;
	public static final int NODE_TYPE_I_ADD3 = 26;
	public static final int NODE_TYPE_I_ADD4 = 27;
	public static final int NODE_TYPE_I_ADD5 = 28;
	public static final int NODE_TYPE_I_ADD6 = 29;
	public static final int NODE_TYPE_I_ADD7 = 30;
	public static final int NODE_TYPE_I_ADD8 = 31;
	public static final int NODE_TYPE_I_ADD9 = 32;
	
	public static final String[] NODE_TYPE_ICONS_SMALL = {
		"", // none
		"", // NODE_TYPE_I_NONE = 1; 
		"", // NODE_TYPE_I_ALL = 2; 
		"", // NODE_TYPE_I_AUTHORED = 3; 
		"folder_small.gif",				// NODE_TYPE_I_FOLDER = 4;
		"doc_small.gif",				// NODE_TYPE_I_UNTYPDOC = 5;
		"cope_paper_small.gif",			// NODE_TYPE_I_PAPER = 6;
		"cope_paper_summary_small.gif",	// NODE_TYPE_I_SUMMARY = 7;
		"concepts_index_small.gif",		// NODE_TYPE_I_CONCEPTS = 8;
		"myprofile_small.gif",			// NODE_TYPE_I_MYPROFILE = 9;
		"folder_topic_small.gif",		// NODE_TYPE_I_TOPIC_FOLDER = 10;
		"quiz_small.gif",				// NODE_TYPE_I_QUIZ = 11;
		"doc_small.gif",				// NODE_TYPE_I_COPE_SEARCH = 12;
		"bin_small.gif",				// NODE_TYPE_I_BIN = 13;
		"dissection_small.gif",			// NODE_TYPE_I_DISSECTION = 14;
		"wadein_small.gif",				// NODE_TYPE_I_WADEIN = 15;
		"code_example_small.gif",		// NODE_TYPE_I_CODEEXAMPLE = 16;
		"karel_small.gif",				// NODE_TYPE_I_KARELROBOT = 17;
		"quiz_small.gif",				// NODE_TYPE_I_SYS_QUIZGUIDE = 18;
		"dissection_small.gif",			// NODE_TYPE_I_SYS_NAVEX = 19;
		"wadein_small.gif",				// NODE_TYPE_I_SYS_WADEIN = 20;
		"folder_course_small.gif",		// NODE_TYPE_I_COURSE = 21;
		"link_small.gif",				// ODE_TYPE_I_LINK = 22;
		"link_small.gif"				// NODE_TYPE_I_LINK_POPUP = 23;
	};

	public static final String[] NODE_TYPE_ICONS_LARGE = {
		"", // none
		"", // NODE_TYPE_I_NONE = 1; 
		"", // NODE_TYPE_I_ALL = 2; 
		"", // NODE_TYPE_I_AUTHORED = 3; 
		"folder_large.gif",             // NODE_TYPE_I_FOLDER = 4;
		"doc_large.gif",                // NODE_TYPE_I_UNTYPDOC = 5;
		"cope_paper_large.gif",         // NODE_TYPE_I_PAPER = 6;
		"cope_paper_summary_large.gif", // NODE_TYPE_I_SUMMARY = 7;
		"concepts_index_large.gif",     // NODE_TYPE_I_CONCEPTS = 8;
		"myprofile_large.gif",          // NODE_TYPE_I_MYPROFILE = 9;
		"folder_topic_large.gif",       // NODE_TYPE_I_TOPIC_FOLDER = 10;
		"quiz_large.gif",               // NODE_TYPE_I_QUIZ = 11;
		"doc_large.gif", // NODE_TYPE_I_COPE_SEARCH = 12;
		"bin_large.gif",                // NODE_TYPE_I_BIN = 13;
		"dissection_large.gif",         // NODE_TYPE_I_DISSECTION = 14;
		"wadein_large.gif",             // NODE_TYPE_I_WADEIN = 15;
		"code_example_large.gif",       // NODE_TYPE_I_CODEEXAMPLE = 16;
		"karel_large.gif",              // NODE_TYPE_I_KARELROBOT = 17;
		"quiz_large.gif",               // NODE_TYPE_I_SYS_QUIZGUIDE = 18;
		"dissection_large.gif",         // NODE_TYPE_I_SYS_NAVEX = 19;
		"wadein_large.gif",             // NODE_TYPE_I_SYS_WADEIN = 20;
		"folder_course_large.gif",		// NODE_TYPE_I_COURSE = 21;
		"link_large.gif",				// ODE_TYPE_I_LINK = 22;
		"link_large.gif"				// NODE_TYPE_I_LINK_POPUP = 23;
	};

	// Working arrays
	// 	String arrays
	public static final String[] NODE_TYPES_S_ALL = { NODE_TYPE_S_ALL,
		NODE_TYPE_S_NONE, NODE_TYPE_S_AUTHORED, NODE_TYPE_S_FOLDER,
		NODE_TYPE_S_UNTYPDOC, NODE_TYPE_S_PAPER, NODE_TYPE_S_SUMMARY, 
		NODE_TYPE_S_CONCEPTS, NODE_TYPE_S_TOPIC_FOLDER,
		NODE_TYPE_S_QUIZ, NODE_TYPE_S_COPE_SEARCH,
		NODE_TYPE_S_BIN, NODE_TYPE_S_DISSECTION, NODE_TYPE_S_WADEIN,
		NODE_TYPE_S_CODEEXAMPLE, NODE_TYPE_S_KARELROBOT,
		NODE_TYPE_S_SYS_QUIZGUIDE, NODE_TYPE_S_SYS_NAVEX,
		NODE_TYPE_S_SYS_WADEIN, NODE_TYPE_S_COURSE, NODE_TYPE_S_LINK,
		NODE_TYPE_S_LINK_POPUP};

	
	public static final String[] NODE_TYPES_S_DEFINITIVE = {
		NODE_TYPE_S_FOLDER, NODE_TYPE_S_UNTYPDOC, NODE_TYPE_S_PAPER,
		NODE_TYPE_S_SUMMARY, NODE_TYPE_S_CONCEPTS, NODE_TYPE_S_TOPIC_FOLDER,
		NODE_TYPE_S_QUIZ, NODE_TYPE_S_COPE_SEARCH,
		NODE_TYPE_S_BIN, NODE_TYPE_S_DISSECTION, NODE_TYPE_S_WADEIN,
		NODE_TYPE_S_CODEEXAMPLE, NODE_TYPE_S_KARELROBOT,
		NODE_TYPE_S_SYS_QUIZGUIDE, NODE_TYPE_S_SYS_NAVEX,
		NODE_TYPE_S_SYS_WADEIN, NODE_TYPE_S_COURSE, NODE_TYPE_S_LINK,
		NODE_TYPE_S_LINK_POPUP};
	public static final String[] NODE_TYPES_S_ACTIVITIES = {
		NODE_TYPE_S_QUIZ, NODE_TYPE_S_DISSECTION, NODE_TYPE_S_WADEIN,
		NODE_TYPE_S_CODEEXAMPLE, NODE_TYPE_S_KARELROBOT,
		NODE_TYPE_S_SYS_QUIZGUIDE, NODE_TYPE_S_SYS_NAVEX,
		NODE_TYPE_S_SYS_WADEIN, NODE_TYPE_S_COURSE, NODE_TYPE_S_LINK,
		NODE_TYPE_S_LINK_POPUP};
	// 	Integer arrays
	public static final int[] NODE_TYPES_I_ALL = { NODE_TYPE_I_NONE,
		NODE_TYPE_I_ALL, NODE_TYPE_I_AUTHORED, NODE_TYPE_I_FOLDER,
		NODE_TYPE_I_UNTYPDOC, NODE_TYPE_I_PAPER, NODE_TYPE_I_SUMMARY, 
		NODE_TYPE_I_CONCEPTS, NODE_TYPE_I_TOPIC_FOLDER,
		NODE_TYPE_I_QUIZ, NODE_TYPE_I_COPE_SEARCH,
		NODE_TYPE_I_BIN, NODE_TYPE_I_DISSECTION, NODE_TYPE_I_WADEIN,
		NODE_TYPE_I_CODEEXAMPLE, NODE_TYPE_I_KARELROBOT,
		NODE_TYPE_I_SYS_QUIZGUIDE, NODE_TYPE_I_SYS_NAVEX,
		NODE_TYPE_I_SYS_WADEIN, NODE_TYPE_I_COURSE, NODE_TYPE_I_LINK,
		NODE_TYPE_I_LINK_POPUP};
	public static final int[] NODE_TYPES_I_DEFINITIVE = {
		NODE_TYPE_I_FOLDER, NODE_TYPE_I_UNTYPDOC, NODE_TYPE_I_PAPER,
		NODE_TYPE_I_SUMMARY, NODE_TYPE_I_CONCEPTS, NODE_TYPE_I_TOPIC_FOLDER,
		NODE_TYPE_I_QUIZ, NODE_TYPE_I_COPE_SEARCH,
		NODE_TYPE_I_BIN, NODE_TYPE_I_DISSECTION, NODE_TYPE_I_WADEIN,
		NODE_TYPE_I_CODEEXAMPLE, NODE_TYPE_I_KARELROBOT,
		NODE_TYPE_I_SYS_QUIZGUIDE, NODE_TYPE_I_SYS_NAVEX,
		NODE_TYPE_I_SYS_WADEIN, NODE_TYPE_I_COURSE, NODE_TYPE_I_LINK,
		NODE_TYPE_I_LINK_POPUP};
	public static final int[] NODE_TYPES_I_ACTIVITIES = {
		NODE_TYPE_I_QUIZ, NODE_TYPE_I_DISSECTION, NODE_TYPE_I_WADEIN,
		NODE_TYPE_I_CODEEXAMPLE, NODE_TYPE_I_KARELROBOT,
		NODE_TYPE_I_SYS_QUIZGUIDE, NODE_TYPE_I_SYS_NAVEX,
		NODE_TYPE_I_SYS_WADEIN, NODE_TYPE_I_COURSE, NODE_TYPE_I_LINK,
		NODE_TYPE_I_LINK_POPUP};

	// Form fields (most common)
	public static final String NODE_FRMFIELD_ID = "fld_id";
	public static final String NODE_FRMFIELD_NODETYPE = "fld_node_type";
	public static final String NODE_FRMFIELD_URL = "fld_url";
	public static final String NODE_FRMFIELD_TITLE = "fld_title";

	// Parameters & their values
	public static final String NODE_PARAM_EXTID = "extid";
	public static final String NODE_PARAM_WEIGHT = "weight";
	public static final String NODE_PARAM_ORDER = "order";
	public static final String NODE_PARAM_VAL_YES = "yes";
	public static final String NODE_PARAM_VAL_NO = "no";
	
	// Changes in the node
	public static final int NODE_CHANGE_NONE = 0;
	public static final int NODE_CHANGE_TITLE = 1;
	public static final int NODE_CHANGE_DESCRIPTION = 2;
	public static final int NODE_CHANGE_PROPERTIES = 4;
	public static final int NODE_CHANGE_URL = 8;

	public static final int NODE_CHANGE_RATING_ADD = 16;
	public static final int NODE_CHANGE_RATING_EDIT = 32;
	public static final int NODE_CHANGE_CHILDREN_WEIGHT_ORDR = 64;
	
	public static final int NODE_CHANGE_YEAR = 128;
	public static final int NODE_CHANGE_BIBLIO = 256;
	public static final int NODE_CHANGE_AUTHORS = 512;
	
	public static final int NODE_CHANGE_SUMMARY = 1024;
	
	// METHODS
	public Vector<Right> getRights();
	
	public ItemVector<iConcept> copyToResourceMap(ResourceMap resmap, 
		User new_user, ItemVector<iConcept> copy_concepts,
		boolean can_see_authors);

	public int getNodeType();
	public void setNodeType(int _node_type);
	
	public User getUser();
	public void setUser(User _user);

	public String getDescription();
	public void setDescription(String _descr);

	public boolean getFolderFlag();
	public void setFolderFlag(boolean _folder_flag);

	public String getURL();
	public void setURL(String _url);
	
	public iResource getExternalObject();
	public void setExternalObject(iResource _xtrnl_object);

	public iNode clone(User _user, boolean _set_xtrnal_obj);
	
	public void outputTreeNode(JspWriter out, HttpServletRequest req,
		iNode current_node, int level, int show_mode, User user, 
		boolean track_opens) throws IOException;
		
	public void showAdd(JspWriter out, HttpServletRequest request, 
		String cancel_to_url, ResourceMap res_map) throws IOException;

	public void showCopy(JspWriter out, HttpServletRequest request, 
		String cancel_to_url, ResourceMap res_map) throws IOException;

	public void showDelete(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException;

	public boolean isAllowedWhatWho(int _right_type, int _user_id, 
		boolean ini_authorship);
//	public boolean isAllowedWhatWhoFrom(int _right_type, int _user_id,
//		int _parent_node_type);
	public boolean isAllowedWhatWhoFromTo(int _right_type, int _user_id,
		int _parent_node_type, int _child_node_type,
		boolean ini_authorship);

	public boolean isAllowedWhatWhoFromTo_DownInhibitory(int _right_type,
		int _user_id, int _parent_node_type, int _child_node_type, 
		boolean ini_authorship, Vector<Right> _globally_def_rights,
		Vector<Right> _globally_acc_rights);

	public boolean isAllowedWhatWhoFromToQuant(int _right_type, int _user_id,
		int _parent_node_type, int _child_node_type, boolean ini_authorship,
		int _quant);
	
	public NodeProperty getComment();
	
	public ItemVector<iConcept> getOntologies();
	public NodeConceptsOld getConcepts();
	public ItemVector<iConcept> getAllOntologies(ItemVector<iConcept> _ontologies);
	
	public iNode findChildOfTypeByUser(int _node_type, int _user_id);

	public float getProfileMatch(User _user);
	
	public boolean canSeeAuthor(User user, User group, ResourceMap resmap);

	/** Method recursively traverses the node tree and adds all possible 
	 * parent-child node type pairs
	 * @param map - map containing all the found node type pairs
	 * @since 1.5
	 */
	public void reportParentChildNodeTypePairs(HashMap<Integer, ItemVector<Item>> map);

	/** Method creates a separate copy of the node as a parent of the other. 
	 * 	External objects are not duplicated but referenced
	 * @param parent - parent of the new node
	 * @param resmap - resource map
	 * @param conn - connection to the database
	 * @since 1.5
	 */
	public void createACopyRecursive(iNode parent, ResourceMap resmap, 
		int user_id, User user, Connection conn);

	/** Method returns the rating represented as a group of values - the members of the group
	 * @return - the rating represented as a group of values - the members of the group
	 * @since 1.5
	 */
	public Rating getGroupRating();

	/** Method returns user's personal rating
	 * @return user's personal rating
	 * @since 1.5
	 */
	public Rating getPersonalRating();

	/** Method sets the rating represented as a group of values - the members of the group
	 * @since 1.5
	 */
	public void setGroupRating(Rating _rating);

	/** Method sets user's personal rating
	 * @since 1.5
	 */
	public void setPersonalRating(Rating _rating);

}
