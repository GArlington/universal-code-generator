package org.varnerlab.userver.output.handler;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import org.varnerlab.server.localtransportlayer.*;

import org.varnerlab.userver.input.handler.OrderFileReader;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;
import org.varnerlab.userver.language.handler.ScilabModel;

public class WriteScilabModel implements IOutputHandler {
	// Class/instance attributes -
	private XMLPropTree _xmlPropTree = null;
	private Logger _logger = null;

	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(XMLPropTree prop) {
		_xmlPropTree = prop;
	}

	public void writeResource(Object object) throws Exception {
		// Method attributes -
        StringBuffer massbalances_buffer = new StringBuffer();
        StringBuffer driver_buffer = new StringBuffer();
        StringBuffer inputs_buffer = new StringBuffer();
        StringBuffer data_buffer = new StringBuffer();
        StringBuffer kinetics_buffer = new StringBuffer();
        StringBuffer jacabian_buffer = new StringBuffer();
        StringBuffer bmatrix_buffer = new StringBuffer();
        Vector<Reaction> vecReactions = new Vector<Reaction>();
        Vector<Species> vecSpecies = new Vector<Species>();
        Vector vecSpeciesOrder = new Vector();
        
        double[][] dblSTMatrix = null;
        ScilabModel scilab = new ScilabModel();
        
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
              
        // Check for reversible rates -
        SBMLModelUtilities.convertReversibleRates(model_wrapper,vecReactions);
        
        // set props on octave -
        scilab.setModel(model_wrapper);
        scilab.setPropertyTree(_xmlPropTree);

        // Ok, lets build the stoichiometric matrix - get the system dimension 
        int NUMBER_OF_SPECIES = (int)vecSpecies.size(); 
        int NUMBER_OF_RATES = (int)vecReactions.size(); 
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions,vecSpecies);
        
        // Ok, so let's build the different parts of the scilab program -
        scilab.buildMassBalanceBuffer(massbalances_buffer);
        scilab.buildInputsBuffer(inputs_buffer);
        scilab.buildKineticsBuffer(kinetics_buffer,vecReactions,vecSpecies);
        scilab.buildDataFileBuffer(data_buffer,_xmlPropTree);
		scilab.buildDriverBuffer(driver_buffer);
		scilab.buildJacobianBuffer(jacabian_buffer,vecReactions,vecSpecies);
		scilab.buildPMatrixBuffer(bmatrix_buffer,vecReactions,vecSpecies);
		
		// Dump the different components to disk -
		SBMLModelUtilities.dumpDriverToDisk(driver_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpMassBalancesToDisk(massbalances_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
        SBMLModelUtilities.dumpDataFileToDisk(data_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpKineticsToDisk(kinetics_buffer, _xmlPropTree);
		SBMLModelUtilities.dumpInputFunctionToDisk(inputs_buffer, _xmlPropTree);
		SBMLModelUtilities.dumpJacobianToDisk(jacabian_buffer, _xmlPropTree);
		SBMLModelUtilities.dumpBMatrixToDisk(bmatrix_buffer, _xmlPropTree);
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
}
