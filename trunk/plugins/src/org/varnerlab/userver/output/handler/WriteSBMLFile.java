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
        
        System.out.println("How many species (in output handler)? "+model_wrapper.getNumSpecies());
        
        // set the model -
        doc.setModel(model_wrapper.getModel());
        
        // Get the resource string -
        String strNetworkFileName = _xmlPropTree.getProperty(".//OutputOptions/SBMLOutputFile/@filename");
        
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
        
        // THIS SHOULD *NOT* BE NEEDED...there must be a bug in libSBML?
        // If we call writeXX directly on the document, then we get an empty model?
        // Create string buffer -
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buffer.append("<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\">\n");
        buffer.append(model_wrapper.toSBML());
        buffer.append("\n");
        buffer.append("</sbml>");
        
        //System.out.println("WTF?? - "+doc.toSBML());
        
        // Write to file 
        GIOL.write(strSBMLFile,buffer.toString());
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
