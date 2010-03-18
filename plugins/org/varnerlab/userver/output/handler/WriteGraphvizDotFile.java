package org.varnerlab.userver.output.handler;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.varnerlab.server.transport.IOutputHandler;
import org.varnerlab.server.transport.LoadXMLPropFile;
import org.varnerlab.userver.language.handler.GraphvizModel;
import org.varnerlab.userver.language.handler.OctaveCModel;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;

public class WriteGraphvizDotFile implements IOutputHandler {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;
	private int NUMBER_OF_SPECIES = 0; 
    private int NUMBER_OF_RATES = 0;
    private Logger _logger = null;

	public void setProperties(Hashtable prop) {
	// TODO Auto-generated method stub
	
	}

	public void setProperties(LoadXMLPropFile prop) {
		_xmlPropTree = prop;
	}

	public void writeResource(Object object) throws Exception {
		// Method attributes -
        StringBuffer dot_buffer = new StringBuffer();
        double[][] dblSTMatrix = null;
        Vector<Reaction> vecReactions = new Vector<Reaction>();
        GraphvizModel graphiz_model = new GraphvizModel();
	
        // Get the resource type (sbml model) -
        Model model_wrapper = (Model)object;
        
        // Check to make sure all the reversible rates are 0,inf
        SBMLModelUtilities.convertReversibleRates(model_wrapper,vecReactions);
        
        // set the model_wrapper -
        graphiz_model.setModel(model_wrapper);
        graphiz_model.setProperties(_xmlPropTree);
        
        // Ok, lets build the stoichiometric matrix -
        NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        NUMBER_OF_RATES = (int)vecReactions.size();
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions);
        
        // Construct the dot file -
        graphiz_model.buildDotFileHeader(dot_buffer, model_wrapper, vecReactions);
        graphiz_model.buildGraphizNodeList(dot_buffer, model_wrapper, vecReactions);
        graphiz_model.buildGraphvizReactionList(dot_buffer,model_wrapper, vecReactions);
        
        // add the last line of the buffer -
        dot_buffer.append("\n}\n");
        
        // Get the filename that we are going to dump to -
        SBMLModelUtilities.dumpGeneralBufferToDisk(dot_buffer, _xmlPropTree);
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
}
