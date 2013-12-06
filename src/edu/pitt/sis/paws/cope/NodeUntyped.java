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
import java.sql.*;

import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.cbum.report.ReportAPI;

//import edu.pitt.sis.paws.core.*;

import java.util.regex.*;
import edu.pitt.sis.paws.core.utils.SQLManager;

/**
 * @author Michael V. Yudelson
 */
public class NodeUntyped extends Node// implements iNode
{
	// CONSTANTS
	// Form fields (specific)
	public static final String NODE_FRMFIELD_PROPERTIES = "fld_properties";
	public static final String NODE_FRMFIELD_DESCRIPTION = "fld_description";
	public static final String NODE_FRMFIELD_WEIGHTORDCHANGED = "fld_weight_order_changed";

	public static final String NODE_FRMFIELD_RATING_VALUE = "your_rating_js_Value";
	public static final String NODE_FRMFIELD_RATING_TEXT_VALUE = "your_rating_js_Text_Value";
	public static final String NODE_FRMFIELD_RATING_EDIT_FLAG = "your_rating_edit";
	public static final String NODE_FRMFIELD_RATING_ANONYMITY = "anonymity";
	public static final String NODE_FRMFIELD_RATING_COMMENT = "your_comment_js_text";


	/**
	 * Additional properties for the URL
	 */
	protected String properties;
	
	public static final boolean IS_STORED_IN_DB = true; 
	public static final boolean IS_NOT_STORED_IN_DB = false; 
	
	protected static ReportAPI reportApi = new ReportAPI(ClientDaemon.CONTEXT_UMS);
	
	// This constructor is for adding new
//	public NodeUntyped()
//	{
//		;
//	}

	// This constructor is for cloning only
	public NodeUntyped(int _id, String _title, int _node_type, String _descr,
		String _url, boolean _folder_flag, String _properties, iResource _xtrnl_object)
	{
		super(_id, _title, _node_type, _descr, _url, _folder_flag, _xtrnl_object);
		creator = null;
		rights = new Vector<Right>();
		properties = (_properties==null)?"":_properties;
		stored_in_db = IS_STORED_IN_DB;
	}
	
	public NodeUntyped(int _id, String _title, int _node_type, String _descr,
		String _url, boolean _folder_flag, String _properties, User _user, iResource _xtrnl_object)
	{
		super(_id, _title, _node_type, _descr, _url, _folder_flag, _xtrnl_object);
		creator = _user;
		rights = new Vector<Right>();
		properties = (_properties==null)?"":_properties;
		stored_in_db = IS_STORED_IN_DB;
}

	public NodeUntyped(int _id, String _title, int _node_type, String _descr,
		String _url, boolean _folder_flag, String _properties, User _user,
		iNode _parent, iResource _xtrnl_object)
	{
		super(_id, _title, _node_type, _descr, _url, _folder_flag, _xtrnl_object);
		creator = _user;
		rights = new Vector<Right>();
		parent = _parent;
		properties = (_properties==null)?"":_properties;;
		stored_in_db = IS_STORED_IN_DB;
	}
	
	public iNode clone(User _user, boolean _set_xtrnal_obj) 
	{
		NodeUntyped copy = null;
		try
		{
			iResource external_object = (_set_xtrnal_obj)?this.getExternalObject():null;
			// get node's user
//			User user = this.creator;
			copy = new NodeUntyped(this.getId(),new String(this.getTitle()),
				this.node_type, new String(this.description), 
				new String(this.url), this.folder_flag,
				new String(this.properties), external_object); 
			//add properties
			copy.setComment( this.comments.clone(copy, _user));
			// copy ontologies
//			for(int i=0; i<this.ontologies.size(); i++)
//			{
//				ItemVector<iConcept> onto_concepts = 
//					this.ontologies.get(i).copyConceptTree(null, null);
//				copy.getOntologies().add( onto_concepts.get(0) );
//			}
		}
		catch (Exception e) { System.err.println(e.toString()); }
		return copy;
	}

	public int compareTo(Object e)
	{
		return getTitle().compareTo(((NodeUntyped)e).getTitle());
	}

//	public void setRight(Right right) { rights.add(right); }
//	public void revokeRight(Right right) { rights.remove(right); }
	
	public String toString() { return "[ NodeUntyped title:'" + getTitle() + "' id:" + getId() + "]"; }
	
