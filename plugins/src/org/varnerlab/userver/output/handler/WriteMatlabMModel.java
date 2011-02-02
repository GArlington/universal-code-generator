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

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.input.handler.OrderFileReader;
import org.varnerlab.userver.language.handler.CodeGenUtilMethods;
import org.varnerlab.userver.language.handler.MatlabModel;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;

public class WriteMatlabMModel implements IOutputHandler {
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
        StringBuffer kinetics_buffer = new StringBuffer();
        double[][] dblSTMatrix = null;
        MatlabModel octave = new MatlabModel();
        Vector<Reaction> vecReactions = new Vector<Reaction>();
        Vector<Species> vecSpecies = new Vector<Species>();
        Vector vecSpeciesOrder = new Vector();
        
        // Get the resource type (SBML model tree)
        Model model_wrapper = (Model)object;
        
        // Ok get the order file -
        // Need to check to see if order file is there -
		String strOrderFileName = _xmlPropTree.getProperty("//OrderFileName/orderfile_filename/text()");
		String strOrderFileNamePath = _xmlPropTree.getProperty("//OrderFileName/orderfile_path/text()");
		String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
		
		// Ok, load the order file if we have a pointer
		if (!strOrderFileName.isEmpty())
		{
			String strTmp = "";
			OrderFileReader orderReader = new OrderFileReader();
			if (!strOrderFileNamePath.isEmpty())
			{
				// Create a tmp path string -
				strTmp = strWorkingDir+"/"+strOrderFileNamePath+"/"+strOrderFileName;
			}
			else
			{
				// Create a tmp path string -
				strTmp = strWorkingDir+"/"+strOrderFileName;
			}

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
        CodeGenUtilMethods.convertReversibleRates(model_wrapper);
        
        // set props on octave -
        octave.setModel(model_wrapper);
        octave.setPropertyTree(_xmlPropTree);
        
        // Ok, lets build the stoichiometric matrix - get the system dimension 
        NUMBER_OF_SPECIES = (int)vecSpecies.size(); 
        NUMBER_OF_RATES = (int)vecReactions.size(); 
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions,vecSpecies);
             
        // Ok, so let's start building the different parts of the octave m program -
        octave.buildMassBalanceBuffer(massbalances_buffer);
        octave.buildInputsBuffer(inputs_buffer);
        octave.buildKineticsBuffer(kinetics_buffer,model_wrapper,vecReactions);
        octave.buildDriverBuffer(driver_buffer, _xmlPropTree);
        SBMLModelUtilities.buildDataFileBuffer(data_buffer, model_wrapper, _xmlPropTree);
        
		// Dump out the model code to disk -
        SBMLModelUtilities.dumpDriverToDisk(driver_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpMassBalancesToDisk(massbalances_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
        SBMLModelUtilities.dumpDataFileToDisk(data_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpKineticsToDisk(kinetics_buffer, _xmlPropTree);
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
}
