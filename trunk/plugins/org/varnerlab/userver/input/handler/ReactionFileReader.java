package org.varnerlab.userver.input.handler;

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
