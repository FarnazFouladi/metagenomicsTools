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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import dynamicProgramming.PairedAlignment;

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
	private boolean yMerged = false;
	
	public boolean isMerged()
	{
		return merged;
	}
	
	private List<EditRepresentation> clusteredSequences = new ArrayList<EditRepresentation>();
	
	
	public static HashHolder getMatchPosition( HashMap<Long, Integer> map1, String s ) throws Exception
	{
		HashHolder hh = new HashHolder(WORD_SIZE);
		hh.setToString(s);
		
		if( map1.containsKey(hh.getBits()) )
			return hh;
		
		while( hh.advance())
			if( map1.containsKey(hh.getBits()) )
				return hh;
			
		return null;
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
		this.consensusSequence = s;
	}
	
	@Override
	public int compareTo(Cluster o)
	{
		return o.getTotalNum() - this.getTotalNum();
	}
	
	public static List<Cluster> getInitialListFromDereplicatedFile(File file, int minNumRequired) throws Exception
	{
		List<Cluster> list = new ArrayList<Cluster>();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		reader.readLine();
		
		int numRead=0;
		for(String s= reader.readLine(); s != null; s = reader.readLine())
		{
			Cluster c= new Cluster();
			StringTokenizer sToken = new StringTokenizer(s);
			c.setConsensusSequence(new String( sToken.nextToken()));
			int numCopies = Integer.parseInt(sToken.nextToken());
			
			if( sToken.hasMoreTokens())
				throw new Exception("Unexpected line "  + s);
			
			EditRepresentation cr = new EditRepresentation( numCopies, null,0);
			c.clusteredSequences.add(cr);
			
			if(numCopies >= minNumRequired)
				list.add(c);
			
			numRead++;
			
			if(numRead %1000 ==0)
				System.out.println("Read " + numRead);
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
		long startTime = System.currentTimeMillis();
		
		List<Cluster> list = getClusteredList(
				new File(
				ConfigReader.getReducedOTUDir() + File.separator + "derepped.txt"), 1);

		double seconds = ((System.currentTimeMillis()) - startTime ) / 1000.0;

		System.out.println(   seconds + "seconds");
		
		writeRepresentativeFiles(list, new File(ConfigReader.getReducedOTUDir() +
				File.separator + "repOTU.fasta"));
		
		writeAlignentFile(list, new File(ConfigReader.getReducedOTUDir() +
				File.separator + "repOTU.summary"));
		
	}
	
	private static void writeAlignentFile(List<Cluster> clusters, File outFile)
			throws Exception
		{
			System.out.println("Writing alignment " + outFile.getAbsolutePath());
		
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
			
			int otuNum =1;
			
			for(Cluster c : clusters)
			{
				if( ! c.yMerged)
				{
					writer.write("#\n");
					writer.write("repOTU" + otuNum  + " with numSeqs=" + c.getTotalNum() 
							+ " length=" + c.consensusSequence.length() + "\n");
					writer.write(c.consensusSequence + "\n");
					
					for( int x=0; x < c.clusteredSequences.size(); x++)
					{
						EditRepresentation er = c.clusteredSequences.get(x);
						writer.write("\t"  + er.getNumCopies() + " with distance " + er.getDistance() + "\n");
						
						if( x==0)
						{
							writer.write(c.consensusSequence + "\n");
							
							for( int y=0; y < c.consensusSequence.length(); y++)
								writer.write("|");
							
							writer.write("\n" + c.consensusSequence + "\n");
						}
						else
						{
							writer.write(er.getEditList().toString() + "\n");
							PairedAlignment pa = ReducedTools.getAlignment(c.consensusSequence, er.getEditList());
							writer.write( pa.getFirstSequence() + "\n");
							writer.write(pa.getMiddleString() + "\n");
							writer.write(pa.getSecondSequence() + "\n");
						}	
					}

					otuNum++;
				}
			}
			
			writer.flush();  writer.close();
		}
	
	private static void writeRepresentativeFiles(List<Cluster> clusters, File outFile)
		throws Exception
	{
		System.out.println("Writing fasta " + outFile.getAbsolutePath());
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		
		int otuNum =1;
		
		for(Cluster c : clusters)
		{
			if( ! c.yMerged)
			{
				writer.write(">repOTU" + otuNum + " length="  + 
							c.consensusSequence.length() + " numSeqs=" + c.getTotalNum() +  "\n");
				writer.write( c.consensusSequence + "\n" );
				otuNum++;
			}
		}
		
		writer.flush();  writer.close();
	}
	
	public static List<Cluster> getClusteredList(File dereppedFile, int minNumRequired) throws Exception
	{
		List<Cluster> list = getInitialListFromDereplicatedFile(dereppedFile, minNumRequired);
		
		System.out.println(list.size());
		int numMerged =0;
		
		for( int x=0; x < list.size() -1; x++)
		{
			Cluster xCluster = list.get(x);
			
			if( xCluster.merged == false )
			{
				HashMap<Long, Integer> xMap = HashHolder.getWordIndex(xCluster.consensusSequence, WORD_SIZE);
				xCluster.merged = true;
				for( int y=x+1; y < list.size(); y++)
				{
					Cluster yCluster = list.get(y);
					
					if( yCluster.merged == false)
					{	
						HashHolder yPosition = getMatchPosition(xMap, yCluster.consensusSequence);
						
						if( yPosition != null)
						{
							int max = Math.max(xCluster.consensusSequence.length(), yCluster.consensusSequence.length());
							int numErrors = (int) (0.03 * max+ 1 );
								
							
							DP_Expand expand = new DP_Expand(xCluster.consensusSequence, 
									yCluster.consensusSequence, xMap.get(yPosition.getBits()), 
											yPosition.getStringIndex(), WORD_SIZE, numErrors);
							
							if( expand.alignmentWasSuccesful())
							{
								yCluster.merged = true;
								yCluster.yMerged = true;
								
								double distance = 
									((double)expand.getNumErrors()) 
									/ Math.min(xCluster.consensusSequence.length(), yCluster.consensusSequence.length());
								
								EditRepresentation er = new EditRepresentation(yCluster.getTotalNum(),
													expand.getEditList(), distance);
								
								xCluster.clusteredSequences.add(er);
								numMerged++;
								
								if( numMerged %10000 == 0 )
									System.out.println("Merged " + x + " with " + y  +" as merge # " + numMerged);
							}
						}
					}
				}
			}	
		}
		
		Collections.sort(list);
		
		return list;
	}
} 
