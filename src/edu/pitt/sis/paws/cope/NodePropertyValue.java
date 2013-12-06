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

public abstract class NodePropertyValue implements iNodePropertyValue
{
	protected final int id;
	protected User creator;
	protected NodeProperty property;

	protected NodePropertyValue(int _id)
	{
		id = _id;
		creator = null;
	}

	protected NodePropertyValue(int _id, User _creator)
	{
		id = _id;
		creator = _creator;
	}

	public NodePropertyValue(int _id, User _user, NodeProperty _prop)
	{
		id = _id;
		creator = _user;
		property = _prop;
	}

	public abstract iNodePropertyValue clone(User _user);

	public int getId() { return  id; }

	public User getUser() { return creator; }
	public void setUser(User _user) { creator = _user; }
}