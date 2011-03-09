package org.varnerlab.userver.output.handler.dfba;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.Species;
import org.varnerlab.server.localtransportlayer.XMLPropTree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

public class DFBAOctaveMMatlabMModelUtilities {

	
	public static void buildExtracellularKinetics(Model model_wrapper,StringBuffer buffer,Vector<Species> vecSpecies,XMLPropTree xmlPropTree) throws Exception
	{
		// Method attributes -
		Hashtable<String,String> pathTableKineticsFile = xmlPropTree.buildFilenameBlockDictionary("KineticsFunction");
		Document xmlDoc = (Document)xmlPropTree.getResource(null);
		XPathFactory  xpFactory = XPathFactory.newInstance();
		XPath xpath = xpFactory.newXPath();
		
		// Get the name of the kinetics function -
		String strKineticsFunctionName = pathTableKineticsFile.get("FUNCTION_NAME");
		buffer.append("function qV = ");
		buffer.append(strKineticsFunctionName);
		buffer.append("(x,kV)\n");
        buffer.append("\n");
        buffer.append("% ----------------------------------------------------------------------\n");
		buffer.append("% ");
	    buffer.append(strKineticsFunctionName);
	    buffer.append(".m was generated using the UNIVERSAL code generator system.\n");
	    buffer.append("% Username: ");
	    buffer.append(xmlPropTree.getProperty(".//Model/@username"));
	    buffer.append("\n");
	    buffer.append("% Type: ");
	    buffer.append(xmlPropTree.getProperty(".//Model/@type"));
	    buffer.append("\n");
	    buffer.append("% Version: ");
	    buffer.append(xmlPropTree.getProperty(".//Model/@version"));
	    buffer.append("\n");
	    buffer.append("% \n");
	    buffer.append("% Arguments: \n");
	    buffer.append("% x  - simulation state vector \n");
	    buffer.append("% kV - extracellular parameter values \n");
	    buffer.append("% ----------------------------------------------------------------------\n");
	    buffer.append("\n");
        
	    // Alias the extracellular species -
		int NUMBER_OF_EXTRACELLULAR_SPECIES = vecSpecies.size();
		buffer.append("% ---------------------------------------- \n");
        buffer.append("% Alias the x's -\n");
        buffer.append("% ---------------------------------------- \n");
        
        // Populate -
        for (int index=0;index<NUMBER_OF_EXTRACELLULAR_SPECIES;index++)
        {
        	// Get the species -
        	Species species = vecSpecies.get(index);
        	
        	// Fill the buffer -
        	buffer.append(species.getId());
        	buffer.append("\t = \t");
        	buffer.append("x(");
        	buffer.append(index+1);
        	buffer.append(",1);\n");
        }
        buffer.append("\n");
        
        buffer.append("% ---------------------------------------- \n");
        buffer.append("% Compute the rates -\n");
        buffer.append("% ---------------------------------------- \n");
        
        // Build the rates -
        formulateReactionKinetics(model_wrapper,buffer,vecSpecies,xmlPropTree);
        
        // return -
        buffer.append("\n");
        buffer.append("return;\n");
	}
	
