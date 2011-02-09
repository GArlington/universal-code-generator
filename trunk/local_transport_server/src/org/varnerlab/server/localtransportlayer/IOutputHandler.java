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
 * Interface to be implemented for any Universal OutputHandler class.
 *
 * Universal can be extended to generate many output types.  This extensibility
 * is in the form of plugins that encode for a specific OutputHandler class.
 * An instance of the OutputHandler is created by Universal to write a file
 * containing a specific representation or code for the current model in memory.
 * The model in memory is of a common type so that it can be translated into any
 * other Universal Output.  The final output file written to disk can be anything from
 * model simulation code to a new graphical input format.  Proper implementation of
 * the IOutputHandler methods will ensure compatibility
 * of new OutputHandler classes with Universal and all InputHandler classes.
 *
 * @author jeffreyvarner
 */
public interface IOutputHandler {

    // dump the resource -
    /**
     * Generates and writes OutputHandler specific code for a given model.
     * The given model is passed in as an SBML model object.
     *
     * When creating a custom plugin, write writeResource to use the passed
     * SBML model object to create custom
     * code for model visualization, simulation, ect.
     * Be sure to include methods to dump output to disk in the path specified
     * by the XMLPropTree.
     * @param object    an SBML model object that represents the current model
     * @throws Exception
     */
    public void writeResource(Object object) throws Exception;
     /**
     * Used by Universal to pass required properties to the OutputHandler.
     * When constructing a model there are many properties
     * that are required; these properties may change from one instance to another.
     * For example, the output file path is obviously required but is also variable.
     * Universal is written to store and communicate these properties in an
     * XMLPropTree object.  The XMLPropTree object is then made accessible
     * to the OutputHandler via the setProperties Method.
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
    public void setProperties(Hashtable prop);
    public void setProperties(XMLPropTree prop);
    /**
     * Creates a new instance of a Logger object in the OutputHandler from the
     * existing Logger object, log.  Used to communicate errors and other
     * information between the OutputHandler and Universal and ultimately between
     * Universal and the user.
     *
     * When creating a custom plugin, write setLogger to set the OutputHandler's
     * Logger object equal to the one passed.  It is encouraged to log useful information
     * on status, errors and procedures to the Logger object so that it can be
     * communicated back to the user via the Universal GUI.  Logging can and should
     * be done throughout all of the OutputHandler methods.
     *
     * @param log   Instance of Logger for a named subsystem created by Universal.
     *
     */
    public void setLogger(Logger log);
}

