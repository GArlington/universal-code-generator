package org.varnerlab.server.localtransportlayer;

//import statements -
import java.util.Properties;
import java.util.Hashtable;

public interface IBCXOutputDriver extends IOutputHandler {

	public void setProperties(Hashtable prop);
    public void setProperties(XMLPropTree prop);

    // dump the resource -
    public void writeResource(Object[] expList) throws Exception;

}
