package parsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;

import utils.TabReader;

public class OtuWrapper
{
	/*
	 * All of these lists will be made unmodifiable (and hence thread safe) in
	 * the constructor
	 */
	// lists are sample then otu
	private List<List<Double>> dataPointsNormalized = new ArrayList<List<Double>>();
	private List<List<Double>> dataPointsUnnormalized = new ArrayList<List<Double>>();
	private List<List<Double>> dataPointsNormalizedThenLogged = new ArrayList<List<Double>>();
	private List<String> sampleNames = new ArrayList<String>();
	private List<String> otuNames = new ArrayList<String>();
	private double avgNumber;
	private final String filePath;

	public double getAvgNumber()
	{
		return avgNumber;
	}
	
	public String getFilePath()
	{
		return filePath;
	}
	
	public static class MaxColumnHolder
	{
		public int taxaIndex;
		public double proportion;
	}
	
	public double getChaoRichness(String sampleName) throws Exception
	{
		return getChaoRichness(getIndexForSampleName(sampleName));
	}
	
	public double getChaoRichness( int sampleIndex) throws Exception
	{
		double richness = getRichness(sampleIndex);
		
		int singetons=0;
		int doubletons =0;
		
		List<Double> list = dataPointsUnnormalized.get(sampleIndex);
		
		for( Double d : list)
		{
			if( d== 1)
				singetons++;
			else if (d == 2)
				doubletons++;
		}
		
		return richness + singetons*(singetons-1) / 2*(doubletons +1);
	}
	
	/*
	 * Takes the median as the middle position.
	 * Doesn't average if there are an even number of taxa
	 */
	public double getTaxaMedian(int col)
	{
		List<Double> taxaList = new ArrayList<Double>();
		
		for( int x=0; x < getSampleNames().size(); x++)
			taxaList.add(dataPointsUnnormalized.get(x).get(col));
		
		Collections.sort(taxaList);
		
		return taxaList.get(taxaList.size() / 2);
	}
	
	public double getTaxaAverageExcludingZeros(int col)
	{
		double sum =0;
		int n=0;
		
		List<List<Double>> list = getDataPointsUnnormalized();
		
		for( int x=0; x< getSampleNames().size(); x++ )
		{
			double val = list.get(x).get(col);
			
			if( val != 0)
			{
				sum+=val;
				n++;
			}
		}
		
		return sum / n;
	}
	
	public double getMinimumExcludingZeros(int col)
	{
		double d = Double.MAX_VALUE;
		
		List<List<Double>> list = getDataPointsUnnormalized();
		
		for( int x=0; x< getSampleNames().size(); x++ )
		{
			double val = list.get(x).get(col);
			
			if( val != 0)
			{
				d = Math.min(d,  val);
			}
		}
		
		return d;
	}
	
	private static void throwIfDuplicateSamples(OtuWrapper wrapper1, OtuWrapper wrapper2)
		throws Exception
	{
		HashSet<String> set = new HashSet<String>();
		
		for(String s : wrapper1.getSampleNames())
		{
			if(set.contains(s))
				throw new Exception("Duplicate "  + s);
			
			set.add(s);
		}
		
		for(String s : wrapper2.getSampleNames())
		{
			if(set.contains(s))
				throw new Exception("Duplicate "  + s);
			
			set.add(s);
		}
	}
	
	public static void merge(File inFile1, File inFile2, File outFile) throws Exception
	{
		OtuWrapper wrapper1 = new OtuWrapper(inFile1);
		OtuWrapper wrapper2 = new OtuWrapper(inFile2);
		throwIfDuplicateSamples(wrapper1, wrapper2);
		
		HashSet<String> otuSet= new LinkedHashSet<String>();
		
		for(String s : wrapper1.getOtuNames())
			otuSet.add(s);
		
		for(String s : wrapper2.getOtuNames())
			otuSet.add(s);
		
		List<String> otuList = new ArrayList<String>();
		otuList.addAll(otuSet);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		
		writer.write("samples");
		
		for(String s: otuList)
			writer.write("\t" + s);
		
		writer.write("\n");
		addWrapper(wrapper1, writer, otuList);
		addWrapper(wrapper2, writer, otuList);
		writer.flush(); writer.close();
	}
	
	private static void addWrapper(OtuWrapper wrapper, BufferedWriter writer, List<String> otuList)
		throws Exception
	{
		for(String s : wrapper.getOtuNames() )
		{
			int sampleIndex = wrapper.getIndexForSampleName(s);
			writer.write(s);
			
			for( String s2 : otuList )
			{
				int index = wrapper.getIndexForOtuName(s2);
				if( index == -1 )
					writer.write("\t0");
				else
					writer.write("\t" + wrapper.getDataPointsUnnormalized().get(sampleIndex).get(index));
			}
			
			writer.write("\n");
		}
		
		writer.flush();

	}

