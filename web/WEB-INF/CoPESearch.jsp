<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8" import="edu.pitt.sis.paws.cope.*, 
    	edu.pitt.sis.paws.core.*, java.util.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<link rel='stylesheet' href="<%=request.getContextPath() + "/" +
	ClientDaemon.CONTEXT_ASSETS_PATH%>/KnowledgeTree.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title><!-- -->

<%!
	String dojo_path = "";
	ResourceMap resmap = null;
	Item2Vector<Paper> papers = null;
	String filter_author = "";
	String filter_year = "";
	String filter_title = "";
	String sort = "";
	String sort_author = "";
	String sort_year = "";
	String sort_title = "";
	String sorl_url_prefix = "";
	String style_author = "";
	String style_year = "";
	String style_title = "";
	String CoPE_Search_url = "";
%>
<%
	dojo_path = request.getContextPath() + "/" +
	ClientDaemon.CONTEXT_ASSETS_PATH + "/dojo/";
	resmap = (ResourceMap) session.getAttribute(ClientDaemon.SESSION_RES_MAP);
	papers = resmap.getPapers();
	
	request.setCharacterEncoding("utf-8");
	
//	System.out.println("papers#" + papers.size());
//	System.out.println("nodes#" + resmap.getNodes().size());
	
	
	filter_author = request.getParameter(CoPESearchNode.COPESEARCH_FILTER_AUTHOR);
//System.out.println("jsp filter_author original= " + filter_author + " decoded=" + URLDecoder.decode(filter_author, "utf-8"));	
//	filter_author = URLDecoder.decode(filter_author, "utf-8");
	
	filter_year = request.getParameter(CoPESearchNode.COPESEARCH_FILTER_YEAR);
	filter_title = request.getParameter(CoPESearchNode.COPESEARCH_FILTER_TITLE);
	sort = request.getParameter(CoPESearchNode.COPESEARCH_SORT);

	CoPE_Search_url = request.getContextPath() + "/content/jspCOPESearch";
//System.out.println("jsp @ " + CoPE_Search_url);	
	
//System.out.println("jspCoPE Search: auth="+filter_author+", yr="+filter_year+", tit="+filter_title+" ");

	filter_author = (filter_author==null)?"":filter_author;
	filter_year = (filter_year==null)?"":filter_year;
	filter_title = (filter_title==null)?"":filter_title;
	sort = (sort==null)?"":sort;
	
	// assign sorting flags
	sort_author = (sort.equals(CoPESearchNode.COPESEARCH_SORT_AUTHOR_INC))
	?CoPESearchNode.COPESEARCH_SORT_AUTHOR_DEC
	:CoPESearchNode.COPESEARCH_SORT_AUTHOR_INC;
	sort_year = (sort.equals(CoPESearchNode.COPESEARCH_SORT_YEAR_INC))
	?CoPESearchNode.COPESEARCH_SORT_YEAR_DEC
	:CoPESearchNode.COPESEARCH_SORT_YEAR_INC;
	sort_title = (sort.equals(CoPESearchNode.COPESEARCH_SORT_TITLE_INC))
	?CoPESearchNode.COPESEARCH_SORT_TITLE_DEC
	:CoPESearchNode.COPESEARCH_SORT_TITLE_INC;
	
	sorl_url_prefix = request.getContextPath() + "/content/jspCOPESearch" +
		"?" + CoPESearchNode.COPESEARCH_FILTER_AUTHOR + "=" + filter_author +
		"&" + CoPESearchNode.COPESEARCH_FILTER_YEAR + "=" + filter_year +
		"&" + CoPESearchNode.COPESEARCH_FILTER_TITLE + "=" + filter_title +
		"&" + CoPESearchNode.COPESEARCH_SORT + "=";
	
	style_author = (sort.equals(CoPESearchNode.COPESEARCH_SORT_AUTHOR_INC))
	?"sortedUp"
	:(sort.equals(CoPESearchNode.COPESEARCH_SORT_AUTHOR_DEC)
			?"sortedDown"
			:"sortedNot"
		);
	style_year = (sort.equals(CoPESearchNode.COPESEARCH_SORT_YEAR_INC))
	?"sortedUp"
	:(sort.equals(CoPESearchNode.COPESEARCH_SORT_YEAR_DEC)
			?"sortedDown"
			:"sortedNot"
		);
	style_title = (sort.equals(CoPESearchNode.COPESEARCH_SORT_TITLE_INC))
	?"sortedUp"
	:(sort.equals(CoPESearchNode.COPESEARCH_SORT_TITLE_DEC)
			?"sortedDown"
			:"sortedNot"
		);
