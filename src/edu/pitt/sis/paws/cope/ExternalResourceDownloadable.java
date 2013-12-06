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

public class ExternalResourceDownloadable extends ExternalResourceVisual
{
	
	/**Constructor of the ExternalResourceDownloadable - for using addToDB only */
	public ExternalResourceDownloadable()
	{
		super(0,"","","","");
	}

	// Constructors & cloners

	 /** Main constructor of the ExternalResourceDownloadable
	 * @param _id - ExternalResourceDownloadable id
	 * @param _title - ExternalResourceDownloadable title
	 * @param _url - url of the ExternalResourceDownloadable
	 * @param _user - creator of the ExternalResourceDownloadable
	 */
	public ExternalResourceDownloadable(int _id, String _title, String _uri, User _user, String _url, String _ext_id)
	{
	      super(_id, _title, _uri, _user, ((_url==null)?"":_url), _ext_id);
	}
	
	/** Constructor for cloning (creator will be set to null)
	 * @param _id - ExternalResourceDownloadable id
	 * @param _title - ExternalResourceDownloadable title
	 * @param _url - url of the ExternalResourceDownloadable
	 */
	public ExternalResourceDownloadable(int _id, String _title, String _uri, String _url, String _ext_id)
	{
		super(_id, _title, _uri, ((_url==null)?"":_url), _ext_id);
	}

	/** This cloner should be used in the interest of the Node object when
 	 * the latter emulates ExternalResourceDownloadable and is being cloned itself.
	 * @return copy of the ExternalResourceDownloadable object */
	public ExternalResourceDownloadable clone() 
	{
		ExternalResourceDownloadable copy = null;
		try
		{ 
			copy = new ExternalResourceDownloadable( this.getId(), new String(this.getTitle()), new String(this.getURI()),
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
//System.out.println("ExternalResourceDownloadable.showView starting...");	
//System.out.println("ExternalResourceDownloadable.showView forwarding to URL='" + this.url + "'");	

		out.println("<!-- Download -->");
		out.println("<div class='pt_main_subheader' title='Downloadable link'>Download&nbsp;\"" + this.getTitle() + "\"</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println("<a href='" + activity_url + "'target='_blank'>link</a>&nbsp(Right-click and choose \"Save AS\")");
		out.println("</div>");
	}
	
}
