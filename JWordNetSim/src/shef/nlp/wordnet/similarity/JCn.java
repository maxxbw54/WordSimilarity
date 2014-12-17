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

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Synset;

/**
 * An implementation of the WordNet similarity measure developed by Jiang and
 * Conrath. For full details of the measure see:
 * <blockquote>Jiang J. and Conrath D. 1997. Semantic similarity based on corpus
 * statistics and lexical taxonomy. In Proceedings of International
 * Conference on Research in Computational Linguistics, Taiwan.</blockquote>
 * @author Mark A. Greenwood
 */
public class JCn extends ICMeasure
{
	/**
	 * Instances of this similarity measure should be generated using the
	 * factory methods of {@link SimilarityMeasure}.
	 */
	protected JCn()
	{
		//A protected constructor to force the use of the newInstance method
	}
	
	@Override public double getSimilarity(Synset s1, Synset s2) throws JWNLException
	{
		//if the POS tags are not the same then return 0 as this measure
		//only works with 2 nouns or 2 verbs.
		if (!s1.getPOS().equals(s2.getPOS())) return 0;
		
		//see if the similarity is already cached and...
		Double cached = getFromCache(s1, s2);
		
		//if it is then simply return it
		if (cached != null) return cached.doubleValue();
		
		//Get the Information Content (IC) values for the two supplied synsets
		double ic1 = getIC(s1);
		double ic2 = getIC(s2);

		//if either IC value is zero then cache and return a sim of 0
		if (ic1 == 0 || ic2 == 0) return addToCache(s1,s2,0);
		
		//Get the Lowest Common Subsumer (LCS) of the two synsets
		Synset lcs = getLCSbyIC(s1,s2);
		
		//if there isn't an LCS then cache and return a sim of 0
		if (lcs == null) return addToCache(s1,s2,0);
		
		//get the IC valueof the LCS
		double icLCS = getIC(lcs);
		
		//compute the distance between the two synsets
		//NOTE: This is the original JCN measure
		double distance = ic1 + ic2 - (2 * icLCS);
		
		//assume the similarity between the synsets is 0
		double sim = 0;
		
		if (distance == 0)
		{
			//if the distance is 0 (i.e. ic1 + ic2 = 2 * icLCS) then...
			
			//get the root frequency for this POS tag
			double rootFreq = getFrequency(s1.getPOS());
			
			if (rootFreq > 0.01)
			{
				//if the root frequency has a value then use it to generate a
				//very large sim value
				sim = 1/-Math.log((rootFreq - 0.01) / rootFreq);
		    }		    
		}
		else
		{
			//this is the normal case so just convert the distance
			//to a similarity by taking the multiplicative inverse
			sim = 1/distance;
		}
		
		//cache and return the calculated similarity
		return addToCache(s1,s2,sim);
	}
}
