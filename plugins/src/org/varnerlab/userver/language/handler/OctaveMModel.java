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


import java.util.ArrayList;
import java.util.Vector;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

public class OctaveMModel {
	// Class/instance attributes -
	private XMLPropTree _xmlPropTree = null;
	private Model model_wrapper = null;
	
	public void setPropertyTree(XMLPropTree prop)
	{
		_xmlPropTree = prop;
	}
	
	public void setModel(Model model)
	{
		model_wrapper = model;
	}
	
	
	public void buildDriverBuffer(StringBuffer driver,XMLPropTree propTree) throws Exception {
        
        // Put in the header and go -
    	//String strFunctionNameRaw = propTree.getProperty("//DriverFile/driver_filename/text()");
    	//int INT_2_DOT = strFunctionNameRaw.indexOf(".");
    	///String strFunctionName = strFunctionNameRaw.substring(0, INT_2_DOT);
    	
		ArrayList<String> arrList = propTree.processFilenameBlock("DriverFile");
    	String strFunctionName = arrList.get(1);
    	
        driver.append("function [TSIM,X,OUTPUT]=");
        driver.append(strFunctionName);
        driver.append("(pDataFile,TSTART,TSTOP,Ts,DFIN)\n");
        driver.append("\n");
        
        driver.append("% ----------------------------------------------------------------------\n");
        driver.append("% ");
        driver.append(strFunctionName);
        driver.append(".m was generated using the UNIVERSAL code generator system.\n");
        driver.append("% Username: ");
        driver.append(propTree.getProperty(".//Model/@username"));
        driver.append("\n");
        driver.append("% Type: ");
        driver.append(propTree.getProperty(".//Model/@type"));
        driver.append("\n");
        driver.append("% Version: ");
        driver.append(propTree.getProperty(".//Model/@version"));
        driver.append("\n");
        driver.append("% \n");
        driver.append("% Arguments: \n");
        driver.append("% pDataFile  - pointer to datafile \n");
        driver.append("% TSTART  - Time start \n");
        driver.append("% TSTOP  - Time stop \n");
        driver.append("% Ts - Time step \n");
        driver.append("% DFIN  - Custom data file instance \n");
        driver.append("% TSIM - Simulation time vector \n");
        driver.append("% X - Simulation state array (NTIME x NSPECIES) \n");
        driver.append("% ----------------------------------------------------------------------\n");
        driver.append("\n");
        
        driver.append("% Check to see if I need to load the datafile\n");
        driver.append("if (~isempty(DFIN))\n");
        driver.append("\tDF = DFIN;\n");
        driver.append("else\n");
        driver.append("\tDF = feval(pDataFile,TSTART,TSTOP,Ts,[]);\n");
        driver.append("end;\n");
        driver.append("\n");
        driver.append("% Get reqd stuff from data struct -\n");
        driver.append("IC = DF.INITIAL_CONDITIONS;\n");
        driver.append("TSIM = TSTART:Ts:TSTOP;\n");
        driver.append("S = DF.STOICHIOMETRIC_MATRIX;\n");
        driver.append("kV = DF.PARAMETER_VECTOR;\n");
        driver.append("NRATES = DF.NUMBER_PARAMETERS;\n");
        driver.append("NSTATES = DF.NUMBER_OF_STATES;\n"); 
        driver.append("MEASUREMENT_INDEX_VECTOR = DF.MEASUREMENT_SELECTION_VECTOR;\n");
        driver.append("\n");
        driver.append("% Call the ODE solver - the default is LSODE\n");
        
        //@todo should use the name the user passed in..
        //String strMassBalanceFunctionNameRaw = propTree.getProperty("//MassBalanceFunction/massbalance_filename/text()");
        //INT_2_DOT = strMassBalanceFunctionNameRaw.indexOf(".");
        //String strMassBalanceFunctionName = strMassBalanceFunctionNameRaw.substring(0, INT_2_DOT);
        
        ArrayList<String> arrList2 = propTree.processFilenameBlock("MassBalanceFunction");
        String strMassBalanceFunctionName = arrList2.get(1);
        
        driver.append("f = @(x,t)");
        driver.append(strMassBalanceFunctionName);
        driver.append("(x,t,S,kV,NRATES,NSTATES);\n");
        driver.append("[X]=lsode(f,IC,TSIM);\n");
        driver.append("\n");
        driver.append("% Calculate the output - \n");
        driver.append("OUTPUT = X(:,MEASUREMENT_INDEX_VECTOR);\n");
        driver.append("\n");
        driver.append("% return to caller - \n");
        driver.append("return;\n");
    }


