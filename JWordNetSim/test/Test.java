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
package JWordNetSim.test;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import shef.nlp.wordnet.similarity.SimilarityMeasure;

/**
 * A simple test of this WordNet similarity library.
 * @author Mark A. Greenwood
 */
public class Test
{
	public static void main(String[] args) throws Exception
	{	
		//Initialize WordNet - this must be done before you try
		//and create a similarity measure otherwise nasty things
		//might happen!
		JWNL.initialize(new FileInputStream("D:\\JAVAProjectWorkSpace\\jwnl\\JWordNetSim\\test\\wordnet.xml"));
		
		//Create a map to hold the similarity config params
		Map<String,String> params = new HashMap<String,String>();
		
		//the simType parameter is the class name of the measure to use
		params.put("simType","shef.nlp.wordnet.similarity.JCn");
		
		//this param should be the URL to an infocontent file (if required
		//by the similarity measure being loaded)
		params.put("infocontent","file:D:\\JAVAProjectWorkSpace\\jwnl\\JWordNetSim\\test\\ic-bnc-resnik-add1.dat");
		
		//this param should be the URL to a mapping file if the
		//user needs to make synset mappings
		params.put("mapping","file:D:\\JAVAProjectWorkSpace\\jwnl\\JWordNetSim\\test\\domain_independent.txt");
		
		//create the similarity measure
		SimilarityMeasure sim = SimilarityMeasure.newInstance(params);
		
		//Get two words from WordNet
		Dictionary dict = Dictionary.getInstance();		
		IndexWord word1 = dict.getIndexWord(POS.NOUN, "dog");
		IndexWord word2 = dict.getIndexWord(POS.NOUN,"cat");
		
		//and get the similarity between the first senses of each word
		System.out.println(word1.getLemma()+"#"+word1.getPOS().getKey()+"#1  " + word2.getLemma()+"#"+word2.getPOS().getKey()+"#1  " + sim.getSimilarity(word1.getSense(1), word2.getSense(1)));		
		
		//get similarity using the string methods (note this also makes use
		//of the fake root node)
		System.out.println(sim.getSimilarity("cat#n","cat#n"));
		
		//get a similarity that involves a mapping
		System.out.println(sim.getSimilarity("namperson", "namperson"));
	}
}
