/*
 * IInputHandler.java
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
public interface IInputHandler {

    // load the resource -
    public void loadResource(Object object) throws Exception;
    public Object getResource(Object object) throws Exception;
    public void setProperties(Hashtable prop);
    public void setProperties(LoadXMLPropFile prop);
}
