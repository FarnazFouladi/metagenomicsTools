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


package bottomUpTree.rdp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


import parsers.NewRDPNode;
import parsers.NewRDPParserFileLine;
import parsers.OtuWrapper;

import utils.ConfigReader;
import utils.TTest;
import utils.TabReader;

public class RdpToJson
{
	public static final String ROOT="root";
	
	private static HashMap<String, Integer> getCaseControlMap() throws Exception
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(ConfigReader.getNinaWithDuplicatesDir() + File.separator + 
				"TopeNewDataMetadata.txt")));
		
		reader.readLine();
		
		for(String s= reader.readLine(); s != null; s= reader.readLine())
		{
			TabReader sToken = new TabReader(s);
			
			String key = sToken.nextToken();
			
			if( map.containsKey(key))
				throw new Exception("Duplicate " + key);
			
			for( int x=0; x < 5; x++)
				sToken.nextToken();
			
			int caseContolInt = Integer.parseInt(sToken.nextToken());
			
			// 1 is case; 0 is control
			if( caseContolInt != 0 && caseContolInt != 1)
				throw new Exception("Illegal val " + caseContolInt);
			
			map.put(key, caseContolInt);
		}
		
		reader.close();
		
		//for(String s : map.keySet())
			//System.out.println(s);
		
		return map;
	}
	
	static String getKeyFromFilename(String s)
	{
		s = s.replace("Tope_","");
		s = s.replace(".fas_rdpOut.txt.gz", "");
		 
		StringTokenizer sToken = new StringTokenizer(s, "_");
		
		s = sToken.nextToken();
		
		if( s.endsWith("A"))
			s = s.substring(0, s.length()-1);
		
		//System.out.println("Stripping an A " + s);
		
		return s;
	}
	
	public static void main(String[] args) throws Exception
	{
		HashMap<String, Integer> caseControlMap = getCaseControlMap();
		
		Holder root = new Holder();
		
		root.taxonomicLevel = ROOT;
		root.taxonomicName= ROOT;
		
		List<NewRDPParserFileLine> allRDPLines = NewRDPParserFileLine.getRdpListSingleThread(ConfigReader.getNinaWithDuplicatesDir() + File.separator + 
				"rdp" + File.separator + "allRdpOut.txt");
		
		for(int x=1; x < NewRDPParserFileLine.TAXA_ARRAY.length; x++)
			addALevel(caseControlMap, x, root, allRDPLines);
		
		for( Holder child : root.children.values() )
		{
			root.caseCounts += child.caseCounts;
			root.controlCounts += child.controlCounts;
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ConfigReader.getD3Dir() + File.separator + "ismeJRDP_CaseControl.json")));
		
		writeNodeAndChildren(writer, root);
		
		writer.flush();  writer.close();
		
		
	}
	
	private static String findParentTaxa( int rdpLevel, List<NewRDPParserFileLine> allRDPLines, String taxaName )
		throws Exception
	{
		for( NewRDPParserFileLine fileLine : allRDPLines)
		{
			NewRDPNode aNode = fileLine.getTaxaMap().get(NewRDPParserFileLine.TAXA_ARRAY[rdpLevel]);
			
			if( aNode != null && aNode.getTaxaName().equals(taxaName))
			{
				//System.out.println(taxaName + " " + fileLine.getSummaryString());
				NewRDPNode parentNode = fileLine.getTaxaMap().get(NewRDPParserFileLine.TAXA_ARRAY[rdpLevel-1]);
				
				if( parentNode != null)
					return parentNode.getTaxaName();
			}
		}
		
		return null;
	}
	
	private static void addALevel( HashMap<String, Integer> caseControlMap, 
										int rdpDepth, 
										Holder root,
										List<NewRDPParserFileLine> allRDPLines)
		throws Exception
	{
		System.out.println("Trying depth " + rdpDepth);
		OtuWrapper wrapper = new OtuWrapper(ConfigReader.getNinaWithDuplicatesDir() + File.separator + 
				"rdp" + File.separator + NewRDPParserFileLine.TAXA_ARRAY[rdpDepth]
									+"_taxaAsColumns.txt");
		List<List<Double>> logNormData = wrapper.getDataPointsNormalizedThenLogged();
		List<List<Double>> unloggedData= wrapper.getDataPointsUnnormalized();
		String parentLevel = NewRDPParserFileLine.TAXA_ARRAY[rdpDepth-1];
		
		if( rdpDepth -1 == 0 )
			parentLevel = ROOT;
		
		HashMap<String, Holder> parentLevelMap = getMapForLevel(parentLevel, root);
		
		for(int x=0; x < wrapper.getOtuNames().size(); x++ )
		{
			String childTaxa = wrapper.getOtuNames().get(x);
			String parentTaxa = ROOT;
			
			if( rdpDepth >= 2 )
			{
				parentTaxa = findParentTaxa(rdpDepth, allRDPLines, childTaxa);
			}
			
			if( parentTaxa != null)
			{
				Holder parentHolder = parentLevelMap.get(parentTaxa);
				
				if( parentHolder != null)
				{
					if( parentHolder.children.containsKey(childTaxa))
						throw new Exception("Duplicate child " + childTaxa);
					
					Holder childHolder = new Holder();
					parentHolder.children.put(childTaxa, childHolder);
					childHolder.taxonomicLevel = NewRDPParserFileLine.TAXA_ARRAY[rdpDepth];
					childHolder.taxonomicName = childTaxa;
					
					for( int y=0; y < wrapper.getSampleNames().size(); y++)
					{
						Integer caseControlStatus = caseControlMap.get(wrapper.getSampleNames().get(y));
						
						if( caseControlStatus == null)
							throw new Exception("Could not find case/control");
						
						if( caseControlStatus.equals(1))
						{
							childHolder.caseCounts+= unloggedData.get(y).get(x);
							childHolder.caseSamplesLogNormalized.add(logNormData.get(y).get(x));
						}
						else if ( caseControlStatus.equals(0))
						{
							childHolder.controlCounts += unloggedData.get(y).get(x);
							childHolder.controlSamplesLogNormalized.add(logNormData.get(y).get(x));
						}
						else throw new Exception("Parsing error");
					}
				}
			}
		}	
	}
	
	private static void flatten( Holder aHolder, List<Holder> list )
	{
		list.add(aHolder);
		
		for( Holder child : aHolder.children.values() )
			flatten(child, list);
	}
	
	private static HashMap<String, Holder> getMapForLevel(String level, Holder root)
		throws Exception
	{
		List<Holder> aList = new ArrayList<Holder>();
		flatten(root,aList);
		
		HashMap<String, Holder> returnMap = new HashMap<String, Holder>();
		
		for(Holder h : aList)
			if( h.taxonomicLevel.equals(level))
			{
				if( returnMap.containsKey(h.taxonomicName))
					throw new Exception("Unexpected duplicate " + h.taxonomicName);
				
				returnMap.put(h.taxonomicName, h);
			}
				
		
		return returnMap;
	}
	
	private static double getTTestVal(Holder h) 
	{
		try
		{
			return TTest.ttestFromNumber(h.caseSamplesLogNormalized, h.controlSamplesLogNormalized).getPValue();
		}
		catch(Exception ex)
		{
			
		}
		
		return 1;
	}
	
	private static void writeNodeAndChildren( BufferedWriter writer,Holder h) throws Exception
	{
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		
		writer.write("{\n");
		
		writer.write("\"numSeqs\": " +  ((int)(h.caseCounts + h.controlCounts))+ ",\n");		
		writer.write("\"rpdLevel\": \"" +  h.taxonomicLevel + "\",\n");
		writer.write("\"rdptaxa\": \"" +  h.taxonomicName + "\",\n");
		
		double tTestVal = getTTestVal(h);
		writer.write("\"tTestP\": \"" +  nf.format(tTestVal)+ "\",\n");
		writer.write("\"t-log10TTesetP\": \"" +  nf.format( Math.log10( tTestVal))+ "\",\n");
		
		
		double ratio = h.caseCounts / (h.caseCounts + h.controlCounts) ;
		writer.write("\"fractionCase\": \"" + nf.format(ratio)+ "\"\n");
		
		
		if ( h.children.size() > 0) 
		{
			writer.write(",\"children\": [\n");
				
			for( Iterator<Holder> i = h.children.values().iterator(); i.hasNext(); )
			{
				writeNodeAndChildren(writer,i.next());
				if( i.hasNext())
					writer.write(",");
			}
					writer.write("]\n");
		}
		
		
		writer.write("}\n");
}
	
	private static class Holder
	{
		String taxonomicName;
		String taxonomicLevel;
		double caseCounts=0;
		double controlCounts=0;
		List<Number> caseSamplesLogNormalized =new ArrayList<Number>();
		List<Number> controlSamplesLogNormalized =new ArrayList<Number>();
		HashMap<String, Holder> children = new HashMap<String, Holder>();
	}
}