	public int getIndexForOtuName(String s) throws Exception
	{
		for (int x = 0; x < otuNames.size(); x++)
			if (otuNames.get(x).equals(s))
				return x;
		
		return -1;
	}

	public int getIndexForSampleName(String s) throws Exception
	{
		for (int x = 0; x < sampleNames.size(); x++)
			if (sampleNames.get(x).equals(s))
				return x;
		
		return -1;
	}
	
	public double getNumberOfSequencesForOTU(String otu) throws Exception
	{
		double sum =0;
		int index = getIndexForOtuName(otu);
		
		for( int x=0; x < getSampleNames().size(); x++)
			sum += dataPointsUnnormalized.get(x).get(index);
		
		return sum;
	}
	
	public double getNumberOfSequencesForSampleWithMaxNumberOfSequences()
		throws Exception
	{
		double d = -1;
		
		for(String s : getSampleNames())
			d = Math.max(getNumberSequences(s), d);
		
		return d;
	}

	public double getNumberSequences(String sampleName) throws Exception
	{
		double num = 0;

		int sampleIndex = getIndexForSampleName(sampleName);

		for (int x = 0; x < otuNames.size(); x++)
			num += dataPointsUnnormalized.get(sampleIndex).get(x);

		return num;
	}

	public int getRichness(String sampleName) throws Exception
	{
		return getRichness(getIndexForSampleName(sampleName));
	}

	public int getRichness(int sampleIndex) throws Exception
	{
		int richness = 0;

		for (int x = 0; x < otuNames.size(); x++)
			if (dataPointsUnnormalized.get(sampleIndex).get(x) > 0.1)
				richness++;

		return richness;
	}

	/*
	 * Returns the number of species you observed on average for that index 
	 * over the numIterations
	 */
	public float[] getRarefactionCurve(int sampleIndex, int numIterations) throws Exception
	{
		List<Integer> otuList = new ArrayList<Integer>();
		
		List<Double> initialData = dataPointsUnnormalized.get(sampleIndex);
		
		int someVal =0;
		for( Double d : initialData)
		{
			someVal++;
			
			for( int x=0; x < d; x++)
				otuList.add(someVal);
		}
		
		float[] returnArray = new float[otuList.size()];
		
		for( int x=0; x < numIterations; x++)
		{
			Collections.shuffle(otuList);
			
			HashSet<Integer> observedOtus = new HashSet<Integer>();
			
			for( int y=0; y < otuList.size();y++)
			{
				observedOtus.add(otuList.get(y));
				returnArray[y] += observedOtus.size();
			}
		}
		
		for( int x=0; x< returnArray.length; x++)
			returnArray[x] = returnArray[x] / numIterations;
		
		return returnArray;
			
	}
	
	public float[] getRarefactionCurve(int sampleIndex, int numIterations, int limitNumSequences) 
		throws Exception
	{
		List<Integer> otuList = new ArrayList<Integer>();
		
		List<Double> initialData = dataPointsUnnormalized.get(sampleIndex);
		
		int someVal =0;
		for( Double d : initialData)
		{
			someVal++;
			
			for( int x=0; x < d; x++)
				otuList.add(someVal);
		}
		
		float[] returnArray = new float[limitNumSequences];
		
		for( int x=0; x < numIterations; x++)
		{
			Collections.shuffle(otuList);
			
			HashSet<Integer> observedOtus = new HashSet<Integer>();
			
			for( int y=0; y < limitNumSequences;y++)
			{
				observedOtus.add(otuList.get(y));
				returnArray[y] += observedOtus.size();
			}
		}
		
		for( int x=0; x< returnArray.length; x++)
			returnArray[x] = returnArray[x] / numIterations;
		
		return returnArray;
			
	}
	
	public MaxColumnHolder getMostAbundantTaxa(int sampleIndex)
	{
		List<Double> innerList = dataPointsNormalized.get(sampleIndex);

		MaxColumnHolder mch = new MaxColumnHolder();

		for (int x = 0; x < innerList.size(); x++)
		{
			double val = innerList.get(x);

			if (mch.proportion < val)
			{
				mch.proportion = val;
				mch.taxaIndex = x;
			}
		}

		return mch;
	}

	public List<String> getOtuNames()
	{
		return otuNames;
	}

	public List<String> getSampleNames()
	{
		return sampleNames;
	}
	
	public static double crank(List<Double> w)
	{
		double s;
		
		int j=1,ji,jt;
		double t,rank;

		int n=w.size();
		s=0.0f;
		while (j < n) 
		{
			if ( ! w.get(j).equals(w.get(j-1)))
			{
				w.set(j-1,j + 0.0);
				++j;
			} 
			else 
			{
				for (jt=j+1;jt<=n && w.get(jt-1).equals(w.get(j-1));jt++);
				rank=0.5f*(j+jt-1);
				for (ji=j;ji<=(jt-1);ji++)
					w.set(ji-1,rank);
				t=jt-j;
				s += (t*t*t-t);
				j=jt;
			}
		}
		if (j == n) w.set(n-1,n + 0.0);
		
		return s;
	}
	
