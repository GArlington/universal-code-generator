package org.varnerlab.userver.language.handler;

import java.io.File;
import java.util.Properties;
import java.util.Vector;

import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.ListOfSpeciesReferences;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;

public class OctaveMMetabolicModel {

	public void buildDriverBuffer(StringBuffer buffer,Properties propTable) throws Exception
	{
		// Get the name of the function name -
		String fileName = propTable.getProperty("DRIVER_FILENAME");
		
		// Populate the buffer -
		buffer.append("% =========================================================================== %\n");
		buffer.append("% ");
		buffer.append(fileName);
		buffer.append(".m - Solves the LP problem associated with an FBA calculation. \n");
		buffer.append("%\n");
		buffer.append("% Input Args:\n");
		buffer.append("% pDataFile		-	Pointer to the DataFile function (@DataFile)\n");
		buffer.append("% OBJ			-	Index vector of fluxes to max/min\n");
		buffer.append("% MIN_MAX_FLAG	-	Flag = 1 -> minimize, Flag = -1 -> maximize\n");
		buffer.append("% DFIN			-	DataStruct in memory ([] if you want to load from disk)\n");
		buffer.append("%\n");
		buffer.append("% Output Args:\n");
		buffer.append("% FLOW			-	NFLUX x 1 vector with the network flux\n");
		buffer.append("% status		-	status returned by glpk (180 means job completed OK)\n");
		buffer.append("% UPTAKE		-	NFREE_METAB x 1 vector with unbalanced upatke/production\n");
		buffer.append("%\n");
		buffer.append("% =========================================================================== %\n");
		buffer.append("function [FLOW,status,UPTAKE] = ");
		buffer.append(fileName);
		buffer.append("(pDataFile,OBJ,MIN_MAX_FLAG,DFIN)\n");
		buffer.append("\n");
		buffer.append("\t% Check to see if we are passing in a data struct -\n");
		buffer.append("\tif (isempty(DFIN))\n");
		buffer.append("\t\tDF = feval(pDataFile,MIN_MAX_FLAG,OBJ);\n");
		buffer.append("\telse\n");
		buffer.append("\t\tDF = DFIN;\n");
		buffer.append("\tend;\n");
		buffer.append("\n");
		buffer.append("\t% Get some stuff from the DF -\n");
		buffer.append("\tSTM = DF.STOICHIOMETRIC_MATRIX;\n");
		buffer.append("\t[NM,NRATES] = size(STM);\n");
		buffer.append("\n");	
		buffer.append("\t% Formulate objective vector (default is to minimize fluxes)\n");
		buffer.append("\tif (isempty(OBJ))\n");
		buffer.append("\t\tf=ones(1,NRATES);\n");
		buffer.append("\telse\n");
		buffer.append("\t\tf=zeros(1,NRATES);\n");
		buffer.append("\n");
		buffer.append("\t\tNI=length(OBJ);\n");
		buffer.append("\t\tfor obj_index=1:NI\n");
		buffer.append("\t\t\tif (MIN_MAX_FLAG==1)\n");
		buffer.append("\t\t\t\tf(OBJ(obj_index))=1;\n");
		buffer.append("\t\t\telse\n");
		buffer.append("\t\t\t\tf(OBJ(obj_index))=-1;\n");
		buffer.append("\t\t\tend;\n");
		buffer.append("\t\tend;\n");
		buffer.append("\tend;\n");
		buffer.append("\n");	
		buffer.append("\tOBJVECTOR = f;\n");
		buffer.append("\n");	
		buffer.append("\t% Get bounds from the DF -\n");
		buffer.append("\tvb = DF.FLUX_BOUNDS;\n");
		buffer.append("\tLB = vb(:,1);\n");
		buffer.append("\tUB = vb(:,2);\n");
		buffer.append("\n");
		buffer.append("\t% Setup the bV and the constraint types required by the solver -\n");
		buffer.append("\tSTM_BALANCED_BLOCK = DF.BALANCED_MATRIX;\n");
		buffer.append("\t% Get the dimension of the balanced block -\n");
		buffer.append("\t[NUM_BALANCED,NUM_RATES] = size(STM_BALANCED_BLOCK);\n");
		buffer.append("\t% Formulate the bV -\n");
		buffer.append("\tbV = zeros(NUM_BALANCED,1);\n");
		buffer.append("\t% Formulate the CTYPE vector -\n");
		buffer.append("\tfor species_index=1:NUM_BALANCED\n");
		buffer.append("\t\tCTYPE(species_index,1) = 'S';\n");
		buffer.append("\tend;\n");
		buffer.append("\n");
		buffer.append("\t% Formulate the VARTYPE vector -\n");
		buffer.append("\tfor rate_index=1:NUM_RATES\n");
		buffer.append("\t\tVARTYPE(rate_index,1) = 'C';\n");
		buffer.append("\tend;\n");
		buffer.append("\n");
		buffer.append("\t% This code puts bounds on the species, i.e., the mass balances run from l < xmb < u\n");
		buffer.append("\tSBA=DF.SPECIES_BOUND_ARRAY;\n");
		buffer.append("\tif (~isempty(SBA))\n");
		buffer.append("\n");	
		buffer.append("\t\t% Setup -\n");
		buffer.append("\t\tAC=DF.SPECIES_CONSTRAINTS;\n");
		buffer.append("\t\tCM = [STM_BALANCED_BLOCK ; AC ; AC];\n");
		buffer.append("\t\tN=size(SBA,1);\n");
		buffer.append("\t\tfor species_index=1:N\n");
		buffer.append("\t\t\tCTYPE(species_index+NUM_BALANCED,1) = 'U';\n");
		buffer.append("\t\t\tbV(species_index+NUM_BALANCED,1)=SBA(species_index,3);\n");
		buffer.append("\t\tend;\n");
		buffer.append("\n");
		buffer.append("\t\tM=size(CTYPE,1);\n");
		buffer.append("\t\tfor species_index=1:N\n");
		buffer.append("\t\t\tCTYPE(species_index+M,1) = 'L';\n");
		buffer.append("\t\t\tbV(species_index+M,1)=SBA(species_index,2);\n");
		buffer.append("\t\tend;\n");
		buffer.append("\tend;\n");
		buffer.append("\n");
		buffer.append("\t% Set the sense flag -\n");
		buffer.append("\tSENSE = 1;\n");
		buffer.append("\n");
		buffer.append("\t% Setup the PARAM structure -\n");
		buffer.append("\tPARAM.msglev = 3;\n");
		buffer.append("\n");
		buffer.append("\t% Type of LPSOLVE -\n");
		buffer.append("\tLPSOLVER = 1;\n");
		buffer.append("\t% Call GLPK -\n");
		buffer.append("\t[FLOW,fmin,status,extra]=glpk(OBJVECTOR,CM,bV,LB,UB,CTYPE,VARTYPE,SENSE,PARAM);\n");
		buffer.append("\n");
		buffer.append("\t% Calc the actual bV -\n");
		buffer.append("\tUPTAKE = AC*FLOW;\n");
		buffer.append("return;\n");
	}
		
