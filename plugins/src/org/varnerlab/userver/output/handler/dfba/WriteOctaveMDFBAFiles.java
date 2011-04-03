package org.varnerlab.userver.output.handler.dfba;

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
import java.util.logging.Logger;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.varnerlab.server.localtransportlayer.IOutputHandler;
import org.varnerlab.server.localtransportlayer.XMLPropTree;
import org.varnerlab.userver.language.handler.SBMLModelUtilities;

public class WriteOctaveMDFBAFiles implements IOutputHandler {
	// Class/instance -
	private Logger _logger = null;
	private XMLPropTree _xmlPropTree = null;
	
	@Override
	public void setLogger(Logger log) {
		
		// Grab the logger -
		_logger = log;

	}

	@Override
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProperties(XMLPropTree prop) {
		
		// Grab the xmlprop tree -
		_xmlPropTree = prop;

	}

	@Override
	public void writeResource(Object object) throws Exception {
		// Method attributes -
		Vector<Reaction> vecReactions = new Vector<Reaction>();
        Vector<Species> vecSpecies = new Vector<Species>();
        StringBuffer kinetics_buffer = new StringBuffer();
        StringBuffer datafile_buffer = new StringBuffer();
        StringBuffer extracellular_buffer = new StringBuffer();
        StringBuffer driver_buffer = new StringBuffer();
        StringBuffer matching_buffer = new StringBuffer();
		Hashtable<String,String> pathTableExtracellular = _xmlPropTree.buildFilenameBlockDictionary("ExtracellularMassBalanceFunction");
		
		// Get the large-scaled flag?
        String strLargeScaleFlag = _xmlPropTree.getProperty(".//Model/@large_scale_optimized");
        String strExtracellularTag = _xmlPropTree.getProperty(".//ExtracellularCompartmentName/@name");
        String strExtracellularSymbol = _xmlPropTree.getProperty(".//ExtracellularCompartmentName/@symbol");
        
		// Get the resource type (SBML model tree)
        Model model_wrapper = (Model)object;
        
        // Transfer the SBML species list into a vector -
		ListOf species_list_tmp = model_wrapper.getListOfSpecies();
        long NUMBER_OF_SPECIES = model_wrapper.getNumSpecies();
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
        	// Get the species -
        	Species species_tmp = (Species)species_list_tmp.get(scounter);
        	String strID = species_tmp.getId();
        	
        	// Check the compartment - look for extracellular flag -
        	String strCompartment = species_tmp.getCompartment();
        	
        	if (strCompartment.equalsIgnoreCase(strExtracellularTag) && strID.contains(strExtracellularSymbol))
        	{
        		vecSpecies.add(species_tmp);
        	}
        }
        
        // Build the extracellular kinetics buffer -
        DFBAOctaveMMatlabMModelUtilities.buildExtracellularKinetics(model_wrapper,kinetics_buffer, vecSpecies, _xmlPropTree);
        DFBAOctaveMMatlabMModelUtilities.buildKineticDataFile(model_wrapper, datafile_buffer, vecSpecies, _xmlPropTree);
        DFBAOctaveMMatlabMModelUtilities.buildExtracellularMassBalances(model_wrapper, extracellular_buffer, vecSpecies, _xmlPropTree);
        DFBAOctaveMMatlabMModelUtilities.buildDriverFile(model_wrapper, driver_buffer, vecSpecies, _xmlPropTree);
        DFBAOctaveMMatlabMModelUtilities.buildBoundsMatchingFile(model_wrapper, matching_buffer, vecSpecies, _xmlPropTree);
        
        // Dump buffers to disk -
        SBMLModelUtilities.dumpKineticsToDisk(kinetics_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpDataFileToDisk(datafile_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpExtracellularMassBalancesToDisk(extracellular_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpDriverToDisk(driver_buffer, _xmlPropTree);
        SBMLModelUtilities.dumpGeneralBufferToDisk(matching_buffer, _xmlPropTree, "BoundsArrayMatchingFunction");
	}

}
