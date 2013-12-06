/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/** 
 * This class handles CoPE Search
 * @author Michael V. Yudelson
 */

package edu.pitt.sis.paws.cope;

import java.io.*;
import java.sql.*;
import java.util.Comparator;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

//import edu.pitt.sis.paws.core.iItem;

//import edu.pitt.sis.paws.core.*;

public class CoPESearchNode extends Node
{
	// Constants
	public static final String COPESEARCH_FILTER_AUTHOR = "fltauth";
	public static final String COPESEARCH_FILTER_YEAR = "fltyear";
	public static final String COPESEARCH_FILTER_TITLE = "flttitle";
	public static final String COPESEARCH_FILTER_RATING = "fltrating";
	public static final String COPESEARCH_SORT = "srt";
	public static final String COPESEARCH_SORT_AUTHOR_INC = "a";
	public static final String COPESEARCH_SORT_AUTHOR_DEC = "A";
	public static final String COPESEARCH_SORT_YEAR_INC = "y";
	public static final String COPESEARCH_SORT_YEAR_DEC = "Y";
	public static final String COPESEARCH_SORT_TITLE_INC = "t";
	public static final String COPESEARCH_SORT_TITLE_DEC = "T";

	public static Comparator<Paper> cmpAuthorInc = new PaperAuthorComparatorInc<Paper>();
	public static Comparator<Paper> cmpAuthorDec = new PaperAuthorComparatorDec<Paper>();
	public static Comparator<Paper> cmpTitleInc = new PaperTitleComparatorInc<Paper>();
	public static Comparator<Paper> cmpTitleDec = new PaperTitleComparatorDec<Paper>();
	public static Comparator<Paper> cmpYearInc = new PaperYearComparatorInc<Paper>();
	public static Comparator<Paper> cmpYearDec = new PaperYearComparatorDec<Paper>();
	
	public CoPESearchNode(int _id)
	{
		super(_id, "CoPE Search", NODE_TYPE_I_COPE_SEARCH, ""/*descr*/, 
			""/*url*/, false/*folderflag*/, null/*external*/);
	}

	public iNode clone(User _user, boolean _set_xtrnal_obj) { return null; }	// Cannot be cloned

	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
		out.println("<div class='pt_main_subheader_editing_name'>CoPE Search node</div>");
		out.println("<div>");
		out.println("<p>CoPE Search node cannot be edited</p>");
		String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";
		out.println("<a class='pt_main_edit_button_ok' " + cancel_to_url2 + ">Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</div>");
	}
	
	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		out.println("</head>");
		out.println("<body>");
	}

	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException
	{
		HttpSession session = request.getSession();
		request.setCharacterEncoding("utf-8");
		
		// get request parameters
		String filter_author = (String)session.getAttribute(COPESEARCH_FILTER_AUTHOR);
//System.out.println("CoPENode:: filter_author original= " + filter_author + " decoded=" + URLDecoder.decode(filter_author, "utf-8") + " re-encode=" + URLEncoder.encode(filter_author, "utf-8"));	

		String filter_year = (String)session.getAttribute(COPESEARCH_FILTER_YEAR);
		String filter_title = (String)session.getAttribute(COPESEARCH_FILTER_TITLE);
		String filter_rating = (String)session.getAttribute(COPESEARCH_FILTER_RATING);

		session.removeAttribute(COPESEARCH_FILTER_AUTHOR);
		session.removeAttribute(COPESEARCH_FILTER_YEAR);
		session.removeAttribute(COPESEARCH_FILTER_TITLE);
		session.removeAttribute(COPESEARCH_FILTER_RATING);
		
		filter_author = (filter_author==null)?"":filter_author;
		filter_year = (filter_year==null)?"":filter_year;
		filter_title = (filter_title==null)?"":filter_title;
		filter_rating = (filter_rating==null)?"":filter_rating;
//System.out.println("param @ " + request.getParameterMap());	
		
		String CoPE_Search_url = request.getContextPath() + "/content/jspCOPESearch" +
			"?" + COPESEARCH_FILTER_AUTHOR + "=" + filter_author + //URLEncoder.encode(filter_author,"utf-8") +
			"&" + COPESEARCH_FILTER_YEAR + "=" + filter_year +
			"&" + COPESEARCH_FILTER_TITLE + "=" + filter_title +
			"&" + COPESEARCH_FILTER_RATING + "=" + filter_rating ;
//System.out.println("search @ " + CoPE_Search_url);	

		out.println("<script>");
		out.println("	if(parent != null)");
		out.println("	{");
//		out.println("		alert(encodeURI('" + filter_author + "','utf-8'));");
		out.println("		document.location = '" + CoPE_Search_url + "';");
		out.println("	}");
		out.println("</script>");
		out.println("<body>");
		out.println("</body>");
//System.out.println("CoPENode2:: CoPE_Search_url= " + CoPE_Search_url);	
		
	}
	
	public void showViewHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		return;
	}

	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{// Doesn't add anything new
		return 0;
	}

	public void saveToDB(Connection conn, HttpServletRequest request, iNode node, 
			int changes) throws Exception
	{// Doesn't save anything
		return;
	}	
	
	public int updateObject(HttpServletRequest request) throws Exception
	{
		return 0;
	}
	
	public String toString() { return "CoPE Search"; };
	
}

// Comparators
class PaperAuthorComparatorInc<E extends Paper> implements Comparator<E>
{
	public int compare(E p1, E p2)
	{
		return (p1.getAuthors().compareToIgnoreCase(p2.getAuthors()));
	}	
}

class PaperAuthorComparatorDec<E extends Paper> implements Comparator<E>
{
	public int compare(E p1, E p2)
	{
		return -(p1.getAuthors().compareToIgnoreCase(p2.getAuthors()));
	}	
}

class PaperTitleComparatorInc<E extends Paper> implements Comparator<E>
{
	public int compare(E p1, E p2)
	{
		return (p1.getTitle().compareToIgnoreCase(p2.getTitle()));
	}	
}

class PaperTitleComparatorDec<E extends Paper> implements Comparator<E>
{
	public int compare(E p1, E p2)
	{
		return -(p1.getTitle().compareToIgnoreCase(p2.getTitle()));
	}	
}

class PaperYearComparatorInc<E extends Paper> implements Comparator<E>
{
	public int compare(E p1, E p2)
	{
		return (p1.getYear() - p2.getYear());
	}	
}

class PaperYearComparatorDec<E extends Paper> implements Comparator<E>
{
	public int compare(E p1, E p2)
	{
		return -(p1.getYear() - p2.getYear());
	}	
}