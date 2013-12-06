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

public class NodeConceptsOld extends NodeProperty 
{
	public NodeConceptsOld(iNode _node)
	{
		super(_node);
	}
	
	public NodeConceptsOld clone(iNode _parent_node, User _user) 
	{
		NodeConceptsOld copy = null;
		try
		{
			copy = new NodeConceptsOld(_parent_node); 
			for(int i=0; i<values.size();i++)
			{
				copy.getValues().add( values.get(i).clone(_user) );
			}
		}
		catch (Exception e) { System.err.println(e.toString()); }
		return copy;
	}
	
	public void addNewValue(iConcept _concept, User _user)
	{
//		User _existing_user = null;
		NodeConceptsValue ncv = null;
		for(int i=0; i<values.size(); i++)
			if(values.get(i).getUser() != null &&
				values.get(i).getUser().getId() == _user.getId() )
			{
//				_existing_user = values.get(i).getUser();
				ncv = (NodeConceptsValue)values.get(i);
			}
			
//		if( (_existing_user == null) && (ncv == null) )
//		{
//			_existing_user = _user;
//			ncv = new NodeConceptsValue(_user, this);
//			ncv.getConcepts().add(_concept);
//			this.values.add(ncv);
//		}
		if( (ncv == null) )
		{
//			if(_existing_user == null)
//				_existing_user = _user;
			ncv = new NodeConceptsValue(_user, this);
			this.values.add(ncv);
		}
		ncv.getConcepts().add(_concept);

	}

	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException{ ; }
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException{ ; }
		
	public void showView(JspWriter out, HttpServletRequest request,
		boolean show_ratings) throws IOException 
	{
		HttpSession session = request.getSession();
//		Integer user_id_I = ((Integer)session.getAttribute(AppDaemon.SESSION_USER_ID));
		ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
		iNode current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		Integer group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		
		String property_id_s = "";
		int property_id_i = -1;
		property_id_s = request.getParameter(NodeProperty.REQUEST_NODE_PROPERTY_ID);
		if(property_id_s != null) property_id_i = Integer.parseInt(property_id_s);
		

		iNode c_node = null;
		if(current_node != null)
			c_node = res_map.getNodes().findById(current_node.getId());
		
		
		String suff_en = "_enable";
		String suff_dis = "_disable";
		String next_suff = "";
		String prev_suff = "";
		String next_href = "";
		String prev_href = "";
		boolean can_edit = false;
		
		// Find out whether this user has a value submitted
		int this_users_value_idx = -1;
//		NodeConceptsValue this_users_value = null;
		
		for(int i=0; i<values.size(); i++)
			if( (((NodeConceptsValue)values.get(i)).getUser() != null) && 
				(((NodeConceptsValue)values.get(i)).getUser().getId() == user_id))
			{
				this_users_value_idx = i;
//				this_users_value = (NodeConceptsValue)values.get(i);
			}

		if(property_id_i == -1)
		{// no id was specified
			if(this_users_value_idx != -1)
				property_id_i = this_users_value_idx;
			else
				property_id_i = 0;
		}
		
		String url_base = request.getContextPath() + "/content/jspBottom";
		
		if(	(res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_EDIT, user_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_CONCEPTS)
			||
			res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_EDIT, group_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_CONCEPTS))
			//&& (values.size() > 0) && (this_users_value != null)
		)
			can_edit = true;

		next_href = "";
		prev_href = "";
		if( values.size() == 0)
		{// no comments submitted
			next_suff = suff_dis;
			prev_suff = suff_dis;
		}// - end - no comments submitted
		else
		{// there are comments _enable
			if(property_id_i < (values.size()-1))
			{
				next_suff = suff_en;
				next_href = " href='" + url_base + "?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_CONCEPTS +
					"&" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_VIEW  + 
					"&" + REQUEST_NODE_PROPERTY_ID + "=" + (property_id_i+1) + "'";
			}
			else 
				next_suff = suff_dis;
			if(property_id_i > 0 )
			{
				prev_suff = suff_en;
				prev_href = " href='" + url_base + "?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_CONCEPTS +
					"&" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_VIEW  + 
					"&" + REQUEST_NODE_PROPERTY_ID + "=" + (property_id_i-1) + "'";
			}
			else 
				prev_suff = suff_dis;
		}// -end - there are comments

		//----------
		String edit_href = ((can_edit)?" href='" + url_base + "?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_CONCEPTS +
			"&" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_EDIT  + 
			"&" + REQUEST_NODE_PROPERTY_ID + "=" + property_id_i + "'":"");
		out.println("");


		out.println("<table width='100%' border='0' cellpadding='0' cellspacing='0'>");
		out.println("	<tr width='100%'>");
		out.println("		<td>");

		out.println("<table border='0' class='pt_bottom_menu'>");
		out.println("	<tr>");
		out.println("		<td width='100%'>&nbsp;</td>");
		out.println("		<td><a" + edit_href + "><img border='0' title='Edit set of concepts' alt='[Edit]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/edit" + ((can_edit)?suff_en:suff_dis) + ".gif' /></a></td>");
		out.println("		<td><img border='0' alt='  ' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH+ "/spacer16x8.gif' /></a></td>");
		out.println("		<td><a" + prev_href + "><img border='0' title='Previous set of concepts' alt='[<]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/left2" + prev_suff + ".gif' /></a></td>");
		out.println("		<td><a" + next_href + "><img border='0'  title='Next set of concepts' alt='[>]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/right2" + next_suff + ".gif' /></a></td>");
		out.println("	</tr>");
		out.println("</table>");
		
		if( values.size() == 0)
		{// no comments submitted
			out.println("<div style='padding:3px;'>There are no concepts to view.</div>");
		}// - end - no comments submitted
		else
		{// there are comments
			this.getValues().get(property_id_i).showView(out, request,
				show_ratings);
		}// -end - there are comments

		out.println("		</td>");
		out.println("	</tr>");
		out.println("</table>");

	}
		
	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
