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
 *
 * Created on May 9, 2007, 3:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.language.handler;


// import statements -
import java.util.Hashtable;
import java.util.Vector;

import org.sbml.libsbml.*;

/**
 *
 * @author jeffreyvarner
 */
public class CodeGenUtilMethods extends Object {
    
    /** Creates a new instance of CodeGenUtilMethods */
    public CodeGenUtilMethods() {
    	// Going to test the commit...
    }
    
    
    // Ok, some methods that are static so I can call them from anywhere -

     public static void buildHardCodeMassBalanceEquations(StringBuffer buffer,Model model_wrapper) throws Exception {
         // Get the dimension of the system -
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies();
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions();

        int NROWS = NUMBER_OF_SPECIES;
        int NCOLS = NUMBER_OF_RATES;
        double[][] matrix = null;				// Create a local copy of the stoichiometric matrix -


        // Initialize the stoichiometric matrix -
        matrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];

        // Build the matrix -
        CodeGenUtilMethods.buildStoichiometricMatrix(matrix,model_wrapper);

        double stm = 0;
        boolean firstTime = false;

        buffer.append("void calculateMassBalances(int NRATES,int NSTATES,ColumnVector& rV,ColumnVector& dx)\n");
        buffer.append("{\n");
        // Changing this line: buffer.append("\tdx=STMATRIX*rV;\n"); -
		for (int i=0;i<NROWS;i++) // Loop through the species
       		 {
			for (int j=0;j<NCOLS;j++) // Loop through the reactions
			{
				if(j==0) // If not the first reaction, append a plus sign in between the rates
				{
                                    buffer.append("dx("+String.valueOf(i)+")=");
                                    firstTime = true;
                                }
                                if(matrix[i][j] != 0)
				{
                                    if(j!=0 && firstTime != true) // If not the first reaction, append a plus sign in between the rates
                                    {
					buffer.append("+");
                                    }
                                    stm = matrix[i][j]; // Grab the value from the stoich. matrix
                                    buffer.append(String.valueOf(stm)+"*rV("+String.valueOf(j)+")");
                                    firstTime = false;
				}
			}
			if(i!=NROWS) // If not the last species, enter new line
			{
				buffer.append(";\n");
			}
		}
        buffer.append("}\n");
    }

    public static void buildJacobianBuffer_2(StringBuffer buffer,Model model_wrapper) throws Exception
    {
        
    	// Get the dimension of the system - 
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
    	
        int NROWS = NUMBER_OF_SPECIES;
        double[][] dblSTMatrix = null;				// Create a local copy of the stoichiometric matrix -
        
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        CodeGenUtilMethods.buildStoichiometricMatrix(dblSTMatrix,model_wrapper);
        
//        
//        // Create a local copy of the stoichiometric matrix -
//        double[][] matrix = new double[NROWS][NCOLS];
//        Iterator iter = this._vecRowWrappers.iterator();
//        int row_counter = 0;
//        int col_counter = 0;
//        while (iter.hasNext())
//        {
//            // Get the row -
//            RowWrapper row = (RowWrapper)iter.next();
//            String strRow = row.toString();
//            
//            // Tokenize the row -
//            StringTokenizer tokenizer = new StringTokenizer(strRow," ");
//            while (tokenizer.hasMoreElements())
//            {
//                String element = (String)tokenizer.nextElement();
//                matrix[row_counter][col_counter]= Double.parseDouble(element);
//                
//                // update the col_counter -
//                col_counter++;
//            }
//            
//            // go around again -
//            row_counter++;
//            
//            // reset the col counter -
//            col_counter=0;
//        }
        
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
        
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // put jacobian logic here -
                strJacobian[state_counter_outer][state_counter_inner]=formulateJacobianElement_2(dblSTMatrix,state_counter_outer,state_counter_inner,model_wrapper);
            }
        }
        
        
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // skip this entry if it is zero
                if(!strJacobian[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\tDENSE_ELEM(J,");
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
     public static void buildJacobianBuffer(StringBuffer buffer,Model model_wrapper) throws Exception
    {
        
    	// Get the dimension of the system - 
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
    	
        int NROWS = NUMBER_OF_SPECIES;
        double[][] dblSTMatrix = null;				// Create a local copy of the stoichiometric matrix -
        
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        CodeGenUtilMethods.buildStoichiometricMatrix(dblSTMatrix,model_wrapper);
        
//        
//        // Create a local copy of the stoichiometric matrix -
//        double[][] matrix = new double[NROWS][NCOLS];
//        Iterator iter = this._vecRowWrappers.iterator();
//        int row_counter = 0;
//        int col_counter = 0;
//        while (iter.hasNext())
//        {
//            // Get the row -
//            RowWrapper row = (RowWrapper)iter.next();
//            String strRow = row.toString();
//            
//            // Tokenize the row -
//            StringTokenizer tokenizer = new StringTokenizer(strRow," ");
//            while (tokenizer.hasMoreElements())
//            {
//                String element = (String)tokenizer.nextElement();
//                matrix[row_counter][col_counter]= Double.parseDouble(element);
//                
//                // update the col_counter -
//                col_counter++;
//            }
//            
//            // go around again -
//            row_counter++;
//            
//            // reset the col counter -
//            col_counter=0;
//        }
        
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
        
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // put jacobian logic here -
                strJacobian[state_counter_outer][state_counter_inner]=formulateJacobianElement(dblSTMatrix,state_counter_outer,state_counter_inner,model_wrapper);
            }
        }
        
        // Ok, so when I get here I have the Jacobian - we need to convert it into a string buffer
        buffer.append("void calculateJacobian(int NSTATES, ColumnVector& k, ColumnVector& x, Matrix& JM)\n");
        buffer.append("{\n");
        buffer.append("\t// Machine generated dfdx matrix (Jacobian).\n");
        buffer.append("\n");
        
        // Use a fill call -
        buffer.append("\t// Initialize the Jacobian to 0.0\n");
        buffer.append("\tJM.fill(0.0);\n");
        buffer.append("\n");
        
        //buffer.append("\tint i;\n");
        //buffer.append("\tint j;\n");
        //buffer.append("\tfor (i=0;i<NSTATES;i++){\n");
        //buffer.append("\t\tfor (j=0;j<NSTATES;j++){\n");
        //buffer.append("\t\t\tJM(i,j) = 0.0;\n");
        //buffer.append("\t\t}\n");
        //buffer.append("\t}\n");
        
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
    
    public static void buildPMatrixBuffer_2(StringBuffer buffer,Model model_wrapper) throws Exception
    {
        // Get the dimension of the system -
    	// Get the dimension of the system - 
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
    	
    	int NROWS = NUMBER_OF_SPECIES;				// Number of species (rows in the stmatrix)
        int NCOLS = NUMBER_OF_RATES;				// Number of rates (cols in the stmatrix)
        double[][] dblSTMatrix = null;				// Create a local copy of the stoichiometric matrix -
        
        
//        Iterator iter = this._vecRowWrappers.iterator();
//        int row_counter = 0;
//        int col_counter = 0;
//        while (iter.hasNext())
//        {
//            // Get the row -
//            RowWrapper row = (RowWrapper)iter.next();
//            String strRow = row.toString();
//            
//            // Tokenize the row -
//            StringTokenizer tokenizer = new StringTokenizer(strRow," ");
//            while (tokenizer.hasMoreElements())
//            {
//                String element = (String)tokenizer.nextElement();
//                matrix[row_counter][col_counter]= Double.parseDouble(element);
//                
//                // update the col_counter -
//                col_counter++;
//            }
//            
//            // go around again -
//            row_counter++;
//            
//            // reset the col counter -
//            col_counter=0;
//        }
        
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        CodeGenUtilMethods.buildStoichiometricMatrix(dblSTMatrix,model_wrapper);
        
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
                strPMatrix[counter_outer][counter_inner]=formulatePMatrixElement_2(dblSTMatrix,counter_outer,counter_inner,model_wrapper);
            }
        }
        
        
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<(NROWS+NCOLS);state_counter_inner++)
            {
                // if it is a zero entry, just skip it
                if(!strPMatrix[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\tDENSE_ELEM(PM(,");
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
     public static void buildPMatrixBuffer(StringBuffer buffer,Model model_wrapper) throws Exception
    {
        // Get the dimension of the system -
    	// Get the dimension of the system - 
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
    	
    	int NROWS = NUMBER_OF_SPECIES;				// Number of species (rows in the stmatrix)
        int NCOLS = NUMBER_OF_RATES;				// Number of rates (cols in the stmatrix)
        double[][] dblSTMatrix = null;				// Create a local copy of the stoichiometric matrix -
        
        
//        Iterator iter = this._vecRowWrappers.iterator();
//        int row_counter = 0;
//        int col_counter = 0;
//        while (iter.hasNext())
//        {
//            // Get the row -
//            RowWrapper row = (RowWrapper)iter.next();
//            String strRow = row.toString();
//            
//            // Tokenize the row -
//            StringTokenizer tokenizer = new StringTokenizer(strRow," ");
//            while (tokenizer.hasMoreElements())
//            {
//                String element = (String)tokenizer.nextElement();
//                matrix[row_counter][col_counter]= Double.parseDouble(element);
//                
//                // update the col_counter -
//                col_counter++;
//            }
//            
//            // go around again -
//            row_counter++;
//            
//            // reset the col counter -
//            col_counter=0;
//        }
        
        
        // Initialize the stoichiometric matrix -
        dblSTMatrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        
        // Build the matrix -
        CodeGenUtilMethods.buildStoichiometricMatrix(dblSTMatrix,model_wrapper);
        
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
                strPMatrix[counter_outer][counter_inner]=formulatePMatrixElement(dblSTMatrix,counter_outer,counter_inner,model_wrapper);
            }
        }
        
        
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
    
    private static String formulatePMatrixElement_2(double[][] matrix,int massbalance,int parameter,Model model_wrapper)
    {
        
    	// Get the dimension of the system - 
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
    	
    	StringBuffer buffer = new StringBuffer();
        String rString = "0.0";
        
        int NROWS = NUMBER_OF_SPECIES;
                
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
                        buffer.append("NV_Ith_S(x");
                        buffer.append(state_counter);
                        buffer.append(")*");
                    }
                    // any thing else I need to raise to a power
                    else {
                        buffer.append("pow(NV_Ith_S(x");
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
     private static String formulatePMatrixElement(double[][] matrix,int massbalance,int parameter,Model model_wrapper)
    {
        
    	// Get the dimension of the system - 
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
    	
    	StringBuffer buffer = new StringBuffer();
        String rString = "0.0";
        
        int NROWS = NUMBER_OF_SPECIES;
                
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
    
 // Dump methods -
    public static void dumpKineticsFileToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // Ok, dump this to disk -
        GIOL.write((String)_propTable.get("OUTPUT_KINETICS_FILENAME"),buffer);
    }

    public static void dumpMassBalancesToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_MASSBALANCES_FILENAME"),buffer);
    }
    
    public static void dumpAdjBalFntToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_ADJBALFNT_FILENAME"),buffer);
    }
    public static void dumpSolveAdjBalToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_SOLVEADJBAL_FILENAME"),buffer);
    }

    public static void dumpDriverToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_DRIVER_FILENAME"),buffer);
    }

    public static void dumpDataFileToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_DATAFILE_FILENAME"),buffer);
    }

    public static void dumpInputsToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_INPUTS_FILENAME"),buffer);
    }
    
    public static void dumpJacobianToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_JACOBIAN_FILENAME"),buffer);
    }
    
    public static void dumpPMatrixToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_PMATRIX_FILENAME"),buffer);
    }
    public static void dumpDSDTToDisk(Hashtable _propTable,StringBuffer buffer) throws Exception {
        // I have populated the string buffer, dump that mofo
        GIOL.write((String)_propTable.get("OUTPUT_DSDT_FILENAME"),buffer);
    }

    public static void dumpStoichiometricMatrixToDisk(Hashtable _propTable,int NR,int NC,double[][] dblSTMatrix) throws Exception
    {
        // Method attributes -
        String strPath = "";
        StringBuffer buffer = new StringBuffer();
        
        for (int scounter=0;scounter<NR;scounter++)
        {
            for (int rcounter=0;rcounter<NC;rcounter++)
            {
                buffer.append(dblSTMatrix[scounter][rcounter]);
                buffer.append("\t");
            }
            
            buffer.append("\n");
        }
        
        
        // Get the path and dump -2- disk 
        GIOL.write((String)_propTable.get("OUTPUT_STM_FILENAME"),buffer);
    }
    
    // This logic will need to be overriden for kinetics other than mass action -
    private static String formulateJacobianElement_2(double[][] matrix,int massbalance,int state,Model model_wrapper)
    {
        StringBuffer buffer = new StringBuffer();
        String rString = "";
        
        // Ok, so when I get here, I have to formulate the entry in the jacobian matrix -
        // I'm hacking together code from two different code bases - come back and fix later ...
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
        int NROWS = NUMBER_OF_SPECIES;
        int NCOLS = NUMBER_OF_RATES;
        
        // Grab the stoichiometric coefficients for this species (iterate over the reactions)
        Vector<String> vecRates = new Vector<String>(); 
        for (int counter=0;counter<NCOLS;counter++)
        {
            if (matrix[massbalance][counter]!=0.0)
            {
                vecRates.addElement(String.valueOf(counter));
            }
        }
        
      
        // RT wrote this .... if it breaks, blame him (rat44@cornell.edu)
        boolean lFlag = false;
        for (int rate_index=0;rate_index<NUMBER_OF_RATES;rate_index++)
        {
        	// Get the index of the stoichiometric coefficients for this rate -
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
                    buffer.append("NV_Ith_S(k,");
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
                    buffer.append("NV_Ith_S(k,");
                    buffer.append(test_index);
                    buffer.append(")*NV_Ith_S(x,");
                    buffer.append(state);
                    buffer.append(")");
                }
                
                // any thing else I need to raise to a power
                else {
                    buffer.append(matrix[massbalance][test_index]);
                    buffer.append("*");
                    buffer.append(tempStmElement);
                    buffer.append("*");
                    buffer.append("NV_Ith_S(k,");
                    buffer.append(test_index);
                    buffer.append(")*pow(NV_Ith_S(x,");
                    buffer.append(state);
                    buffer.append("),");
                    buffer.append(tempStmElement-1);
                    buffer.append(")");
                }
                

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
                            buffer.append("*NV_Ith_S(x,");
                            buffer.append(species_index);
                            buffer.append(")");
                        }
                        // any thing else I need to raise to a power
                        else {
                            buffer.append("*pow(NV_Ith_S(x,");
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
    
     // This logic will need to be overriden for kinetics other than mass action -
    private static String formulateJacobianElement(double[][] matrix,int massbalance,int state,Model model_wrapper)
    {
        StringBuffer buffer = new StringBuffer();
        String rString = "";
        
        // Ok, so when I get here, I have to formulate the entry in the jacobian matrix -
        // I'm hacking together code from two different code bases - come back and fix later ...
    	int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
        int NROWS = NUMBER_OF_SPECIES;
        int NCOLS = NUMBER_OF_RATES;
        
        // Grab the stoichiometric coefficients for this species (iterate over the reactions)
        Vector<String> vecRates = new Vector<String>(); 
        for (int counter=0;counter<NCOLS;counter++)
        {
            if (matrix[massbalance][counter]!=0.0)
            {
                vecRates.addElement(String.valueOf(counter));
            }
        }
        
      
        // RT wrote this .... if it breaks, blame him (rat44@cornell.edu)
        boolean lFlag = false;
        for (int rate_index=0;rate_index<NUMBER_OF_RATES;rate_index++)
        {
        	// Get the index of the stoichiometric coefficients for this rate -
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
    
    
    // convert reversible metabolic reactions -
    public static void convertMetabolicRates(Model model_wrapper) throws Exception
    {
        // We need to treat the reversible reactions -
        int NUMBER_OF_RATES_INITIAL = (int)model_wrapper.getNumReactions();
        ListOf rate_list_initial = model_wrapper.getListOfReactions();
        for (int rate_counter=0;rate_counter<NUMBER_OF_RATES_INITIAL;rate_counter++)
        {
            // Get Reaction =
            Reaction rxn_local = (Reaction)rate_list_initial.get(rate_counter);
            
            
            if (rxn_local.getReversible())
            {
                // If the rate is reversible then I need to split -
           
                // Create a new reaction object -
                Reaction rxn_new = new Reaction(2,4);
                
                // Before I do anything else, lets grab the notes and reverse
                if (rxn_local.isSetNotes())
                {
                    /*
                    // Gets the notes and split -
                    String strNotes = rxn_local..getNotes();
                    String[] bounds = strNotes.split(";");
                    
                    String newBounds = bounds[1]+";"+bounds[0]+";"+bounds[2];
                    
                    // Set the new bounds -
                    rxn_new.setNotes(newBounds);
                     */
                }
                
                
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
               
                
                // Ok, I need to add the reaction to the model_wrapper -
                model_wrapper.addReaction(rxn_new);
            }
        }
    }
          
    
    // check for reversible rates -
    public static void convertReversibleRates(Model model_wrapper) throws Exception
    {
        // We need to treat the reversible reactions -
        int NUMBER_OF_RATES_INITIAL = (int)model_wrapper.getNumReactions();
        ListOf rate_list_initial = model_wrapper.getListOfReactions();
        for (int rate_counter=0;rate_counter<NUMBER_OF_RATES_INITIAL;rate_counter++)
        {
            // Get Reaction =
            Reaction rxn_local = (Reaction)rate_list_initial.get(rate_counter);
            
            
            if (rxn_local.getReversible())
            {
                // If the rate is reversible then I need to split -
           
                // Create a new reaction object -
                Reaction rxn_new = new Reaction(2,4);
                
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
               
                
                // Ok, I need to add the reaction to the model_wrapper -
                model_wrapper.addReaction(rxn_new);
            }
        }
    }
    
    
    // Build the data file - these are always the same, so put here
    public static void buildDataFileBuffer(StringBuffer datafile,Model model,Hashtable _propTable) throws Exception
    {
        // Put in the header and go forward my undercover brotha...
        datafile.append("function DF=DataFile(TSTART,TSTOP,Ts,INDEX)\n");
        datafile.append("% Machine generated by Universal.\n");
        datafile.append("\n");
        datafile.append("% Load the stoichiometric matrix --\n");
        datafile.append("S=load('");
        
        // I need to get the file name, not the entire path -
        String strTmpPath = (String)_propTable.get("OUTPUT_STM_FILENAME");
        int last_slash = strTmpPath.lastIndexOf("/");
        
        datafile.append("../network/");
        datafile.append(strTmpPath.substring(last_slash+1, strTmpPath.length()));
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
            datafile.append("\t%\t");
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
            datafile.append(species.getInitialAmount());
            datafile.append("\t%\t");
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
        
        datafile.append("% =========== DO NOT EDIT BELOW THIS LINE ==============\n");
        datafile.append("DF.STOICHIOMETRIC_MATRIX=S;\n");
        datafile.append("DF.RATE_CONSTANT_VECTOR=k;\n");
        datafile.append("DF.INITIAL_CONDITIONS=IC;\n");
        datafile.append("DF.NUMBER_PARAMETERS=NPARAMETERS;\n");
        datafile.append("DF.NUMBER_OF_STATES=NSTATES;\n");
        datafile.append("DF.PARAMETER_VECTOR=kV;\n");
        datafile.append("% ======================================================\n");
        datafile.append("return;\n");
        
    }
    
    // Build the stoichiometric matrix -
    public static void buildStoichiometricMatrix(double[][] dblSTMatrix,Model model_wrapper) throws Exception
    {
        
        // Get the dimension of the system -
        int NUMBER_OF_SPECIES = 0; 
        int NUMBER_OF_RATES = 0;
        
        // Get the system dimension -
        NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
        
        // Go through and put everything as zeros by default -
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
            {
                dblSTMatrix[scounter][rcounter]=0.0;
            }
        }
        
        // When I get here, I have a st. matrix w/all zeros - 
        // put in the correct values - 
        ListOf listRates = model_wrapper.getListOfReactions();
        ListOf listSpecies = model_wrapper.getListOfSpecies();
        
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            // Get the species reference -
            Species species = (Species)listSpecies.get(scounter);
            String strSpecies = species.getName();
            
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
                       
                        dblSTMatrix[scounter][rcounter]=species_ref.getStoichiometry();
                    }
                    
                }
                
                // go through the products of this reaction -
                for (int product_index=0;product_index<NUMBER_OF_PRODUCTS;product_index++)
                {
                    // Get the species reference -
                    SpeciesReference species_ref = rxn_obj.getProduct(product_index);
                    String strProduct = species_ref.getSpecies();
                    
                    //System.out.println("Comparing NP="+NUMBER_OF_PRODUCTS+" to "+strProduct+"="+strSpecies+"?");
                    
                    if (strProduct.equalsIgnoreCase(strSpecies))
                    {
                        dblSTMatrix[scounter][rcounter]=species_ref.getStoichiometry();
                    }
                }
            }
        }
    }
}
