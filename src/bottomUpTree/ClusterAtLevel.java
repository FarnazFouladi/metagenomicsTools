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


package bottomUpTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import parsers.FastaSequence;
import parsers.FastaSequenceOneAtATime;
import probabilisticNW.KmerDatabaseForProbSeq;
import probabilisticNW.KmerQueryResultForProbSeq;
import probabilisticNW.ProbNW;
import probabilisticNW.ProbSequence;

public class ClusterAtLevel
{
	/*
	 * 
	 * As a side effect, all seqs are removed from seqsToCluster
	 */
	public static List<ProbSequence> clusterAtLevel( List<ProbSequence> seqstoCluster, 
								float levelToCluster, float stopSearchThreshold) throws Exception
	{

		if( stopSearchThreshold < levelToCluster)
			throw new Exception("Illegal arguments ");
		
		List<ProbSequence> clusters = new ArrayList<ProbSequence>();
		System.out.println("Building database");
		KmerDatabaseForProbSeq db = KmerDatabaseForProbSeq.buildDatabase(seqstoCluster);
		System.out.println("Finished");
		
		while( ! seqstoCluster.isEmpty())
		{
			ProbSequence seedSeq = seqstoCluster.remove(0);
			List<KmerQueryResultForProbSeq> targets = 
					db.queryDatabase(seedSeq.getConsensusUngapped());
			
			boolean keepGoing = true;
			int targetIndex =0;
			
			while(keepGoing && targetIndex < targets.size())
			{
				KmerQueryResultForProbSeq possibleMatch = targets.get(targetIndex);
				
				ProbSequence possibleAlignment = 
						ProbNW.align(seedSeq, possibleMatch.getProbSeq());
					double distance =possibleAlignment.getAverageDistance();
					
				if(distance <= levelToCluster)
				{
					ProbSequence oldSeq = seedSeq;
					seedSeq = possibleAlignment;
					seedSeq.setMapCount(oldSeq, possibleMatch.getProbSeq());
					possibleMatch.getProbSeq().setMarkedForRemoval(true);
				}
				else if( distance <= stopSearchThreshold)
				{
					keepGoing = false;
				}
				
				targetIndex++;
			}
			
			clusters.add(seedSeq);
			
			for( Iterator<ProbSequence> i = seqstoCluster.iterator(); i.hasNext(); )
				if( i.next().isMarkedForRemoval())
					i.remove();
			
			db = KmerDatabaseForProbSeq.buildDatabase(seqstoCluster);
					
			//System.out.println("Have " + clusters.size() + " with " + seqstoCluster.size() + " left ");
		}
		
		return clusters;
	}
	

	static int getNumberOfDereplicatedSequences(FastaSequence fs) throws Exception
	{
		StringTokenizer header = new StringTokenizer(fs.getFirstTokenOfHeader(), "_");
		header.nextToken();
		header.nextToken();
		
		int returnVal = Integer.parseInt(header.nextToken());
		
		if( header.hasMoreTokens())
			throw new Exception("Parsing error");
		
		return returnVal;
	}
	
	public static List<ProbSequence> getInitialSequencesFromFasta(String fastaPath, String sampleId,
			float threshold, float stopThreshold) throws Exception
	{
		List<ProbSequence> probSeqs = new ArrayList<ProbSequence>();
		
		FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime(fastaPath);
	
		int expectedSum =0;
		for(FastaSequence fs = fsoat.getNextSequence(); fs != null; fs = fsoat.getNextSequence())
			if( fs.isOnlyACGT())
			{
				expectedSum += getNumberOfDereplicatedSequences(fs);
				//System.out.println(getNumberOfDereplicatedSequences(fs));
				probSeqs.add(new ProbSequence(fs.getSequence(), getNumberOfDereplicatedSequences(fs),sampleId));
			}
		
		List<ProbSequence> clustered = clusterAtLevel(probSeqs, threshold, stopThreshold);
		
		int numClustered = 0;
		for(ProbSequence ps : clustered)
		{
			//System.out.println(ps);
			numClustered += ps.getNumRepresentedSequences();
		}
			
		System.out.println("Expecting " + expectedSum);
		System.out.println("Finished with " + clustered.size()  + " clusters with " + numClustered + " sequences");
		return clustered;
	}
}