	public String getProperties() { return properties; }
	public void setProperties(String _properties) { properties = _properties; }

	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException
	{
//System.out.println("Node.showView before check to external... node_type=" + this.node_type + " xtrnl_object="+this.xtrnl_object);	
		String cancel_to = request.getContextPath() + "/content" + 
			"/Show?" + ClientDaemon.REQUEST_NODE_ID + "=" + this.getId();
		
		if( ((this.node_type != iNode.NODE_TYPE_I_FOLDER)&&(this.node_type != iNode.NODE_TYPE_I_NONE)&&(this.xtrnl_object != null)) )
		{
//System.out.println("Node.showView before call to external...");	
			xtrnl_object.showView(out, request, show_ratings);
//System.out.println("Node.showView after call to external...");
			if(show_ratings) showRatings(out, request, cancel_to);
			return;
		}
		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		ResourceMap resmap = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
//System.out.println("ResourceMap.displayFolderView activities " + res_map.getActivities().size());/// DEBUG
		User user = resmap.getUsers().findById(user_id);
		User group = resmap.getUsers().findById(group_id);
		
		boolean can_see_author = canSeeAuthor(user, group, resmap);
		String display_author = ((can_see_author)? "&nbsp<sup>[" + 
				this.getCreatorAdderNames() + "]</sup>": "");
//System.out.println("Node.showView can_see_author = " + can_see_author);	
		// up-dir icon
//		boolean no_way_up = false;
		
		if(this.folder_flag)
		{// if it's a folder - show sub-nodes (not for root)
			if(this.getNodeType() != iNode.NODE_TYPE_I_ALL)
			{
				// Show 'Description' caption
				out.println("<div class='pt_main_subheader'>" +
					 this.getTitle() + display_author + "</div>");
					 
				out.println("<p>" + this.getDescription() + "</p>");
			}
			
			ResourceMap.displayFolderView(this.getChildren(), out, request, show_ratings);
		}// -- end if it's a folder - show sub-nodes
		else
		{// otherwise show default document
			if( (this.getDescription() != null) && 
				(!this.getDescription().equals("")) )
			{// show a description if it exists
				// Show 'Description' caption
/*				out.println("<div style='font-size:0.9em; " + 
					"font-weight:bold; color:#000099; " +
					"margin:15px 0px 5px 0px; border:0px; " +
					"border-bottom:1px solid #999999; " +
					"font-family:Times, serif;'>" +
					"Current folder description</div>");*/
				out.println("<p>" + this.getDescription() + "</p>");	
			}
			else if( (this.getURL() != null) && 
				(!this.getURL().equals("")) )
			{
				boolean has_quest = this.getURL().indexOf("?") != -1;
				String sess_ = session.getId().substring(session.getId().length()-5);
				String url = this.getURL() + ((has_quest)?"&":"?") +
					"usr=" + user.getLogin() + "&grp=" + 
					group.getLogin() + "&sid=" + sess_;
				//Show the url and append user,group & session id
				out.println("<script>");
				out.println("	document.location = '" + url + "';");
				out.println("</script>");
				out.println("<body>");
				out.println("</body>");
				;
			}
			else
			// otherwise - default text
				out.println("<p>This document is empty.</p>");
			
		}// -- end otherwise show default document
		if(show_ratings) showRatings(out, request, cancel_to);
		
		// REPORT VISIT
		if(this.getId() > 0 )
		{
//System.out.println("[CoPE] NodeUntyped.showView reporting to UMS=" + reportApi.getUMS());			
			ResourceMap res_map = (ResourceMap) session.
					getAttribute(ClientDaemon.SESSION_RES_MAP);
			user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
			group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
			User user_ = res_map.users.findById(user_id);
			User group_ = res_map.users.findById(group_id);
			String sess_ = session.getId().substring(session.getId().length()-5);
	
			reportApi.report(ReportAPI.APPLICATION_KNOWLEDGETREE, "ktree" + this.getId(), sess_, -1, 
					group_.getLogin(), user_.getLogin(), "Portal");
		}
	}
	
