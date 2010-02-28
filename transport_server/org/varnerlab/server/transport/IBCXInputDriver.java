package org.varnerlab.server.transport;

import java.util.Properties;
import java.util.Hashtable;
import org.sbml.libsbml.*;

public interface IBCXInputDriver extends IInputHandler {

	// load the resource -
    public void loadResource() throws Exception;
    public Object getResource(Object object) throws Exception;
    public void setProperties(Hashtable prop);
    public void setProperties(LoadXMLPropFile prop);
    
    
    // public void setModel(Model _model);
	
}
