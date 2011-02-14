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
	
	@Override
	public Object getResource(Object object) throws Exception {
		// Method attributes -
	
		
		// Ok, so when I get here I need to be populate the SBML model object from the 
		// the biopax model -
		
		
		// return the model -
		return (_bioPaxModel);
	}

	@Override
	public void loadResource(Object object) throws Exception {
		// Method attributes -
		Hashtable<String,String> pathDictionary = null;
		
		// Ok, so we need to get the path for the biopax file -
		pathDictionary = _xmlPropTree.buildFilenameBlockDictionary("NetworkFile");
		
		// Get the path -
		String strPath = pathDictionary.get("FULLY_QUALIFIED_PATH");
		
		System.out.println("Tyring to load - "+strPath);
		
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
