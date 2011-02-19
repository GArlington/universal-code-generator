/*
 * IOutputHandler.java
 *
 * Created on March 4, 2007, 8:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.server.localtransportlayer;

// import statements -
import java.util.Properties;
import java.util.Hashtable;
import java.util.logging.Logger;


/**
 * Interface to be implemented for all UNIVERSAL OutputHandler classes
 * @author jeffreyvarner
 */
public interface IOutputHandler {
	public void setProperties(Hashtable prop);
    // dump the resource -
    
	/**
     * Generates and writes OutputHandler specific code for a given model.
     * The given model is passed in as an SBML model object.
     * @param object    an SBML model object that represents the current model
     * @throws Exception
     */
    public void writeResource(Object object) throws Exception;
    
    /**
     * Used by Universal to pass required properties to the OutputHandler.
     * The XMPPropTree is a warpper for the XML tree edited in the GUI.
     * For more see the javadoc associated with XMLPropTree class.
     * @param prop  an XMLPropTree object containing various properties
     */
    public void setProperties(XMLPropTree prop);
    
    /**
     * Passes an instance of the UNIVERSAL Logger instance to the OutputHandler 
      * The GUI captures all standard out and standard error 
     * @param log   Logger instance created by UNIVERSAL
     */
    public void setLogger(Logger log);
}

