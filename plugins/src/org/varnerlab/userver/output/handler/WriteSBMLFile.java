/*
 * WriteSBMLFile.java
 *
 * Created on May 9, 2007, 8:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.varnerlab.userver.output.handler;

// import statements -
import java.util.Hashtable;
import java.util.logging.Logger;

import org.sbml.libsbml.*;

import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.language.handler.GIOL;

/**
 *
 * @author jeffreyvarner
 */
public class WriteSBMLFile implements IOutputHandler {
    // Class/instance attributes -
    private Hashtable _propTable = null;
    private XMLPropTree _xmlPropTree = null;
    private Logger _logger = null;
    
    /** Creates a new instance of WriteSBMLFile */
    public WriteSBMLFile() {
    }

    public void writeResource(Object object) throws Exception {
        // Get the resource type -
        Model model_wrapper = (Model)object;
        
        // Load the lib -
        System.loadLibrary("sbmlj");
        
        // New document -
        SBMLDocument doc = new SBMLDocument();
        
        // set the model -
        doc.setModel(model_wrapper);
        
        // Get the resource string -
        String strNetworkFileName = _xmlPropTree.getProperty("//OutputOptions/SBMLOutputFile/@filename");
        
        // Get the path key for this file name -
        String strNetworkPathKey = _xmlPropTree.getProperty(".//OutputOptions/SBMLOutputFile[@filename='"+strNetworkFileName+"']/@path_symbol");
        String strNetworkFileNamePath = _xmlPropTree.getPathForKey(strNetworkPathKey);
        
        String strSBMLFile = "";
        if (strNetworkFileNamePath.isEmpty())
        {
        	strSBMLFile = strNetworkFileName;
        }
        else
        {
        	strSBMLFile = strNetworkFileNamePath+"/"+strNetworkFileName;
        }
        
        // Write the SBML file -
        String strSBMLTree = doc.toSBML();
        GIOL.write(strSBMLFile,strSBMLTree);
    }

    public void setHashtable(Hashtable prop) {
        _propTable = prop;
    }

    public void setProperties(Hashtable prop) {
        this._propTable = prop;
    }

	public void setProperties(XMLPropTree prop) {
		this._xmlPropTree = prop;
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
    
}
