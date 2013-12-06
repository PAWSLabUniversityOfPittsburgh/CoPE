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

import java.util.*;
import java.sql.*;
import javax.servlet.http.*;

public abstract class NodeProperty implements iHTMLRepresentable
{
	//Constants
	
	/** Session parameter - fixes a certain property to be loaded every next
	 * time once activated previously (and if available)
	 */
	public static final String SESSION_NODE_PROPERTY_TYPE = "property_type";
	
	/** Property type: comment/summary, or concepts, etc. If none specified
	 * comment/summary is assumed by default
	 */
	public static final String REQUEST_NODE_PROPERTY_TYPE = "property_type";
	public static final int REQUEST_NODE_PROPERTY_TYPE_COMMENT = 1;
	public static final int REQUEST_NODE_PROPERTY_TYPE_CONCEPTS = 2;

	/** Request parameter, denotes in which mode the property is opened
	 * (view, edit, add, or submit) If none specified then view is assumed.
	 * Submit mode means that first the property value should be saved in
	 * database and then viewed.
	 */
	public static final String REQUEST_NODE_PROPERTY_MODE = "property_mode";
	public static final int REQUEST_NODE_PROPERTY_MODE_VIEW = 0;//or null
	public static final int REQUEST_NODE_PROPERTY_MODE_EDIT = 1;
	public static final int REQUEST_NODE_PROPERTY_MODE_ADD = 2;
	public static final int REQUEST_NODE_PROPERTY_MODE_SUBMIT = 3;

	/** Request parameter, denotes the Id if the property value currently
	 * being considered.
	 */
	public static final String REQUEST_NODE_PROPERTY_ID = "property_id";

	public static final String FORM_NODE_PROPERTY_TYPE = "fld_property_type";
	public static final String FORM_NODE_PROPERTY_ID = "fld_property_id";
	public static final String FORM_NODE_PROPERTY_COMMENT_TEXT = "fld_property_comment_text";

	
	protected iNode node;
	protected Vector<iNodePropertyValue> values;
		
	private NodeProperty()
	{
		node = null;
		values = new Vector<iNodePropertyValue>();
	}
	
	public NodeProperty(iNode _node)
	{
		node = _node;
		values = new Vector<iNodePropertyValue>();
	}
	
	public abstract NodeProperty clone(iNode _parent_node, User _user);
	
	public Vector<iNodePropertyValue> getValues() { return values; }

	//-----
	/** returns true if user identified by a user_id has submitted a value for
	 * this property
	 * @param user_id - the user id to be checked
	 */
	protected boolean hasUserValue(int user_id)
	{
		boolean result = false;
		for(int i=0; i<values.size(); i++)
		{
			if( ((iNodePropertyValue)values.get(i)).getUser().getId() 
				== user_id )
			{
				result = true;
				break;
			}
		}
		return result;
	}
	
	/** returns true if user identified by a user_id has submitted a value for
	 * this property
	 * @param user_id - the user id to be checked
	 */
	protected iNodePropertyValue getUserValue(int user_id)
	{
		iNodePropertyValue result = null;
		for(int i=0; i<values.size(); i++)
		{
			if( ((iNodePropertyValue)values.get(i)).getUser().getId() 
				== user_id )
			{
				result = (iNodePropertyValue)values.get(i);
				break;
			}
		}
		return result;
	}
	
	public abstract void saveToDB(Connection conn, HttpServletRequest request)
		throws Exception;
	
}