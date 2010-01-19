/*
 * IOutputHandler.java
 *
 * Created on March 4, 2007, 8:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.server.transport;

// import statements -
import java.util.Properties;
import java.util.Hashtable;


/**
 *
 * @author jeffreyvarner
 */
public interface IOutputHandler {

    // dump the resource -
    public void writeResource(Object object) throws Exception;
    public void setProperties(Hashtable prop);
    public void setProperties(LoadXMLPropFile prop);
}
