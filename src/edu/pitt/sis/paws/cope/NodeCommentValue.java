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

public class NodeCommentValue extends NodePropertyValue 
	implements iNodePropertyValue
{
	protected String comment;
	
	private NodeCommentValue(int _id, String _comment, User _user)
	{
		super(_id, _user);
		comment = _comment;
	}
	
	public NodeCommentValue(int _id, String _comment, User _user, 
		NodeProperty _prop)
	{
		super(_id, _user, _prop);
		comment = (_comment==null)?"":_comment;
	}
	
	public iNodePropertyValue clone(User _user) 
	{
		NodeCommentValue copy = null;
		try { copy = new NodeCommentValue(this.id,new String(this.comment), _user); }
		catch (Exception e) { System.err.println(e.toString()); }
		return copy;
	}
		
	
	public String getStringValue() { return comment; }
	public void setStringValue(String _str_value)
	{
		comment = (_str_value==null)?"":_str_value;
	}

	public int getIntValue() { return 0; }
	public void setIntValue(int _int_value) { ; }
	
	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException { ; }
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException{ ; }
		
	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException 
	{
		out.println("<div style='border:1px solid #999999;padding:5px;'>" + comment + "</div>");
	}
		
	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		out.println("<script type='text/javascript'>");
		out.println("   _editor_url = '" + request.getContextPath() + "/assets/htmlarea/';");
		out.println("   _editor_lang = 'en';");
		out.println("</script>");
		out.println("<script type='text/javascript' src='" + request.getContextPath() + "/assets/htmlarea/htmlarea.js'></script>");
		out.println("<script type='text/javascript' >");
		out.println("var editor = null;");
		out.println("function initEditor()");
		out.println("{");
		out.println("	editor = new HTMLArea('" + NodeProperty.FORM_NODE_PROPERTY_COMMENT_TEXT + "');");
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
		out.println("");
		out.println("</head>");
		out.println("");
		out.println("<body onload='HTMLArea.init(); HTMLArea.onload = initEditor'>");
		out.println("");
		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/jspBottom?" + NodeProperty.REQUEST_NODE_PROPERTY_TYPE + "=" + NodeProperty.REQUEST_NODE_PROPERTY_TYPE_COMMENT + "&" + NodeProperty.REQUEST_NODE_PROPERTY_MODE + "=" + NodeProperty.REQUEST_NODE_PROPERTY_MODE_SUBMIT +"'>");
//		out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Comment/Summary</div>");
		out.println("<!-- Node property type field -->");
		out.println("<input name='" +  NodeProperty.FORM_NODE_PROPERTY_TYPE + "' type='hidden' value='" + NodeProperty.REQUEST_NODE_PROPERTY_TYPE_COMMENT + "'>");
		out.println("<!-- Comment Value ID field -->");
		out.println("<input name='" + NodeProperty.FORM_NODE_PROPERTY_ID +  "' type='hidden' value='" + this.getId() + "'>");
		out.println("<!-- Comment Value ID - as position in Property field -->");
		out.println("<input name='" + NodeProperty.REQUEST_NODE_PROPERTY_ID +  "' type='hidden' value='" + request.getParameter(NodeComment.REQUEST_NODE_PROPERTY_ID) + "'>");
		out.println("<div style='margins:0px 0px 10px 15px; border:1px solid #999999;'>");
		out.println("<textarea name='" + NodeProperty.FORM_NODE_PROPERTY_COMMENT_TEXT + "' id='" + NodeProperty.FORM_NODE_PROPERTY_COMMENT_TEXT + "' style='width:100%;padding:0px 0px 10px 15px;' rows='20' cols='80' title='The description of the document/folder'>");
		out.println(this.comment);
		out.println("</textarea>");
		out.println("</div>");
		out.println("<p>");
		out.println("<a style='color:#000099; font-weight=bold; border:1px solid #000099; padding:5px; margin:0px 10px 0px 0px;' href='javascript:mySubmit()'>Submit</a>");
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit()");
		out.println("{");
//		out.println("	var error_msg = 'Fields:\\n';");
//		out.println("	var error = false");
//		out.println("	");
//		out.println("	if( document.edit.fld_title.value.length == 0 )");
//		out.println("	{");
//		out.println("		errror_msg += '  * Title';");
//		out.println("		error = true;");
//		out.println("	}");
//		out.println("	error_msg = '\\n should not contail an empty string';");
//		out.println("	");
//		out.println("	if(error) alert(error_msg );");
//		out.println("	else");
//		out.println("	{");
		out.println("		document.edit.onsubmit();");
		out.println("		document.edit.submit();");
//		out.println("	}");	
		out.println("}");
		out.println("</script>");
		String cancel_to_url2 = " href='" + cancel_to_url + "'";
		out.println("<a style='color:#000099; font-weight=bold; border:1px solid #000099; padding:5px; margin:0px 10px 0px 0px;'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</p>");
		out.println("</form>");
	}
	
}