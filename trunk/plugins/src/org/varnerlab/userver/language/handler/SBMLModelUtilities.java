package org.varnerlab.userver.language.handler;

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


//import statements -
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;


import org.sbml.libsbml.*;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

public class SBMLModelUtilities {

	public static void dumpBoundsFileToDisk(Properties _propTable,StringBuffer buffer,XMLPropTree propTree) throws Exception {
        // Ok, dump this to disk -
        
		ArrayList<String> arrList = propTree.processFilenameBlock("BoundsFile");
		String strFileName = arrList.get(0);
		String strFilePath = arrList.get(2);
		String strPath = "";
		if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
		{
			strPath = strFilePath+"/"+strFileName;
			GIOL.write(strPath,buffer);
		}	
    }
	
	// Generate a vector of reacrions -
	public static void buildReactionStringVector(Model model, Vector vecReactions,Vector vecOut) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		int NUMBER_OF_RATES = (int)vecReactions.size();
		for (int index=0;index<NUMBER_OF_RATES;index++)
		{
			// Get the reaction -
			Reaction rxn = (Reaction)vecReactions.get(index);
			
			// Formulate the reaction string -
			ListOf reactant_list = rxn.getListOfReactants();
			int NUMBER_REACTANTS = (int)reactant_list.size();
			
			ListOf product_list = rxn.getListOfProducts();
			int NUMBER_PRODUCTS = (int)product_list.size();
			
			// buffer.append("// ");
			// buffer.append(index);
			// buffer.append("\t");
			for (int reactant_index=0;reactant_index<NUMBER_REACTANTS;reactant_index++)
			{
				SpeciesReference species_ref = (SpeciesReference)reactant_list.get(reactant_index);
                String strReactant = species_ref.getSpecies();
                buffer.append(strReactant);
                
                if (reactant_index<NUMBER_REACTANTS-1)
                {
                	buffer.append("+");
                }
                else
                {
                	buffer.append(" = ");
                }
			}
			
			for (int reactant_index=0;reactant_index<NUMBER_PRODUCTS;reactant_index++)
			{
				SpeciesReference species_ref = (SpeciesReference)product_list.get(reactant_index);
                String strReactant = species_ref.getSpecies();
                buffer.append(strReactant);
                
                if (reactant_index<NUMBER_PRODUCTS-1)
                {
                	buffer.append("+");
                }
			}
			
			//buffer.append("\n");
			vecOut.addElement(buffer.toString());
			buffer.delete(0, buffer.length());
		}
	}
	
	// write a reaction list to disk for debug -
	public static void buildDebugReactionListBuffer(StringBuffer buffer,Model model, Vector vecReactions) throws Exception
	{
		int NUMBER_OF_RATES = (int)vecReactions.size();
		for (int index=0;index<NUMBER_OF_RATES;index++)
		{
			// Get the reaction -
			Reaction rxn = (Reaction)vecReactions.get(index);
			
			// Formulate the reaction string -
			ListOf reactant_list = rxn.getListOfReactants();
			int NUMBER_REACTANTS = (int)reactant_list.size();
			
			ListOf product_list = rxn.getListOfProducts();
			int NUMBER_PRODUCTS = (int)product_list.size();
			
			buffer.append("// ");
			buffer.append(index);
			buffer.append("\t");
			for (int reactant_index=0;reactant_index<NUMBER_REACTANTS;reactant_index++)
			{
				SpeciesReference species_ref = (SpeciesReference)reactant_list.get(reactant_index);
                String strReactant = species_ref.getSpecies();
                buffer.append(strReactant);
                
                if (reactant_index<NUMBER_REACTANTS-1)
                {
                	buffer.append("+");
                }
                else
                {
                	buffer.append(" = ");
                }
			}
			
			for (int reactant_index=0;reactant_index<NUMBER_PRODUCTS;reactant_index++)
			{
				SpeciesReference species_ref = (SpeciesReference)product_list.get(reactant_index);
                String strReactant = species_ref.getSpecies();
                buffer.append(strReactant);
                
                if (reactant_index<NUMBER_PRODUCTS-1)
                {
                	buffer.append("+");
                }
			}
			
			buffer.append("\n");
			
		}
	}
	
	
	// Build the data file for the case when the reactions were expanded (and we are assming mass action kinetics)
	public static void buildDataFileBuffer(StringBuffer datafile,Model model,XMLPropTree propTree,Vector<Reaction> vecReactions,Vector<Species> vecSpecies) throws Exception
	{
		// Method attributes -
		ArrayList<String> arrList = propTree.processFilenameBlock("DataFile");
		String strDataFileNameRaw = arrList.get(0);
		String strDataFileName = arrList.get(1);
		
		// Check to make sure we have content -
		if (!strDataFileNameRaw.equalsIgnoreCase("EMPTY") && !strDataFileName.equalsIgnoreCase("EMPTY"))
		{
		
			//String strDataFileNameRaw = (String)propTree.getProperty("//DataFile/datafile_filename/text()");
	    	//int INT_TO_DOT = strDataFileNameRaw.indexOf(".");
	    	//String strDataFileName = strDataFileNameRaw.substring(0, INT_TO_DOT);
	    	
	    	// Put in the header and go forward my undercover brotha...
	        datafile.append("function DF=");
	        datafile.append(strDataFileName);
	        datafile.append("(TSTART,TSTOP,Ts,INDEX)\n");
	        
	        datafile.append("% ----------------------------------------------------------------------\n");
	        datafile.append("% ");
	        datafile.append(strDataFileName);
	        datafile.append(".m was generated using the UNIVERSAL code generator system.\n");
	        datafile.append("% Username: ");
	        datafile.append(propTree.getProperty(".//Model/@username"));
	        datafile.append("\n");
	        datafile.append("% Type: ");
	        datafile.append(propTree.getProperty(".//Model/@type"));
	        datafile.append("\n");
	        datafile.append("% Version: ");
	        datafile.append(propTree.getProperty(".//Model/@version"));
	        datafile.append("\n");
	        datafile.append("% \n");
	        datafile.append("% Arguments: \n");
	        datafile.append("% TSTART  - Time start \n");
	        datafile.append("% TSTOP  - Time stop \n");
	        datafile.append("% Ts - Time step \n");
	        datafile.append("% INDEX - Parameter set index (for ensemble calculations) \n");
	        datafile.append("% DF  - Data file instance \n");
	        datafile.append("% ----------------------------------------------------------------------\n");
	        datafile.append("\n");
	        datafile.append("% Load the stoichiometric matrix --\n");
	        datafile.append("S=load('");
	       
	        // I need to get the file name, not the entire path -
	        ArrayList<String> arrListStMatrix = propTree.processFilenameBlock("StoichiometricMatrix");
	        String strSTMNameRaw = arrListStMatrix.get(0);
	        String strSTMPath = arrListStMatrix.get(2);
	        String strPathToSTMFile = strSTMPath+"/"+strSTMNameRaw;
	        
	        //String strSTMNameRaw = (String)propTree.getProperty("//StoichiometricMatrix/stoichiometric_matrix_filename/text()");
	    	//String strSTMPath = (String)propTree.getProperty("//StoichiometricMatrix/stoichiometric_matrix_path/text()");
	        //String strWorkingDirectory = (String)propTree.getProperty("//working_directory/text()");
	       
	    	//INT_TO_DOT = strSTMNameRaw.indexOf(".");
	    	//String strSTMName = strSTMNameRaw.substring(0, INT_TO_DOT);
	                
	        datafile.append(strPathToSTMFile);
	        datafile.append("');\n");
	        datafile.append("[NROWS,NCOLS]=size(S);\n");
	        datafile.append("\n");
	        datafile.append("% Formulate the rate constant vector k --\n");
	        datafile.append("k=zeros(NCOLS,1);\n");
	        datafile.append("\n");
	        datafile.append("% Parameter vector --");
	        datafile.append("\n");
	        
	        // Put the initial values of parameters -
	        Vector vecOut = new Vector();
	        SBMLModelUtilities.buildReactionStringVector(model, vecReactions, vecOut);
	        datafile.append("k=[\n");
	        ListOf parameter_list = model.getListOfParameters();
	        int NUMBER_OF_PARAMETERS = (int)model.getNumParameters();
	        for (int pindex=0;pindex<NUMBER_OF_PARAMETERS;pindex++)
	        {
	        	
	        	Parameter parameter = (Parameter)parameter_list.get(pindex);        
	            Reaction rate = (Reaction)vecReactions.get(pindex);
	            
	            datafile.append("\t");
	            datafile.append(parameter.getValue());
	            datafile.append("\t;\t%\t");
	            datafile.append(pindex+1);
	            datafile.append("\t");
	            datafile.append(rate.getName());
	            datafile.append("\t");
	            datafile.append(vecOut.get(pindex));
	            datafile.append("\n");
	        }
	        datafile.append("];\n");
	        datafile.append("\n");
	        datafile.append("% Initial conditions --\n");
	        
	        // Put the initial condition -
	        datafile.append("IC=[\n");
	        //ListOf species_list = model.getListOfSpecies();
	        int NUMBER_OF_SPECIES = (int)vecSpecies.size();
	        for (int pindex=0;pindex<NUMBER_OF_SPECIES;pindex++)
	        {
	            Species species = (Species)vecSpecies.get(pindex);
	            datafile.append("\t");
	            datafile.append(species.getInitialConcentration());
	            datafile.append("\t;%\t");
	            datafile.append(pindex+1);
	            datafile.append("\t");
	            datafile.append(species.getId());
	            datafile.append("\t");
	            datafile.append(species.getName());
	            datafile.append("\n");
	        }
	        
	        datafile.append("];\n");
	        datafile.append("\n");
	        datafile.append("% Load parameter sets from disk -\n");
	        datafile.append("NPARAMETERS=length(k);\n");
	        datafile.append("NSTATES=length(IC);\n");
	        datafile.append("kV = [k ; IC];\n");
	        datafile.append("% Ok, override the choice of parameters above, load from disk -\n");
	        datafile.append("if (~isempty(INDEX))\n");
	        datafile.append("\tcmd=['load PSET_',num2str(INDEX),'.mat'];\n");
	        datafile.append("\teval(cmd);\n");
	        datafile.append("\tkV = kP;\n");
	        datafile.append("\t% get k and IC -\n");
	        datafile.append("\tk=kV(1:NPARAMETERS);\n");
	        datafile.append("\tIC=kV((NPARAMETERS+1):end);\n");
	        datafile.append("end;\n");
	        datafile.append("\n");
	        
	        // Populate the measurement selection matrix -
	        datafile.append("% Initialize tehg measurement selection matrix. Default is the identity matrix \n");
	        datafile.append("MEASUREMENT_INDEX_VECTOR = [1:NSTATES];\n");
	        datafile.append("\n");
	        datafile.append("% =========== DO NOT EDIT BELOW THIS LINE ==============\n");
	        datafile.append("DF.STOICHIOMETRIC_MATRIX=S;\n");
	        datafile.append("DF.RATE_CONSTANT_VECTOR=k;\n");
	        datafile.append("DF.INITIAL_CONDITIONS=IC;\n");
	        datafile.append("DF.NUMBER_PARAMETERS=NPARAMETERS;\n");
	        datafile.append("DF.NUMBER_OF_STATES=NSTATES;\n");
	        datafile.append("DF.PARAMETER_VECTOR=kV;\n");
	        datafile.append("DF.MEASUREMENT_SELECTION_VECTOR = MEASUREMENT_INDEX_VECTOR;\n");
	        datafile.append("% ======================================================\n");
	        datafile.append("return;\n");
		}
		else
		{
			throw new Exception("ERROR: Missing DataFile information. Please check your DataFile settings.");
		}
	}
	
	// Build the data file - these are always the same, so put here
    public static void buildDataFileBuffer(StringBuffer datafile,Model model,XMLPropTree propTree) throws Exception
    {
        // Method attributes -	
    	ArrayList<String> arrList = propTree.processFilenameBlock("DataFile");
		String strDataFileNameRaw = arrList.get(0);
		String strDataFileName = arrList.get(1);
		
		// Check to make sure we have content -
		if (!strDataFileNameRaw.equalsIgnoreCase("EMPTY") && !strDataFileName.equalsIgnoreCase("EMPTY"))
		{
    	
    	
    	//String strDataFileNameRaw = (String)propTree.getProperty("//DataFile/datafile_filename/text()");
    	//int INT_TO_DOT = strDataFileNameRaw.indexOf(".");
    	//String strDataFileName = strDataFileNameRaw.substring(0, INT_TO_DOT);
    	
	    	// Put in the header and go forward my undercover brotha...
	        datafile.append("function DF=");
	        datafile.append(strDataFileName);
	        datafile.append("(TSTART,TSTOP,Ts,INDEX)\n");
	        
	        datafile.append("% ----------------------------------------------------------------------\n");
	        datafile.append("% ");
	        datafile.append(strDataFileName);
	        datafile.append(".m was generated using the UNIVERSAL code generator system.\n");
	        datafile.append("% Username: ");
	        datafile.append(propTree.getProperty(".//Model/@username"));
	        datafile.append("\n");
	        datafile.append("% Type: ");
	        datafile.append(propTree.getProperty(".//Model/@type"));
	        datafile.append("\n");
	        datafile.append("% Version: ");
	        datafile.append(propTree.getProperty(".//Model/@version"));
	        datafile.append("\n");
	        datafile.append("% \n");
	        datafile.append("% Arguments: \n");
	        datafile.append("% TSTART  - Time start \n");
	        datafile.append("% TSTOP  - Time stop \n");
	        datafile.append("% Ts - Time step \n");
	        datafile.append("% INDEX - Parameter set index (for ensemble calculations) \n");
	        datafile.append("% DF  - Data file instance \n");
	        datafile.append("% ----------------------------------------------------------------------\n");
	        datafile.append("\n");
	        datafile.append("% Load the stoichiometric matrix --\n");
	        datafile.append("S=load('");
	       
	        // I need to get the file name, not the entire path -
	        ArrayList<String> arrListStMatrix = propTree.processFilenameBlock("StoichiometricMatrix");
	        String strSTMNameRaw = arrListStMatrix.get(0);
	        String strSTMPath = arrListStMatrix.get(2);
	        String strPathToSTMFile = strSTMPath+"/"+strSTMNameRaw;
	        
	        datafile.append(strPathToSTMFile);
	        datafile.append("');\n");
	        datafile.append("[NROWS,NCOLS]=size(S);\n");
	        datafile.append("\n");
	        datafile.append("% Formulate the rate constant vector k --\n");
	        datafile.append("k=zeros(NCOLS,1);\n");
	        datafile.append("\n");
	        datafile.append("% Parameter vector --");
	        datafile.append("\n");
	        
	        // Put the initial values of parameters -
	        datafile.append("k=[\n");
	        ListOf parameter_list = model.getListOfParameters();
	        ListOf rate_list = model.getListOfReactions();
	        int NUMBER_OF_PARAMETERS = (int)model.getNumParameters();
	        for (int pindex=0;pindex<NUMBER_OF_PARAMETERS;pindex++)
	        {
	            Parameter parameter = (Parameter)parameter_list.get(pindex);        
	            Reaction rate = (Reaction)rate_list.get(pindex);
	            
	            datafile.append("\t");
	            datafile.append(parameter.getValue());
	            datafile.append("\t;\t%\t");
	            datafile.append(pindex+1);
	            datafile.append("\t");
	            datafile.append(rate.getName());
	            datafile.append("\n");
	        }
	        datafile.append("];\n");
	        datafile.append("\n");
	        datafile.append("% Initial conditions --\n");
	        
	        // Put the initial condition -
	        datafile.append("IC=[\n");
	        ListOf species_list = model.getListOfSpecies();
	        int NUMBER_OF_SPECIES = (int)species_list.size();
	        for (int pindex=0;pindex<NUMBER_OF_SPECIES;pindex++)
	        {
	            Species species = (Species)species_list.get(pindex);
	            datafile.append("\t");
	            
	            // Ok, initial condition -or- amount?
	          
	    
	            datafile.append(species.getInitialConcentration());
	            datafile.append("\t;%\t");
	            datafile.append(pindex+1);
	            datafile.append("\t ID: ");
	            datafile.append(species.getId());
	            datafile.append("\t NAME: ");
	            datafile.append(species.getName());
	            datafile.append("\n");
	        }
	        
	        datafile.append("];\n");
	        datafile.append("\n");
	        datafile.append("% Load parameter sets from disk -\n");
	        datafile.append("NPARAMETERS=length(k);\n");
	        datafile.append("NSTATES=length(IC);\n");
	        datafile.append("kV = [k ; IC];\n");
	        datafile.append("% Ok, override the choice of parameters above, load from disk -\n");
	        datafile.append("if (~isempty(INDEX))\n");
	        datafile.append("\tcmd=['load PSET_',num2str(INDEX),'.mat'];\n");
	        datafile.append("\teval(cmd);\n");
	        datafile.append("\tkV = kP;\n");
	        datafile.append("\t% get k and IC -\n");
	        datafile.append("\tk=kV(1:NPARAMETERS);\n");
	        datafile.append("\tIC=kV((NPARAMETERS+1):end);\n");
	        datafile.append("end;\n");
	        datafile.append("\n");
	        
	        // Populate the measurement selection matrix -
	        datafile.append("% Initialize tehg measurement selection matrix. Default is the identity matrix \n");
	        datafile.append("MEASUREMENT_INDEX_VECTOR = [1:NSTATES];\n");
	        datafile.append("\n");
	        datafile.append("% =========== DO NOT EDIT BELOW THIS LINE ==============\n");
	        datafile.append("DF.STOICHIOMETRIC_MATRIX=S;\n");
	        datafile.append("DF.RATE_CONSTANT_VECTOR=k;\n");
	        datafile.append("DF.INITIAL_CONDITIONS=IC;\n");
	        datafile.append("DF.NUMBER_PARAMETERS=NPARAMETERS;\n");
	        datafile.append("DF.NUMBER_OF_STATES=NSTATES;\n");
	        datafile.append("DF.PARAMETER_VECTOR=kV;\n");
	        datafile.append("DF.MEASUREMENT_SELECTION_VECTOR = MEASUREMENT_INDEX_VECTOR;\n");
	        datafile.append("% ======================================================\n");
	        datafile.append("return;\n");
		}
		else
		{
			throw new Exception("ERROR: Missing DataFile information. Please check your DataFile settings.");
		}
    }


    // Build the data file for the case when the reactions were expanded (and we are assming mass action kinetics)
    // also we do not want to include the STM (for large models)
	public static void buildDataFileBufferNoSTM(StringBuffer datafile,Model model,XMLPropTree propTree,Vector<Reaction> vecReactions,Vector<Species> vecSpecies) throws Exception
	{
		// Method attributes -
		ArrayList<String> arrList = propTree.processFilenameBlock("DataFile");
		String strDataFileNameRaw = arrList.get(0);
		String strDataFileName = arrList.get(1);

		// Check to make sure we have content -
		if (!strDataFileNameRaw.equalsIgnoreCase("EMPTY") && !strDataFileName.equalsIgnoreCase("EMPTY"))
		{

			//String strDataFileNameRaw = (String)propTree.getProperty("//DataFile/datafile_filename/text()");
	    	//int INT_TO_DOT = strDataFileNameRaw.indexOf(".");
	    	//String strDataFileName = strDataFileNameRaw.substring(0, INT_TO_DOT);

	    	// Put in the header and go forward my undercover brotha...
	        datafile.append("function DF=");
	        datafile.append(strDataFileName);
	        datafile.append("(TSTART,TSTOP,Ts,INDEX)\n");

	        datafile.append("% ----------------------------------------------------------------------\n");
	        datafile.append("% ");
	        datafile.append(strDataFileName);
	        datafile.append(".m was generated using the UNIVERSAL code generator system.\n");
	        datafile.append("% Username: ");
	        datafile.append(propTree.getProperty(".//Model/@username"));
	        datafile.append("\n");
	        datafile.append("% Type: ");
	        datafile.append(propTree.getProperty(".//Model/@type"));
	        datafile.append("\n");
	        datafile.append("% Version: ");
	        datafile.append(propTree.getProperty(".//Model/@version"));
	        datafile.append("\n");
	        datafile.append("% \n");
	        datafile.append("% Arguments: \n");
	        datafile.append("% TSTART  - Time start \n");
	        datafile.append("% TSTOP  - Time stop \n");
	        datafile.append("% Ts - Time step \n");
	        datafile.append("% INDEX - Parameter set index (for ensemble calculations) \n");
	        datafile.append("% DF  - Data file instance \n");
	        datafile.append("% ----------------------------------------------------------------------\n");
	        datafile.append("\n");
                datafile.append("% Loading a null stoichiometric matrix for consistance --\n");
	        datafile.append("S=[0];");


	        // Put the initial values of parameters -
	        Vector vecOut = new Vector();
	        SBMLModelUtilities.buildReactionStringVector(model, vecReactions, vecOut);
	        datafile.append("k=[\n");
	        ListOf parameter_list = model.getListOfParameters();
	        int NUMBER_OF_PARAMETERS = (int)model.getNumParameters();
                datafile.append("% Formulate the rate constant vector k --\n");
	        datafile.append("k=zeros(");
                datafile.append(String.valueOf(NUMBER_OF_PARAMETERS));
                datafile.append(",1);\n");
	        datafile.append("\n");
	        datafile.append("% Parameter vector --");
	        datafile.append("\n");
	        for (int pindex=0;pindex<NUMBER_OF_PARAMETERS;pindex++)
	        {

	        	Parameter parameter = (Parameter)parameter_list.get(pindex);
	            Reaction rate = (Reaction)vecReactions.get(pindex);

	            datafile.append("\t");
	            datafile.append(parameter.getValue());
	            datafile.append("\t;\t%\t");
	            datafile.append(pindex+1);
	            datafile.append("\t");
	            datafile.append(rate.getName());
	            datafile.append("\t");
	            datafile.append(vecOut.get(pindex));
	            datafile.append("\n");
	        }
	        datafile.append("];\n");
	        datafile.append("\n");
	        datafile.append("% Initial conditions --\n");

	        //ListOf species_list = model.getListOfSpecies();
	        int NUMBER_OF_SPECIES = (int)vecSpecies.size();
                datafile.append("IC=zeros(");
                datafile.append(String.valueOf(NUMBER_OF_SPECIES));
                datafile.append(",1);\n");
	        datafile.append("\n");

	        // Put the initial condition -
	        datafile.append("IC=[\n");
	        for (int pindex=0;pindex<NUMBER_OF_SPECIES;pindex++)
	        {
	            Species species = (Species)vecSpecies.get(pindex);
	            datafile.append("\t");
	            datafile.append(species.getInitialAmount());
	            datafile.append("\t;%\t");
	            datafile.append(pindex+1);
	            datafile.append("\t");
	            datafile.append(species.getId());
	            datafile.append("\t");
	            datafile.append(species.getName());
	            datafile.append("\n");
	        }

	        datafile.append("];\n");
	        datafile.append("\n");
	        datafile.append("% Load parameter sets from disk -\n");
	        datafile.append("NPARAMETERS=length(k);\n");
	        datafile.append("NSTATES=length(IC);\n");
	        datafile.append("kV = [k ; IC];\n");
	        datafile.append("% Ok, override the choice of parameters above, load from disk -\n");
	        datafile.append("if (~isempty(INDEX))\n");
	        datafile.append("\tcmd=['load PSET_',num2str(INDEX),'.mat'];\n");
	        datafile.append("\teval(cmd);\n");
	        datafile.append("\tkV = kP;\n");
	        datafile.append("\t% get k and IC -\n");
	        datafile.append("\tk=kV(1:NPARAMETERS);\n");
	        datafile.append("\tIC=kV((NPARAMETERS+1):end);\n");
	        datafile.append("end;\n");
	        datafile.append("\n");

	        // Populate the measurement selection matrix -
	        datafile.append("% Initialize tehg measurement selection matrix. Default is the identity matrix \n");
	        datafile.append("MEASUREMENT_MATRIX = eye(NSTATES,NSTATES);\n");

	        datafile.append("% =========== DO NOT EDIT BELOW THIS LINE ==============\n");
                datafile.append("DF.STOICHIOMETRIC_MATRIX=S;\n");
	        datafile.append("DF.RATE_CONSTANT_VECTOR=k;\n");
	        datafile.append("DF.INITIAL_CONDITIONS=IC;\n");
	        datafile.append("DF.NUMBER_PARAMETERS=NPARAMETERS;\n");
	        datafile.append("DF.NUMBER_OF_STATES=NSTATES;\n");
	        datafile.append("DF.PARAMETER_VECTOR=kV;\n");
	        datafile.append("DF.MEASUREMENT_SELECTION_MATRIX = MEASUREMENT_MATRIX;\n");
	        datafile.append("% ======================================================\n");
	        datafile.append("return;\n");
		}
		else
		{
			throw new Exception("ERROR: Missing DataFile information. Please check your DataFile settings.");
		}
	}
    
    public static void dumpKineticsToDisk(StringBuffer data_buffer,XMLPropTree _xmlPropTree) throws Exception {
    	
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("KineticsFunction");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,data_buffer);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the kinetics files. Check the kinetics settings.");
        } 	
    }

    public static void dumpDataFileToDisk(StringBuffer data_buffer,XMLPropTree _xmlPropTree) throws Exception {
    	// I have populated the string buffer, dump that mofo
        
    	// Method attributes -	
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("DataFile");
		String strDataFileName = arrList.get(0);
		String strDataFilePath = arrList.get(2);
		String strSBMLFile = "";
		if (!strDataFileName.equalsIgnoreCase("EMPTY") && !strDataFilePath.equalsIgnoreCase("EMPTY"))
		{
    	
			//String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
			//String strFileName = _xmlPropTree.getProperty("//DataFile/datafile_filename/text()");
			//String strFilePath = _xmlPropTree.getProperty("//DataFile/datafile_path/text()");
			strSBMLFile = strDataFilePath+"/"+strDataFileName;
			GIOL.write(strSBMLFile,data_buffer);
		}
		else
		{
			// We have a malfunction - throw an exception
			throw new Exception("ERROR: Missing DataFile information. Please check your DataFile settings.");
		}
    }
    
    public static void dumpSEBufferToDisk(StringBuffer data_buffer,XMLPropTree _xmlPropTree,String strFileName) throws Exception
    {
    	// I have populated the string buffer, dump that mofo
        String strWorkingDir = _xmlPropTree.getProperty(".//path[@symbol='UNIVERSAL_OUTPUT_PATH']/@path_location");
        String strFilePath = "";
        String strSBMLFile = "";
        
        if (strFilePath.isEmpty())
        {
        	strSBMLFile = strWorkingDir+"/"+strFileName;
        }
        else
        {
        	strSBMLFile = strWorkingDir+"/"+strFilePath+"/"+strFileName;
        }
        
        GIOL.write(strSBMLFile,data_buffer);
    }
    
    public static void dumpGeneralBufferToDisk(StringBuffer data_buffer,XMLPropTree xmlPropTree) throws Exception
    {
    	// I have populated the string buffer, dump that mofo
    	//String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        //String strDotFileName = _xmlPropTree.getProperty("//OutputFileName/output_filename/text()");
        //String strDotFilePath = _xmlPropTree.getProperty("//OutputFileName/output_file_path/text()");
        
    	// Get the output name -
    	Hashtable<String,String> pathTable = xmlPropTree.buildFilenameBlockDictionary("OutputFile");
    	String strSBMLFile = pathTable.get("FULLY_QUALIFIED_PATH");
        
        GIOL.write(strSBMLFile,data_buffer);
    }

    public static void dumpDebugFileToDisk(StringBuffer data_buffer,XMLPropTree _xmlPropTree) throws Exception 
    {
    	// I have populated the string buffer, dump that mofo
        String strFileName = _xmlPropTree.getProperty(".//DebugOutputFile/@filename");
        String strFilePath = _xmlPropTree.getProperty(".//path[@symbol='UNIVERSAL_DEBUG_OUTPUT_PATH']/@path_location");
        
        String strSBMLFile = "";  
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,data_buffer);
        }
        else
        {
        	throw new Exception("ERROR: Missing debug file information. Please check the Debug file settings.");
        } 
    }
    
    public static void dumpStoichiometricMatrixToDisk(double[][] dblSTMatrix,XMLPropTree _xmlPropTree,Model model_wrapper,Vector<Reaction> vecReactions) throws Exception
    {
        // Method attributes -
        StringBuffer buffer = new StringBuffer();
        int NUMBER_OF_SPECIES = 0; 
        int NUMBER_OF_RATES = 0;
        
        // Get the system dimension -
        NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        NUMBER_OF_RATES = (int)vecReactions.size(); 
        
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
            {
                buffer.append(dblSTMatrix[scounter][rcounter]);
                buffer.append("\t");
            }
            
            buffer.append("\n");
        }
        
        // Get the path to the place where I'm going to dump the stmatrix -
        
        // Get information -
        ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("StoichiometricMatrix");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,buffer);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the stoichiometric matrix. Check the stoichiometric matrix settings.");
        }
        
        //String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        //String strFileName = _xmlPropTree.getProperty("//StoichiometricMatrix/stoichiometric_matrix_filename/text()");
        //String strFilePath = _xmlPropTree.getProperty("//StoichiometricMatrix/stoichiometric_matrix_path/text()");
        
    }

    
    public static void dumpMassBalancesToDisk(StringBuffer massbalances,XMLPropTree _xmlPropTree) throws Exception
    {
    	
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("MassBalanceFunction");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,massbalances);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the massbalance equations. Check the massbalance settings.");
        } 	
    }
    
    public static void dumpExtracellularMassBalancesToDisk(StringBuffer massbalances,XMLPropTree _xmlPropTree) throws Exception
    {
    	
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("ExtracellularMassBalanceFunction");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,massbalances);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the massbalance equations. Check the massbalance settings.");
        } 	
    }
    
    public static void dumpAdjDriverFileToDisk(StringBuffer massbalances,XMLPropTree _xmlPropTree) throws Exception
    {
    	
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("AdjointDriver");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,massbalances);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the adjoint driver file. Check the sensitivity settings.");
        } 	
    	
    }
    
    public static void dumpAdjFunctionFileToDisk(StringBuffer massbalances,XMLPropTree _xmlPropTree) throws Exception
    {
    	
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("AdjointBalances");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,massbalances);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the adjoint balances file. Check the sensitivity settings.");
        } 	    	
    }
    
    public static void dumpDriverToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("DriverFile");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,driver);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the driver file. Check the driver file settings.");
        } 	    	
    }
    
    public static void dumpBuildFileToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("BuildFile");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,driver);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the driver file. Check the driver file settings.");
        } 	    
    }
    
    public static void dumpJacobianToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("JacobianMatrix");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,driver);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the Jacobian matrix. Check the Jacobian file settings.");
        } 	    
    }
    
    public static void dumpBMatrixToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("PMatrix");
        String strFileName = arrList.get(0);
        String strFilePath = arrList.get(2);
        
        // Check to make sure we have data in the string -
        String strSBMLFile = "";
        if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
        {
        	// Path information for stoichiometric matrix -
        	strSBMLFile = strFilePath+"/"+strFileName;
        	GIOL.write(strSBMLFile,driver);
        }
        else
        {
        	// OK, there was some malfunction -
        	throw new Exception("ERROR: We have some issue writing the B matrix. Check the BMatrix file settings.");
        } 	    
    }
    
    public static void dumpInputFunctionToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        // I have populated the string buffer, dump that mofo
        Hashtable<String,String> pathHashtable = _xmlPropTree.buildFilenameBlockDictionary("InputFunction");
        
        // Get the fully qualified name -
        String strSBMLFile = pathHashtable.get("FULLY_QUALIFIED_PATH");
        
        // Dump to disk -
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpSimulationFunctionToDisk(StringBuffer driver,XMLPropTree _xmlPropTree,String strExpID) throws Exception
    {
    	// I have populated the string buffer, dump that mofo
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        String strSBMLFile = "";
        strSBMLFile = strWorkingDir+"/SIM_"+strExpID+".m";
        
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpErrorFunctionToDisk(StringBuffer driver,XMLPropTree _xmlPropTree,String strExpID) throws Exception
    {
    	// I have populated the string buffer, dump that mofo
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        String strSBMLFile = "";
        strSBMLFile = strWorkingDir+"/ERR_"+strExpID+".m";
        
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpTestSimFunctionToDisk(StringBuffer driver,XMLPropTree _xmlPropTree,String strExpID) throws Exception
    {
    	// I have populated the string buffer, dump that mofo
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        String strSBMLFile = "";
        strSBMLFile = strWorkingDir+"/TEST_SIM_"+strExpID+".m";
        
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpSimGrpFunctionToDisk(StringBuffer driver,XMLPropTree _xmlPropTree,String strExpID) throws Exception
    {
    	// I have populated the string buffer, dump that mofo
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        String strSBMLFile = "";
        strSBMLFile = strWorkingDir+"/SIMGRP_"+strExpID+".m";
        
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpScaleFunctionToDisk(StringBuffer driver,XMLPropTree _xmlPropTree,String strExpID) throws Exception
    {
    	// I have populated the string buffer, dump that mofo
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        String strSBMLFile = "";
        strSBMLFile = strWorkingDir+"/SCALE_"+strExpID+".m";
        
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpExpDataStructToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        // I have populated the string buffer, dump that mofo
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        String strFileName = _xmlPropTree.getProperty("//experimental_data_structure_filename/text()");
        String strFilePath = "";
        
        String strSBMLFile = "";
        if (strFilePath.isEmpty())
        {
        	strSBMLFile = strWorkingDir+"/"+strFileName;
        }
        else
        {
        	strSBMLFile = strWorkingDir+"/"+strFilePath+"/"+strFileName;
        }
        
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpShellCommandToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        // I have populated the string buffer, dump to disk -
        Hashtable<String,String> pathHashtable = _xmlPropTree.buildFilenameBlockDictionary("SimulationDriverFile");
        String strSBMLFile = pathHashtable.get("FULLY_QUALIFIED_PATH");
        
        // Write to disk -
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpSunsialsPluginToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        // I have populated the string buffer, dump that mofo
        //String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        //String strFilePath = _xmlPropTree.getProperty("//MassBalanceFunction/massbalance_path/text()");
        String strFileName = "SolveSundialsModel.m";
     
        // Process info for the parameters file -
    	Hashtable<String,String> pathParameters = _xmlPropTree.buildFilenameBlockDictionary("MassBalanceFunction");
    	String strParameters = pathParameters.get("FILENAME_PATH");
    	String strSBMLFile = strParameters+"/"+strFileName;
               
        GIOL.write(strSBMLFile,driver);
    }
    
    public static void dumpLSODECallWrapperSundialsToDisk(StringBuffer driver,XMLPropTree _xmlPropTree) throws Exception
    {
        // I have populated the string buffer, dump that mofo
        //String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        //String strFilePath = _xmlPropTree.getProperty("//MassBalanceFunction/massbalance_path/text()");
        String strFileName = "LSODECallWrapper.m";
        
        // Process info for the parameters file -
    	Hashtable<String,String> pathParameters = _xmlPropTree.buildFilenameBlockDictionary("MassBalanceFunction");
    	String strParameters = pathParameters.get("FILENAME_PATH");
    	String strSBMLFile = strParameters+"/"+strFileName;
        
        // Dump to disk -
        GIOL.write(strSBMLFile,driver);
    }
    

	
	public static void organizeSpeciesByCompartment(Properties _propTable,Model model_wrapper,Vector<Species> vecSpecies) throws Exception
	{
		// Get species -
		ListOfSpecies list_species = model_wrapper.getListOfSpecies();
		long NUMBER_OF_SPECIES = model_wrapper.getNumSpecies();
		
		// Get the list of compartments -
		ListOfCompartments compartments = model_wrapper.getListOfCompartments();
		long NUMBER_OF_COMPARTMENTS = model_wrapper.getNumCompartments();

		for (long index=0;index<NUMBER_OF_COMPARTMENTS;index++)
		{
			// Get the current compartment -
			Compartment current_compartment = compartments.get(index);
			String strCompartmentID = current_compartment.getId();
			
			// Collect the species in this compartment -
			for (long species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
			{
				// Get the species -
				Species tmp = list_species.get(species_index);
				
				// Check the compartment -
				String strCompartment = tmp.getCompartment();
				if (strCompartment.equalsIgnoreCase(strCompartmentID))
				{
					vecSpecies.addElement(tmp);
				}
			}
		}
	}
	
	public static void reorderSpeciesVector(Model model_wrapper,Vector vecOrder,Vector<Species> vecSpecies) throws Exception
	{
		// Method resources -
		Vector<Species> local_vector = new Vector<Species>();
		Vector<String> local_vector_id = new Vector<String>();
		Vector<Species> bottom_vector = new Vector<Species>();
		
		// Transfer the SBML species list into a vector -
		ListOf species_list_tmp = model_wrapper.getListOfSpecies();
        long NUMBER_OF_SPECIES = model_wrapper.getNumSpecies();
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            Species species_tmp = (Species)species_list_tmp.get(scounter);
            String strSpeciesID = species_tmp.getId();
            local_vector.add(species_tmp);
            local_vector_id.add(strSpeciesID);
        }
		
		// Ok, when I get here I have all the species in the local_vector -
        int NUMBER_ORDER = vecOrder.size();
        for (int order_index=0;order_index<NUMBER_ORDER;order_index++)
        {
        	// Ok, get the test species symbol -
        	String strTestSymbol = (String)vecOrder.get(order_index);
        	
        	// Check to see if this is a valid symbol -
        	if (local_vector_id.contains(strTestSymbol))
        	{
        		// Ok, If I get here then I have a valid symbol -
        		local_vector_id.remove(strTestSymbol);

				if (!local_vector_id.contains(strTestSymbol))
				{
					local_vector_id.addElement(strTestSymbol);
				}
        	}		
        }
        
        // When I get here I have the sorted local_vector of ID's
        // I need to get the species object -
        int NUMBER_SORTED_SPECIES = local_vector_id.size();
        for (int species_index=0;species_index<NUMBER_SORTED_SPECIES;species_index++)
        {
        	// Get the species -
        	String strTmpID = local_vector_id.get(species_index);
        	
        	// Find this species in the <Species> vector -
        	int NUMBER_SORTED_INNER = local_vector.size();
        	for (int species_index_inner=0;species_index_inner<NUMBER_SORTED_INNER;species_index_inner++)
        	{
        		// Get the species id -
        		Species tmpSpecies = (Species)local_vector.get(species_index_inner);
        		String strSpeciesTmpId = tmpSpecies.getId();
        		
        		// Compare -
        		if (strTmpID.equalsIgnoreCase(strSpeciesTmpId))
        		{
        			// Add this Species to vecSpecies -
        			vecSpecies.addElement(tmpSpecies);
        			break;
        		}
        	}
        }
     }
	
	
	// Util method to dump species list to disk -
	public static void dumpSpeciesToDisk(Properties _propTable,Model model_wrapper) throws Exception
	{
		// Method attributes =
		StringBuffer buffer = new StringBuffer();
		Vector<Species> vecSpecies = new Vector<Species>();
		
		// Get species -
		ListOfSpecies list_species = model_wrapper.getListOfSpecies();
		long NUMBER_OF_SPECIES = model_wrapper.getNumSpecies();
		
		// Get the list of compartments -
		ListOfCompartments compartments = model_wrapper.getListOfCompartments();
		long NUMBER_OF_COMPARTMENTS = model_wrapper.getNumCompartments();

		int outer_counter = 1;
		for (long index=0;index<NUMBER_OF_COMPARTMENTS;index++)
		{
			// Get the current compartment -
			Compartment current_compartment = compartments.get(index);
			String strCompartmentID = current_compartment.getId();
			
			// Collect the species in this compartment -
			for (long species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
			{
				// Get the species -
				Species tmp = list_species.get(species_index);
				
				// Check the compartment -
				String strCompartment = tmp.getCompartment();
				if (strCompartment.equalsIgnoreCase(strCompartmentID))
				{
					vecSpecies.addElement(tmp);
				}
			}
			
			// Ok, when I get here I have collected all the species in the current compartment - write them to the buffer
			int NUM_SPECIES_IN_COMPARTMENT = vecSpecies.size();
			int inner_counter = 1;
			for (int buffer_index=0;buffer_index<NUM_SPECIES_IN_COMPARTMENT;buffer_index++)
			{
				// Get the species -
				Species tmp = vecSpecies.get(buffer_index);
				buffer.append(tmp.getId());
				buffer.append("\t");
				buffer.append(inner_counter);
				buffer.append("\t");
				buffer.append(outer_counter);
				buffer.append("\t");
				buffer.append(strCompartmentID);
				buffer.append("\n");
				
				// update the counter -
				inner_counter++;
				
				// Update the outer_counter -
				outer_counter++;
			}
			
			// Ok, clear out the species vec and go around again -
			vecSpecies.removeAllElements();
		}
		
		// Get the path and dump -2- disk 
		// Create the path string -
		String strPath = _propTable.getProperty("PATH_NETWORK_DIRECTORY")+"/"+_propTable.getProperty("OUTPUT_SPECIES_FILENAME");
        GIOL.write(strPath,buffer);
	}
	
	
	
	// Build the stoichiometric matrix -
    public static void buildStoichiometricMatrix(double[][] dblSTMatrix,Model model_wrapper,Vector<Reaction> listRates,Vector<Species> listSpecies) throws Exception
    {
        
        // Get the dimension of the system -
        int NUMBER_OF_SPECIES = 0; 
        int NUMBER_OF_RATES = 0;
        
        // Get the system dimension -
        NUMBER_OF_SPECIES = (int)listSpecies.size();
        NUMBER_OF_RATES = (int)listRates.size(); 
        
        System.out.println("Dimension "+NUMBER_OF_SPECIES+" by "+NUMBER_OF_RATES);
        
        // Go through and put everything as zeros by default -
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
            {
                dblSTMatrix[scounter][rcounter]=0.0;
            }
        }
        
        // ListOf listSpecies = model_wrapper.getListOfSpecies();
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            // Get the species reference -
            Species species = (Species)listSpecies.get(scounter);
            String strSpecies = species.getId();
            
            System.out.println("Processing species  - "+strSpecies);
            
            // Ok, I need to go through the rates and determine if this species is involved -
            for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
            {
                // Get the Reaction object -
                Reaction rxn_obj = (Reaction)listRates.get(rcounter);
                
                // Get the 'radius' of this rate -
                int NUMBER_OF_REACTANTS = (int)rxn_obj.getNumReactants();
                int NUMBER_OF_PRODUCTS = (int)rxn_obj.getNumProducts();
                
                // go through the reactants of this reaction -
                for (int reactant_index=0;reactant_index<NUMBER_OF_REACTANTS;reactant_index++)
                {
                    // Get the species reference -
                    SpeciesReference species_ref = rxn_obj.getReactant(reactant_index);
                    String strReactant = species_ref.getSpecies();
                          
                    if (strReactant.equalsIgnoreCase(strSpecies))
                    {       
                    	double tmp = species_ref.getStoichiometry();
                        if (tmp>=0.0)
                        {
                            dblSTMatrix[scounter][rcounter]=-1.0*tmp;
                        }
                        else
                        {
                            dblSTMatrix[scounter][rcounter]=tmp;
                        }
                    }
                    
                }
                
                // go through the products of this reaction -
                for (int product_index=0;product_index<NUMBER_OF_PRODUCTS;product_index++)
                {
                    // Get the species reference -
                    SpeciesReference species_ref = rxn_obj.getProduct(product_index);
                    String strProduct = species_ref.getSpecies();
                     
                    if (strProduct.equalsIgnoreCase(strSpecies))
                    {
                        dblSTMatrix[scounter][rcounter]=species_ref.getStoichiometry();
                    }
                }
            }
        }
    }
    
    // Build the stoichiometric matrix -
    public static void buildStoichiometricMatrixFloat(float[][] dblSTMatrix,Model model_wrapper,Vector<Reaction> listRates,Vector<Species> listSpecies) throws Exception
    {
        
        // Get the dimension of the system -
        int NUMBER_OF_SPECIES = 0; 
        int NUMBER_OF_RATES = 0;
        
        // Get the system dimension -
        NUMBER_OF_SPECIES = (int)listSpecies.size();
        NUMBER_OF_RATES = (int)listRates.size(); 
        
        System.out.println("Dimension "+NUMBER_OF_SPECIES+" by "+NUMBER_OF_RATES);
        
        // Go through and put everything as zeros by default -
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
            {
                dblSTMatrix[scounter][rcounter]=0;
            }
        }
        
        // ListOf listSpecies = model_wrapper.getListOfSpecies();
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            // Get the species reference -
            Species species = (Species)listSpecies.get(scounter);
            String strSpecies = species.getId();
            
            System.out.println("Processing species  - "+strSpecies);
            
            // Ok, I need to go through the rates and determine if this species is involved -
            for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
            {
                // Get the Reaction object -
                Reaction rxn_obj = (Reaction)listRates.get(rcounter);
                
                // Get the 'radius' of this rate -
                int NUMBER_OF_REACTANTS = (int)rxn_obj.getNumReactants();
                int NUMBER_OF_PRODUCTS = (int)rxn_obj.getNumProducts();
                
                // go through the reactants of this reaction -
                for (int reactant_index=0;reactant_index<NUMBER_OF_REACTANTS;reactant_index++)
                {
                    // Get the species reference -
                    SpeciesReference species_ref = rxn_obj.getReactant(reactant_index);
                    String strReactant = species_ref.getSpecies();
                          
                    if (strReactant.equalsIgnoreCase(strSpecies))
                    {       
                    	float tmp = (float)species_ref.getStoichiometry();
                        if (tmp>=0.0)
                        {
                            dblSTMatrix[scounter][rcounter]=-1*tmp;
                        }
                        else
                        {
                            dblSTMatrix[scounter][rcounter]=tmp;
                        }
                    }
                    
                }
                
                // go through the products of this reaction -
                for (int product_index=0;product_index<NUMBER_OF_PRODUCTS;product_index++)
                {
                    // Get the species reference -
                    SpeciesReference species_ref = rxn_obj.getProduct(product_index);
                    String strProduct = species_ref.getSpecies();
                     
                    if (strProduct.equalsIgnoreCase(strSpecies))
                    {
                        dblSTMatrix[scounter][rcounter]=(float)species_ref.getStoichiometry();
                    }
                }
            }
        }
    }
	
	// check for reversible rates - insert *directly after* the reversible rate  
    public static void convertReversibleRates(Model model_wrapper,Vector<Reaction> vecReactions) throws Exception
    { 	
        // We need to treat the reversible reactions -
        int NUMBER_OF_RATES_INITIAL = (int)model_wrapper.getNumReactions();
        ListOf rate_list_initial = model_wrapper.getListOfReactions();
        for (int rate_counter=0;rate_counter<NUMBER_OF_RATES_INITIAL;rate_counter++)
        {
            // Get Reaction =
            Reaction rxn_local = (Reaction)rate_list_initial.get(rate_counter);
            String strOldName = rxn_local.getName();
            String strOldID = rxn_local.getId();
            
            // Check to see if this is reversible -
            if (rxn_local.getReversible())
            {
                // If the rate is reversible then I need to split -
           
                // Create a new reaction object -
                Reaction rxn_new = model_wrapper.createReaction();
                String strNewName = "_REVERSE_"+strOldName;
                String strNewID = "_REVERSE_"+strOldID;
                rxn_new.setName(strNewName);
                rxn_new.setId(strNewID);
                
                // Add the reactants to the products list and vice-versa -
                int NPRODUCTS_LOCAL = (int)rxn_local.getNumProducts();
                int NREACTANTS_LOCAL = (int)rxn_local.getNumReactants();
                ListOf reactants_list_local = rxn_local.getListOfReactants();
                ListOf products_list_local = rxn_local.getListOfProducts();
                
                for (int index =0;index<NPRODUCTS_LOCAL;index++)
                {
                    rxn_new.addReactant((SpeciesReference)products_list_local.get(index));
                }
                
                for (int index =0;index<NREACTANTS_LOCAL;index++)
                {
                    rxn_new.addProduct((SpeciesReference)reactants_list_local.get(index));
                }
               
                // add the forword reaction -
                vecReactions.addElement(rxn_local);
                
                // add the back reaction -
                vecReactions.addElement(rxn_new);
                
                // Ok, I need to add the reaction to the model_wrapper -
                //model_wrapper.addReaction(rxn_new);
            }
            else
            {
            	vecReactions.add(rxn_local);
            }
        }
    }
}
	
