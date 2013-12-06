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

/** This class represents a CoPE summary */
public class Summary extends Resource 
{
	// CONSTANTS
	public static String URI_PREFIX = "summary";
	// Form fields (specific)
	public static final String SUMMARY_FRMFIELD_ID = "fld_summary_id";
	public static final String SUMMARY_FRMFIELD_SUMMARY = "fld_summary_text";

	/** text of the summary */
	protected String summary_text;
	
	/** Paper, the summary is about */
	protected Paper paper;
	
	/** Main constructor of the Summary
	* @param _id - Id of the summary
	* @param _user - creator of the summary
	* @param _summary_text - text of the summary */
	public Summary(int _id, User _user, String _uri, String _summary_text, Paper _paper)
	{
		super(_id, "Summary", _uri, _user);
		summary_text = _summary_text;
		paper = _paper;
	}
	
	/** Cloner */
	public Summary clone()
	{
		Summary copy = null;
		try
		{ 
			copy = new Summary(this.getId(), null,
					new String(this.getURI()), new String(this.summary_text), null); 
		}
		catch (Exception e) { e.printStackTrace(System.out); }
		return copy;
	}
	
	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException 
	{
		out.println("<div class='pt_main_subheader'>Summary</div>");
		out.println("<div style='border:1px solid #999999;padding:5px;'>" + summary_text + "</div>");
	}

	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		StringBuffer browserDetector = HTMLUtilities.javaScriptBrowserDetector();		
		out.println(browserDetector);
		
		out.println("<script type='text/javascript'>");
		out.println("   _editor_url = '" + request.getContextPath() + "/assets/htmlarea/';");
		out.println("   _editor_lang = 'en';");
		out.println("</script>");
		out.println("<script type='text/javascript' src='" + request.getContextPath() + "/assets/htmlarea/htmlarea.js'></script>");
		out.println("<script type='text/javascript' >");
		out.println("var editor = null;");
		out.println("function initEditor()");
		out.println("{");
		out.println("	editor = new HTMLArea('" + SUMMARY_FRMFIELD_SUMMARY + "');");
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
		out.println("</script>");
		out.println("</head>");
		out.println("");
		out.println("<body onload='HTMLArea.init(); HTMLArea.onload = initEditor'>");
	}
	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		HttpSession session = request.getSession();
		iNode	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
		int node_id = current_node.getId();
		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/doEdit' target='_top'>");
//		out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Comment/Summary</div>");
		out.println("<!-- Node Id -->");
		out.println("<input name='" +  iNode.NODE_FRMFIELD_ID + "' type='hidden' value='" + node_id + "'>");
		out.println("<!-- Summary Id -->");
		out.println("<input name='" +  SUMMARY_FRMFIELD_ID + "' type='hidden' value='" + this.getId() + "'>");
		out.println("<div class='pt_main_subheader'>Summary</div>");
		out.println("<div class='pt_main_subheader_editing_value_textarea'>");
		out.println("<textarea name='" + SUMMARY_FRMFIELD_SUMMARY + "' id='" + SUMMARY_FRMFIELD_SUMMARY + "' style='width:100%;padding:0px;' rows='20' cols='80' title='The description of the document/folder'>");
		out.println(this.summary_text);
		out.println("</textarea>");
		out.println("</div>");
		out.println("<p>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit() { if(BrowserDetect.browser!='Safari')document.edit.onsubmit();/**/ document.edit.submit(); }");
		out.println("</script>");
		String cancel_to_url2 = " href='" + cancel_to_url + "'";
		out.println("<a target='_top' class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</p>");
		out.println("</form>");
	}
	
	public int updateObject(HttpServletRequest request) throws Exception
	{
		int changes = iNode.NODE_CHANGE_NONE;
		
//		int frm_id = Integer.parseInt(request.getParameter(SUMMARY_FRMFIELD_ID));
		String new_summary = SQLManager.stringUnbreak(request.getParameter(SUMMARY_FRMFIELD_SUMMARY));
//	System.out.println("Summary: new_summary='" + new_summary + "'");	
		if(!this.getSummary().equals(new_summary))
		{
			this.setSummary(new_summary);
			changes |= iNode.NODE_CHANGE_SUMMARY;
		}
		
		return changes;
	}
	
	public void saveToDB(Connection conn, HttpServletRequest request, iNode node, int changes)
		throws Exception
	{
		// if not stored in DB - exit
		if(!stored_in_db) return;
		
		// clean up summary
//System.out.println("CMP " + this.getSummary().compareTo(SQLManager.stringUnquote(this.getSummary())));		
		
		String summary_dequoted = SQLManager.stringUnbreak(
				SQLManager.stringUnquote(this.getSummary()));

		String qry = "UPDATE ent_cope_summary SET SummaryText='" + summary_dequoted + "'," +
			"DateModified=NOW() WHERE SummaryID=" + this.getId() + ";";
//System.out.println("Summary.saveToDB qry=" + qry);		
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(qry);
		stmt.close();
	}

	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{
		int summary_id = 0;

		HttpSession session = request.getSession();
		ResourceMap resmap = ((ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP));
//		iNode current_node = (iNode)session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
		Paper current_paper = (Paper)node.getParent().getExternalObject(); 

		if(this.isStoredInDB())
		{
			int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
			String safe_summary = SQLManager.stringUnquote(this.getSummary());

			String qry = "INSERT INTO ent_cope_summary (SummaryText, PaperID,"+
				" UserID, DateCreated, DateModified, DateAltered) " +
				"VALUES ('" + safe_summary + "', " + current_paper.getId() + ", " + user_id + ",NOW(),NOW(),NOW());";

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(qry);
			stmt.close();
			
			qry = "SELECT MAX(LAST_INSERT_ID(SummaryID)) AS LastID FROM ent_cope_summary WHERE UserID=" + user_id + ";";
			PreparedStatement statement = conn.prepareStatement(qry);
			ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				summary_id = rs.getInt("LastID");
			}
			rs.close();
			rs = null;
			statement .close();
			statement = null;
			
			qry = "UPDATE ent_node SET ExtID=" + summary_id + " WHERE NodeID=" +
					node.getId() + ";";
			
			Statement stmt1 = conn.createStatement();
			stmt1.executeUpdate(qry);
			stmt1.close();
		}
		// re-add the node with correct id
		this.setId(summary_id);

		current_paper.getSummaries().remove(this);
		current_paper.getSummaries().add(this);
		
		resmap.getSummaries().remove(this);
		resmap.getSummaries().add(this);

		session.setAttribute(ClientDaemon.SESSION_RES_MAP, resmap);
		
		return summary_id;
	}

	public String getSummary() { return summary_text; }
	public void setSummary(String _summary_text) { summary_text = _summary_text; }
	
	public Paper getPaper() { return paper; }
	public void setPaper(Paper _paper) { paper = _paper; } 
}