%>
	<script type="text/javascript">
		function clearFilters(key)
		{
			document.getElementById('<%=CoPESearchNode.COPESEARCH_FILTER_YEAR%>').value = "";
			document.getElementById('<%=CoPESearchNode.COPESEARCH_FILTER_AUTHOR%>').value = "";
			document.getElementById('<%=CoPESearchNode.COPESEARCH_FILTER_TITLE%>').value = "";
			document.cope_filter.submit();
		}
		
		if (document.layers)
			document.captureEvents(Event.KEYPRESS);
		document.onkeypress = function (evt)
		{
			var key = 
			document.all ? event.keyCode :
			evt.which ? evt.which : evt.keyCode;
			if (key == 13) 
			document.cope_filter.submit();
		};
		
		function checkNumeric(node)
		{
			var val = node.value;
			for(var i=0;i<val.length;i++)
				if(val.charAt(i)<'0' || val.charAt(i)>'9')
				{
					if(i==(val.length-1))
					{
						node.value = val.substr(0,i);
					}
					else
					{
						node.value = val.substr(0,i)+val.substr(i+1,val.length-i-1);
					}
					alert('Only numbers are allowed for this parameter!');
				}
		}
	</script>
	<style type="text/css">
		.table {
			font-family:sans-serif;
			font-size:0.9em;
			width:100%;
			border:1px solid #CCCCCCC;
			border-collapse:collapse;
			cursor:default;
		}
		.td {
			border-bottom:1px solid #DDDDDD;
			padding:2px;
			font-weight:normal;
		}
		.sortedNot {
			background-image:url(<%=request.getContextPath() + "/" +
					ClientDaemon.CONTEXT_ASSETS_PATH%>/ft-head.gif);
			background-repeat:no-repeat;
			background-position:top right;
			padding:4px;
			font-weight:normal;
		}
		.sortedUp {
			background-image:url(<%=request.getContextPath() + "/" +
					ClientDaemon.CONTEXT_ASSETS_PATH%>/ft-headup.gif);
			background-repeat:no-repeat;
			background-position:top right;
			padding:4px;
			font-weight:normal;
		}
		.sortedDown {
			background-image:url(<%=request.getContextPath() + "/" +
					ClientDaemon.CONTEXT_ASSETS_PATH%>/ft-headdown.gif);
			background-repeat:no-repeat;
			background-position:top right;
			padding:4px;
			font-weight:normal;
		}
	</style>

