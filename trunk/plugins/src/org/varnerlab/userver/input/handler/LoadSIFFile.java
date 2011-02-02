package org.varnerlab.userver.input.handler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.Species;
import org.varnerlab.server.localtransportlayer.IInputHandler;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

public class LoadSIFFile implements IInputHandler {
	// Class attributes -
	private Logger _logger = null;
	private XMLPropTree _xmlPropTree = null;
	private Vector<String> _vecRecords = new Vector<String>();
	private Model _modelWrapper = null; 
	
	
	@Override
	public Object getResource(Object object) throws Exception {
		
		// Load libSBML -
		System.loadLibrary("sbmlj");

		// Create a new modelWrapper -
		_modelWrapper = new Model(3,1);
		
		// Ok, so I need to construct an SBML model from the SIF file -
		
		
		// return the model -
		return _modelWrapper;
	}
	
	

	@Override
	public void loadResource(Object object) throws Exception {
		// Method attributes -
		ArrayList<String> pathArrList = new ArrayList<String>(); 
		String strSIFPath = "";
		StringBuffer xmlBuffer = new StringBuffer();
		
		// Populate the array list with path information -
		pathArrList = _xmlPropTree.processFilenameBlock("NetworkFileName");
		
		// Create path -
		strSIFPath = pathArrList.get(2)+"/"+pathArrList.get(0);
		
		
		// Ok, log that I loaded the SIF file -
		_logger.log(Level.INFO,"Parsered the SIF file at "+strSIFPath);
	}
		

	@Override
	public void setLogger(Logger log) {
		
		// grab the log instance -
		_logger = log;
		
	}

	@Override
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProperties(XMLPropTree prop) {
		
		// grab the prop tree instance
		_xmlPropTree = prop;
		
	}

}
