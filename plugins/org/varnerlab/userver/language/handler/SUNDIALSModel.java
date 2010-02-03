/*
 * SUNDIALSModel.java
 *
 * Created on June 26 2009 9:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.language.handler;

import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.varnerlab.server.transport.LoadXMLPropFile;

/**
 *
 * @author jeffreyvarner
 */
public class SUNDIALSModel {
    // Class/instance members -
    
    /** Creates a new instance of SUNDIALSModel */
    public SUNDIALSModel() {
    	System.out.println("New - "+System.getProperty("java.library.path"));
        System.loadLibrary("sbmlj");     
    }


    public void buildMassBalanceEquations(StringBuffer buffer) throws Exception {
        // Ok, so we need to build the buffer with the mass balance equations in it -

        buffer.append("static int MassBalances(realtype t, N_Vector StateVector, N_Vector dxdt, void *user_data)\n");
        buffer.append("{\n");
        buffer.append("\t// Prep some stuff\n");
        buffer.append("\tint i, j;\n");
        buffer.append("\trealtype tmp;\n");
        buffer.append("\tstruct params* Parameters = user_data;\n");
        buffer.append("\tN_Vector rateVector;\n\n");

        buffer.append("\t// Allocate rateVector memory\n");
        buffer.append("\trateVector = N_VNew_Serial(NUMBER_OF_RATES);\n");
        buffer.append("\t\tif (check_flag((void *)rateVector, \"N_VNew_Serial\", 0)) return(1);\n\n");

        buffer.append("\t// Grab the kinetics\n");
        buffer.append("\tKinetics(t, StateVector, (Parameters->pRateConstantVector), rateVector);\n\n");

        buffer.append("\t// Calculate dx/dt\n");
        buffer.append("\tfor (i=0; i< NUMBER_OF_STATES; i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\ttmp = 0;\n");
        buffer.append("\t\tfor (j=0; j < NUMBER_OF_RATES; j++)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\ttmp += DENSE_ELEM(Parameters->pSTM,i,j) * NV_Ith_S(rateVector,j);\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tNV_Ith_S(dxdt, i) = tmp;\n");
        buffer.append("\t}\n\n");

        buffer.append("\t// Free up rateVector memory\n");
        buffer.append("\tN_VDestroy(rateVector);\n\n");

        buffer.append("\treturn(0);\n");
        buffer.append("}\n\n");

    }

    public void buildKineticsBuffer(StringBuffer buffer,Model model_wrapper,Vector vecReactions) throws Exception {
        // Ok, build the kinetics -

    	// Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)vecReactions.size();
        

        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions);
    	

        // Now the fun begins -
        buffer.append("static void Kinetics(realtype t, N_Vector stateVector, N_Vector rateConstVector, N_Vector rateVector)\n");
        buffer.append("{\n");
        buffer.append("\t// Put the x's in terms of symbols, helps with debugging\n");

        // look through the sorted alpha -
        ListOfSpecies alphabetList = model_wrapper.getListOfSpecies();
        for (int alpha_index=0;alpha_index<NROWS;alpha_index++)
        {
            // Ok, so put this in the buffer -
            buffer.append("\t// ");
            buffer.append(alphabetList.get((long)alpha_index).getName());
            buffer.append("=x(");
            buffer.append(alpha_index);
            buffer.append(");\n");
        }
        buffer.append("\n");

        // Build the kinetics -
        buffer.append("\t// Build the kinetics\n");
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
            buffer.append("\tNV_Ith_S(rateVector,");
            buffer.append(col_index);
            buffer.append(")=NV_Ith_S(rateConstVector,");
            buffer.append(col_index);
            buffer.append(")");

            int NTMP = tmp.size();
            for (int reactant = 0;reactant<NTMP;reactant++)
            {
                // Get the local index -
                int local_index = ((Integer)tmp.get(reactant)).intValue();

                // Get the st.coeff -
                double dblCoeff = matrix[local_index][col_index];
                if (-1*dblCoeff!=1.0)
                {
                    buffer.append("*pow(NV_Ith_S(stateVector,");
                    buffer.append(local_index);
                    buffer.append("),");
                    buffer.append(-1*dblCoeff);
                    buffer.append(")");
                }
                else
                {
                    buffer.append("*NV_Ith_S(stateVector,");
                    buffer.append(local_index);
                    buffer.append(")");
                }

            }

