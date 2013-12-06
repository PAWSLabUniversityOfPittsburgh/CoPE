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

import java.io.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import edu.pitt.sis.paws.core.*;

public class Concept extends Item implements iConcept
{
	protected iConcept parent;
	protected OrderedWeightedItemVector<iConcept> children;

	protected boolean expanded = false; // property for tree visualization

	public Concept(int _id, String _title)
	{
		super(_id, _title);
		parent = null;
		children = new OrderedWeightedItemVector<iConcept>();
	}

	public iConcept clone() 
	{
		Concept copy = null;
		try
		{
			copy = new Concept(this.getId(), new String(this.getTitle())); 
		}
		catch (Exception e) { System.err.println(e.toString()); }
		return copy;
	}

	public void setParent(iConcept _parent) { parent = _parent; }
	public iConcept getParent() { return parent; }

	public int getChildCountOfTypeByUser(int _node_type, int _user_id)
	{
		int result = 0;
		return result;
	}

	// Children property
	public OrderedWeightedItemVector<iConcept> getChildren() { return children; }
	
	// Expanded property
	public boolean getExpanded() { return expanded; }
	public void setExpanded(boolean _expanded) {  expanded = _expanded; }
	
	/** Method recursively checks whether all the Nodes the parent-child path 
	 * to the Node that initialized the check are expanded. If not the 
	 * expanded parameter is set ot true.
	 * @param   _res   this parameter has to be set to false in an initial
	 *	call to the function
	 * @return  boolean value of whether any expanded parameters have been
	 * altered
	 */ 
	public boolean expandParents(boolean _res)
	{
		boolean res = _res;
		
		if(parent != null)
		{
			if(!parent.getExpanded())
			{
				parent.setExpanded(true);
				res = true;
			}
			parent.expandParents(res);
		}
		/* Stub for multiparent version
		for(int i=0;i<sup_modules.Count();i++)
		{
			if(!sup_modules.At(i).expanded)
			{
				sup_modules.At(i).expanded = true;
				res = true;
			}
			sup_modules.At(i).expandParents(res);
		}/**/
		return res;
	}
	
	public ItemVector<iConcept> copyConceptTree(iConcept parent, 
		ItemVector<iConcept> vector_copy)
	{
		boolean initial = (vector_copy == null) && (parent == null);
//System.out.println("Concept.copyConceptTree Concept#=" + this.getId() + " initial="+initial);		
		
		ItemVector<iConcept> result = (!initial) ? vector_copy : 
			new ItemVector<iConcept>();
		
		iConcept this_copy = this.clone();
//System.out.println("Concept.copyConceptTree (this_copy==null)=" + (this_copy==null));		
//System.out.println("Concept.copyConceptTree (result_vector_copy==null)=" + (result==null));		
		result.addNew(this_copy);
		if(!initial)
		{
			iConcept this_parent = result.findById(parent.getId());
			this_parent.getChildren().addNew(this_copy);
			this_copy.setParent(this_parent);
		}
		
		for(int i=0; i< this.getChildren().size(); i++)
		{
			result = this.getChildren().get(i).copyConceptTree(this, 
				result);
		}
		
		return result;
	}
	

	public void outputTreeConcept(JspWriter out, HttpServletRequest req,
		ItemVector<iConcept> chosen_concepts, int level, 
		boolean show_checkbox, String jsActions) throws IOException
	{
//		int user_id = ((Integer)req.getSession().getAttribute(AppDaemon.SESSION_USER_ID)).intValue();
		out.print("<div class='pt_tree_left_opener_div'>");//<a name='" +  +"'></a>
		//display empty spaces before
		for(int i=0;i<level;i++)
			out.print("<IMG SRC='" + req.getContextPath() +
				ClientDaemon.CONTEXT_ASSETS_PATH +
				"/dir_empty.gif' alt='.    ' border=0 " + 
				"style='display:inline'/>");
		//display own opener or empty if no subs
		if(this.getChildren().size()>0)
		{
			out.print("<A name='concept" + this.getId() + "' href='#concept" + this.getId() + "'" + 
				" onClick='flip_dir_icon(this);'" +			
//			req.getContextPath() +	"/content/jspLeft?" + AppDaemon.REQUEST_EXPANDED +"=" + concept.getId() + "#concept" + concept.getId() + "'"+
				" class='pt_tree_left_opener_bullet'>");
			//onClick='flip_dir_icon(this);'
			if(this.getExpanded())
			{
				out.print("<IMG SRC='" + req.getContextPath() +
					ClientDaemon.CONTEXT_ASSETS_PATH +
					"/dir_minus.gif' alt='[-]' border=0 " +
					"style='display:inline'>");
				out.print("<IMG SRC='" + req.getContextPath() +
					ClientDaemon.CONTEXT_ASSETS_PATH +
					"/dir_plus.gif' alt='[+]' border=0 " +
					"style='display:none'>");
			}
			else
			{
				out.print("<IMG SRC='" + req.getContextPath() +
					ClientDaemon.CONTEXT_ASSETS_PATH +
					"/dir_minus.gif' alt='[-]' border=0 " +
					"style='display:none'>");
				out.print("<IMG SRC='" + req.getContextPath() +
					ClientDaemon.CONTEXT_ASSETS_PATH +
					"/dir_plus.gif' alt='[+]' border=0 " +
					"style='display:inline'>");
			}
			out.print("</A>");
		}
		else
		{// Document (default text)
			out.print("<A name='node" + this.getId() + "' border=0 style='display:inline'><IMG SRC='" +
				req.getContextPath() +
				ClientDaemon.CONTEXT_ASSETS_PATH + "/dir_empbull.gif'" +
				" alt='&curren;' border=0 style='display:inline'/></A>");
		}
		//Show checkbox or not
		if(show_checkbox)
		{
			// checl whether the concept is in the chosen or not
			String checked = (chosen_concepts.findById(this.getId())!= null)?" checked":"";
			out.print("<input type='checkbox' name='concept" + this.getId() + "' value='concept" + this.getId() + "'" + checked + " " + jsActions + ">");//checked
		}
		else
		{
			out.print("&nbsp;");
		}
		//Show title
		String style = "pt_tree_left_item_no_link";

		out.print("<a" + 
//			((concept.isAuthoredBy(user_id))?" style='font-weight:bold;color:#006600;'":"") +
			" class='" + style + "' title='" + this.getTitle() + "'" + 
//			" href='" +	req.getContextPath() + "/content/Show?" + AppDaemon.REQUEST_NODE_ID + "=" + concept.getId() + "'" +
			" target='_top'>" + this.getTitle() + "</a>");
		//end of self
		out.print("</div>");// end if 'self' <div>
		
		//beginning of children
		if(this.getChildren().size()>0)
			out.println("<div style='display:" +
				((this.getExpanded())?"block":"none")+ "'>");
		//SHOW
		for(int i=0;i<this.getChildren().size();i++)
			this.getChildren().get(i).outputTreeConcept(out, req, 
				chosen_concepts, level+1, show_checkbox, jsActions);
		//end of children
		if(this.getChildren().size()>0) out.println("</div>");
	}
	
}