	private static class RankHolder
	{
		int originalIndex;
		double rank;
		double originalData;
	}
	
	public List<List<Double>> getRankNormalizedDataPoints()
	{
		List<List<Double>> rankList = new ArrayList<List<Double>>();
		
		for( int x=0; x < getSampleNames().size(); x++)
		{
			List<RankHolder> innerRanks = new ArrayList<RankHolder>();
			
			for( int y=0; y < getOtuNames().size(); y++ )
			{
				RankHolder rh = new RankHolder();
				rh.originalData = getDataPointsNormalized().get(x).get(y);
				rh.originalIndex = y;
				innerRanks.add(rh);
			}
			
			Collections.sort(innerRanks, new Comparator<RankHolder>()
					{@Override
					public int compare(RankHolder o1, RankHolder o2)
					{
						return Double.compare(o1.originalData, o2.originalData);
					}});
			
			List<Double> crankedList = new ArrayList<Double>();
			
			for( RankHolder rh : innerRanks)
				crankedList.add(rh.originalData);
			crank(crankedList);
			
			for( int y=0; y < innerRanks.size(); y++)
				innerRanks.get(y).rank = crankedList.get(y);
			
			double[] ranks = new double[innerRanks.size()];
			
			for( int y=0; y < innerRanks.size(); y++)
			{
				RankHolder rh = innerRanks.get(y);
				ranks[rh.originalIndex] = rh.rank;
			}
			
			List<Double> newList = new ArrayList<Double>();
			
			for( Double d : ranks)
				newList.add(d);
			
			rankList.add(newList);
		}
		
		return rankList;
	}

	public List<List<Double>> getDataPointsNormalized()
	{
		return dataPointsNormalized;
	}

	public List<List<Double>> getDataPointsNormalizedThenLogged()
	{
		return dataPointsNormalizedThenLogged;
	}

	public boolean hasOneOverThreshold(int otuIndex, double threshold)
	{
		for (int x = 0; x < getDataPointsUnnormalized().size(); x++)
		{
			double d = getDataPointsUnnormalized().get(x).get(otuIndex);

			if (d >= threshold)
				return true;
		}

		return false;
	}

	// samples as columns; no taxa names
	public void writeLoggedNormalizedDataForR( File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		for( int x=0; x < getSampleNames().size(); x++)
			writer.write( getSampleNames().get(x) + (x < getSampleNames().size()-1 ? "\t" : "\n") );
		
		for( int y=0; y < getOtuNames().size(); y++)
		{
			for( int x=0; x < getSampleNames().size(); x++)
				writer.write( getDataPointsNormalizedThenLogged().get(x).get(y)
						+ (x < getSampleNames().size()-1 ? "\t" : "\n") );	
		}
		
		writer.flush();  writer.close();
	}
	

	// samples as columns; no taxa names
	public void writePresentAbsenceDataForR( File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		for( int x=0; x < getSampleNames().size(); x++)
			writer.write( getSampleNames().get(x) + (x < getSampleNames().size()-1 ? "\t" : "\n") );
		
		for( int y=0; y < getOtuNames().size(); y++)
		{
			for( int x=0; x < getSampleNames().size(); x++)
				writer.write( 
						(getDataPointsUnnormalized().get(x).get(y) > 0.1 ? "1" : "0") + 
						 (x < getSampleNames().size()-1 ? "\t" : "\n") );	
		}
		
		writer.flush();  writer.close();
	}

	
	public void writeLoggedDataWithTaxaAsColumns(File file) throws Exception
	{
		writeLoggedDataWithTaxaAsColumns(file, this.sampleNames, this.otuNames);
	}

	public double getTaxaSum(String sample, List<String> otus) throws Exception
	{
		double sum = 0;

		int sampleIndex = getIndexForSampleName(sample);

		for (String s : otus)
		{
			int otuIndex = getIndexForOtuName(s);
			sum += dataPointsNormalized.get(sampleIndex).get(otuIndex);
		}

		return sum;
	}

	public void writeLoggedDataWithTaxaAsColumns(File file,
			List<String> newFileNames, List<String> newOtuNames)
			throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (String s : newOtuNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < getSampleNames().size(); x++)
		{
			writer.write("S_" + newFileNames.get(x));

			for (int y = 0; y < getOtuNames().size(); y++)
			{
				writer.write("\t"
						+ dataPointsNormalizedThenLogged.get(x).get(y));
			}

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}
	
