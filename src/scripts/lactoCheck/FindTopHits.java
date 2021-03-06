package scripts.lactoCheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import parsers.HitScores;
import utils.ConfigReader;

public class FindTopHits
{
	private static HashMap<String, HashSet<Integer>> getAccessionToOTUID() throws Exception
	{
		HashMap<String, HashSet<Integer>> map = new HashMap<String, HashSet<Integer>>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				ConfigReader.getLactoCheckDir() + File.separator + 
				"gg_13_5_accessions.txt"
					)));
		
		reader.readLine();
		
		for(String s= reader.readLine(); s != null; s= reader.readLine())
		{
			String[] splits = s.split("\t");
			
			if(splits.length != 3)
				throw new Exception("No");
			
			HashSet<Integer> innerSet = map.get(splits[2]);
			
			if(innerSet == null)
			{
				innerSet = new HashSet<Integer>();
				map.put(splits[2], innerSet);
			}
			
			innerSet.add(Integer.parseInt(splits[0]));
		}
		
		reader.close();
		return map;
	}
	
	public static void main(String[] args) throws Exception
	{
		HashMap<String, HashSet<Integer>> accessionToGreengenesMap =
				getAccessionToOTUID();
		
		HashMap<String, HitScores> topHitsMap = 
				HitScores.getTopHitsAsQueryMap(ConfigReader.getLactoCheckDir() + File.separator + 
						"otusToCrispatusMatchingByBlast.txt");
	//					"otusToInersMatchingByBlast.txt");
		
		HashSet<Integer> toKeep = new HashSet<Integer>();
		
		for(String s : topHitsMap.keySet())
		{
			HitScores hs = topHitsMap.get(s);
			
			if( hs.getPercentIdentity() > 99)
			{
				System.out.println(s + " " + hs.getQueryId() + " " + hs.getAlignmentLength() + " "+ 
						hs.getPercentIdentity());
				
				System.out.println(accessionToGreengenesMap.get(hs.getQueryId()));
				toKeep.addAll(accessionToGreengenesMap.get(hs.getQueryId()));
			}
				
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(
			ConfigReader.getLactoCheckDir() + File.separator + 
				"merged.txt")));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				ConfigReader.getLactoCheckDir() + File.separator + 
				"mergedOnlyCrispatus.txt")));
		
		List<Boolean> includeColumn = new ArrayList<Boolean>();
		
		for(int x=0; x< 5; x++)
			includeColumn.add(true);
		
		String[] splits = reader.readLine().split("\t");
		
		for( int x=5; x < splits.length; x++)
		{
			if( toKeep.contains(Integer.parseInt(splits[x])))
			{
				includeColumn.add(true);
			}
			else
			{
				includeColumn.add(false);
			}
		}
		
		writer.write(splits[0]);
		for(int x=1; x < splits.length; x++)
			if( includeColumn.get(x))
				writer.write("\t" + splits[x]);
		
		writer.write("\n");
		
		for( String s= reader.readLine(); s!=null; s= reader.readLine())
		{
			splits=  s.split("\t");
			

			writer.write(splits[0]);
			for(int x=1; x < splits.length; x++)
				if( includeColumn.get(x))
					writer.write("\t" + splits[x]);
			
			writer.write("\n");
			
		}
		
		writer.flush();  writer.close();
		
		reader.close();
	}
}