</head>
<body>
	<div>
		<form id="cope_filter" name="cope_filter" method="post" action="<%=CoPE_Search_url%>">
		<!-- Author -->
		<div style="display:inline;">
			<span class='pt_main_subheader_editing_name'>Author(s)</span>&nbsp;
			<span class="pt_main_subheader_editing_value2"><input id="<%=CoPESearchNode.COPESEARCH_FILTER_AUTHOR%>" name="<%=CoPESearchNode.COPESEARCH_FILTER_AUTHOR%>" 
					type="text" value="<%=filter_author%>" size="15" maxlength="15"></span>
		</div>&nbsp;&nbsp;
		<!-- Year -->
		<div style="display:inline;">
			<span class='pt_main_subheader_editing_name'>Year</span>&nbsp;
			<span class="pt_main_subheader_editing_value2"><input onKeyUp='checkNumeric(this);' id="<%=CoPESearchNode.COPESEARCH_FILTER_YEAR%>" name="<%=CoPESearchNode.COPESEARCH_FILTER_YEAR%>" 
					type="text" value="<%=filter_year%>" size="6" maxlength="4"></span>
		</div>&nbsp;&nbsp;
		<!-- Title -->
		<div style="display:inline;">
			<span class='pt_main_subheader_editing_name'>Title</span>&nbsp;
			<span class="pt_main_subheader_editing_value2"><input id="<%=CoPESearchNode.COPESEARCH_FILTER_TITLE%>" name="<%=CoPESearchNode.COPESEARCH_FILTER_TITLE%>" 
					type="text" value="<%=filter_title%>" size="30" maxlength="30"></span>
		</div>
		<!-- Sort (hidden) -->
		<input type="hidden" id="<%=CoPESearchNode.COPESEARCH_SORT%>" name ="<%=CoPESearchNode.COPESEARCH_SORT%>" value="<%=sort%>">
		</form>
		<p />
		<a class='pt_main_edit_button_ok' href='javascript:document.cope_filter.submit();'>Search</a>&nbsp;&nbsp;
		<a class='pt_main_edit_button_cancel' href='javascript:clearFilters();'>Reset</a>
	</div>

	<table class="table" cellpadding="0" cellspacing="0" border="0">
	<thead>
		<tr>
			<th class="<%=style_author%>" field="Authors" dataType="String" valign="top" onclick="document.cope_filter.<%=CoPESearchNode.COPESEARCH_SORT%>.value='<%=sort_author%>';document.location='<%=sorl_url_prefix+sort_author%>';" >Author(s)</th>
			<th class="<%=style_year%>" field="Year" dataType="Number" align="center" valign="top" onclick="document.cope_filter.<%=CoPESearchNode.COPESEARCH_SORT%>.value='<%=sort_year%>';document.location='<%=sorl_url_prefix+sort_year%>';">Year</th>
			<th class="<%=style_title%>" field="Title" dataType="html" valign="top" onclick="document.cope_filter.<%=CoPESearchNode.COPESEARCH_SORT%>.value='<%=sort_title%>';document.location='<%=sorl_url_prefix+sort_title%>';">Title</th>
		</tr>
	</thead>
	<tbody>
		<%
			Item2Vector<Paper> selected_papers = new Item2Vector<Paper>();
			for(int i=0; i<papers.size(); i++)
			{
				boolean show = true;
				if((filter_year!= null) && (filter_year != "") 
				&& Integer.parseInt(filter_year) != papers.get(i).getYear())
				{
					show = false;
				}

				if((filter_author!= null) && (filter_author != "") 
				&& papers.get(i).getAuthors().toLowerCase().indexOf(filter_author.toLowerCase())<0 )
				{
					show = false;
				}
				
				if((filter_title!= null) && (filter_title != "") 
				&& papers.get(i).getTitle().toLowerCase().indexOf(filter_title.toLowerCase())<0 )
				{
					show = false;
				}
				

				if(show)
				{
					selected_papers.add(papers.get(i));
				}
			}
			
			// sorting
			if(sort.equals(CoPESearchNode.COPESEARCH_SORT_AUTHOR_INC))
			{
				Collections.sort(selected_papers, CoPESearchNode.cmpAuthorInc);
			}
			else if(sort.equals(CoPESearchNode.COPESEARCH_SORT_AUTHOR_DEC))
			{
				Collections.sort(selected_papers, CoPESearchNode.cmpAuthorDec);
			}
			else if(sort.equals(CoPESearchNode.COPESEARCH_SORT_YEAR_INC))
			{
				Collections.sort(selected_papers, CoPESearchNode.cmpYearInc);
			}
			else if(sort.equals(CoPESearchNode.COPESEARCH_SORT_YEAR_DEC))
			{
				Collections.sort(selected_papers, CoPESearchNode.cmpYearDec);
			}
			else if(sort.equals(CoPESearchNode.COPESEARCH_SORT_TITLE_INC))
			{
				Collections.sort(selected_papers, CoPESearchNode.cmpTitleInc);
			}
			else if(sort.equals(CoPESearchNode.COPESEARCH_SORT_TITLE_DEC))
			{
				Collections.sort(selected_papers, CoPESearchNode.cmpTitleDec);
			}
			
			for(int i=0; i<selected_papers.size(); i++)
			{
		//		out.print("<tr value=\"" + (i+1) + "\"><td>");
		//		resmap.displayPaperTableView(

				String url = request.getContextPath() + "/content/Show?id=" +
				selected_papers.get(i).getOwners().get(0).getId();
				out.print("<tr><td class='td'>");
				out.print(selected_papers.get(i).getAuthors() + "</td><td class='td'>");
				out.print(selected_papers.get(i).getYear() + "</td><td class='plain'>");
				
				String other_folders = "";
				if(selected_papers.get(i).getOwners().size()>1)
				{
					int count = 0;
					for(int j=0; j<selected_papers.get(i).getOwners().size(); j++)
					{
				
				other_folders += ((other_folders.length()>0)?"&lt;br/&gt;":"") + "" + (++count) + ". &lt;a href=" +
						request.getContextPath() + "/content/Show?id=" +
						selected_papers.get(i).getOwners().get(j).getId() + " target=_top&gt;" +
						selected_papers.get(i).getOwners().get(j).getParent().getTitle() +
						"&lt;/a&gt;";
					}
					out.print("<a href='javascript:void(0);' onmouseover=\"this.T_TITLE='Paper can be found in these folders'; this.T_STICKY=true; this.T_PADDING=5; return escape('" +
					other_folders + "')\">" + selected_papers.get(i).getTitle() + "</a></td></tr>");
				}
				else
				{
					out.print("<a href='" + url + "' target='_top'>");
					out.println(selected_papers.get(i).getTitle() + "</a></td></tr>");
				}
			}
		%>
	</tbody>
	</table>
	<p />
	<div class="pt_main_subheader_editing_value2"><%=selected_papers.size()%> paper(s) selected</div>
	<script src='<%=request.getContextPath() + "/" + ClientDaemon.CONTEXT_ASSETS_PATH%>/wz_tooltip.js' type='text/javascript'></script>
	
</body>
</html>