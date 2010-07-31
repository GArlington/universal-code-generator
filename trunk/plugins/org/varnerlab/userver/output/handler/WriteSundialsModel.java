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
import org.varnerlab.userver.language.handler.SBMLModelUtilities;
import org.varnerlab.userver.language.handler.SUNDIALSModel;

public class WriteSundialsModel implements IOutputHandler {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;
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

	public void setProperties(LoadXMLPropFile prop) {
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
        int NUMBER_OF_SPECIES = (int)vecSpecies.size(); 
        int NUMBER_OF_RATES = (int)vecReactions.size();
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions,vecSpecies);
		
		// Generate Model.c
		sundialsModel.buildMassBalanceBuffer(bufferModelC,model_wrapper);
		sundialsModel.buildMassBalanceEquations(bufferModelC);
		sundialsModel.buildKineticsBuffer(bufferModelC,model_wrapper,vecReactions,vecSpecies);
		sundialsModel.buildJacobianBuffer(bufferModelC,model_wrapper,vecReactions,vecSpecies);
		SBMLModelUtilities.dumpMassBalancesToDisk(bufferModelC, _xmlPropTree);
		
		// Build a data file buffer -
        SBMLModelUtilities.buildDataFileBuffer(bufferDataFile, model_wrapper, _xmlPropTree,vecReactions,vecSpecies);
        SBMLModelUtilities.dumpDataFileToDisk(bufferDataFile,_xmlPropTree);
        
		// Generate Build.sh
		sundialsModel.buildBuildFileBuffer(bufferBuilder,_xmlPropTree);
		SBMLModelUtilities.dumpBuildFileToDisk(bufferBuilder,_xmlPropTree);
		SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
		
		// Generate RunModel.sh, and the code reqrd to run the model from Octave
		sundialsModel.buildShellCommand(bufferRunModel,_xmlPropTree);
		sundialsModel.buildOctavePlugin(bufferPlugin);
		sundialsModel.buildLSODECallWrapper(bufferLSODEWrapper, _xmlPropTree);
		SBMLModelUtilities.dumpShellCommandToDisk(bufferRunModel, _xmlPropTree);
		SBMLModelUtilities.dumpSunsialsPluginToDisk(bufferPlugin, _xmlPropTree);
		SBMLModelUtilities.dumpLSODECallWrapperSundialsToDisk(bufferLSODEWrapper, _xmlPropTree);
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
}
