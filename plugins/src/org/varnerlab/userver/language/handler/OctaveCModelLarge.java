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
 * @author rtasseff
 */
public class OctaveCModelLarge {
    // Class/instance members -
	private int NUMBER_OF_RATES = 0;
	private int NUMBER_OF_SPECIES = 0;
	private Model model_wrapper = null;


    /** Creates a new instance of OctaveCModel */
    public OctaveCModelLarge() {

    	// load the sbml?
        System.out.println("New - "+System.getProperty("java.library.path"));
        System.loadLibrary("sbmlj");
    }

    public void setModel(Model model)
	{
		model_wrapper = model;
	}

    public void buildMassBalanceEquations(StringBuffer buffer) throws Exception {
        CodeGenUtilMethods.buildHardCodeMassBalanceEquations(buffer, model_wrapper);
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
                buffer.append("k_");
                buffer.append(rcounter);
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
        buffer.append("void calculateMassBalances(int,int,ColumnVector&,ColumnVector&);\n");
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
        buffer.append("\tColumnVector kVM(args(2).vector_value());	\t// Rate constant vector;\n");
        buffer.append("\tconst int NRATES = args(3).int_value();\t// Number of rates\n");
        buffer.append("\tconst int NSTATES = args(4).int_value();\t// Number of states\n");
        buffer.append("\tColumnVector rV=ColumnVector(NRATES);\t// Setup the rate vector;\n");
        buffer.append("\tColumnVector dx = ColumnVector(NSTATES);\t// dxdt vector;\n");
        buffer.append("\t//Call the methods to calc the kinetic, massbalances and etc\n");
        buffer.append("\n");
        buffer.append("\t// Calculate the kinetics\n");
        buffer.append("\tcalculateKinetics(kVM,xV,rV);\n");
        buffer.append("\n");
        buffer.append("\t// Calculate the mass balance equations - \n");
        buffer.append("\tcalculateMassBalances(NRATES,NSTATES,rV,dx);\n");
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

        driver.append("function [TSIM,X]=");
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
        driver.append("(x,t,kV,NRATES,NSTATES);\n");
        driver.append("[X]=lsode(f,IC,TSIM);\n");
        driver.append("% Calculate the output - \n");
        driver.append("OUTPUT = X(MEASUREMENT_INDEX_VECTOR,:);\n");
        driver.append("\n");
        driver.append("% return to caller -");
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

 





}
