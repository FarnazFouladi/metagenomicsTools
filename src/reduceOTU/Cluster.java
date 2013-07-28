/** 
 * Author:  anthony.fodor@gmail.com    
 * This code is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version,
* provided that any use properly credits the author.
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details at http://www.gnu.org * * */


package reduceOTU;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import utils.ConfigReader;

public class Cluster implements Comparable<Cluster>
{
	// todo: This should be a usable adjustable parameter
	// in current implementation can't be set above 32
	// (since we use a long as the key and 32 nucleotides can be encoded in the 64
	// bits of the long
	public static final int WORD_SIZE = 32;
	
	private String consensusSequence;
	
	private boolean merged =false;
	
	public boolean isMerged()
	{
		return merged;
	}
	
	private List<EditRepresentation> clusteredSequences = new ArrayList<EditRepresentation>();
	
	private HashMap<Long, Integer> hashes;
	
	public HashMap<Long, Integer> getHashes()
	{
		return hashes;
	}
	

	public static Long findFirstMatch( HashMap<Long, Integer> map1, HashMap<Long, Integer> map2 )
		throws Exception
	{
		for( Long l : map1.keySet() )
			if( map2.containsKey(l))
				return l;
			
		return null;
	}
	
	public int getTotalNum()
	{
		int sum=0;
		
		for( EditRepresentation cr : clusteredSequences)
			sum += cr.getNumCopies();
		
		return sum;
	}
	
	/*
	 * As a side-effect re-hashes.  Not even remotely thread safe.
	 */
	public void setConsensusSequence(String s) throws Exception
	{
		hashes = HashHolder.getWordIndex(s, WORD_SIZE);
	}
	
	@Override
	public int compareTo(Cluster o)
	{
		return o.getTotalNum() - this.getTotalNum();
	}
	
	public static List<Cluster> getInitialListFromDereplicatedFile(File file) throws Exception
	{
		List<Cluster> list = new ArrayList<Cluster>();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		reader.readLine();
		
		for(String s= reader.readLine(); s != null; s = reader.readLine())
		{
			Cluster c= new Cluster();
			StringTokenizer sToken = new StringTokenizer(s);
			c.setConsensusSequence(new String( sToken.nextToken()));
			int numCopies = Integer.parseInt(sToken.nextToken());
			
			if( sToken.hasMoreTokens())
				throw new Exception("Unexpected line "  + s);
			
			EditRepresentation cr = new EditRepresentation( numCopies, null);
			c.clusteredSequences.add(cr);
			
			list.add(c);
		}
		
		Collections.sort(list);
		return list;
	}
	
	@Override
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		buff.append(consensusSequence + "\n");
		
		for(EditRepresentation cr : clusteredSequences)
			buff.append("\t" + cr.toString() + "\n");
		
		return buff.toString();
	}
	
	public static void main(String[] args) throws Exception
	{
		List<Cluster> getClusteredList = getClusteredList(
				new File(
				ConfigReader.getReducedOTUDir() + File.separator + "derepped.txt"));
		
		
	}
	
	private static void writeRepresentativeFiles(List<Cluster> cluuster, File outFile)
	{
		
	}
	
	public static List<Cluster> getClusteredList(File dereppedFile) throws Exception
	{
		List<Cluster> list = getInitialListFromDereplicatedFile(dereppedFile);
		
		System.out.println(list.size());
		int numMerged =0;
		
		for( int x=0; x < list.size() -1; x++)
		{
			Cluster xCluster = list.get(x);
			System.out.println("Trying " + (x+1) + " with " + numMerged + " merged ");
			
			if( xCluster.merged == false )
			{
				xCluster.merged = true;
				for( int y=x+1; y < list.size(); y++)
				{
					Cluster yCluster = list.get(y);
					
					if( yCluster.merged == false)
					{
						Long key = findFirstMatch(xCluster.getHashes(), yCluster.getHashes());
						
						if( key != null)
						{
							int numErrors = (int) 
								(0.03 *	
								Math.min(xCluster.consensusSequence.length(), yCluster.consensusSequence.length()) 
									+ 0.51);
							
							DP_Expand expand = new DP_Expand(xCluster.consensusSequence, 
									yCluster.consensusSequence, xCluster.hashes.get(key), 
											yCluster.hashes.get(key), WORD_SIZE, numErrors);
							
							if( expand.alignmentWasSuccesful())
							{
								yCluster.merged = true;
								EditRepresentation er = new EditRepresentation(yCluster.getTotalNum(),
													expand.getEditList());
								
								xCluster.clusteredSequences.add(er);
								numMerged++;
							}
						}
					}
				}
			}	
		}
		
		return list;
	}
} 
