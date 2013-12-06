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
import java.net.*;
import java.sql.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.*;
import edu.pitt.sis.paws.core.utils.SQLManager;

public class Paper extends Resource
{
	// CONSTANTS
	protected static String URI_PREFIX = "paper";
	// Form fields (specific)
//	private static final String PAPER_FRMFIELD_TITLE = "fld_title";
	public static final String PAPER_FRMFIELD_YEAR = "fld_year";
	public static final String PAPER_FRMFIELD_BIBLIOINFO = "fld_biblioinfo";
	public static final String PAPER_FRMFIELD_AUTHORS = "fld_authors";
	public static final String PAPER_FRMFIELD_AUTHOR_LASTNAME = "fld_author_lastname";
	public static final String PAPER_FRMFIELD_AUTHOR_GIVENNAMES = "fld_author_givennames";
	public static final String PAPER_FRMFIELD_AUTHOR_ID = "fld_author_id";
	public static final String PAPER_FRMFIELD_AUTHOR_STATUS = "fld_status";
	public static final String PAPER_FRMFIELD_AUTHOR_NUMBER = "fld_author_num";
	public static final String PAPER_FRMFIELD_AUTHOR_MAX_NUMBER = "fld_author_max_num";
	public static final int AUTHORS_MAX_NUMBER = 10;
	
//	private static final String PAPER_FRMFIELD_URL = "fld_url";

//	@SuppressWarnings("unused")
//	private static final String RESOURCE_RATING_LABELS = 
//		"['<span style='color:red;'>Highly Irrelevant</span>','<span style='color:orange;'>Irrelevant</span>','Neutral','<span style='color:olive;'>Relevant</span>','<span style='color:green;'>Highly Relevant</span>']";
//	@SuppressWarnings("unused")
//	private static final String[] NODE_RATING_LABELS_ARRAY = {
//		"<span style='color:red;'>Highly Irrelevant</span>",
//		"<span style='color:orange;'>Irrelevant</span>",
//		"Neutral",
//		"<span style='color:olive;'>Relevant</span>",
//		"<span style='color:green;'>Highly Relevant</span>"};

	
	// Rating labels
//	private static final String[] RESOURCE_RATING_LABELS = {"Not relevant", 
//		"Maybe not relevant", "Maybe Relevant", "Relevant", 
//		"Definitely Relevant"};
	
	/**
	 * Year paper was published in
	 */
	private int year;
	/** URL of the paper on the web
	 */
	private String biblio_info;
	/** Authors - TEMPORATY FIELD - paper authors list
	 */
	private String authors;
	/** URL - the url of the paper
	 */
	private String url;
	/** Summaries collection for the paper*/
	private Item2Vector<Summary> summaries;

	/** Authors - TAKEOVER FIELD - paper authors list
	 */
	protected OrderedWeightedItem2Vector<Author> authorss;
	
	/**Constructor of the Paper - for using addToDB only */
	public Paper()
	{
		super(0,"", "");
	}
	// Constructors & cloners
	/**Main constructor of the Paper
	* @param _id - paper id
	* @param _title - paper title
	* @param _year - year of publication
	* @param _biblio_info - bibliographic info
	* @param _authors - list of authors
	* @param _url - url of the paper
	* @param _user - creator of the paper
	*/
	public Paper(int _id, String _title, String _uri, int _year,
		String _biblio_info, String _authors, String _url, User _user)
	{
		super(_id, _title, _uri, _user);
		year = _year;
		biblio_info = (_biblio_info==null)?"":_biblio_info;
		authors = (_authors==null)?"":_authors;
		url = (_url==null)?"":_url;
		summaries = new Item2Vector<Summary>();
		authorss = new OrderedWeightedItem2Vector<Author>(); 
	}
	
	/**Constructor for cloning (creator will be set to null
	* @param _id - paper id
	* @param _title - paper title
	* @param _year - year of publication
	* @param _biblio_info - bibliographic info
	* @param _authors - list of authors
	* @param _url - url of the paper
	*/
	public Paper(int _id, String _title, String _uri, int _year,
		String _biblio_info, String _authors, String _url)
	{
		super(_id, _title, _uri);
		year = _year;
		biblio_info = (_biblio_info==null)?"":_biblio_info;
		authors = (_authors==null)?"":_authors;
		url = (_url==null)?"":_url;
		summaries = new Item2Vector<Summary>();
		authorss = new OrderedWeightedItem2Vector<Author>(); 
	}

	/**This cloner should be used in the interest of the Node object when
	* the latter emulates Paper and is being cloned itself.
	* @return copy of the Paper object
	*/
	public Paper clone() 
	{
		Paper copy = null;
		try
		{ 
			copy = new Paper(this.getId(), new String(this.getTitle()), new String(this.getURI()),
				this.year, new String(this.biblio_info), 
				new String(this.authors), new String(this.url) ); 
		}
		catch (Exception e) { e.printStackTrace(System.out); }
		return copy;
	}
	// Setters & getters
	/** Getter for year field
	* @return year */
	public int getYear() { return year; }
	/** Getter for year field
	* @return year */
	public void setYear(int _year) { year = _year; }
	/** Getter for bibliographic information field
	* @return bibliographic information */
	public String getBiblioInfo() { return biblio_info; }
	/** Getter for bibliographic information field
	* @return bibliographic information */
	public void setBiblioInfo(String _biblio_info) { biblio_info = _biblio_info; }
	/** Getter for list of authors field
	* @return authors */
//	public String getAuthors() { return authors; }
	public String getAuthors()
	{
		String result = "";
		for(int i=0; i<this.authorss.size(); i++)
			result += ((result.length()>0)?"; ":"") + this.authorss.get(i).getLastName() + ", " + this.authorss.get(i).getGivenNames(); 
		return result;
	}
	/** Getter for list of authors field
	* @return authors */
	public Item2Vector<Author> getAuthorss() { return authorss; }
	/** Getter for list of authors field
	* @return authors */
	public void setAuthors(String _authors) { authors = _authors; }
	/** Getter for url field
	* @return url */
	public String getURL() { return url; }
	/** Getter for url field
	* @return url */
	public void setURL(String _url) { url = _url; }
	/** Getter for summaries collection
	* @return summaries collection */
	public Item2Vector<Summary> getSummaries() { return summaries; }
	
	/** Method returns 5 rating labels for the item that might vary from item type to item type.
	 * @return 5 rating labels for 5 stars
	 * @since 1.5
	 */
	public String getRatingLabels()
	{
		String RESOURCE_RATING_LABELS = 
			"['<span style=\"color:red;\">Highly Irrelevant</span>','<span style=\"color:orange;\">Irrelevant</span>','Neutral','<span style=\"color:olive;\">Relevant</span>','<span style=\"color:green;\">Highly Relevant</span>']";
		return RESOURCE_RATING_LABELS;
	}

	public String[] getRatingLabelsArray()
	{
		String[] RESOURCE_RATING_LABELS_ARRAY = {
				"<span style=\"color:red;\">Highly Irrelevant</span>",
				"<span style=\"color:orange;\">Irrelevant</span>",
				"Neutral",
				"<span style=\"color:olive;\">Relevant</span>",
				"<span style=\"color:green;\">Highly Relevant</span>"};
		return RESOURCE_RATING_LABELS_ARRAY;
	}

	// IMPLEMENTATION OF HTML Visualization
	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException
	{
//System.out.println("Paper.showView starting...");	

		HttpSession session = request.getSession();
		int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();
		int group_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_GROUP_ID)).intValue();
		iNode c_node = ((iNode)session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE));
		ResourceMap resmap = (ResourceMap) session.getAttribute(
				ClientDaemon.SESSION_RES_MAP);
