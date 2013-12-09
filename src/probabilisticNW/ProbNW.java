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


package probabilisticNW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProbNW
{
	public static final int MATCH_REWARD= 1;
	public static final int MISMATCH_PENALTY = -3;
	public static final int GAP_PENALTY =-2;
	
	public static ProbSequence align( ProbSequence seq1, ProbSequence seq2  ) throws Exception
	{
		NwCell[][] matrix = getMatrix(seq1, seq2);
		//printMatrix(matrix);
		//System.out.println("SUCCESFUL ALIGN" );
		return traceback(seq1, seq2, matrix);
	}
	
	private static ProbSequence traceback( ProbSequence seq1, ProbSequence seq2 , NwCell[][] matrix ) 
			throws Exception
	{
		List<ProbColumn> list = new ArrayList<ProbColumn>();
		
		int y= seq1.getColumns().size();
		int x = seq2.getColumns().size();
		
		double score = matrix[x][y].getScore();
		
		while( x != 0 || y != 0)
		{
			//System.out.println(x + " " + y);
			NwCell cell = matrix[x][y];
			
			if( cell.getDirection().equals( NwCell.Direction.DIAGNOL ) )
			{
				list.add( seq1.getColumns().get(y-1).merge(seq2.getColumns().get(x-1)));
				x--;
				y--;
			}
			else if ( cell.getDirection().equals(NwCell.Direction.UP))
			{
				list.add( new ProbColumn('-', seq1.getNumRepresentedSequences()).merge(seq2.getColumns().get(x-1)) );
				x--;
			}
			else if( cell.getDirection().equals(NwCell.Direction.LEFT))
			{
				list.add( new ProbColumn('-', seq2.getNumRepresentedSequences()).merge(seq1.getColumns().get(y-1)) );
				y--;
			}
			else throw new Exception("LOGIC ERROR "  + x + " " + y);
		}
		
		Collections.reverse(list);
		ProbSequence probSeq = new ProbSequence(list, seq1.getNumRepresentedSequences() + seq2.getNumRepresentedSequences());
		probSeq.setAlignmentScore(score);
		return probSeq;
	}
	
	public static void printMatrix( NwCell[][] matrix )
	{
		for(int x=0; x < matrix.length; x++)
		{
			for(int y=0; y < matrix[x].length; y++)
			{
				System.out.print( matrix[x][y] + "\t");
			}
			
			System.out.println("");
		}
	}
	
	public static NwCell[][] getMatrix( ProbSequence seq1, ProbSequence seq2   ) throws Exception
	{
		NwCell[][] matrix  = new NwCell[seq2.getColumns().size() + 1][seq1.getColumns().size() + 1  ];
		
		matrix[0][0] = new NwCell(0, NwCell.Direction.INIT);
		
		for (int x=1; x <= seq1.getColumns().size(); x++)
			matrix[0][x] = new NwCell( 0, NwCell.Direction.LEFT );
		
		for (int x=1; x <= seq2.getColumns().size(); x++)
			matrix[x][0] = new NwCell( 0, NwCell.Direction.UP);
		
		for( int y=1; y <= seq1.getColumns().size(); y++)
		{
			ProbColumn seq1Col = seq1.getColumns().get(y-1);
			
			for( int x=1; x <= seq2.getColumns().size(); x++)
			{
				ProbColumn seq2Col = seq2.getColumns().get(x-1);
				
				double diagScore = seq1Col.getScoreDiag(seq2Col, MATCH_REWARD, MISMATCH_PENALTY, GAP_PENALTY) + 
							matrix[x-1][y-1].getScore()	;
				
				double upScore = matrix[x-1][y].getScore() + GAP_PENALTY;
				double leftScore = matrix[x][y-1].getScore() + GAP_PENALTY;
				
				double max = Math.max(diagScore, upScore);
				max = Math.max(max, leftScore);
				
				if( max == diagScore)
				{
					matrix[x][y] = new NwCell( diagScore, NwCell.Direction.DIAGNOL );
				}
				else if ( max == leftScore)
				{
					matrix[x][y] = new NwCell( leftScore, NwCell.Direction.LEFT);
				}
				else if ( max == upScore)
				{
					matrix[x][y] = new NwCell(upScore, NwCell.Direction.UP);
				}
				else throw new Exception(x + " " + y + " " + seq1.toString() + " " + seq2.toString() + " " +  "Logic error " + max + " " + diagScore + " " + leftScore + "  " + upScore + " " + seq1Col + "  " + seq2Col);
				
			}
		}
		
		return matrix;
	}
	
	
	
	public static void main(String[] args) throws Exception
	{
		ProbSequence probSeq1 = new ProbSequence("ACC","Sample1");
		ProbSequence probSeq2 = new ProbSequence("AGGC","Sample1");
		
		System.out.println(probSeq1);
		System.out.println(probSeq2);
		
		ProbSequence aligned =  align(probSeq1, probSeq2);
		
		System.out.println( aligned );
		
		ProbSequence probSeq3 = new ProbSequence("ACT","Sample1");
		aligned =  align(aligned, probSeq3);
		System.out.println( aligned );
		
		ProbSequence probSeq4 = new ProbSequence("TGGC","Sample1");
		aligned =  align(aligned, probSeq4);
		System.out.println( aligned );

		
		/*
		ProbSequence probSeq3 = new ProbSequence("ACCGGA");
		aligned =  align(aligned, probSeq3);
		System.out.println( aligned );
		
		ProbSequence probSeq4 = new ProbSequence("ACCGTA");
		aligned =  align(aligned, probSeq4);
		System.out.println( aligned );
		*/
		
	}
}
