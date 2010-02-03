/*
 * MatlabModel.java
 *
 * Created on December 29, 2006, 3:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.language.handler;

// import statements -
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.varnerlab.server.transport.LoadXMLPropFile;

/**
 *
 * @author jeffreyvarner
 */
public class ScilabModel {
	// Class/instance attributes -
	private LoadXMLPropFile _xmlPropTree = null;
	private Model model_wrapper = null;
	
    /** Creates a new instance of MatlabModel */
    public ScilabModel() {
    }
    
    public void setPropertyTree(LoadXMLPropFile prop)
	{
		_xmlPropTree = prop;
	}
	
	public void setModel(Model model)
	{
		model_wrapper = model;
	}
	
	private String getFunctionName(String xpath)
    {
    	String strTmpRaw = "";
    	
    	strTmpRaw = _xmlPropTree.getProperty(xpath);
    	int INT_2_DOT = strTmpRaw.indexOf(".");
    	String strName = strTmpRaw.substring(0, INT_2_DOT);
    	
    	return(strName);
    }
    
    private String getFileName(String xpath)
    {
    	String strTmpRaw = "";
    	strTmpRaw = _xmlPropTree.getProperty(xpath);	
    	return(strTmpRaw);
    }
    
    private String getFileNamePath(String xpath_name,String xpath_path)
    {
    	String strTmpRaw = "";
    	String strTmpPath = "";
    	String strReturnString = "";
    	
    	// Get the name -
    	strTmpRaw = _xmlPropTree.getProperty(xpath_name);	
    	
    	// Get the path -
    	strTmpPath = _xmlPropTree.getProperty(xpath_path);	
    	
    	// Formulate the return string -
    	if (!strTmpPath.isEmpty())
    	{
    		strReturnString = strTmpPath+"/"+strTmpRaw;
    	}
    	else
    	{
    		strReturnString = strTmpRaw;
    	}
    	
    	// return -
    	return(strReturnString);
    }
       
    public void buildInputsBuffer(StringBuffer inputs) throws Exception
    {
        // Ok, so the build the input buffer -
        
        // Away we go...
		inputs.append("function [uV] = Inputs(t,x,DF)\n\n");
		inputs.append("// Ouput variables initialisation (not found in input variables)\n");
		inputs.append("uV=[];\n\n");
		inputs.append("// Display mode\n");
		inputs.append("mode(0);\n\n");
		inputs.append("// Display warning for floating point exception\n");
		inputs.append("ieee(1);\n\n");
		inputs.append("// The default is to return a vector of zeros.\n");
		inputs.append("// Override with specific logic.\n");
		inputs.append("nR = max(size(x));\n");
		inputs.append("uV = zeros(nR,1);\n");
		inputs.append("return;\n");
		inputs.append("endfunction\n");
		inputs.append("\n");
    }
    
