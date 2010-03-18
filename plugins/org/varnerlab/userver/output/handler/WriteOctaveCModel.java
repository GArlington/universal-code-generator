package org.varnerlab.userver.output.handler;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import org.varnerlab.server.transport.IOutputHandler;
import org.varnerlab.server.transport.LoadXMLPropFile;
import org.varnerlab.userver.language.handler.*;

public class WriteOctaveCModel implements IOutputHandler {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;
    private int NUMBER_OF_SPECIES = 0; 
    private int NUMBER_OF_RATES = 0;
    private String strMassBalance = null;
    private String mbfunctionName = null;
    private int last_slash = 0;
    private int last_dot = 0;
    private Logger _logger = null;
	
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(LoadXMLPropFile prop) {
		this._xmlPropTree = prop;
	}
	

	// Calls OctaveCModel -
	public void writeResource(Object object) throws Exception {
		// Method attributes -
        StringBuffer massbalances_buffer = new StringBuffer();
        StringBuffer driver_buffer = new StringBuffer();
        StringBuffer data_buffer = new StringBuffer();
        StringBuffer adj_driver_buffer = new StringBuffer();
        StringBuffer adj_buffer = new StringBuffer();
        StringBuffer debug_buffer = new StringBuffer();
        Vector<Reaction> vecReactions = new Vector<Reaction>();
        
        double[][] dblSTMatrix = null;
        OctaveCModel octave = new OctaveCModel();
        
        System.out.println("Im here...writeResources");
         
        // Grab some names - mass balances
        String strMassBalanceFileName = _xmlPropTree.getProperty("//MassBalanceFunction/massbalance_filename/text()");
        String strMassBalancePath = _xmlPropTree.getProperty("//MassBalanceFunction/massbalance_path/text()");
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        // Get the massbalance filename -
        last_dot = strMassBalanceFileName.lastIndexOf(".");
    	mbfunctionName = strMassBalanceFileName.substring(0,last_dot);
        
        // Get the resource type (sbml model) -
        Model model_wrapper = (Model)object;
        
        // Check to make sure all the reversible rates are 0,inf
        SBMLModelUtilities.convertReversibleRates(model_wrapper,vecReactions);
        
        // Set the reference to the model wrapper -
        octave.setModel(model_wrapper);
        
        // Ok, lets build the stoichiometric matrix -
        NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        NUMBER_OF_RATES = (int)vecReactions.size();
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions);
            
        // Ok, so lets start spanking my monkey ...
        octave.buildMassBalanceBuffer(massbalances_buffer,_xmlPropTree);
        octave.buildMassBalanceEquations(massbalances_buffer);
        octave.buildKineticsBuffer(massbalances_buffer,model_wrapper,vecReactions);
        octave.buildDriverBuffer(driver_buffer,_xmlPropTree);
        
        SBMLModelUtilities.buildDebugReactionListBuffer(debug_buffer, model_wrapper, vecReactions);
        SBMLModelUtilities.dumpDebugFileToDisk(debug_buffer, _xmlPropTree);
        
        // Ok, build adj buffer -
        octave.buildSolveAdjBalBuffer(adj_driver_buffer, _xmlPropTree);
        octave.buildAdjBalFntBuffer(adj_buffer, _xmlPropTree);
        octave.buildKineticsBuffer(adj_buffer,model_wrapper,vecReactions);
        octave.buildDSDTBuffer(adj_buffer);
        octave.buildMassBalanceEquations(adj_buffer);
        octave.buildJacobianBuffer(adj_buffer,vecReactions);
        octave.buildPMatrixBuffer(adj_buffer,vecReactions);
        octave.buildInputsBuffer(adj_buffer);
        
        // Build the data file -
        SBMLModelUtilities.buildDataFileBuffer(data_buffer, model_wrapper, _xmlPropTree,vecReactions);
        
        // Dump to regular model to disk -
        SBMLModelUtilities.dumpDriverToDisk(driver_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpMassBalancesToDisk(massbalances_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
        SBMLModelUtilities.dumpDataFileToDisk(data_buffer,_xmlPropTree);
        
        // Dump the sensitivity analysis -
        SBMLModelUtilities.dumpAdjDriverFileToDisk(adj_driver_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpAdjFunctionFileToDisk(adj_buffer, _xmlPropTree);
	}

	public void setLogger(Logger log) {
		_logger = log;
	}    
}