//System.out.println("ResourceMap.displayFolderView activities " + res_map.getActivities().size());/// DEBUG
		User user = resmap.getUsers().findById(user_id);
		User group = resmap.getUsers().findById(group_id);

		// searhc for other folders where paper might be
		String other_folders = "";
		if(this.getOwners().size()>1)
		{
			int count = 0;
			for(int i=0; i<getOwners().size(); i++)
			{
				
				if(getOwners().get(i).getId() != c_node.getId())
				{
					other_folders += ((other_folders.length()>0)?"&lt;br/&gt;":"") + "" + (++count) + ". &lt;a href=" +
							request.getContextPath() + "/content/Show?id=" +
							getOwners().get(i).getParent().getId() + " target=_top&gt;" +
							getOwners().get(i).getParent().getTitle() +
							"&lt;/a&gt;";
				}
			}
			other_folders = "&nbsp;&nbsp;<a href='#' onmouseup=\"this.T_TITLE='Paper can be also found in these folders'; this.T_STICKY=true; this.T_PADDING=5; return escape('" +
					other_folders + "')\"><img border='0'src='" + request.getContextPath() + "/" + 
					ClientDaemon.CONTEXT_ASSETS_PATH + "/folder_btn_enable.gif" + "' /></a>";
		}
		
		boolean can_see_author = c_node.canSeeAuthor(user, group, resmap);
		String display_author = ((can_see_author)? "<div style='" + 
			"font-size:0.9em; font-weight:bold; color:#000099; " + 
			"margin:5px 0px 5px 0px; font-family:Times, serif;'>This paper is added by " + 
			c_node.getCreatorAdderNames() +	
			
			other_folders +
			
			"</div>": "");
		out.println(display_author);

		
		out.println("<div style='font-size:0.8em;'>&dagger; Point to the lable to see comment</div>");

