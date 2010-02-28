package org.varnerlab.server.transport;

//import statements -
import java.util.Properties;
import java.util.Hashtable;

public interface IBCXOutputDriver extends IOutputHandler {

	public void setProperties(Hashtable prop);
    public void setProperties(LoadXMLPropFile prop);

    // dump the resource -
    public void writeResource(Object[] expList) throws Exception;

}
