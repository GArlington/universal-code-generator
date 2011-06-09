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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.input.handler.OrderFileReader;
import org.varnerlab.userver.language.handler.CodeGenUtilMethods;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;
import org.varnerlab.userver.language.handler.SUNDIALSModel;

public class WriteSundialsModel implements IOutputHandler {
	// Class/instance attributes -
	private XMLPropTree _xmlPropTree = null;
	private Logger _logger = null;
	
	//Create string buffers
    private StringBuffer bufferModelC = new StringBuffer();
    private StringBuffer bufferSensitivtyC = new StringBuffer();
    private StringBuffer bufferBuilder = new StringBuffer();
    private StringBuffer bufferRunModel = new StringBuffer();
    private StringBuffer bufferPlugin = new StringBuffer();
    private StringBuffer bufferLSODEWrapper = new StringBuffer();
    private StringBuffer bufferDataFile = new StringBuffer();

	
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(XMLPropTree prop) {
		_xmlPropTree = prop;
	}

	public void writeResource(Object object) throws Exception {
		// Method attributes -
		SUNDIALSModel sundialsModel = new SUNDIALSModel();
		double[][] dblSTMatrix = null;
		Vector<Reaction> vecReactions = new Vector<Reaction>();
		Vector<Species> vecSpecies = new Vector<Species>();
        Vector vecSpeciesOrder = new Vector();
		
		// Get the resource type (sbml model) -
        Model model_wrapper = (Model)object;
        
        // Set the _xmlPropTree reference -
        sundialsModel.setPropertyTree(_xmlPropTree);
        
        // Grab some names - mass balances
        ArrayList<String> arrMassBalanceList = _xmlPropTree.processFilenameBlock("MassBalanceFunction");
        ArrayList<String> arrOrderFileList = _xmlPropTree.processFilenameBlock("OrderFile");
      
        String strOrderFileName = arrOrderFileList.get(0);
        String strOrderFileNamePath = arrOrderFileList.get(2);
        String strTmp = "";
        if (!strOrderFileName.equalsIgnoreCase("EMPTY") && !strOrderFileNamePath.equalsIgnoreCase("EMPTY"))
        {
        	// Create order file instance -
        	OrderFileReader orderReader = new OrderFileReader();
        	
        	// Get a path to the order file -
        	strTmp = strOrderFileNamePath+"/"+strOrderFileName;
        	
        	// Log that we are going to load the order file -
			_logger.log(Level.INFO,"Going to load the following order file: "+strTmp);
			
			// read the symbol file name -
			orderReader.readFile(strTmp,vecSpeciesOrder);
			
			// generate the new species *ordered* species list -
			SBMLModelUtilities.reorderSpeciesVector(model_wrapper,vecSpeciesOrder,vecSpecies);	
        }
		else
		{
			// I have no order file, but I need to populate to the vecSpecies
			
			// Transfer the SBML species list into a vector -
			ListOf species_list_tmp = model_wrapper.getListOfSpecies();
	        long NUMBER_OF_SPECIES = model_wrapper.getNumSpecies();
	        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
	        {
	            Species species_tmp = (Species)species_list_tmp.get(scounter);
	            vecSpecies.add(species_tmp);
	        }
		}
        
        // Check to make sure all the reversible rates are 0,inf
        SBMLModelUtilities.convertReversibleRates(model_wrapper,vecReactions);
        
        // Ok, lets build the stoichiometric matrix -
        int NUMBER_OF_SPECIES = (int)vecSpecies.size(); 
        int NUMBER_OF_RATES = (int)vecReactions.size();
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions,vecSpecies);
		
        
		// Generate Model.c      
        sundialsModel.buildMassBalanceBuffer(bufferModelC,model_wrapper);
        
		//sundialsModel.buildMassBalanceEquations(bufferModelC);	// Non-Large Scale Optimized
       	sundialsModel.buildHardCodeMassBalanceEquations(bufferModelC, model_wrapper, vecReactions, vecSpecies);	// Large Scale Optimized

		sundialsModel.buildKineticsBuffer(bufferModelC,model_wrapper,vecReactions,vecSpecies);
		sundialsModel.buildJacobianBuffer(bufferModelC,model_wrapper,vecReactions,vecSpecies);
		SBMLModelUtilities.dumpMassBalancesToDisk(bufferModelC, _xmlPropTree);
		
		
		// Build a data file buffer -
        sundialsModel.buildDataFileBuffer(bufferDataFile, model_wrapper, _xmlPropTree,vecReactions,vecSpecies);
        SBMLModelUtilities.dumpDataFileToDisk(bufferDataFile,_xmlPropTree);
        
		// Generate Build.sh
		sundialsModel.buildBuildFileBuffer(bufferBuilder,_xmlPropTree);
		SBMLModelUtilities.dumpBuildFileToDisk(bufferBuilder,_xmlPropTree);
		SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
		
		// Generate RunModel.sh, and the code reqrd to run the model from Octave
		sundialsModel.buildShellCommand(bufferRunModel,_xmlPropTree);
		sundialsModel.buildOctavePlugin(bufferPlugin,_xmlPropTree);
		sundialsModel.buildLSODECallWrapper(bufferLSODEWrapper, _xmlPropTree);
		
		SBMLModelUtilities.dumpShellCommandToDisk(bufferRunModel, _xmlPropTree);
		SBMLModelUtilities.dumpGeneralBufferToDisk(bufferPlugin, _xmlPropTree, "SundialsPluginFunction");
		SBMLModelUtilities.dumpGeneralBufferToDisk(bufferLSODEWrapper, _xmlPropTree, "LSODECallWrapperFunction");
		
		
		//SBMLModelUtilities.dumpSundialsPluginToDisk(bufferPlugin, _xmlPropTree);
		//SBMLModelUtilities.dumpLSODECallWrapperSundialsToDisk(bufferLSODEWrapper, _xmlPropTree);
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
}
