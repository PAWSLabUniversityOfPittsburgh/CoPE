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

/** This interface wrapps the functionality of an class that can have a
 * property if bening authored
 */
public interface iAuthored
{
	public boolean isCreatedBy(int user_id);
	public boolean isCreatedBy(User _user);
	public User getCreator();
	public void setCreator(User _user);
	public String getCreatorAdderNames();
	
	/** Method returns 5 rating labels for the item that might vary from item type to item type.
	 * @return 5 rating labels for 5 stars
	 * @since 1.5
	 */
	public String getRatingLabels();

	/** Method returns 5 rating labels as an array for the item that might vary from item type to item type.
	 * @return 5 rating labels for 5 stars
	 * @since 1.5
	 */
	public String[] getRatingLabelsArray();


}