    public void buildDataFileBuffer(StringBuffer datafile) throws Exception
    {
        // Ok, so build the datafile buffer
        
        
        // Put in the header and go forward my undercover brotha...
    	String strDataFile = getFunctionName("//DataFile/datafile_filename/text()");
    	
		datafile.append("function [DF] = ");
		datafile.append(strDataFile);
		datafile.append("(TSTART,TSTOP,Ts,INDEX)\n\n");
		datafile.append("// Output variables initialisation (not found in input variables)\n");
		datafile.append("DF=[];\n");
        datafile.append("global(\"DF\");\n\n");
		datafile.append("// Display mode\n");
		datafile.append("mode(0);\n\n");
		datafile.append("// Display warning for floating point exception\n");
		datafile.append("ieee(1);\n\n");
		datafile.append("// Datafile machine generated.\n\n");
		datafile.append("// Load the stoichiometrix matrix --\n");
		datafile.append("S = fscanfMat(\"");
		
		// Get the stoichiometric path and filename from the DOM tree -
		String strSTMFile = getFileName("//StoichiometricMatrix/stoichiometric_matrix_filename/text()");
		String strSTMPath = getFileName("//StoichiometricMatrix/stoichiometric_matrix_path/text()");
		
		String strTmp = "";
		if (strSTMPath.isEmpty())
		{
			strTmp = strSTMFile;
		}
		else
		{
			strTmp = strSTMPath+"/"+strSTMFile;
		}
		
		
		datafile.append(strTmp);
        datafile.append("\");\n");
		datafile.append("[NROWS,NCOLS] = size(mtlb_double(S));\n\n");
		
		// Rate constants -
		datafile.append("// Formulate the rate constant vector k --\n");
		datafile.append("k = zeros(NCOLS,1);\n\n");
		datafile.append("k = [\n");
     
		ListOf parameter_list = model_wrapper.getListOfParameters();
        ListOf rate_list = model_wrapper.getListOfReactions();
        int NUMBER_OF_PARAMETERS = (int)model_wrapper.getNumParameters();
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
        
        // Initial conditions -
		datafile.append("// Formulate the initial condition vector IC --\n");
		datafile.append("IC = zeros(NROWS,1);\n\n");
		datafile.append("IC = [\n");
		
		ListOf species_list = model_wrapper.getListOfSpecies();
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
        
        datafile.append("];\n\n");

        // Rest of the file -
		datafile.append("// Load parameter sets from disk -\n");
		datafile.append("NPARAMETERS = size(k,\"*\");\n");
		datafile.append("NSTATES = size(IC,\"*\");\n");
		datafile.append("kV = [k;IC];\n\n");

		datafile.append("// This code block is untested for scilab implementation.\n");
		datafile.append("// Ok, override the choice of parameters above, load from disk -\n");
		datafile.append("if ~isempty(INDEX) then\n");
		datafile.append("  // !! L.24: string output can be different from Matlab num2str output.\n");
		datafile.append("  cmd = \"load PSET_\"+string(INDEX)+\".mat\";\n");
		datafile.append("  mtlb_eval(cmd);\n");
		datafile.append("  // ! L.26: mtlb(kP) can be replaced by kP() or kP whether kP is an M-file or not.\n");
		datafile.append("  kV = mtlb(kP);\n");
		datafile.append("  // get k and IC -\n");
		datafile.append("  k = kV(1:NPARAMETERS);\n");
		datafile.append("  IC = kV(NPARAMETERS+1:$);\n");
		datafile.append("end;\n\n");

		datafile.append("// =========== DO NOT EDIT BELOW THIS LINE ==============\n");
		datafile.append("DF.STOICHIOMETRIC_MATRIX = S;\n");
		datafile.append("DF.RATE_CONSTANT_VECTOR = k;\n");
		datafile.append("DF.INITIAL_CONDITIONS = IC;\n");
		datafile.append("DF.NUMBER_PARAMETERS = NPARAMETERS;\n");
		datafile.append("DF.NUMBER_OF_STATES = NSTATES;\n");
		datafile.append("DF.PARAMETER_VECTOR = kV;\n");
		datafile.append("// ======================================================\n\n");

		datafile.append("return;\n");
		datafile.append("endfunction\n");
		datafile.append("\n");

    }
    
    public void buildDriverBuffer(StringBuffer driver) throws Exception 
    {
        // Ok, so build the buffer 
        
        
        // Put in the header and go -
    	String strDriverFunctionName = getFunctionName("//DriverFile/driver_filename/text()");
		driver.append("function [T,X] = ");
		driver.append(strDriverFunctionName);
		driver.append("(pDataFile,TSTART,TSTOP,Ts,DFIN)\n\n");
		
		driver.append("// Ouput variables initialisation (not found in input variables)\n");
		driver.append("T=[];\n");
		driver.append("X=[];\n\n");
        driver.append("precision = 1e-10;\n");

        // Ok, so we need to get a bunch of stuff from the prop tree -
        String strKinetics = getFileNamePath("//KineticsFunction/kinetics_filename/text()","//KineticsFunction/kinetics_path/text()");
        String strMassBalances = getFileNamePath("//MassBalanceFunction/massbalance_filename/text()","//MassBalanceFunction/massbalance_path/text()");
        String strDataFile = getFileNamePath("//DataFile/datafile_filename/text()","//DataFile/datafile_path/text()");
        String strInputs = getFileNamePath("//InputFunction/input_function_filename/text()","//InputFunction/input_function_path/text()");
        String strJacobian = getFileNamePath("//MassBalanceFunction/jacobian_filename/text()","//MassBalanceFunction/jacobian_path/text()");
        String strBMatrix = getFileNamePath("//MassBalanceFunction/bmatrix_filename/text()","//MassBalanceFunction/bmatrix_path/text()");
        
		driver.append("getf(\""+strKinetics+"\");\n");
		driver.append("getf(\""+strMassBalances+"\");\n");
		driver.append("getf(\""+strBMatrix+"\");\n");
		driver.append("getf(\""+strJacobian+"\");\n");
		driver.append("getf(\""+strDataFile+"\");\n");
		driver.append("getf(\""+strInputs+"\");\n\n");

		driver.append("// Display mode\n");
		driver.append("mode(0);\n\n");

		driver.append("// Display warning for floating point exception\n");
		driver.append("ieee(1);\n\n");

		driver.append("\n");
		driver.append("// Check to see if I need to load the datafile\n");
		driver.append("if ~isempty(DFIN) then\n");
		driver.append("  DF = DFIN;\n");
		driver.append("else\n");
		driver.append("    DF = evstr(pDataFile+\"(TSTART,TSTOP,Ts,[])\");\n");
		driver.append("end;\n\n");

		driver.append("// Get reqd stuff from data struct -\n");
		driver.append("IC = DF.INITIAL_CONDITIONS;\n");
		driver.append("T = TSTART:Ts:TSTOP;\n\n");

		driver.append("// Call the ODE solver, default setting is \"stiff\"\n");
		driver.append("X = (ode(\"stiff\", IC, TSTART, T, precision, precision, list(MassBalances,DF), Jacobian))\';\n\n");

		
		driver.append("// Dump Output to a file for error analysis\n");
		driver.append("fprintfMat(\"scilab_output.dat\",X,\'%g\');\n\n");

		driver.append("return;\n");
		driver.append("endfunction\n");
		driver.append("\n");

    }
    
       
    public void buildMassBalanceBuffer(StringBuffer massbalances) throws Exception
    {
        // build the mass balance buffer -
     
        
        // Put the header -
		massbalances.append("function [DXDT] = MassBalances(t,x,DF)\n\n");

		massbalances.append("// Ouput variables initialisation (not found in input variables)\n");
		massbalances.append("DXDT=[];\n\n");

		massbalances.append("// Display mode\n");
		massbalances.append("mode(0);\n\n");

		massbalances.append("// Display warning for floating point exception\n");
		massbalances.append("ieee(1);\n\n");

		massbalances.append("// This file is machine generated. Plz don\'\'t change. I know who you are...\n\n");

		massbalances.append("// Get some stuff from the data struct -\n");
		massbalances.append("S = DF.STOICHIOMETRIC_MATRIX;\n\n");

		massbalances.append("// Call the kinetics\n");
		massbalances.append("rV = Kinetics(t,x,DF);\n\n");

		massbalances.append("// Calculate the input vector\n");
		massbalances.append("uV = Inputs(t,x,DF);\n\n");

		massbalances.append("// Calculate DXDT\n");
		massbalances.append("DXDT = S*rV+uV;\n\n");

		massbalances.append("// return to caller\n");
		massbalances.append("return;\n");
		massbalances.append("endfunction\n");
		massbalances.append("\n");

    }      
    
