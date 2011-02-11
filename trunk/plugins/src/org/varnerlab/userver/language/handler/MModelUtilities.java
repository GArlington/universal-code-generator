/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plugins.src.org.varnerlab.userver.language.handler;
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
 * @author rat44
 */
public class MModelUtilities {


public static void buildJacobianBuffer(StringBuffer buffer,Vector vecReactions,Vector vecSpecies, Model model_wrapper,XMLPropTree propTree) throws Exception
    {
        ArrayList<String> arrList = propTree.processFilenameBlock("JacobianFunction");
        String strJacobianFunctionName = arrList.get(1);
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
        buffer.append("function JM = ");
        buffer.append(strJacobianFunctionName);
        buffer.append("(x,kV)\n");
        buffer.append("\n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("% ");
        buffer.append(strJacobianFunctionName);
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
        buffer.append("% x  - state vector \n");
        buffer.append("% kV - parameter vector \n");
        buffer.append("% JM - jacpbian Matrix \n");
        buffer.append("% ---------------------------------------------------------------------\n");

        buffer.append("\n");
        buffer.append("n = length(x);\n");
        buffer.append("JM = zeros(n,n);\n");
        buffer.append("\n");
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // skip this entry if it is zero
                if(!strJacobian[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("JM(");
                    buffer.append(state_counter_outer+1);
                    buffer.append(",");
                    buffer.append(state_counter_inner+1);
                    buffer.append(")\t=\t");
                    buffer.append(strJacobian[state_counter_outer][state_counter_inner]);
                    buffer.append(";\n");
                }
            }
        }

        buffer.append("return;\n");
    }


private static String formulateJacobianElement(double[][] matrix,int massbalance,int state,Vector vecReactions,Vector<Species> vecSpecies)
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
                    buffer.append(test_index+1);
                    buffer.append(")");
                }

                // check to see if exponent is 1
                else if((tempStmElement-1)>(1-1E-6)&&(tempStmElement-1)<(1+1E-6)){
                    // no need to raise to a power
                    buffer.append(matrix[massbalance][test_index]);
                    buffer.append("*");
                    buffer.append(tempStmElement);
                    buffer.append("*");
                    buffer.append("kV(");
                    buffer.append(test_index+1);
                    buffer.append(")*x(");
                    buffer.append(state+1);
                    buffer.append(")");
                }

                // any thing else I need to raise to a power
                else {
                    buffer.append(matrix[massbalance][test_index]);
                    buffer.append("*");
                    buffer.append(tempStmElement);
                    buffer.append("*");
                    buffer.append("kV(");
                    buffer.append(test_index);
                    buffer.append(")*(x(");
                    buffer.append(state);
                    buffer.append(")^");
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
                            buffer.append(species_index+1);
                            buffer.append(")");
                        }
                        // any thing else I need to raise to a power
                        else {
                            buffer.append("*(x(");
                            buffer.append(species_index+1);
                            buffer.append(")^");
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








public static void buildAdjBalFntBuffer(StringBuffer buffer,Vector vecReactions,Vector<Species> vecSpecies, Model model_wrapper,XMLPropTree propTree) throws Exception
    {
        ArrayList<String> arrList = propTree.processFilenameBlock("AdjointBalances");
        String strSensitivityBalanceFunctionName = arrList.get(1);

        arrList = propTree.processFilenameBlock("JacobianFunction");
        String strJacobianFunctionName = arrList.get(1);

        ArrayList<String> arrList2 = propTree.processFilenameBlock("MassBalanceFunction");
        String strMassBalanceFunctionName = arrList2.get(1);

        // Convert into string buffer -
        // Ok, so when I get here I have the Jacobian - we need to convert it into a string buffer
        buffer.append("function DXDT = ");
        buffer.append(strSensitivityBalanceFunctionName);
        buffer.append("(x,t,STM,kV,NRATES,NSTATES,pIndex)\n");
        buffer.append("\n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("% ");
        buffer.append(strSensitivityBalanceFunctionName);
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
        buffer.append("% x  - adjoint state vector and sensitivity for parameter pIndex vector \n");
        buffer.append("% kV - parameter vector \n");
        buffer.append("% DSDT - vector of delta sensitivity for parameter pIndex  wrt time \n");
        buffer.append("% ---------------------------------------------------------------------\n");
    	buffer.append("\n");
        buffer.append("% Decompose x into the concentration vector c and the sensitivity vector s \n");
        buffer.append("n = NSTATES;\n");
        buffer.append("m = NRATES;\n");
        buffer.append("c = x(1:n);");
        buffer.append("s = x(n+1:end);");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("% Calculate the Jacobiean Matrix \n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("JM = ");
        buffer.append(strJacobianFunctionName);
        buffer.append("(c,kV);\n");
        buffer.append("\n");

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
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("calculate the P matrix\n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("\n");
        
        buffer.append("PM = zeros(n,m);\n");
        buffer.append("\n");

        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<(NROWS+NCOLS);state_counter_inner++)
            {
                // if it is a zero entry, just skip it
                if(!strPMatrix[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("PM(");
                    buffer.append(state_counter_outer+1);
                    buffer.append(",");
                    buffer.append(state_counter_inner+1);
                    buffer.append(")\t=\t");
                    buffer.append(strPMatrix[state_counter_outer][state_counter_inner]);
                    buffer.append(";\n");
                }
            }
        }
        buffer.append("\n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("% Calculate dsdt vector \n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("\n");
        buffer.append("dsdt \t=\t JM*s+PM(:,pIndex);\n");
        buffer.append("\n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("% Calculate dcdt vector \n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("dcdt = ");
        buffer.append(strMassBalanceFunctionName);
        buffer.append("(c,t,STM,kV,NRATES,NSTATES);\n");
        buffer.append("\n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("% Construct dxdt vector \n");
        buffer.append("% ---------------------------------------------------------------------\n");
        buffer.append("dxdt = [dcdt;dsdt]\n");
        buffer.append("\n");
    }


    private static String formulatePMatrixElement(double[][] matrix,int massbalance,int parameter,Vector vecReactions,Vector<Species> vecSpecies)
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
                        buffer.append("c(");
                        buffer.append(state_counter+1);
                        buffer.append(")*");
                    }
                    // any thing else I need to raise to a power
                    else {
                        buffer.append("(c(");
                        buffer.append(state_counter+1);
                        buffer.append(")^");
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


    







}
