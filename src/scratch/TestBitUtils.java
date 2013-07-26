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


package scratch;

public class TestBitUtils
{
	public static void main(String[] args) throws Exception
	{
		long mask = 0x3l << (32-1)*2;
		
		for( int x=31; x>=0; x--)
		{
			System.out.println( Long.toBinaryString(mask));
			mask= mask >>> 2;
		}
			
	}
}
