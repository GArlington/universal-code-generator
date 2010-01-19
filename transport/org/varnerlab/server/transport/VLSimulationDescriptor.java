package org.varnerlab.server.transport;

// import statements
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;



public class VLSimulationDescriptor extends Object implements IConfigurable {
	// Class/instance attributes
	private Hashtable _table=new Hashtable();
	
	public VLSimulationDescriptor(){
	}
	
	public void setProperty(Object key,Object property){

        System.out.println("Key - "+key+" Object - "+property);


        _table.put(key,property);
	}
	
	public Object getProperty(Object key){
		return(_table.get(key));
	}
	
	public Enumeration getKeys(){
		return(_table.keys());
	}

    public void setProperty(String key, String value) {

        System.out.println("Key - "+key+" Object - "+value);

        _table.put(key,value);
    }
	
}
