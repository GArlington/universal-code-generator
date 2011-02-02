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
 * LoadSBMLFile.java
 *
 * Created on March 4, 2007, 8:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.input.handler;

// import statements -
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.sbml.libsbml.*;

import org.varnerlab.server.localtransportlayer.*;

public class LoadSBMLFile implements IInputHandler {
    // Class/instance attributes -
    private Hashtable _propTable = null;
    private SBMLReader _sbmlReader = null;
    private SBMLDocument _sbmlDocument = null;
    private Model _model = null;
    private Logger _logger = null;
    
    // ths is imported from the transport package -
    private XMLPropTree _xmlPropTree = null;
    
    /** Creates a new instance of LoadSBMLFile */
    public LoadSBMLFile() {
    }
    
    // Get the Hashtable object -
    public void setHashtable(Hashtable prop)
    {
        _propTable = prop;
    }
    
    public Object getResource(Object object) throws Exception
    {
        if (_model!=null)
        {
            return(_model);
        }
        else
        {
           throw new Exception("ERROR: Model not loaded"); 
        }
    }
    
    public void loadResource(Object object) throws Exception
    {
        // Method attributes -
        String _strSBMLFile = "";
        
        // Load the sbml lib -
        System.loadLibrary("sbmlj");
        
        // Create an instance of the SBML reader -
        _sbmlReader = new SBMLReader();
        
        // Get the resource string -
        //String strNetworkFileName = _xmlPropTree.getProperty("//NetworkFileName/input_network_filename/text()");
        //String strNetworkFileNamePath = _xmlPropTree.getProperty("//NetworkFileName/input_network_path/text()");
        //String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        // New objectice-c impl -
        ArrayList<String> tmpArray = _xmlPropTree.processFilenameBlock("NetworkFile");
        String strNetworkFileName = tmpArray.get(0);
        String strNetworkFileNamePath = tmpArray.get(2);
        
        if (!strNetworkFileName.equalsIgnoreCase("EMPTY") && !strNetworkFileNamePath.equalsIgnoreCase("EMPTY"))
        {
        	// Formulate the path string -
        	_strSBMLFile = strNetworkFileNamePath+"/"+strNetworkFileName;
        	
        	System.out.println("Reading ... "+_strSBMLFile);
            
            // Set the model reference -
            _sbmlDocument = _sbmlReader.readSBML(_strSBMLFile);
            _model = _sbmlDocument.getModel();
            
            // String strsbml = _sbmlDocument.toSBML();
            // GIOL.write(_propTable.getProperty("TMP_SBML_OUT"),strsbml);
        }
        else
        {
        	throw new Exception("ERROR: Problem loading the SMBL file. Check your filename settings.");
        } 
    }

    public void setProperties(Hashtable prop) {
    }

    public void setProperty(String key, String value) {
        _propTable.put(key, value);
    }

    public void setProperty(Object key, Object value) {
        _propTable.put(key, value);
    }

	public void setProperties(XMLPropTree prop) {
		this._xmlPropTree = prop;
	}

	public void setLogger(Logger log) {
		_logger = log;	
	}
            
    
}
