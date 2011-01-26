package org.varnerlab.userver.input.handler;

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