	public void writeUnnormalizedFirstTaxaWithTaxaAsColumns(File file, int numTaxa)
		throws Exception
	{

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (int x=0; x< numTaxa; x++)
			writer.write("\t" + getOtuNames().get(x));

		writer.write("\tother\n");

		for (int x = 0; x < getSampleNames().size(); x++)
		{
			writer.write(getSampleNames().get(x));

			for (int y = 0; y < numTaxa; y++)
			{
				writer.write("\t" + dataPointsNormalized.get(x).get(y));
				
			}


			double sum =0;
			
			for( int y=numTaxa; y < getOtuNames().size(); y++)
				sum += dataPointsNormalized.get(x).get(y);
			
			writer.write("\t" + sum + "\n");
		}

		writer.flush();
		writer.close();
	}
	
	public void writeNormalizedUnloggedDataWithSamplesAsColumns(File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		writer.write("taxa");
		
		for( String s: getSampleNames())
			writer.write("\t" + s);
		
		writer.write("\n");
		
		for( int x=0; x < getOtuNames().size(); x++)
		{
			writer.write( getOtuNames().get(x) );
			
			for( int y=0; y < getSampleNames().size(); y++)
			{
				writer.write("\t" + dataPointsNormalized.get(y).get(x));
			}
			
			writer.write("\n");
		}
		
		writer.flush();  writer.close();
		
	}

	public void writeNormalizedUnloggedDataWithTaxaAsColumns(File file,
			List<String> newFileNames, List<String> newOtuNames)
			throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (String s : newOtuNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < getSampleNames().size(); x++)
		{
			writer.write(newFileNames.get(x));

			for (int y = 0; y < getOtuNames().size(); y++)
			{
				writer.write("\t" + dataPointsNormalized.get(x).get(y));
			}

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}

	
	public void writeRawDataWithTaxaAsColumns(String filePath) throws Exception
	{
		writeRawDataWithTaxaAsColumns(new File(filePath));
	}
	
	public void writeRawDataWithTaxaAsColumns(File file) throws Exception
	{
		writeRawDataWithTaxaAsColumns(file, this.getSampleNames(), this.getOtuNames());
	}
	
	public void writeRawDataWithTaxaAsColumns(File file,
			List<String> newFileNames, List<String> newOtuNames)
			throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (String s : newOtuNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < getSampleNames().size(); x++)
		{
			writer.write(newFileNames.get(x));

			for (int y = 0; y < getOtuNames().size(); y++)
			{
				writer.write("\t" + dataPointsUnnormalized.get(x).get(y));
			}

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}

	public void writeunLoggedDataWithTaxaAsColumns(File file,
			List<String> newFileNames, List<String> newOtuNames)
			throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (String s : newOtuNames)
			writer.write("\t" + "S_" + s);

		writer.write("\n");

		for (int x = 0; x < getSampleNames().size(); x++)
		{
			writer.write(newFileNames.get(x));

			for (int y = 0; y < getOtuNames().size(); y++)
			{
				writer.write("\t" + dataPointsNormalized.get(x).get(y));
			}

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}

	public void writeloggedDataWithSamplesAsColumns(File file) throws Exception
	{
		writeloggedDataWithSamplesAsColumns(file, getSampleNames(),
				getOtuNames());
	}

