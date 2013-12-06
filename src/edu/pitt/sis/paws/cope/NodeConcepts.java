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
import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.*;
import edu.pitt.sis.paws.core.utils.SQLManager;

/** Class represents a cluster of indexing concepts (do not confuse with 
 * assigning ontologies to made such indexing accessible) assigned to a node by
 * a certain user. */
public class NodeConcepts extends Resource 
{
	// CONSTANTS
	protected static String URI_PREFIX = "NodeConcepts";
	
	ItemVector<iConcept> concepts;
	
	/**Main constructor
	* @param _id - id of the node being indexed, NOTE this is not a 
	* 	<em>parent</em> node, this is the node to which parent node is a child
	* @param _title - usually "concepts"
	* @param _user - user that added the concepts */
	public NodeConcepts(int _id, String _title, String _uri, User _user)
	{
		super(_id, _title, _uri, _user);
		concepts = new ItemVector<iConcept>();
	}
	
	/** Cloner */
	public NodeConcepts clone()
	{
		NodeConcepts copy = null;
		try
		{ 
			copy = new NodeConcepts(this.getId(), new String(this.getTitle()), new String(this.getURI()), null); 
		}
		catch (Exception e) { e.printStackTrace(System.out); }
		return copy;
	}
	
	/** Getter for concepts property */
	public ItemVector<iConcept> getConcepts() { return concepts; }
	
	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException 
	{
		if(concepts.size()==0)
			out.println("<div>No concepts added</div>");
		for(int i=0; i<concepts.size(); i++)
			out.println("<div class='pt_concept'>" + concepts.get(i).getTitle() + "</div>");
	}
		
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		out.println("<script language='javascript'>");
		out.println("<!--");
		out.println("	function flip_dir_icon(node)");
		out.println("	{");
		out.println("");
		out.println("		node.childNodes[0].style.display = (node.childNodes[0].style.display == 'none') ? 'inline' : 'none';");
		out.println("		node.childNodes[1].style.display = (node.childNodes[1].style.display == 'none') ? 'inline' : 'none';");
		out.println("		node.parentNode.nextSibling.style.display = (node.parentNode.nextSibling.style.display == 'none') ? 'block' : 'none';");
		out.println("	}");
		out.println("-->");
		out.println("</script>");
	
		out.println("</head>");
		out.println("<body>");
	}

	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		HttpSession session = request.getSession();
//		int user_id = ((Integer)session.getAttribute(AppDaemon.SESSION_USER_ID)).intValue();
		iNode current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
//		ResourceMap res_map = (ResourceMap) session.getAttribute(AppDaemon.SESSION_RES_MAP);

		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/doEdit' target='_top'>");

		// find all the ontologies
		ItemVector<iConcept> all_ontologies = current_node.getAllOntologies(null);
		// find the indexing for the user
//		out.println("<div style='padding:3px;'>There are " + all_ontologies.size() + " ontologies available</div>");			
		out.println("<input name='" + iNode.NODE_FRMFIELD_ID + "' type='hidden' value='" + current_node.getId() + "'>");
		out.println("<div style='padding:0px 5px 0px 5px'>");
		for(int i=0; i < all_ontologies.size(); i++)
			all_ontologies.get(i).outputTreeConcept(out, request, this.concepts, 0, true, "");
		out.println("</div>");
		
		out.println("<p>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit() { document.edit.submit(); }");
		out.println("</script>");
//		String cancel_to_url2 = " href='" + cancel_to_url + "'";
		out.println("<a class='pt_main_edit_button_cancel' target='_top' href='" + cancel_to_url + "'>Cancel</a>");
		out.println("</p>");
		out.println("</form>");

//		out.println("		</tr>");
//		out.println("	</td>");
//		out.println("</table>");
//		if(this.getValues().size() == 0 )
//		{
//			out.println("<div style='padding:3px;'>There are no concepts added previously.</div>");
//		}
//		else
//			this.getValues().get(value_id).showEdit(out, request, cancel_to_url_new);
	}

	public void showAdd(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException { ; }

	public void saveToDB(Connection conn, HttpServletRequest request, iNode node, int changes)
		throws Exception
	{
		// if not stored in DB - exit
		if(!stored_in_db) return;

		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();

		// delete previously added concepts -- from DB & add new concepts -- to DB

		String qry = "DELETE FROM rel_node_concept WHERE" +
				" NodeID=" + this.getId() + " AND RelationType=" +
				iConcept.CONCEPT_NODE_REL_CONCEPT + " AND UserID=" + user_id + ";";

		Statement stmt = conn.createStatement();
		stmt.executeUpdate(qry);
		stmt.close();
		
		for(int i=0; i<this.getConcepts().size(); i++)
		{
			qry = "INSERT INTO rel_node_concept (NodeID, ConceptID, RelationType, UserID)" +
					"VALUES (" + node.getParent().getId() + "," + 
					this.getConcepts().get(i).getId() + "," +
					iConcept.CONCEPT_NODE_REL_CONCEPT + "," + user_id + ");";
//System.out.println(qry);			
			Statement stmt1 = conn.createStatement();
			stmt1.executeUpdate(qry);
			stmt1.close();
		}
	}
	
	public int updateObject(HttpServletRequest request) throws Exception
	{
		int changes = iNode.NODE_CHANGE_NONE;

		// Getting session, resource map and a user
		HttpSession session = request.getSession();
		ResourceMap resmap = (ResourceMap) session.
			getAttribute(ClientDaemon.SESSION_RES_MAP);
//		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		// Getting the parameters
		Enumeration names = request.getParameterNames();
		ItemVector<iConcept> concepts = resmap.getConceptsByParameterEnumeration(names);
		
//System.out.println("NodeConcepts.saveToDB concepts submited # " + concepts.size());
		// delete previously added concepts -- logically
		String concept_id_list = "";
		for(int i=0;i<this.getConcepts().size();i++)
			concept_id_list += (((concept_id_list.length()>0)?",":"") +
				this.getConcepts().get(i).getId());
		this.getConcepts().clear();
		this.getConcepts().addAll(concepts);

		resmap.setPendingNode(null);
		
		return changes;
	}

	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{
		return 0;
	}

	/** Method determines whether the rating of the item should be shown before item itself or after
	 * @return true if ration sjould be showm before, false otherwise
	 * @since 1.5
	 */
	public boolean isRatingShownBefore() { return false /* after */; }
	
}
