package org.varnerlab.userver.input.handler;

/*
 * Copyright (c) 2011 Varnerlab, 
 * School of Chemical and Biomolecular Engineering, Cornell
 * University, Ithaca NY 14853 USA.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


// import statements
import java.util.Hashtable;
import java.util.*;

public class Record extends Object implements IReactionFile {
	// Class members
	private Hashtable _hashDataTable=new Hashtable();
	
	public void setData(String key,Object value)
	{
		_hashDataTable.put(key,value);
	}
	
	public Object getData(String key) throws Exception
	{
		return(_hashDataTable.get(key));
	}
	
	
        
        
	public String toString()
	{
		
		String rString="";
		
		Enumeration enumDataTable=_hashDataTable.keys();
		while (enumDataTable.hasMoreElements())
		{
			Object key=enumDataTable.nextElement();
			Object val=_hashDataTable.get(key);
			
			rString=rString+key+"="+val+"\n";
		}
		
		return(rString);
	}
	
}
