package org.varnerlab.userver.output.handler;

import java.util.Hashtable;

import org.sbml.libsbml.Model;
import org.varnerlab.server.transport.IOutputHandler;
import org.varnerlab.server.transport.LoadXMLPropFile;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;
import org.varnerlab.userver.language.handler.ScilabModel;

public class WriteScilabModel implements IOutputHandler {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;

	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(LoadXMLPropFile prop) {
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
        
        double[][] dblSTMatrix = null;
        ScilabModel scilab = new ScilabModel();
        
        // Get the resource type (SBML model tree)
        Model model_wrapper = (Model)object;
              
        // Check for reversible rates -
        SBMLModelUtilities.convertReversibleRates(model_wrapper);
        
        // set props on octave -
        scilab.setModel(model_wrapper);
        scilab.setPropertyTree(_xmlPropTree);

        // Ok, lets build the stoichiometric matrix - get the system dimension 
        int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper);
        
        // Ok, so let's build the different parts of the scilab program -
        scilab.buildMassBalanceBuffer(massbalances_buffer);
        scilab.buildInputsBuffer(inputs_buffer);
        scilab.buildKineticsBuffer(kinetics_buffer);
        scilab.buildDataFileBuffer(data_buffer);
		scilab.buildDriverBuffer(driver_buffer);
		scilab.buildJacobianBuffer(jacabian_buffer);
		scilab.buildPMatrixBuffer(bmatrix_buffer);
		
		// Dump the different components to disk -
		SBMLModelUtilities.dumpDriverToDisk(driver_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpMassBalancesToDisk(massbalances_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpStoichiometricMatrixToDisk(dblSTMatrix,_xmlPropTree,model_wrapper);
        SBMLModelUtilities.dumpDataFileToDisk(data_buffer,_xmlPropTree);
        SBMLModelUtilities.dumpKineticsToDisk(kinetics_buffer, _xmlPropTree);
		SBMLModelUtilities.dumpInputFunctionToDisk(inputs_buffer, _xmlPropTree);
		SBMLModelUtilities.dumpJacobianToDisk(jacabian_buffer, _xmlPropTree);
		SBMLModelUtilities.dumpBMatrixToDisk(bmatrix_buffer, _xmlPropTree);
	}

}
