package org.varnerlab.userver.language.handler;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.varnerlab.server.transport.LoadXMLPropFile;

public class OctaveMModel {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;
	private Model model_wrapper = null;
	
	public void setPropertyTree(LoadXMLPropFile prop)
	{
		_xmlPropTree = prop;
	}
	
	public void setModel(Model model)
	{
		model_wrapper = model;
	}
	
	
	public void buildDriverBuffer(StringBuffer driver,LoadXMLPropFile propTree) throws Exception {
        
        // Put in the header and go -
    	String strFunctionNameRaw = propTree.getProperty("//DriverFile/driver_filename/text()");
    	int INT_2_DOT = strFunctionNameRaw.indexOf(".");
    	String strFunctionName = strFunctionNameRaw.substring(0, INT_2_DOT);
    	
        driver.append("function [TSIM,X]=");
        driver.append(strFunctionName);
        driver.append("(pDataFile,TSTART,TSTOP,Ts,DFIN)\n");
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
        driver.append("\n");
        driver.append("% Call the ODE solver - the default is LSODE\n");
        
        //@todo should use the name the user passed in..
        String strMassBalanceFunctionNameRaw = propTree.getProperty("//MassBalanceFunction/massbalance_filename/text()");
        INT_2_DOT = strMassBalanceFunctionNameRaw.indexOf(".");
        String strMassBalanceFunctionName = strMassBalanceFunctionNameRaw.substring(0, INT_2_DOT);
        
        driver.append("f = @(x,t)");
        driver.append(strMassBalanceFunctionName);
        driver.append("(x,t,S,kV,NRATES,NSTATES);\n");
        driver.append("[X]=lsode(f,IC,TSIM);\n");
        driver.append("return;\n");
    }

	public void buildInputsBuffer(StringBuffer inputs) throws Exception
    {
        // Ok, so the build the input buffer -
		// Setup the kinetics filename -
        String strInputFunctionNameRaw = _xmlPropTree.getProperty("//InputFunction/input_function_filename/text()");
        int INT_2_DOT = strInputFunctionNameRaw.indexOf(".");
        String strInputFunctionName = strInputFunctionNameRaw.substring(0, INT_2_DOT);
		
        // Away we go...
        inputs.append("function uV=");
        inputs.append(strInputFunctionName);
        inputs.append("(t,x,DF);\n");
        inputs.append("% The default is to return a vector of zeros.\n");
        inputs.append("% Override with specific logic.\n");
        inputs.append("nR=length(x);\n");
        inputs.append("uV = zeros(nR,1);\n");
        inputs.append("return;\n");
    }
	
	public void buildMassBalanceBuffer(StringBuffer massbalances) throws Exception
    {
        
		// Get the massbalance name -
		String strMassBalanceFunctionNameRaw = _xmlPropTree.getProperty("//MassBalanceFunction/massbalance_filename/text()");
        int INT_2_DOT = strMassBalanceFunctionNameRaw.indexOf(".");
        String strMassBalanceFunctionName = strMassBalanceFunctionNameRaw.substring(0, INT_2_DOT);
        
        // Put the header -
        massbalances.append("function [DXDT]=");
        massbalances.append(strMassBalanceFunctionName);
        massbalances.append("(x,t,S,kV,NRATES,NSTATES)\n");
        massbalances.append("% This file is machine generated. Please don't change. I know who you are...\n");
        massbalances.append("\n");
        massbalances.append("% Call the kinetics\n");
        
        // Setup the kinetics filename -
        String strKineticesFunctionNameRaw = _xmlPropTree.getProperty("//KineticsFunction/kinetics_filename/text()");
        INT_2_DOT = strKineticesFunctionNameRaw.indexOf(".");
        String strKineticesFunctionName = strKineticesFunctionNameRaw.substring(0, INT_2_DOT);
        
        massbalances.append("[rV]=");
        massbalances.append(strKineticesFunctionName);
        massbalances.append("(t,x,kV);\n");
        massbalances.append("\n");
        massbalances.append("% Calculate the input vector\n");
        
        
        // Setup the kinetics filename -
        String strInputFunctionNameRaw = _xmlPropTree.getProperty("//InputFunction/input_function_filename/text()");
        INT_2_DOT = strInputFunctionNameRaw.indexOf(".");
        String strInputFunctionName = strInputFunctionNameRaw.substring(0, INT_2_DOT);
        
        massbalances.append("[uV]=");
        massbalances.append(strInputFunctionName);
        massbalances.append("(t,x,DF);\n");
        massbalances.append("\n");
        massbalances.append("% Calculate DXDT\n");
        massbalances.append("DXDT = S*rV+uV;\n");
        massbalances.append("\n");
        massbalances.append("% return to caller\n");
        massbalances.append("return;\n");
    }      

