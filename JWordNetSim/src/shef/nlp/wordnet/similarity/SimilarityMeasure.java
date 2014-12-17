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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * An abstract notion of a similarity measure that all provided
 * implementations extend.
 * @author Mark A. Greenwood
 */
public abstract class SimilarityMeasure
{	
	/**
	 * A mapping of terms to specific synsets. Usually used to map domain
	 * terms to a restricted set of synsets but can also be used to map
	 * named entity tags to appropriate synsets.
	 */
	private Map<String,Set<Synset>> domainMappings = new HashMap<String,Set<Synset>>();
	
	/**
	 * The maximum size the cache can grow to
	 */
	private int cacheSize = 5000;
	
	/**
	 * To speed up computation of the similarity between two synsets
	 * we cache each similarity that is computed so we only have to
	 * do each one once.
	 */
	private Map<String,Double> cache = new LinkedHashMap<String,Double>(16,0.75f,true)
	{
        public boolean removeEldestEntry(Map.Entry<String,Double> eldest)
        {
            //if the size is less than zero then the user is asking us
        	//not to limit the size of the cache so return false
        	if (cacheSize < 0) return false;
        	
        	//if the cache has crown bigger than it's max size return true
        	return size() > cacheSize;
        }
    }; 
	
	/**
	 * Get a previously computed similarity between two synsets from the cache.
	 * @param s1 the first synset between which we are looking for the similarity.
	 * @param s2 the other synset between which we are looking for the similarity.
	 * @return The similarity between the two sets or null
	 *         if it is not in the cache.
	 */
	protected final Double getFromCache(Synset s1, Synset s2)
	{
		return cache.get(s1.getKey()+"-"+s2.getKey());
	}
	
	/**
	 * Add a computed similarity between two synsets to the cache so that
	 * we don't have to compute it if it is needed in the future.
	 * @param s1 one of the synsets between which we are storring a similarity.
	 * @param s2 the other synset between which we are storring a similarity.
	 * @param sim the similarity between the two supplied synsets.
	 * @return the similarity score just added to the cache.
	 */
	protected final double addToCache(Synset s1, Synset s2, double sim)
	{
		cache.put(s1.getKey()+"-"+s2.getKey(),sim);
		
		return sim;
	}
	
	/**
	 * Configures the similarity measure using the supplied parameters.
	 * @param params a set of key-value pairs that are used to configure
	 *        the similarity measure. See concrete implementations for details
	 *        of expected/possible parameters. 
	 * @throws Exception if an error occurs while configuring the similarity measure.
	 */
	protected abstract void config(Map<String,String> params) throws Exception;
	
	/**
	 * Create a new instance of a similarity measure.
	 * @param confURL the URL of a configuration file. Parameters are specified
	 *        one per line as key:value pairs.
	 * @return a new instance of a similairy measure as defined by the
	 *         supplied configuration URL.
	 * @throws Exception if an error occurs while creating the similarity measure.
	 */
	public static SimilarityMeasure newInstance(URL confURL) throws Exception
	{
		//create map to hold the key-value pairs we are going to read from
		//the configuration file
		Map<String,String> params = new HashMap<String,String>();
		
		//create a reader for the config file
		BufferedReader in = null;
		
		try
		{
			//open the config file
			in = new BufferedReader(new InputStreamReader(confURL.openStream()));
					
			String line = in.readLine();
			while (line != null)
			{
				line = line.trim();
				
				if (!line.equals(""))
				{
					//if the line contains something then
					
					//split the data so we get the key and value
					String[] data = line.split("\\s*:\\s*",2);
					
					if (data.length == 2)
					{
						//if the line is valid add the two parts to the map
						params.put(data[0], data[1]);
					}
					else
					{
						//if the line isn't valid tell the user but continue on
						//with the rest of the file
						System.out.println("Config Line is Malformed: " + line);
					}
				}
				
				//get the next line ready to process
				line = in.readLine();
			}
		}
		finally
		{
			//close the config file if it got opened
			if (in != null) in.close();
		}
		
		//create and return a new instance of the similarity measure specified
		//by the config file
		return newInstance(params);
	}
	
