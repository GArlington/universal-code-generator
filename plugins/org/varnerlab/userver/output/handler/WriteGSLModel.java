package org.varnerlab.userver.output.handler;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import org.varnerlab.server.transport.IOutputHandler;
import org.varnerlab.server.transport.LoadXMLPropFile;
import org.varnerlab.userver.input.handler.OrderFileReader;
import org.varnerlab.userver.language.handler.CodeGenUtilMethods;
import org.varnerlab.userver.language.handler.GSLModel;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;
import org.varnerlab.userver.language.handler.SUNDIALSModel;

public class WriteGSLModel implements IOutputHandler {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;
	private Logger _logger = null;
	
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(LoadXMLPropFile prop) {
		_xmlPropTree = prop;
	}

	public void writeResource(Object object) throws Exception {
		// Method attributes -
		StringBuffer massbalances_buffer = new StringBuffer();
        StringBuffer shell_buffer = new StringBuffer();
        StringBuffer compile_buffer = new StringBuffer();
        Vector<Reaction> vecReactions = new Vector<Reaction>();
        Vector<Species> vecSpecies = new Vector<Species>();
        Vector vecSpeciesOrder = new Vector();
        
		GSLModel gslModel = new GSLModel();
		double[][] dblSTMatrix = null;
		
		// Get the resource type (sbml model) -
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
        SBMLModelUtilities.convertReversibleRates(model_wrapper,vecReactions);
        
        // Ok, lets build the stoichiometric matrix -
        int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions,vecSpecies);
		
        // Set some properties -
        gslModel.setModel(model_wrapper);
        gslModel.setPropertyTree(_xmlPropTree);
        
        // Build the different components of the GSL program -
        gslModel.buildMassBalanceBuffer(massbalances_buffer);
        gslModel.buildMassBalanceEquations(massbalances_buffer);
        gslModel.buildKineticsBuffer(massbalances_buffer,vecReactions,vecSpecies);
        gslModel.buildJacobianBuffer(massbalances_buffer,vecReactions,vecSpecies);
        gslModel.buildBuildFileBuffer(compile_buffer);
        gslModel.buildShellCommandBuffer(shell_buffer);
            
        // Dump stuff to disk -
        SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
        SBMLModelUtilities.dumpMassBalancesToDisk(massbalances_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpShellCommandToDisk(shell_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpBuildFileToDisk(compile_buffer, _xmlPropTree);
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
}
