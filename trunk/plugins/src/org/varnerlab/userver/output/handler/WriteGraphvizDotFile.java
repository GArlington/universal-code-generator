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

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.varnerlab.server.localtransportlayer.IOutputHandler;
import org.varnerlab.server.localtransportlayer.XMLPropTree;
import org.varnerlab.userver.input.handler.OrderFileReader;
import org.varnerlab.userver.language.handler.GraphvizModel;
import org.varnerlab.userver.language.handler.OctaveCModel;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;

public class WriteGraphvizDotFile implements IOutputHandler {
	// Class/instance attributes -
	private XMLPropTree _xmlPropTree = null;
	private int NUMBER_OF_SPECIES = 0; 
    private int NUMBER_OF_RATES = 0;
    private Logger _logger = null;

	public void setProperties(Hashtable prop) {
	// TODO Auto-generated method stub
	
	}

	public void setProperties(XMLPropTree prop) {
		_xmlPropTree = prop;
	}

	public void writeResource(Object object) throws Exception {
		// Method attributes -
        StringBuffer dot_buffer = new StringBuffer();
        double[][] dblSTMatrix = null;
        Vector<Reaction> vecReactions = new Vector<Reaction>();
        GraphvizModel graphiz_model = new GraphvizModel();
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
        
        // set the model_wrapper -
        graphiz_model.setModel(model_wrapper);
        graphiz_model.setProperties(_xmlPropTree);
        
        // Ok, lets build the stoichiometric matrix -
        NUMBER_OF_SPECIES = (int)vecSpecies.size(); 
        NUMBER_OF_RATES = (int)vecReactions.size();
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        SBMLModelUtilities.buildStoichiometricMatrix(dblSTMatrix, model_wrapper,vecReactions,vecSpecies);
        
        // Construct the dot file -
        graphiz_model.buildDotFileHeader(dot_buffer, model_wrapper, vecReactions);
        graphiz_model.buildGraphizNodeList(dot_buffer, model_wrapper, vecReactions,vecSpecies);
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
