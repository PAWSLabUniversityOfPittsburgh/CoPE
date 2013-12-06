/* Disclaimer:
 * 	Java code contained in this file is created as part of educational
 *    research and development. It is intended to be used by researchers of
 *    University of Pittsburgh, School of Information Sciences ONLY.
 *    You assume full responsibility and risk of lossed resulting from compiling
 *    and running this code.
 */
 
/** Interface in intended to wrap all of the entities that can be used as a node
 * in a portal tree: folders, untyped nodes, and nodes with special types.
 * @author Michael V. Yudelson
 */
 
package edu.pitt.sis.paws.cope;

import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.*;

public interface iConcept extends iHierarchicalItem<iConcept>
{
	//Constants
	public static final int CONCEPT_NODE_REL_ONTOLOGY = 1; 
	public static final int CONCEPT_NODE_REL_CONCEPT = 2; 
	
	public ItemVector<iConcept> copyConceptTree(iConcept parent, 
		ItemVector<iConcept> vector_copy);
	
	public void outputTreeConcept(JspWriter out, HttpServletRequest req,
		ItemVector<iConcept> chosen_concepts, int level, 
		boolean show_checkbox, String jsActions) throws IOException;
}