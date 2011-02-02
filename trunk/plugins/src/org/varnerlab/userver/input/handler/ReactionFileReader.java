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


// Import statements
import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.StringTokenizer;


public class ReactionFileReader extends Object implements IReactionFile {
	// Class members

	public ReactionFileReader()
	{
	}


	public void readData(String fileName,Vector vector) throws Exception
	{
		// Print out the filename that I'm going to
		// read

		// Ok, now down to bizness...
		BufferedReader inReader=new BufferedReader(new FileReader(fileName));
		String dataRecord=null;

		while ((dataRecord=inReader.readLine())!=null)
		{
			// When I get here I have a data record, I need to 
			// parse the record
			int whitespace = dataRecord.length();

			// Need to check to make sure I have do not have a comment 
			if (!dataRecord.contains("//") && whitespace !=0)
			{

				// Create a data record wrapper
				Record objRecord=new Record();

				StringTokenizer tokenizer=new StringTokenizer(cleanRecord(dataRecord),",",false);
				int intCounter=1;
				while (tokenizer.hasMoreElements())
				{

					// Get a data from the tokenizer -
					Object dataChunk=tokenizer.nextToken();

					//System.out.println((String)dataChunk);

					if (intCounter==1)
					{
						objRecord.setData(IReactionFile.RXNNAME,dataChunk);	
					}
					else if (intCounter==2)
					{
						String strTmp = ((String)dataChunk).replace("-", "_");
						objRecord.setData(IReactionFile.REACTANTS,strTmp);
					}
					else if (intCounter==3)
					{
						String strTmp = ((String)dataChunk).replace("-", "_");
						objRecord.setData(IReactionFile.PRODUCTS,strTmp);
					}
					else if (intCounter==4)
					{
						objRecord.setData(IReactionFile.REVERSE,dataChunk);
					}
					else if (intCounter==5)
					{
						objRecord.setData(IReactionFile.FORWARD,dataChunk);
					}

					// increment the counter
					intCounter++;
				}

				// Add the record to the vector
				vector.addElement(objRecord);
			}              
		}

	}

	private String cleanRecord(String record)
	{
		String rString=record.replaceAll(",,",",NULL,");
		return(rString);
	}
}
