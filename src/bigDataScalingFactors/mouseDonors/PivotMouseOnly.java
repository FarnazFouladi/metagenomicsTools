package bigDataScalingFactors.mouseDonors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

import parsers.OtuWrapper;
import utils.ConfigReader;

public class PivotMouseOnly
{
	public static void main(String[] args) throws Exception
	{
		// the first row and last column were manually removed...
		File inFile = new File( ConfigReader.getBigDataScalingFactorsDir() + File.separator + 
				"MouseDonors" + File.separator + "otu_table_edited.txt");
		
		File outFile = new File( ConfigReader.getBigDataScalingFactorsDir() + File.separator + 
				"MouseDonors" + File.separator + "otu_table_mouseOnlyAllSamples.txt");
		
		OtuWrapper.transpose(inFile.getAbsolutePath(), outFile.getAbsolutePath(), false);
		
		OtuWrapper wrapper = new OtuWrapper(outFile);
		
		HashMap<String, MetadataParserFileLine> metaMap = MetadataParserFileLine.parseMetadata();
		
		HashSet<String> excludedSamples = new HashSet<String>();
		
		for(MetadataParserFileLine mpfl : metaMap.values())
			if( mpfl.getEnv_package().indexOf("human") != -1)
				excludedSamples.add(mpfl.getSample());
		
		HashSet<String> excludedOtus = new HashSet<String>();
		
		for(int x=0; x < wrapper.getOtuNames().size(); x++)
		{
			String otuName = wrapper.getOtuNames().get(x);
			if( wrapper.getCountForTaxaExcludingTheseSamples(x, excludedSamples) < 0.1 )
				excludedOtus.add(otuName);
		}
		
		wrapper = new OtuWrapper(outFile, excludedSamples, excludedOtus);
		
		wrapper.writeUnnormalizedDataToFile(
				new File(ConfigReader.getBigDataScalingFactorsDir() + File.separator + 
				"MouseDonors" + File.separator + "otu_table_mouseOnlyAllSamplesTaxaAsColumns.txt"));
		
		wrapper.writeNormalizedDataToFile(
				new File(ConfigReader.getBigDataScalingFactorsDir() + File.separator + 
				"MouseDonors" + File.separator + "otu_table_mouseOnlyAllSamplesTaxaAsColumnsNorm.txt"));
		
		wrapper.writeLoggedDataWithTaxaAsColumns(
				new File(ConfigReader.getBigDataScalingFactorsDir() + File.separator + 
				"MouseDonors" + File.separator + "otu_table_mouseOnlyAllSamplesTaxaAsColumnsLogNorm.txt"));
		
		wrapper.writeRankedSpreadsheet(ConfigReader.getBigDataScalingFactorsDir() + File.separator + 
				"MouseDonors" + File.separator + "otu_table_mouseOnlyAllSamplesTaxaAsColumnsRanked.txt");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				new File( 
				ConfigReader.getBigDataScalingFactorsDir() + File.separator + 
				"MouseDonors" + File.separator 
				+ "otu_table_mouseOnlyAllSamplesTaxaAsColumnsPlusMetaData.txt")));
		
		writer.write("sample\tinoculum");
		
		for(String s: wrapper.getOtuNames())
			writer.write("\t" + s);
		
		writer.write("\n");
		
		for(int x=0; x < wrapper.getSampleNames().size(); x++)
		{
			String sampleName = wrapper.getSampleNames().get(x);
			writer.write(sampleName + "\t");
			
			MetadataParserFileLine mpfl = metaMap.get(sampleName);
			
			if( mpfl == null)
				throw new Exception("No");
			
			writer.write(mpfl.getInnoculum());
			
			for( int y=0; y < wrapper.getOtuNames().size(); y++)
				writer.write("\t" + wrapper.getDataPointsUnnormalized().get(x).get(y));
			
			writer.write("\n");
		}
		
		writer.flush();  writer.close();
	}
}