    public void buildKineticsBuffer(StringBuffer kinetics,Vector vecReactions) throws Exception
    {
    	// Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)vecReactions.size();
        
        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions);

        // Ok, so add the header to the file -
		kinetics.append("function [r] = Kinetics(t,x,DF)\n\n");

		kinetics.append("// Ouput variables initialisation (not found in input variables)\n");
		kinetics.append("r=[];\n\n");

		kinetics.append("// Display mode\n");
		kinetics.append("mode(0);\n\n");

		kinetics.append("// Display warning for floating point exception\n");
		kinetics.append("ieee(1);\n\n");

		kinetics.append("// Machine generated file. Edit on pain of death. You have been warned.\n\n");

		kinetics.append("// Get some stuff from the data struct.\n");
		kinetics.append("k = DF.PARAMETER_VECTOR;\n\n");

		kinetics.append("// Put the x\'\'s in terms of the symbols -- helps with debugging.\n");

        // look through the sorted alpha -
		ListOfSpecies alphabetList = model_wrapper.getListOfSpecies();
        for (int alpha_index=0;alpha_index<NROWS;alpha_index++)
        {
            // Ok, so put this in the buffer -
            kinetics.append("// ");
            kinetics.append(alphabetList.get((long)alpha_index).getName());
            kinetics.append("=x(");
            kinetics.append(alpha_index);
            kinetics.append(");\n");
        }
        kinetics.append("\n");

        kinetics.append("\t// Build the kinetics\n");

        // Build the kinetics -
        for (int col_index=0;col_index<NCOLS;col_index++)
        {
            // Tmp vector -
            Vector tmp = new Vector();
            for (int row_index=0;row_index<NROWS;row_index++)
            {
                // Go through each row and find the mij < 0
                if (matrix[row_index][col_index]<0)
                {
                    tmp.addElement(row_index);
                }
            }

            // Ok, so I've figures out what rows we have to worry about -
            kinetics.append("\tr(");
            kinetics.append(col_index + 1);
            kinetics.append(",1)=k(");
            kinetics.append(col_index + 1);
            kinetics.append(",1)");

            int NTMP = tmp.size();
            for (int reactant = 0;reactant<NTMP;reactant++)
            {
                // Get the local index -
                int local_index = ((Integer)tmp.get(reactant)).intValue();

                // Get the st.coeff -
                double dblCoeff = matrix[local_index][col_index];
                if (-1*dblCoeff!=1.0)
                {
                    kinetics.append("*x(");
                    kinetics.append(local_index + 1);
                    kinetics.append(",1)^(");
                    kinetics.append(-1*dblCoeff);
                    kinetics.append(")");
                }
                else
                {
                    kinetics.append("*x(");
                    kinetics.append(local_index + 1);
                    kinetics.append(",1)");
                }
            }

            // Add the new line -
            kinetics.append(";\n");
        }

