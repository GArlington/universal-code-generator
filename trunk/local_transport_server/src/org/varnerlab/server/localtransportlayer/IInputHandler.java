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
 * Interface to be implemented for any Universal InputHandler class.
 *
 * Universal can be extended to interpret many input types.  This extensibility
 * is in the form of plugins that encode for a specific InputHandler class.
 * An instance of the InputHandler is created by Universal to read a file
 * and to generate a standardized model in memory.  The model in memory is of a
 * common type so that it can be translated into any other Universal Output.
 * Proper implementation of the IInputHandler methods will ensure compatibility
 * of new InputHandler classes with Universal and all OutputHandler classes.
 * 
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
     * For more see the javadoc accosiated with XMLPropTree class.
     *
     *
     * @param prop  an XMLPropTree object containing various properties
     */
    public void setProperties(XMLPropTree prop);

    public void setProperties(Hashtable prop);

    /**
     * Creates a new instance of a Logger object in the InputHandler from the
     * existing Logger object, log.  Used to communicate errors and other
     * information between the InputHandler and Universal and ultimately between
     * Universal and the user.
     *
     * When creating a custom plugin, write setLogger to set the InputHandler's
     * Logger object equal to the one passed.  It is encouraged to log useful information
     * on status, errors and procedures to the Logger object so that it can be
     * communicated back to the user via the Universal GUI.  Logging can and should
     * be done throughout all of the InputHandler methods.
     *
     * @param log   Instance of Logger for a named subsystem created by Universal.
     *
     */
    public void setLogger(Logger log);

    /**
     * Reads the user specified input file and records required information
     * into the current instance of InputHandler.
     *
     * When creating a custom plugin, use loadResource to set private fields
     * of the InputHandler.  These fields should define the model represented
     * by the given input file.
     * @param object    Currently not implemented by Universal, expect null object.
     * @throws Exception    Input exceptions related to opening and reading files.
     */
    public void loadResource(Object object) throws Exception;

    /**
     * Generates an SBML model which represents the current instance of the
     * InputHandler.  SBML is used internally by Universal to communicate
     * model information to different OutputHandlers.
     *
     * When creating a custom plugin, getResources should use the private fields
     * populated by the method loadResource to create and return an appropriate
     * SBML model object.
     * @param object    Currently not implemented by Universal, expect null object.
     * @return SBML model object
     * @throws Exception
     */
    public Object getResource(Object object) throws Exception;
}

