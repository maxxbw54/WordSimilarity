/************************************************************************
 *         Copyright (C) 2006-2007 The University of Sheffield          *
 *      Developed by Mark A. Greenwood <m.greenwood@dcs.shef.ac.uk>     *
 *                                                                      *
 * This program is free software; you can redistribute it and/or modify *
 * it under the terms of the GNU General Public License as published by *
 * the Free Software Foundation; either version 2 of the License, or    *
 * (at your option) any later version.                                  *
 *                                                                      *
 * This program is distributed in the hope that it will be useful,      *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of       *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
 * GNU General Public License for more details.                         *
 *                                                                      *
 * You should have received a copy of the GNU General Public License    *
 * along with this program; if not, write to the Free Software          *
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.            *
 ************************************************************************/

package shef.nlp.wordnet.similarity;

import java.util.List;
import java.util.Map;

import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;

/**
 * An abstract class that addes path based methods to the top level similarity
 * measure class but doesn't itself define a similarity measure.
 * @author Mark A. Greenwood
 */
public abstract class PathMeasure extends SimilarityMeasure
{
	/**
	 * If true then a fake root node is used to joing the multiple
	 * verb and noun hierarchies into one hierarchy per POS tag
	 */
	private boolean root = true;
	
	/**
	 * Should we use a siingle root node for each POS tag hierarchy
	 * @return true if we should use a single root node for each POS tag, false otherwise
	 */
	protected boolean useSingleRoot() { return root; }
	
	protected void config(Map<String,String> params) throws Exception
	{
		//A protected constructor to force the use of the newInstance method
		
		if (params.containsKey("root")) root = Boolean.parseBoolean(params.remove("root"));
	}
	
	/**
	 * Utility method to determine if the list of nodes contains a given synset
	 * @param l a list of nodes
	 * @param s a synset
	 * @return true if the synset is contained within the list of nodes, false otherwise
	 */
	protected static boolean contains(List<PointerTargetNode> l, Synset s)
	{
		for (PointerTargetNode node : l)
		{
			if (node.getSynset().equals(s)) return true;
		}
		
		return false;
	}
}
