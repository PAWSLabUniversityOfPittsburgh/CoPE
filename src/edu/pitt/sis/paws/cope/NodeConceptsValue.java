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

public class NodeConceptsValue extends NodePropertyValue 
	implements iNodePropertyValue
{
	protected ItemVector<iConcept> concepts;
	
	private NodeConceptsValue(User _user)
	{
		super(_user.getId(), _user);
		concepts = new ItemVector<iConcept>();
	}
	
	public NodeConceptsValue(User _user, NodeProperty _prop)
	{
		super(_user.getId(), _user, _prop);
		concepts = new ItemVector<iConcept>();
	}
	
	public iNodePropertyValue clone(User _user) 
	{
		NodeConceptsValue copy = null;
		try { copy = new NodeConceptsValue(_user); }
		catch (Exception e) { System.err.println(e.toString()); }
		return copy;
	}

	public ItemVector<iConcept> getConcepts() { return concepts; }
	
	public String getStringValue()
	{
		String result = "";
		for(int i=0; i<concepts.size(); i++)
			result+= ((result.length()>0)?", ":"") + 
				concepts.get(i).getTitle();
		return ("<div style='margins:0px 5px 0px 5px'>" + result + "</div>");
	}
	
	public void setStringValue(String _str_value) { ; }

	public int getIntValue() { return 0; }
	public void setIntValue(int _int_value) { ; }
	
	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException { ; }
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException{ ; }
		
	public void showView(JspWriter out, HttpServletRequest request,
		boolean show_ratings) throws IOException 
	{
		out.print(this.getStringValue());
	}
		
	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException { ; }

}