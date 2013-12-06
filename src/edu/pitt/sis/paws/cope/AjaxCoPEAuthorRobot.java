package edu.pitt.sis.paws.cope;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.pitt.sis.paws.core.ItemVector;

import java.io.*;

/**
 * Servlet implementation class for Servlet: AjaxCoPEAuthorRobot
 *
 */
 public class AjaxCoPEAuthorRobot extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet
 {
	// Constants 
	static final long serialVersionUID = -2L;
	public static final String PARAM_TYPING = "typing";
	public static final String AUTHOR_INDEX = "idx";
	
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public AjaxCoPEAuthorRobot() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
//		ClientDaemon cd = ClientDaemon.getInstance();
		PrintWriter out = response.getWriter();
		
		HttpSession session = request.getSession();
		ResourceMap resmap = (ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP);
		String typing = request.getParameter(PARAM_TYPING);
		String auth_index = request.getParameter(AUTHOR_INDEX);
		
		if(resmap != null)
		{
			ItemVector<Author> authors = resmap.getAuthors();
			out.println("<table bgcolor='white' style='border:1px solid darkGrey;' width='100%' cellpadding='2' cellspacing='0'>");
			int count = 0;
			for(int i=0; i<authors.size(); i++)
			{
				if( (authors.get(i).getLastName().toLowerCase().indexOf(typing.toLowerCase()) >= 0) || 
						(authors.get(i).getGivenNames().toLowerCase().indexOf(typing.toLowerCase()) >= 0) )
				{
					out.println("<tr onselect=\"this.text.value = '" + authors.get(i).getLastName() + "';"+
							"$('" + Paper.PAPER_FRMFIELD_AUTHOR_GIVENNAMES + auth_index + "').value='" + authors.get(i).getGivenNames() + "';" +
							"$('" + Paper.PAPER_FRMFIELD_AUTHOR_ID + auth_index + "').value='" + authors.get(i).getId() + "';" +
							"$('" + Paper.PAPER_FRMFIELD_AUTHOR_STATUS + auth_index + "').src='" + request.getContextPath() + ClientDaemon.CONTEXT_ASSETS_PATH + "/check.gif';" +
							" \">");
					
					//onSelect=" this.text.value = 'Tommy'; $('stdID').value = '001'; "
					out.println("<td><b>" + (++count) + "</b></td>");
					out.println("<td>" + authors.get(i).getLastName() + ", " + authors.get(i).getGivenNames() + "</td>");
					out.println("</tr>");
				}
			}
			out.println("</table>");
		}
		
		/*Connection conn = null;
		String qry = "";
		try
		{
			conn = cd.getSQLManager().getConnection();

			String typing = request.getParameter(PARAM_TYPING);
			
			if( (typing != null) && (typing.length() > 1))
			{
				qry = "SELECT Authors FROM ent_cope_paper WHERE Authors LIKE '%" + typing + "%';";
				PreparedStatement statement = conn.prepareStatement(qry);
				ResultSet rs = SQLManager.executeStatement(statement);
				out.println("<table width='100%' cellpadding='2' cellspacing='0'>");
				int count = 0;
				while(rs.next())
				{
					String authors = rs.getString("Authors");
					out.println("<tr onselect=\"this.text.value = '" + authors + "';\">");
					out.println("<td><b>" + (++count) + "</b></td>");
					out.println("<td>" + authors + "</td>");
					out.println("</tr>");
				}
				out.println("</table>");
			}
		}
		catch (Exception e) { e.printStackTrace(System.out); }
		finally { cd.getSQLManager().freeConnection(conn); }/**/
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}   	  	    
}