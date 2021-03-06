package scripts.topeOneAtATime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import parsers.NewRDPParserFileLine;
import parsers.OtuWrapper;
import utils.ConfigReader;

public class AddMetadataMerged
{
	public static HashMap<String, Integer> getCaseControlMap() throws Exception
	{
		HashMap<String, Integer> map = new HashMap<String,Integer>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(
			ConfigReader.getTopeJan2016Dir() + File.separator + "tk_out_29Jan2016_corrected.txt"	)));
		
		reader.readLine();
		
		for(String s = reader.readLine(); s != null; s= reader.readLine())
		{
			String[] splits = s.split("\t");
			
			if( splits.length > 2)
				throw new Exception("No");
			
			if( splits.length == 2)
			{
				if( map.containsKey(splits[0]))
					throw new Exception("No");
				
				map.put(splits[0], Integer.parseInt(splits[1]));
			}
		}
		
		reader.close();
		
		return map;
	}
	
	public static void main(String[] args) throws Exception
	{
		HashSet<String> fileSet3=  getFileSet(3);
		HashSet<String> fileSet4 = getFileSet(4);
		
		for( int x=1; x < NewRDPParserFileLine.TAXA_ARRAY.length; x++)
		{
			System.out.println(NewRDPParserFileLine.TAXA_ARRAY[x]);
			
			File rawCounts = new File(ConfigReader.getTopeOneAtATimeDir()
					+ File.separator + "merged" +
					File.separator + "pivoted_" + 
			NewRDPParserFileLine.TAXA_ARRAY[x] + "asColumns.txt");
			
			OtuWrapper wrapper = new OtuWrapper( rawCounts );
			
			File logNormalizedFile = new File(	ConfigReader.getTopeOneAtATimeDir()
					+ File.separator + "merged" + 
					File.separator + "pivoted_" + 
			NewRDPParserFileLine.TAXA_ARRAY[x] + "asColumnsLogNormal.txt");
			
			File outFile = new File( ConfigReader.getTopeOneAtATimeDir()
					+ File.separator + "merged" +
					File.separator + "pivoted_" + 
					NewRDPParserFileLine.TAXA_ARRAY[x] + "asColumnsLogNormalPlusMetadata.txt");
			
			addMetadata(wrapper, logNormalizedFile, outFile,false, fileSet3, fileSet4);
			
			logNormalizedFile = new File(ConfigReader.getTopeOneAtATimeDir()
					+ File.separator + "merged" +
					File.separator +  "mds_"+ NewRDPParserFileLine.TAXA_ARRAY[x] +  ".txt" );
			
			outFile = new File( ConfigReader.getTopeOneAtATimeDir()
					+ File.separator + "merged" +
					File.separator +  "mds_"+ NewRDPParserFileLine.TAXA_ARRAY[x] +  "PlusMetadata.txt" );
			
			addMetadata(wrapper, logNormalizedFile, outFile, true , fileSet3, fileSet4);
			
			File linearMetadataFile = new File(ConfigReader.getTopeOneAtATimeDir()
					+ File.separator + "merged" +
					File.separator + "pivoted_" + 
			NewRDPParserFileLine.TAXA_ARRAY[x] + "asColumnsPlusMetadata.txt");
			
			addMetadata(wrapper, rawCounts, linearMetadataFile, false, fileSet3, fileSet4);	
		}
		
		System.out.println("otu");
		File rawCounts = new File( ConfigReader.getTopeOneAtATimeDir() + File.separator +
				"qiimeSummary" + File.separator +  
				"diverticulosis_closed_otu_AsColumnsRareTaxaRemoved.txt");
			
		OtuWrapper wrapper = new OtuWrapper( rawCounts );
		
		File unnormalizedOtu = new File(ConfigReader.getTopeOneAtATimeDir() + File.separator + 
				"merged" + File.separator + 
				"pivoted_" + "otu"+ "asColumnsPlusMetadata.txt");
		
		addMetadata(wrapper, rawCounts, unnormalizedOtu, false, fileSet3, fileSet4);
		
		File logNormalFile= new File(  ConfigReader.getTopeOneAtATimeDir()
				+ File.separator + "merged" + 
				File.separator + "pivoted_" + 
		"otu" + "asColumnsLogNormal.txt" );
			
		wrapper.writeNormalizedLoggedDataToFile(logNormalFile.getAbsolutePath());
		
		File outFile = new File( ConfigReader.getTopeOneAtATimeDir()
					+ File.separator + "merged" +
					File.separator + "pivoted_" + 
		"otu" + "asColumnsLogNormalPlusMetadata.txt");
		
		addMetadata(wrapper, logNormalFile, outFile, false, fileSet3, fileSet4);
		
		System.out.println("otu_qiime_cr");
		rawCounts = new File( ConfigReader.getTopeOneAtATimeDir() + File.separator +
				"qiimeSummary" + File.separator +  
				"diverticulosis_closed_g_AsColumns.txt");
			
		wrapper = new OtuWrapper( rawCounts );
		
		logNormalFile= new File(  ConfigReader.getTopeOneAtATimeDir() + File.separator +
				"qiimeSummary" + File.separator +  
		"diverticulosis_closed_genus_AsColumnsLogNormal.txt" );
			
		outFile = new File( ConfigReader.getTopeOneAtATimeDir()
					+ File.separator + "merged" +
					File.separator + "pivoted_" + 
		"otu_qiime_cr" + "asColumnsLogNormalPlusMetadata.txt");
		
		addMetadata(wrapper, logNormalFile, outFile, false, fileSet3, fileSet4);	
		
		System.out.println("finished");
	}
	
	private static int getReadNum(String key) throws Exception
	{
		//System.out.println(key);
		String[] splits = key.split("_");
		
		int splitID = 2;
		int val = -1;
		
		try
		{
			 val = Integer.parseInt(splits[splitID]);
		}
		catch(Exception ex)
		{
			splitID = 1;
			val = Integer.parseInt(splits[splitID]);	
		}
		
		if( val != 1 && val != 4)
			throw new Exception("No ");
		
		return val;
	}
	
	static HashSet<String> getFileSet(int fileNum) throws Exception
	{
		HashSet<String> set = new HashSet<String>();
		
		File file3Dir =new File(ConfigReader.getTopeOneAtATimeDir() + File.separator + 
				"File" + fileNum + File.separator + "fastaOut");
		
		for( String s : file3Dir.list())
		{
			set.add(new StringTokenizer(s, "_").nextToken());
		}
		
		return set;
	}
	
	static String getIdOrThrow(String sampleID, HashSet<String> file3Set, HashSet<String> file4Set)
		throws Exception
	{
		if( file3Set.contains(sampleID)  && file4Set.contains(sampleID))
			throw new Exception("Sample id in both " + sampleID);
		
		if( file3Set.contains(sampleID) )
			return "File3";
		
		if( file4Set.contains(sampleID))
			return "File4";
		
		throw new Exception("File id in neither " + sampleID);
	}
	
	static HashMap<String, String> getTicLocationMap() throws Exception
	{
		HashMap<String, String> map = new HashMap<String,String>();
		
		BufferedReader reader = new BufferedReader( new FileReader(
				ConfigReader.getTopeOneAtATimeDir()+
				File.separator + "tk_out_24Jan2017.txt" ));
		
		reader.readLine();
		
		for(String s= reader.readLine(); s != null; s= reader.readLine())
		{
			String[] splits = s.split("\t");
			//System.out.println(splits[0] + " " + splits[9]);
			map.put(splits[0],splits[9]);
		}
		
		reader.close();
		
		return map;
	}
	
	private static void addMetadata( OtuWrapper wrapper, File inFile, File outFile,
				boolean fromR, HashSet<String> file3Set, HashSet<String> file4Set) throws Exception
	{
		HashMap<String, Nov2016MetadataParser> novMetaMap = Nov2016MetadataParser.getMetaMap();
		HashMap<String, Integer> caseControlMap = getCaseControlMap();
		HashMap<String, String> ticLocaitonMap = getTicLocationMap();
		HashMap<String, String> hemeMap = GetHemsizeMap.getHemsizeMap();
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		
		writer.write("id\tkey\t");
		
		writer.write("waist\tticsCount\tage\tsex\tbmi\twhr\twbo\tbmi_CAT\tticLocation\themsize_s_ml\t");
		
		writer.write("readNum\tisBlankControl\tnumberSequencesPerSample\tshannonEntropy\tcaseControl\tset\tread");
		
		String[] firstSplits = reader.readLine().split("\t");
		
		int startPos = fromR ? 0 : 1;
		
		for( int x=startPos; x < firstSplits.length; x++)
			writer.write("\t" + firstSplits[x]);
		
		writer.write("\n");
		
		for(String s = reader.readLine(); s != null; s= reader.readLine())
		{
			String[] splits = s.split("\t");
			
			String key = splits[0].replaceAll("\"", "");
			String sampleId = new StringTokenizer(key, "_").nextToken();
			
			Nov2016MetadataParser novMeta = novMetaMap.get(key.split("_")[0] );
				
			writer.write(key+ "\t" + key.split("_")[0] + "\t");
			
			if( novMeta == null)
			{
				writer.write("\t\t\t\t\t\t\t\t");
			}
			else
			{
				//writer.write("waist\tticsCount\tage\tsex\tbmi\twhr\twbo\tbmi_CAT\t");
				
				writer.write( getStringOrNothing(novMeta.getWaist()) + "\t");
				writer.write( getStringOrNothing(novMeta.getTicsCount()) + "\t");
				writer.write( getStringOrNothing(novMeta.getAge()) + "\t");
				writer.write( getStringOrNothing(novMeta.getSex()) + "\t");
				writer.write( getStringOrNothing(novMeta.getBmi()) + "\t");
				writer.write(  getStringOrNothing(novMeta.getWhr()) + "\t");
				writer.write(  getStringOrNothing(novMeta.getWbo()) + "\t");
				writer.write(  getStringOrNothing(novMeta.getBmi_CAT()) + "\t");	
			}
			
			String location = ticLocaitonMap.get(key.split("_")[0] );
			
			if( location == null || location.length() == 0)
				writer.write("\t");
			else
				writer.write(location + "\t");
			
			writer.write(  getStringOrNothing(hemeMap.get( key.split("_")[0])) + "\t");	
			
			writer.write( getReadNum(key) + "\t" + 
						( key.indexOf("DV-000-") != -1) + "\t" + 
					wrapper.getNumberSequences(key) 
						+ "\t" + wrapper.getShannonEntropy(key) + "\t" );
			
			Integer val = caseControlMap.get( new StringTokenizer(key, "_").nextToken());
			
			if( val == null)
				writer.write("-1\t");
			else
				writer.write("" + val + "\t");
			
			writer.write(getIdOrThrow(sampleId, file3Set, file4Set)+ "\t");
			
			writer.write( Integer.parseInt(key.split("_")[1]) + "");
				
			for( int x=1; x < splits.length; x++)
				writer.write("\t" + splits[x]);
			
			writer.write("\n");
		}
		
		writer.flush();  writer.close();
		reader.close();
	}
	
	static String getStringOrNothing(Object o)
	{
		if( o == null)
			return "NA";
		
		return o.toString();
	}
	
}
