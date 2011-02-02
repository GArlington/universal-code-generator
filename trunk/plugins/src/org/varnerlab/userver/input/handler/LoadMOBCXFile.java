package org.varnerlab.userver.input.handler;

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
 */


import java.io.File;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sbml.libsbml.Model;
import org.varnerlab.server.localtransportlayer.*;
import org.w3c.dom.Document;

public class LoadMOBCXFile implements IInputHandler {
	// Class/instance attributes -
	private XMLPropTree _xmlPropTree = null;
	private Model _model = null;
	private Logger _logger = null;
	private Document doc = null;
	private Document _bcxPropTree = null;
	
	public Object getResource(Object object) throws Exception {
		
		if (_bcxPropTree!=null)
		{
			return(_bcxPropTree);
		}
		else
		{
			throw new Exception("ERROR: MOBCXFile tree is null. MOBCX file has not been parsed.");
		}		
	}

	public void loadResource(Object object) throws Exception {
		// Load the bcx file -
		String strPath = "";
		
		// Ok, so we need load the sbml for this experiment list -
		LoadSBMLFile sbmlReader = new LoadSBMLFile();
		sbmlReader.setProperties(_xmlPropTree);
		sbmlReader.loadResource(null);
		_model = (Model)sbmlReader.getResource(null);
		
		// Get the resource string -
        String strFileName = _xmlPropTree.getProperty("//Model/bcx_datafilename/text()");
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
 
        // Formulate the path -
        strPath = strWorkingDir+"/"+strFileName;
        
        System.out.println("Going to load - "+strPath);
       
		// Ok, bitches, let's load the bcx file and then hand the DOM tree to Ryan's loadResources method -
		File configFile = new File(strPath);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	_bcxPropTree = dBuilder.parse(configFile);
    	_bcxPropTree.getDocumentElement().normalize();
	}
	
	
	public void setLogger(Logger log) {
		_logger = log;
	}

	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(XMLPropTree prop) {
		this._xmlPropTree = prop;
	}
}
