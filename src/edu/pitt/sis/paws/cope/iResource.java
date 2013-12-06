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

import edu.pitt.sis.paws.core.*;

/** This class is a wrapper to any object that a Node may be emulating.
 * If Node does emulate some other object (e.g. Paper in CoPE) an instance
 * implmenenting this interface is attached to a Node instance
 */
public interface iResource extends iItem2, iHTMLRepresentable, 
	iDBStored, iAuthored
{
	public ItemVector<iNode> getOwners();

	// each restource can be rated

	/** Method determines whether the rating of the item should be shown before item itself or after
	 * @return true if ration sjould be showm before, false otherwise
	 * @since 1.5
	 */
	public boolean isRatingShownBefore();

}