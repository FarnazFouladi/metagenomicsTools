package creOrthologs.coinFlip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;

import bitManipulations.Encode;
import parsers.FastaSequence;
import utils.ConfigReader;
import utils.Translate;

public class CoinFlipTest
{
	public static final int KMER_SIZE = 31;
	
	private static class GeneHolder
	{
		List<RangeHolder> rangeList=new ArrayList<RangeHolder>();
		String chr;
		int numOver;
		int numUnder;
		
		HashSet<Long> includedKmers = new HashSet<Long>();
	}
	
	private static class RangeHolder 
	{
		private int stop;
		private int start;
		
		RangeHolder(int start, int stop)
		{
			this.stop = stop;
			this.start = start;
			
			if( this.stop < this.start)
			{
				int temp = this.stop;
				this.stop = this.start;
				this.start = temp;
			}
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		HashMap<String, GeneHolder> map = parseGTFFile();
		List<BinHolder> list = parseBinFile();
		addKmersToGenes( new ArrayList<GeneHolder>(map.values()));
		
		for(GeneHolder gh : map.values())
			System.out.println(gh.includedKmers.size());
			
	}
	
	private static void addKmersToGenes(List<GeneHolder> geneHolderList) throws Exception
	{
		HashMap<String, FastaSequence> fastaMap = FastaSequence.getFirstTokenSequenceMap(
				ConfigReader.getBioLockJDir() + File.separator + "resistantAnnotation" + 
						File.separator +"refGenome" + File.separator 
						+ "klebsiella_pneumoniae_chs_11.0.scaffolds.fasta");
		
		int index=0;
		for(GeneHolder gh: geneHolderList)
		{
			System.out.println(index++ + " " + geneHolderList.size());
			FastaSequence fs = fastaMap.get(gh.chr);
			String seq = fs.getSequence();
			
			for( RangeHolder rh : gh.rangeList )
			{
				for( int x=rh.start - 1; x + KMER_SIZE <= rh.stop ; x++)
				{
					String nucl = seq.substring(x, x + KMER_SIZE);
					
					Long encode = Encode.makeLong(nucl);
					
					if( encode != null)
						gh.includedKmers.add(encode);
					
					nucl = Translate.safeReverseTranscribe(nucl);
					encode = Encode.makeLong(nucl);
					
					if( encode != null)
						gh.includedKmers.add(encode);					
			}
			}
		}
	}
 	
	private static void populateHolders() throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(
			ConfigReader.getBioLockJDir() + File.separator + "resistantAnnotation" 
						+ File.separator + 
					"resistantVsSuc_kneu.txt"));
		
		
		reader.close();
	}
	
	private static class BinHolder
	{
		private double low;
		private double high;
		
		private double average;
	}
	
	private static List<BinHolder> parseBinFile() throws Exception
	{
		List<BinHolder> list = new ArrayList<BinHolder>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				ConfigReader.getBioLockJDir() + 
		File.separator + "resistantAnnotation" + File.separator + "resistantVsSucSummary.txt")));
		
		reader.readLine();
		
		for(String s= reader.readLine(); s != null; s= reader.readLine())
		{
			String[] splits = s.split("\t");
			BinHolder h = new BinHolder();
			h.low = Double.parseDouble(splits[0]);
			h.high = Double.parseDouble(splits[1]);
			h.average = Double.parseDouble(splits[3]);
			list.add(h);
		}
			
		
		return list;
	}
	
	private static HashMap<String, GeneHolder> parseGTFFile() throws Exception
	{
		HashMap<String, GeneHolder> map = new LinkedHashMap<String, GeneHolder>();
		
		BufferedReader reader =new BufferedReader(new FileReader(
			ConfigReader.getBioLockJDir() + File.separator + 
				"resistantAnnotation" + File.separator + 
						"klebsiella_pneumoniae_chs_11.0.genes.gtf"	));
		
		for(String s = reader.readLine(); s != null; s = reader.readLine())
		{
			String[] splits = s.split("\t");
			String key = new StringTokenizer(splits[8], ";").nextToken();
			key = key.replace("gene_id", "").replaceAll("\"", "");
			
			GeneHolder gh = map.get(key);
			
			if( gh == null)
			{
				gh = new GeneHolder();
				gh.chr = splits[0];
				map.put(key,gh);
			}
			
			RangeHolder h = new RangeHolder(Integer.parseInt(splits[3]), Integer.parseInt(splits[4]));
			gh.rangeList.add(h);
			if( ! gh.chr.equals(splits[0]))
				throw new Exception("Parsing error");
		}
		
		reader.close();
		
		return map;
	}
}
