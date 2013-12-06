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

/** This class is a wrapper to a apecific  object that a Node may be emulating.
 * Namely a Resource which is deployed elsewhere that Node refers to as an 
 * external entity */
public interface iExternalResource extends iResource
{
	/**Getter for arbitraty parameters (to be defined by a detminal class)
	 * @return - arbitraty parameters */
//	public String getParameters(); 
	/**Setter for arbitraty parameters (to be defined by a detminal class)
	 * @param _parameters - value of arbitrary parameters */
//	public void setParameters(String _parameters);
	/** Getter for an external URL 
	 * @return - an external URL */
	public String getURL();
	/** Setter for an external URL 
	 * @param _url - an external URL */
	public void setURL(String _url);

	/** Cloning method 
	 * @return - a copy of the iExternalResource object */
	public iExternalResource clone();
}