  public void buildSolveAdjBalBuffer(StringBuffer driver,XMLPropTree propTree) throws Exception 
  {

        // Put in the header and go -
    	//String strFunctionNameRaw = propTree.getProperty("//DriverFile/driver_filename/text()");
    	//int INT_2_DOT = strFunctionNameRaw.indexOf(".");
    	///String strFunctionName = strFunctionNameRaw.substring(0, INT_2_DOT);

	  	ArrayList<String> arrList = propTree.processFilenameBlock("AdjointDriver");
    	String strFunctionName = arrList.get(1);

        driver.append("function [TSIM,X,S]=");
        driver.append(strFunctionName);
        driver.append("(pDataFile,TSTART,TSTOP,Ts,pIndex,DFIN)\n");
        driver.append("\n");

        driver.append("% ----------------------------------------------------------------------\n");
        driver.append("% ");
        driver.append(strFunctionName);
        driver.append(".m was generated using the UNIVERSAL code generator system.\n");
        driver.append("% Username: ");
        driver.append(propTree.getProperty(".//Model/@username"));
        driver.append("\n");
        driver.append("% Type: ");
        driver.append(propTree.getProperty(".//Model/@type"));
        driver.append("\n");
        driver.append("% Version: ");
        driver.append(propTree.getProperty(".//Model/@version"));
        driver.append("\n");
        driver.append("% \n");
        driver.append("% Arguments: \n");
        driver.append("% pDataFile  - pointer to datafile \n");
        driver.append("% TSTART  - Time start \n");
        driver.append("% TSTOP  - Time stop \n");
        driver.append("% Ts - Time step \n");
        driver.append("% DFIN  - Custom data file instance \n");
        driver.append("% TSIM - Simulation time vector \n");
        driver.append("% X - Simulation state array (NTIME x NSPECIES) \n");
        driver.append("% S - Simulation sensitivity array for parameter pIndex(NTIME X NSPECIES) \n");
        driver.append("% ----------------------------------------------------------------------\n");
        driver.append("\n");

        driver.append("% Check to see if I need to load the datafile\n");
        driver.append("if (~isempty(DFIN))\n");
        driver.append("\tDF = DFIN;\n");
        driver.append("else\n");
        driver.append("\tDF = feval(pDataFile,TSTART,TSTOP,Ts,[]);\n");
        driver.append("end;\n");
        driver.append("\n");
        driver.append("% Get reqd stuff from data struct -\n");
        driver.append("IC = DF.INITIAL_CONDITIONS;\n");
        driver.append("TSIM = TSTART:Ts:TSTOP;\n");
        driver.append("STM = DF.STOICHIOMETRIC_MATRIX;\n");
        driver.append("kV = DF.PARAMETER_VECTOR;\n");
        driver.append("NRATES = DF.NUMBER_PARAMETERS;\n");
        driver.append("NSTATES = DF.NUMBER_OF_STATES;\n");
        driver.append("\n");
        driver.append("% Append the initial sensitivity values - assuming zero here\n");
        driver.append("IC = [IC; zeros(NSTATES,1);\n");
        driver.append("\n");
        driver.append("% Call the ODE solver - the default is LSODE\n");

        //@todo should use the name the user passed in..
        //String strMassBalanceFunctionNameRaw = propTree.getProperty("//MassBalanceFunction/massbalance_filename/text()");
        //INT_2_DOT = strMassBalanceFunctionNameRaw.indexOf(".");
        //String strMassBalanceFunctionName = strMassBalanceFunctionNameRaw.substring(0, INT_2_DOT);

        ArrayList<String> arrList2 = propTree.processFilenameBlock("AdjointBalances");
        String strAdjointBalancesFunctionName = arrList2.get(1);

        driver.append("f = @(x,t)");
        driver.append(strAdjointBalancesFunctionName);
        driver.append("(x,t,STM,kV,NRATES,NSTATES,pIndex);\n");
        driver.append("[xOut]=lsode(f,IC,TSIM);\n");
        driver.append("\n");
        driver.append("% Seperate xOut into state and sensitivity matrix\n");
        driver.append("X = xOut(:,1:NSTATES);\n");
        driver.append("S = xOut(:,NSTATES+1:end);\n");
        driver.append("return;\n");
    }