	public void writeloggedDataWithSamplesAsColumns(File file,
			List<String> newFileNames, List<String> newOtuNames)
			throws Exception
	{
		if (newFileNames == null)
			newFileNames = getSampleNames();

		if (newOtuNames == null)
			newOtuNames = getOtuNames();

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("taxa");

		for (String s : newFileNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < getOtuNames().size(); x++)
		{
			writer.write(newOtuNames.get(x));

			for (int y = 0; y < getSampleNames().size(); y++)
				writer.write("\t"
						+ dataPointsNormalizedThenLogged.get(y).get(x));

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}

	public void writeunLoggedDataWithSamplesAsColumns(File file,
			List<String> newFileNames, List<String> newOtuNames)
			throws Exception
	{
		if (newFileNames == null)
			newFileNames = getSampleNames();

		if (newOtuNames == null)
			newOtuNames = getOtuNames();

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("taxa");

		for (String s : newFileNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < getOtuNames().size(); x++)
		{
			writer.write(newOtuNames.get(x));

			for (int y = 0; y < getSampleNames().size(); y++)
				writer.write("\t" + dataPointsNormalized.get(y).get(x));

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}

	public List<Double> getUnnormalizedDataForTaxa(String taxa)
			throws Exception
	{
		List<Double> list = new ArrayList<Double>();

		int index = -1;

		for (int x = 0; x < otuNames.size(); x++)
			if (otuNames.get(x).equals(taxa))
				index = x;

		if (index == -1)
			throw new Exception("Can't find " + taxa);

		for (int x = 0; x < dataPointsUnnormalized.size(); x++)
			list.add(dataPointsUnnormalized.get(x).get(index));

		return list;
	}

	public List<List<Double>> getDataPointsUnnormalized()
	{
		return dataPointsUnnormalized;
	}

	public double getShannonEntropy(String sampleName) throws Exception
	{
		return getShannonEntropy(getIndexForSampleName(sampleName));
	}
	
	public double getShannonEntropy(int sampleIndex) throws Exception
	{
		double sum = 0;

		List<Double> innerList = getDataPointsUnnormalized().get(sampleIndex);

		for (Double d : innerList)
			sum += d;

		List<Double> newList = new ArrayList<Double>();

		for (Double d : innerList)
			newList.add(d / sum);

		sum = 0;
		for (Double d : newList)
			if (d > 0)
			{
				sum += d * Math.log(d);

			}

		return -sum;
	}
	
	public double getSimpsonsDiversity(String sample) throws Exception
	{
		return getSimpsonsDiversity(getIndexForSampleName(sample));
	}

	public double getSimpsonsDiversity(int sampleIndex)
	{
		double sum = 0;

		List<Double> innerList = getDataPointsUnnormalized().get(sampleIndex);

		for (Double d : innerList)
			if (d > 0.1)
				sum += d;

		double returnVal = 0;

		for (Double d : innerList)
			if (d > 0.1)
			{
				double n = d / sum;

				returnVal += n * n;
			}

		return returnVal;

	}

	public double getEvenness(String sampleName)  throws Exception
	{
		return getEvenness(getIndexForSampleName(sampleName));
	}
	
	public double getEvenness(int sampleIndex) throws Exception
	{
		double sum = 0;

		List<Double> innerList = getDataPointsUnnormalized().get(sampleIndex);

		for (Double d : innerList)
			if (d > 0.1)
				sum++;

		return getShannonEntropy(sampleIndex) / Math.log(sum);
	}

	public double[][] getUnnorlalizedAsArray()
	{
		double[][] d = new double[dataPointsUnnormalized.size()][dataPointsUnnormalized
				.get(0).size()];

		for (int x = 0; x < dataPointsUnnormalized.size(); x++)
			for (int y = 0; y < dataPointsUnnormalized.get(0).size(); y++)
				d[x][y] = dataPointsUnnormalized.get(x).get(y);

		return d;
	}

	public double[][] getPresenceAbsenceArray()
	{
		double[][] d = new double[dataPointsUnnormalized.size()][dataPointsUnnormalized.get(0).size()];
		                                          
		for (int x = 0; x < dataPointsUnnormalized.size(); x++)
			for (int y = 0; y < dataPointsUnnormalized.get(0).size(); y++)
				d[x][y] = (dataPointsUnnormalized.get(x).get(y) > 0.1 ? 1 : 0);

		return d;

	}
	
	public double[][] getNormalizedThenLoggedAsArray()
	{
		double[][] d = new double[dataPointsNormalizedThenLogged.size()][dataPointsNormalizedThenLogged
				.get(0).size()];
		// new
		// double[dataPointsNormalizedThenLogged.get(0).size()][dataPointsNormalizedThenLogged.size()];

		for (int x = 0; x < dataPointsNormalizedThenLogged.size(); x++)
			for (int y = 0; y < dataPointsNormalizedThenLogged.get(0).size(); y++)
				d[x][y] = dataPointsNormalizedThenLogged.get(x).get(y);

		return d;
	}
	
	public double[][] getLoggedAsArray()
	{
		double[][] d = new double[dataPointsUnnormalized.size()][dataPointsUnnormalized
				.get(0).size()];
		// new
		// double[dataPointsNormalizedThenLogged.get(0).size()][dataPointsNormalizedThenLogged.size()];

		for (int x = 0; x < dataPointsUnnormalized.size(); x++)
			for (int y = 0; y < dataPointsUnnormalized.get(0).size(); y++)
				d[x][y] =  Math.log10( dataPointsUnnormalized.get(x).get(y));

		return d;
	}
	
	public double[][] getAsArray()
	{
		double[][] d = new double[dataPointsUnnormalized.size()][dataPointsUnnormalized
				.get(0).size()];
		// new
		// double[dataPointsNormalizedThenLogged.get(0).size()][dataPointsNormalizedThenLogged.size()];

		for (int x = 0; x < dataPointsUnnormalized.size(); x++)
			for (int y = 0; y < dataPointsUnnormalized.get(0).size(); y++)
				d[x][y] =  dataPointsUnnormalized.get(x).get(y);

		return d;
	}

	public double[][] getNormalizedAsArray()
	{
		double[][] d = new double[dataPointsNormalized.size()][dataPointsNormalized
				.get(0).size()];

		for (int x = 0; x < dataPointsNormalized.size(); x++)
			for (int y = 0; y < dataPointsNormalized.get(0).size(); y++)
				d[x][y] = dataPointsNormalized.get(x).get(y);

		return d;
	}
	
	public double[][] getCubeRootNormalizedAsArray()
	{
		double[][] d = new double[dataPointsNormalized.size()][dataPointsNormalized
				.get(0).size()];

		for (int x = 0; x < dataPointsNormalized.size(); x++)
			for (int y = 0; y < dataPointsNormalized.get(0).size(); y++)
				d[x][y] = Math.pow( dataPointsNormalized.get(x).get(y), (1/3.0));

		return d;
	}
	
	public void writeUnnormalizedDataToFile(File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (String s : otuNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < sampleNames.size(); x++)
		{
			writer.write(sampleNames.get(x));

			for (int y = 0; y < otuNames.size(); y++)
				writer.write("\t" + dataPointsUnnormalized.get(x).get(y));

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}

	public void writeNormalizedDataToFile(File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (String s : otuNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < sampleNames.size(); x++)
		{
			writer.write(sampleNames.get(x));

			for (int y = 0; y < otuNames.size(); y++)
				writer.write("\t" + dataPointsNormalized.get(x).get(y));

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}

	public void writeNormalizedLoggedDataToFile(String filePath) throws Exception
	{
		writeNormalizedLoggedDataToFile(new File(filePath));
	}
	
	public void writeNormalizedLoggedDataToFile(File file) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (String s : otuNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < sampleNames.size(); x++)
		{
			writer.write(sampleNames.get(x));

			for (int y = 0; y < otuNames.size(); y++)
				writer.write("\t" + dataPointsNormalizedThenLogged.get(x).get(y));

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}
	
	public void writeNormalizedLoggedDataToFile(File file, List<String> newSampleNames) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("sample");

		for (String s : otuNames)
			writer.write("\t" + s);

		writer.write("\n");

		for (int x = 0; x < sampleNames.size(); x++)
		{
			writer.write(newSampleNames.get(x));

			for (int y = 0; y < otuNames.size(); y++)
				writer.write("\t" + dataPointsNormalizedThenLogged.get(x).get(y));

			writer.write("\n");
		}

		writer.flush();
		writer.close();
	}
	
	
	public HashMap<String, Double> getNormalizedDataAsMap() throws Exception
	{
		HashMap<String, Double> map = new HashMap<String, Double>();

		for (int x = 0; x < sampleNames.size(); x++)
		{
			String sampleName = sampleNames.get(x);
			sampleName = new StringTokenizer(sampleName, "_").nextToken();

			for (int y = 0; y < otuNames.size(); y++)
			{
				String key = otuNames.get(y) + "@" + sampleName;

				if (map.containsKey(key))
					throw new Exception("Duplicate key");

				map.put(key, dataPointsNormalized.get(x).get(y));
			}
		}

		return map;
	}

	public HashMap<String, Double> getLoggedNormalizedDataAsMap()
			throws Exception
	{
		HashMap<String, Double> map = new HashMap<String, Double>();

		for (int x = 0; x < sampleNames.size(); x++)
		{
			String sampleName = sampleNames.get(x);
			sampleName = new StringTokenizer(sampleName, "_").nextToken();

			for (int y = 0; y < otuNames.size(); y++)
			{
				String key = otuNames.get(y) + "@" + sampleName;

				if (map.containsKey(key))
					throw new Exception("Duplicate key");

				map.put(key, dataPointsNormalizedThenLogged.get(x).get(y));
			}
		}

		return map;
	}

	public int getTotalCounts() throws Exception
	{
		int sum = 0;

		for (int x = 0; x < getOtuNames().size(); x++)
			sum += getCountsForTaxa(x);

		return sum;
	}

	public int getCountsForTaxa(String s) throws Exception
	{
		return getCountsForTaxa(getIndexForOtuName(s));
	}
	
	public int getCountsForTaxa(int index) throws Exception
	{
		double counts = 0;

		for (int x = 0; x < getDataPointsUnnormalized().size(); x++)
			counts += getDataPointsUnnormalized().get(x).get(index);

		return (int) (counts + 0.1);
	}

	public int getCountsForSample(int index) throws Exception
	{
		double counts = 0;

		for (int x = 0; x < getDataPointsUnnormalized().get(index).size(); x++)
			counts += getDataPointsUnnormalized().get(index).get(x);

		return (int) (counts + 0.1);

	}
	
	public int getCountsForSample(String sample) throws Exception
	{
		return getCountsForSample(getIndexForSampleName(sample));
	}

	public Double getNormalizedLoggedDataPoint(String sample, String taxa)
			throws Exception
	{
		int x = -1;

		for (int i = 0; x == -1 && i < getSampleNames().size(); i++)
			if (sample.equals(getSampleNames().get(i)))
				x = i;

		int y = -1;
		for (int i = 0; y == -1 && i < getOtuNames().size(); i++)
			if (taxa.equals(getOtuNames().get(i)))
				y = i;

		if (x == -1 || y == -1)
			return null;

		return dataPointsNormalizedThenLogged.get(x).get(y);
	}

	public Double getUnnormalizedDataPoint(String sample, String taxa)
			throws Exception
	{
		int x = -1;

		for (int i = 0; x == -1 && i < getSampleNames().size(); i++)
			if (sample.equals(getSampleNames().get(i)))
				x = i;

		int y = -1;
		for (int i = 0; y == -1 && i < getOtuNames().size(); i++)
			if (taxa.equals(getOtuNames().get(i)))
				y = i;

		if (x == -1 || y == -1)
			return null;

		return dataPointsUnnormalized.get(x).get(y);
	}

	public OtuWrapper(String filePath) throws Exception
	{
		this(new File(filePath));
	}

	public OtuWrapper(File f) throws Exception
	{
		this(f, null, null);
	}

	private static boolean excludeTaxa(String taxaName,
			HashSet<String> excludedTaxa)
	{
		if (excludedTaxa == null)
			return true;

		for (String s : excludedTaxa)
			if (s.equalsIgnoreCase(taxaName))
				return true;

		return false;
	}

	public OtuWrapper(String filepath, HashSet<String> excludedSamples,
			HashSet<String> excludedTaxa) throws Exception
	{
		this(filepath, excludedSamples, excludedTaxa, 0.01);
	}

	public OtuWrapper(String filepath, HashSet<String> excludedSamples,
			HashSet<String> excludedTaxa, double threshold) throws Exception
	{
		this(new File(filepath), excludedSamples, excludedTaxa, threshold);
	}

	public OtuWrapper(File f, HashSet<String> excludedSamples,
			HashSet<String> excludedTaxa) throws Exception
	{
		this(f, excludedSamples, excludedTaxa, -1000);
	}
	
	public double getBrayCurtis(int i, int j, boolean log)
	{
		double si = 0;
		double sj =0;
		double cij =0;
		
		List<List<Double>> list = getDataPointsNormalized();
		
		if(log)
			list = getDataPointsNormalizedThenLogged();
		
		for( int x=0; x < getOtuNames().size(); x++)
		{
			si+= list.get(i).get(x);
			sj+= list.get(j).get(x);
			cij += Math.abs( list.get(i).get(x) - list.get(j).get(x));
		}

		return  cij / (si + sj);
	}
	
	public void writeBrayCurtisForMothur(String filepath, boolean log) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filepath)));
		
