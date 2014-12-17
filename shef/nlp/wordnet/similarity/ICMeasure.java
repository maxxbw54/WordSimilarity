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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;

/**
 * An abstract class that addes information content based methods to the
 * top level similarity measure class but doesn't itself define a
 * similarity measure.
 * @author Mark A. Greenwood
 */
public abstract class ICMeasure extends PathMeasure
{
	/**
	 * This map stores the synset IDs and there associated frequencies
	 * as read from the supplied information content file.
	 */
	private Map<String,Double> freq = new HashMap<String,Double>();
			
	protected void config(Map<String,String> params) throws Exception
	{
		super.config(params);
		
		//a handle to the infocontent file
		BufferedReader in = null;
		
		try
		{
			URL url = new URL(params.remove("infocontent"));
			
			//open the info content file for reading
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			
			//get the first line from the file (should be the WordNet version info)
			String line = in.readLine();
					
			//Check that what we have is actually a file of IC values
			if (line == null || !line.startsWith("wnver::")) throw new IOException("Malformed InfoContent file");
			
			//Check that the IC file is meant for use with the version
			//of WordNet we are currently using
			if (!line.endsWith("::"+JWNL.getVersion().getNumber())) throw new Exception("InfoContent file version doesn't match WordNet version");
			
			//Initially set the IC values of the noun and verb roots to 0
			freq.put("n",0d);
			freq.put("v",0d);
			
			//Get the first line of real data ready for use
			line = in.readLine();		
			
			while (line != null && !line.equals(""))
			{
				//while there is still data in the file to process...
				
				//split the line on the whitespace
				String[] data = line.split("\\s+");
				
				//store the frequency (2nd column) against the synset ID (1st column)
				freq.put(data[0],new Double(data[1]));
				
				if (data.length == 3 && data[2].equals("ROOT"))
				{
					//if there are three columns on this line and the
					//last one is ROOT then...
					
					//get the POS tag of the synset
					String pos = data[0].substring(data[0].length()-1);
					
					//updated the node frequency for the POS tag
					freq.put(pos, Double.parseDouble(data[1])+freq.get(pos));
				}
				
				//read in the next line from the file ready for processing
				line = in.readLine();
			}
		}
		finally
		{
			//if we managed to open the file then close it
			if (in != null) in.close();
		}
	}
	
	/**
	 * Generates the key to access the frequency count data loaded
	 * from the information content file.
	 * @param synset the synset for which to generate the key.
	 * @return the key to access the frequency count map.
	 */
	protected String getFreqKey(Synset synset)
	{
		//the keys used by the infomation content files are simply
		//the offsets in the wordnet database (minus leading zeros)
		//followed by the single character POS tag. So simply build
		//a key of this type...
				
		return synset.getOffset()+synset.getPOS().getKey();
	}
	
	/**
	 * Gets the Information Content (IC) value associated with the given synset.
	 * @param synset the synset for which to calcualte IC.
	 * @return the IC of the given synset.
	 */
	protected double getIC(Synset synset)
	{
		//get the POS tag of this synset
		POS pos = synset.getPOS();
		
		//Information Content is only defined for nouns and verbs
		//so return 0 if the POS tag is something else
		if (!pos.equals(POS.NOUN) && !pos.equals(POS.VERB)) return 0;
		
		//Get the frequency of this synset from the storred data
		Double synFreq = freq.get(getFreqKey(synset));
				
		//if the frequency isn't defined or it's 0 then simlpy return 0 
		if (synFreq == null || synFreq.doubleValue() == 0) return 0;
		
		//Get the frequency of the root node for this POS tage
		Double rootFreq = freq.get(synset.getPOS().getKey());
	
		//calcualte the probability for this synset
		double prob = synFreq.doubleValue() / rootFreq.doubleValue();
		
		//if the probability is valid then use it to return the IC value
		if (prob > 0) return -Math.log(prob);
		
		//something went wrong so assume IC of 0
		return 0;
	}
	
	/**
	 * Returns the frequency of the root node of the hierarchy for the
	 * given POS tag.
	 * @param pos the POS tag of the root node to access
	 * @return the frequency of the root node for the given POS tag
	 */
	protected double getFrequency(POS pos)
	{
		return freq.get(pos.getKey());
	}
	
	/**
	 * Returns the frequency of the given synset.
	 * @param synset the synset to retrieve the frequency of
	 * @return the frequency of the supplied synset
	 */
	protected double getFrequency(Synset synset)
	{
		Double f = freq.get(getFreqKey(synset));
		
		if (f == null || f.doubleValue() == 0) return 0;
		
		return f.doubleValue();
	}
	
	/**
	 * Finds the lowerst common subsumer of the two synsets using information content.
	 * @param s1 the first synset
	 * @param s2 the second synset
	 * @return the lowest common subsumer of the two provided synsets
	 * @throws JWNLException if an error occurs accessing WordNet
	 */
	protected Synset getLCSbyIC(Synset s1, Synset s2) throws JWNLException
	{
		//TODO Handle the different types of LCS handled by the perl version which are
		//   1) Largest IC value
		//   2) Results in shortest path
		//   3) Greatest depth (i.e. the LCS whose shortest path to root is longest)
		//Although in here we only need the IC based one
				
		@SuppressWarnings("unchecked")
		List<List<PointerTargetNode>> trees1 = PointerUtils.getInstance().getHypernymTree(s1).toList();
		
		@SuppressWarnings("unchecked")
		List<List<PointerTargetNode>> trees2 = PointerUtils.getInstance().getHypernymTree(s2).toList();
		
		Set<Synset> pLCS = new HashSet<Synset>();
		
		for (List<PointerTargetNode> t1 : trees1)
		{
			for (List<PointerTargetNode> t2 : trees2)
			{
				for (PointerTargetNode node : t1)
				{
					if (contains(t2,node.getSynset()))
					{
						pLCS.add(node.getSynset());
						break;
					}
				}
				
				for (PointerTargetNode node : t2)
				{
					if (contains(t1,node.getSynset()))
					{
						pLCS.add(node.getSynset());
						break;
					}
				}
			}
		}
		
		Synset lcs = null;
		double score = 0;
		
		for (Synset s : pLCS)
		{
			if (lcs == null)
			{
				lcs = s;
				score = getIC(s);
			}
			else
			{
				double ic = getIC(s);
				
				if (ic > score)
				{
					score = ic;
					lcs = s;
				}
			}
		}
		
		
		if (lcs == null && useSingleRoot())
		{	
			//link the two synsets by a fake root node
			
			//TODO: Should probably create one of these for each POS tag and cache them so that we can always return the same one
			lcs = new Synset(s1.getPOS(),0l,new Word[0],new Pointer[0],"",new java.util.BitSet());
						
		}
		
		return lcs;
	}
}
