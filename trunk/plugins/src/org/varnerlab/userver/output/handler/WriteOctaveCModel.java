package org.varnerlab.userver.output.handler;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.input.handler.OrderFileReader;
import org.varnerlab.userver.language.handler.*;

public class WriteOctaveCModel implements IOutputHandler {
	// Class/instance attributes -
	private XMLPropTree _xmlPropTree = null;
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

	public void setProperties(XMLPropTree prop) {
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
        Vector<Species> vecSpecies = new Vector<Species>();
        Vector vecSpeciesOrder = new Vector();
        
        double[][] dblSTMatrix = null;
        OctaveCModel octave = new OctaveCModel();
        
        //System.out.println("Im here...writeResources");
         
        // Get the resource type (sbml model) -
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
        
        String strMassBalanceFileName = arrMassBalanceList.get(0);
        String strMassBalancePath = arrMassBalanceList.get(2);
        if (!strMassBalanceFileName.equalsIgnoreCase("EMPTY") && !strMassBalancePath.equalsIgnoreCase("EMPTY"))
        {
        	// Get the function name for the mass balance function -
        	mbfunctionName = arrMassBalanceList.get(1);
        
	        // Check to make sure all the reversible rates are 0,inf
	        SBMLModelUtilities.convertReversibleRates(model_wrapper,vecReactions);
	        
	        // Set the reference to the model wrapper -
	        octave.setModel(model_wrapper);
	        
	        // Ok, lets build the stoichiometric matrix -
	        NUMBER_OF_SPECIES = (int)vecSpecies.size();
	        NUMBER_OF_RATES = (int)vecReactions.size();
	        
	        // Initialize the stoichiometric matrix -
	        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
	        
	        // Build the matrix -
	        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions,vecSpecies);
	            
	        // Ok, so lets start spanking my monkey ...
	        octave.buildMassBalanceBuffer(massbalances_buffer,_xmlPropTree);
	        octave.buildMassBalanceEquations(massbalances_buffer);
	        octave.buildKineticsBuffer(massbalances_buffer,model_wrapper,vecReactions,vecSpecies);
	        octave.buildDriverBuffer(driver_buffer,_xmlPropTree);
	        
	        SBMLModelUtilities.buildDebugReactionListBuffer(debug_buffer, model_wrapper, vecReactions);
	        SBMLModelUtilities.dumpDebugFileToDisk(debug_buffer, _xmlPropTree);
	        
	        // Ok, build adj buffer -
	        octave.buildSolveAdjBalBuffer(adj_driver_buffer, _xmlPropTree);
	        octave.buildAdjBalFntBuffer(adj_buffer, _xmlPropTree);
	        octave.buildKineticsBuffer(adj_buffer,model_wrapper,vecReactions,vecSpecies);
	        octave.buildDSDTBuffer(adj_buffer);
	        octave.buildMassBalanceEquations(adj_buffer);
	        octave.buildJacobianBuffer(adj_buffer,vecReactions,vecSpecies);
	        octave.buildPMatrixBuffer(adj_buffer,vecReactions,vecSpecies);
	        octave.buildInputsBuffer(adj_buffer);
	        
	        // Build the data file -
	        SBMLModelUtilities.buildDataFileBuffer(data_buffer, model_wrapper, _xmlPropTree,vecReactions,vecSpecies);
	        
	        // Dump to regular model to disk -
	        SBMLModelUtilities.dumpDriverToDisk(driver_buffer,_xmlPropTree);
	        SBMLModelUtilities.dumpMassBalancesToDisk(massbalances_buffer,_xmlPropTree);
	        SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
	        SBMLModelUtilities.dumpDataFileToDisk(data_buffer,_xmlPropTree);
	        
	        // Dump the sensitivity analysis -
	        SBMLModelUtilities.dumpAdjDriverFileToDisk(adj_driver_buffer,_xmlPropTree);
	        SBMLModelUtilities.dumpAdjFunctionFileToDisk(adj_buffer, _xmlPropTree);
        }
        else
        {
        	throw new Exception("ERROR: We have an error in WriteOctaveCModel. There is an issue with the massbalance settings.");
        }
	}
	
	
	
		

	public void setLogger(Logger log) {
		_logger = log;
	}    
}