	public void showRatings(JspWriter out, HttpServletRequest request,
		String cancel_to) throws IOException
	{
		float value = 0;
		String value_s = "0";
		String rating_icon;
		String rating_html = "";
		out.println("<p/>");
		
		// PERSONAL RATING
		// grab value
		if(this.getPersonalRating() != null)
		{
			value = this.getPersonalRating().getRatingValue();
			value_s = Integer.toString((int)value);
		}
		
		rating_icon = (value!=0)?"stars_own" + (int)value + ".gif":"stars_0.gif";
		
		// personal rating html
		rating_html += "<div id='your_rating' style='display:block;'>" +
			"<img src='" + request.getContextPath() + 
			ClientDaemon.CONTEXT_ASSETS_PATH + "/dir_empbull.gif'>&nbsp;" +
			"<span style='font-size:0.9em; font-weight:bold; color:#000099; font-family:Times, serif;'>My rating:" +
			"</span>&nbsp;<img src='" + request.getContextPath() +
			ClientDaemon.CONTEXT_ASSETS_PATH +	"/" + rating_icon + "' " + 
			"width='53' height='16'>&nbsp;" + 
			((value!=0)?value_s+"/5&nbsp;&nbsp;<img src='" + request.getContextPath() +
					ClientDaemon.CONTEXT_ASSETS_PATH + "/" +
				(this.getPersonalRating().getAnonymous()?
					"anonymous.gif'>": "signed.gif'>")
			:"N/A" ) + "&nbsp;&nbsp;<a href='#'><img border='0' src='" +
				request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/" +
			((value!=0)?"edit":"add") + "_enable.gif' title='" +
			((value!=0)?"Edit":"Add") + " your rating' alt='[" +
			((value!=0)?"Edit":"Add") + "]' width='16' height='16' " + 
			"onClick='document.getElementById(\"add_edit_rating\")." +
			"style.display=\"block\";document.getElementById" +
			"(\"your_rating\").style.display=\"none\";document." + 
			"getElementById(\"group_rating\").style.display=\"none\";'>" +
			"</a>&nbsp;&nbsp;" + ((value!=0)?"" + this.getPersonalRating().getComment():"") + "</div>";
		
		// personal comment add/edit form
		rating_html += "\n\n <form id='add_edit_rating' name='add_edit_rating' style='display:none;' method='post' action='" +
			request.getContextPath() + "/content/doEdit' target='_top'>";
//		rating_html += "\n<table width='100%' id='add_edit_rating_table' name='add_edit_rating_table'  border='0' cellspacing='0' cellpadding='0' style='display:block;'>";
		rating_html += "\n\t<script language='javascript' src='" +
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH +  "/CastleRater.js'> var Likert = new Array(); </script>";
		rating_html += "\n\t<script language='javascript'>";
		
		// grabbing the scale of ratings
		
		rating_html += "\n\t\tLikert=" + ((getExternalObject() !=null )?getExternalObject().getRatingLabels():this.getRatingLabels()) + ";";

		String[] rating_labels_array = ((getExternalObject() !=null )?getExternalObject().getRatingLabelsArray():this.getRatingLabelsArray());
		
		rating_html += "\n\t\tfunction tree_opener(node)";
		rating_html += "\n\t\t{";
		rating_html += "\n\t\t\tnodeDiv = node.parentNode;";
		rating_html += "\n\t\t\tnodeDivPlus = document.getElementById(nodeDiv.id + 'plus');";
		rating_html += "\n\t\t\tnodeDivMinus = document.getElementById(nodeDiv.id + 'minus');";
		rating_html += "\n\t\t\tnodeDivChildren = document.getElementById(nodeDiv.id + 'children');";
		rating_html += "\n\t\t";
		rating_html += "\n\t\t\tnodeDivPlus.style.display = (nodeDivPlus.style.display == 'none') ? 'inline' : 'none';";
		rating_html += "\n\t\t\tnodeDivMinus.style.display = (nodeDivMinus.style.display == 'none') ? 'inline' : 'none';";
		rating_html += "\n\t\t\tnodeDivChildren.style.display = (nodeDivMinus.style.display == 'inline') ? 'block' : 'none';";
		rating_html += "\n\t\t}";
		
		rating_html += "\n\t</script>";
		rating_html += "\n\t<input type='hidden' id='" + NODE_FRMFIELD_RATING_VALUE + "' name='" + NODE_FRMFIELD_RATING_VALUE + "' value='" + value_s + "' />";
		rating_html += "\n\t<input type='hidden' id='" + NODE_FRMFIELD_RATING_TEXT_VALUE + "' name='" + NODE_FRMFIELD_RATING_TEXT_VALUE + "' value='" + ((value!=0)?rating_labels_array[(int)value-1]:"") + "' />";
		rating_html += "\n\t<input type='hidden' id='" + NODE_FRMFIELD_RATING_EDIT_FLAG + "' name='" + NODE_FRMFIELD_RATING_EDIT_FLAG + "' value='" + ((value!=0)?"1":"0") + "' />";

		rating_html += "\n\t<input name='" + iNode.NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>";


		rating_html += "\n\t\t<span style='font-size:0.9em; font-weight:bold; color:#000099; font-family:Times, serif;'>" + 
			"My rating:</span>&nbsp;<a id='your_rating_js' ";
		rating_html += "\n\t\t\tonMouseOut=\"document.getElementById('latestHover').innerHTML=document.getElementById('" + NODE_FRMFIELD_RATING_TEXT_VALUE + "').value;\"";
		rating_html += "\n\t\t\tonMouseDown=\"document.getElementById('" + NODE_FRMFIELD_RATING_TEXT_VALUE + "').value=(Likert[your_rating_js.HoverValue-1]);\"";
		rating_html += "\n\t\t\tonMouseOver=\"document.getElementById('latestHover').innerHTML=(Likert[your_rating_js.HoverValue-1]);\"><script language='javascript'>var your_rating_js = CastleRater.CreateJSControl('your_rating_js', '" + 
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/star_on.gif', '" + 
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/star_off.gif', '" +
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/star_over.gif', 5, " +
			value_s + ");</script></a>" /*"</td>"*/;
		
		rating_html += "\n\t\t\t<a id='latestHover' >" + ((value!=0)?rating_labels_array[(int)value-1]:"") + "</a><br />"; // onClick='alert("Rating value = " + your_rating_js.Value);'
		rating_html += "\n\t\t\t<label><input type='radio' id='" + NODE_FRMFIELD_RATING_ANONYMITY + "' name='" + NODE_FRMFIELD_RATING_ANONYMITY + "' value='signed'" + ((value!=0 && !this.getPersonalRating().getAnonymous())?" checked":"") + "><img src='" + 
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/signed.gif'></label>&nbsp;&nbsp;" /*"<br />"*/;
		rating_html += /*"\t\t\t"+*/ "<label><input type='radio' id='" + NODE_FRMFIELD_RATING_ANONYMITY + "' name='" + NODE_FRMFIELD_RATING_ANONYMITY + "' value='anonymous'" + ((value!=0 && this.getPersonalRating().getAnonymous())?" checked":"") + "><img src='" + 
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/anonymous.gif'></label>";
		rating_html += "<br/><textarea name='" + NODE_FRMFIELD_RATING_COMMENT + "' cols='40' rows='5' id='" + NODE_FRMFIELD_RATING_COMMENT + "'>" +
			((value != 0)?this.getPersonalRating().getComment():"")  + "</textarea><br/><br/>"/*</td>"*/;
 		rating_html += "\n<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;";
		rating_html += "\n<script type='text/javascript'>";
		rating_html += "\nfunction mySubmit()";
		rating_html += "\n{";
		rating_html += "\n\tvar error_msg = '';";
		rating_html += "\n\tvar error = false";
		rating_html += "\n\tif( document.getElementById('" + NODE_FRMFIELD_RATING_VALUE + "').value == '0' )";
		rating_html += "\n\t{";
		rating_html += "\n\t\terror_msg += 'Please choose your rating value\\n';";
		rating_html += "\n\t\terror = true;";
		rating_html += "\n\t}";
		rating_html += "\n\tif( !((document.add_edit_rating." + NODE_FRMFIELD_RATING_ANONYMITY + "[0].checked) || (document.add_edit_rating." + NODE_FRMFIELD_RATING_ANONYMITY + "[1].checked)))";
		rating_html += "\n\t{";
		rating_html += "\n\t\terror_msg += 'Please choose your rating to be either Signed or Anonymous\\n';";
		rating_html += "\n\t\terror = true;";
		rating_html += "\n\t}";
	
		rating_html += "\n\tif(error) alert(error_msg );";
		rating_html += "\n\telse";
		rating_html += "\n\t\tdocument.add_edit_rating.submit();";
		rating_html += "\n}";
		rating_html += "\n</script>";
		rating_html += "\n<a class='pt_main_edit_button_cancel' href='" + 
			cancel_to + "' target='_top'>Cancel</a>";
		rating_html += "\n</p>";
		rating_html += "\n</form>";

		// print personal rating html				
		out.println(rating_html);

		// GROUP RATING
		// grab value
		value = 0;
		value_s = "0";
		
		if(this.getGroupRating() != null) 
			value = this.getGroupRating().getRatingValue();

		if(((float)Math.round((float)value*2))/2 == Math.ceil(((float)Math.round((float)value*2))/2))
		{
			rating_icon = (value!=0)?"stars_other" + (int)value + ".gif":"stars_0.gif";
			value_s = Integer.toString((int)value);
		}
		else
		{
			rating_icon = (value!=0)?"stars_other" + ((float)Math.round((float)value*2))/2 + ".gif":"stars_0.gif";
			value_s = Float.toString(((float)Math.round((float)value*2))/2);
		}
		
		// group rating html
		rating_html = "<div id='group_rating' style='display:block;'>" +
			((value!=0)?
			"<a onClick='tree_opener(this)'><img id='group_ratingminus' src='" +
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/dir_minus.gif' style='display:none;'><img id='group_ratingplus' src='" + 
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/dir_plus.gif'  style='display:inline;'></a>"  
			:"<img src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/dir_empbull.gif'>"
			) +
			"&nbsp;<span style='font-size:0.9em; font-weight:bold; color:#000099; font-family:Times, serif;'>Group rating:</span>&nbsp;<img src='" +
			request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/" + rating_icon + "' width='53' height='16'>&nbsp;" + ((value!=0)?value_s + "/5":"N/A");
		if(value != 0) // if there are group ratings
		{
			// deal with ability to see atuhors of ratings
			HttpSession session = request.getSession();
			ResourceMap res_map = (ResourceMap) session.
				getAttribute(ClientDaemon.SESSION_RES_MAP);
			int user_id = ((Integer)session.getAttribute(ClientDaemon.
				SESSION_USER_ID)).intValue();
			int group_id = ((Integer)session.getAttribute(ClientDaemon.
				SESSION_GROUP_ID)).intValue();
			User user = res_map.getUsers().findById(user_id);
			User group = res_map.getUsers().findById(group_id);
			
			boolean can_see_author = canSeeAuthor(user, group, res_map);
	 
 			rating_html += "\n\t<div id='group_ratingchildren' style='display:none;'>";

			for(int i=0; i<this.getGroupRating().getRatings().size(); i++)
			{
				value = 0;
				value_s = "0";
				value = this.getGroupRating().getRatings().get(i).getRatingValue();
				
				if(((float)Math.round((float)value*2))/2 == Math.ceil(((float)Math.round((float)value*2))/2))
				{
					rating_icon = (value!=0)?"stars_other" + (int)value + ".gif":"stars_0.gif";
					value_s = Integer.toString((int)value);
				}
				else
				{
					rating_icon = (value!=0)?"stars_other" + ((float)Math.round((float)value*2))/2 + ".gif":"stars_0.gif";
					value_s = Float.toString(((float)Math.round((float)value*2))/2);
				}

				rating_html += "\n\t\t<div style='display:block;'><img src='" +
					request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH +  "\\dir_empty.gif' width='18' height='16'>" 
					+ ((can_see_author && !this.getGroupRating().getRatings().get(i).getAnonymous())?"[" + this.getGroupRating().getRatings().get(i).getUser().getTitle() + "]&nbsp;&nbsp;":"") + "<img src='" + 
					request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH +  "/" + 
					rating_icon + "'>&nbsp;&nbsp;" + this.getGroupRating().getRatings().get(i).getComment() + "</div>";
			}

			rating_html += "\n\t</div>";
		}
		rating_html += "\n</div>";


		// print group rating html	
		out.println(rating_html + " </div>");

	}

	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		if( (((this.node_type == iNode.NODE_TYPE_I_FOLDER)||(this.node_type == iNode.NODE_TYPE_I_NONE))&&(this.xtrnl_object != null)) &&
			(!this.getFolderFlag()) && (this.getURL() != null) && 
			(!this.getURL().equals("")) )
		{
			out.println("<meta http-equiv='refresh' content='0;URL=" +
				this.getURL() + "'>");
		}
		out.println("</head>");
		out.println("<body>");
	}
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		if( ((this.node_type != iNode.NODE_TYPE_I_FOLDER)&&(this.node_type != iNode.NODE_TYPE_I_NONE)&&(this.xtrnl_object != null)) )
		{
			xtrnl_object.showEditHeader(out, request);
			return;			
		}

		StringBuffer browserDetector = HTMLUtilities.javaScriptBrowserDetector();
		
		out.println(browserDetector);
		
		out.println("<script type='text/javascript'>");
		out.println("   _editor_url = '" + request.getContextPath() + "/assets/htmlarea/';");
		out.println("   _editor_lang = 'en';");
		out.println("</script>");
		out.println("<script type='text/javascript' src='" + request.getContextPath() + "/assets/htmlarea/htmlarea.js'></script>");
		out.println("<script type='text/javascript' >");
		out.println("<!--");
		out.println("var editor = null;");
		out.println("function initEditor()");
		out.println("{");
		out.println("	editor = new HTMLArea('fld_description');");
		out.println("	editor.generate();");
		out.println("	return false;");
		out.println("}");
		out.println("function insertHTML()");
		out.println("{");
		out.println("	var html = prompt('Enter some HTML code here');");
		out.println("	if (html) { editor.insertHTML(html); }");
		out.println("}");
		out.println("function highlight()");
		out.println("{");
		out.println("	editor.surroundHTML('<span style=\"background-color: yellow\">', '</span>');");
		out.println("}");
		out.println("function flip(node, dir)");
		out.println("{");
		out.println("	actor = node.parentNode.parentNode;");
