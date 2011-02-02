package org.varnerlab.userver.input.handler;

import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.*;

import org.sbml.libsbml.Model;

import org.varnerlab.server.localtransportlayer.IInputHandler;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

public class LoadBioPAXFile implements IInputHandler {
	// Class attributes -
	private Logger _logger = null;
	private XMLPropTree _xmlPropTree = null;
	private org.biopax.paxtools.model.Model _bioPaxModel = null;
	private Model _model_wrapper = null;
	
	@Override
	public Object getResource(Object object) throws Exception {
		// Method attributes -
		
		// Load the SBML lib -
		System.loadLibrary("sbmlj");
		
		// Create a new sbml model instance -
		_model_wrapper = new Model(3,1);
		
		// Ok, so when I get here I need to be populate the SBML model object from the 
		// the biopax model -
		
		
		// return the model -
		return (_model_wrapper);
	}

	@Override
	public void loadResource(Object object) throws Exception {
		// Method attributes -
		Hashtable<String,String> pathDictionary = null;
		
		// Ok, so we need to get the path for the biopax file -
		pathDictionary = _xmlPropTree.buildFilenameBlockDictionary("NetworkFileName");
		
		// Get the path -
		String strPath = pathDictionary.get(XMLPropTree.FullyQualifiedPath);
		
		// Load the biopax file and create a biopax model 
		// Note we need to use the full package name to avoid conflict
		// with SBML -
		BioPAXIOHandler biopaxReader = new SimpleReader();
		_bioPaxModel = biopaxReader.convertFromOWL(new FileInputStream(strPath));
	}
	

	@Override
	public void setLogger(Logger log) {
		
		// Grab the logger -
		_logger = log;
		
	}

	@Override
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProperties(XMLPropTree prop) {
		
		// Grab the prop tree -
		_xmlPropTree = prop;
		
	}

}
