package org.varnerlab.userver.output.handler;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.varnerlab.server.transport.IOutputHandler;
import org.varnerlab.server.transport.LoadXMLPropFile;
import org.varnerlab.userver.language.handler.CodeGenUtilMethods;
import org.varnerlab.userver.language.handler.OctaveCModel;
import org.varnerlab.userver.language.handler.OctaveMModel;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;

public class WriteOctaveMModel implements IOutputHandler {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;
	private Logger _logger = null;
	
	// Get the dimension of the system -
    private int NUMBER_OF_SPECIES = 0; 
    private int NUMBER_OF_RATES = 0;

	
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(LoadXMLPropFile prop) {
		
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
        OctaveMModel octave = new OctaveMModel();
        Vector<Reaction> vecReactions = new Vector<Reaction>();
        
        // Get the resource type (SBML model tree)
        Model model_wrapper = (Model)object;
        
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
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions);
             
        // Ok, so let's start building the different parts of the octave m program -
        octave.buildMassBalanceBuffer(massbalances_buffer);
        octave.buildInputsBuffer(inputs_buffer);
        octave.buildKineticsBuffer(kinetics_buffer,model_wrapper);
        
        // Call out to the octave class and have it build the driver buffer -
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
