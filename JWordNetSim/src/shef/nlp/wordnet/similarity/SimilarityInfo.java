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
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * Simple encapsualtion of the similarity between two synsets.
 * @author Mark A. Greenwood
 */
public class SimilarityInfo
{
	private Synset s1, s2;
	private IndexWord iw1, iw2;
	private double sim = 0;
	private String d1, d2;
	
	/**
	 * @param w1 the first word (or it's encoded form)
	 * @param s1 the first synset
	 * @param w2 the second word (or it's encoded form)
	 * @param s2 the second synset
	 * @param sim the similarity between the two synsets
	 * @throws JWNLException
	 */
	protected SimilarityInfo(String w1, Synset s1, String w2, Synset s2, double sim) throws JWNLException
	{
		//store the synsets and the similarity between them
		this.s1 = s1;
		this.s2 = s2;
		this.sim = sim;
		
		//The following is just for display purposes and as this class
		//is immutable we just generate this stuff once
		
		//get access to WordNet
		Dictionary dict = Dictionary.getInstance();
		
		//get the two index words
		iw1 = dict.getIndexWord(s1.getPOS(), w1.split("#")[0]);
		iw2 = dict.getIndexWord(s2.getPOS(), w2.split("#")[0]);
		
		//build the descriptions of the two words
		d1 = (iw1 == null ? w1 : iw1.getLemma()+"#"+s1.getPOS().getKey()+"#"+getSenseNumber(iw1,s1));
		d2 = (iw2 == null ? w2 : iw2.getLemma()+"#"+s2.getPOS().getKey()+"#"+getSenseNumber(iw2,s2));
	}
	
	/**
	 * Given an index word and synset works out which sense index we are looking at
	 * @param iw the index word
	 * @param s a synset that includes the index word
	 * @return the sense indes of the sysnet for the given word,
	 *         or -1 if the word is not in the synset
	 * @throws JWNLException if an error occurs accessing WordNet
	 */
	private static final int getSenseNumber(IndexWord iw, Synset s) throws JWNLException
	{
		//get all the senses of the word
		Synset[] senses = iw.getSenses();
		
		for (int i = 0 ; i < senses.length ; ++i)
		{
			//if the sense we are looking at is the one we
			//want then return it's index
			if (senses[i].equals(s)) return (i+1);
		}
		
		//we didn't find the sense so return -1 to denote failure
		return -1;
	}
	
	/**
	 * Get the first synset used to compute similarity
	 * @return the first synset used to compute similarity.
	 */
	public Synset getSynset1() { return s1; }

	/**
	 * Get the second synset used to compute similarity
	 * @return the second synset used to compute similarity.
	 */
	public Synset getSynset2() { return s2; }
	
	/**
	 * Get the similarity between the two synsets
	 * @return the similarity between the two synsets.
	 */
	public double getSimilarity() { return sim; }
	
	@Override public String toString()
	{
		return d1+"  "+d2+"  "+sim;
	}
}
