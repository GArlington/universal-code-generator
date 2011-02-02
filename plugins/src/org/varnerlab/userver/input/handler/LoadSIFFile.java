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