//		ResourceMap res_map = (ResourceMap) session.getAttribute(AppDaemon.SESSION_RES_MAP);

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
	
	
//		if(this.getValues().size() == 0 )
//		{
//			showAdd(out, request, cancel_to_url);
//			return;
//		}
		
//		int value_id = Integer.parseInt((String)request.getParameter(NodeProperty.REQUEST_NODE_PROPERTY_ID));

		out.println("<table width='100%' border='0' cellpadding='0' cellspacing='0'>");
		out.println("	<tr width='100%'>");
		out.println("		<td>");

		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/jspBottom?" + NodeProperty.REQUEST_NODE_PROPERTY_TYPE + "=" + NodeProperty.REQUEST_NODE_PROPERTY_TYPE_CONCEPTS + "&" + NodeProperty.REQUEST_NODE_PROPERTY_MODE + "=" + NodeProperty.REQUEST_NODE_PROPERTY_MODE_SUBMIT +"'>");

		out.println("<table border='0' class='pt_bottom_menu'>");
		out.println("	<tr>");
		out.println("		<td width='100%'>&nbsp;</td>");
		out.println("		<td><a><img border='0' title='Edit set of concepts' alt='[Edit]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/edit_disable.gif' /></a></td>");
		out.println("		<td><img border='0' alt='  ' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH+ "/spacer16x8.gif' /></a></td>");
		out.println("		<td><a><img border='0' title='Previous set of concepts' alt='[<]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/left2_disable.gif' /></a></td>");
		out.println("		<td><a><img border='0'  title='Next set of concepts' alt='[>]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/right2_disable.gif' /></a></td>");
		out.println("	</tr>");
		out.println("</table>");
//		String cancel_to_url_new = request.getContextPath() + "/content/jspBottom?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_CONCEPTS +
//			"&"+ REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_VIEW  + 
//			"&" + REQUEST_NODE_PROPERTY_ID + "=" + (value_id) + "'";
		
		ItemVector<iConcept> indexed_concepts = new ItemVector<iConcept>();
		// find all the ontologies
		ItemVector<iConcept> all_ontologies = this.node.getAllOntologies(null);
		// find the indexing for the user
		NodeConceptsValue ncv = (NodeConceptsValue)this.getUserValue(user_id);
		if( ncv!= null )
			indexed_concepts = ncv.getConcepts();
//		out.println("<div style='padding:3px;'>There are " + all_ontologies.size() + " ontologies available</div>");			
		out.println("<div style='padding:0px 5px 0px 5px'>");
		for(int i=0; i < all_ontologies.size(); i++)
			all_ontologies.get(i).outputTreeConcept(out, request, indexed_concepts, 0, true, "");
		out.println("</div>");
		
		out.println("<p>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit() { document.edit.submit(); }");
		out.println("</script>");
		String cancel_to_url2 = " href='" + cancel_to_url + "'";
		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</p>");
		out.println("</form>");

		out.println("		</tr>");
		out.println("	</td>");
		out.println("</table>");

//		if(this.getValues().size() == 0 )
//		{
//			out.println("<div style='padding:3px;'>There are no concepts added previously.</div>");
//		}
//		else
//			this.getValues().get(value_id).showEdit(out, request, cancel_to_url_new);
	}

	public void showAdd(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException { ; }

	public void saveToDB(Connection conn, HttpServletRequest request)
		throws Exception
	{
		// Getting session, resource map and a user
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap) session.
			getAttribute(ClientDaemon.SESSION_RES_MAP);
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		// Getting the parameters
		Enumeration names = request.getParameterNames();
		ItemVector<iConcept> concepts = res_map.getConceptsByParameterEnumeration(names);
System.out.println("NodeConcepts.saveToDB concepts submited # " + concepts.size());
		// Find the Concepts value for the user
		NodeConceptsValue ncv = null;
		for(int i=0; i<this.values.size(); i++)
		{
			if(this.values.get(i).getUser() != null && 
				this.values.get(i).getUser().getId() == user_id)
				ncv = (NodeConceptsValue)this.values.get(i);		
		}
		// if there was no concepts before...
		if(ncv == null)
		{
			User user = res_map.getUsers().findById(user_id);
			ncv = new NodeConceptsValue(user, this);
			this.values.add(ncv);
		}
		// delete previously added concepts -- logically
		String concept_id_list = "";
		for(int i=0;i<ncv.getConcepts().size();i++)
			concept_id_list += (((concept_id_list.length()>0)?",":"") +
				ncv.getConcepts().get(i).getId());
		ncv.getConcepts().clear();
		// delete previously added concepts -- from DB & add new concepts -- to DB
//		ResultSet rs = null;
		String qry = "DELETE FROM rel_node_concept WHERE" +
			" NodeID=" + this.node.getId() + " AND RelationType=" +
			iConcept.CONCEPT_NODE_REL_CONCEPT + " AND UserID=" + user_id + ";";
//		try
	//		{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(qry);
			stmt.close();
			for(int i=0; i<concepts.size(); i++)
			{
				qry = "INSERT INTO rel_node_concept (NodeID, ConceptID, RelationType, UserID)" +
					"VALUES (" + this.node.getId() + "," + 
					concepts.get(i).getId() + "," +
					iConcept.CONCEPT_NODE_REL_CONCEPT + "," + user_id + ")";
				Statement stmt1 = conn.createStatement();
				stmt1.executeUpdate(qry);
				stmt1.close();
			}
//		}
//		catch (Exception e) { e.printStackTrace(System.err); }
//		finally { AppDaemon.freeConnection(conn); }
		// add new concepts -- logically
		ncv.getConcepts().addAll(concepts);
		
// TODO
//System.out.println("NodeConcepts.saveToDB concepts existed # " + values.size());
//		for(int i=0; i<values.size(); i++)
//System.out.println("	NodeConcepts.saveToDB existing concept: " + (iConcept)values.get(i));			
//		values.clear();
//System.out.println("NodeConcepts.saveToDB concepts now: " + values.size());
//		for(int i=0; i<concepts.size(); i++)
//			values.add((iConcept)concepts.get(i));
//System.out.println("	NodeConcepts.saveToDB new concepts # " + values.size());			
//		for(int i=0; i<values.size(); i++)
//System.out.println("	NodeConcepts.saveToDB new concept: " + (iConcept)values.get(i));			

		
		// Get the valus of all the fields
/*		String frm_property_id_s = request.getParameter(NodeProperty.FORM_NODE_PROPERTY_ID);
System.out.println("NodeConcepts.saveToDB FORM_NODE_PROPERTY_ID=" + frm_property_id_s);
		String frm_property_comment_text = request.getParameter(NodeProperty.FORM_NODE_PROPERTY_COMMENT_TEXT);
		int property_id = Integer.parseInt(frm_property_id_s);

		String property_idx_s = request.getParameter(NodeProperty.REQUEST_NODE_PROPERTY_ID);
		int property_idx_i = Integer.parseInt(property_idx_s);
		String comment = frm_property_comment_text.replace("'", "&rsquo;");

		// save into object
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap) session.getAttribute(AppDaemon.SESSION_RES_MAP);
		
		this.getValues().get(property_idx_i).setStringValue(comment);
		session.setAttribute(AppDaemon.SESSION_RES_MAP,res_map);
		
		// and DB
		String qry = "UPDATE ent_comment SET CommentText='" + comment + "'," +
			"DateModified=NOW() " +	"WHERE CommentID=" + property_id + ";";
		String big_qry = "";
		big_qry = qry.replaceAll("\\r*\\n*", "");
System.out.println("NodeConcepts.SaveToDB big_qry=" + big_qry);		
		AppDaemon.executeUpdate(conn, big_qry);
		/**/
	}

}