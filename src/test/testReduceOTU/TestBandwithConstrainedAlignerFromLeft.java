/** 
 * Author:  anthony.fodor@gmail.com
 * 
 * This code is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version,
* provided that any use properly credits the author.
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details at http://www.gnu.org * * */

package test.testReduceOTU;

import java.util.List;

import dynamicProgramming.PairedAlignment;

import reduceOTU.DP_Expand;
import reduceOTU.IndividualEdit;
import reduceOTU.ReducedTools;
import junit.framework.TestCase;

public class TestBandwithConstrainedAlignerFromLeft extends TestCase
{
	public void testSingleMisMatch() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 = "ACTGACG" + common;
		String s2 = "ACTGACT" + common;
		
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
						32, 3);
		
		List<IndividualEdit> list = dp.getEditList();
		assertEquals(list.size(),1);
		assertEquals(dp.getNumErrors(),1);
		//System.out.println(list);
		assertEquals(list.get(0).toString(),"S6T" );
		assertTrue(dp.alignmentWasSuccesful());
		

		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
		//System.out.println(pa);
	}
	
	public void testFailedAlignment()  throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 = "AAAA" + common;
		String s2 = "CCCC" + common;
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 3);
		//System.out.println(dp.getEditList());
		assertFalse(dp.alignmentWasSuccesful());
		List<IndividualEdit> list = dp.getEditList();
		
		assertEquals(list.size(),4);
		assertEquals(dp.getNumErrors(),4);
		
		dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 4);
		//System.out.println(dp.getEditList());
		assertTrue(dp.alignmentWasSuccesful());
		list = dp.getEditList();
		
		assertEquals(list.size(),4);
		assertEquals(dp.getNumErrors(),4);
		
		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
		//System.out.println(pa.toString());
	}


	public void testSingleLeftAlignmentInsertionInString2() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 = "ACTGACTG" + common;
		String s2 = "ACTGACT" + common;
		
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
						32, 3);
		
		List<IndividualEdit> list = dp.getEditList();
		assertEquals(list.size(),1);
		assertEquals(dp.getNumErrors(),1);
		//System.out.println(list);
		assertTrue(dp.alignmentWasSuccesful());
		

		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
		//System.out.println(pa.toString());
	}
	
	
	public void testDoubleLeftAlignmentDeltionInString2() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 = "AAATTT" + common;
		String s2 = "AAATTTTT" + common;
		
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 3);

		List<IndividualEdit> list = dp.getEditList();
		//System.out.println(list);
		assertEquals(list.size(),2);
		assertEquals(dp.getNumErrors(),2);
		assertTrue( dp.alignmentWasSuccesful());
		
		
		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
		//System.out.println(pa.toString());
	}
	
	
	public void testSingleLeftAlignmentDeletionInString2() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 = "ACTGACT" + common;
		String s2 = "ACTGACTG" + common;
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
						32, 3);
		
		List<IndividualEdit> list = dp.getEditList();
		assertEquals(list.size(),1);
		assertEquals(dp.getNumErrors(),1);
		//System.out.println(list);
		assertTrue(dp.alignmentWasSuccesful());
		
		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
		//System.out.println(pa.toString());
	}

	public void testDoubleLeftAlignmentInsertionInString2() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 = "AAATTTTT" + common;
		String s2 = "AAATTT" + common;
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 3);

		List<IndividualEdit> list = dp.getEditList();
		//System.out.println(list);
		assertEquals(list.size(),2);
		assertEquals(dp.getNumErrors(),2);
		assertTrue( dp.alignmentWasSuccesful());
		

		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
		//System.out.println(pa.toString());
	}
	
	
	public void testTrailingNotCountingAsErrors() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 = "AAAAAATTTTTTTG" + common;
		String s2 = "AAAAAAAAAATTTTTTTC" + common;
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 3);

		List<IndividualEdit> list = dp.getEditList();
		//System.out.println(list);
		assertEquals(list.size(),5);
		assertEquals(dp.getNumErrors(),2);
		assertTrue( dp.alignmentWasSuccesful());
		
		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		//System.out.println(pa.toString());
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
	}
	
	public void testTrailingNotCountingAsErrors2() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();

		String s1 = "AAAAAAAAAATTTTTTTC" + common;
		String s2 = "AAAAAATTTTTTTG" + common;
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 3);

		List<IndividualEdit> list = dp.getEditList();
		//System.out.println(list);
		assertEquals(list.size(),5);
		assertEquals(dp.getNumErrors(),2);
		assertTrue( dp.alignmentWasSuccesful());
		
		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		//System.out.println(pa.toString());
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
	}
	
	
	public void testSomeSequence() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 =  "AAACCGGTTAAGGGGTTA" + common;
		String s2 =  "AAGGGTTGGGGCTTG" + common;
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 20);

		List<IndividualEdit> list = dp.getEditList();
		//System.out.println(list);
		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		//System.out.println(pa.toString());
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
	}
	
	public void testSomeSequence2() throws Exception
	{
		StringBuffer buff = new StringBuffer();
		
		for( int x=0; x < 32; x++)
			buff.append("X");
		
		String common = buff.toString();
		String s1 = "GATG" + common ;
		String s2 = "GAGTC" + common;
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 3);

		List<IndividualEdit> list = dp.getEditList();
		//System.out.println(list);
		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		//System.out.println(pa.toString());
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
		
		
		assertEquals(list.size(),2);
		assertEquals(dp.getNumErrors(),2);
		assertTrue( dp.alignmentWasSuccesful());
	}
	
	public void testASequence() throws Exception
	{
		String common = "";
		
		for( int x=0; x < 32; x++)
			common += "X";
		
		String s1 ="GGGAGG" + common;
		String s2 ="ACCGAG" + common;
		
		DP_Expand dp = new DP_Expand(s1, s2, s1.indexOf(common), s2.indexOf(common), 
				32, 200);
		
		assertTrue(dp.alignmentWasSuccesful());
		List<IndividualEdit> list = dp.getEditList();
		//System.out.println(list);
		PairedAlignment pa = ReducedTools.getAlignment(s1, list);
		//System.out.println(pa);
		
		//System.out.println(pa.getFirstSequence().replaceAll("-",""));
		//System.out.println(s1);
		
		assertEquals( pa.getFirstSequence().replaceAll("-",""), s1);
		assertEquals( pa.getSecondSequence().replaceAll("-",""), s2);
	}
	
}
