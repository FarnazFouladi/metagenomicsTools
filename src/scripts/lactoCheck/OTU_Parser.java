package scripts.lactoCheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utils.ConfigReader;

public class OTU_Parser
{	
	public static void main(String[] args) throws Exception
	{
		List<String> headers= getSampleHeaders();
		
		System.out.println(headers.size());
		
		List<Holder> allLacto =getAllLacto();
		
		System.out.println(allLacto.size());
		
		writeResults(headers, allLacto);
	}
	
	private static void writeResults( List<String> headers, List<Holder> allLacto  )
		throws Exception
	{
		HashMap<String, PCR_DataParser> pcrMap =
				PCR_DataParser.getPCRData();
		
		BufferedWriter writer =new BufferedWriter(new FileWriter(new File(
			ConfigReader.getLactoCheckDir() + File.separator + "merged.txt"	)));
		
		writer.write("id\tgroup\tL_crispatus\tL_iners\tbglobulin");
		
		for(Holder h : allLacto)
			writer.write("\t" + h.taxaString.replace(";", "_").
						replace("k__Bacteria_ p__Firmicutes_ c__Bacilli_ o__Lactobacillales_ f__Lactobacillaceae_", ""));
		
		writer.write("\n");
		
		for( int x=0; x < headers.size(); x++)
		{
			writer.write(headers.get(x));
			
			String[] subSplit = headers.get(x).split("\\.");
			
			writer.write("\t" + subSplit[2]);
			
			PCR_DataParser pcr = pcrMap.get(subSplit[2]);
			
			if( pcr == null)
				writer.write("\t\t\t");
			else
				writer.write("\t" + pcr.getL_crispatus() + "\t" + pcr.getL_iners() + "\t" + pcr.getBglobulin());
			
			if( subSplit.length != 3)
				throw new Exception("No");
			
			for( Holder h : allLacto)
			{
				if( h.values.size() != headers.size())
					throw new Exception("No");
				
				writer.write("\t" + h.values.get(x));
				
			}
			
			writer.write("\n");
		}
		
		writer.flush(); writer.close();
	}
	private static class Holder
	{
		String taxaString;
		List<Double> values = new ArrayList<Double>();
		
		
	}
	
	private static final List<Holder> getAllLacto() throws Exception
	{
		List<Holder> list = new ArrayList<Holder>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				ConfigReader.getLactoCheckDir() + File.separator + 
					"gaQiimeClosedRef.txt")));
		
		reader.readLine(); reader.readLine();
		
		for(String s =reader.readLine(); s != null; s= reader.readLine())
		{
			String[] splits = s.split("\t");
			
			String taxaString = splits[splits.length-1];
			
			if(taxaString.contains("g__Lactobacillus"))
			{
				Holder h = new Holder();
				h.taxaString = splits[0];
				
				for( int x=1; x < splits.length-1; x++)
					h.values.add(Double.parseDouble(splits[x]));
				list.add(h);
			}
				
		}
		reader.close();
		return list;
	}
	
	private static final List<String> getSampleHeaders() throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(new File(
				ConfigReader.getLactoCheckDir() + File.separator + 
					"gaQiimeClosedRef.txt")));
		reader.readLine();
		
		String[] splits = reader.readLine().split("\t");
		
		List<String> list = new ArrayList<String>();
		
		for( int x=1; x < splits.length-1; x++)
			list.add(splits[x]);
		
		reader.close();
		return list;
	}
}
