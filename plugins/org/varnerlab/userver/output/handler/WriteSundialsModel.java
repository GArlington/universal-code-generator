package org.varnerlab.userver.output.handler;

import java.util.Hashtable;
import java.util.Vector;

import org.sbml.libsbml.*;
import org.varnerlab.server.transport.IOutputHandler;
import org.varnerlab.server.transport.LoadXMLPropFile;
import org.varnerlab.userver.language.handler.CodeGenUtilMethods;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;
import org.varnerlab.userver.language.handler.SUNDIALSModel;

public class WriteSundialsModel implements IOutputHandler {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;
	
	//Create string buffers
    private StringBuffer bufferModelC = new StringBuffer();
    private StringBuffer bufferSensitivtyC = new StringBuffer();
    private StringBuffer bufferBuilder = new StringBuffer();
    private StringBuffer bufferRunModel = new StringBuffer();

	
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
		
		// Get the resource type (sbml model) -
        Model model_wrapper = (Model)object;
        
        // Check to make sure all the reversible rates are 0,inf
        SBMLModelUtilities.convertReversibleRates(model_wrapper,vecReactions);
        
        // Ok, lets build the stoichiometric matrix -
        int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)vecReactions.size();
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions);
		
		// Generate Model.c
		sundialsModel.buildMassBalanceBuffer(bufferModelC,model_wrapper);
		sundialsModel.buildMassBalanceEquations(bufferModelC);
		sundialsModel.buildKineticsBuffer(bufferModelC,model_wrapper,vecReactions);
		sundialsModel.buildJacobianBuffer(bufferModelC,model_wrapper,vecReactions);
		SBMLModelUtilities.dumpMassBalancesToDisk(bufferModelC, _xmlPropTree);
		
		// Generate Build.sh
		sundialsModel.buildBuildFileBuffer(bufferBuilder,_xmlPropTree);
		SBMLModelUtilities.dumpBuildFileToDisk(bufferBuilder,_xmlPropTree);
		SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper,vecReactions);
		
		// Generate RunModel.sh
		sundialsModel.buildShellCommand(bufferRunModel,_xmlPropTree);
		SBMLModelUtilities.dumpShellCommandToDisk(bufferRunModel, _xmlPropTree);
	}

}
