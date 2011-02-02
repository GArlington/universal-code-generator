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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.varnerlab.server.localtransportlayer.IInputHandler;
import org.varnerlab.server.localtransportlayer.XMLPropTree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LoadGraphMLFile implements IInputHandler {
	// Class/instance attributes -
	private Logger _logger = null;
	private Model _model_wrapper = null;
	private XMLPropTree _xmlPropTree = null;
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private Document _graphMLDocument = null;

	@Override
	public Object getResource(Object object) throws Exception {
		
		return (_model_wrapper);
		
	}

	@Override
	public void loadResource(Object object) throws Exception {
		// Method attributes -
		String strFilePath = "";
		
		System.loadLibrary("sbmlj");
		
		// Get the path from the propTree -
		Hashtable<String,String> dictionary = _xmlPropTree.buildFilenameBlockDictionary("NetworkFile");
		
		// Get the path -
		strFilePath = dictionary.get(XMLPropTree.FullyQualifiedPath);
		
		System.out.println("What is going on ??? Do we have the key"+dictionary.containsKey(XMLPropTree.FullyQualifiedPath));
		System.out.println("What is the GraphML path in the hastable - "+dictionary.get("FULLY_QUALIFIED_PATH"));
		
		// Ok, so graphML is just an xml file, so load it up ...
		File graphFile = new File(strFilePath);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	_graphMLDocument = dBuilder.parse(graphFile);
    	_graphMLDocument.getDocumentElement().normalize();
    	
    	
    	// Ok, so now we need to convert the graphML tree into an SBML tree -
		// To do that, we need to instantiate the model -
    	_model_wrapper = new Model(3,1);
    	
    	// Populate the compartment list -
    	populateSBMLCompartmentList();
    	
    	// Populate the species list -
    	populateSBMLSpeciesList();
    	
    	// Ok, populate the reaction list -
    	populateSBMLReactionList();
    	
    	// populate the parameter list -
    	populateSBMLParameterList();
	}
	
	
	private void populateSBMLParameterList() throws Exception
	{
		// Ok, so the last thing we need to do is put some parameters in the SBML file -
		// Since I have no idea what type of reaction these are, we just use random values -
		ListOfReactions rxnList = _model_wrapper.getListOfReactions();
		int NUMBER_OF_REACTIONS = (int) rxnList.size();
		for (int index=0;index<NUMBER_OF_REACTIONS;index++)
		{
			// Create a parameter and add to the model -
			Parameter para = _model_wrapper.createParameter();
			para.setValue(Math.random());
			
			String pId = "EDGE_PARAMETER_"+String.valueOf(index);
			String pName = "k_"+String.valueOf(index);
			para.setId(pId);
			para.setName(pName);
			
			// Add to the model -
			_model_wrapper.addParameter(para);
		}
	}
	
	private void populateSBMLReactionList() throws Exception
	{
		// Ok, so we need to find all the edges and hyperedges in the tree -
			
		// First process the edges -
		String strXPathEdges = ".//graph/edge/@id";
		NodeList nodeList = (NodeList)_xpath.evaluate(strXPathEdges,_graphMLDocument,XPathConstants.NODESET);
		int NUMBER_OF_NODES = nodeList.getLength();
		int rxn_counter = 0;
		for (int index=0;index<NUMBER_OF_NODES;index++)
		{
			// Get the id node -
			Node idNode = nodeList.item(index);
			
			// Ok, so I need to lookup the source and target attributes -
			String strXPathSource = ".//graph/edge[@id='"+idNode.getNodeValue()+"']/@source";
			Node sourceNode = (Node) _xpath.evaluate(strXPathSource, _graphMLDocument, XPathConstants.NODE);
			String strXPathTarget = ".//graph/edge[@id='"+idNode.getNodeValue()+"']/@target";
			Node targetNode = (Node) _xpath.evaluate(strXPathTarget, _graphMLDocument, XPathConstants.NODE);
			
			// Populate the reaction object -
			Reaction tmpEdgeReaction = _model_wrapper.createReaction();
			tmpEdgeReaction.setReversible(false);
			
			// Reaction attributes -
			String strReactionID = "RXN_"+String.valueOf(rxn_counter)+"_"+idNode.getNodeValue();
			String strReactionName = sourceNode.getNodeValue()+" = "+targetNode.getNodeValue();
			tmpEdgeReaction.setId(strReactionID);
			tmpEdgeReaction.setName(strReactionName);
			
			// Source -
			SpeciesReference sourceReference = new SpeciesReference(3,1);
			sourceReference.setSpecies(sourceNode.getNodeValue());
			sourceReference.setStoichiometry(1.0);
			tmpEdgeReaction.addReactant(sourceReference);
			
			// Target -
			SpeciesReference targetReference = new SpeciesReference(3,1);
			sourceReference.setSpecies(targetNode.getNodeValue());
			sourceReference.setStoichiometry(1.0);
			tmpEdgeReaction.addProduct(targetReference);
			
			// set the reaction in the model_wrapper -
			_model_wrapper.addReaction(tmpEdgeReaction);
		}
		
		// Ok, so now we need to process the hyper edges -
		// this will be imperfect - do a straignt translation -
		String strXPathHyperEdges = ".//graph/hyperedge/@id";
		NodeList nodeHyperedgeList = (NodeList)_xpath.evaluate(strXPathHyperEdges,_graphMLDocument,XPathConstants.NODESET);
		int NUMBER_OF_HYPEREDGES = nodeHyperedgeList.getLength();
		for (int index=0;index<NUMBER_OF_HYPEREDGES;index++)
		{
			// Get the id node -
			Node idNode = nodeHyperedgeList.item(index);
			
			// Populate the reaction object -
			Reaction tmpEdgeReaction = _model_wrapper.createReaction();
			tmpEdgeReaction.setReversible(false);
			
			
			// Ok, when I get here, I need to get the endpoints -
			String strXPathEndpoints = ".//graph/hyperedge[@id='"+idNode.getNodeValue()+"']/endpoint/@node";
			
			System.out.println("What is the endpoints XPath - "+strXPathEndpoints);
			
			NodeList nodeEndpointsList = (NodeList)_xpath.evaluate(strXPathEndpoints,_graphMLDocument,XPathConstants.NODESET);
			int NUMBER_OF_ENDPOINTS = nodeEndpointsList.getLength();
			for (int endpoint_index=0;endpoint_index<NUMBER_OF_ENDPOINTS;endpoint_index++)
			{
				// Ok, get the endpoint -
				Node endpointNode = nodeEndpointsList.item(endpoint_index);
				
				// Reaction attributes -
				String strReactionID = "RXN_"+String.valueOf(rxn_counter)+"_"+endpointNode.getNodeValue();
				String strReactionName = "HYPEREDGE";
				tmpEdgeReaction.setId(strReactionID);
				tmpEdgeReaction.setName(strReactionName);
				
				
				// is this a in or out?
				String strTypeXPath = ".//graph/hyperedge[@id='"+idNode.getNodeValue()+"']/endpoint[@node='"+endpointNode.getNodeValue()+"']/@type";
				
				System.out.println("What is this the XPath - "+strTypeXPath);
				
				Node typeNode = (Node) _xpath.evaluate(strTypeXPath, _graphMLDocument, XPathConstants.NODE);
				String strTypeString = typeNode.getNodeValue();
				if (strTypeString.equalsIgnoreCase("out"))
				{
					// If we get here, then I have a reactant -
					// Source -
					SpeciesReference sourceReference = new SpeciesReference(3,1);
					sourceReference.setSpecies(endpointNode.getNodeValue());
					sourceReference.setStoichiometry(1.0);
					tmpEdgeReaction.addReactant(sourceReference);
				}
				else if (strTypeString.equalsIgnoreCase("in"))
				{
					// If I get here I have a product -
					// Target -
					SpeciesReference sourceReference = new SpeciesReference(3,1);
					sourceReference.setSpecies(endpointNode.getNodeValue());
					sourceReference.setStoichiometry(1.0);
					tmpEdgeReaction.addProduct(sourceReference);
				}
			}
			
			// Add the reaction to the model -
			_model_wrapper.addReaction(tmpEdgeReaction);
		}
	}
	
	private void populateSBMLSpeciesList() throws Exception
	{
		// Ok, so we need to populate the species list -
		String strXPath = ".//node/@id";
		NodeList nodeList = (NodeList)_xpath.evaluate(strXPath,_graphMLDocument,XPathConstants.NODESET);
		int NUMBER_OF_NODES = nodeList.getLength();
		
		System.out.println("I have "+NUMBER_OF_NODES+" nodes.");
		
		for (int index=0;index<NUMBER_OF_NODES;index++)
		{
			// Ok, when I get here, I have the list of nodes - we need to put these into species -
			Node node = nodeList.item(index);
			
			// Create the species -
			Species tmpSpecies = _model_wrapper.createSpecies();
			tmpSpecies.setName(node.getNodeValue());
			tmpSpecies.setId(node.getNodeValue());
			tmpSpecies.setCompartment("DEFAULT_MODEL_COMPARTMENT");
			tmpSpecies.setInitialAmount(0.0);
			
			// Add the species to the model_wrapper and go around again -
			_model_wrapper.addSpecies(tmpSpecies);
			
			_logger.log(Level.INFO,"Processed "+tmpSpecies.getName()+" in GraphML InputHandler");
		}
	}
	
	private void populateSBMLCompartmentList() throws Exception
	{
		// Ok, so this is easy - the graphML doesn't have any compartments. Just put in a default compartment.
		Compartment tmpCompartment = _model_wrapper.createCompartment();
		tmpCompartment.setName("DEFAULT_MODEL_COMPARTMENT");
		_model_wrapper.addCompartment(tmpCompartment);
	}

	@Override
	public void setLogger(Logger log) {
		
		// grab the logger -
		_logger = log;
		
	}

	@Override
	public void setProperties(Hashtable prop) {
	}

	@Override
	public void setProperties(XMLPropTree prop) {
		
		// Grab the propTree instance -
		_xmlPropTree = prop;		
	}

}
