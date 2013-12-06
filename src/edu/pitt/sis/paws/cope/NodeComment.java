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

import javax.servlet.jsp.*;
import javax.servlet.http.*;
import edu.pitt.sis.paws.core.utils.SQLManager;

//import edu.pitt.sis.paws.core.*;

public class NodeComment extends NodeProperty 
{
	public NodeComment(iNode _node)
	{
		super(_node);
	}
	
	public NodeComment clone(iNode _parent_node, User _user) 
	{
		NodeComment copy = null;
		try
		{
			copy = new NodeComment(_parent_node); 
			for(int i=0; i<values.size();i++)
			{
//System.out.println("NodeComment.clone there exist Comment Values to clone");		
				copy.getValues().add( values.get(i).clone(_user) );
			}
//System.out.println("NodeComment.clone copy.getValues().size()" + copy.getValues().size());		
		}
		catch (Exception e) { System.err.println(e.toString()); }
		return copy;
	}

	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException { ; }
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException{ ; }
		
	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException 
	{
		HttpSession session = request.getSession();
		Integer user_id_I = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID));
		ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
		iNode current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
		Integer user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		Integer group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		int session_user_id = user_id_I.intValue();
		
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
		boolean can_add = false;
		
		int this_users_value_idx = -1;
		NodeCommentValue this_users_value = null;
		for(int i=0; i<values.size(); i++)
			if( (((NodeCommentValue)values.get(i)).getUser() != null) && 
				(((NodeCommentValue)values.get(i)).getUser().getId() == session_user_id))
			{
				this_users_value_idx = i;
				this_users_value = (NodeCommentValue)values.get(i);
			}

		if(property_id_i == -1)
		{// no id was specified
			if(this_users_value_idx != -1)
				property_id_i = this_users_value_idx;
			else
				property_id_i = 0;
		}
		
		String url_base = request.getContextPath() + "/content/jspBottom";
		
//		if(	(res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_ADD, user_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_COMMENT)
//			||
//			res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_ADD, group_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_COMMENT))
//		)
		
		if(res_map.isAllowedWhatWho2ForFromToQuant(Right.RIGHT_TYPE_ADD,
			user_id, group_id, c_node, c_node.getNodeType(),iNode.NODE_TYPE_I_SUMMARY))
			can_add = true;

		if(	(res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_EDIT, user_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_SUMMARY)
			||
			res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_EDIT, group_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_SUMMARY))
			&& (values.size() > 0) && (this_users_value != null)
		)
		can_edit = true;

