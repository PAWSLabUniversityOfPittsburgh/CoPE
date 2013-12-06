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

//import edu.pitt.sis.paws.core.*;

public abstract class ExternalResource extends Resource implements iExternalResource
{
	/** URL of the external resource*/
	protected String url;

	/** ExtID of the external resource*/
	protected String ext_id;

	/** Constructor for cloning - creator will be set to null
	 * @param _id - object Id
	 * @param _title - object Title
	 * @param _url - external url of the resource
	 */
	public ExternalResource(int _id, String _title, String _uri, String _url, String _ext_id)
	{
		super(_id, _title, _uri);
		url = _url;
		ext_id = _ext_id;
	}
	
	/** Main constructor for ExternalResource
	 * @param _id - object Id
	 * @param _title - object Title
	 * @param _url - external url of the resource
	 * @param _user - creator
	 */
	public ExternalResource(int _id, String _title, String _uri, User _user, String _url, String _ext_id)
	{
		super(_id, _title, _uri, _user);
		url = _url;
		ext_id = _ext_id;
	}

	/** Getter for an external URL 
	 * @return - an external URL */
	public String getURL() { return url; }

	/** Setter for an external URL 
	 * @param _url - an external URL */
	public void setURL(String _url) { url = _url; }

	/** Cloning method 
	 * @return - a copy of the ExternalResource object */
	public abstract iExternalResource clone();

	public String getExtID() { return ext_id; }
	
}