//		out.println("	alert('actor.id='+actor.id);");
		out.println("	swap = null;");
		out.println("	if(dir==-1) // swap up");
		out.println("	{");
		out.println("		swap = actor.previousSibling;");
		out.println("	}");
		out.println("	else if(dir==1) // swap down");
		out.println("	{");
		out.println("		swap = actor.nextSibling;");
		out.println("	}");
//		out.println("	alert('swap.id='+swap.id);");
		out.println("	swap_id = swap.id;");
		out.println("	if(swap_id!=null) // swap if somewhere to swap");
		out.println("	{");
		out.println("		i_actor_id = parseInt(actor.id.substring(3));");
		out.println("		i_swap_id = parseInt(swap.id.substring(3));");
		out.println("		actor_in = actor.childNodes[0];");
		out.println("		swap_in = swap.childNodes[0];");
//		out.println("		actor_in = actor.childNodes[0];");
//		out.println("		swap_in = swap.childNodes[0];");
		out.println("		");
		out.println("		actor_in_html = actor_in.innerHTML;");
		out.println("		actor_in.innerHTML = swap_in.innerHTML;");
		out.println("		swap_in.innerHTML = actor_in_html;");
		out.println("		");
		out.println("		actor_in.childNodes[0].value = i_actor_id;");
		out.println("		swap_in.childNodes[0].value = i_swap_id;");
		out.println("		document.getElementById('" + NODE_FRMFIELD_WEIGHTORDCHANGED + "').value = 1;");
		out.println("	}");
		out.println("}");
		out.println("-->");
		out.println("</script>");
		out.println("</head>");
		out.println("");
		out.println("<body onload='HTMLArea.init(); HTMLArea.onload = initEditor;'>");
	}

	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		if( ((this.node_type != iNode.NODE_TYPE_I_FOLDER)&&(this.node_type != iNode.NODE_TYPE_I_NONE)&&(this.xtrnl_object != null)) )
		{
			xtrnl_object.showEdit(out, request, cancel_to_url);
			return;			
		}
		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/doEdit' target='_top'>");
		out.println("<!-- ID field -->");
		out.println("<input name='" + iNode.NODE_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>");
		out.println("<!-- Title field -->");
		out.println("<div class='pt_main_subheader_editing_name'>Title</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The title of the document/folder'><input name='" + NODE_FRMFIELD_TITLE + "' type='text' value=\"" + this.getTitle() + "\" size='70' maxlength='200'></div>");
		out.println("<!-- URL field -->");
		out.println("<div class='pt_main_subheader_editing_name'>URL</div>");
		out.println("<div class='pt_main_subheader_editingue' title='The URL the document/folder is associated with'><input name='" + NODE_FRMFIELD_URL + "' type='text' value=\"" + this.url + "\" size='70' maxlength='255'></div>");
		out.println("<!-- Properties field -->");
		out.println("<div class='pt_main_subheader_editing_name'>Properties</div>");
		out.println("<div class='pt_main_subheader_editingue' title='Additional properties'><input name='" + NODE_FRMFIELD_PROPERTIES + "' type='text' value=\"" + this.properties + "\" size='70' maxlength='100'></div>");

/*		out.println("<!-- Node type field -->");
		out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Node type</div>");
		out.println("<div style='padding:0px 0px 10px 15px;' title='The type of the document/folder'>");
		out.println("	<select name='fld_node_type' size='1'>");
		// Display all definitive doc/folder styles mark the one specified
		for(int i=0; i<NODE_TYPES_S_DEFINITIVE.length; i++)
		{
			boolean selected = this.node_type == NODE_TYPES_I_DEFINITIVE[i];
			out.println("		<option value='" + 
				NODE_TYPES_I_DEFINITIVE[i] + "'" + 
				((selected)?" selected":"") + ">" + 
				NODE_TYPES_S_DEFINITIVE[i] + "</option>");
		}
		out.println("	</select>");
		out.println("</div>");*/
		out.println("<!-- Description field -->");
		out.println("<div class='pt_main_subheader_editing_name'>Description</div>");
		out.println("<div class='pt_main_subheader_editingue_textarea'>");
		out.println("<textarea name='fld_description' id='" + NODE_FRMFIELD_DESCRIPTION + "' style='width:100%;padding:0px' rows='30' cols='80' title='The description of the document/folder'>");
		out.println(this.description);
/*		out.println("&lt;p&gt;Here is some sample text: &lt;b&gt;bold&lt;/b&gt;, &lt;i&gt;italic&lt;/i&gt;, &lt;u&gt;underline&lt;/u&gt;. &lt;/p&gt;");
		out.println("&lt;p align=center&gt;Different fonts, sizes and colors (all in bold):&lt;/p&gt;");
		out.println("&lt;p&gt;&lt;b&gt;");
		out.println("&lt;font face='arial'           size='7' color='#000066'&gt;arial&lt;/font&gt;,");
		out.println("&lt;font face='courier new'     size='6' color='#006600'&gt;courier new&lt;/font&gt;,");
		out.println("&lt;font face='georgia'         size='5' color='#006666'&gt;georgia&lt;/font&gt;,");
		out.println("&lt;font face='tahoma'          size='4' color='#660000'&gt;tahoma&lt;/font&gt;,");
		out.println("&lt;font face='times new roman' size='3' color='#660066'&gt;times new roman&lt;/font&gt;,");
		out.println("&lt;font face='verdana'         size='2' color='#666600'&gt;verdana&lt;/font&gt;,");
		out.println("&lt;font face='tahoma'          size='1' color='#666666'&gt;tahoma&lt;/font&gt;");
		out.println("&lt;/b&gt;&lt;/p&gt;");
		out.println("&lt;p&gt;Click on &lt;a href='http://www.interactivetools.com/'&gt;this link&lt;/a&gt; and then on the link button to the details ... OR ... select some text and click link to create a &lt;b&gt;new&lt;/b&gt; link.&lt;/p&gt;");
*/
		out.println("</textarea>");
		out.println("</div>");
		
		
		// Pring children names, weights and ordering
		boolean is_weighted = true;
		boolean is_ordered = true;
		
		if( (this.getChildren().size()>0) && (is_weighted || is_ordered) )
		{
			out.println("<!-- Folder content -->");
			out.println("<div class='pt_main_subheader_editing_name'>Content</div>");// 
			out.println("<input name='" + NODE_FRMFIELD_WEIGHTORDCHANGED + "' type='hidden' id='" + NODE_FRMFIELD_WEIGHTORDCHANGED + "' value='0'>");
			out.print("<div>");
			for( int i=0; i<this.getChildren().size(); i++)
			{
//System.out.println("NodeUntyped.showEdit child:" + this.getChildren().get(i).getTitle() + " weight:" + this.getChildren().getWeight(i));			
				out.println("<div id='pos" + (i+1) + "'><div style='display:inline;'><input name='doc" + this.getChildren().get(i).getId() + "' value='" + (i+1) + "' type='hidden'>");
				out.println("	<a name='anch" + i + "'></a><a href='#anch" + i + "' title='Move Up' onClick='flip(this, -1);'>&nbsp;&uarr;&nbsp;Up</a>&nbsp;<a href='#anch" + i + "' title='Move Down' onClick='flip(this, 1);'>&nbsp;&darr;&nbsp;Down</a>&nbsp;Weight:&nbsp;");
				out.println("	<select name='weight" + (this.getChildren().get(i).getId()) + "' style='width:50px;' onChange='document.getElementById(\"" + NODE_FRMFIELD_WEIGHTORDCHANGED + "\").value = 1;'>");
				out.println("		<option value='0.1'" + ((this.getChildren().getWeight(i)==0.1)?" selected":"") + ">0.1</option>");
				out.println("		<option value='0.2'" + ((this.getChildren().getWeight(i)==0.2)?" selected":"") + ">0.2</option>");
				out.println("		<option value='0.3'" + ((this.getChildren().getWeight(i)==0.3)?" selected":"") + ">0.3</option>");
				out.println("		<option value='0.4'" + ((this.getChildren().getWeight(i)==0.4)?" selected":"") + ">0.4</option>");
				out.println("		<option value='0.5'" + ((this.getChildren().getWeight(i)==0.5)?" selected":"") + ">0.5</option>");
				out.println("		<option value='0.6'" + ((this.getChildren().getWeight(i)==0.6)?" selected":"") + ">0.6</option>");
				out.println("		<option value='0.7'" + ((this.getChildren().getWeight(i)==0.7)?" selected":"") + ">0.7</option>");
				out.println("		<option value='0.8'" + ((this.getChildren().getWeight(i)==0.8)?" selected":"") + ">0.8</option>");
				out.println("		<option value='0.9'" + ((this.getChildren().getWeight(i)==0.9)?" selected":"") + ">0.9</option>");
				out.println("		<option value='1.0'" + ((this.getChildren().getWeight(i)==1.0)?" selected":"") + ">1.0</option>");
				out.println("		<option value='2.0'" + ((this.getChildren().getWeight(i)==2.0)?" selected":"") + ">2.0</option>");
				out.println("		<option value='3.0'" + ((this.getChildren().getWeight(i)==3.0)?" selected":"") + ">3.0</option>");
				out.println("		<option value='4.0'" + ((this.getChildren().getWeight(i)==4.0)?" selected":"") + ">4.0</option>");
				out.println("		<option value='5.0'" + ((this.getChildren().getWeight(i)==5.0)?" selected":"") + ">5.0</option>");
				out.println("		<option value='6.0'" + ((this.getChildren().getWeight(i)==6.0)?" selected":"") + ">6.0</option>");
				out.println("		<option value='7.0'" + ((this.getChildren().getWeight(i)==7.0)?" selected":"") + ">7.0</option>");
				out.println("		<option value='8.0'" + ((this.getChildren().getWeight(i)==8.0)?" selected":"") + ">8.0</option>");
				out.println("		<option value='9.0'" + ((this.getChildren().getWeight(i)==9.0)?" selected":"") + ">9.0</option>");
				out.println("		<option value='10.0'" + ((this.getChildren().getWeight(i)==10.0)?" selected":"") + ">10.0</option>");
				out.println("	</select>");
				out.println("	&nbsp;&nbsp;" + this.getChildren().get(i).getTitle() + "</div>");
				out.print("</div>");
			}
			out.print("</div>");
		}
		
		out.print("<div>&nbsp;</div>");
		out.println("<div>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit()");
		out.println("{");
		out.println("	var error_msg = 'Fields:\\n';");
		out.println("	var error = false");
		out.println("	");
		out.println("	if( document.edit.fld_title.value.length == 0 )");
		out.println("	{");
		out.println("		errror_msg += '  * Title';");
		out.println("		error = true;");
		out.println("	}");
		out.println("	error_msg = '\\n should not contail an empty string';");
		out.println("	");
		out.println("	if(error) alert(error_msg );");
		out.println("	else");
		out.println("	{");
//		out.println("		alert(navigator.appName);");
		out.println("		if(BrowserDetect.browser!='Safari') document.edit.onsubmit();");
		out.println("		document.edit.submit();");
		out.println("	}");	
		out.println("}");
		out.println("</script>");
		
//**//**		
//		String cancel_to_url = " href='" + request.getContextPath() + "/content" + 
//			"/Show" +
//			((cancel_to != null)?
//				("?" + ClientDaemon.REQUEST_NODE_ID + "=" + cancel_to.getId()):""
//			) + "' " + "target='_top'";

		String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";
//System.out.println("NodeUn : cancel_to_url = " + cancel_to_url);
//System.out.println("NodeUn : cancel_to_url2 = " + cancel_to_url2);

		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</div>");
		out.println("</form>");
	}
	
	public void saveToDB(Connection conn, HttpServletRequest request, iNode node,
			int changes) throws Exception
	{
		// if not stored in DB - exit
		if(!stored_in_db) return;
		
		String qry = "";
		String qry_children = "";
		
		// Process Title
		if((changes & iNode.NODE_CHANGE_TITLE) > 0)
		{
			qry += ((qry.length() > 0)?" ,":"") + "Title='" +
					SQLManager.stringUnquote(this.getTitle()) + "'";
		}
		
		// Process URL
		if((changes & iNode.NODE_CHANGE_URL) > 0)
		{
			qry += ((qry.length() > 0)?" ,":"") + "URL='" +
					SQLManager.stringUnquote(this.url) + "'";
		}
		
		// Process Properties
		if((changes & iNode.NODE_CHANGE_PROPERTIES) > 0)
		{
			qry += ((qry.length() > 0)?" ,":"") + "URI='" +
					SQLManager.stringUnquote(this.properties ) + "'";
		}

		// Process Description
		if((changes & iNode.NODE_CHANGE_DESCRIPTION) > 0)
		{
			qry += ((qry.length() > 0)?", ":"") + "Description='" +
					SQLManager.stringUnquote(this.description) + "'";
		}
		
		// change db
		if(!qry.equals(""))
		{// Save changes into Node if any
			// Alter modification time/date
			qry += ((qry.length() > 0)?" ,":"") + "DateModified=NOW()";		
			String big_qry = "UPDATE ent_node SET " + qry + 
					" WHERE NodeID=" + this.getId() + ";";
			// Throw out line feeds
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQLManager.stringUnbreak(big_qry));
			stmt.close();

			
		}// -- end -- Save changes into Node if any
		
		// get user_id when rating is changed
		int user_id = 0;
		if((changes & (iNode.NODE_CHANGE_RATING_ADD | iNode.NODE_CHANGE_RATING_EDIT)) > 0)
		{
			HttpSession session = request.getSession();
//			ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
			user_id = ((Integer) session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		}
		
		// new rating added
		if((changes & iNode.NODE_CHANGE_RATING_ADD) > 0)
		{
			qry = "INSERT INTO ent_node_rating (NodeID, Rating, " + 
					"Anonymous, Comment, UserID, DateCreated, " + 
					"DateModified, DateAltered) VALUES (" + this.getId() + 
					", " + this.getPersonalRating().getRatingValue() + ", " + 
					this.getPersonalRating().getAnonymous() + 
					", '" + SQLManager.stringUnquote(this.getPersonalRating().getComment()) + 
					"', " + user_id + 
					", NOW(), NOW(), NOW());";
			
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(qry);
			stmt.close();

		}
		
		// rating changing
		if((changes & iNode.NODE_CHANGE_RATING_EDIT) > 0)
		{
			qry = "UPDATE ent_node_rating SET Rating=" + this.getPersonalRating().getRatingValue() +
			", Anonymous=" + this.getPersonalRating().getAnonymous() + ", Comment='" + 
			SQLManager.stringUnquote(this.getPersonalRating().getComment())+ "', DateModified=NOW() WHERE " +
			"NodeID=" + this.getId() + " AND UserID=" + user_id + ";";
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(qry);
			stmt.close();
		}
		
		// children order/weights changed
		if((changes & iNode.NODE_CHANGE_CHILDREN_WEIGHT_ORDR) > 0)
		{
			for(int i=0; i<this.getChildren().size(); i++)
			{
				qry_children = "UPDATE rel_node_node SET OrderRank = " + (i+1) + 
						", Weight="+ this.getChildren().getWeight(i) + " WHERE ParentNodeID = " + this.getId() + 
						" AND ChildNodeID = " + this.getChildren().get(i).getId() + ";";
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(qry_children);
				stmt.close();
			}
		}
		
		// save external objects if any
		if( this.xtrnl_object != null )
		{
			xtrnl_object.saveToDB(conn, request, this, changes);
			return;			
		}
		
	}// ed of -- saveToDB

	public int updateObject(HttpServletRequest request) throws Exception
	{
//String new_summary = request.getParameter(Summary.SUMMARY_FRMFIELD_SUMMARY);
//System.out.println("Node: new_summary='" + new_summary + "'");

		int changes = iNode.NODE_CHANGE_NONE;
		
		String new_title = request.getParameter(NodeUntyped.NODE_FRMFIELD_TITLE);
		String new_desc = SQLManager.stringUnnull(request.getParameter(NodeUntyped.NODE_FRMFIELD_DESCRIPTION));
		String new_properties = SQLManager.stringUnnull(request.getParameter(NodeUntyped.NODE_FRMFIELD_PROPERTIES));
		String new_url = SQLManager.stringUnnull(request.getParameter(NodeUntyped.NODE_FRMFIELD_URL));

		String frm_weight_order_changed = request.getParameter(NODE_FRMFIELD_WEIGHTORDCHANGED);
		
		String rate_textue = request.getParameter(NODE_FRMFIELD_RATING_TEXT_VALUE);
		
		// Title
		if( (new_title != null) && (!this.getTitle().equals(new_title)) )
		{
			this.setTitle(new_title);
			changes |= iNode.NODE_CHANGE_TITLE;
		}
		
		// Description
		if(!this.getDescription().equals(new_desc))
		{
			this.setDescription(new_desc);
			changes |= iNode.NODE_CHANGE_DESCRIPTION;
		}
		
		// Properties
		if(!this.getProperties().equals(new_properties))
		{
			this.setProperties(new_properties);
			changes |= iNode.NODE_CHANGE_PROPERTIES;
		}
		
		// URL
		if(!this.getURL().equals(new_url))
		{
			this.setURL(new_url);
			changes |= iNode.NODE_CHANGE_URL;
		}
		
		if( (frm_weight_order_changed != null) && (frm_weight_order_changed.equals("1")) )
		{// reweight, reorder children
			changes |= iNode.NODE_CHANGE_CHILDREN_WEIGHT_ORDR;
			
			Vector<Integer> indices = new Vector<Integer>();
			Vector<Integer> ids = new Vector<Integer>();//
			Enumeration enu = request.getParameterNames();
			for(;enu.hasMoreElements();)
			{
				String key = (String)enu.nextElement();
				Pattern p = Pattern.compile("doc[0-9]+");
				Matcher m = p.matcher("");
				m.reset(key);
				if(m.matches())
				{
					int item_id = Integer.parseInt( key.substring(3) );
					int item_idx = Integer.parseInt(request.getParameter(key));
					double weight = Double.parseDouble(request.getParameter("weight"+item_id));
//System.out.println("Reordering Id=" + item_id + " Idx=" + item_idx + " Weight=" + weight);
 					indices.add(item_idx-1);
					ids.add(item_id);//
					this.getChildren().getWeights().set((item_idx-1), weight);
				}		
			}
			this.getChildren().reorder(indices, ids);
		}// end of -- reweight, reorder children
		
		if(rate_textue != null)
		{// save rating
//System.out.println("NodeUntyped.saveToDB adding/editing rating");		
			int rateue = Integer.parseInt(request.getParameter(NODE_FRMFIELD_RATING_VALUE));
			int edit_flag = Integer.parseInt(request.getParameter(NODE_FRMFIELD_RATING_EDIT_FLAG));
			String anonymous_s = request.getParameter(NODE_FRMFIELD_RATING_ANONYMITY);
			String comment = request.getParameter(NODE_FRMFIELD_RATING_COMMENT);
			int anonymous_i = (anonymous_s.equalsIgnoreCase("anonymous"))?1:0;
			HttpSession session = request.getSession();
			ResourceMap res_map = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
			int user_id = ((Integer) session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
			User user = res_map.getUsers().findById(user_id);
			
			if(edit_flag == 0) // adding new rating
			{
				changes |= iNode.NODE_CHANGE_RATING_ADD;
				
				Rating new_rating = new Rating(rateue,
					(anonymous_i==1), comment, user, this);
				this.setPersonalRating(new_rating);
				res_map.getRatings().add(new_rating);
			}
			else if (edit_flag == 1) // editing existing rating
			{
				changes |= iNode.NODE_CHANGE_RATING_EDIT;
				
				Rating edited_rating = this.getPersonalRating();
				edited_rating.setRatingValue(rateue);
				edited_rating.setAnonymous((anonymous_i==1));
				edited_rating.setComment(comment);
			}
		}// end of -- save rating

		if( ((this.node_type != iNode.NODE_TYPE_I_FOLDER)&&
				(this.node_type != iNode.NODE_TYPE_I_ALL)&&
				(this.xtrnl_object != null)) &&
				(rate_textue == null) )
		{
			changes |= xtrnl_object.updateObject(request);
		}
		
		return changes;
	}
	
	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{
		int node_id = 0;
		HttpSession session = request.getSession();
		ResourceMap resmap = ((ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP));

		if(this.isStoredInDB())
		{// if object is stored in db
			int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
			
			// save the node
			ResultSet rs = null;
			String qry = "INSERT INTO ent_node (Title, Description, URI, URL, UserID, DateCreated, " +
				"DateModified, DateAltered, ItemTypeID, FolderFlag) VALUES ('" + 
				this.getTitle() + "', '" + 
				SQLManager.stringUnnull(this.getDescription()) + "', '" + 
				SQLManager.stringUnnull(this.getProperties()) +"', '" + 
				SQLManager.stringUnnull(this.getURL()) + "', " + user_id + ", NOW(), NOW(), NOW(), " + 
				this.getNodeType() + ", " + this.getFolderFlag() + ");";
			// get the id
			try
			{
				// insert stub node
				if(node_type != iNode.NODE_TYPE_I_CONCEPTS)
				{
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(qry);
					stmt.close();
					
					// get last inserted id
					qry = "SELECT MAX(LAST_INSERT_ID(NodeID)) AS LastID FROM ent_node WHERE UserID=" + user_id + ";";
					PreparedStatement statement = conn.prepareStatement(qry);
					rs = statement.executeQuery();
					while(rs.next())
					{
						node_id = rs.getInt("LastID");
					}
					rs.close();
					rs = null;
					statement .close();
					statement = null;
					
					// connect new node to the parent
					qry = "INSERT INTO rel_node_node (ParentNodeID, ChildNodeID, Weight, OrderRank)" +
						" VALUES (" + this.getParent().getId() + ", " + node_id + ", 1, " + (this.getParent().getChildren().size()+1) + ");";
					Statement stmt1 = conn.createStatement();
					stmt1.executeUpdate(qry);
					stmt1.close();

				}
			}//end -- try
			catch (Exception e) { e.printStackTrace(System.err); }
		}// end of -- if object is stored in db
		else
		{
			node_id = -resmap.getNextVirtualNodeId();
			session.setAttribute(ClientDaemon.SESSION_RES_MAP, resmap);
		}
		
		// re-add the node with correct id
		this.setId(node_id);
		
		this.getParent().getChildren().remove(this);
		this.getParent().getChildren().add(this);
		
		resmap.getNodes().remove(this);
		resmap.getNodes().add(this);
		
		resmap.setPendingNode(null);
		
		session.setAttribute(ClientDaemon.SESSION_RES_MAP, resmap);
		
		if(this.xtrnl_object != null)
		{
			xtrnl_object.addToDB(conn, request, this);
		}
		
		return node_id;
	}
	
}