//System.out.println("NodeComment.showView  can_edit=" + can_edit + " values.size()=" + values.size() + " (this_users_value != null)=" + (this_users_value != null));

		
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
				next_href = " href='" + url_base + "?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_COMMENT +
					"&" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_VIEW  + 
					"&" + REQUEST_NODE_PROPERTY_ID + "=" + (property_id_i+1) + "'";
			}
			else 
				next_suff = suff_dis;
			if(property_id_i > 0 )
			{
				prev_suff = suff_en;
				prev_href = " href='" + url_base + "?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_COMMENT +
					"&" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_VIEW  + 
					"&" + REQUEST_NODE_PROPERTY_ID + "=" + (property_id_i-1) + "'";
			}
			else 
				prev_suff = suff_dis;
		}// -end - there are comments

		//----------
		String add_href = ((can_add)?" href='" + url_base + "?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_COMMENT +
					"&" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_ADD + "'":"");
		String edit_href = ((can_edit)?" href='" + url_base + "?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_COMMENT +
			"&" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_EDIT  + 
			"&" + REQUEST_NODE_PROPERTY_ID + "=" + property_id_i + "'":"");
		out.println("");


		out.println("<table width='100%' border='0' cellpadding='0' cellspacing='0'>");
		out.println("	<tr width='100%'>");
		out.println("		<td>");

		out.println("<table border='0' class='pt_bottom_menu'>");
		out.println("	<tr>");
		out.println("		<td width='100%'>&nbsp;</td>");
		out.println("		<td><a" + edit_href + "><img border='0' title='Edit comment' alt='[Edit]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/edit" + ((can_edit)?suff_en:suff_dis) + ".gif' /></a></td>");
		out.println("		<td><img border='0' alt='  ' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH+ "/spacer16x8.gif' /></a></td>");
		out.println("		<td><a" + add_href + "><img border='0' title='Add comment' alt='[Add]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/add" + ((can_add)?suff_en:suff_dis) + ".gif' /></a></td>");
		out.println("		<td><img border='0' alt='  ' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH+ "/spacer16x8.gif' /></a></td>");
		out.println("		<td><a" + prev_href + "><img border='0' title='Previous comment' alt='[<]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/left2" + prev_suff + ".gif' /></a></td>");
		out.println("		<td><a" + next_href + "><img border='0'  title='Next comment' alt='[>]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/right2" + next_suff + ".gif' /></a></td>");
		out.println("	</tr>");
		out.println("</table>");
		
		if( values.size() == 0)
		{// no comments submitted
			out.println("<div style='padding:3px;'>There are no comments to view.</div>");
		}// - end - no comments submitted
		else
		{// there are comments
			this.getValues().get(property_id_i).showView(out, request, show_ratings);
		}// -end - there are comments

		out.println("		</td>");
		out.println("	</tr>");
		out.println("</table>");

	}
		
	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		int value_id = Integer.parseInt((String)request.getParameter(NodeProperty.REQUEST_NODE_PROPERTY_ID));

		out.println("<table width='100%' border='0' cellpadding='0' cellspacing='0'>");
		out.println("	<tr width='100%'>");
		out.println("		<td>");

		out.println("<table border='0' class='pt_bottom_menu'>");
		out.println("	<tr>");
		out.println("		<td width='100%'>&nbsp;</td>");
		out.println("		<td><a><img border='0' title='Edit comment' alt='[Edit]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/edit_disable.gif' /></a></td>");
		out.println("		<td><img border='0' alt='  ' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH+ "/spacer16x8.gif' /></a></td>");
		out.println("		<td><a><img border='0' title='Add comment' alt='[Add]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/add_disable.gif' /></a></td>");
		out.println("		<td><img border='0' alt='  ' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH+ "/spacer16x8.gif' /></a></td>");
		out.println("		<td><a><img border='0' title='Previous comment' alt='[<]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/left2_disable.gif' /></a></td>");
		out.println("		<td><a><img border='0'  title='Next comment' alt='[>]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/right2_disable.gif' /></a></td>");
		out.println("	</tr>");
		out.println("</table>");
		String cancel_to_url_new = request.getContextPath() + "/content/jspBottom?" + REQUEST_NODE_PROPERTY_TYPE + "=" + REQUEST_NODE_PROPERTY_TYPE_COMMENT +
					"&" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_VIEW  + 
					"&" + REQUEST_NODE_PROPERTY_ID + "=" + (value_id) + "'";
		this.getValues().get(value_id).showEdit(out, request, cancel_to_url_new);


		out.println("		</td>");
		out.println("	</tr>");
		out.println("</table>");
	}

	public void showAdd(JspWriter out, HttpServletRequest request, 
		String cancel_to_url, boolean show_ratings) throws IOException
	{
		HttpSession session = request.getSession();
		Integer user_id_I = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID));
		ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
		iNode current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
		Integer user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		Integer group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		int session_user_id = user_id_I.intValue();
		
		String property_id_s = "";
		int property_id_i = -1;
		property_id_s = request.getParameter(NodeProperty.REQUEST_NODE_PROPERTY_ID);
		if(property_id_s != null) property_id_i = Integer.parseInt(property_id_s);

		iNode c_node = null;
		if(current_node != null)
			c_node = res_map.getNodes().findById(current_node.getId());
		
		String suff_en = "_enable";
		String suff_dis = "_disable";
		boolean can_edit = false;
		boolean can_add = false;
		
		NodeCommentValue this_users_value = null;
		for(int i=0; i<values.size(); i++)
			if( (((NodeCommentValue)values.get(i)).getUser() != null) && 
				(((NodeCommentValue)values.get(i)).getUser().getId() == session_user_id))
				this_users_value = (NodeCommentValue)this.getValues().get(i);
		
		String url_base = request.getContextPath() + "/content/jspBottom";
		
		if(	(res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_ADD, user_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_SUMMARY)
			||
			res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_ADD, group_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_SUMMARY))
		)
			can_add = true;

		if(	(res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_EDIT, user_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_SUMMARY)
			||
			res_map.isAllowedWhatWhoForFromTo(Right.RIGHT_TYPE_EDIT, group_id, c_node, c_node.getNodeType(),Node.NODE_TYPE_I_SUMMARY))
			&& (values.size() > 0) && (this_users_value != null)
		)
			can_edit = true;
		
		String icon_suffix = "";
		if( values.size() == 0)
		{// no comments submitted
			icon_suffix = "_disable";
		}// - end - no comments submitted
		else
		{// there are comments
			icon_suffix = "_enable";
		}// -end - there are comments

		//----------
		String add_href = ((can_add)?" href='" + url_base + "?" + REQUEST_NODE_PROPERTY_MODE + "=" + REQUEST_NODE_PROPERTY_MODE_ADD + "'":"");
		out.println("");
		out.println("<table width='100%' border='0' cellpadding='0' cellspacing='0'>");
		out.println("	<tr width='100%'>");
		out.println("		<td>");

		out.println("<table border='0' class='pt_bottom_menu'>");
		out.println("	<tr>");
		out.println("		<td width='100%'>&nbsp;</td>");
		out.println("		<td><a href='#'><img border='0' title='Edit comment' alt='[Edit]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/edit" + ((can_edit)?suff_en:suff_dis) + ".gif' /></a></td>");
		out.println("		<td><img border='0' alt='  ' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH+ "/spacer16x8.gif' /></a></td>");
		out.println("		<td><a" + add_href + "><img border='0' title='Add comment' alt='[Add]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/add" + ((can_add)?suff_en:suff_dis) + ".gif' /></a></td>");
		out.println("		<td><img border='0' alt='  ' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH+ "/spacer16x8.gif' /></a></td>");
		out.println("		<td><a href='#'><img border='0' title='Previous comment' alt='[<]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/left2" + icon_suffix + ".gif' /></a></td>");
		out.println("		<td><a href='#'><img border='0'  title='Next comment' alt='[>]' src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/right2" + icon_suffix + ".gif' /></a></td>");
		out.println("	</tr>");
		out.println("</table>");
		
		if( values.size() == 0)
		{// no comments submitted
			out.println("<div style='padding:3px;'>There are no comments to view.</div>");
		}// - end - no comments submitted
		else
		{// there are comments
			if(property_id_i == -1)
			{// there's no specific value specified
				if(this_users_value != null)
				{// show current user's comment
					this_users_value.showView(out, request, show_ratings);
				}// -end - show current user's comment
				else
				{// show first comment
					this.getValues().get(0).showView(out, request, show_ratings);
				}// - end - show first comment
			}// - end - there's no specific value specified
			else
			{// specific value IS specified
				this.getValues().get(property_id_i).showView(out, request, show_ratings);
			}// - end - specific value IS specified
		}// -end - there are comments

		out.println("		</td>");
		out.println("	</tr>");
		out.println("</table>");
	}

	public void saveToDB(Connection conn, HttpServletRequest request)
		throws Exception
	{
		// Get the valus of all the fields
		String frm_property_id_s = request.getParameter(NodeProperty.FORM_NODE_PROPERTY_ID);
//System.out.println("NodeComment.saveToDB FORM_NODE_PROPERTY_ID=" + frm_property_id_s);
		String frm_property_comment_text = request.getParameter(NodeProperty.FORM_NODE_PROPERTY_COMMENT_TEXT);
		int property_id = Integer.parseInt(frm_property_id_s);

		String property_idx_s = request.getParameter(NodeProperty.REQUEST_NODE_PROPERTY_ID);
		int property_idx_i = Integer.parseInt(property_idx_s);
		String comment = frm_property_comment_text.replace("'", "&rsquo;");

		// save into object
		HttpSession session = request.getSession();
		ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
		
		this.getValues().get(property_idx_i).setStringValue(comment);
		session.setAttribute(ClientDaemon.SESSION_RES_MAP,res_map);
		
		// and DB
		String qry = "UPDATE ent_comment SET CommentText='" + comment + "'," +
			"DateModified=NOW() " +	"WHERE CommentID=" + property_id + ";";
		String big_qry = "";
		big_qry = qry.replaceAll("\\r*\\n*", "");
//System.out.println("NodeComment.SaveToDB big_qry=" + big_qry);		
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(big_qry);
		stmt.close();
		
	}

}