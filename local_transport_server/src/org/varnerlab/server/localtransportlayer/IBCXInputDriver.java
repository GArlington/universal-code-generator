package org.varnerlab.server.localtransportlayer;

import java.util.Properties;
import java.util.Hashtable;


public interface IBCXInputDriver extends IInputHandler {

	// load the resource -
    public void loadResource() throws Exception;
    public Object getResource(Object object) throws Exception;
    public void setProperties(Hashtable prop);
    public void setProperties(XMLPropTree prop);
    
    
    // public void setModel(Model _model);
	
}
