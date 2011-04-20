package org.varnerlab.userver.output.handler;

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


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.language.handler.MOBCXModel;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WriteMOBCXFiles implements IOutputHandler {
	// Class/instance variables
	private XMLPropTree _xmlPropTree = null;
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	
	public void setLogger(Logger log) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(XMLPropTree prop) {
		this._xmlPropTree = prop;
	}

	// Ok -- this is the method that gets called from the translator. 
	// The object passed in is a DOM tree holding the MOBCX tree from which I will generate the required code.
	public void writeResource(Object object) throws Exception {
		// Method attributes -
		Document bcxTree = (Document)object;
		MOBCXModel model_wrapper = new MOBCXModel();
		ArrayList<StringBuffer> expList = new ArrayList<StringBuffer>();
		StringBuffer expdata_buffer = new StringBuffer();
		StringBuffer moseBuffer = new StringBuffer();
		StringBuffer soseBuffer = new StringBuffer();
		
		
		// Populate the buffers -
		model_wrapper.buildExperimentalDataStructBuffer(expdata_buffer, bcxTree,_xmlPropTree);
		
		// Process the list of experiments and generate the error files -
		String strExpXPath = "//experiment/@id";
		NodeList expNodeList = (NodeList) _xpath.evaluate(strExpXPath, bcxTree, XPathConstants.NODESET);
		int NUMBER_OF_EXPERIMENTS = expNodeList.getLength();
		for (int exp_index=0;exp_index<NUMBER_OF_EXPERIMENTS;exp_index++)
		{
			// Create a string buffer for this simulation and error -
			StringBuffer exp_buffer = new StringBuffer();
			StringBuffer err_buffer = new StringBuffer();
			StringBuffer testSimBuffer = new StringBuffer();
			StringBuffer simGrpBuffer = new StringBuffer();
			StringBuffer scaleBuffer = new StringBuffer();
			
			// Get the id string for this experiment -
			Node expNode = expNodeList.item(exp_index);
			String strExpId = expNode.getNodeValue();
			
			// Ok, build the buffer -
			model_wrapper.buildSimFileBuffer(exp_buffer, bcxTree, _xmlPropTree,strExpId);
			model_wrapper.buildErrorBuffer(err_buffer, bcxTree, _xmlPropTree, strExpId);
			model_wrapper.buildTestSimBuffer(testSimBuffer, bcxTree, _xmlPropTree, strExpId);
			model_wrapper.buildSimGroupFunctionBuffer(simGrpBuffer, bcxTree, _xmlPropTree, strExpId);
			model_wrapper.buildScalingFunctionBuffer(scaleBuffer, bcxTree, _xmlPropTree, strExpId);
			
			// Dump the buffer to disk -
			SBMLModelUtilities.dumpSimulationFunctionToDisk(exp_buffer,_xmlPropTree,strExpId);
			SBMLModelUtilities.dumpErrorFunctionToDisk(err_buffer, _xmlPropTree, strExpId);
			SBMLModelUtilities.dumpTestSimFunctionToDisk(testSimBuffer, _xmlPropTree, strExpId);
			SBMLModelUtilities.dumpSimGrpFunctionToDisk(simGrpBuffer, _xmlPropTree, strExpId);
			SBMLModelUtilities.dumpScaleFunctionToDisk(scaleBuffer, _xmlPropTree, strExpId);
			
		}
		
		// Build and write the MOSE and SOSE files -
		model_wrapper.buildMOSEBuffer(moseBuffer, bcxTree, _xmlPropTree);
		model_wrapper.buildSOSEBuffer(soseBuffer, bcxTree, _xmlPropTree);
		
		// Dump xOSE files to disk --
		SBMLModelUtilities.dumpSEBufferToDisk(moseBuffer, _xmlPropTree, "MOSE.m");
		SBMLModelUtilities.dumpSEBufferToDisk(soseBuffer, _xmlPropTree, "SOSE.m");
		
		// Dump the experimental data struct to disk -
		SBMLModelUtilities.dumpExpDataStructToDisk(expdata_buffer,_xmlPropTree);
	}

}