	public void buildBoundsArray(Model model_wrapper,StringBuffer _buffer) throws Exception
	{
		System.out.println("Building the bounds array ... start");
		
		// Get some stuff we need (we are assuming the rates have been converted to 0,inf)
		ListOfReactions list_reactions = model_wrapper.getListOfReactions();
		ListOfSpecies list_species = model_wrapper.getListOfSpecies();
		
		// Ok, get the dimension of the network -
		int NUMBER_OF_REACTIONS = (int)list_reactions.size();
		int NUMBER_OF_SPECIES = (int)list_species.size();
		
		// Dump the bounds file to disk -
		int counter = 1;
		for (long index=0;index<NUMBER_OF_REACTIONS;index++)
		{
			// Build a line in the bounds array -
			_buffer.append("0	inf; % ");
			_buffer.append(counter);
			_buffer.append("\t");
			
			// Ok, lets get the string -
			Reaction rxn = list_reactions.get(index);
			
			// Populate the reactant string -
			ListOfSpeciesReferences list_reactants = rxn.getListOfReactants();
            long NUMBER_OF_REACTANTS = list_reactants.size();
			for (long reactant_index = 0;reactant_index<NUMBER_OF_REACTANTS;reactant_index++)
			{
				// Get the species reference and pull out information -
                SpeciesReference species = (SpeciesReference)list_reactants.get(reactant_index);
                _buffer.append(species.getSpecies());
                
                if (reactant_index<NUMBER_OF_REACTANTS-1)
                {
                	_buffer.append(" + ");
                }
			}
			
			// Put the arrow in -
			_buffer.append(" --> ");
			
			// Populate the product string -
			ListOfSpeciesReferences list_products = rxn.getListOfProducts();
            long NUMBER_OF_PRODUCTS = list_products.size();
			for (long product_index = 0;product_index<NUMBER_OF_PRODUCTS;product_index++)
			{
				// Get the species reference and pull out information -
                SpeciesReference species = (SpeciesReference)list_products.get(product_index);
                _buffer.append(species.getSpecies());
                
                if (product_index<NUMBER_OF_PRODUCTS-1)
                {
                	_buffer.append(" + ");
                }
			}
			
			// Ok, so when I get here I'm ready to end the rxn line -
			_buffer.append("\n");
			
			// Update the counter -
			counter++;
		}
		
		
		System.out.println("Building the bounds array ... completed");
	}
	
	
	// Logic to populate the datafile -
	public void populateDataFileBuffer(Model model_wrapper,StringBuffer _stbDataFile,Properties _propTable,Vector<Wrapper> vecSpecies) throws Exception
	{
		// Methods attribute -
		String strNetwork = _propTable.getProperty("OUTPUT_STM_FILENAME");
		String strBounds = _propTable.getProperty("OUTPUT_BOUNDS_FILENAME");
		String strProject = _propTable.getProperty("PATH_SRC_DIRECTORY");
		String strNetworkDir = _propTable.getProperty("PATH_NETWORK_DIRECTORY");
		String strDFFileName = _propTable.getProperty("OUTPUT_DATAFILE_FILENAME");
		Vector<Species> vecSpeciesLocal = new Vector<Species>();
		
		// Start the buffer -
		int PERIOD = strDFFileName.lastIndexOf(".");
		String strFunctionName = strDFFileName.substring(0, PERIOD); 
		
		_stbDataFile.append("function DF = ");
		_stbDataFile.append(strFunctionName);
		_stbDataFile.append("(MAX_FLAG,OBJ_VEC)\n");
		_stbDataFile.append("\n");
		
		// Setup the STM path and filename -
		_stbDataFile.append("% Load the stoichiometric matrix and flux bounds - \n");
		_stbDataFile.append("STM\t=\tload('");
		_stbDataFile.append(strNetworkDir);
		_stbDataFile.append("/");
		_stbDataFile.append(strNetwork);
		_stbDataFile.append("');\n");
		
		// Setup the FB path and filename -
		_stbDataFile.append("FB\t=\tload('");
		_stbDataFile.append(strNetworkDir);
		_stbDataFile.append("/");
		_stbDataFile.append(strBounds);
		_stbDataFile.append("');\n");
		
		_stbDataFile.append("% Get the dimension of the system - \n");
		_stbDataFile.append("[NR,NCO]=size(STM);\n");
		_stbDataFile.append("\n");
		
		// Figure out the external species -
		// Get the extracellular species tag -
		String strExtracellularTag = _propTable.getProperty("EXTRACELLULAR_TAGNAME");
		
		// Get species -
		ListOfSpecies list_species = model_wrapper.getListOfSpecies();
		
		// OK, so I've organized the species by compartment -
		SBMLModelUtilities.organizeSpeciesByCompartment(_propTable,model_wrapper,vecSpeciesLocal);
		int NUMBER_OF_SPECIES = vecSpeciesLocal.size();
		
		// Collect the species in this compartment -
		_stbDataFile.append("IDX_FREE_METABOLITES = [\n");
		int counter = 1;
		for (int species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
		{
			// Get the species -
			Species tmp = vecSpeciesLocal.get(species_index);
			
			// Check the compartment -
			String strCompartment = tmp.getCompartment();
			if (strCompartment.equalsIgnoreCase(strExtracellularTag))
			{
				// Ok, if I'm here then I have an extracellular species - figure out if it has a _b
				String strID = tmp.getId();
				if (strID.contains("_b"));
				{
					
					int INDEX = strID.indexOf("_b");
					System.out.println("WTF - "+strID+" index="+INDEX+" at counter="+counter);
					if (INDEX>1)
					{
						_stbDataFile.append("\t");
						_stbDataFile.append(counter);
						_stbDataFile.append("\t;\t%\t");
						_stbDataFile.append(strID);
						_stbDataFile.append("\n");
						
						// vec -
						Wrapper wrapper = new Wrapper();
						wrapper.setIndex(counter);
						wrapper.setSpecies(tmp);
						vecSpecies.addElement(wrapper);
					}
				}
			}
			
			// update the counter -
			counter++;
		}
		
		_stbDataFile.append("];\n");
		_stbDataFile.append("IDX_BALANCED_METABOLITES = setdiff(1:NR,IDX_FREE_METABOLITES);\n");
		_stbDataFile.append("N_IDX_BALANCED_METABOLITES = length(IDX_BALANCED_METABOLITES);\n");
		_stbDataFile.append("\n");
		
		_stbDataFile.append("% Setup bounds on species - \n");
		_stbDataFile.append("BASE_BOUND = 10;\n");
		_stbDataFile.append("SPECIES_BOUND=[\n");
		
		int NUMBER_OF_ESPECIES = vecSpecies.size();
		for (int index=0;index<NUMBER_OF_ESPECIES;index++)
		{
			// Get the wrapper from the vec -
			Wrapper wrapper = vecSpecies.get(index);
			
			// Get the species -
			Species species = wrapper.getSpecies();
			int local_index = wrapper.getIndex();
			
			_stbDataFile.append("\t");
			_stbDataFile.append(local_index);
			_stbDataFile.append("\t");
			_stbDataFile.append("0");
			//_stbDataFile.append("-1*BASE_BOUND");
			_stbDataFile.append("\t");
			_stbDataFile.append("BASE_BOUND");
			_stbDataFile.append("\t;\t");
			_stbDataFile.append("% ");
			_stbDataFile.append(index+1);
			_stbDataFile.append("\t");
			_stbDataFile.append(species.getId());
			_stbDataFile.append("\n");
		}
		
		_stbDataFile.append("];\n");
		_stbDataFile.append("\n");
		
		_stbDataFile.append("% Split the stochiometrix matrix - \n");
		_stbDataFile.append("S\t=\tSTM(IDX_BALANCED_METABOLITES,:);\n");
		_stbDataFile.append("SDB\t=\tSTM(SPECIES_BOUND(:,1),:);\n");
		
		_stbDataFile.append("\n");
		
		
		_stbDataFile.append("% === DO NOT EDIT BELOW THIS LINE ===================== %\n");
		_stbDataFile.append("DF.STOICHIOMETRIC_MATRIX = STM;\n");
		_stbDataFile.append("DF.SPECIES_BOUND_ARRAY=SPECIES_BOUND;\n");
		_stbDataFile.append("DF.FLUX_BOUNDS = FB;\n");
		_stbDataFile.append("DF.BALANCED_MATRIX = S;\n");
		_stbDataFile.append("DF.SPECIES_CONSTRAINTS=SDB;\n");
		_stbDataFile.append("% ===================================================== %\n");
		_stbDataFile.append("return;\n");
	}
}
