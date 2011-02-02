/*
 * Copyright (c) 2011 Varnerlab, 
 * School of Chemical and Biomolecular Engineering, Cornell
 * University, Ithaca NY 14853 USA.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * WriteSBMLFile.java
 *
 * Created on May 9, 2007, 8:54 PM
 *
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