	public void buildInputsBuffer(StringBuffer inputs,XMLPropTree propTree) throws Exception
    {
        // Ok, so the build the input buffer -
		// Setup the kinetics filename -
        //String strInputFunctionNameRaw = _xmlPropTree.getProperty("//InputFunction/input_function_filename/text()");
        //int INT_2_DOT = strInputFunctionNameRaw.indexOf(".");
        //String strInputFunctionName = strInputFunctionNameRaw.substring(0, INT_2_DOT);
		
        ArrayList<String> arrList = propTree.processFilenameBlock("InputFunction");
    	String strInputFunctionName = arrList.get(1);
        
        // Away we go...
        inputs.append("function uV=");
        inputs.append(strInputFunctionName);
        inputs.append("(t,x,kV);\n");
        
        // header information -
        inputs.append("% ----------------------------------------------------------------------\n");
        inputs.append("% ");
        inputs.append(strInputFunctionName);
        inputs.append(".m was generated using the UNIVERSAL code generator system.\n");
        inputs.append("% Username: ");
        inputs.append(propTree.getProperty(".//Model/@username"));
        inputs.append("\n");
        inputs.append("% Type: ");
        inputs.append(propTree.getProperty(".//Model/@type"));
        inputs.append("\n");
        inputs.append("% Version: ");
        inputs.append(propTree.getProperty(".//Model/@version"));
        inputs.append("\n");
        inputs.append("% \n");
        inputs.append("% Arguments: \n");
        inputs.append("% t	-	current time\n");
        inputs.append("% x	-	state vector (M x 1) at the current time point\n");
        inputs.append("% kV	-	Parameter vector \n");
        inputs.append("% uV -	M x 1 inputs vector\n");
        inputs.append("\n");
        inputs.append("% The default is to return a vector of zeros.\n");
        inputs.append("% Override with specific logic.\n");
        inputs.append("% ----------------------------------------------------------------------\n");
        inputs.append("\n");
        inputs.append("nR=length(x);\n");
        inputs.append("uV = zeros(nR,1);\n");
        inputs.append("return;\n");
    }
	
	public void buildLargeScaleMassBalanceBuffer(StringBuffer massbalances,Model model_wrapper,Vector<Reaction> vecReactions,Vector<Species> vecSpecies,XMLPropTree propTree) throws Exception
	{
		// Populate the large scale mass balances -
		MModelUtilities.buildLargeScaleOctaveMassBalanceBuffer(massbalances,model_wrapper,vecReactions,vecSpecies,propTree);
	}
	
	public void buildMassBalanceBuffer(StringBuffer massbalances,XMLPropTree propTree) throws Exception
    {
        
		// Get the massbalance name -
		//String strMassBalanceFunctionNameRaw = _xmlPropTree.getProperty("//MassBalanceFunction/massbalance_filename/text()");
        //int INT_2_DOT = strMassBalanceFunctionNameRaw.indexOf(".");
        //String strMassBalanceFunctionName = strMassBalanceFunctionNameRaw.substring(0, INT_2_DOT);
        
		ArrayList<String> arrList2 = propTree.processFilenameBlock("MassBalanceFunction");
        String strMassBalanceFunctionName = arrList2.get(1);
		
        // Put the header -
        massbalances.append("function [DXDT]=");
        massbalances.append(strMassBalanceFunctionName);
        massbalances.append("(x,t,S,kV,NRATES,NSTATES)\n");
        
        massbalances.append("% ----------------------------------------------------------------------\n");
        massbalances.append("% ");
        massbalances.append(strMassBalanceFunctionName);
        massbalances.append(".m was generated using the UNIVERSAL code generator system.\n");
        massbalances.append("% Username: ");
        massbalances.append(propTree.getProperty(".//Model/@username"));
        massbalances.append("\n");
        massbalances.append("% Type: ");
        massbalances.append(propTree.getProperty(".//Model/@type"));
        massbalances.append("\n");
        massbalances.append("% Version: ");
        massbalances.append(propTree.getProperty(".//Model/@version"));
        massbalances.append("\n");
        massbalances.append("% \n");
        massbalances.append("% Arguments: \n");
        massbalances.append("% t  - current time \n");
        massbalances.append("% x  - state vector \n");
        massbalances.append("% S  - stoichiometric matrix \n");
        massbalances.append("% kV - parameter vector \n");
        massbalances.append("% NRATES - Number of rates \n");
        massbalances.append("% NSTATES - Number of states \n");
        massbalances.append("% DXDT - right hand side vector \n");
        massbalances.append("% ----------------------------------------------------------------------\n");
        massbalances.append("\n");
        massbalances.append("% Call the kinetics function \n");
        
        // Setup the kinetics filename -
        //String strKineticesFunctionNameRaw = _xmlPropTree.getProperty("//KineticsFunction/kinetics_filename/text()");
        //INT_2_DOT = strKineticesFunctionNameRaw.indexOf(".");
        //String strKineticesFunctionName = strKineticesFunctionNameRaw.substring(0, INT_2_DOT);
        
        ArrayList<String> arrList = propTree.processFilenameBlock("KineticsFunction");
        String strKineticesFunctionName = arrList.get(1);
        
        massbalances.append("[rV]=");
        massbalances.append(strKineticesFunctionName);
        massbalances.append("(t,x,kV);\n");
        massbalances.append("\n");
        massbalances.append("% Calculate the input vector\n");
        
        
        // Setup the kinetics filename -
        //String strInputFunctionNameRaw = _xmlPropTree.getProperty("//InputFunction/input_function_filename/text()");
        //INT_2_DOT = strInputFunctionNameRaw.indexOf(".");
        //String strInputFunctionName = strInputFunctionNameRaw.substring(0, INT_2_DOT);
        ArrayList<String> arrList3 = propTree.processFilenameBlock("InputFunction");
        String strInputFunctionName = arrList3.get(1);
        
        massbalances.append("[uV]=");
        massbalances.append(strInputFunctionName);
        massbalances.append("(t,x);\n");
        massbalances.append("\n");
        massbalances.append("% Calculate DXDT\n");
        massbalances.append("DXDT = S*rV+uV;\n");
        massbalances.append("\n");
        massbalances.append("% return to caller\n");
        massbalances.append("return;\n");
    }      