        // Ok, so when I get here, then I can add the return line -
        kinetics.append("return;");
    }

    public void buildJacobianBuffer(StringBuffer buffer,Vector vecReactions) throws Exception
    {
    	// Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)vecReactions.size();
        
        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions);

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

        // StringBuffer tmpBuffer = new StringBuffer();
        // Vector<String> vecConnect = new Vector<String>();
        // Vector<String> vecSpeciesRate = new Vector<String>();
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // put jacobian logic here -
                strJacobian[state_counter_outer][state_counter_inner]=formulateJacobianElement(matrix,state_counter_outer,state_counter_inner);
            }
        }

        // Ok, so when I get here I have the Jacobian - we need to convert it into a string buffer
		buffer.append("function [JM] = Jacobian(t,x)\n\n");
		buffer.append("// Ouput variables initialisation (not found in input variables)\n");
		buffer.append("JM=[];\n\n");
		buffer.append("// Display mode\n");
		buffer.append("mode(0);\n\n");
		buffer.append("// Display warning for floating point exception\n");
		buffer.append("ieee(1);\n\n");
        buffer.append("k = DF.PARAMETER_VECTOR;\n\n");
		buffer.append("// Machine generated dfdx matrix (Jacobian).\n");

        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // skip this entry if it is zero
                if(!strJacobian[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\tJM(");
                    buffer.append(state_counter_outer+1);
                    buffer.append(",");
                    buffer.append(state_counter_inner+1);
                    buffer.append(")=");
                    buffer.append(strJacobian[state_counter_outer][state_counter_inner]);
                    buffer.append(";\n");
                }
            }
        }
        buffer.append("return;\n");
		buffer.append("endfunction\n");
		buffer.append("\n");
    }

    private String formulateJacobianElement(double[][] matrix,int massbalance,int state)
    {
        StringBuffer buffer = new StringBuffer();
        String rString = "";

        // Ok, so when I get here, I have to formulate the entry in the jacobian matrix -
        
        // Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)model_wrapper.getNumReactions();

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
                    buffer.append("k(");
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
                    buffer.append("k(");
                    buffer.append(test_index+1);
                    buffer.append(")*x(");
                    buffer.append(state);
                    buffer.append(")^(");
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
                            buffer.append(species_index+1);
                            buffer.append(")");
                        }
                        // any thing else I need to raise to a power
                        else {
                            buffer.append("*x(");
                            buffer.append(species_index+1);
                            buffer.append(")^(");
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


    public void buildPMatrixBuffer(StringBuffer buffer,Vector vecReactions) throws Exception
    {
        // Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)vecReactions.size();
        
        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions);

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
                strPMatrix[counter_outer][counter_inner]=formulatePMatrixElement(matrix,counter_outer,counter_inner);
            }
        }

        // Convert into string buffer -
		buffer.append("function [PM] = PMatrix(x,t)\n\n");
		buffer.append("// Ouput variables initialisation (not found in input variables)\n");
		buffer.append("PM=[];\n\n");
		buffer.append("// Display mode\n");
		buffer.append("mode(0);\n\n");
		buffer.append("// Display warning for floating point exception\n");
		buffer.append("ieee(1);\n\n");
		buffer.append("// Machine generated dfdp matrix.\n");

        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<(NROWS+NCOLS);state_counter_inner++)
            {
                // if it is a zero entry, just skip it
                if(!strPMatrix[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\tPM(");
                    buffer.append(state_counter_outer+1);
                    buffer.append(",");
                    buffer.append(state_counter_inner+1);
                    buffer.append(")=");
                    buffer.append(strPMatrix[state_counter_outer][state_counter_inner]);
                    buffer.append(";\n");
                }
            }
        }
		buffer.append("return;\n");
		buffer.append("endfunction\n");
		buffer.append("\n");
    }


    String formulatePMatrixElement(double[][] matrix,int massbalance,int parameter)
    {
        StringBuffer buffer = new StringBuffer();
        String rString = "0.0";

        // Get the size of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)model_wrapper.getNumReactions();

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
                        buffer.append(state_counter+1);
                        buffer.append(")*");
                    }
                    // any thing else I need to raise to a power
                    else {
                        buffer.append("x(");
                        buffer.append(state_counter+1);
                        buffer.append(")^(");
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
