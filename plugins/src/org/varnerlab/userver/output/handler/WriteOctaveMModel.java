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

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.input.handler.OrderFileReader;
import org.varnerlab.userver.language.handler.CodeGenUtilMethods;
import org.varnerlab.userver.language.handler.OctaveCModel;
import org.varnerlab.userver.language.handler.OctaveMModel;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;

public class WriteOctaveMModel implements IOutputHandler {
	// Class/instance attributes -
	private XMLPropTree _xmlPropTree = null;
	private Logger _logger = null;
	
	// Get the dimension of the system -
    private int NUMBER_OF_SPECIES = 0; 
    private int NUMBER_OF_RATES = 0;

	
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(XMLPropTree prop) {
		
		// Grab the properties tree -
		_xmlPropTree = prop;
	}

	
	public void writeResource(Object object) throws Exception {
		// Method attributes -
        StringBuffer massbalances_buffer = new StringBuffer();
        StringBuffer driver_buffer = new StringBuffer();
        StringBuffer inputs_buffer = new StringBuffer();
        StringBuffer data_buffer = new StringBuffer();
        StringBuffer adj_driver_buffer = new StringBuffer();
        StringBuffer adj_buffer = new StringBuffer();
        StringBuffer kinetics_buffer = new StringBuffer();
        StringBuffer pmatrix_buffer = new StringBuffer();
        StringBuffer adj_balances = new StringBuffer();
        StringBuffer adj_driver = new StringBuffer();
        StringBuffer jacobian_buffer = new StringBuffer();
        
        double[][] dblSTMatrix = null;
        OctaveMModel octave = new OctaveMModel();
        Vector<Reaction> vecReactions = new Vector<Reaction>();
        Vector<Species> vecSpecies = new Vector<Species>();
        Vector vecSpeciesOrder = new Vector();
        
        // Get the resource type (SBML model tree)
        Model model_wrapper = (Model)object;
        
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
        
        // set props on octave -
        octave.setModel(model_wrapper);
        octave.setPropertyTree(_xmlPropTree);
        
        // Ok, lets build the stoichiometric matrix - get the system dimension 
        NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        NUMBER_OF_RATES = (int)vecReactions.size();
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions,vecSpecies);
             
        // Ok, so let's start building the different parts of the octave m program -
        octave.buildMassBalanceBuffer(massbalances_buffer,_xmlPropTree);
        octave.buildInputsBuffer(inputs_buffer,_xmlPropTree);
        octave.buildKineticsBuffer(kinetics_buffer,model_wrapper,_xmlPropTree);
        
        octave.buildAdjBalFntBuffer(adj_balances, vecReactions, vecSpecies, _xmlPropTree);
        octave.buildJacobianBuffer(jacobian_buffer, vecReactions, vecSpecies, _xmlPropTree);
        octave.buildPMatrixBuffer(pmatrix_buffer, vecReactions, vecSpecies, _xmlPropTree);
        octave.buildSolveAdjBalBuffer(adj_driver, _xmlPropTree);

        // Ok, build adj buffer -
        octave.buildSolveAdjBalBuffer(adj_driver_buffer, _xmlPropTree);
        octave.buildAdjBalFntBuffer(adj_buffer,vecReactions,vecSpecies, _xmlPropTree);
        octave.buildJacobianBuffer(jacobian_buffer,vecReactions,vecSpecies, _xmlPropTree);

        
        // Call out to the octave class and have it build the driver buffer -
		octave.buildDriverBuffer(driver_buffer, _xmlPropTree);
		SBMLModelUtilities.buildDataFileBuffer(data_buffer, model_wrapper, _xmlPropTree);
		
		// Dump out the model code to disk -
        SBMLModelUtilities.dumpDriverToDisk(driver_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpMassBalancesToDisk(massbalances_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
        SBMLModelUtilities.dumpDataFileToDisk(data_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpKineticsToDisk(kinetics_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpInputFunctionToDisk(inputs_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpBMatrixToDisk(pmatrix_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpJacobianToDisk(jacobian_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpAdjFunctionFileToDisk(adj_balances, _xmlPropTree);
        SBMLModelUtilities.dumpAdjDriverFileToDisk(adj_driver, _xmlPropTree);

        // Dump the sensitivity analysis -
        SBMLModelUtilities.dumpAdjDriverFileToDisk(adj_driver_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpAdjFunctionFileToDisk(adj_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpJacobianToDisk(driver_buffer, _xmlPropTree);
	}

	public void setLogger(Logger log) {
		_logger = log;	
	}	
}
