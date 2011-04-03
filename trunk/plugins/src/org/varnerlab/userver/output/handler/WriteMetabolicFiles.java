package org.varnerlab.userver.output.handler;

/*
* Copyright (c) 2011 Varnerlab, 
* School of Chemical and Biomolecular Engineering, 
* Cornell University, Ithaca NY 14853 USA.
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

// Import statements -
import java.io.File;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.language.handler.*;

public class WriteMetabolicFiles implements IOutputHandler {
	// Class/instance attributes -
	private Properties _propTable = null;
    private Logger _logger = null;
    
	public void setProperties(Properties prop) {
		_propTable = prop;
	}

	public void writeResource(Object object) throws Exception {
		// TODO Auto-generated method stub
		Model model_wrapper = (Model)object;
		OctaveMMetabolicModel octave = new OctaveMMetabolicModel();
		// Create a new string buffer and populate it with model details -
        StringBuffer buffer = new StringBuffer();
        StringBuffer stbDataFile = new StringBuffer();
        StringBuffer calcFluxBuffer = new StringBuffer();
        Vector<Wrapper> vecSpecies = new Vector<Wrapper>();
		
        // Load the lib -
        System.loadLibrary("sbmlj");
         
        // Ok, so the first thing we need to do is split all reversible reactions -
		SBMLMetabolicModelUtilities.convertReversibleRates(model_wrapper);
        
		// Get some stuff we need (we are assuming the rates have been converted to 0,inf)
		ListOfReactions list_reactions = model_wrapper.getListOfReactions();
		ListOfSpecies list_species = model_wrapper.getListOfSpecies();
		
		// Ok, get the dimension of the network -
		int NUMBER_OF_REACTIONS = (int)list_reactions.size();
		int NUMBER_OF_SPECIES = (int)list_species.size();
        
		// Initialize and populate the matrix -
		double[][] dblArr = new double[(int)NUMBER_OF_SPECIES][(int)NUMBER_OF_REACTIONS];
		SBMLMetabolicModelUtilities.buildStoichiometricMatrix(dblArr, model_wrapper);
        
        // Create the data file, bounds file and calculate flux driver -
        octave.populateDataFileBuffer(model_wrapper,stbDataFile,_propTable,vecSpecies);
        octave.buildBoundsArray(model_wrapper,buffer);
        octave.buildDriverBuffer(calcFluxBuffer,_propTable);
        
    
		// Dump stuff to disk -
		SBMLModelUtilities.dumpSpeciesToDisk(_propTable, model_wrapper);
		SBMLMetabolicModelUtilities.dumpBoundsFileToDisk(_propTable,buffer);
		SBMLMetabolicModelUtilities.dumpStoichiometricMatrixToDisk(_propTable, NUMBER_OF_SPECIES, NUMBER_OF_REACTIONS, dblArr);
		SBMLMetabolicModelUtilities.dumpDriverFileToDisk(calcFluxBuffer, _propTable);
		SBMLMetabolicModelUtilities.dumpDataFileToDisk(stbDataFile, _propTable);
	}
	
	@Override
	public void setLogger(Logger log) {
		
		_logger = log;
		
	}

	@Override
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProperties(XMLPropTree propTree) {
		
		// Ok, so we need to put a bunch of stuff in the propTable -
		//String strNetwork = _propTable.getProperty("OUTPUT_STM_FILENAME");
		//String strBounds = _propTable.getProperty("OUTPUT_BOUNDS_FILENAME");
		//String strProject = _propTable.getProperty("PATH_SRC_DIRECTORY");
		//String strNetworkDir = _propTable.getProperty("PATH_NETWORK_DIRECTORY");
		//String strDFFileName = _propTable.getProperty("OUTPUT_DATAFILE_FILENAME");
		
		// Get properties out of the XMLPropTree -
		String strNetwork = propTree.getProperty(".//StoichiometricMatrix/@filename");
		String strBounds = propTree.getProperty(".//BoundsFile/@filename");
		String strDFFileName = propTree.getProperty(".//DataFile/@filename");
		String strSpeciesFileName = propTree.getProperty(".//DebugOutputFile/@filename");
		String strTagName = propTree.getProperty(".//ExtracellularTagName/@name");
		String strDriverName = propTree.getProperty(".//DriverFile/@filename");
		String strExtracellularSymbol = propTree.getProperty(".//ExtracellularTagName/@symbol");
		
		String strProject = propTree.getProperty(".//path[@symbol=\"UNIVERSAL_SOURCE_OUTPUT_PATH\"]/@path_location");
		String strNetworkDir = propTree.getProperty(".//path[@symbol=\"UNIVERSAL_NETWORK_OUTPUT_PATH\"]/@path_location");
		
		// Put properties in the _propTable -
		_propTable = new Properties();
		_propTable.put("OUTPUT_STM_FILENAME", strNetwork);
		_propTable.put("OUTPUT_BOUNDS_FILENAME", strBounds);
		_propTable.put("PATH_SRC_DIRECTORY", strProject);
		_propTable.put("PATH_NETWORK_DIRECTORY", strNetworkDir);
		_propTable.put("OUTPUT_DATAFILE_FILENAME", strDFFileName);
		_propTable.put("OUTPUT_SPECIES_FILENAME", strSpeciesFileName);
		_propTable.put("EXTRACELLULAR_TAGNAME", strTagName);
		_propTable.put("DRIVER_FILENAME", strDriverName);
		_propTable.put("EXTRACELLULAR_SYMBOL", strExtracellularSymbol);
	}
	
	

}
