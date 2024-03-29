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
 *
 * Created on December 29, 2006, 4:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.language.handler;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author jeffreyvarner
 */
public class OctaveCModel {
    // Class/instance members -
	private int NUMBER_OF_RATES = 0;
	private int NUMBER_OF_SPECIES = 0;
	private Model model_wrapper = null;
	
    
    /** Creates a new instance of OctaveCModel */
    public OctaveCModel() {
       
    	// load the sbml?
        System.out.println("New - "+System.getProperty("java.library.path"));
        System.loadLibrary("sbmlj");     
    }
    
    public void setModel(Model model)
	{
		model_wrapper = model;
	}
    
    
    // Ok, some methods that are static so I can call them from anywhere -
    public void buildHardCodeMassBalanceEquations(StringBuffer buffer,Model model_wrapper,Vector<Reaction> vecReactions,Vector<Species> vecSpecies) throws Exception {
    	// Get the dimension of the system -
    	int NUMBER_OF_SPECIES = vecSpecies.size();
    	int NUMBER_OF_RATES = vecReactions.size();
    	int NROWS = NUMBER_OF_SPECIES;
    	int NCOLS = NUMBER_OF_RATES;
    	StringBuffer tmpBuffer = new StringBuffer();
        
        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions,vecSpecies);
    
        // Populate the buffer -
        double stm = 0;
        boolean firstTime = false;

        buffer.append("\n");
        buffer.append("void calculateMassBalances(int NRATES,int NSTATES,Matrix& STMATRIX,ColumnVector& rV,ColumnVector& dx)\n");
        buffer.append("{\n");
        
        buffer.append("\t// Formulate the mass balance equations -- use sparse representation \n");
        
        /*// Changing this line: buffer.append("\tdx=STMATRIX*rV;\n"); - - THIS IMPL MADE ME F*ING CRAZY so I changed it...jv 5-3-20112
		for (int i=0;i<NROWS;i++) // Loop through the species
		{
			for (int j=0;j<NCOLS;j++) // Loop through the reactions
			{
				if (j==0) // If not the first reaction, append a plus sign in between the rates
				{
					buffer.append("\t dx("+String.valueOf(i)+")=");
                    firstTime = true;
                }
                if (matrix[i][j] != 0)
				{
                	if(j!=0 && firstTime != true) // If not the first reaction, append a plus sign in between the rates
                    {
                		buffer.append("+");
                    }
                    
                	// Grab the value from the stoich. matrix
                	stm = matrix[i][j]; 
                    buffer.append(String.valueOf(stm)+"*rV("+String.valueOf(j)+")");
                    firstTime = false;
				}
			}
			if(i!=NROWS) // If not the last species, enter new line
			{
				buffer.append(";\n");
			}
		}*/
        
        // Ok, go through the species and reactions -
        for (int species_index=0;species_index<NROWS;species_index++)
        {
        	// Set the null-species flag -
        	boolean blnNullSpecies = true;
        	
        	
        	// populate the dxdt --
        	buffer.append("\t");
        	buffer.append("dx(");
        	buffer.append(species_index);
        	buffer.append(",0) = ");
        	
        	// Need to check to see of this row has any reactions at all ..
        	for (int rate_index=0;rate_index<NCOLS;rate_index++)
        	{
        		// Ok, get the stoichiometric matrix -
        		double tmpValue = matrix[species_index][rate_index];
        		
        		if (tmpValue!=0.0)
        		{
        			blnNullSpecies = false;
        			break;
        		}
        	}
        	
        	if (!blnNullSpecies)
        	{
        	
	        	// Ok, go through the rates --
	        	for (int rate_index=0;rate_index<NCOLS;rate_index++)
	        	{
	        		// Ok, get the stoichiometric matrix -
	        		double tmpValue = matrix[species_index][rate_index];
	        		
	        		// Check to see, if we have a non-zero
	        		if (tmpValue!=0.0)
	        		{
	        			// Build a term -
	        			tmpBuffer.append(tmpValue);
	        			tmpBuffer.append("*rV(");
	        			tmpBuffer.append(rate_index);
	        			tmpBuffer.append(",0)+");
	        		}	
	        	}
	        	
	        	// Grab the contents of the buffer -
	        	String tmpString = tmpBuffer.toString();
	        	
	        	// Clear out the buffer -
	        	tmpBuffer.delete(0, tmpBuffer.length());
	        	
	        	// Ok, so when I get here, I'll have a trailing +
	        	int INDEX_OF_TRAILING_PLUS = tmpString.lastIndexOf("+");
	        	
	        	// replace the + with a ;
	        	buffer.append(tmpString.substring(0,INDEX_OF_TRAILING_PLUS));
	        	buffer.append(";\n");
        	}
        	else
        	{
        		// Ok, if I get here then I have a species w/no reactions (can arise when dealing w/partitioned systems)
        		buffer.append("0;\n");
        	}
        }
		