	private static void formulateReactionKinetics(Model model_wrapper,StringBuffer buffer,Vector<Species> vecSpecies,XMLPropTree xmlPropTree) throws Exception
	{
		// Method attributes -
		Document xmlDoc = (Document)xmlPropTree.getResource(null);
		XPathFactory  xpFactory = XPathFactory.newInstance();
		XPath xpath = xpFactory.newXPath();
		
		// Ok, get the list of reactions that are extracellular *and* that we are modeling kinetically -
        // We are going to do this by do a xpath hit on the *actual* xmltree 
        String strXPath = ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction/@name";
        NodeList nodeList = (NodeList)xpath.evaluate(strXPath,xmlDoc,XPathConstants.NODESET);
        int NUMBER_OF_REACTIONS= nodeList.getLength();
        int global_counter = 0;
		for (int index = 0;index<NUMBER_OF_REACTIONS;index++)
		{
			// Fill in the first part of the buffer -
			buffer.append("qV(");
			buffer.append(index+1);
			buffer.append(",1) \t = \tkV(");
			buffer.append(++global_counter);
			buffer.append(",1)");
			
			// Get the current reaction id -
			Node tmpIDNode = nodeList.item(index);
			
			// Get the id string -
			String strNodeID = tmpIDNode.getNodeValue();
			
			// Ok, so now we need to get a list of species for this rate -
			String strXPathSpeciesRefs = ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction[@name='"+strNodeID+"']/listOfReactants/speciesReference/@species";
			NodeList speciesNodeList = (NodeList)xpath.evaluate(strXPathSpeciesRefs,xmlDoc,XPathConstants.NODESET);
			int NUMBER_OF_SPECIES = speciesNodeList.getLength();
			for (int species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
			{
				// Get the species node -
				Node speciesNode = speciesNodeList.item(species_index);
				
				// Ok, get the species string -
				String strSpeciesSymbol = speciesNode.getNodeValue();
				if (strSpeciesSymbol.isEmpty())
				{
					// Ok, if I get here, then I'm all done. put a new line in
					buffer.append(";\n");
				}
				else
				{
					// Ok, so if i get here, then I have actual species -
					buffer.append("*(");
					buffer.append(strSpeciesSymbol);
					buffer.append("/(");
					buffer.append("kV(");
					buffer.append(++global_counter);
					buffer.append(",1)");
					buffer.append(" + ");
					buffer.append(strSpeciesSymbol);
					buffer.append("))");
				}
			}
			
			// Ok, so I should have a rate here - close it out 
			buffer.append(";\t % ");
			buffer.append(index+1);
			buffer.append(" \t");
			buffer.append(strNodeID);
			buffer.append("\n");
		}
	}
	
	private static void formulateReactionParameterList(Model model_wrapper,StringBuffer buffer,Vector<Species> vecSpecies,XMLPropTree xmlPropTree) throws Exception
	{
		// Method attributes -
		Document xmlDoc = (Document)xmlPropTree.getResource(null);
		XPathFactory  xpFactory = XPathFactory.newInstance();
		XPath xpath = xpFactory.newXPath();
		
		// Ok, get the list of reactions that are extracellular *and* that we are modeling kinetically -
        // We are going to do this by do a xpath hit on the *actual* xmltree 
        String strXPath = ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction/@id";
        NodeList nodeList = (NodeList)xpath.evaluate(strXPath,xmlDoc,XPathConstants.NODESET);
        int NUMBER_OF_REACTIONS= nodeList.getLength();
        int global_counter = 0;
        for (int index = 0;index<NUMBER_OF_REACTIONS;index++)
		{
			// Fill in the first part of the buffer -
			buffer.append("kV(");
			buffer.append(++global_counter);
			buffer.append(",1) \t = \t");
			buffer.append(10*Math.random());
			buffer.append("\t ; % ");
			buffer.append(global_counter);
			buffer.append(" Reaction ");
			buffer.append(index);
			buffer.append(" rate constant \n");
			
			// Get the current reaction id -
			Node tmpIDNode = nodeList.item(index);
			
			// Get the id string -
			String strNodeID = tmpIDNode.getNodeValue();
			
			// Ok, so now we need to get a list of species for this rate -
			String strXPathSpeciesRefs = ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction[@id='"+strNodeID+"']/listOfReactants/speciesReference/@species";
			NodeList speciesNodeList = (NodeList)xpath.evaluate(strXPathSpeciesRefs,xmlDoc,XPathConstants.NODESET);
			int NUMBER_OF_SPECIES = speciesNodeList.getLength();
			for (int species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
			{
				// Get the species node -
				Node speciesNode = speciesNodeList.item(species_index);
				
				// Ok, get the species string -
				String strSpeciesSymbol = speciesNode.getNodeValue();
				if (strSpeciesSymbol.isEmpty())
				{
					// Ok, if I get here, then I'm all done. put a new line in
					buffer.append(";\n");
				}
				else
				{
					// Ok, so if i get here, then I have actual species -
					buffer.append("kV(");
					buffer.append(++global_counter);
					buffer.append(",1) \t = \t");
					buffer.append(Math.random());
					buffer.append("\t ; % ");
					buffer.append(global_counter);
					buffer.append(" Reaction ");
					buffer.append(index);
					buffer.append(" saturation constant \n");
				}
			}
		}
	}
	
	private static void formulateUptakeVector(Model model_wrapper,StringBuffer buffer,Vector<Species> vecSpecies,XMLPropTree xmlPropTree) throws Exception
	{
		// Method attributes --
		Document xmlDoc = (Document)xmlPropTree.getResource(null);
		XPathFactory  xpFactory = XPathFactory.newInstance();
		XPath xpath = xpFactory.newXPath();
		ArrayList<String> globalSpeciesList = new ArrayList<String>();
		
		int NUMBER_EXTERNAL_SPECIES = vecSpecies.size();
		buffer.append("qV = zeros(");
		buffer.append(NUMBER_EXTERNAL_SPECIES);
		buffer.append(",1);\n");
		
		for (int external_species_index=0;external_species_index<NUMBER_EXTERNAL_SPECIES;external_species_index++)
		{
		
			// Get the name of the extracellular species -
			Species external_species = vecSpecies.get(external_species_index);
			String strExternalSpeciesName = external_species.getId();
			
		
			buffer.append("qV(");
			buffer.append(external_species_index+1);
			buffer.append(",1) \t = \t B*(");
		
			// Ok, get the list of reactions that are extracellular *and* that we are modeling kinetically -
			// We are going to do this by do a xpath hit on the *actual* xmltree 
			String strXPath = ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction/@id";
			NodeList nodeList = (NodeList)xpath.evaluate(strXPath,xmlDoc,XPathConstants.NODESET);
			int NUMBER_OF_REACTIONS= nodeList.getLength();
			int global_counter = 0;
			for (int index = 0;index<NUMBER_OF_REACTIONS;index++)
			{
				// Get the current reaction id -
				Node tmpIDNode = nodeList.item(index);
			
				// Get the id string -
				String strNodeID = tmpIDNode.getNodeValue();
			
				// Ok, so now we need to get a list of species for this rate -
				String strXPathSpeciesRefs = ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction[@id='"+strNodeID+"']/listOfReactants/speciesReference/@species";
				NodeList speciesNodeList = (NodeList)xpath.evaluate(strXPathSpeciesRefs,xmlDoc,XPathConstants.NODESET);
				ArrayList<String> speciesListReactants = new ArrayList<String>();
				ArrayList<String> stCoeffListReactants = new ArrayList<String>();
				int NUMBER_OF_SPECIES = speciesNodeList.getLength();
				for (int species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
				{
					// Get the species node -
					Node speciesNode = speciesNodeList.item(species_index);
					String strSpeciesName = speciesNode.getNodeValue();
					
					if (!strSpeciesName.isEmpty())
					{
					
						// Add name to list -
						speciesListReactants.add(strSpeciesName);
						
						if (!globalSpeciesList.contains(strSpeciesName))
						{
							globalSpeciesList.add(strSpeciesName);
						}
					
						// Get the stoichiometric coefficients -
						String strSTCoeff =  ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction[@id='"+strNodeID+"']/listOfReactants/speciesReference[@species='"+strSpeciesName+"']/@stoichiometry";
						Node stCoeffNode = (Node)xpath.evaluate(strSTCoeff,xmlDoc,XPathConstants.NODE);
						String strStrCoeff = stCoeffNode.getNodeValue();
					
						// Add st coeff to list -
						stCoeffListReactants.add(strStrCoeff);
					}
				}
				
				// Ok, so now we need to get a list of species for this rate -
				String strXPathSpeciesRefsProducts = ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction[@id='"+strNodeID+"']/listOfProducts/speciesReference/@species";
				NodeList speciesNodeListProducts = (NodeList)xpath.evaluate(strXPathSpeciesRefsProducts,xmlDoc,XPathConstants.NODESET);
				ArrayList<String> speciesListProducts = new ArrayList<String>();
				ArrayList<String> stCoeffListProducts = new ArrayList<String>();
				int NUMBER_OF_PRODUCTS = speciesNodeListProducts.getLength();
				for (int species_index=0;species_index<NUMBER_OF_PRODUCTS;species_index++)
				{
					// Get the species node -
					Node speciesNode = speciesNodeListProducts.item(species_index);
					String strSpeciesName = speciesNode.getNodeValue();
					
					if (!strSpeciesName.isEmpty())
					{
						// Add name to list -
						speciesListProducts.add(strSpeciesName);
						
						if (!globalSpeciesList.contains(strSpeciesName))
						{
							globalSpeciesList.add(strSpeciesName);
						}
					
						// Get the stoichiometric coefficients -
						String strSTCoeff =  ".//RequiredSBMLInformation/listOfKineticExtracellularReactions/reaction[@id='"+strNodeID+"']/listOfProducts/speciesReference[@species='"+strSpeciesName+"']/@stoichiometry";
						Node stCoeffNode = (Node)xpath.evaluate(strSTCoeff,xmlDoc,XPathConstants.NODE);
						String strStrCoeff = stCoeffNode.getNodeValue();
					
						// Add st coeff to list -
						stCoeffListProducts.add(strStrCoeff);
					}
				}
				
				// Ok, when I get here I have the reactants for this reaction -
				// Is my externalSpecies involved in this reaction?
				if (speciesListReactants.contains(strExternalSpeciesName))
				{
					// Get the index -
					int local_index = speciesListReactants.indexOf(strExternalSpeciesName);
					
					// Get the stcoeff value -
					String strLocalCoeffVal = stCoeffListReactants.get(local_index);
					
					// Ok, so I need to add this reaction to the list -
					buffer.append("-");
					buffer.append(strLocalCoeffVal);
					buffer.append("*");
					buffer.append("rV(");
					buffer.append(index+1);
					buffer.append(",1)");	
				}
				else if (speciesListProducts.contains(strExternalSpeciesName))
				{
					// Get the index -
					int local_index = speciesListProducts.indexOf(strExternalSpeciesName);
					
					// Get the stcoeff value -
					String strLocalCoeffVal = stCoeffListProducts.get(local_index);
					
					// Ok, so I need to add this reaction to the list -
					buffer.append(strLocalCoeffVal);
					buffer.append("*");
					buffer.append("rV(");
					buffer.append(index+1);
					buffer.append(",1)");	
				}
				else
				{
					// Ok, so If I get here, then I have an external variable that is not involved in any reactions -
				}
			}
			
			
			// Ok, when I get here I have gone through all the reactions - I have may have a malfunction??
			if (!globalSpeciesList.contains(strExternalSpeciesName))
			{
				// We have a problem -- we have an external species *not* involved in any reactions?
				buffer.append("0); \t % You need to check this -- ");
				buffer.append(strExternalSpeciesName);
				buffer.append(" is an extracellular species that is NOT involved in any uptake/production reactions?\n");
				
			}
			else
			{
			
				// close out and go around again -
				buffer.append(");\n");
			}
		}
	}
	
	public static void buildExtracellularMassBalances(Model model_wrapper,StringBuffer buffer,Vector<Species> vecSpecies,XMLPropTree xmlPropTree) throws Exception
	{
		// Method attributes -
		Hashtable<String,String> pathTableMassbalanceFile = xmlPropTree.buildFilenameBlockDictionary("ExtracellularMassBalanceFunction");
		Hashtable<String,String> pathTableKineticsFile = xmlPropTree.buildFilenameBlockDictionary("KineticsFunction");
		
		// Get the extracellular massbalance file name -
		String strMassBalanceFunctionName = pathTableMassbalanceFile.get("FUNCTION_NAME");
		String strKineticsFunctionName = pathTableKineticsFile.get("FUNCTION_NAME");
		
		// Populate the buffer -
		int NUMBER_OF_EXTRACELLULAR_SPECIES = vecSpecies.size();
		
		buffer.append("function [DXDT] = ");
		buffer.append(strMassBalanceFunctionName);
		buffer.append("(x,t)\n");
        buffer.append("\n");
        
        buffer.append("% ----------------------------------------------------------------------\n");
        buffer.append("% ");
        buffer.append(strMassBalanceFunctionName);
        buffer.append(".m was generated using the UNIVERSAL code generator system.\n");
        buffer.append("% Username: ");
        buffer.append(xmlPropTree.getProperty(".//Model/@username"));
        buffer.append("\n");
        buffer.append("% Type: ");
        buffer.append(xmlPropTree.getProperty(".//Model/@type"));
        buffer.append("\n");
        buffer.append("% Version: ");
        buffer.append(xmlPropTree.getProperty(".//Model/@version"));
        buffer.append("\n");
        buffer.append("% \n");
        buffer.append("% Arguments: \n");
        buffer.append("% x\t - \t state vector \n");
        buffer.append("% t\t - \t current time \n");
        buffer.append("% DXDT\t - \t right-hand side of the extracellular mass balances \n");
        buffer.append("% ----------------------------------------------------------------------\n");
        buffer.append("\n");
        
        // call the globals -
        buffer.append("% ---------------------------------------- \n");
        buffer.append("% Declare the global variables -\n");
        buffer.append("% ---------------------------------------- \n");
        buffer.append("global DF_NETWORK;\n");
        buffer.append("global DF_KINETICS;\n");
        buffer.append("global LP_SOLUTION_GROWTH_RATE;\n");
        buffer.append("\n");
        
        buffer.append("% ---------------------------------------- \n");
        buffer.append("% Get FIN, FOUT and FEED from DF_KINETIC -\n");
        buffer.append("% ---------------------------------------- \n");
        buffer.append("FARR = DF_KINETICS.FLOW_ARRAY;\n");
        buffer.append("FEED = DF_KINETICS.FEED_ARRAY;\n");
        buffer.append("\n");
        buffer.append("% Interpolate to the current timescale -\n");
        buffer.append("FIN = interp1(FARR(:,1),FARR(:,2),t);\n");
        buffer.append("FOUT = interp1(FARR(:,1),FARR(:,3),t);\n");
        buffer.append("\n");
        
        buffer.append("% ---------------------------------------- \n");
        buffer.append("% Call the kinetics function -\n");
        buffer.append("% ---------------------------------------- \n");
        buffer.append("rV = ");
        buffer.append(strKineticsFunctionName);
        buffer.append("(x,kV);\n");
        buffer.append("\n");
          
        buffer.append("% ---------------------------------------- \n");
        buffer.append("% Alias the x's -\n");
        buffer.append("% ---------------------------------------- \n");
        
        // Populate -
        for (int index=0;index<NUMBER_OF_EXTRACELLULAR_SPECIES;index++)
        {
        	// Get the species -
        	Species species = vecSpecies.get(index);
        	
        	// Fill the buffer -
        	buffer.append(species.getId());
        	buffer.append("\t = \t");
        	buffer.append("x(");
        	buffer.append(index+1);
        	buffer.append(",1);\n");
        }
        
        // Add the volume -
        buffer.append("V\t = \t");
        buffer.append("x(");
    	buffer.append(NUMBER_OF_EXTRACELLULAR_SPECIES+1);
    	buffer.append(",1);\n");
    	
    	// Add the cellmass -
    	buffer.append("B\t = \t");
        buffer.append("x(");
    	buffer.append(NUMBER_OF_EXTRACELLULAR_SPECIES+2);
    	buffer.append(",1);\n");
    	buffer.append("\n");
    	
    	buffer.append("% ---------------------------------------- \n");
        buffer.append("% Formulate qV -\n");
        buffer.append("% ---------------------------------------- \n");
        formulateUptakeVector(model_wrapper,buffer,vecSpecies,xmlPropTree);
		
    	buffer.append("\n");
    	buffer.append("% ---------------------------------------- \n");
        buffer.append("% Extracellular massbalances -\n");
        buffer.append("% ---------------------------------------- \n");
        
        // Populate -
        for (int index=0;index<NUMBER_OF_EXTRACELLULAR_SPECIES;index++)
        {
        	// Get the species -
        	Species species = vecSpecies.get(index);
        	
        	// Write the extracellular mass balance -
        	buffer.append("DXDT(");
        	buffer.append(index+1);
        	buffer.append(",1)\t = \t");
        	buffer.append("(FIN/V)*FEED(");
        	buffer.append(index+1);
        	buffer.append(",1) ");
        	buffer.append(" - (FOUT/V)*");
        	buffer.append(species.getId());
        	buffer.append(" - ");
        	buffer.append(species.getId());
        	buffer.append("*(1/V)*(FIN-FOUT) + qV(");
        	buffer.append(index+1);
        	buffer.append(",1);\n");
        	
        	// Ok, we need to formulate the "bio" part of the problem -
        }
        
        // Put the volume balance -
        buffer.append("DXDT(");
        buffer.append(NUMBER_OF_EXTRACELLULAR_SPECIES+1);
        buffer.append(",1)\t = \tFIN - FOUT;\n");
        
        // Put the cellmass balance -
        buffer.append("DXDT(");
        buffer.append(NUMBER_OF_EXTRACELLULAR_SPECIES+2);
        buffer.append(",1)\t = \t");
        buffer.append(" - (FOUT/V)*B");
    	buffer.append(" - ");
    	buffer.append("B");
    	buffer.append("*(1/V)*(FIN-FOUT)+B*LP_SOLUTION_GROWTH_RATE;\n");
        
        // return -
        buffer.append("\n");
        buffer.append("return;\n");
        
	}
	
	public static void buildKineticDataFile(Model model_wrapper,StringBuffer buffer,Vector<Species> vecSpecies,XMLPropTree xmlPropTree) throws Exception
	{
		// Method attributes -
		Hashtable<String,String> pathTableKineticDataFile = xmlPropTree.buildFilenameBlockDictionary("KineticDataFile");
		Hashtable<String,String> pathTableFlowFile = xmlPropTree.buildFilenameBlockDictionary("VolumetricFlowFile");
		
		// Get the file name information -
		String strKFileName = pathTableKineticDataFile.get("FUNCTION_NAME");
		String strFlowFilePath = pathTableFlowFile.get("FULLY_QUALIFIED_PATH");
		
		// Get the number of extracellular species -
		int NUMBER_OF_EXTRACELLULAR_SPECIES = vecSpecies.size();
		
		// Populate -
		buffer.append("[KDF]=");
		buffer.append(strKFileName);
		buffer.append("(TSIM);\n");
		buffer.append("% ----------------------------------------------------------------------\n");
		buffer.append("% ");
	    buffer.append(strKFileName);
	    buffer.append(".m was generated using the UNIVERSAL code generator system.\n");
	    buffer.append("% Username: ");
	    buffer.append(xmlPropTree.getProperty(".//Model/@username"));
	    buffer.append("\n");
	    buffer.append("% Type: ");
	    buffer.append(xmlPropTree.getProperty(".//Model/@type"));
	    buffer.append("\n");
	    buffer.append("% Version: ");
	    buffer.append(xmlPropTree.getProperty(".//Model/@version"));
	    buffer.append("\n");
	    buffer.append("% \n");
	    buffer.append("% Arguments: \n");
	    buffer.append("% TSIM  - simulation time vector \n");
	    buffer.append("% KDF - data file structure holding extracellular parameters \n");
	    buffer.append("% ----------------------------------------------------------------------\n");
	    buffer.append("\n");
		
		buffer.append("% Load the flow array -\n");
        buffer.append("FARR = load('");
        buffer.append(strFlowFilePath);
        buffer.append("');\n");
        buffer.append("\n");
        buffer.append("% Formulate the parameter array -\n");
        
        // Build the parameter list -
        formulateReactionParameterList(model_wrapper,buffer,vecSpecies,xmlPropTree);
        
        buffer.append("\n");
        buffer.append("% Formulate the initial condition array - \n");
        buffer.append("XI = [\n");
        
        // Loop through the initial conditions -
        for (int index=0;index<NUMBER_OF_EXTRACELLULAR_SPECIES;index++)
        {
        	// Get the species object -
        	Species species = vecSpecies.get(index);
        	
        	// Fill the buffer -
        	buffer.append("\t");
        	buffer.append(species.getInitialAmount());
        	buffer.append("\t ; \t");
        	buffer.append(index+1);
        	buffer.append(" ID:");
        	buffer.append(species.getId());
        	buffer.append(" NAME:");
        	buffer.append(species.getName());
        	buffer.append("\n");
        }
        
        // Add the volume -
        buffer.append("\t");
        buffer.append("1.0");
        buffer.append("\t ; \t");
        buffer.append(NUMBER_OF_EXTRACELLULAR_SPECIES+1);
        buffer.append(" ID:V ");
        buffer.append(" NAME:Volume");
        buffer.append("\n");
        
        // Add cell-mass -
        buffer.append("\t");
        buffer.append("1.0");
        buffer.append("\t ; \t");
        buffer.append(NUMBER_OF_EXTRACELLULAR_SPECIES+2);
        buffer.append(" ID:B ");
        buffer.append(" NAME:Biomass");
        buffer.append("\n");
        
        // Close the array -
        buffer.append("];\n");
        
        buffer.append("\n");
        buffer.append("% Set the FEED levels - \n");
        for (int index=0;index<NUMBER_OF_EXTRACELLULAR_SPECIES;index++)
        {
        	// Get the species object -
        	Species species = vecSpecies.get(index);
        	buffer.append("FEED(");
        	buffer.append(index+1);
        	buffer.append(",1) \t = \t 0.0 \t ; \t % ");
        	buffer.append(index+1);
        	buffer.append(" Feed level for ");
        	buffer.append(" ID:");
        	buffer.append(species.getId());
        	buffer.append(" NAME:");
        	buffer.append(species.getName());
        	buffer.append("\n");
        }
        
        buffer.append("\n");
        buffer.append("% =========== DO NOT EDIT BELOW THIS LINE ==============\n");
        buffer.append("KDF.FLOW_ARRAY = FARR;\n");
        buffer.append("KDF.PARAMETER_ARRAY = kV;\n");
        buffer.append("KDF.INITIAL_CONDITION = XI;\n");
        buffer.append("KDF.FEED_ARRAY = FEED;\n");
        buffer.append("KDF.NUMBER_OF_DYNAMIC_STATES = ");
        buffer.append(NUMBER_OF_EXTRACELLULAR_SPECIES);
        buffer.append("\n");
        buffer.append("% ======================================================\n");
        buffer.append("return;\n");
        
	}
	
}
