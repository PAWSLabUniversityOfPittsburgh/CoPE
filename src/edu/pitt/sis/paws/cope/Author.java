package edu.pitt.sis.paws.cope;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import edu.pitt.sis.paws.core.Item2Vector;
import edu.pitt.sis.paws.core.utils.SQLManager;

public class Author extends Resource
{
	protected static String URI_PREFIX = "author";
	//protected String last_name - use Title for that
	protected String given_names;

	protected Item2Vector<Paper> authored_paper;
	
	public Author()
	{
		super(0, "", null);
		given_names = null;
		authored_paper = new Item2Vector<Paper>();
	}
	
	public Author(int _id, String _title, String _uri, String _given_names)
	{
		super(_id, _title, _uri);
		given_names = _given_names;
		authored_paper = new Item2Vector<Paper>();
	}
	
	public Author clone()
	{
		Author copy = new Author(this.getId(), new String(this.getLastName()), new String(this.getURI()),
				new String (this.getGivenNames()));
		return copy; 
	}
	
	public String getLastName() { return getTitle(); }
	public void setLastName(String _last_name) { setTitle(_last_name); }

	public String getGivenNames() { return given_names; }
	public void setGivenNames(String given_names) { this.given_names = given_names; }

	public Item2Vector<Paper> getPapers() { return authored_paper; } 

	public void showEdit(JspWriter out, HttpServletRequest request, 
			String cancel_to_url) throws IOException
	{
		return;
	}
	
	public void showView(JspWriter out, HttpServletRequest request, boolean show_ratings)
		throws IOException
	{
		return;
	}

	public void saveToDB(Connection conn, HttpServletRequest request, iNode node,
			int changes) throws Exception
	{
		return;
	}

	public int updateObject(HttpServletRequest request) throws Exception
	{
		return iNode.NODE_CHANGE_NONE;
	}
	
	public int addToDB(Connection conn, HttpServletRequest request, iNode node)
			throws Exception
	{
		int author_id = 0;
		HttpSession session = request.getSession();
		ResourceMap resmap = ((ResourceMap)session.getAttribute(ClientDaemon.SESSION_RES_MAP));

		if(this.isStoredInDB())
		{
//			int user_id = ((Integer)session.getAttribute(ClientDaemon.SESSION_USER_ID)).intValue();

			String qry = "INSERT INTO ent_cope_author (LastName, GivenNames) "+
					"VALUES ('" + 
					SQLManager.stringUnquote(SQLManager.stringUnnull(this.getLastName())) + "', '" + 
					SQLManager.stringUnquote(SQLManager.stringUnnull(this.getGivenNames())) + "');";
			
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(qry);
			stmt.close();
			stmt = null;

			qry = "SELECT MAX(LAST_INSERT_ID(AuthorID)) AS LastID FROM ent_cope_author"+
					" WHERE LastName='" + SQLManager.stringUnquote(SQLManager.stringUnnull(this.getLastName())) + "';";
			
			PreparedStatement statement = conn.prepareStatement(qry);
			ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				author_id = rs.getInt("LastID");
			}
			rs.close();
			rs = null;
			statement .close();
			statement = null;

		}

		// add the author with correct id
		this.setId(author_id);
		
//		resmap.getAuthors().remove(this);
		resmap.getAuthors().add(this);
		
		session.setAttribute(ClientDaemon.SESSION_RES_MAP, resmap);
		
		return author_id;
	}
	
}
