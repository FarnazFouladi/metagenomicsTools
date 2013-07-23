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


package scripts.clusterManipulations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import parsers.FastaSequence;
import parsers.FastaSequenceOneAtATime;

public class SplitFasta
{
	public static final int SPLIT_SIZE = 1000;
	
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage SplitFasta fileToSplit");
			System.exit(1);
		}
		
		FastaSequenceOneAtATime fsoat = new FastaSequenceOneAtATime(args[0]);
		
		int count=0;
		int file =1;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(args[0] + "_FILE_" + file)));
		
		for( FastaSequence fs = fsoat.getNextSequence(); fs != null; fs = fsoat.getNextSequence() )
		{
			count++;
			
			if( count == SPLIT_SIZE)
			{
				writer.flush();  writer.close();
				count =0;
				file++;
				writer = new BufferedWriter(new FileWriter(new File(args[0] + "_FILE_" + file)));
			}
			
			writer.write(">" + fs.getHeader() + "\n");
			writer.write(fs.getSequence() + "\n");
		}
		
		writer.flush();  writer.close();
	}
}