	public void buildKineticsBuffer(StringBuffer buffer,Model model_wrapper,XMLPropTree propTree) throws Exception
    {
		
		// First things first - get the size of the system -
        int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
		
		// Setup the kinetics filename -
        //String strKineticesFunctionNameRaw = _xmlPropTree.getProperty("//KineticsFunction/kinetics_filename/text()");
        //int INT_2_DOT = strKineticesFunctionNameRaw.indexOf(".");
        //String strKineticesFunctionName = strKineticesFunctionNameRaw.substring(0, INT_2_DOT);
        
        // Gets the kinetics function -
        ArrayList<String> arrList = propTree.processFilenameBlock("KineticsFunction");
        String strKineticesFunctionName = arrList.get(1);
		
		buffer.append("function [rV]=");
		buffer.append(strKineticesFunctionName);
		buffer.append("(t,x,kV)\n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("% ");
        buffer.append(strKineticesFunctionName);
        buffer.append(".m was generated using the UNIVERSAL code generator system.\n");
        buffer.append("% Username: ");
        buffer.append(propTree.getProperty(".//Model/@username"));
        buffer.append("\n");
        buffer.append("% Type: ");
		buffer.append(propTree.getProperty(".//Model/@type"));
        buffer.append("\n");
        buffer.append("% Version: ");
        buffer.append(propTree.getProperty(".//Model/@version"));
        buffer.append("\n");
        buffer.append("% \n");
        buffer.append("% Arguments: \n");
        buffer.append("% t  - current time \n");
        buffer.append("% x  - state vector \n");
        buffer.append("% kV - parameter vector \n");
        buffer.append("% rV - rate vector \n");
        buffer.append("% ---------------------------------------------------------------------\n");
		
        buffer.append("\n");
        buffer.append("% ---------------------------------------------- \n");
        buffer.append("% Convert x's to symbols -- helps with debugging.\n");
        buffer.append("% ---------------------------------------------- \n");
        
        ListOf species_list_tmp = model_wrapper.getListOfSpecies();
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            Species species_tmp = (Species)species_list_tmp.get(scounter);
            buffer.append(species_tmp.getName());
            buffer.append("\t = \t");
            buffer.append("x(");
            buffer.append(scounter+1);
            buffer.append(",1);\n");
        }
        buffer.append("\n");
        buffer.append("% --------------------------------------------- \n");
        buffer.append("% Alias the parameters -- helps with debugging.\n");
        buffer.append("% --------------------------------------------- \n");
        // connect the parameter names to incoming parameter vector from datafile
        ListOf parameter_list_tmp = model_wrapper.getListOfParameters();
        long NUMBER_OF_PARAMETERS = model_wrapper.getNumParameters();
        for (int scounter=0;scounter<NUMBER_OF_PARAMETERS;scounter++)
        {
            Parameter param_tmp = (Parameter)parameter_list_tmp.get(scounter);
            buffer.append(param_tmp.getName());
            buffer.append("\t = \t");
            buffer.append("kV(");
            buffer.append(scounter+1);
            buffer.append(",1);\n");
        }
                
        buffer.append("\n");
        
        buffer.append("% ----------------------------------- \n");
        buffer.append("% List of the rates -- \n");
        buffer.append("% ----------------------------------- \n");
        // Ok, so I need to see if the rates have kinietc laws, if so use those. Otherwise
        // use mass action as the default
        ListOf rate_list = model_wrapper.getListOfReactions();
        for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
        {
            // Get the reaction object 
            Reaction rxn_obj = (Reaction)rate_list.get(rcounter);
            
            if (rxn_obj.isSetKineticLaw())
            {
                // If I get here then I already have a kinetic law -
                KineticLaw law = rxn_obj.getKineticLaw();
                buffer.append("rV(");
                buffer.append(rcounter+1);
                buffer.append(",1)\t=\t");
                buffer.append(law.getFormula());
                buffer.append(";");
                buffer.append("\n");
            }
            else
            {
                // Ok, so If I get here then I have no rate, so I need to 
                // formulate the mass action rate -
                buffer.append("rV(");
                buffer.append(rcounter+1);
                buffer.append(",1)\t=\t");
                         
                // Get the list of reactants and products -
                ListOf reactant_list = rxn_obj.getListOfReactants();
                  
                // Ok, so if this rate is 
                buffer.append("k_");
                buffer.append(rcounter);
                buffer.append("*");
          
                // Get the number of reactants -
                int NUMBER_OF_REACTANTS = (int)rxn_obj.getNumReactants();
                
                // Ok, process the reactants -
                for (int species_index=0;species_index<NUMBER_OF_REACTANTS;species_index++)
                {
                	// Ok, get the species -
                	SpeciesReference srTmp = (SpeciesReference)reactant_list.get(species_index);
                	String strTmpRaw = srTmp.getSpecies();
                    String strTmp = strTmpRaw.replaceAll("-", "_");
                      
                    // Check to see if we have [];
                    if (strTmp.isEmpty())
                    {
                    	// If we get here, then I have a [] - put 1
                    	buffer.append("1.0");
                    }
                    else
                    {
                    	      	
                    	// Ok, so we need to put in the st coeff -
                    	double dblTmp = srTmp.getStoichiometry();
                    	
                    	// handle the non-one coefficients --
                    	if (dblTmp!=1.0)
	                    {
	                        // Put the pow statement to capture the st coeff -
	                        buffer.append("pow(");
	                        buffer.append(strTmp);
	                        buffer.append(",");
	                        buffer.append(dblTmp);
	                        buffer.append(")");
	                    }
	                    else
	                    {
	                        buffer.append(strTmp); 
	                    }
                    }
                    
                    // Ok, we need to check to make sure that we either continue or add a ;
                    if (species_index<NUMBER_OF_REACTANTS-1)
                    {
                    	buffer.append("*");
                    }
                    else
                    {
                    	buffer.append("\t ; % \t ");
                    	buffer.append(rcounter+1);
                    	buffer.append("\t ");
                    	buffer.append(rxn_obj.getName());
                    	buffer.append("\n");
                    }
                }       
            }
        }
        
        // add the return -
        buffer.append("\n");
        buffer.append("return;\n");
    }

    public void buildAdjBalFntBuffer(StringBuffer buffer,Vector vecReactions,Vector<Species> vecSpecies,XMLPropTree propTree) throws Exception 
    {
    	// Call the utility class and populate the buffer -
    	MModelUtilities.buildAdjBalFntBuffer(buffer,vecReactions, vecSpecies, model_wrapper, propTree);
    }

    public void buildJacobianBuffer(StringBuffer buffer,Vector vecReactions,Vector vecSpecies,XMLPropTree propTree) throws Exception
    {
    	// Call the utility class and populate the buffer -
    	MModelUtilities.buildJacobianBuffer(buffer, vecReactions, vecSpecies, model_wrapper, propTree);
    }
    
    public void buildPMatrixBuffer(StringBuffer buffer,Vector vecReactions,Vector vecSpecies,XMLPropTree propTree) throws Exception
    {
    	// Call the utility class and populate the buffer -
    	MModelUtilities.buildPMatrixBuffer(buffer, vecReactions, vecSpecies, model_wrapper, propTree);
    }
}