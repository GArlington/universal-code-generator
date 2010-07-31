/*
 * LoadSBMLFile.java
 *
 * Created on March 4, 2007, 8:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.input.handler;

// import statements -
import java.util.Hashtable;
import java.util.logging.Logger;

import org.sbml.libsbml.*;

import org.varnerlab.server.transport.*;

/**
 *
 * @author jeffreyvarner
 */
public class LoadSBMLFile implements IInputHandler {
    // Class/instance attributes -
    private Hashtable _propTable = null;
    private SBMLReader _sbmlReader = null;
    private SBMLDocument _sbmlDocument = null;
    private Model _model = null;
    private Logger _logger = null;
    
    // ths is imported from the transport package -
    private LoadXMLPropFile _xmlPropTree = null;
    
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
        String strNetworkFileName = _xmlPropTree.getProperty("//NetworkFileName/input_network_filename/text()");
        String strNetworkFileNamePath = _xmlPropTree.getProperty("//NetworkFileName/input_network_path/text()");
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        if (strNetworkFileNamePath.isEmpty())
        {
        	_strSBMLFile = strWorkingDir+"/"+strNetworkFileName;
        }
        else
        {
        	_strSBMLFile = strWorkingDir+"/"+strNetworkFileNamePath+"/"+strNetworkFileName;
        }
        
        // Set the model reference -
        _sbmlDocument = _sbmlReader.readSBML(_strSBMLFile);
        _model = _sbmlDocument.getModel();
        
        
        
        // String strsbml = _sbmlDocument.toSBML();
        // GIOL.write(_propTable.getProperty("TMP_SBML_OUT"),strsbml);
    }

    public void setProperties(Hashtable prop) {
    }

    public void setProperty(String key, String value) {
        _propTable.put(key, value);
    }

    public void setProperty(Object key, Object value) {
        _propTable.put(key, value);
    }

	public void setProperties(LoadXMLPropFile prop) {
		this._xmlPropTree = prop;
	}

	public void setLogger(Logger log) {
		_logger = log;	
	}
            
    
}