	/**
	 * Creates a new instance of a similarity measure using the supplied parameters.
	 * @param params a set of key-value pairs which define the similarity measure.
	 * @return the newly created similarity measure.
	 * @throws Exception if an error occurs  while creating the similarity measure.
	 */
	public static SimilarityMeasure newInstance(Map<String,String> params) throws Exception
	{
		//get the class name of the implementation we need to load
		String name = params.remove("simType");
		
		//if the name hasn't been specified then throw an exception
		if (name == null) throw new Exception("Must specifiy the similarity measure to use");
		
		//Get hold of the class we need to load
		@SuppressWarnings("unchecked") Class<SimilarityMeasure> c = (Class<SimilarityMeasure>)Class.forName(name);
		
		//create a new instance of the similarity measure
		SimilarityMeasure sim = c.newInstance();
		
		//get the cache parameter from the config params
		String cSize = params.remove("cache");
		
		//if a cache size was specified then set it
		if (cSize != null) sim.cacheSize = Integer.parseInt(cSize);
		
		//get the url of the domain mapping file
		String mapURL = params.remove("mapping");
		
		if (mapURL != null)
		{
			//if a mapping file has been provided then 
						
			//open a reader over the file
			BufferedReader in = new BufferedReader(new InputStreamReader((new URL(mapURL)).openStream()));
			
			//get the first line ready for processing
			String line = in.readLine();
			
			while (line != null)
			{
				if (!line.startsWith("#"))
				{
					//if the line isn't a comment (i.e. it doesn't start with #) then...
					
					//split the line at the white space
					String[] data = line.trim().split("\\s+");
					
					//create a new set to hold the mapped synsets
					Set<Synset> mappedTo = new HashSet<Synset>();
					
					for (int i = 1 ; i < data.length ; ++i)
					{
						//for each synset mapped to get the actual Synsets
						//and store them in the set
						mappedTo.addAll(sim.getSynsets(data[i]));
					}
					
					//if we have found some actual synsets then
					//store them in the domain mappings
					if (mappedTo.size() > 0) sim.domainMappings.put(data[0], mappedTo);
				}
				
				//get the next line from the file
				line = in.readLine();
			}
			
			//we have finished with the mappings file so close it
			in.close();
		}		
		
		//make sure it is configured properly
		sim.config(params);
		
		//then return it
		return sim;
	}
	
	/**
	 * This is the method responsible for computing the similarity between two
	 * specific synsets. The method is implemented differently for each
	 * similarity measure so see the subclasses for detailed information.
	 * @param s1 one of the synsets between which we want to know the similarity.
	 * @param s2 the other synset between which we want to know the similarity.
	 * @return the similarity between the two synsets.
	 * @throws JWNLException if an error occurs accessing WordNet.
	 */
	public abstract double getSimilarity(Synset s1, Synset s2) throws JWNLException;
	
	/**
	 * Get the similarity between two words. The words can be specified either
	 * as just the word or in an encoded form including the POS tag and possibly
	 * the sense number, i.e. cat#n#1 would specifiy the 1st sense of the noun cat.
	 * @param w1 one of the words to compute similarity between.
	 * @param w2 the other word to compute similarity between.
	 * @return a SimilarityInfo instance detailing the similarity between the
	 *         two words specified.
	 * @throws JWNLException if an error occurs accessing WordNet.
	 */
	public final SimilarityInfo getSimilarity(String w1, String w2) throws JWNLException
	{
		//Get the (possibly) multiple synsets associated with each word
		Set<Synset> ss1 = getSynsets(w1);
		Set<Synset> ss2 = getSynsets(w2);
				
		//assume the words are not at all similar
		SimilarityInfo sim = null;
		
		for (Synset s1 : ss1)
		{
			for (Synset s2 : ss2)
			{
				//for each pair of synsets get the similarity
				double score = getSimilarity(s1, s2);
								
				if (sim == null || score > sim.getSimilarity())
				{
					//if the similarity is better than we have seen before
					//then create and store an info object describing the
					//similarity between the two synsets
					sim = new SimilarityInfo(w1, s1, w2, s2, score);
				}
			}
		}
		
		//return the maximum similarity we have found
		return sim;	
	}
	
	/**
	 * Finds all the synsets associated with a specific word.
	 * @param word the word we are interested. Note that this may be encoded
	 *        to include information on POS tag and sense index.
	 * @return a set of synsets that are associated with the supplied word
	 * @throws JWNLException if an error occurs accessing WordNet
	 */
	private final Set<Synset> getSynsets(String word) throws JWNLException
	{		
		//get a handle on the WordNet dictionary
		Dictionary dict = Dictionary.getInstance();
		
		//create an emptuy set to hold any synsets we find
		Set<Synset> synsets = new HashSet<Synset>();
		
		//split the word on the # characters so we can get at the
		//upto three componets that could be present: word, POS tag, sense index
		String[] data = word.split("#");
		
		//if the word is in the domainMappings then simply return the mappings
		if (domainMappings.containsKey(data[0])) return domainMappings.get(data[0]);
		
		if (data.length == 1)
		{
			//if there is just the word
				
			for (IndexWord iw : dict.lookupAllIndexWords(data[0]).getIndexWordArray())
			{
				//for each matching word in WordNet add all it's senses to
				//the set we are building up
				synsets.addAll(Arrays.asList(iw.getSenses()));
			}
			
			//we have finihsed so return the synsets we found
			return synsets;
		}
	
		//the calling method specified a POS tag as well so get that
		POS pos = POS.getPOSForKey(data[1]);
		
		//if the POS tag isn't valid throw an exception
		if (pos == null) throw new JWNLException("Invalid POS Tag: " + data[1]);
		
		//get the word with the specified POS tag from WordNet
		IndexWord iw = dict.getIndexWord(pos, data[0]);
		
		if (data.length > 2)
		{
			//if the calling method specified a sense index then
			//add just that sysnet to the set we are creating
			synsets.add(iw.getSense(Integer.parseInt(data[2])));
		}
		else
		{
			//no sense index was specified so add all the senses of
			//the word to the set we are creating
			synsets.addAll(Arrays.asList(iw.getSenses()));
		}
		
		//return the set of synsets we found for the specified word
		return synsets;
	}
}