            // Add the new line -
            buffer.append(";\n");
        }

        // Ok, so when I get here, then I can add the return line -
        buffer.append("}\n");
    }


    // Calculates using the Jacobian and PMatrix   (B)

    public void buildAdjBalFntBuffer(StringBuffer buffer,Model model_wrapper) throws Exception {

        buffer.append("/*\n");
		buffer.append(" * Template written by Robert Dromms\n");
		buffer.append(" * Created 2009-07-10 13:37\n");
		buffer.append(" * Based off example code provided in SUNDIALS documentation: cvRoberts_dns.c\n");
		buffer.append(" */\n\n");

		buffer.append("#include <stdio.h>\n");
		buffer.append("#include <stdarg.h>\n");
		buffer.append("#include <stdlib.h>\n");
		buffer.append("#include <unistd.h>\n");
		buffer.append("#include <math.h>\n");
		buffer.append("#include <time.h>\n");
		buffer.append("#include <errno.h>\n");
		buffer.append("#include <string.h>\n");
		buffer.append("#include <cvode/cvode.h>\t\t// prototypes for CVODE fcts., consts.\n");
		buffer.append("#include <nvector/nvector_serial.h>\t// serial N_Vector types, fcts., macros\n");
		buffer.append("#include <sundials/sundials_direct.h>\t// definitions DlsMat DENSE_ELEM\n");
		buffer.append("#include <sundials/sundials_types.h>\t// definition of type realtype\n\n");

		buffer.append("//PROBLEM SPECIFIC VALUES\n");
		buffer.append("#define TOLERANCE\t\t1e-10\t\t// global error tolerance\n");
		buffer.append("#define NUMBER_OF_STATES\t"+(int)model_wrapper.getNumSpecies()+"\t\t// number of equations\n");
		buffer.append("#define NUMBER_OF_RATES\t\t"+(int)model_wrapper.getNumReactions()+"\t\t// number of parameters\n");
		buffer.append("#define TOTAL_ADJ_STATES\t"+((int)model_wrapper.getNumSpecies() * ((int)model_wrapper.getNumReactions()+1))+"\t\t// Number of Adjoined States\n\n");

		buffer.append("// Functions to grab the kinetics rate constants and ic\'s from files\n");
		buffer.append("static int getRateConstants(const char* filename, N_Vector RateConstantVector);\n");
		buffer.append("static int getICs(const char* filename, N_Vector StateVector);\n");
		buffer.append("static int getSTM(const char* filename, DlsMat STM);\n");
		buffer.append("// Functions called by the solver\n");
		buffer.append("static int AdjBalances(realtype t, N_Vector x, N_Vector dxdt, void *user_data);\n");
		buffer.append("static void Kinetics(realtype t, N_Vector x, N_Vector rateConstVector, N_Vector rateVector);\n");
		buffer.append("static void calculatePMatrix(N_Vector k, N_Vector x, DlsMat PM);\n");
		buffer.append("static int Jac(int N, realtype t, N_Vector x, N_Vector fx, DlsMat J, void *user_data, N_Vector tmp1, N_Vector tmp2, N_Vector tmp3);\n");
		buffer.append("static void calcDSDT(N_Vector x, N_Vector dxdt, DlsMat Jac, DlsMat PMat);\n");
		buffer.append("// Function to dump data to files\n");
		buffer.append("static int dumpData(char* pDataFileName, N_Vector x, int xSize, realtype t, int FileCount, int newFile);\n");
		buffer.append("// Private function to check function return values\n");
		buffer.append("static int check_flag(void *flagvalue, char *funcname, int opt);\n\n");

		buffer.append("struct params\n");
		buffer.append("{\n");
		buffer.append("\tDlsMat pSTM;\n");
		buffer.append("\tN_Vector pRateConstantVector;\n");
		buffer.append("};\n\n");

		buffer.append("int main(int argc, char* const argv[])\n");
		buffer.append("{\n");
		buffer.append("\t/*\n");
		buffer.append("\t * Arguments must be in this order:\n");
		buffer.append("\t * 1. Data output file name\n");
		buffer.append("\t * 2. Kinetics rate constants / parameters file\n");
		buffer.append("\t * 3. Initial conditions file\n");
		buffer.append("\t * 4. STM data file\n");
		buffer.append("\t * 5. Start Time\n");
		buffer.append("\t * 6. End Time\n");
		buffer.append("\t * 7. Time step size\n");
		buffer.append("\t */\n\n");

		buffer.append("\tclock_t start = clock();\n\n");

		buffer.append("\t//Check number of arguments\n");
		buffer.append("\tif (argc != 8)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Incorrect number of arguments.\\n\");\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Prep some variables\n");
		buffer.append("\tint i, nTimes, flag, fileCount = 1;\n");
		buffer.append("\trealtype dblTime, dblTSTOP, dblTs;\n");
		buffer.append("\tN_Vector adjStateVector;\n");
		buffer.append("\tvoid *cvode_mem;\n");
		buffer.append("\tstruct params Parameters;\n\n");

		buffer.append("\t// assign info from arguments\n");
		buffer.append("\tchar *pOutputDataFile = argv[1];\t\t// Assign data output file\n");
		buffer.append("\tchar *pInputKineticsFile = argv[2];\t\t// Get kinetics datafile name\n");
		buffer.append("\tchar *pInputICFile = argv[3];\t\t\t// Get ic datafile name\n");
		buffer.append("\tchar *pSTMFile = argv[4];\t\t\t// Get STM datafile name\n");
		buffer.append("\tsscanf(argv[5], \"%lf\", &dblTime);\t\t// Start time\n");
		buffer.append("\tsscanf(argv[6], \"%lf\", &dblTSTOP);\t\t// Stop time\n");
		buffer.append("\tsscanf(argv[7], \"%lf\", &dblTs);\t\t\t// Time step size\n\n");

		buffer.append("\t//Allocate N_Vectors, DlsMats\n");
		buffer.append("\tadjStateVector = N_VNew_Serial(TOTAL_ADJ_STATES);\n");
		buffer.append("\t\tif (check_flag((void *)adjStateVector, \"N_VNew_Serial\", 0)) return(1);\n");
		buffer.append("\tParameters.pRateConstantVector = N_VNew_Serial(NUMBER_OF_RATES);\n");
		buffer.append("\t\tif (check_flag((void *)Parameters.pRateConstantVector, \"N_VNew_Serial\", 0)) return(1);\n");
		buffer.append("\tParameters.pSTM = NewDenseMat(NUMBER_OF_STATES, NUMBER_OF_RATES);\n");
		buffer.append("\t\tif (check_flag((void *)Parameters.pSTM, \"NewDenseMat\", 0)) return(1);\n\n");

		buffer.append("\t// Generate timestep array\n");
		buffer.append("\tnTimes = floor((dblTSTOP-dblTime)/dblTs)+1;\n");
		buffer.append("\trealtype TSIM[nTimes];\n");
		buffer.append("\tfor (i = 0; i<nTimes; i++)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tTSIM[i] = dblTime + i*dblTs;\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Load kinetics, IC\'s and STM\n");
		buffer.append("\tflag = getRateConstants(pInputKineticsFile, Parameters.pRateConstantVector);\n");
		buffer.append("\t\tif (flag != 0) return(1);\n");
		buffer.append("\tflag = getICs(pInputICFile, adjStateVector);\n");
		buffer.append("\t\tif (flag != 0) return(1);\n");
		buffer.append("\tflag = getSTM(pSTMFile, Parameters.pSTM);\n");
		buffer.append("\t\tif (flag != 0) return(1);\n\n");

		buffer.append("\t//Set Sensitivity Matrix to zero at t0\n");
		buffer.append("\tfor (i=NUMBER_OF_STATES; i<TOTAL_ADJ_STATES; i++)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tNV_Ith_S(adjStateVector, i) = 0;\n");
		buffer.append("\t}\n\n");

		buffer.append("\t/********************Set up the ODE solver*******************/\n\n");

		buffer.append("\t// Create ODE solver memory block\n");
		buffer.append("\tcvode_mem = CVodeCreate(CV_ADAMS, CV_FUNCTIONAL);\t// Adams Method, Functional iteration\n");
		buffer.append("\t\tif (check_flag((void *)cvode_mem, \"CVodeCreate\", 0)) return(1);\n");
		buffer.append("\t// Initialize the ODE solver\n");
		buffer.append("\tflag = CVodeInit(cvode_mem, AdjBalances, TSIM[0], adjStateVector);\n");
		buffer.append("\t\tif (check_flag(&flag, \"CVodeInit\", 1)) return(1);\n");
		buffer.append("\t// Pass rate constants, STM to solver\n");
		buffer.append("\tflag = CVodeSetUserData(cvode_mem, &Parameters);\n");
		buffer.append("\t\tif (check_flag(&flag, \"CVodeSetUserData\", 1)) return(1);\n");
		buffer.append("\t// Specifify absolute and relative tolerances\n");
		buffer.append("\tflag = CVodeSStolerances(cvode_mem, TOLERANCE, TOLERANCE);\n");
		buffer.append("\t\tif (check_flag(&flag, \"CVodeSStolerances\", 1)) return(1);\n");
                buffer.append("\t// Adjust max internal steps - 0 for default (=500), <0 for unlimited, or >0.\n");
                buffer.append("\tflag = CVodeSetMaxNumSteps(cvode_mem, -1);\n");
                buffer.append("\tif (check_flag(&flag, \"CVodeSetMaxNumSteps\", 1)) return(1);\n\n");

		buffer.append("\t/*****************ODE Solver Setup Complete!*****************/\n\n");

		buffer.append("\t// Dump intitial state to output\n");
		buffer.append("\tfileCount = dumpData(pOutputDataFile, adjStateVector, TOTAL_ADJ_STATES, TSIM[0], fileCount, 1);\n");
		buffer.append("\tif (fileCount <1)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error dumping data at t = %g\\n\", TSIM[0]);\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Main ODE Solver loop\n");
		buffer.append("\tfor (i=0; i < nTimes-1; i++)\n");
		buffer.append("\t{\n");
		buffer.append("\t\t// Calculate one step of the ODE Solution\n");
		buffer.append("\t\tflag = CVode(cvode_mem, TSIM[i+1], adjStateVector, TSIM+i, CV_NORMAL);\n\n");

		buffer.append("\t\tif (check_flag(&flag, \"CVode\", 1))\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tprintf(\"ODE Solver loop failed at t = %g\\n\", TSIM[i]);\n");
		buffer.append("\t\t\treturn(1);\n");
		buffer.append("\t\t}\n\n");

		buffer.append("\t\tif (flag == CV_SUCCESS)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\t//Dump Current state to output files\n");
		buffer.append("\t\t\tfileCount = dumpData(pOutputDataFile, adjStateVector, TOTAL_ADJ_STATES, TSIM[i], fileCount, 0);\n");
		buffer.append("\t\t\tif (fileCount <1)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tprintf(\"Error dumping data at t = %g\\n\", TSIM[i]);\n");
		buffer.append("\t\t\t\treturn(1);\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Free N_Vectors\n");
		buffer.append("\tN_VDestroy_Serial(adjStateVector);\n");
		buffer.append("\tN_VDestroy_Serial(Parameters.pRateConstantVector);\n");
		buffer.append("\tDestroyMat(Parameters.pSTM);\n\n");

		buffer.append("\t// Free integrator memory\n");
		buffer.append("\tCVodeFree(&cvode_mem);\n\n");

		buffer.append("\tprintf(\"Time elapsed: %f\\n\", ((double)clock() - start) / CLOCKS_PER_SEC);\n\n");

		buffer.append("\treturn(0);\n");
		buffer.append("}\n\n");

		buffer.append("/* This function comes directly from the example code cvRoberts_dns.c included with SUNDIALS.\n");
		buffer.append(" * Check function return value...\n");
		buffer.append(" *   opt == 0 means SUNDIALS function allocates memory so check if\n");
		buffer.append(" *            returned NULL pointer\n");
		buffer.append(" *   opt == 1 means SUNDIALS function returns a flag so check if\n");
		buffer.append(" *            flag >= 0\n");
		buffer.append(" *   opt == 2 means function allocates memory so check if returned\n");
		buffer.append(" *            NULL pointer\n");
		buffer.append(" */\n");
		buffer.append("static int check_flag(void *flagvalue, char *funcname, int opt)\n");
		buffer.append("{\n");
		buffer.append("\tint *errflag;\n\n");

		buffer.append("\t// Check if SUNDIALS function returned NULL pointer - no memory allocated\n");
		buffer.append("\tif (opt == 0 && flagvalue == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tfprintf(stderr, \"\\nSUNDIALS_ERROR: %s() failed - returned NULL pointer\\n\\n\", funcname);\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Check if flag < 0\n");
		buffer.append("\telse if (opt == 1)\n");
		buffer.append("\t{\n");
		buffer.append("\t\terrflag = (int *) flagvalue;\n");
		buffer.append("\t\tif (*errflag < 0)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfprintf(stderr, \"\\nSUNDIALS_ERROR: %s() failed with flag = %d\\n\\n\", funcname, *errflag);\n");
		buffer.append("\t\t\treturn(1);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Check if function returned NULL pointer - no memory allocated\n");
		buffer.append("\telse if (opt == 2 && flagvalue == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tfprintf(stderr, \"\\nMEMORY_ERROR: %s() failed - returned NULL pointer\\n\\n\",funcname);\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\treturn(0);\n");
		buffer.append("}\n\n");

		buffer.append("// Used in dumpData() to format the modified filename\n");
		buffer.append("char *sprintf_alloc(const char *format, ...)\n");
		buffer.append("{\n");
		buffer.append("\tva_list ap;\n");
		buffer.append("\tva_start(ap, format);\n\n");

		buffer.append("\tchar *s;\n");
		buffer.append("\tint len = vsnprintf(NULL, 0, format, ap);\n\n");

		buffer.append("\tif(len < 0)\n");
		buffer.append("\t{\n");
		buffer.append("\t\treturn(NULL);\n");
		buffer.append("\t}\n\n");

		buffer.append("\tif(!(s = malloc(len + 1)))\n");
		buffer.append("\t{\n");
		buffer.append("\t\treturn(NULL);\n");
		buffer.append("\t}\n\n");

		buffer.append("\tvsprintf(s, format, ap);\n\n");

		buffer.append("\treturn(s);\n");
		buffer.append("}\n\n");

		buffer.append("static int getSTM(const char* filename, DlsMat STM)\n");
		buffer.append("{\n");
		buffer.append("\tint i=0, j=0;\n");
		buffer.append("\trealtype tmpVector[NUMBER_OF_RATES*NUMBER_OF_STATES];\n\n");

		buffer.append("\tFILE *pFile;\n\n");

		buffer.append("\tif ((pFile= fopen(filename, \"r\")) == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: STM file could not be read\\n\");\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\twhile (!feof(pFile) && i < NUMBER_OF_RATES * NUMBER_OF_STATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfscanf(pFile,\"%lf\",tmpVector+i);\n");
		buffer.append("\t\t\ti++;\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\tif (pFile!= NULL) fclose(pFile);\n\n");

		buffer.append("\t\tif (i < NUMBER_OF_RATES * NUMBER_OF_STATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tprintf(\"Error Reading STM file. Check your formatting. (%d/%d)\\n\", i, NUMBER_OF_RATES * NUMBER_OF_STATES);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tfor (j=0; j < NUMBER_OF_RATES; j++)\n");
		buffer.append("\t\t\t\tDENSE_ELEM(STM,i,j) = tmpVector[(NUMBER_OF_RATES*i)+j];\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\treturn(0);\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");

		buffer.append("static int getRateConstants(const char* filename, N_Vector RateConstantVector)\n");
		buffer.append("{\n");
		buffer.append("\tint i=0, j=0;\n");
		buffer.append("\trealtype tmpVector[NUMBER_OF_RATES];\n");
		buffer.append("\tFILE *pFile;\n\n");

		buffer.append("\tif ((pFile= fopen(filename, \"r\")) == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: Parameters file could not be read\\n\");\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\twhile (!feof(pFile) && i < NUMBER_OF_RATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfscanf(pFile,\"%lf\",tmpVector+i);\n");
		buffer.append("\t\t\ti++;\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\tif (pFile!= NULL) fclose(pFile);\n\n");

		buffer.append("\t\tif (i < NUMBER_OF_RATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tprintf(\"Error: Insufficient Parameters. (%d/%d)\\n\", i,NUMBER_OF_RATES);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfor (j=0; j<NUMBER_OF_RATES; j++)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tNV_Ith_S(RateConstantVector, j) = tmpVector[j];\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\treturn(0);\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");

		buffer.append("static int getICs(const char* filename, N_Vector StateVector)\n");
		buffer.append("{\n");
		buffer.append("\tint i=0, j=0;\n");
		buffer.append("\trealtype tmpVector[NUMBER_OF_STATES];\n");
		buffer.append("\tFILE *pFile;\n\n");

		buffer.append("\tif ((pFile= fopen(filename, \"r\")) == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: Initial Conditions file could not be read\\n\");\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\twhile (!feof(pFile) && i < NUMBER_OF_STATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfscanf(pFile,\"%lf\",tmpVector+i);\n");
		buffer.append("\t\t\ti++;\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\tif (pFile!= NULL) fclose(pFile);\n\n");

		buffer.append("\t\tif (i < NUMBER_OF_STATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tprintf(\"Error: Insufficient Initial Conditions. (%d/%d)\\n\", i,NUMBER_OF_STATES); \n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfor (j=0; j<NUMBER_OF_STATES; j++)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tNV_Ith_S(StateVector, j) = tmpVector[j];\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\treturn(0);\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");

		buffer.append("// Returns the current file number, or an error if < 0.\n");
		buffer.append("int dumpData(char* pDataFileName, N_Vector x, int xSize, realtype t, int FileCount, int newFile)\n");
		buffer.append("{\n");
		buffer.append("\t// Make sure the function is being called with an acceptable filenumber\n");
		buffer.append("\tif (FileCount < 1)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: dumpData() called with FileCount = %d\\n\", FileCount);\n");
		buffer.append("\t\treturn(-1);\n");
		buffer.append("\t}\n");
		buffer.append("\tif (FileCount > 99)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: File count exceeded, terminating.\\n\");\n");
		buffer.append("\t\treturn(-1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\tint i, j, flag = 0;\n");
		buffer.append("\tlong lastNewline;\n");
		buffer.append("\tchar* pDataFile, openType;\n");
		buffer.append("\tFILE *pData;\n\n");

		buffer.append("\t// append FileCount to filename if >1 (result of first file becoming too large)\n");
		buffer.append("\t// eg filename.dat becomes filename~02.dat, filename~02.dat becomes filename~03.dat\n");
		buffer.append("\tif (FileCount > 1 && FileCount < 100)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tchar *c = strrchr(pDataFileName, \'.\');\n\n");

		buffer.append("\t\tif(!c)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tpDataFile = sprintf_alloc(\"%s~%.2d\", pDataFileName, FileCount);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tj = (int)(c-pDataFileName);\n");
		buffer.append("\t\t\tchar* pTmp = malloc(j+1);\n");
		buffer.append("\t\t\tstrncpy(pTmp, pDataFileName, j+1);\n");
		buffer.append("\t\t\tpTmp[j] = \'\\0\';\n\n");

		buffer.append("\t\t\tif ((pDataFile = sprintf_alloc(\"%s~%.2d%s\", pTmp, FileCount, c)) == NULL) return(-1);\n\n");

		buffer.append("\t\t\tfree(pTmp);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\tpDataFile = pDataFileName;\n");
		buffer.append("\t}\n\n");

		buffer.append("\tif (newFile == 0)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tpData = fopen(pDataFile, \"a\");\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\tpData = fopen(pDataFile, \"w\");\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Try dumping the data\n");
		buffer.append("\tif (pData == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\t// File failed to open.  EXTERMINATE!!\n");
		buffer.append("\t\tprintf (\"Error opening %s at t = %g\\nerrno = %d\\n\", pDataFile, t, errno);\n");
		buffer.append("\t\tif (pDataFile != pDataFileName) free(pDataFile);\n");
		buffer.append("\t\treturn(-1);\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\tif (pDataFile != pDataFileName) free(pDataFile);\n\n");

		buffer.append("\t\tlastNewline = ftell(pData);\n\n");

		buffer.append("\t\t// write the data to the outgoing buffer\n");
		buffer.append("\t\tfor (i=0; i<xSize; i++)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfprintf(pData,\"%g \", NV_Ith_S(x,i));\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\tfprintf(pData,\"\\n\");\n\n");

		buffer.append("\t\t// force attempt to dump the buffer to disk\n");
		buffer.append("\t\tif ((flag = fflush(pData)) == 0)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\t//Data successfully dumped\n");
		buffer.append("\t\t\tfclose(pData);\n");
		buffer.append("\t\t\treturn(FileCount);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\t// need to clear the incomplete line before closing the file\n");
		buffer.append("\t\t\tif ((flag = ftruncate(fileno(pData), lastNewline)) != 0)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tprintf(\"Error scrubbing incomplete data at t = %g\\nErrno = %d\\n\", t, errno);\n");
		buffer.append("\t\t\t}\n\n");

		buffer.append("\t\t\tfclose(pData);\n\n");

		buffer.append("\t\t\t// Attempt to expand output into a new file\n");
		buffer.append("\t\t\tif (errno == 27)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tprintf(\"The current datafile has reached its size limit.  Opening a new file at t = %g.\\n\", t);\n");
		buffer.append("\t\t\t\treturn(dumpData(pDataFileName, x, xSize, t, FileCount+1, 1));\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t\telse\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\t//Dump failed, EXTERMINATE!!\n");
		buffer.append("\t\t\t\tprintf(\"Datadump failed at t = %g\\nErrno = %d\\n\", t, errno);\n");
		buffer.append("\t\t\t\treturn(-1);\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");

		buffer.append("// Solver functions\n");
		buffer.append("\n");


    }

    public void buildMassBalanceBuffer(StringBuffer buffer,Model model_wrapper) throws Exception {
        //this contains the main part of the SUNDIALS code.

		buffer.append("/*\n");
		buffer.append(" * Template written by Robert Dromms\n");
		buffer.append(" * Created 2009-06-19 15:32\n");
		buffer.append(" * Based off example code provided in SUNDIALS documentation: cvRoberts_dns.c\n");
		buffer.append(" */\n\n");

		buffer.append("#include <stdio.h>\n");
		buffer.append("#include <stdarg.h>\n");
		buffer.append("#include <stdlib.h>\n");
		buffer.append("#include <unistd.h>\n");
		buffer.append("#include <math.h>\n");
		buffer.append("#include <time.h>\n");
		buffer.append("#include <errno.h>\n");
		buffer.append("#include <string.h>\n");
		buffer.append("#include <cvode/cvode.h>\t\t// prototypes for CVODE fcts., consts.\n");
		buffer.append("#include <nvector/nvector_serial.h>\t// serial N_Vector types, fcts., macros\n");
		buffer.append("#include <cvode/cvode_spgmr.h>\t\t// prototype for CVSPGMR\n");
		buffer.append("#include <sundials/sundials_direct.h>\t// definitions DlsMat DENSE_ELEM\n");
		buffer.append("#include <sundials/sundials_types.h>\t// definition of type realtype\n\n");

		buffer.append("//PROBLEM SPECIFIC VALUES\n");
		buffer.append("#define TOLERANCE\t\t1e-10\t\t// global error tolerance\n");
		buffer.append("#define NUMBER_OF_STATES\t"+(int)model_wrapper.getNumSpecies()+"\t\t// number of equations\n");
		buffer.append("#define NUMBER_OF_RATES\t\t"+(int)model_wrapper.getNumReactions()+"\t\t// number of parameters\n\n");

		buffer.append("// Functions to grab the kinetics rate constants and ic\'s from files\n");
		buffer.append("static int getRateConstants(const char* filename, N_Vector RateConstantVector);\n");
		buffer.append("static int getICs(const char* filename, N_Vector StateVector);\n");
		buffer.append("static int getSTM(const char* filename, DlsMat STM);\n");
		buffer.append("// Functions called by the solver\n");
		buffer.append("static int MassBalances(realtype t, N_Vector StateVector, N_Vector dxdt, void *user_data);\n");
		buffer.append("static void Kinetics(realtype t, N_Vector x, N_Vector rateConstVector, N_Vector rateVector);\n");
		buffer.append("static int JacTimesVec(N_Vector v, N_Vector Jv, realtype t, N_Vector x, N_Vector fx, void *user_data, N_Vector tmp);\n");
		buffer.append("// Function to dump data to files\n");
		buffer.append("static int dumpData(char* pDataFileName, N_Vector x, int xSize, realtype t, int FileCount, int newFile);\n");
		buffer.append("// Private function to check function return values\n");
		buffer.append("static int check_flag(void *flagvalue, char *funcname, int opt);\n\n");

		buffer.append("struct params\n");
		buffer.append("{\n");
		buffer.append("\tDlsMat pSTM;\n");
		buffer.append("\tN_Vector pRateConstantVector;\n");
		buffer.append("};\n\n");

		buffer.append("int main(int argc, char* const argv[])\n");
		buffer.append("{\n");
		buffer.append("\t/*\n");
		buffer.append("\t * Arguments must be in this order:\n");
		buffer.append("\t * 1. Data output file name\n");
		buffer.append("\t * 2. Kinetics rate constants / parameters file\n");
		buffer.append("\t * 3. Initial conditions file\n");
		buffer.append("\t * 4. STM data file\n");
		buffer.append("\t * 5. Start Time\n");
		buffer.append("\t * 6. End Time\n");
		buffer.append("\t * 7. Time step size\n");
		buffer.append("\t */\n\n");

		buffer.append("\tclock_t start = clock();\n\n");

		buffer.append("\t//Check number of arguments\n");
		buffer.append("\tif (argc != 8)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Incorrect number of arguments.\\n\");\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Prep some variables\n");
		buffer.append("\tint i, nTimes, flag, fileCount = 1;\n");
		buffer.append("\trealtype dblTime, dblTSTOP, dblTs;\n");
		buffer.append("\tN_Vector StateVector;\n");
		buffer.append("\tvoid *cvode_mem;\n");
		buffer.append("\tstruct params Parameters;\n\n");

		buffer.append("\t// assign info from arguments\n");
		buffer.append("\tchar *pOutputDataFile = argv[1];\t\t// Assign data output file\n");
		buffer.append("\tchar *pInputKineticsFile = argv[2];\t\t// Get kinetics datafile name\n");
		buffer.append("\tchar *pInputICFile = argv[3];\t\t\t// Get ic datafile name\n");
		buffer.append("\tchar *pSTMFile = argv[4];\t\t\t// Get STM datafile name\n");
		buffer.append("\tsscanf(argv[5], \"%lf\", &dblTime);\t\t// Start time\n");
		buffer.append("\tsscanf(argv[6], \"%lf\", &dblTSTOP);\t\t// Stop time\n");
		buffer.append("\tsscanf(argv[7], \"%lf\", &dblTs);\t\t\t// Time step size\n\n");

		buffer.append("\t//Allocate N_Vectors, DlsMats\n");
		buffer.append("\tStateVector = N_VNew_Serial(NUMBER_OF_STATES);\n");
		buffer.append("\t\tif (check_flag((void *)StateVector, \"N_VNew_Serial\", 0)) return(1);\n");
		buffer.append("\tParameters.pRateConstantVector = N_VNew_Serial(NUMBER_OF_RATES);\n");
		buffer.append("\t\tif (check_flag((void *)Parameters.pRateConstantVector, \"N_VNew_Serial\", 0)) return(1);\n");
		buffer.append("\tParameters.pSTM = NewDenseMat(NUMBER_OF_STATES, NUMBER_OF_RATES);\n");
		buffer.append("\t\tif (check_flag((void *)Parameters.pSTM, \"NewDenseMat\", 0)) return(1);\n\n");

		buffer.append("\t// Generate timestep array\n");
		buffer.append("\tnTimes = floor((dblTSTOP-dblTime)/dblTs)+1;\n");
		buffer.append("\trealtype TSIM[nTimes];\n");
		buffer.append("\tfor (i = 0; i<nTimes; i++)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tTSIM[i] = dblTime + i*dblTs;\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Load kinetics, IC\'s and STM\n");
		buffer.append("\tflag = getRateConstants(pInputKineticsFile, Parameters.pRateConstantVector);\n");
		buffer.append("\t\tif (flag != 0) return(1);\n");
		buffer.append("\tflag = getICs(pInputICFile, StateVector);\n");
		buffer.append("\t\tif (flag != 0) return(1);\n");
		buffer.append("\tflag = getSTM(pSTMFile, Parameters.pSTM);\n");
		buffer.append("\t\tif (flag != 0) return(1);\n\n");

		buffer.append("\t/********************Set up the ODE solver*******************/\n\n");
		buffer.append("\t// Create ODE solver memory block\n");
		buffer.append("\tcvode_mem = CVodeCreate(CV_BDF, CV_NEWTON);\t// Backward Differentiation, Newton iteration\n");
		buffer.append("\t\tif (check_flag((void *)cvode_mem, \"CVodeCreate\", 0)) return(1);\n");
		buffer.append("\t// Initialize the ODE solver\n");
		buffer.append("\tflag = CVodeInit(cvode_mem, MassBalances, TSIM[0], StateVector);\n");
		buffer.append("\t\tif (check_flag(&flag, \"CVodeInit\", 1)) return(1);\n");
		buffer.append("\t// Pass rate constants, STM to solver\n");
		buffer.append("\tflag = CVodeSetUserData(cvode_mem, &Parameters);\n");
		buffer.append("\t\tif (check_flag(&flag, \"CVodeSetUserData\", 1)) return(1);\n");
		buffer.append("\t// Specifify absolute and relative tolerances\n");
		buffer.append("\tflag = CVodeSStolerances(cvode_mem, TOLERANCE, TOLERANCE);\n");
		buffer.append("\t\tif (check_flag(&flag, \"CVodeSStolerances\", 1)) return(1);\n");
		buffer.append("\t// Call CVSPGMR to specify Krylov Method solver using NO preconditioning\n");
		buffer.append("\tflag = CVSpgmr(cvode_mem, PREC_NONE, NUMBER_OF_STATES);\n");
		buffer.append("\t\tif (check_flag(&flag, \"CVSpgmr\", 1)) return(1);\n");
		buffer.append("\t// Set up Jacobian-times-vector function\n");
		buffer.append("\tflag = CVSpilsSetJacTimesVecFn(cvode_mem, JacTimesVec);\n");
		buffer.append("\t\tif (check_flag(&flag, \"CVodeSStolerances\", 1)) return(1);\n");
        buffer.append("\t// Adjust max internal steps - 0 for default (=500), <0 for unlimited, or >0.\n");
        buffer.append("\tflag = CVodeSetMaxNumSteps(cvode_mem, -1);\n");
        buffer.append("\tif (check_flag(&flag, \"CVodeSetMaxNumSteps\", 1)) return(1);\n\n");

		buffer.append("\t/*****************ODE Solver Setup Complete!*****************/\n\n");
		buffer.append("\t// Dump intitial state to output\n");
		buffer.append("\tfileCount = dumpData(pOutputDataFile, StateVector, NUMBER_OF_STATES, TSIM[0], fileCount, 1);\n");
		buffer.append("\tif (fileCount <1)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error dumping data at t = %g\\n\", TSIM[0]);\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Main ODE Solver loop\n");
		buffer.append("\tfor (i=0; i < nTimes-1; i++)\n");
		buffer.append("\t{\n");
		buffer.append("\t\t// Calculate one step of the ODE Solution\n");
		buffer.append("\t\tflag = CVode(cvode_mem, TSIM[i+1], StateVector, TSIM+i, CV_NORMAL);\n\n");

		buffer.append("\t\tif (check_flag(&flag, \"CVode\", 1))\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tprintf(\"ODE Solver loop failed at t = %g\\n\", TSIM[i]);\n");
		buffer.append("\t\t\treturn(1);\n");
		buffer.append("\t\t}\n\n");

		buffer.append("\t\tif (flag == CV_SUCCESS)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\t//Dump Current state to output files\n");
		buffer.append("\t\t\tfileCount = dumpData(pOutputDataFile, StateVector, NUMBER_OF_STATES, TSIM[i], fileCount, 0);\n");
		buffer.append("\t\t\tif (fileCount <1)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tprintf(\"Error dumping data at t = %g\\n\", TSIM[i]);\n");
		buffer.append("\t\t\t\treturn(1);\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Free N_Vectors\n");
		buffer.append("\tN_VDestroy_Serial(StateVector);\n");
		buffer.append("\tN_VDestroy_Serial(Parameters.pRateConstantVector);\n");
		buffer.append("\tDestroyMat(Parameters.pSTM);\n\n");

		buffer.append("\t// Free integrator memory\n");
		buffer.append("\tCVodeFree(&cvode_mem);\n\n");

		buffer.append("\tprintf(\"Time elapsed: %f\\n\", ((double)clock() - start) / CLOCKS_PER_SEC);\n\n");

		buffer.append("\treturn(0);\n");
		buffer.append("}\n\n");

		buffer.append("/* This function comes directly from the example code cvRoberts_dns.c included with SUNDIALS.\n");
		buffer.append(" * Check function return value...\n");
		buffer.append(" *   opt == 0 means SUNDIALS function allocates memory so check if\n");
		buffer.append(" *            returned NULL pointer\n");
		buffer.append(" *   opt == 1 means SUNDIALS function returns a flag so check if\n");
		buffer.append(" *            flag >= 0\n");
		buffer.append(" *   opt == 2 means function allocates memory so check if returned\n");
		buffer.append(" *            NULL pointer\n");
		buffer.append(" */\n");
		buffer.append("static int check_flag(void *flagvalue, char *funcname, int opt)\n");
		buffer.append("{\n");
		buffer.append("\tint *errflag;\n\n");

		buffer.append("\t// Check if SUNDIALS function returned NULL pointer - no memory allocated\n");
		buffer.append("\tif (opt == 0 && flagvalue == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tfprintf(stderr, \"\\nSUNDIALS_ERROR: %s() failed - returned NULL pointer\\n\\n\", funcname);\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Check if flag < 0\n");
		buffer.append("\telse if (opt == 1)\n");
		buffer.append("\t{\n");
		buffer.append("\t\terrflag = (int *) flagvalue;\n");
		buffer.append("\t\tif (*errflag < 0)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfprintf(stderr, \"\\nSUNDIALS_ERROR: %s() failed with flag = %d\\n\\n\", funcname, *errflag);\n");
		buffer.append("\t\t\treturn(1);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Check if function returned NULL pointer - no memory allocated\n");
		buffer.append("\telse if (opt == 2 && flagvalue == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tfprintf(stderr, \"\\nMEMORY_ERROR: %s() failed - returned NULL pointer\\n\\n\",funcname);\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\treturn(0);\n");
		buffer.append("}\n\n");

		buffer.append("// Used in dumpData() to format the modified filename\n");
		buffer.append("char *sprintf_alloc(const char *format, ...)\n");
		buffer.append("{\n");
		buffer.append("\tva_list ap;\n");
		buffer.append("\tva_start(ap, format);\n\n");

		buffer.append("\tchar *s;\n");
		buffer.append("\tint len = vsnprintf(NULL, 0, format, ap);\n\n");

		buffer.append("\tif(len < 0)\n");
		buffer.append("\t{\n");
		buffer.append("\t\treturn(NULL);\n");
		buffer.append("\t}\n\n");

		buffer.append("\tif(!(s = malloc(len + 1)))\n");
		buffer.append("\t{\n");
		buffer.append("\t\treturn(NULL);\n");
		buffer.append("\t}\n\n");

		buffer.append("\tvsprintf(s, format, ap);\n\n");

		buffer.append("\treturn(s);\n");
		buffer.append("}\n\n");

		buffer.append("static int getSTM(const char* filename, DlsMat STM)\n");
		buffer.append("{\n");
		buffer.append("\tint i=0, j=0;\n");
		buffer.append("\trealtype tmpVector[NUMBER_OF_RATES*NUMBER_OF_STATES];\n\n");

		buffer.append("\tFILE *pFile;\n\n");
		buffer.append("\tif ((pFile= fopen(filename, \"r\")) == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: STM file could not be read\\n\");\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\twhile (!feof(pFile) && i < NUMBER_OF_RATES * NUMBER_OF_STATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfscanf(pFile,\"%lf\",tmpVector+i);\n");
		buffer.append("\t\t\ti++;\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\tif (pFile!= NULL) fclose(pFile);\n\n");

		buffer.append("\t\tif (i < NUMBER_OF_RATES * NUMBER_OF_STATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tprintf(\"Error Reading STM file. Check your formatting. (%d/%d)\\n\", i, NUMBER_OF_RATES * NUMBER_OF_STATES);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tfor (j=0; j < NUMBER_OF_RATES; j++)\n");
		buffer.append("\t\t\t\tDENSE_ELEM(STM,i,j) = tmpVector[(NUMBER_OF_RATES*i)+j];\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\treturn(0);\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");

		buffer.append("static int getRateConstants(const char* filename, N_Vector RateConstantVector)\n");
		buffer.append("{\n");
		buffer.append("\tint i=0, j=0;\n");
		buffer.append("\trealtype tmpVector[NUMBER_OF_RATES];\n");
		buffer.append("\tFILE *pFile;\n\n");

		buffer.append("\tif ((pFile= fopen(filename, \"r\")) == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: Parameters file could not be read\\n\");\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\twhile (!feof(pFile) && i < NUMBER_OF_RATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfscanf(pFile,\"%lf\",tmpVector+i);\n");
		buffer.append("\t\t\ti++;\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\tif (pFile!= NULL) fclose(pFile);\n\n");

		buffer.append("\t\tif (i < NUMBER_OF_RATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tprintf(\"Error: Insufficient Parameters. (%d/%d)\\n\", i,NUMBER_OF_RATES);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfor (j=0; j<NUMBER_OF_RATES; j++)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tNV_Ith_S(RateConstantVector, j) = tmpVector[j];\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\treturn(0);\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");

		buffer.append("static int getICs(const char* filename, N_Vector StateVector)\n");
		buffer.append("{\n");
		buffer.append("\tint i=0, j=0;\n");
		buffer.append("\trealtype tmpVector[NUMBER_OF_STATES];\n");
		buffer.append("\tFILE *pFile;\n\n");

		buffer.append("\tif ((pFile= fopen(filename, \"r\")) == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: Initial Conditions file could not be read\\n\");\n");
		buffer.append("\t\treturn(1);\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\twhile (!feof(pFile) && i < NUMBER_OF_STATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfscanf(pFile,\"%lf\",tmpVector+i);\n");
		buffer.append("\t\t\ti++;\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\tif (pFile!= NULL) fclose(pFile);\n\n");

		buffer.append("\t\tif (i < NUMBER_OF_STATES)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tprintf(\"Error: Insufficient Initial Conditions. (%d/%d)\\n\", i,NUMBER_OF_STATES); \n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfor (j=0; j<NUMBER_OF_STATES; j++)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tNV_Ith_S(StateVector, j) = tmpVector[j];\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\treturn(0);\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");

		buffer.append("// Returns the current file number, or an error if < 0.\n");
		buffer.append("int dumpData(char* pDataFileName, N_Vector x, int xSize, realtype t, int FileCount, int newFile)\n");
		buffer.append("{\n");
		buffer.append("\t// Make sure the function is being called with an acceptable filenumber\n");
		buffer.append("\tif (FileCount < 1)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: dumpData() called with FileCount = %d\\n\", FileCount);\n");
		buffer.append("\t\treturn(-1);\n");
		buffer.append("\t}\n");
		buffer.append("\tif (FileCount > 99)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tprintf(\"Error: File count exceeded, terminating.\\n\");\n");
		buffer.append("\t\treturn(-1);\n");
		buffer.append("\t}\n\n");

		buffer.append("\tint i, j, flag = 0;\n");
		buffer.append("\tlong lastNewline;\n");
		buffer.append("\tchar* pDataFile, openType;\n");
		buffer.append("\tFILE *pData;\n\n");

		buffer.append("\t// append FileCount to filename if >1 (result of first file becoming too large)\n");
		buffer.append("\t// eg filename.dat becomes filename~02.dat, filename~02.dat becomes filename~03.dat\n");
		buffer.append("\tif (FileCount > 1 && FileCount < 100)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tchar *c = strrchr(pDataFileName, \'.\');\n\n");

		buffer.append("\t\tif(!c)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tpDataFile = sprintf_alloc(\"%s~%.2d\", pDataFileName, FileCount);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tj = (int)(c-pDataFileName);\n");
		buffer.append("\t\t\tchar* pTmp = malloc(j+1);\n");
		buffer.append("\t\t\tstrncpy(pTmp, pDataFileName, j+1);\n");
		buffer.append("\t\t\tpTmp[j] = \'\\0\';\n\n");

		buffer.append("\t\t\tif ((pDataFile = sprintf_alloc(\"%s~%.2d%s\", pTmp, FileCount, c)) == NULL) return(-1);\n\n");

		buffer.append("\t\t\tfree(pTmp);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\tpDataFile = pDataFileName;\n");
		buffer.append("\t}\n\n");

		buffer.append("\tif (newFile == 0)\n");
		buffer.append("\t{\n");
		buffer.append("\t\tpData = fopen(pDataFile, \"a\");\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\tpData = fopen(pDataFile, \"w\");\n");
		buffer.append("\t}\n\n");

		buffer.append("\t// Try dumping the data\n");
		buffer.append("\tif (pData == NULL)\n");
		buffer.append("\t{\n");
		buffer.append("\t\t// File failed to open.  EXTERMINATE!!\n");
		buffer.append("\t\tprintf (\"Error opening %s at t = %g\\nerrno = %d\\n\", pDataFile, t, errno);\n");
		buffer.append("\t\tif (pDataFile != pDataFileName) free(pDataFile);\n");
		buffer.append("\t\treturn(-1);\n");
		buffer.append("\t}\n");
		buffer.append("\telse\n");
		buffer.append("\t{\n");
		buffer.append("\t\tif (pDataFile != pDataFileName) free(pDataFile);\n\n");

		buffer.append("\t\tlastNewline = ftell(pData);\n\n");

		buffer.append("\t\t// write the data to the outgoing buffer\n");
		buffer.append("\t\tfor (i=0; i<xSize; i++)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\tfprintf(pData,\"%g \", NV_Ith_S(x,i));\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\tfprintf(pData,\"\\n\");\n\n");

		buffer.append("\t\t// force attempt to dump the buffer to disk\n");
		buffer.append("\t\tif ((flag = fflush(pData)) == 0)\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\t//Data successfully dumped\n");
		buffer.append("\t\t\tfclose(pData);\n");
		buffer.append("\t\t\treturn(FileCount);\n");
		buffer.append("\t\t}\n");
		buffer.append("\t\telse\n");
		buffer.append("\t\t{\n");
		buffer.append("\t\t\t// need to clear the incomplete line before closing the file\n");
		buffer.append("\t\t\tif ((flag = ftruncate(fileno(pData), lastNewline)) != 0)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tprintf(\"Error scrubbing incomplete data at t = %g\\nErrno = %d\\n\", t, errno);\n");
		buffer.append("\t\t\t}\n\n");

		buffer.append("\t\t\tfclose(pData);\n\n");

		buffer.append("\t\t\t// Attempt to expand output into a new file\n");
		buffer.append("\t\t\tif (errno == 27)\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\tprintf(\"The current datafile has reached its size limit.  Opening a new file at t = %g.\\n\", t);\n");
		buffer.append("\t\t\t\treturn(dumpData(pDataFileName, x, xSize, t, FileCount+1, 1));\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t\telse\n");
		buffer.append("\t\t\t{\n");
		buffer.append("\t\t\t\t//Dump failed, EXTERMINATE!!\n");
		buffer.append("\t\t\t\tprintf(\"Datadump failed at t = %g\\nErrno = %d\\n\", t, errno);\n");
		buffer.append("\t\t\t\treturn(-1);\n");
		buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");

		buffer.append("// Solver functions\n");
		buffer.append(" \n");
		buffer.append("\n");

    }

    public void buildBuildFileBuffer(StringBuffer driver,LoadXMLPropFile xmlPropTree) throws Exception {

//      driver.append("gcc -std=c99 -pedantic -Wall -Wextra -O2 -o modelCode Model.c -L/usr/local/lib -lsundials_cvode -lsundials_nvecserial -lm\n");
//      driver.append("gcc -std=c99 -pedantic -Wall -Wextra -O2 -o sensitivityCode Sensitivity.c -L/usr/local/lib -lsundials_cvode -lsundials_nvecserial -lm\n");
        
    	// Ok, so we need to get the exename from the modelname -
    	String strModelName = getFullFileName("//MassBalanceFunction/massbalance_filename/text()",xmlPropTree);
    	String strExeName = getFileName("//MassBalanceFunction/massbalance_filename/text()",xmlPropTree);
    	
    	driver.append("gcc -o ");
    	driver.append(strExeName);
    	driver.append(" ");
    	driver.append(strModelName);
    	driver.append(" ");
    	driver.append("-L/usr/local/lib -I/usr/local/include -lsundials_cvode -lsundials_nvecserial -lm\n");
        
        // driver.append("gcc -o sensitivityCode Sensitivity.c -L/usr/local/lib -I/usr/local/include -lsundials_cvode -lsundials_nvecserial -lm\n");
    }
    
    public void buildSolveAdjBalBuffer(StringBuffer driver) throws Exception {

        driver.append("// ODE RHS function\n");
        driver.append("static int AdjBalances(realtype t, N_Vector x, N_Vector dxdt, void *user_data)\n");
        driver.append("{\n");
        driver.append("\t// Prep some stuff\n");
        driver.append("\tint i, j, k;\n");
        driver.append("\trealtype tmp;\n");
        driver.append("\tstruct params* Parameters = user_data;\n");
        driver.append("\tN_Vector rateVector;\n");
        driver.append("\tDlsMat Jacobian, PMatrix;\n\n");

        driver.append("\t// Allocate memory\n");
        driver.append("\trateVector = N_VNew_Serial(NUMBER_OF_RATES);\n");
        driver.append("\t\tif (check_flag((void *)rateVector, \"N_VNew_Serial\", 0)) return(1);\n");
        driver.append("\tJacobian = NewDenseMat(NUMBER_OF_STATES, NUMBER_OF_STATES);\n");
        driver.append("\t\tif (check_flag((void *)Jacobian, \"NewDenseMat\", 0)) return(1);\n");
        driver.append("\tPMatrix = NewDenseMat(NUMBER_OF_STATES, NUMBER_OF_RATES);\n");
        driver.append("\t\tif (check_flag((void *)PMatrix, \"NewDenseMat\", 0)) return(1);\n\n");

        driver.append("\t// Grab the kinetics\n");
        driver.append("\tKinetics(t, x, (Parameters->pRateConstantVector), rateVector);\n\n");

        driver.append("\t// Calculate dx/dt\n");
        driver.append("\tfor (i=0; i< NUMBER_OF_STATES; i++)\n");
        driver.append("\t{\n");
        driver.append("\t\ttmp = 0;\n");
        driver.append("\t\tfor (j=0; j < NUMBER_OF_RATES; j++)\n");
        driver.append("\t\t{\n");
        driver.append("\t\t\ttmp += DENSE_ELEM(Parameters->pSTM,i,j) * NV_Ith_S(rateVector,j);\n");
        driver.append("\t\t}\n");
        driver.append("\t\tNV_Ith_S(dxdt, i) = tmp;\n");
        driver.append("\t}\n\n");

        driver.append("\t// Find dS/dt matrix\n");
        driver.append("\tJac(NUMBER_OF_STATES, t, x, dxdt, Jacobian, user_data, NULL, NULL, NULL);\n");
        driver.append("\tcalculatePMatrix((Parameters->pRateConstantVector), x, PMatrix);\n\n");

        driver.append("\tcalcDSDT(x, dxdt, Jacobian, PMatrix);\n\n");

/*      Obsoleted by calcDSDT
        driver.append("\t// DSDT=J*S+P;\n");
        driver.append("\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
        driver.append("\t{\n");
        driver.append("\t\tfor (j=0; j<NUMBER_OF_RATES; j++)\n");
        driver.append("\t\t{\n");
        driver.append("\t\t\ttmp = 0.0;\n");
        driver.append("\t\t\tfor (k=0; k<NUMBER_OF_STATES; k++)\n");
        driver.append("\t\t\t{\n");
        driver.append("\t\t\t\ttmp += DENSE_ELEM(Jacobian,i,k) * NV_Ith_S(x,NUMBER_OF_STATES+k*NUMBER_OF_RATES+j);\n");
        driver.append("\t\t\t}\n");
        driver.append("\t\tNV_Ith_S(dxdt,NUMBER_OF_STATES+i*NUMBER_OF_RATES+j) = tmp + DENSE_ELEM(PMatrix,i,j);\n");
        driver.append("\t\t}\n");
        driver.append("\t}\n\n");
 */

        driver.append("\t// Free up memory\n");
        driver.append("\tN_VDestroy(rateVector);\n");
        driver.append("\tDestroyMat(Jacobian);\n");
        driver.append("\tDestroyMat(PMatrix);\n\n");

        driver.append("\treturn(0);\n");
        driver.append("}\n\n");

    }

    public void buildDataFileBuffer(StringBuffer buffer) throws Exception {
        //currently unused in SUNDIALS.

        //Use this to dump my kinetics/ic files?

        // Ok, build the data file. This is the same as Matlab -

        // Setup matlabModel and call the buildDataFile method -
        // matlabModel.setAlphabetVector(this._alphabetVector);
        // matlabModel.setProperties(this._propTable);
        // matlabModel.setReactionVector(this._rxnVector);
        // matlabModel.setRowWrapperVector(this._vecRowWrappers);

        // Build the buffer -
        // matlabModel.buildDataFileBuffer(buffer);
    }

    private String getFileName(String xpath,LoadXMLPropFile xmlPropTree)
    {
    	String strTmpRaw = "";
    	
    	strTmpRaw = xmlPropTree.getProperty(xpath);
    	int INT_2_DOT = strTmpRaw.indexOf(".");
    	String strName = strTmpRaw.substring(0, INT_2_DOT);
    	
    	return(strName);
    }
    
    private String getFullFileName(String xpath,LoadXMLPropFile xmlPropTree)
    {
    	String strTmpRaw = "";
    	strTmpRaw = xmlPropTree.getProperty(xpath);	
    	return(strTmpRaw);
    }
    
    public void buildShellCommand(StringBuffer buffer,LoadXMLPropFile xmlPropTree) throws Exception {
        
    	// Ok, so we need to get a bunch of stuff from the propfile -
    	String strExeName = getFileName("//MassBalanceFunction/massbalance_filename/text()",xmlPropTree);
    	String strParameters = getFullFileName("//KineticParametersFileName/kineticparameters_filename/text()",xmlPropTree);
    	String strInitialCondtions = getFullFileName("//InitialConditionFileName/initialcondition_filename/text()",xmlPropTree);
    	String strSTMatrix = getFullFileName("//StoichiometricMatrix/stoichiometric_matrix_filename/text()",xmlPropTree);
    	String strOutputName = getFullFileName("//OutputFileName/output_filename/text()",xmlPropTree);
    	
    	// Dumps RunModel.sh command
    	buffer.append("./");
    	buffer.append(strExeName);
    	buffer.append(" ");
    	buffer.append(strOutputName);
    	buffer.append(" ");
    	buffer.append(strParameters);
    	buffer.append(" ");
    	buffer.append(strInitialCondtions);
    	buffer.append(" ");
    	buffer.append(strSTMatrix);
    	buffer.append(" $1 $2 $3\n");
    	
        // buffer.append("./modelCode output.dat kinetics.dat ic.dat stm.dat $1 $2 $3\n");
    }


    // uses Jacobian and PMatrix to find DSDT   (B)
    public void buildDSDTBuffer(StringBuffer buffer) throws Exception
    {
        // uses Jacobian and PMatrix to find DSDT   (B)

        //currently not used in SUNDIALS.

        // Convert into string buffer -
        buffer.append("void calculateDSDT(int NSTATES, int NRATES, Matrix& SC,ColumnVector& k, ColumnVector& x, Matrix& DSDT)\n");
        buffer.append("{\n");
        buffer.append("\t// Machine generated matrix to solve for time\n");
        buffer.append("\t// derivative of the sensitivity matrix.\n");
        buffer.append("\tMatrix J = Matrix(NSTATES,NSTATES); // jacobian matrix df/dx\n");
        buffer.append("\tMatrix P = Matrix(NSTATES,NRATES); // pmatrix df/dp\n");
        buffer.append("\tcalculateJacobian(NSTATES, k, x, J);\n");
        buffer.append("\tcalculatePMatrix(NSTATES, NRATES, k, x, P);\n");
        buffer.append("\tDSDT=J*SC+P;\n");
        buffer.append("}\n");

    }

    public void buildJacobianBuffer(StringBuffer buffer,Model model_wrapper,Vector vecReactions) throws Exception
    {
        // Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)vecReactions.size();
        

        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions);
        
        // Ok, when I get here I have the stoichiometric matrix -
        // Initialize the array the jacobian array -
        String[][] strJacobian = new String[NROWS][NROWS];
        for (int counter_outer=0;counter_outer<NROWS;counter_outer++)
        {
            for (int counter_inner=0;counter_inner<NROWS;counter_inner++)
            {
                strJacobian[counter_outer][counter_inner]="0.0";
            }
        }

        StringBuffer tmpBuffer = new StringBuffer();
        Vector<String> vecConnect = new Vector<String>();
        Vector<String> vecSpeciesRate = new Vector<String>();
        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // put jacobian logic here -
                strJacobian[state_counter_outer][state_counter_inner]=formulateJacobianElement(matrix,state_counter_outer,state_counter_inner,model_wrapper);
            }
        }

        // Ok, so when I get here I have the Jacobian - we need to convert it into a string buffer
        buffer.append("\n");
        buffer.append("static int Jac(int N, realtype t, N_Vector x, N_Vector fx, DlsMat J, void *user_data, N_Vector tmp1, N_Vector tmp2, N_Vector tmp3)\n");
        buffer.append("{\n");
        buffer.append("\tint i, j;\n");
        buffer.append("\tN_Vector k;\n");
        buffer.append("\tstruct params* Parameters = user_data;\n");
        buffer.append("\tk = Parameters->pRateConstantVector;\n\n");

        buffer.append("\tfor (i=0;i<NUMBER_OF_STATES;i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tfor (j=0;j<NUMBER_OF_STATES;j++)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tDENSE_ELEM(J,i,j) = 0;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t}\n\n");

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

        buffer.append("\n\treturn(0);\n");
        buffer.append("}\n\n");


        // Function for Model.c solver methods using krylov subspaces
        buffer.append("static int JacTimesVec(N_Vector v, N_Vector Jv, realtype t, N_Vector x, N_Vector fx, void *user_data, N_Vector tmp)\n");
        buffer.append("{\n");
        buffer.append("\tN_Vector k;\n");
        buffer.append("\tstruct params* Parameters = user_data;\n");
        buffer.append("\tk = Parameters->pRateConstantVector;\n\n");

        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            buffer.append("\tNV_Ith_S(Jv,");
            buffer.append(state_counter_outer);
            buffer.append(") =");
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // skip this entry if it is zero
                if(!strJacobian[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\n\t\t("+strJacobian[state_counter_outer][state_counter_inner]+")");
                    buffer.append("*NV_Ith_S(v,");
                    buffer.append(state_counter_inner);
                    buffer.append(") +");
                }
            }
            buffer.append(" 0;\n");
        }
        buffer.append("\treturn(0);\n");
        buffer.append("}\n\n");

        buffer.append("// DSDT RHS function\n");
        buffer.append("static void calcDSDT(N_Vector x, N_Vector dxdt, DlsMat Jac, DlsMat PMat)\n");
        buffer.append("{\n");
        buffer.append("\tint j;\n\n");

        buffer.append("\t for (j=0; j<NUMBER_OF_RATES; j++)\n");
        buffer.append("\t{\n");

        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            buffer.append("\t\tNV_Ith_S(dxdt, NUMBER_OF_STATES+");
            buffer.append(state_counter_outer);
            buffer.append("*NUMBER_OF_RATES+j) =");
            for (int state_counter_inner=0;state_counter_inner<NROWS;state_counter_inner++)
            {
                // skip this entry if it is zero
                if(!strJacobian[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\n\t\t\t(DENSE_ELEM(Jac, "+state_counter_outer+", "+state_counter_inner+")");
                    buffer.append("*NV_Ith_S(x, NUMBER_OF_STATES+");
                    buffer.append(state_counter_inner);
                    buffer.append("*NUMBER_OF_RATES+j)) +");
                }
            }
            buffer.append("\n\t\t\tDENSE_ELEM(PMat, "+state_counter_outer+", j);\n");
        }

        buffer.append("\t}\n");
        buffer.append("}\n");
    }


    public void buildPMatrixBuffer(StringBuffer buffer,Model model_wrapper,Vector vecReactions) throws Exception
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
                strPMatrix[counter_outer][counter_inner]=formulatePMatrixElement(matrix,counter_outer,counter_inner,model_wrapper);
            }
        }

        // Convert into string buffer -
        buffer.append("static void calculatePMatrix(N_Vector k, N_Vector x, DlsMat PM)\n");
        buffer.append("{\n");
        buffer.append("\t// Machine generated dfdp matrix.\n\n");

        buffer.append("\tint i,j;\n\n");

        buffer.append("\tfor (i=0;i<NUMBER_OF_RATES;i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tfor (j=0;j<NUMBER_OF_STATES;j++)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tDENSE_ELEM(PM,i,j) = 0;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t}\n\n");

        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<(NROWS+NCOLS);state_counter_inner++)
            {
                // if it is a zero entry, just skip it
                if(!strPMatrix[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\tDENSE_ELEM(PM,");
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

    private String formulatePMatrixElement(double[][] matrix,int massbalance,int parameter,Model model_wrapper)
    {
        StringBuffer buffer = new StringBuffer();
        String rString = "0.0";

        // Get the dimension of the system -
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
                        buffer.append("NV_Ith_S(x,");
                        buffer.append(state_counter);
                        buffer.append(")*");
                    }
                    // any thing else I need to raise to a power
                    else {
                        buffer.append("pow(NV_Ith_S(x,");
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

    private String formulateJacobianElement(double[][] matrix,int massbalance,int state,Model model_wrapper)
    {
        StringBuffer buffer = new StringBuffer();
        String rString = "";

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
        if (rString.lastIndexOf(" + ")==rString.length()-1)
        {
            rString=rString.substring(0,rString.length()-1);
        }



// return the buffer -
        return(rString);
    }

}