	public void buildKineticsBuffer(StringBuffer buffer,Model model_wrapper) throws Exception
    {
		
		// First things first - get the size of the system -
        int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
		
		// Setup the kinetics filename -
        String strKineticesFunctionNameRaw = _xmlPropTree.getProperty("//KineticsFunction/kinetics_filename/text()");
        int INT_2_DOT = strKineticesFunctionNameRaw.indexOf(".");
        String strKineticesFunctionName = strKineticesFunctionNameRaw.substring(0, INT_2_DOT);
		
		
		buffer.append("function [rV]=");
		buffer.append(strKineticesFunctionName);
		buffer.append("(t,x,kV)\n");
        buffer.append("% Machine generated file. Edit on pain of death. You have been warned.\n");
        buffer.append("\n");
        buffer.append("% Put the x's in terms of the symbols -- helps with debugging.\n");
        
        
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
        
        buffer.append("\t% List of the rates -- \n");
        
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
                buffer.append("\trV(");
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
                buffer.append("\trV(");
                buffer.append(rcounter+1);
                buffer.append(",1)\t=\t");
                
                // Get the 'radius' of this rate -
                int NUMBER_OF_REACTANTS = (int)rxn_obj.getNumReactants();
             
                
                // Get the list of reactants and products -
                ListOf reactant_list = rxn_obj.getListOfReactants();
                  
                // Ok, so if this rate is 
                buffer.append("kV(");
                buffer.append(rcounter+1);
                buffer.append(",1)");
                buffer.append("*");
                
                // Get the number of modifiers -
                long NUMBER_OF_MODIFIERS = rxn_obj.getNumModifiers();
                for (long mod_index=0;mod_index<NUMBER_OF_MODIFIERS;mod_index++)
                {
                    // Get the species reference -
                    ModifierSpeciesReference mSpecRef = rxn_obj.getModifier(mod_index);
                    String strTmp = mSpecRef.getSpecies();
                    
                    // put the mod species -
                    buffer.append(strTmp);
                    buffer.append("*");
                }
                
                
                for (int reactant_index=0;reactant_index<NUMBER_OF_REACTANTS;reactant_index++)
                {
                    SpeciesReference srTmp = (SpeciesReference)reactant_list.get(reactant_index);
                    String strTmp = srTmp.getSpecies();
                    
                    // Ok, I need to check to see if there is a stoichiometric coefficient
                    // that is not 1
                    double dblTmp = srTmp.getStoichiometry();
                    //System.out.println("What the f*ck - Species "+strTmp+" has a coeff of "+dblTmp+" in rxn "+reactant_index);
                    
                    if (dblTmp!=1.0)
                    {
                        
                        //System.out.println("Why is "+(-1*dblTmp)+" equal to 1.0");
                        
                        buffer.append("pow(");
                        buffer.append(strTmp);
                        buffer.append(",");
                        buffer.append(-1*dblTmp);
                        buffer.append(")");
                    }
                    else
                    {
                        buffer.append(strTmp); 
                    }
                    
                    if (reactant_index<NUMBER_OF_REACTANTS-1)
                    {
                        buffer.append("*");
                    }
                    else
                    {
                        buffer.append(";");
                    }
                }
                
                buffer.append("\n");
                
            }
        }
        
        buffer.append("\n");
        buffer.append("return;\n");
    }
}