//		out.println("<!-- Authors -->");
//		out.println("<div class='pt_main_subheader' title='Author(s) of the paper'>Author(s)</div>");
//		out.println("<div style='padding:3px; padding-left:15px;'/>");
//		out.println(this.authors);
//		out.println("</div>");

		out.println("<!-- Authors Collection-->");
		String cope_search_url = request.getContextPath() + "/content/Show?id=" +
				resmap.getCopeSearchNodeId() + "&" + 
				CoPESearchNode.COPESEARCH_FILTER_AUTHOR + "=";
		String authorss = "";
		for(int i=0; i<this.getAuthorss().size(); i++)
		{	
			authorss += ((authorss.length()>0)?"; ":"") + 
					"<a target='_top' href='" + cope_search_url + URLEncoder.encode(this.getAuthorss().get(i).getLastName(), "utf-8")  + "'>" + this.getAuthorss().get(i).getLastName() + ", " + 
					this.getAuthorss().get(i).getGivenNames() + "</a>";
//System.out.println("Paper:: original=" + this.getAuthorss().get(i).getLastName() + " encoded=" + URLEncoder.encode(this.getAuthorss().get(i).getLastName(), "utf-8"));
		}
		
		out.println("<div class='pt_main_subheader' title='Author(s) of the paper'>Author(s)</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println(authorss);
		out.println("</div>");

		out.println("<!-- Title -->");
		out.println("<div class='pt_main_subheader' title='Title of the paper'>Title</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println(this.getTitle());
		out.println("</div>");

		out.println("<!-- Bibliographic Info -->");
		out.println("<div class='pt_main_subheader' title='Bibliographic information: journal/conference, publisher, location'>Bibliographic information</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println(this.biblio_info);
		out.println("</div>");
		
		out.println("<!-- Year -->");
		cope_search_url = request.getContextPath() + "/content/Show?id=" +
				resmap.getCopeSearchNodeId() + "&" + 
				CoPESearchNode.COPESEARCH_FILTER_YEAR + "=" + this.year;
;
		out.println("<div class='pt_main_subheader' title='Year of publication'>Year</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println("<a target='_top' href='" + cope_search_url + "' title='Find all papers with this year of publication'>" + this.year + "</a>");
		out.println("</div>");

		out.println("<!-- URL -->");
		out.println("<div class='pt_main_subheader' title='URL where the paper can be found at'>URL</div>");
		out.println("<div style='padding:3px; padding-left:15px;'/>");
		out.println("<a href='" + this.url + "' target='_blank'>" + this.url + "</a>");
		out.println("</div>");
		
		// Display subordinates of the Paper (e.g. Summary, Concepts) if such exist
		out.println("<!-- subordinates of the Paper (e.g. Summary, Concepts) if such exis -->");
		out.println("<p />");
		out.println("<script src='" + request.getContextPath() + "/" + 
					ClientDaemon.CONTEXT_ASSETS_PATH + "/wz_tooltip.js' type='text/javascript'></script>");

		// verify whether ratings should be displayed
		ResourceMap.displayFolderView(c_node.getChildren(), out, request, false);
	}

	public void showEditHeader(JspWriter out, HttpServletRequest request)
		throws IOException
	{
		out.println("<script type='text/javascript' src='" + request.getContextPath() + "/" + 
				ClientDaemon.CONTEXT_ASSETS_PATH + "/prototype.js'></script>");
		out.println("<script type='text/javascript' src='" + request.getContextPath() + "/" + 
				ClientDaemon.CONTEXT_ASSETS_PATH + "/capxous.js'></script>");
		out.println("<link rel='stylesheet' type='text/css' href='" + request.getContextPath() + "/" + 
			ClientDaemon.CONTEXT_ASSETS_PATH + "/capxous.css'/>");
		
		out.println("<script type='text/javascript'>");
		out.println("function checkNumeric(node)");
		out.println("{");
		out.println("	var val = node.value;");
		out.println("	for(var i=0;i<val.length;i++)");
		out.println("		if(val.charAt(i)<'0' || val.charAt(i)>'9')");
		out.println("		{");
		out.println("			if(i==(val.length-1))");
		out.println("			{");
		out.println("				node.value = val.substr(0,i);");
		out.println("			}");
		out.println("			else");
		out.println("			{");
		out.println("				node.value = val.substr(0,i)+val.substr(i+1,val.length-i-1);");
		out.println("			}");
		out.println("			alert('Only numbers are allowed for this parameter!');");
		out.println("		}");
		out.println("}");
//		out.println("if (document.layers)");
//		out.println("	document.captureEvents(Event.KEYPRESS);");
//		out.println("document.onkeypress = function (evt)");
//		out.println("{");
//		out.println("	var key = ");
//		out.println("	document.all ? event.keyCode :");
//		out.println("	evt.which ? evt.which : evt.keyCode;");
//		out.println("	if (key == 13) ");
//		out.println("	document.edit.submit();");
//		out.println("};");
		
		out.println("function add_author()");
		out.println("{");
		out.println("	if(" + PAPER_FRMFIELD_AUTHOR_NUMBER +"<" + PAPER_FRMFIELD_AUTHOR_MAX_NUMBER +")");
		out.println("	{");
		out.println("		" + PAPER_FRMFIELD_AUTHOR_NUMBER +" ++;");
//		out.println("		alert('author to show' + document.getElementById('tr_author_'+" + PAPER_FRMFIELD_AUTHOR_NUMBER + "));");
//		out.println("		alert('author to show.style' + document.getElementById('tr_author_'+" + PAPER_FRMFIELD_AUTHOR_NUMBER + ").style);");
//		out.println("		alert('author to show.style.display' + document.getElementById('tr_author_'+" + PAPER_FRMFIELD_AUTHOR_NUMBER + ").style.display);");
		out.println("		document.getElementById('tr_author_'+" + PAPER_FRMFIELD_AUTHOR_NUMBER + ").style.display = 'block';");
		out.println("		if(" + PAPER_FRMFIELD_AUTHOR_NUMBER +"==" + PAPER_FRMFIELD_AUTHOR_MAX_NUMBER +")");
		out.println("			document.getElementById('tr_add').style.display = 'none';");
		out.println("	}");
		out.println("	document.getElementById('" + PAPER_FRMFIELD_AUTHOR_NUMBER +"').value = " + PAPER_FRMFIELD_AUTHOR_NUMBER +";");
		out.println("}");

		out.println("function rem_author(node)");
		out.println("{");
		out.println("	var rem_author_num = parseInt(node.name);");
		out.println("	var i;");
		out.println("	// copy the authors after the deleted one");
		out.println("	if(rem_author_num != " + PAPER_FRMFIELD_AUTHOR_MAX_NUMBER +")");
		out.println("	{// delete not the last element");
		out.println("		for(i=rem_author_num+1; i<=" + PAPER_FRMFIELD_AUTHOR_NUMBER +"; i++)");
		out.println("		{");
		out.println("			document.getElementById('" + PAPER_FRMFIELD_AUTHOR_LASTNAME + "'+(i-1)).value = ");
		out.println("				document.getElementById('" + PAPER_FRMFIELD_AUTHOR_LASTNAME + "'+(i)).value;");
		out.println("			document.getElementById('" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + "'+(i-1)).value=");
		out.println("				document.getElementById('" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + "'+(i)).value;");
		out.println("			document.getElementById('" + PAPER_FRMFIELD_AUTHOR_ID + "'+(i-1)).value=");
		out.println("				document.getElementById('" + PAPER_FRMFIELD_AUTHOR_ID + "'+(i)).value;");
		out.println("			document.getElementById('" + PAPER_FRMFIELD_AUTHOR_STATUS + "'+(i-1)).src=");
		out.println("				document.getElementById('" + PAPER_FRMFIELD_AUTHOR_STATUS + "'+(i)).src;");
		out.println("		}");
		out.println("	}");
		out.println("	// hide and decrease the counter");
		out.println("	document.getElementById('tr_author_'+" + PAPER_FRMFIELD_AUTHOR_NUMBER +").style.display = 'none';");
		out.println("	" + PAPER_FRMFIELD_AUTHOR_NUMBER +" --;");
		out.println("	document.getElementById('tr_add').style.display = 'block';");
		out.println("	// wipe the remainder clean");
		out.println("	for(i=" + PAPER_FRMFIELD_AUTHOR_NUMBER +"+1; i<=" + PAPER_FRMFIELD_AUTHOR_MAX_NUMBER +"; i++)");
		out.println("	{");
		out.println("		document.getElementById('" + PAPER_FRMFIELD_AUTHOR_LASTNAME + "'+i).value='';");
		out.println("		document.getElementById('" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + "'+i).value='';");
		out.println("		document.getElementById('" + PAPER_FRMFIELD_AUTHOR_ID + "'+i).value='0';");
		out.println("		document.getElementById('" + PAPER_FRMFIELD_AUTHOR_STATUS + "'+i).src='" + request.getContextPath() + "/" + 
				ClientDaemon.CONTEXT_ASSETS_PATH + "/new.gif';");
		out.println("	}");
		out.println("	");
		out.println("	document.getElementById('" + PAPER_FRMFIELD_AUTHOR_NUMBER +"').value = " + PAPER_FRMFIELD_AUTHOR_NUMBER +";");
		out.println("}");
		
		out.println("function mark_author_new(node)");
		out.println("{");
		out.println("	var edit_author_num = 0;");
		out.println("	if(node.name.indexOf('" + PAPER_FRMFIELD_AUTHOR_LASTNAME + "')>=0)");
		out.println("		edit_author_num = parseInt(node.name.substring(" + PAPER_FRMFIELD_AUTHOR_LASTNAME.length() + "));");
		out.println("	else if (node.name.indexOf('" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + "')>=0)");
		out.println("		edit_author_num = parseInt(node.name.substring(" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES.length() + "));");
		out.println("	else");
		out.println("		return;");
		
//		out.println("	var edit_author_num = parseInt(node.name);");
		out.println("	document.getElementById('" + PAPER_FRMFIELD_AUTHOR_ID + "'+edit_author_num).value='0';");
		out.println("	document.getElementById('" + PAPER_FRMFIELD_AUTHOR_STATUS + "'+edit_author_num).src='" + request.getContextPath() + "/" + 
				ClientDaemon.CONTEXT_ASSETS_PATH + "/new.gif';");
		out.println("}");
		out.println("</script>");
		out.println("</head>");
		out.println("<body>");
	}
/*
	public void showEditHeader(JspWriter out, HttpServletRequest request)
	throws IOException
{
	out.println("<script type='text/javascript' src='" + request.getContextPath() + "/" + 
			ClientDaemon.CONTEXT_ASSETS_PATH + "/prototype.js'></script>");
	out.println("<script type='text/javascript' src='" + request.getContextPath() + "/" + 
			ClientDaemon.CONTEXT_ASSETS_PATH + "/capxous.js'></script>");
	out.println("<link rel='stylesheet' type='text/css' href='" + request.getContextPath() + "/" + 
		ClientDaemon.CONTEXT_ASSETS_PATH + "/capxous.css'/>");
	
	out.println("<script type='text/javascript'>");
	out.println("function checkNumeric(node)");
	out.println("{");
	out.println("	var val = node.value;");
	out.println("	for(var i=0;i<val.length;i++)");
	out.println("		if(val.charAt(i)<'0' || val.charAt(i)>'9')");
	out.println("		{");
	out.println("			if(i==(val.length-1))");
	out.println("			{");
	out.println("				node.value = val.substr(0,i);");
	out.println("			}");
	out.println("			else");
	out.println("			{");
	out.println("				node.value = val.substr(0,i)+val.substr(i+1,val.length-i-1);");
	out.println("			}");
	out.println("			alert('Only numbers are allowed for this parameter!');");
	out.println("		}");
	out.println("}");
//	out.println("if (document.layers)");
//	out.println("	document.captureEvents(Event.KEYPRESS);");
//	out.println("document.onkeypress = function (evt)");
//	out.println("{");
//	out.println("	var key = ");
//	out.println("	document.all ? event.keyCode :");
//	out.println("	evt.which ? evt.which : evt.keyCode;");
//	out.println("	if (key == 13) ");
//	out.println("	document.edit.submit();");
//	out.println("};");
	
	out.println("function add_author()");
	out.println("{");
	out.println("	var elem = document.getElementById('add');");
	out.println("	var ndParent = null;");
	out.println("	var elNew = null;");
	out.println("	var count = 0;");
	out.println("	ndParent = elem.parentNode;");
	out.println("	count = ndParent.childNodes.length;");
	out.println("	elNew = document.createElement('tr');");
	out.println("");
	out.println("	elNew1td = document.createElement('td');");
	out.println("	elNew1 = document.createElement('input');");
	out.println("	elNew1.type = 'text';");
	out.println("	elNew1.value = '';");
	out.println("	elNew1.size = 30;");
	out.println("	elNew1.maxlength = 50;");
	out.println("	elNew1.onkeypress = set_author_new;");
	out.println("");
	out.println("	elNew2td = document.createElement('td');");
	out.println("	elNew2 = document.createElement('input');");
	out.println("	elNew2.type = 'text';");
	out.println("	elNew2.value = '';");
	out.println("	elNew2.size = 30;");
	out.println("	elNew2.maxlength = 50;");
	out.println("	elNew2.onkeypress = set_author_new;");
	out.println("");
	out.println("	elNew2Atd = document.createElement('td');");
	out.println("	elNew2A = document.createElement('input');");
	out.println("	elNew2A.type = 'hidden';");
	out.println("	elNew2A.value = '0';");
	out.println("	elNew2A.size = 5;");
	out.println("	elNew2A.maxlength = 5;");
	out.println("");
	out.println("	elNew2Btd = document.createElement('td');");
	out.println("	elNew2B = document.createElement('img');");
	out.println("	elNew2B.src = '" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/new.gif" + "';");
	out.println("");
	out.println("	elNew3td = document.createElement('td');");
	out.println("	elRem = document.createElement('a');");
	out.println("	elRem.href = '#';");
	out.println("	elRem.id = 'rem';");
	out.println("	elRem.onclick = remove;");
	out.println("	elRem.innerHTML = '&times;&nbsp(remove)';");
	out.println("	elRem.style.padding = '0px 0px 0px 2px';");
	out.println("	elRem.style.display = 'inline';");
	out.println("	elRem.title = 'Remove author';");
	out.println("");
	out.println("	elNew1td.insertBefore(elNew1, null);");
	out.println("	elNew2td.insertBefore(elNew2, null);");
	out.println("	elNew2Atd.insertBefore(elNew2A, null);");
	out.println("	elNew2Btd.insertBefore(elNew2B, null);");
	out.println("	elNew3td.insertBefore(elRem, null);");
	out.println("");
	out.println("	elNew.insertBefore(elNew1td, null);");
	out.println("	elNew.insertBefore(elNew2td, null);");
	out.println("	elNew.insertBefore(elNew2Atd, null);");
	out.println("	elNew.insertBefore(elNew2Btd, null);");
	out.println("	elNew.insertBefore(elNew3td, null);");
	out.println("");
	out.println("	ndParent.insertBefore(elNew, elem);");
	out.println("	index(ndParent);");
	out.println("}");
	
	out.println("function remove(rem_node)");
	out.println("{");

	out.println("	var parentDiv = null;");
	out.println("	var topParent = null;");
	out.println("	if(this!=window)");
	out.println("		parentDiv = this.parentNode.parentNode;");
	out.println("	else");
	out.println("		parentDiv = rem_node.parentNode.parentNode;");
	out.println("	topParent = parentDiv.parentNode;");
	out.println("	topParent.removeChild(parentDiv);");
	out.println("	index(topParent);");
	
	// wipe elements
	out.println("	var j = 0;");
	out.println("	for(j=0;j<autocomplete.length;j++)");
	out.println("		delete autocomplete[j];");
	// wipe array
	out.println("	delete autocomplete;");
	
	out.println("	autocomplete = new Array();");
	
	out.println("	autocomplete_count--;");
	
	out.println("	j = 0;");
	out.println("	for(j=0;j<autocomplete_count;j++)");
	out.println("	{");
	out.println("		autocomplete[j] = new CAPXOUS.AutoComplete(\"fld_author_lastname\" + (j+1), function() { return \"" + request.getContextPath() + "/content/AjaxCoPEAuthorRobot?idx=\"+(j)+\"&typing=\"+this.text.value; });");
	out.println("	}");
	
	
//	out.println("	autocomplete2 = new Array();");
//	out.println("	var count = 0;");
//	out.println("	var j = 0;");
//	out.println("	var idx = (parseInt(this.name)-1);");
//	out.println("	for(j=0;j<idx;j++)");
//	out.println("	{");
//	out.println("//		alert('rem_parse_bf_'+j);");
//	out.println("		autocomplete2[count] = autocomplete[j];");
//	out.println("		count++;");
//	out.println("	}");
//	out.println("	//autocomplete[idx]=null;");
//	out.println("	delete autocomplete[idx];");
//	out.println("	for(j=(idx+1);j<autocomplete.length;j++)");
//	out.println("	{");
//	out.println("//		alert('rem_parse_af_'+j);");
//	out.println("		autocomplete2[count] = new CAPXOUS.AutoComplete(\"fld_author_lastname\" + (j), function() { return \"" +
//			request.getContextPath() + "/content/AjaxCoPEAuthorRobot?idx=\"+(j-1)+\"&typing=\" + this.text.value; });");
//	out.println("		delete autocomplete[j];");
//	out.println("		count++;");
//	out.println("	}");
//	
//	out.println("	delete autocomplete;");
//	out.println("	autocomplete = autocomplete2;");
//	out.println("	autocomplete2 = null;");
//	out.println("	autocomplete_count--;");
	out.println("}");
	
	out.println("function index(ndParent)");
	out.println("{");
//	out.println("	alert('table id ' + ndParent.id + ' ' + ndParent.childNodes.length);");
	out.println("	var i = 0;");
	out.println("	for(i=1;i<(ndParent.childNodes.length-1);i++)");
	out.println("	{");
	out.println("		elem = ndParent.childNodes[i];");
	out.println("		elem.childNodes[0].childNodes[0].name = '" + PAPER_FRMFIELD_AUTHOR_LASTNAME + "' + (i+0);");
	out.println("		elem.childNodes[1].childNodes[0].name = '" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + "' + (i+0);");
	out.println("		elem.childNodes[2].childNodes[0].name = '" + PAPER_FRMFIELD_AUTHOR_ID + "' + (i+0);");
	out.println("		elem.childNodes[3].childNodes[0].name = '" + PAPER_FRMFIELD_AUTHOR_STATUS + "' + (i+0);");
	
	out.println("		elem.childNodes[0].childNodes[0].id = '" + PAPER_FRMFIELD_AUTHOR_LASTNAME + "' + (i+0);");
	out.println("		elem.childNodes[1].childNodes[0].id = '" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + "' + (i+0);");
	out.println("		elem.childNodes[2].childNodes[0].id = '" + PAPER_FRMFIELD_AUTHOR_ID + "' + (i+0);");
	out.println("		elem.childNodes[3].childNodes[0].id = '" + PAPER_FRMFIELD_AUTHOR_STATUS + "' + (i+0);");

	out.println("		if(i!=1)elem.childNodes[4].childNodes[0].name = (i+0);"); // number the 'remove' button
	
	out.println("		if(i>autocomplete_count)");
	out.println("		{");
	out.println("			autocomplete[autocomplete_count] = new CAPXOUS.AutoComplete(\"fld_author_lastname\" + i, function() { return \"" + request.getContextPath() + "/content/AjaxCoPEAuthorRobot?idx=\"+(i-1)+\"&typing=\" + this.text.value; });");
	out.println("			autocomplete_count++;");
	out.println("		}");
	out.println("	}");
	out.println("}");
	
	
	out.println("function set_author_new(id_element)");
	out.println("{");
	out.println("	var parentDiv = null;");
	out.println("	if(this!=window)");
	out.println("		parentDiv = this.parentNode.parentNode;");
	out.println("	else");
	out.println("		parentDiv = id_element.parentNode.parentNode;");
	out.println("	parentDiv.childNodes[2].childNodes[0].value=0;");
	out.println("	parentDiv.childNodes[3].childNodes[0].src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/new.gif" + "';");
	out.println("}");

	out.println("var autocomplete_count = " + this.getAuthorss().size() + ";");
	out.println("var autocomplete = new Array();");
	
	out.println("</script>");
	out.println("</head>");
	out.println("<body>");
}
/**/	
/*
	public void showEdit(JspWriter out, HttpServletRequest request, 
		String cancel_to_url) throws IOException
	{
//System.out.println("Paper.showEdit starting...");	
		HttpSession session = request.getSession();
		iNode	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
		int node_id = current_node.getId();
		out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
			+ request.getContextPath() + "/content/doEdit' target='_top'>");
		out.println("<!-- ID field -->");
		out.println("<input name='" + iNode.NODE_FRMFIELD_ID + "' type='hidden' value='" + node_id + "'>");

//		out.println("<!-- Authors field -->");
//		out.println("<div style='font-family:\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Authors</div>");
//		out.println("<div style='padding:0px 0px 10px 15px;' title='Author(s) of the paper'><input id='" + PAPER_FRMFIELD_AUTHORS + "' name='" + PAPER_FRMFIELD_AUTHORS + "' type='text' value='" + this.authors + "' size='70' maxlength='255'></div>");

		out.println("<!-- Authors field -->");
		out.println("<div style='font-family:\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Authors</div>");
		
		out.print("<table style='padding:0px 0px 0px 15px;' id='authorsSeparately' cellpadding='0' cellspacing='0'>");
		out.print("<tr style='font-size:0.8em; color:#666666;'>");
		out.print("<td scope='col'>Last name</td>");
		out.print("<td scope='col'>First name [, M.I.]</td>");// width='50%'
		out.print("</tr>");
		
		for(int i=0; i<this.getAuthorss().size(); i++)
		{
			out.print("<tr>");
			out.print("<td><input type='text' onmouseover='this.onkeypress=set_author_new;' name='" + 
					PAPER_FRMFIELD_AUTHOR_LASTNAME + (i+1) + 
					"' value='" + this.getAuthorss().get(i).getLastName() +
					"' id='" + PAPER_FRMFIELD_AUTHOR_LASTNAME + (i+1) + 
					"' size='30' maxlength='50'></td>");
			out.print("<td><input type='text' onmouseover='this.onkeypress=set_author_new;' name='" + 
					PAPER_FRMFIELD_AUTHOR_GIVENNAMES + (i+1) + 
					"' value='" + this.getAuthorss().get(i).getGivenNames() +
					"' id='" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + (i+1) + 
					"' size='30' maxlength='50'></td>");
			out.print("<td><input type='hidden' name='" + 
					PAPER_FRMFIELD_AUTHOR_ID + (i+1) + 
 					"' value='" + this.getAuthorss().get(i).getId() +
					"' id='" + PAPER_FRMFIELD_AUTHOR_ID + (i+1) + 
					"' size='5' maxlength='5'></td>");
			out.print("<td><img src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/check.gif' "+
					"id='" + PAPER_FRMFIELD_AUTHOR_STATUS + (i+1) + "' "+
					"name='" + PAPER_FRMFIELD_AUTHOR_STATUS + (i+1) + "'></td>");
			out.print((i>0)?"<td><a id='rem' name='"+(i+1)+"' href='#' onmouseover='this.onclick=remove;' style='padding:0px 0px 0px 2px; display:inline;' title='Remove Author'>&times;&nbsp(remove)</a></td>":"");
			out.print("</tr>");
		}
		out.print("<tr id='add'>");
		out.print("<td colspan='3'><a title='Add author' href='#' onclick='add_author();'>add</a></td>");
		out.print("</tr>");
		out.println("</table>"); 
		out.println("<p />"); 
		
		
		out.println("<!-- Title field -->");
		out.println("<div style='font-family:\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Title</div>");
		out.println("<div style='padding:0px 0px 10px 15px;' title='The title of the paper'><input id='" + iNode.NODE_FRMFIELD_TITLE + "' name='" + iNode.NODE_FRMFIELD_TITLE + "' type='text' value='" + this.title + "' size='70' maxlength='255'></div>");

		out.println("<!-- Bibliographic Info field -->");
		out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Bibliographic Info</div>");
		out.println("<div style='padding:0px 0px 10px 15px;' title='Bibliographic information of the paper'><input id='" + PAPER_FRMFIELD_BIBLIOINFO + "' name='" + PAPER_FRMFIELD_BIBLIOINFO +"' type='text' value='" + this.biblio_info + "' size='70' maxlength='255'></div>");

		out.println("<!--Year field -->");
		out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Year</div>");
		out.println("<div style='padding:0px 0px 10px 15px;' title='Year of publication'><input id='" + PAPER_FRMFIELD_YEAR + "' name='" + PAPER_FRMFIELD_YEAR +"' type='text' value='" + this.year + "' size='8' maxlength='4' onKeyUp='checkNumeric(this);'></div>");

		out.println("<!-- URL field -->");
		out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>URL</div>");
		out.println("<div style='padding:0px 0px 10px 15px;' title='The URL the paper may be foud at'><input id='" + iNode.NODE_FRMFIELD_URL + "' name='" + iNode.NODE_FRMFIELD_URL + "' type='text' value='" + this.url + "' size='70' maxlength='255'></div>");

		out.println("<p>");
		out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
		out.println("<script type='text/javascript'>");
		out.println("function mySubmit()");
		out.println("{");
		out.println("	var error_msg = 'Fields: \\n';");
		out.println("	var error = false");
		out.println("	");
		out.println("	if( document.edit." + iNode.NODE_FRMFIELD_TITLE + ".value.length == 0 )");
		out.println("	{");
		out.println("		error_msg += ' * Title; \\n';");
		out.println("		error = true;");
		out.println("	}");
		out.println("	error_msg += ' should not contail an empty string';");
		out.println("	");
		out.println("	if(error) alert(error_msg );");
		out.println("	else");
		out.println("		document.edit.submit();");
		out.println("}");
		out.println("</script>");

		String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";

		out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
		out.println("</p>");
		out.println("</form>");
		
		out.println("<script>");
		// Autocomplete Author
		for(int i=0; i<this.getAuthorss().size(); i++)
//			out.println("autocomplete[" + i + "] = new CAPXOUS.AutoComplete(\"fld_author_lastname" + (i+1) + "\", function() { return \"" +
//					request.getContextPath() + "/content/AjaxCoPEAuthorRobot?idx=" + (i+1) + "&typing=\" + document.getElementById(\"fld_author_lastname" + (i+1) + "\").value; });");
			out.println("autocomplete[" + i + "] = new CAPXOUS.AutoComplete(\"fld_author_lastname" + (i+1) + "\", function() { return \"" +
				request.getContextPath() + "/content/AjaxCoPEAuthorRobot?idx=" + (i+1) + "&typing=\" + this.text.value; });");
		out.println("</script>");
		
	}
/**/

	public void showEdit(JspWriter out, HttpServletRequest request, 
			String cancel_to_url) throws IOException
		{
//	System.out.println("Paper.showEdit starting...");	
			HttpSession session = request.getSession();
			iNode	current_node = (iNode) session.getAttribute(ClientDaemon.SESSION_CURRENT_NODE);
			int node_id = current_node.getId();
			out.println("<form style='padding:5px 5px 5px 5px;' id='edit' name='edit' method='post' action='"
				+ request.getContextPath() + "/content/doEdit' target='_top'>");
			out.println("<!-- ID field -->");
			out.println("<input name='" + iNode.NODE_FRMFIELD_ID + "' type='hidden' value='" + node_id + "'>");
			
			out.println("<input id='" + PAPER_FRMFIELD_AUTHOR_NUMBER + "' name='" + PAPER_FRMFIELD_AUTHOR_NUMBER + "' type='hidden' value='" + this.getAuthorss().size() + "'>");

//			out.println("<!-- Authors field -->");
//			out.println("<div style='font-family:\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Authors</div>");
//			out.println("<div style='padding:0px 0px 10px 15px;' title='Author(s) of the paper'><input id='" + PAPER_FRMFIELD_AUTHORS + "' name='" + PAPER_FRMFIELD_AUTHORS + "' type='text' value='" + this.authors + "' size='70' maxlength='255'></div>");

			out.println("<!-- Authors field -->");
			out.println("<div style='font-family:\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Authors</div>");
			
			out.println("<table style='padding:0px 0px 0px 15px;' id='authorsSeparately' cellpadding='0' cellspacing='0'>");
			out.println("<tr style='font-size:0.8em; color:#666666;display:block;'>");
			out.println("<td scope='col'>Last name</td>");
			out.println("<td scope='col'>First name [, M.I.]</td>");// width='50%'
			out.println("<td scope='col'>&nbsp;</td>");
			out.println("<td scope='col'>&nbsp;</td>");
			out.println("<td scope='col'>&nbsp;</td>");
			out.println("</tr>");
			
			for(int i=0; i<this.getAuthorss().size(); i++)
			{
				out.println("<tr id='tr_author_" + (i+1) + "' style='display:block;'>");
				out.println("	<td><input type='text' id='" + PAPER_FRMFIELD_AUTHOR_LASTNAME  + (i+1) + "' onkeydown='mark_author_new(this);' value='" + this.getAuthorss().get(i).getLastName() +"' name='" + PAPER_FRMFIELD_AUTHOR_LASTNAME  + (i+1) + "' size='30' maxlength='50'></td>");
				out.println("	<td><input type='text' id='" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + (i+1) + "' onkeydown='mark_author_new(this);' value='" + this.getAuthorss().get(i).getGivenNames() +"' name='" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + (i+1) + "' size='30' maxlength='50'></td>");
				out.println("	<td><input type='hidden' id='" + PAPER_FRMFIELD_AUTHOR_ID + (i+1) + "' value='" + this.getAuthorss().get(i).getId() +"' name='" + PAPER_FRMFIELD_AUTHOR_ID + (i+1) + "' size='5' maxlength='5'></td>");
				out.println("	<td><img src='" + request.getContextPath() + "/" + ClientDaemon.CONTEXT_ASSETS_PATH + "/check.gif' id='" + PAPER_FRMFIELD_AUTHOR_STATUS + (i+1) + "' width='16' height='16' name='" + (i+1) + "'></td>");
				out.println("	<td><a name='" + (i+1) + "' id='rem_" + (i+1) + "' href='#' style='padding:0px 0px 0px 2px;' onclick='rem_author(this);' title='Remove Author'>&times;&nbsp;(remove)</a></td>");
				out.println("</tr>");
			}
			
			for(int i=this.getAuthorss().size(); i<AUTHORS_MAX_NUMBER; i++)
			{
				out.println("<tr id='tr_author_" + (i+1) + "' style='display:none;'>");
				out.println("	<td><input type='text' id='" + PAPER_FRMFIELD_AUTHOR_LASTNAME  + (i+1) + "' onkeydown='mark_author_new(this);' value='' name='" + PAPER_FRMFIELD_AUTHOR_LASTNAME + (i+1) + "' size='30' maxlength='50'></td>");
				out.println("	<td><input type='text' id='" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + (i+1) + "' onkeydown='mark_author_new(this);' value='' name='" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + (i+1) + "' size='30' maxlength='50'></td>");
				out.println("	<td><input type='hidden' id='" + PAPER_FRMFIELD_AUTHOR_ID + (i+1) + "' value='0' name='" + PAPER_FRMFIELD_AUTHOR_ID + (i+1) + "' size='5' maxlength='5'></td>");
				out.println("	<td><img src='" + request.getContextPath() + "/" + ClientDaemon.CONTEXT_ASSETS_PATH + "/new.gif' id='" + PAPER_FRMFIELD_AUTHOR_STATUS + (i+1) + "' width='16' height='16' name='" + (i+1) + "'></td>");
				out.println("	<td><a name='" + (i+1) + "' id='rem_" + (i+1) + "' href='#' style='padding:0px 0px 0px 2px;' onclick='rem_author(this);' title='Remove Author'>&times;&nbsp;(remove)</a></td>");
				out.println("</tr>");
			}
			
			out.println("<tr id='tr_add' style=''>");
			out.println("	<td scope='col'><a title='Add author' href='#' onclick='add_author();'>add</a></td>");
			out.println("	<td scope='col'>&nbsp;</td>");
			out.println("	<td scope='col'>&nbsp;</td>");
			out.println("	<td scope='col'>&nbsp;</td>");
			out.println("	<td scope='col'>&nbsp;</td>");
			out.println("</tr>");

			out.println("</table>");

			out.println("<!-- Title field -->");
			out.println("<div style='font-family:\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Title</div>");
			out.println("<div style='padding:0px 0px 10px 15px;' title='The title of the paper'><input id='" + iNode.NODE_FRMFIELD_TITLE + "' name='" + iNode.NODE_FRMFIELD_TITLE + "' type='text' value='" + this.getTitle() + "' size='70' maxlength='255'></div>");

			out.println("<!-- Bibliographic Info field -->");
			out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Bibliographic Info</div>");
			out.println("<div style='padding:0px 0px 10px 15px;' title='Bibliographic information of the paper'><input id='" + PAPER_FRMFIELD_BIBLIOINFO + "' name='" + PAPER_FRMFIELD_BIBLIOINFO +"' type='text' value='" + this.biblio_info + "' size='70' maxlength='255'></div>");

			out.println("<!--Year field -->");
			out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>Year</div>");
			out.println("<div style='padding:0px 0px 10px 15px;' title='Year of publication'><input id='" + PAPER_FRMFIELD_YEAR + "' name='" + PAPER_FRMFIELD_YEAR +"' type='text' value='" + this.year + "' size='8' maxlength='4' onKeyUp='checkNumeric(this);'></div>");

			out.println("<!-- URL field -->");
			out.println("<div style='font-family::\"Times, serif\";font-size:0.9em; font-weight:bold; color:#000099;'>URL</div>");
			out.println("<div style='padding:0px 0px 10px 15px;' title='The URL the paper may be foud at'><input id='" + iNode.NODE_FRMFIELD_URL + "' name='" + iNode.NODE_FRMFIELD_URL + "' type='text' value='" + this.url + "' size='70' maxlength='255'></div>");

			out.println("<p />");
			out.println("<a class='pt_main_edit_button_ok' href='javascript:mySubmit()'>Submit</a>&nbsp;&nbsp;&nbsp;&nbsp;");
			out.println("<script type='text/javascript'>");
			
			for(int i=0; i<AUTHORS_MAX_NUMBER; i++)
				out.println("new CAPXOUS.AutoComplete('" + PAPER_FRMFIELD_AUTHOR_LASTNAME + (i+1) + 
						"', function() { return '" + request.getContextPath() + "/content/AjaxCoPEAuthorRobot?idx=" + (i+1) + "&typing=' + this.text.value; });");

			out.println("var " + PAPER_FRMFIELD_AUTHOR_NUMBER + " = " + this.getAuthorss().size() + ";");
			out.println("document.getElementById('" + PAPER_FRMFIELD_AUTHOR_NUMBER + "').value = " + PAPER_FRMFIELD_AUTHOR_NUMBER + ";");
			out.println("var " + PAPER_FRMFIELD_AUTHOR_MAX_NUMBER +" = " + AUTHORS_MAX_NUMBER + ";");
			out.println("var k;");
			out.println("for(k=" + PAPER_FRMFIELD_AUTHOR_NUMBER + "+1; k<=" + PAPER_FRMFIELD_AUTHOR_MAX_NUMBER +"; k++)");
			out.println("{");
			out.println("	document.getElementById('" + PAPER_FRMFIELD_AUTHOR_LASTNAME + "'+k).value='';");
			out.println("	document.getElementById('" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + "'+k).value='';");
			out.println("	document.getElementById('" + PAPER_FRMFIELD_AUTHOR_ID + "'+k).value='0';");
			out.println("	document.getElementById('" + PAPER_FRMFIELD_AUTHOR_STATUS + "'+k).src='/portal_client/assets/new.gif';");
			out.println("}");
			
			out.println("function mySubmit()");
			out.println("{");
			
			out.println("	var k;");
			out.println("	var do_submit = 1;");
			out.println("	for(k=1; k<=" + PAPER_FRMFIELD_AUTHOR_NUMBER + "; k++)");
			out.println("	{");
			out.println("		var author_lastname = document.getElementById('" + PAPER_FRMFIELD_AUTHOR_LASTNAME + "'+k).value;");
			out.println("		var author_givennames = document.getElementById('" + PAPER_FRMFIELD_AUTHOR_GIVENNAMES + "'+k).value;");
			out.println("		if( author_lastname==null || author_lastname =='' ||");
			out.println("			author_givennames==null || author_givennames=='' )");
			out.println("		{");
			out.println("			do_submit = 0;");
			out.println("			alert('Author #' + k + ' was not specified correctly.\\nLast name and/or given name(s) are null.\\n\\nSpecify the missing value or remove the author.');");
			out.println("			break;");
			out.println("		}");
			out.println("	}");

			out.println("	if(do_submit==1)");
			out.println("	{");

			out.println("		var error_msg = 'Fields: \\n';");
			out.println("		var error = false");
			out.println("		");
			out.println("		if( document.edit." + iNode.NODE_FRMFIELD_TITLE + ".value.length == 0 )");
			out.println("		{");
			out.println("			error_msg += ' * Title; \\n';");
			out.println("			error = true;");
			out.println("		}");
			out.println("		error_msg += ' should not contail an empty string';");
			out.println("		");
			out.println("		if(error) alert(error_msg );");
			out.println("		else");
			out.println("		{");
//			out.println("			alert('title=' + document.edit." + iNode.NODE_FRMFIELD_TITLE + ".value + ' enc='+encodeURI(document.edit." + iNode.NODE_FRMFIELD_TITLE + ".value) );");
			out.println("			document.edit.submit();");
			out.println("		}");

			out.println("	}");
			out.println("}");
			out.println("</script>");

			String cancel_to_url2 = " href='" + cancel_to_url + "' " + "target='_top'";

			out.println("<a class='pt_main_edit_button_cancel'" + cancel_to_url2 + ">Cancel</a>");
			out.println("</p>");
			out.println("</form>");			
		}

	
	public int updateObject(HttpServletRequest request) throws Exception
	{
		HttpSession session = request.getSession();
		ResourceMap resmap = (ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP);

		int changes = iNode.NODE_CHANGE_NONE;

		String new_title = request.getParameter(iNode.NODE_FRMFIELD_TITLE);
		String new_url = request.getParameter(iNode.NODE_FRMFIELD_URL);
		
		int new_year = Integer.parseInt(request.getParameter(PAPER_FRMFIELD_YEAR));
		String new_biblio = request.getParameter(PAPER_FRMFIELD_BIBLIOINFO);
//		String new_authors = request.getParameter(PAPER_FRMFIELD_AUTHORS);
		
		// Title - no 
		if(!this.getTitle().equals(new_title))
		{
			this.setTitle(new_title);
			changes |= iNode.NODE_CHANGE_TITLE;
			
			// change all owners
			for(int i=0; i<this.getOwners().size(); i++)
				this.getOwners().get(i).setTitle(new_title);
		}
		
		// URL 
		if(!this.getURL().equals(new_url))
		{
			this.setURL(new_url);
			changes |= iNode.NODE_CHANGE_URL;

			// change all owners
			for(int i=0; i<this.getOwners().size(); i++)
				this.getOwners().get(i).setURL(new_url);
		}
			
		// Year
		if(this.getYear() != new_year)
		{
			this.setYear(new_year);
			changes |= iNode.NODE_CHANGE_YEAR;
		}
		
		// Biblio
		if(!this.getBiblioInfo().equals(new_biblio))
		{
			this.setBiblioInfo(new_biblio);
			changes |= iNode.NODE_CHANGE_BIBLIO;
		}
		
		// Authors
//		if(!this.getAuthors().equals(new_authors))
//		{
//			this.setAuthors(new_authors);
//			changes |= iNode.NODE_CHANGE_AUTHORS;
//		}
//System.out.println("non-authors done");		
		//Authors - list
		// the colection of paper authors wil be flushed autmatically
		// BUT papers should be deleted from individual authors
		for(int i=0; i<this.getAuthorss().size(); i++)
			this.getAuthorss().get(i).getPapers().remove(this);
		
		// get the count of authors
		String req_author_num = request.getParameter(PAPER_FRMFIELD_AUTHOR_NUMBER);
		int author_num_i = Integer.parseInt(req_author_num);
		
		OrderedWeightedItem2Vector<Author> new_authorss = new OrderedWeightedItem2Vector<Author>();  
		Vector<Integer> new_authorss_idx = new Vector<Integer>();  
//		Vector<Integer> new_authorss_ids = new Vector<Integer>();  
		Enumeration enu = request.getParameterNames();
		for(;enu.hasMoreElements();)
		{// for all parameters
			String key = (String)enu.nextElement();
//System.out.println("param="+key);				
			Pattern p = Pattern.compile(PAPER_FRMFIELD_AUTHOR_LASTNAME+"[0-9]+");
			Matcher m = p.matcher("");
			m.reset(key);
			if(m.matches())
			{
				int author_index = Integer.parseInt( key.substring(PAPER_FRMFIELD_AUTHOR_LASTNAME.length()) );
				if(author_index > author_num_i)
					continue;
				String author_lastname = request.getParameter(key);

				try
				{
					// Convert from Unicode to UTF-8
					String string;
					byte[] utf8 = author_lastname.getBytes("UTF-8");
					
					// Convert from UTF-8 to Unicode
					string = new String(utf8, "UTF-8");
//					System.out.println("new encodings: " + string + " found at " + string.indexOf('\u2019'));
				}
				catch (UnsupportedEncodingException e) {}
				
				String author_givennames = request.getParameter(PAPER_FRMFIELD_AUTHOR_GIVENNAMES + author_index);
				// Safeguard quotes
				author_lastname = HTMLUtilities.replaceSingleQuote(author_lastname);
				author_givennames = HTMLUtilities.replaceSingleQuote(author_givennames);
//System.out.println("author_lastname="+author_lastname);				
				int author_id = Integer.parseInt( request.getParameter(PAPER_FRMFIELD_AUTHOR_ID + author_index) );
				
				Author author = null;
				if(author_id == 0)
					author = new Author(0,author_lastname, "" /*URI*/, author_givennames);
				else
					author = resmap.getAuthors().findById(author_id);
				
				if(author == null)
					System.out.println("Paper.update Author with id " + 
							author_id + " is not found!!!");
				else
				{
					
					// add author to the paper & paper to the author
					new_authorss.add(author);
					author.getPapers().add(this);
					
					new_authorss_idx.add(new Integer(author_index-1));
				}
				
				if( ((changes & iNode.NODE_CHANGE_AUTHORS) == 0) && 
						( (author_index>this.getAuthorss().size()) || 
						(author.getId() != this.getAuthorss().get(author_index-1).getId()) ||
						(this.getAuthorss().size() != new_authorss.size() ) ) )
					changes |= iNode.NODE_CHANGE_AUTHORS;
//System.out.println("Paper.update Author " + "(" + author_id + 
//		")" + author_index + " " + author_lastname + ", " + 
//		author_givennames );
			}// end of - for all parameters
			
		}// end of - for all parameters
		// reorder the new authors

		new_authorss.reorder(new_authorss_idx);
//System.out.print("Paper.updateObject Authors# were " + this.authorss.size() + ", parsed " + new_authorss.size());		
		if( (changes & iNode.NODE_CHANGE_AUTHORS) >0 ) // if any changes were made
		{
//System.out.print(" <changed> ");			
			this.authorss.removeAllElements();
			this.authorss.addAll(new_authorss);
		}
//System.out.println(", become " + this.authorss.size());
		
		return changes;
		
	}
	
	public void addEditAuthors(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{
		HttpSession session = request.getSession();
		ResourceMap resmap = (ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP);
		String qry = "";
		
		// 1. delete old connections
		qry = "DELETE FROM rel_cope_paper_author WHERE PaperID=" + 
				this.getId() + ";";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(qry);
		stmt.close();
		
		// 2. walk new adding new authors
		
		qry = "";
//System.out.println("Paper physicaly adding " + this.getAuthorss().size() + " authors");			
		for(int i=0; i<this.getAuthorss().size(); i++)
		{
//System.out.println("Paper author #" + (i+1) + " " + this.getAuthorss().get(i));					
			if(this.getAuthorss().get(i).getId() == 0)
			{
				Author new_author = this.getAuthorss().get(i);
				new_author.addToDB(conn, request, node);
				// readd for the current paper
				this.getAuthorss().remove(i);
				this.getAuthorss().add(i, new_author);
				// readd for the resource map
				resmap.getAuthors().remove(new_author);
				resmap.getAuthors().add(new_author);
			}
			qry += ((qry.length()>0)?",":"") + "(" + this.getId() + ", " +
					this.getAuthorss().get(i).getId() + "," + (i+1) + ")";
		}
		if(this.getAuthorss().size()>0) // authors were added
		{
			qry = "INSERT INTO rel_cope_paper_author (PaperID, AuthorID, Idx) " +
			"VALUES" + qry + ";";
//System.out.println("Paper qry='" + qry + "'");			
			Statement stmt1 = conn.createStatement();
			stmt1.executeUpdate(qry);
			stmt1.close();

		}
	}
	
	// IMPLEMENTATION OF DBStored interface
	public void saveToDB(Connection conn, HttpServletRequest request, iNode node,
			int changes) throws Exception
	{
		// if not stored in DB - exit
		if(!stored_in_db) return;

		String qryN = "";
		String qryP = "";
		
		// Process Title
		if((changes & iNode.NODE_CHANGE_TITLE) > 0)
		{
			String title_dequoted = SQLManager.stringUnquote(this.getTitle());

			qryN += ((qryN.length() > 0)?" ,":"") + "Title='" +
				title_dequoted + "'";
			qryP += ((qryP.length() > 0)?" ,":"") + "Title='" +
				title_dequoted + "'";
			
		}
		
		// Process URL
		if((changes & iNode.NODE_CHANGE_URL) > 0)
		{
			String url_dequoted = SQLManager.stringUnquote(this.getURL());
			
			qryN += ((qryN.length() > 0)?" ,":"") + "URL='" +
				url_dequoted + "'";
			qryP += ((qryP.length() > 0)?" ,":"") + "URL='" +
				url_dequoted + "'";
		}
		
		// Process Biblio Info
		if((changes & iNode.NODE_CHANGE_BIBLIO) > 0)
		{
			String biblio_info_dequoted = SQLManager.stringUnquote(this.getBiblioInfo());
			
			qryP += ((qryP.length() > 0)?" ,":"") + "BiblioInfo='" +
				biblio_info_dequoted + "'";
		}
		
		// Process Year
		if((changes & iNode.NODE_CHANGE_YEAR) > 0)
		{
			qryP += ((qryP.length() > 0)?" ,":"") + "Year=" + this.getYear();
		}

		// Process Authors
//		if((changes & iNode.NODE_CHANGE_AUTHORS) > 0)
//		{
//			String authors_dequoted = SQLManager.stringUnquote(this.getAuthors());
//
//			qryP += ((qryP.length() > 0)?" ,":"") + "Authors='" +
//				authors_dequoted + "'";
//		}
		
		// Process Authorss
		if((changes & iNode.NODE_CHANGE_AUTHORS) > 0)
		{
			addEditAuthors(conn, request, node);
		}
		
		if(!qryN.equals(""))
		{// Save changes into Node if any
			qryN += ((qryN.length() > 0)?" ,":"") + "DateModified=NOW()";		

//			String where_in = "";
//			for(int i=0; i<owners.size(); i++)
//				where_in += (((where_in.length()>0)?",":"")+owners.get(i).getId());
//			String big_qryN = "UPDATE ent_node SET " + qryN + 
//			" WHERE NodeID IN(" + where_in + ");";

			String big_qryN = "UPDATE ent_node SET " + qryN + 
				" WHERE ExtID=" + this.getId() + " AND ItemTypeID=6;";
			big_qryN = SQLManager.stringUnbreak(big_qryN);

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(big_qryN);
			stmt.close();

		}// -- end -- Save changes into Node if any
			
		if(!qryP.equals(""))
		{// Save changes into Paper if any
			qryP += ((qryP.length() > 0)?" ,":"") + "DateModified=NOW()";		

			String big_qryP = "UPDATE ent_cope_paper SET " + qryP + 
				" WHERE PaperID=" + this.getId() + ";";
			
			// Throw out line feeds
			big_qryP = big_qryP.replaceAll("\\r*\\n*", "");

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(big_qryP);
			stmt.close();

		}// -- end -- Save changes into Paper if any
	}

	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{
		int paper_id = 0;
		HttpSession session = request.getSession();
		ResourceMap resmap = ((ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP));

		if(this.isStoredInDB())
		{
			// Deal with the paper
			int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();

			String qry = "INSERT INTO ent_cope_paper (" /*+ "Authors, "*/ + "Title,"+
					" Year, URL, BiblioInfo, UserID, " + 
					"DateCreated, DateModified, DateAltered) VALUES ('" + 
					/*SQLManager.stringUnnull(this.getAuthors()) + "', '" +*/ 
					SQLManager.stringUnquote(SQLManager.stringUnnull(this.getTitle())) + "', " + 
					this.getYear() + ", '" + 
					SQLManager.stringUnnull(this.getURL()) + "', '" + 
					SQLManager.stringUnquote(SQLManager.stringUnnull(this.getBiblioInfo())) + "'," + 
					user_id + ",NOW(),NOW(),NOW());";
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(qry);
			stmt.close();

			
			qry = "SELECT MAX(LAST_INSERT_ID(PaperID)) AS LastID FROM ent_cope_paper WHERE UserID=" + user_id + ";";
			PreparedStatement statement = conn.prepareStatement(qry);
			ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				paper_id = rs.getInt("LastID");
			}
			rs.close();
			rs = null;
			statement .close();
			statement = null;

			qry = "UPDATE ent_node SET ExtID=" + paper_id + " WHERE NodeID=" +
					node.getId() + ";";
			Statement stmt1 = conn.createStatement();
			stmt1.executeUpdate(qry);
			stmt1.close();
			
		}

		// re-add the paper with correct id
		this.setId(paper_id);
		
		// get the count of authors
		String req_author_num = request.getParameter(PAPER_FRMFIELD_AUTHOR_NUMBER);
		int author_num_i = Integer.parseInt(req_author_num);

		// track the authors from request
		OrderedWeightedItem2Vector<Author> new_authorss = new OrderedWeightedItem2Vector<Author>();  
		Vector<Integer> new_authorss_idx = new Vector<Integer>();  
//		Vector<Integer> new_authorss_ids = new Vector<Integer>();  
		Enumeration enu = request.getParameterNames();
		for(;enu.hasMoreElements();)
		{// for all parameters
			String key = (String)enu.nextElement();
//System.out.println("param="+key);				
			Pattern p = Pattern.compile(PAPER_FRMFIELD_AUTHOR_LASTNAME+"[0-9]+");
			Matcher m = p.matcher("");
			m.reset(key);
			if(m.matches())
			{
				int author_index = Integer.parseInt( key.substring(19) );
				if(author_index > author_num_i)
					continue;

				String author_lastname = request.getParameter(key);
				String author_givennames = request.getParameter(PAPER_FRMFIELD_AUTHOR_GIVENNAMES + author_index);
//				boolean throw_away = (author_lastname == null) || (author_lastname.equals(""));
//				String author_id_s = request.getParameter(PAPER_FRMFIELD_AUTHOR_ID + author_index);
//System.out.println("author_lastname="+author_lastname);				
				int author_id = Integer.parseInt( request.getParameter(PAPER_FRMFIELD_AUTHOR_ID + author_index) );
				
				Author author = null;
				if(author_id == 0)
					author = new Author(0,author_lastname, "" /*URI*/, author_givennames);
				else
					author = resmap.getAuthors().findById(author_id);
				
				if(author == null)
					System.out.println("Paper.update Author with id " + 
							author_id + " is not found!!!");
				else
				{
					
					// add author to the paper & paper to the author
					new_authorss.add(author);
					author.getPapers().add(this);
					
					new_authorss_idx.add(new Integer(author_index-1));
//					new_authorss_ids.add(new Integer(author.getId()));
				}
				
//System.out.println("Paper.addToDB Author " + "(" + author.getId() + 
//		")" + author_index + " " + author.getLastName() + ", " + 
//		author.getGivenNames());
			}// end of - for all parameters
			
		}// end of - for all parameters
		// reorder the new authors

		new_authorss.reorder(new_authorss_idx);
		this.authorss.removeAllElements();
		this.authorss.addAll(new_authorss);

//for(int k=0;k<this.authorss.size();k++)
//	System.out.println("Paper.addToDB -- Author " + "(" + this.authorss.get(k).getId() + 
//			")" + (k+1) + " " + this.authorss.get(k).getLastName() + ", " + 
//			this.authorss.get(k).getGivenNames() );

		
		// add the authors
		addEditAuthors(conn, request, node);
		
		resmap.getPapers().remove(this);
		resmap.getPapers().add(this);
		
		session.setAttribute(ClientDaemon.SESSION_RES_MAP, resmap);
		
		return paper_id;
	}
	
	/** Method determines whether the rating of the item should be shown before item itself or after
	 * @return true if ration sjould be showm before, false otherwise
	 * @since 1.5
	 */
	public boolean isRatingShownBefore() { return false /* after */; }

}