		// Close out -
		buffer.append("}\n");
    }
 
    public void buildMassBalanceEquations(StringBuffer buffer) throws Exception {
        // Ok, so we need to build the buffer with the mass balance equations in it -
        buffer.append("\n");
    	buffer.append("void calculateMassBalances(int NRATES,int NSTATES,Matrix& STMATRIX,ColumnVector& rV,ColumnVector& dx)\n");
        buffer.append("{\n");
        buffer.append("\tdx=STMATRIX*rV;\n");
        buffer.append("}\n");
    }
            
    public void buildKineticsBuffer(StringBuffer buffer,Model model_wrapper,Vector<Reaction> vecReactions,Vector<Species> vecSpecies) throws Exception
    {
    	buffer.append("\n");
        buffer.append("void calculateKinetics(ColumnVector& kV,ColumnVector& x,ColumnVector& rV)\n");
        buffer.append("{\n");
        buffer.append("\t// Formulate the kinetics -\n");
        buffer.append("\t// Put the x's in terms of symbols, helps with debugging\n");
        
        //ListOf species_list_tmp = model_wrapper.getListOfSpecies();
        int NUMBER_OF_SPECIES = vecSpecies.size();
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            Species species_tmp = (Species)vecSpecies.get(scounter);
            buffer.append("\tdouble ");
            
            // replace the - w/underscores -
            String strTmpName = species_tmp.getId();
            
            //buffer.append(strTmpName.replaceAll("-","_"));
            buffer.append(strTmpName);
            buffer.append("\t=\t");
            buffer.append("x(");
            buffer.append(scounter);
            buffer.append(",0);\n");
        }
        buffer.append("\n");
        
        buffer.append("\t// List of the parameters -- \n");
        // connect the parameter names to incoming parameter vector from datafile
        ListOf parameter_list_tmp = model_wrapper.getListOfParameters();
        long NUMBER_OF_PARAMETERS = model_wrapper.getNumParameters();
        for (int scounter=0;scounter<NUMBER_OF_PARAMETERS;scounter++)
        {
            Parameter param_tmp = (Parameter)parameter_list_tmp.get(scounter);
            buffer.append("\tdouble ");
            buffer.append(param_tmp.getName());
            buffer.append("\t=\t");
            buffer.append("kV(");
            buffer.append(scounter);
            buffer.append(",0);\n");
        }
                
        buffer.append("\n");
        buffer.append("\t// List of the rates -- \n");
        
        // Ok, so I need to see if the rates have kinietc laws, if so use those. Otherwise
        // use mass action as the default
        // ListOf rate_list = model_wrapper.getListOfReactions();
        int NUMBER_OF_RATES = vecReactions.size();
        for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
        {
            // Get the reaction object 
            Reaction rxn_obj = (Reaction)vecReactions.get(rcounter);
            Parameter parameter_obj = (Parameter)parameter_list_tmp.get(rcounter);
            
            if (rxn_obj.isSetKineticLaw())
            {
                // If I get here then I already have a kinetic law -
            	//System.out.println("What the heck?...");
                
            	KineticLaw law = rxn_obj.getKineticLaw();
                buffer.append("\trV(");
                buffer.append(rcounter);
                buffer.append(",0)\t=\t");
                buffer.append(law.getFormula());
                buffer.append(";");
                buffer.append("\n");
            }
            else
            {
                // Ok, so If I get here then I have no rate, so I need to 
                // formulate the mass action rate -
                buffer.append("\trV(");
                buffer.append(rcounter);
                buffer.append(",0)\t=\t");
                
                // Get the 'radius' of this rate -
                int NUMBER_OF_REACTANTS = (int)rxn_obj.getNumReactants();
                           
                // Get the list of reactants and products -
                ListOf reactant_list = rxn_obj.getListOfReactants();
                  
                // Ok, so if this rate is 
                //buffer.append("k_");
                //buffer.append(rcounter);
                buffer.append(parameter_obj.getName());
                buffer.append("*");
                
                // Get the number of modifiers -
                long NUMBER_OF_MODIFIERS = rxn_obj.getNumModifiers();
                for (long mod_index=0;mod_index<NUMBER_OF_MODIFIERS;mod_index++)
                {
                    // Get the species reference -
                    ModifierSpeciesReference mSpecRef = rxn_obj.getModifier(mod_index);
                    String strTmpRaw = mSpecRef.getSpecies();
                    String strTmp = strTmpRaw.replaceAll("-","_");
                    
                    // put the mod species -
                    buffer.append(strTmp);
                    buffer.append("*");
                }
                
                
                for (int reactant_index=0;reactant_index<NUMBER_OF_REACTANTS;reactant_index++)
                {
                    SpeciesReference srTmp = (SpeciesReference)reactant_list.get(reactant_index);
                    String strTmpRaw = srTmp.getSpecies();
                    String strTmp = strTmpRaw.replaceAll("-", "_");
                    
                    if (!strTmpRaw.isEmpty())
                    {
                    
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
	                        buffer.append(dblTmp);
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
	                        buffer.append(";\n");
	                    }
	                }
                    else
                    {
                    	buffer.append("1;\n");
                    }
	            }
            }
        }
        
        buffer.append("\n");
        buffer.append("}\n");
    }
    
    
    // Calculates using the Jacobian and PMatrix   (B)
    public void buildAdjBalFntBuffer(StringBuffer buffer,XMLPropTree propTree) throws Exception {
        buffer.append("#include <octave/oct.h>\n");
        buffer.append("#include <ov-struct.h>\n");
        buffer.append("#include <iostream>\n");
        buffer.append("#include <math.h>\n");
        buffer.append("\n");
        
   
        
        buffer.append("// Function prototypes - \n");
        buffer.append("void calculateKinetics(ColumnVector&,ColumnVector&,ColumnVector&);\n");
        buffer.append("void calculateInputs();\n");
        buffer.append("void calculateMassBalances(int,int,Matrix&,ColumnVector&,ColumnVector&);\n");
        buffer.append("void calculateDSDT(int, int, ColumnVector&,ColumnVector&, ColumnVector&, ColumnVector&, int);\n");
        buffer.append("void calculateJacobian(int, ColumnVector&, ColumnVector&, Matrix&);\n");
        buffer.append("void calculatePMatrix(int, int, ColumnVector&, ColumnVector&, Matrix&);\n");
        buffer.append("\n");
        
        // Grab the function name -
    	//String strAdjFunctionNameRaw = propTree.getProperty("//SensitivityAnalysis/adjoint_equations_filename/text()");
    	//int INT_2_DOT = strAdjFunctionNameRaw.indexOf(".");
    	//String strAdjFunctionName = strAdjFunctionNameRaw.substring(0, INT_2_DOT);
        ArrayList<String> arrList = propTree.processFilenameBlock("AdjointBalances");
        String strAdjFunctionName = arrList.get(1);
  
        buffer.append("/* ----------------------------------------------------------------------\n");
        buffer.append(" * ");
        buffer.append(strAdjFunctionName);
        buffer.append(".c was generated using the UNIVERSAL code generator system.\n");
        buffer.append(" * Username: ");
        buffer.append(propTree.getProperty(".//Model/@username"));
        buffer.append("\n");
        buffer.append(" * Type: ");
        buffer.append(propTree.getProperty(".//Model/@type"));
        buffer.append("\n");
        buffer.append(" * Version: ");
        buffer.append(propTree.getProperty(".//Model/@version"));
        buffer.append("\n");
        
        buffer.append(" *\n");
        buffer.append(" * \n");
        buffer.append(" * Template designed by rat44@cornell.edu (R to the T) \n");
        buffer.append(" * ---------------------------------------------------------------------- */\n");
        buffer.append("\n");
        
        
        buffer.append("DEFUN_DLD(");
        buffer.append(strAdjFunctionName);
        buffer.append(",args,nargout,\"Calculate the adjoined mass and sensitivity balances.\")\n");
        buffer.append("{\n");
        buffer.append("\n");
        buffer.append("\t//Initialize variables\n");
        buffer.append("\tColumnVector aV(args(0).vector_value());	// Get the adjoined state & sensitivity  vector (index 0);\n");
        buffer.append("\tMatrix STMATRIX(args(2).matrix_value());	// Get the stoichiometric matrix;\n");
        buffer.append("\tColumnVector kV(args(3).vector_value());		// Rate constant vector;\n");
        buffer.append("\tconst int NRATES = args(4).int_value();	// Number of rates\n");
        buffer.append("\tconst int NSTATES = args(5).int_value();	// Number of states\n");
        buffer.append("\tconst int paramIndex = args(6).int_value();	// Current Parameter Index\n");
        buffer.append("\tColumnVector rV=ColumnVector(NRATES);	// Setup the rate vector;\n");
        buffer.append("\tColumnVector dx = ColumnVector(NSTATES);	// dxdt vector;\n");
        buffer.append("\tColumnVector xV = ColumnVector(NSTATES);  // state vector \n");
        buffer.append("\tColumnVector DSDT = ColumnVector(NSTATES); // time derivative of sensitivity for current parameter index\n");
        buffer.append("\tColumnVector SC = ColumnVector(NSTATES); // the sensitivity coefficent vector for current parameter index\n");
        buffer.append("\tColumnVector da = ColumnVector(NSTATES+NSTATES,1); //time derivative of ajoined vector;\n");
        buffer.append("\tint i;\n");
        buffer.append("\tint j;\n");
        buffer.append("\tint q;\n");
        buffer.append("\n");
        buffer.append("\t// get values for xV from the input aV\n");
        buffer.append("\tfor (i=0;i<NSTATES;i++){\n");
        buffer.append("\t\txV(i) = aV(i);\n");
        buffer.append("  \t}\n");
        buffer.append("  \t// get values for SC from the input aV\n");
        buffer.append("\tq = NSTATES;\n");
        buffer.append("\tfor (i=0;i<NSTATES;i++){\n");
        buffer.append("\t\tSC(i) = aV(q);\n");
        buffer.append("\t\tq = q+1;\n");
        buffer.append("\t}\n");
        buffer.append("\n");
        buffer.append("\t//Call the methods to calc the kinetic, massbalances and etc\n");
        buffer.append("\n");
        buffer.append("\t// Calculate the kinetics\n");
        buffer.append("\tcalculateKinetics(kV,xV,rV);\n");
        buffer.append("\n");
        buffer.append("\t// Calculate the input vector -\n");
        buffer.append("\tcalculateInputs();\n");
        buffer.append("\n");
        buffer.append("\t// Calculate the mass balance equations - \n");
        buffer.append("\tcalculateMassBalances(NRATES,NSTATES,STMATRIX,rV,dx);\n");
        buffer.append("\n");
        buffer.append("\t// Calculate the DSDT matrix\n");
        buffer.append("\tcalculateDSDT(NSTATES, NRATES, SC,kV,xV,DSDT, paramIndex);\n");
        buffer.append("\n");
        buffer.append("\t// put the required dx's into the out going da vector\n");
        buffer.append("\tfor (i=0;i<NSTATES;i++){\n");
        buffer.append("\t\tda(i) = dx(i);\n");
        buffer.append("\t}\n");
        buffer.append("\n");
        buffer.append("\t// put the required DSDT values into the out going da vector \n");
        buffer.append("\tq = NSTATES;\n");
        buffer.append("\tfor (i=0;i<NSTATES;i++){\n");
        buffer.append("\t\tda(q) = DSDT(i);\n");
        buffer.append("\t\tq = q+1;\n");
        buffer.append("\t}\n");
        buffer.append("\n");
        buffer.append("\t// return the time derivatives\n");
        buffer.append("\treturn octave_value(da);\n");
        buffer.append("};\n");
        buffer.append("\n");
    }
    
    public void buildMassBalanceBuffer(StringBuffer buffer,XMLPropTree propTree) throws Exception {
        
    	ArrayList<String> arrList = propTree.processFilenameBlock("MassBalanceFunction");
        String strMassBalanceFunctionName = arrList.get(1);
    	
    	buffer.append("/* ----------------------------------------------------------------------\n");
        buffer.append(" * ");
        buffer.append(strMassBalanceFunctionName);
        buffer.append(".c was generated using the UNIVERSAL code generator system.\n");
        buffer.append(" * Username: ");
        buffer.append(propTree.getProperty(".//Model/@username"));
        buffer.append("\n");
        buffer.append(" * Type: ");
        buffer.append(propTree.getProperty(".//Model/@type"));
        buffer.append("\n");
        buffer.append(" * Version: ");
        buffer.append(propTree.getProperty(".//Model/@version"));
        buffer.append("\n");
        
        buffer.append(" *\n");
        buffer.append(" * \n");
        buffer.append(" * Template designed by JDV \n");
        buffer.append(" * ---------------------------------------------------------------------- */\n");
        buffer.append("\n");

    	
    	
    	buffer.append("#include <octave/oct.h>\n");
        buffer.append("#include <ov-struct.h>\n");
        buffer.append("#include <iostream>\n");
        buffer.append("#include <math.h>\n");
        buffer.append("\n");
        buffer.append("// Function prototypes - \n");
        buffer.append("void calculateKinetics(ColumnVector&,ColumnVector&,ColumnVector&);\n");
        buffer.append("void calculateMassBalances(int,int,Matrix&,ColumnVector&,ColumnVector&);\n");
        buffer.append("\n");
        
        
        //String strMassBalanceFunctionNameRaw = propTree.getProperty("//MassBalanceFunction/massbalance_filename/text()");
        //int INT_2_DOT = strMassBalanceFunctionNameRaw.indexOf(".");
        //String strMassBalanceFunctionName = strMassBalanceFunctionNameRaw.substring(0, INT_2_DOT);
        
        
        buffer.append("DEFUN_DLD(");
        buffer.append(strMassBalanceFunctionName);
        buffer.append(",args,nargout,\"Calculate the mass balances.\")\n");
        buffer.append("{\n");
        buffer.append("\t//Initialize variables\n");
        buffer.append("\tColumnVector xV(args(0).vector_value());\t// Get the state vector (index 0);\n");
        buffer.append("\tMatrix STMATRIX(args(2).matrix_value());\t// Get the stoichiometric matrix;\n");
        buffer.append("\tColumnVector kVM(args(3).vector_value());	\t// Rate constant vector;\n");
        buffer.append("\tconst int NRATES = args(4).int_value();\t// Number of rates\n");
        buffer.append("\tconst int NSTATES = args(5).int_value();\t// Number of states\n");
        buffer.append("\tColumnVector rV=ColumnVector(NRATES);\t// Setup the rate vector;\n");
        buffer.append("\tColumnVector dx = ColumnVector(NSTATES);\t// dxdt vector;\n");
        buffer.append("\t//Call the methods to calc the kinetic, massbalances and etc\n");
        buffer.append("\n");
        buffer.append("\t// Calculate the kinetics\n");
        buffer.append("\tcalculateKinetics(kVM,xV,rV);\n");
        buffer.append("\n");
        buffer.append("\t// Calculate the mass balance equations - \n");
        buffer.append("\tcalculateMassBalances(NRATES,NSTATES,STMATRIX,rV,dx);\n");
        buffer.append("\n");
        buffer.append("\t// return the mass balances\n");
        buffer.append("\treturn octave_value(dx);\n");
        buffer.append("};\n");
        buffer.append("\n");
    }

    public void buildDriverBuffer(StringBuffer driver,XMLPropTree propTree) throws Exception {
        
        // Put in the header and go -
    	//String strFunctionNameRaw = propTree.getProperty("//DriverFile/driver_filename/text()");
    	//int INT_2_DOT = strFunctionNameRaw.indexOf(".");
    	//String strFunctionName = strFunctionNameRaw.substring(0, INT_2_DOT);
    	
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
    
    
    public void buildSolveAdjBalBuffer(StringBuffer driver,XMLPropTree propTree) throws Exception {
        
    	// Grab the driver function name -
    	//String strFunctionNameRaw = propTree.getProperty("//SensitivityAnalysis/adjoint_driver_filename/text()");
    	//int INT_2_DOT = strFunctionNameRaw.indexOf(".");
    	//String strFunctionName = strFunctionNameRaw.substring(0, INT_2_DOT);
    	ArrayList<String> arrList = propTree.processFilenameBlock("AdjointDriver");
    	String strFunctionName = arrList.get(1);
    	
    	
    	driver.append("% * ----------------------------------------------------------------------\n");
        driver.append("% * ");
        driver.append(strFunctionName);
        driver.append(".m was generated using the UNIVERSAL code generator system.\n");
        driver.append("% * Username: ");
        driver.append(propTree.getProperty(".//Model/@username"));
        driver.append("\n");
        driver.append("% * Type: ");
        driver.append(propTree.getProperty(".//Model/@type"));
        driver.append("\n");
        driver.append("% * Version: ");
        driver.append(propTree.getProperty(".//Model/@version"));
        driver.append("\n");
        
        driver.append("% *\n");
        driver.append("% * \n");
        driver.append("% * Template designed by R \"my main man\" T \n");
        driver.append("% * ---------------------------------------------------------------------- */\n");
        driver.append("\n");
    	
    	
        // Go -
        driver.append("function [eT]=");
        driver.append(strFunctionName);
        driver.append("(DataFile,TSTART,TSTOP,Ts,DFIN)\n");
        driver.append("\n");
        driver.append("startTime = clock();\n");
        driver.append("\n");
        driver.append("% Check to see if I need to load the datafile\n");
        driver.append("if (~isempty(DFIN))\n");
        driver.append("\tDF = DFIN;\n");
        driver.append("else\n");
        driver.append("\tDF = feval(DataFile,TSTART,TSTOP,Ts,[]);\n");
        driver.append("end;\n");
        driver.append("\n");
        driver.append("% Get reqd stuff from data struct -\n");
        driver.append("CIC = DF.INITIAL_CONDITIONS;\n");
        driver.append("TSIM = TSTART:Ts:TSTOP;\n");
        driver.append("STM = DF.STOICHIOMETRIC_MATRIX;\n");
        driver.append("kV = DF.PARAMETER_VECTOR;\n");
        driver.append("nParam = DF.NUMBER_PARAMETERS;\n");
        driver.append("NSTATES = DF.NUMBER_OF_STATES;\n");
        driver.append("nTime = length(TSIM);\n");
        driver.append("paramIndex = 0;\n");
        driver.append("SIC = zeros(NSTATES,1);\n");
        driver.append("S = [];\n");
        driver.append("% combine S and C inital conditions for IC\n");
        driver.append("IC = [CIC;SIC];\n");
       
        // Grab the function name -
    	//String strAdjFunctionNameRaw = propTree.getProperty("//SensitivityAnalysis/adjoint_equations_filename/text()");
    	//INT_2_DOT = strAdjFunctionNameRaw.indexOf(".");
    	//String strAdjFunctionName = strAdjFunctionNameRaw.substring(0, INT_2_DOT);
        ArrayList<String> arrList2 = propTree.processFilenameBlock("AdjointBalances");
        String strAdjFunctionName = arrList2.get(1);
        
        driver.append("% prep the ODE solver - the default is LSODE\n");
        driver.append("% lsode_options('integration method','adams');\n");
        driver.append("lsode_options('relative tolerance',1E-5);\n");
        driver.append("lsode_options('absolute tolerance',1E-5);\n");
        driver.append("\n");
        driver.append("tmp_time = startTime;\n");
        driver.append("\n");
        driver.append("% set up a loop to go through the parameter indices\n");
        driver.append("for pindex = 1:nParam\n");
        driver.append("\t% Call the ODE solver -\n");
        driver.append("\n");
        driver.append("\tmsg = ['Starting parameter ',num2str(pindex),' of ',num2str(nParam)];\n");
    	driver.append("\tdisp(msg);\n");
    	driver.append("\n");
        driver.append("\t% prep the function to be solved\n");
        driver.append("\tf = @(x,t)");
        driver.append(strAdjFunctionName);
        driver.append("(x,t,STM,kV,nParam,NSTATES,pindex-1);\n");
        driver.append("\t[X]=lsode(f,IC,TSIM);\n");
        driver.append("\n");
        driver.append("\ttmp_time = etime(clock(),tmp_time);\n");
    	driver.append("\tmsg = ['Completed parameter ',num2str(pindex),' of ',num2str(nParam),' in ',num2str(tmp_time),' seconds'];\n");
    	driver.append("\tdisp(msg);\n");
        driver.append("\n");
    	driver.append("\t% Grab the senstivity coeff -\n");
    	driver.append("\tSU = X(:,(NSTATES+1):end);\n");
    	driver.append("\n");
    	driver.append("\t% Store the nominial state -\n");
    	driver.append("\tif (pindex==1)\n");
    	driver.append("\t\tNOMINAL_STATE = X(:,1:NSTATES);\n");
    	driver.append("\t\tcmd = ['save -mat-binary X_NOMINAL.mat NOMINAL_STATE'];\n");
    	driver.append("\t\teval(cmd);\n");
    	driver.append("\tend;\n");
    	driver.append("\n");
    	driver.append("\n");
    	driver.append("\t% Store the un-scaled sensitivity coefficients -\n");
    	driver.append("\tSU = sparse(SU);\n");
    	driver.append("\tcmd = ['save -mat-binary SU_P',num2str(pindex),'.mat SU'];\n");
    	driver.append("\teval(cmd);\n");
    	driver.append("\n");
    	driver.append("\ttmp_time = clock();\n");
        driver.append("end\n");
        driver.append("eT = etime(clock(),startTime);\n");
        driver.append("return;\n");
    }
    
    public void buildInputsBuffer(StringBuffer buffer) throws Exception {
        
        // Ok, so here I need to put the inputs c-code and the mofify state code
        buffer.append("\n");
        buffer.append("void calculateInputs()\n");
        buffer.append("{\n");
        buffer.append("\t// Add inputs code here...\n");
        buffer.append("}\n");
        buffer.append("\n");
        /* we need to think of a better way to do this.
        buffer.append("void modifyState(octave_value IDX_BOUND,octave_value IDX_FREE,ColumnVector xV)\n");
        buffer.append("{\n");
        buffer.append("\tconst int IDX_BP = IDX_BOUND.int_value();\n");
		buffer.append("\tconst int IDX_FP = IDX_FREE.int_value();\n");
		buffer.append("\tdouble VBASIS = xV(IDX_BP)+xV(IDX_FP);\n");
		buffer.append("\n");
		buffer.append("\t// Get the dimension and go -\n");
		buffer.append("\tconst int N = IDX_SURFACE_VECTOR.length();\n");
		buffer.append("\tfor (int counter = 0;counter < N; counter++)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tint INDEX=IDX_SURFACE_VECTOR(counter);\n");
		buffer.append("\t\txV(INDEX)=xV(INDEX)*VBASIS;\n");
		buffer.append("\t}");
        buffer.append("\n");
         */
    }
    
    // uses Jacobian and PMatrix to find DSDT   (B)
    public void buildDSDTBuffer(StringBuffer buffer) throws Exception
    {

        // Convert into string buffer -
        buffer.append("\n");
    	buffer.append("void calculateDSDT(int NSTATES, int NRATES, ColumnVector& SC,ColumnVector& k, ColumnVector& x, ColumnVector& DSDT, int ode_index)\n");
        buffer.append("{\n");
        buffer.append("\t// Machine generated matrix to solve for time\n");
        buffer.append("\t// derivative of the sensitivity matrix.\n");
        buffer.append("\tMatrix J = Matrix(NSTATES,NSTATES); // jacobian matrix df/dx\n");
        buffer.append("\tMatrix P = Matrix(NSTATES,NRATES); // pmatrix df/dp\n");
        buffer.append("\tColumnVector P_ode_index = ColumnVector(NSTATES); \n");
        buffer.append("\tcalculateJacobian(NSTATES, k, x, J);\n");
        buffer.append("\tcalculatePMatrix(NSTATES, NRATES, k, x, P);\n");
    	buffer.append("\n");
        buffer.append("\tint i;\n");
    	buffer.append("\tfor (i=0;i<NSTATES;i++)\n");
		buffer.append("\t{\n");
        buffer.append("\t\tP_ode_index(i) = P(i,ode_index);\n");
    	buffer.append("\t}\n");
    	buffer.append("\n");
        buffer.append("\tDSDT=J*SC+P_ode_index;\n");
        buffer.append("}\n");
    }
    
    public void buildJacobianBuffer(StringBuffer buffer,Vector vecReactions,Vector vecSpecies) throws Exception
    {
        
    	// Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)vecReactions.size();
        
        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions,vecSpecies);

        // Ok, when I get here I have the stoichiometric matrix -
        // Initialize the array -
        String[][] strJacobian = new String[NROWS][NROWS];
        for (int counter_outer=0;counter_outer<NROWS;counter_outer++)
        {
            for (int counter_inner=0;counter_inner<NROWS;counter_inner++)
            {
                strJacobian[counter_outer][counter_inner]="0.0";
            }
        }
   
        //StringBuffer tmpBuffer = new StringBuffer();
        //Vector<String> vecConnect = new Vector<String>();
        //Vector<String> vecSpeciesRate = new Vector<String>();
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // put jacobian logic here -
                strJacobian[state_counter_outer][state_counter_inner]=formulateJacobianElement(matrix,state_counter_outer,state_counter_inner,vecReactions,vecSpecies);
            }
        }
        
        // Ok, so when I get here I have the Jacobian - we need to convert it into a string buffer
        buffer.append("\n");
        buffer.append("void calculateJacobian(int NSTATES, ColumnVector& k, ColumnVector& x, Matrix& JM)\n");
        buffer.append("{\n");
        buffer.append("\t// Machine generated dfdx matrix (Jacobian).\n");
        buffer.append("\n");
        buffer.append("\tint i;\n");
        buffer.append("\tint j;\n");
        buffer.append("\tfor (i=0;i<NSTATES;i++){\n");
        buffer.append("\t\tfor (j=0;j<NSTATES;j++){\n");
        buffer.append("\t\t\tJM(i,j) = 0.0;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t}\n");
        buffer.append("\n");
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // skip this entry if it is zero
                if(!strJacobian[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\tJM(");
                    buffer.append(state_counter_outer);
                    buffer.append(",");
                    buffer.append(state_counter_inner);
                    buffer.append(")=");
                    buffer.append(strJacobian[state_counter_outer][state_counter_inner]);
                    buffer.append(";\n");
                }
            }
        }
        
        buffer.append("}\n");
    }
    
    
    public void buildPMatrixBuffer(StringBuffer buffer,Vector vecReactions,Vector<Species> vecSpecies) throws Exception
    {
    	// Get the dimension of the system -
        int NROWS = (int)vecSpecies.size();
        int NCOLS = (int)vecReactions.size();
        
        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions,vecSpecies);

        // Ok, when I get here I have the stoichiometric matrix -
        // Initialize the pmatrix array -
        String[][] strPMatrix = new String[NROWS][NROWS+NCOLS];
        for (int counter_outer=0;counter_outer<NROWS;counter_outer++)
        {
            for (int counter_inner=0;counter_inner<(NROWS+NCOLS);counter_inner++)
            {
                strPMatrix[counter_outer][counter_inner]="0.0";
            }
        }
        
        // Ok, figure out the PMatrix -
        for (int counter_outer=0;counter_outer<NROWS;counter_outer++)
        {
            for (int counter_inner=0;counter_inner<NCOLS;counter_inner++)
            {
                strPMatrix[counter_outer][counter_inner]=formulatePMatrixElement(matrix,counter_outer,counter_inner,vecReactions,vecSpecies);
            }
        }
        
        // Convert into string buffer -
        buffer.append("\n");
        buffer.append("void calculatePMatrix(int NSTATES, int NRATES, ColumnVector& k, ColumnVector& x, Matrix& PM)\n");
        buffer.append("{\n");
        buffer.append("\t// Machine generated dfdp matrix.\n");
        buffer.append("\n");
        buffer.append("\tint i;\n");
        buffer.append("\tint j;\n");
        buffer.append("\tfor (i=0;i<NSTATES;i++){\n");
        buffer.append("\t\tfor (j=0;j<NRATES;j++){\n");
        buffer.append("\t\t\tPM(i,j) = 0.0;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t}\n");
        
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<(NROWS+NCOLS);state_counter_inner++)
            {
                // if it is a zero entry, just skip it
                if(!strPMatrix[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\tPM(");
                    buffer.append(state_counter_outer);
                    buffer.append(",");
                    buffer.append(state_counter_inner);
                    buffer.append(")=");
                    buffer.append(strPMatrix[state_counter_outer][state_counter_inner]);
                    buffer.append(";\n");
                }
            }
        }
        buffer.append("}\n");
    }
    
    
    private String formulatePMatrixElement(double[][] matrix,int massbalance,int parameter,Vector vecReactions,Vector<Species> vecSpecies)
    {
        StringBuffer buffer = new StringBuffer();
        String rString = "0.0";
        
        // Get the size of the system -
        int NROWS = (int)vecSpecies.size();
        int NCOLS = (int)vecReactions.size();

        double dblStmElement = matrix[massbalance][parameter];
        if (dblStmElement!=0.0)
        {
            // If I get here then I have a non-zero element in the st matrix for this rate -
            
            // find out the states in this rate -
            // Ok, so when I get here, I have to formulate the entry in the  p-matrix -
            buffer.append(dblStmElement);
            buffer.append("*");
            for (int state_counter=0;state_counter<NROWS;state_counter++)   
            {
                // Ok, figure out the non-zero elements - this will handle normal rates -
                double tempStmElement;
                if ((tempStmElement = matrix[state_counter][parameter])<0.0)
                {
                    tempStmElement = Math.abs(tempStmElement);
                    // check to see if exponent is 0
                    if(tempStmElement>-1E-6&&tempStmElement<1E-6){
                        // do nothing
                    }
                    // check to see if exponent is 1
                    else if(tempStmElement>(1-1E-6)&&tempStmElement<(1+1E-6)){
                        // no need to raise to a power
                        buffer.append("x(");
                        buffer.append(state_counter);
                        buffer.append(")*");
                    }
                    // any thing else I need to raise to a power
                    else {
                        buffer.append("pow(x(");
                        buffer.append(state_counter);
                        buffer.append("),");
                        buffer.append(tempStmElement);
                        buffer.append(")*");
                    }
                }
            }
            
            // When I get here I need to cuttof the trailing * 
            rString = buffer.toString();
            if (rString.lastIndexOf("*")==rString.length()-1)
            {
                rString=rString.substring(0,rString.length()-1);
            }
        }
        
        // return -
        return(rString);
    }
    
    
    // This logic will need to be overriden -
    private String formulateJacobianElement(double[][] matrix,int massbalance,int state,Vector vecReactions,Vector<Species> vecSpecies)
    {
        StringBuffer buffer = new StringBuffer();
        String rString = "";
                
        // Get the dimension of the system -
        int NROWS = (int)vecSpecies.size();
        int NCOLS = (int)vecReactions.size();

        Vector<String> vecRates = new Vector<String>(); 
        for (int counter=0;counter<NCOLS;counter++)
        {
            if (matrix[massbalance][counter]!=0.0)
            {
                vecRates.addElement(String.valueOf(counter));
            }
        }
        
        int NRATES = vecRates.size();
        boolean lFlag = false;
        for (int rate_index=0;rate_index<NRATES;rate_index++)
        {
            int test_index = Integer.parseInt(vecRates.get(rate_index));
            
            double tempStmElement;
            if ((tempStmElement = matrix[state][test_index])<0.0)
            {
                // add the plus if this is not the first time
                if(lFlag){
                    buffer.append("+");
                }
                else{
                    lFlag = true;
                }
                // I'm on a rate that has my state in it -  
                
                // check to see if exponent is 0
                tempStmElement = Math.abs(tempStmElement);
                if((tempStmElement-1)>-1E-6&&(tempStmElement-1)<1E-6){
                    buffer.append(matrix[massbalance][test_index]);
                    buffer.append("*");
                    buffer.append(tempStmElement);
                    buffer.append("*");
                    buffer.append("k(");
                    buffer.append(test_index);
                    buffer.append(")");
                }
                
                // check to see if exponent is 1
                else if((tempStmElement-1)>(1-1E-6)&&(tempStmElement-1)<(1+1E-6)){
                    // no need to raise to a power
                    buffer.append(matrix[massbalance][test_index]);
                    buffer.append("*");
                    buffer.append(tempStmElement);
                    buffer.append("*");
                    buffer.append("k(");
                    buffer.append(test_index);
                    buffer.append(")*x(");
                    buffer.append(state);
                    buffer.append(")");
                }
                
                // any thing else I need to raise to a power
                else {
                    buffer.append(matrix[massbalance][test_index]);
                    buffer.append("*");
                    buffer.append(tempStmElement);
                    buffer.append("*");
                    buffer.append("k(");
                    buffer.append(test_index);
                    buffer.append(")*pow(x(");
                    buffer.append(state);
                    buffer.append("),");
                    buffer.append(tempStmElement-1);
                    buffer.append(")");
                }
                
                
                /*
                if (lFlag)
                {
                    buffer.append("+");
                    lFlag = false;
                }*/

                // I need to check to see if there are more species here -
                for (int species_index=0;species_index<NROWS;species_index++)
                {
                    double tempStmElement2;
                    if ((tempStmElement2 = matrix[species_index][test_index])<0.0 && species_index!=state)
                    {
                        tempStmElement2 = Math.abs(tempStmElement2);
                        // check to see if exponent is 0
                        if((tempStmElement2)>-1E-6&&(tempStmElement2)<1E-6){
                            // do nothing
                        }
                        // check to see if exponent is 1
                        else if((tempStmElement2)>(1-1E-6)&&(tempStmElement2)<(1+1E-6)){
                            // no need to raise to a power
                            buffer.append("*x(");
                            buffer.append(species_index);
                            buffer.append(")");
                        }
                        // any thing else I need to raise to a power
                        else {
                            buffer.append("*pow(x(");
                            buffer.append(species_index);
                            buffer.append("),");
                            buffer.append(tempStmElement2);
                            buffer.append(")");
                        }
                        
                        
                    }
                }
                
                
            }
        }
        
       
        // do a quick check here -
        if (buffer.length()==0)
        {
            buffer.append("0.0");
        }
        
        rString = buffer.toString();
        if (rString.lastIndexOf("+")==rString.length()-1)
        {
            rString=rString.substring(0,rString.length()-1);
        }
        
        
        
        // return the buffer -
        return(rString);
    }
    
}
