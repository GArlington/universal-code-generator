/*
 * IInputHandler.java
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
 * Interface to be implemented for all UNIVERSAL InputHandler classes
 * @author jeffreyvarner
 */
public interface IInputHandler {

    /**
     * Used by Universal to pass required properties to the InputHandler.
     * When constructing a model there are many properties
     * that are required; these properties may change from one instance to another.
     * For example, the input file path is obviously required but is also variable.
     * Universal is written to store and communicate these properties in an
     * XMLPropTree object.  The XMLPropTree object is then made accessible
     * to the InputHandler via the setProperties Method.
     *
     * When creating a custom plugin, write the setProperties method
     * to properly make use of the "instance specific" information in the
     * XMLPropTree object.
     *
     *
     * For more see the javadoc associated with XMLPropTree class.
     *
     *
     * @param prop  an XMLPropTree object containing various properties
     */
    public void setProperties(XMLPropTree prop);

    
    public void setProperties(Hashtable prop);

    /**
     * Passes an instance of the UNIVERSAL Logger instance to the InputHandler.
     * The GUI captures all standard out and standard error 
     * @param log   Logger instance created by UNIVERSAL.
     */
    public void setLogger(Logger log);

    /**
     * Reads a user specified input file.
     * @param object    Currently not implemented by Universal, expect null object.
     * @throws Exception    Input exceptions related to opening and reading files.
     */
    public void loadResource(Object object) throws Exception;

    /**
     * Generates an SBML model object from the input data structure. 
     * @param object  Currently not implemented by Universal, expect null object.
     * @return SBML model object (default)
     * @throws Exception
     */
    public Object getResource(Object object) throws Exception;
}