		writer.write(getSampleNames().size() + "\n");
		
		
		for(int x=0; x < getSampleNames().size(); x++)
		{
			writer.write(getSampleNames().get(x));
			
			for( int y=0; y < getSampleNames().size(); y++)
				writer.write("\t" + getBrayCurtis(x, y,log));
			
			writer.write("\n");
		}
		
		writer.flush();  writer.close();
	}

	public OtuWrapper(File f, HashSet<String> excludedSamples,
			HashSet<String> excludedTaxa, double threshold) throws Exception
	{
		this.filePath = f.getAbsolutePath();
		BufferedReader reader = new BufferedReader(new FileReader(f));

		String nextLine = reader.readLine();

		TabReader tr = new TabReader(nextLine);

		tr.nextToken();

		HashSet<Integer> skipColumns = new HashSet<Integer>();

		int x = 0;
		while (tr.hasMore())
		{
			String taxaName = tr.nextToken();

			if (taxaName.startsWith("\"") && taxaName.endsWith("\""))
				taxaName = taxaName.substring(1, taxaName.length() - 1);

			if (excludedTaxa == null || !excludeTaxa(taxaName, excludedTaxa))
			{
				otuNames.add(taxaName);
			} else
			{
				skipColumns.add(x);
				System.out.println("Wrapper excluding taxa " + taxaName);
			}

			x++;

		}

		nextLine = reader.readLine();

		int totalCounts = 0;
		while (nextLine != null)
		{
			tr = new TabReader(nextLine);

			String sampleName = tr.nextToken();

			boolean includeSample = true;

			if (excludedSamples != null)
				for (String s : excludedSamples)
					if (sampleName.equals(s))
						includeSample = false;

			if (includeSample)
			{
				sampleNames.add(sampleName);
				List<Double> innerList = new ArrayList<Double>();
				dataPointsUnnormalized.add(innerList);
				dataPointsNormalized.add(new ArrayList<Double>());
				dataPointsNormalizedThenLogged.add(new ArrayList<Double>());

				x = 0;
				while (tr.hasMore())
				{
					String nextToken = tr.nextToken();

					double d = 0;

					if (nextToken.length() > 0)
						d = Double.parseDouble(nextToken);

					if (!skipColumns.contains(x))
					{
						innerList.add(d);
						totalCounts += d;
					}

					x++;
				}
			} else
			{
				System.out.println("Wrapper excluding " + sampleName);
			}

			if (x != skipColumns.size() + otuNames.size())
				throw new Exception("Logic error");

			nextLine = reader.readLine();
		}

		// System.out.println( sampleNames.size() + " " + otuNames.size());
		assertNum(totalCounts, dataPointsUnnormalized);
		removeThreshold(otuNames, dataPointsUnnormalized, threshold);

		if (threshold < 0.1)
		{
			assertNoZeros(dataPointsUnnormalized);
			assertNum(totalCounts, dataPointsUnnormalized);
		}

		avgNumber = ((double) totalCounts) / dataPointsNormalized.size();
		//avgNumber =1;	
		
		for (x = 0; x < dataPointsUnnormalized.size(); x++)
		{
			List<Double> unnormalizedInnerList = dataPointsUnnormalized.get(x);
			double sum = 0;

			for (Double d : unnormalizedInnerList)
				sum += d;

			List<Double> normalizedInnerList = dataPointsNormalized.get(x);
			List<Double> loggedInnerList = dataPointsNormalizedThenLogged
					.get(x);

			for (int y = 0; y < unnormalizedInnerList.size(); y++)
			{
				double val = avgNumber * unnormalizedInnerList.get(y) / sum;
				normalizedInnerList.add(val);
				loggedInnerList.add(Math.log10(val + 1));
			}
		}

		this.dataPointsNormalized = Collections
				.unmodifiableList(this.dataPointsNormalized);
		this.dataPointsNormalizedThenLogged = Collections
				.unmodifiableList(this.dataPointsNormalizedThenLogged);
		this.dataPointsUnnormalized = Collections
				.unmodifiableList(this.dataPointsUnnormalized);
		this.otuNames = Collections.unmodifiableList(otuNames);
		this.sampleNames = Collections.unmodifiableList(sampleNames);
	}
	
	public float getFractionZeroForTaxa(int taxaIndex ) 
	{
		float f =0;
		
		for( int x=0; x < getSampleNames().size(); x++)
			if( dataPointsUnnormalized.get(x).get(taxaIndex) < 0.001  )
				f = f +1;
		
		//System.out.println( getOtuNames().get(taxaIndex) + " " + f + " " + getSampleNames().size() );
		return f / getSampleNames().size();
	}

	private static void assertNoZeros(List<List<Double>> dataPointsUnnormalized)
			throws Exception
	{
		for (int x = 0; x < dataPointsUnnormalized.size(); x++)
		{
			for (int y = 0; y < dataPointsUnnormalized.get(x).size(); y++)
			{
				double sum = 0;

				for (Double d : dataPointsUnnormalized.get(x))
					sum += d;

				if (sum == 0)
					throw new Exception("Logic error");

			}
		}
	}

	private static void assertNum(int totalCounts,
			List<List<Double>> dataPointsUnnormalized) throws Exception
	{
		int sum = 0;

		for (int x = 0; x < dataPointsUnnormalized.size(); x++)
			for (int y = 0; y < dataPointsUnnormalized.get(x).size(); y++)
				sum += dataPointsUnnormalized.get(x).get(y);

		if (totalCounts != sum)
			throw new Exception("Logic error " + totalCounts + " " + sum);

		if (dataPointsUnnormalized.size() > 0)
		{
			int length = dataPointsUnnormalized.get(0).size();

			for (int x = 0; x < dataPointsUnnormalized.size(); x++)
				if (length != dataPointsUnnormalized.get(x).size())
					throw new Exception("Jagged array");
		}
	}

	private static void removeThreshold(List<String> otuNames,
			List<List<Double>> dataPointsUnNormalized, double threshold)
	{
		List<Boolean> removeList = new ArrayList<Boolean>();

		for (int x = 0; x < otuNames.size(); x++)
		{
			int sum = 0;

			for (int y = 0; y < dataPointsUnNormalized.size(); y++)
			{
				sum += dataPointsUnNormalized.get(y).get(x);
			}

			if (sum <= threshold)
				removeList.add(true);
			else
				removeList.add(false);
		}

		for (int y = 0; y < dataPointsUnNormalized.size(); y++)
		{
			int x = 0;

			for (Iterator<Double> i = dataPointsUnNormalized.get(y).iterator(); i
					.hasNext();)
			{
				i.next();
				if (removeList.get(x))
					i.remove();

				x++;
			}
		}

		int x = 0;

		for (Iterator<String> i = otuNames.iterator(); i.hasNext();)
		{
			i.next();
			if (removeList.get(x))
				i.remove();

			x++;
		}
	}
}
