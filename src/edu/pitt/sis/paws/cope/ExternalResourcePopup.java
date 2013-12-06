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

public class ExternalResourcePopup extends ExternalResourceVisual
{
	
	/**Constructor of the ExternalResourcePopup - for using addToDB only */
	public ExternalResourcePopup()
	{
		super(0,"","","","");
	}

	// Constructors & cloners

	 /** Main constructor of the ExternalResourcePopup
	 * @param _id - ExternalResourcePopup id
	 * @param _title - ExternalResourcePopup title
	 * @param _url - url of the ExternalResourcePopup
	 * @param _user - creator of the ExternalResourcePopup
	 */
	public ExternalResourcePopup(int _id, String _title, String _uri, User _user, String _url, String _ext_id)
	{
	      super(_id, _title, _uri, _user, ((_url==null)?"":_url), _ext_id);
	}
	
	/** Constructor for cloning (creator will be set to null)
	 * @param _id - ExternalResourcePopup id
	 * @param _title - ExternalResourcePopup title
	 * @param _url - url of the ExternalResourcePopup
	 */
	public ExternalResourcePopup(int _id, String _title, String _uri, String _url, String _ext_id)
	{
		super(_id, _title, _uri, ((_url==null)?"":_url), _ext_id);
	}

	/** This cloner should be used in the interest of the Node object when
 	 * the latter emulates ExternalResourcePopup and is being cloned itself.
	 * @return copy of the ExternalResourcePopup object */
	public ExternalResourcePopup clone() 
	{
		ExternalResourcePopup copy = null;
		try
		{ 
			copy = new ExternalResourcePopup(this.getId(), new String(this.getTitle()), new String(this.getURI()),
				new String(this.url), new String(this.ext_id) ); 
		}
		catch (Exception e) { e.printStackTrace(System.out); }
		return copy;
	}
	
	// IMPLEMENTATION OF HTML Visualization
	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException
	{
		String activity_url = Resource.addIdentityToURL(this.url, request);
//System.out.println("ExternalResourcePopup.showView starting...");	
//System.out.println("ExternalResourcePopup.showView forwarding to URL='" + this.url + "'");	

		out.println("<!-- Open -->");
		out.println("<div class='pt_main_subheader' title='Link to open'>Open&nbsp;\"" + this.getTitle() + "\"</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println("A new window will open automatically. Click <a href='" + activity_url + "'target='_blank'>here</a> to open it manually.");
		out.println("</div>");
		out.println("<script language='javascript'>window.open('" + activity_url + "');</script>");
		
	}
	
}
