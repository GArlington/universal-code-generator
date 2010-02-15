package org.varnerlab.universaleditor.database;

public interface IGFactory {

	 public Object buildComponent() throws Exception;
	 public void setProperty(Object key,Object value);
	
}
