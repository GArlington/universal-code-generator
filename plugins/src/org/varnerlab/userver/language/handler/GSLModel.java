/*
 * GSLModel.java
 *
 * Created on March some-odd?? 2009
 * Modified July 8 2009
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
import org.sbml.libsbml.Species;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

/**
 *
 * @author jeffreyvarner
 */
public class GSLModel {
	// Class/instance attributes -
	private XMLPropTree _xmlPropTree = null;
	private Model model_wrapper = null;
	
    /** Creates a new instance of GSLModel */
    public GSLModel() {
    }
    
	public void setPropertyTree(XMLPropTree prop)
	{
		_xmlPropTree = prop;
	}
	
	public void setModel(Model model)
	{
		model_wrapper = model;
	}


    public void buildMassBalanceEquations(StringBuffer buffer) throws Exception {
        // Ok, so we need to build the buffer with the mass balance equations in it -

        buffer.append("static int MassBalances(double t,const double x[],double f[],void * user_data)\n");
        buffer.append("{\n");
        buffer.append("	// Prep some stuff\n");
        buffer.append("	int i, j;\n");
        buffer.append("	double tmp;\n");
        buffer.append("	struct params* Parameters = (struct params *) user_data;\t// Data Struct w/ STM, rates\n");
        buffer.append("	gsl_vector *pRateVector = gsl_vector_alloc(NUMBER_OF_RATES);\t// The kinetics vector\n");
        buffer.append("	gsl_vector *pDXDT = gsl_vector_alloc(NUMBER_OF_STATES);\t\t//The right-hand side of the mass balances\n\n");

        buffer.append("	// Call the kinetics function\n");
        buffer.append("	Kinetics(t, x, Parameters->pRateConstantVector, pRateVector);\n\n");

        buffer.append("	// Calculate DXDT -\n");
        buffer.append("	gsl_blas_dgemv(CblasNoTrans,1.0,Parameters->pSTM,pRateVector,0.0,pDXDT);\n");
        buffer.append("	// Populate the f[] term\n");
        buffer.append("	for(i=0; i < NUMBER_OF_STATES; i++)\n");
        buffer.append("	{\n");
        buffer.append("		f[i]=gsl_vector_get(pDXDT,i);\n");
        buffer.append("	}\n\n");

        buffer.append("	// Free allocated mem -\n");
        buffer.append("	gsl_vector_free(pRateVector);\n");
        buffer.append("	gsl_vector_free(pDXDT);\n");
        buffer.append("	return(GSL_SUCCESS);\n");
        buffer.append("}\n\n");
    }



    public void buildKineticsBuffer(StringBuffer buffer,Vector vecReactions,Vector<Species> vecSpecies) throws Exception {
        // Ok, build the kinetics -

    	// First things first - get the size of the system -
        int NUMBER_OF_SPECIES = (int)vecSpecies.size(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
        
        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions,vecSpecies);

        // Now the fun begins -
        buffer.append("static void Kinetics(double t, const double x[], gsl_vector *pRateConstantVector, gsl_vector *pRateVector)\n");
        buffer.append("{\n");
        buffer.append("\tdouble dblTmp=0.0;\n\n");

        buffer.append("\t// Put the x's in terms of symbols, helps with debugging\n");    
        // look through the sorted alpha -
        ListOfSpecies alphabetList = model_wrapper.getListOfSpecies();
        for (int alpha_index=0;alpha_index<NUMBER_OF_SPECIES;alpha_index++)
        {
            // Ok, so put this in the buffer -
            buffer.append("\t// ");
            buffer.append(alphabetList.get((long)alpha_index).getName());
            buffer.append("=x(");
            buffer.append(alpha_index);
            buffer.append(");\n");
        }
        buffer.append("\n");

        buffer.append("\t// Calculate the rate vector\n");
        // Build the kinetics -
        for (int col_index=0;col_index<NUMBER_OF_RATES;col_index++)
        {
            // Tmp vector -
            Vector tmp = new Vector();
            for (int row_index=0;row_index<NUMBER_OF_SPECIES;row_index++)
            {
                // Go through each row and find the mij < 0
                if (matrix[row_index][col_index]<0)
                {
                    tmp.addElement(row_index);
                }
            }

            // Ok, so I've figures out what rows we have to worry about -
            buffer.append("\tdblTmp=gsl_vector_get(pRateConstantVector,");
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
                    buffer.append("*pow(x[");
                    buffer.append(local_index);
                    buffer.append("],");
                    buffer.append(-1*dblCoeff);
                    buffer.append(")");
                }
                else
                {
                    buffer.append("*x[");
                    buffer.append(local_index);
                    buffer.append("]");
                }

            }

            buffer.append(";\n\tgsl_vector_set(pRateVector,");
            buffer.append(col_index);
            buffer.append(",dblTmp); \n\n");

        }

        // Ok, so when I get here, then I can add the return line -
        buffer.append("}\n\n");
    }

    // Calculates using the Jacobian and PMatrix   (B)

    public void buildAdjBalFntBuffer(StringBuffer buffer) throws Exception {
        // Method attributes -
    	
    	// First things first - get the size of the system -
        int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
        
    	// Build the buffer -
    	buffer.append("/*\n");
        buffer.append(" * Created 2009-08-05 18:15\n");
        buffer.append(" * Written by Robert Dromms\n");
        buffer.append(" */\n\n");

        buffer.append("#include <stdio.h>\n");
        buffer.append("#include <math.h>\n");
        buffer.append("#include <time.h>\n");
        buffer.append("#include <gsl/gsl_errno.h>\n");
        buffer.append("#include <gsl/gsl_matrix.h>\n");
        buffer.append("#include <gsl/gsl_odeiv.h>\n");
        buffer.append("#include <gsl/gsl_vector.h>\n");
        buffer.append("#include <gsl/gsl_blas.h>\n\n");

        buffer.append("//PROBLEM SPECIFIC VALUES\n");
        buffer.append("#define TOLERANCE\t\t1e-10\t\t// global error tolerance\n");
        buffer.append("#define NUMBER_OF_STATES\t"+NUMBER_OF_SPECIES+"\t\t// number of equations\n");
        buffer.append("#define NUMBER_OF_RATES\t\t"+NUMBER_OF_RATES+"\t\t// number of parameters\n");
        buffer.append("#define TOTAL_ADJ_STATES\t"+(NUMBER_OF_SPECIES * (NUMBER_OF_RATES+1))+"\t\t// Number of Adjoined States\n\n");

        buffer.append("// Functions to grab the kinetics rate constants and ic's from files\n");
        buffer.append("static void getRateConstants(const char* pFilename, gsl_vector *pRateConstantVector);\n");
        buffer.append("static void getICs(const char* pFilename, double x[]);\n");
        buffer.append("static void getSTM(const char* pFilename, gsl_matrix *pSTM);\n\n");

        buffer.append("// Functions called by the solver\n");
        buffer.append("static int AdjBalances(double t, const double x[], double f[], void * user_data);\n");
        buffer.append("static int Jacobian(double t, const double x[], double * dfdx, double dfdt[], void * user_data);\n");
        buffer.append("static void calculatePMatrix(gsl_vector *k, const double x[], gsl_matrix *PM);\n");
        buffer.append("static void calcDSDT(const double x[], double dxdt[], const double Jac[], gsl_matrix *PMat);\n");
        buffer.append("static void Kinetics(double t, const double x[], gsl_vector *pRateConstantVector, gsl_vector *pRateVector);\n\n");

        buffer.append("struct params\n");
        buffer.append("{\n");
        buffer.append("	gsl_matrix *pSTM;\n");
        buffer.append("	gsl_vector *pRateConstantVector;\n");
        buffer.append("};\n\n");

        buffer.append("int main(int argc, char* const argv[])\n");
        buffer.append("{\n");
        buffer.append("	/*\n");
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
        buffer.append("\tint i, j, nTimes;\n");
        buffer.append("\tdouble lastTime, dblTime, dblTSTOP, dblTs, tmp, adjStateVector[TOTAL_ADJ_STATES], OldStateVector[TOTAL_ADJ_STATES];\n");
        buffer.append("\tFILE *pDataFile;\n");
        buffer.append("\tstruct params Parameters;\n\n");

        buffer.append("\t// assign info from arguments\n");
        buffer.append("\tchar *pOutputDataFile = argv[1];\t\t// Assign data output file\n");
        buffer.append("\tchar *pInputKineticsFile = argv[2];\t\t// Get kinetics datafile name\n");
        buffer.append("\tchar *pInputICFile = argv[3];\t\t\t// Get ic datafile name\n");
        buffer.append("\tchar *pSTMFile = argv[4];\t\t\t// Get STM datafile name\n");
        buffer.append("\tsscanf(argv[5], \"%lf\", &dblTime);\t\t// Start time\n");
        buffer.append("\tsscanf(argv[6], \"%lf\", &dblTSTOP);\t\t// Stop time\n");
        buffer.append("\tsscanf(argv[7], \"%lf\", &dblTs);\t\t\t// Time step size\n\n");

        buffer.append("\t//Allocate gsl_vectors, gsl_matrices\n");
        buffer.append("\tParameters.pRateConstantVector = gsl_vector_alloc(NUMBER_OF_RATES);\n");
        buffer.append("\tParameters.pSTM = gsl_matrix_alloc(NUMBER_OF_STATES,NUMBER_OF_RATES);\n\n");

        buffer.append("\t// Generate timestep array\n");
        buffer.append("\tnTimes = floor((dblTSTOP-dblTime)/dblTs)+1;\n");
        buffer.append("\tdouble TSIM[nTimes];\n");
        buffer.append("\tfor (i = 0; i<nTimes; i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tTSIM[i] = dblTime + i*dblTs;\n");
        buffer.append("\t}\n\n");

        buffer.append("\t// Load kinetics, IC's and STM\n");
        buffer.append("\tgetRateConstants(pInputKineticsFile, Parameters.pRateConstantVector);\n");
        buffer.append("\tgetICs(pInputICFile, adjStateVector);\n");
        buffer.append("\tgetSTM(pSTMFile, Parameters.pSTM);\n\n");

        buffer.append("\t//Set Sensitivity Matrix to zero at t0\n");
        buffer.append("\tfor (i=NUMBER_OF_STATES; i<TOTAL_ADJ_STATES; i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tadjStateVector[i] = 0;\n");
        buffer.append("\t}\n\n");

        buffer.append("\t/********************Set up the ODE solver*******************/\n\n");

        buffer.append("\tconst gsl_odeiv_step_type *pT = gsl_odeiv_step_bsimp;\n");
        buffer.append("\tgsl_odeiv_step *pS = gsl_odeiv_step_alloc(pT,NUMBER_OF_STATES);\n");
        buffer.append("\tgsl_odeiv_control *pC = gsl_odeiv_control_y_new(TOLERANCE,TOLERANCE);\n");
        buffer.append("\tgsl_odeiv_evolve *pE = gsl_odeiv_evolve_alloc(NUMBER_OF_STATES);\n\n");

        buffer.append("\t// Setup the GSL system\n");
        buffer.append("\tgsl_odeiv_system sys = {AdjBalances,Jacobian,NUMBER_OF_STATES,&Parameters};\n\n");

        buffer.append("\t/*****************ODE Solver Setup Complete!*****************/\n\n");

        buffer.append("\t// Dump intitial state to output\n");
        buffer.append("\tif ((pDataFile = fopen(pOutputDataFile, \"w\")) == NULL)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tprintf (\"Error opening the datafile for writing.\\n\");\n");
        buffer.append("\t\treturn(1);\n");
        buffer.append("\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t{\n");
        buffer.append("\t\tfor (i=0; i<TOTAL_ADJ_STATES; i++)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tfprintf(pDataFile,\"%g \", adjStateVector[i]);\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tfprintf(pDataFile,\"\\n\");\n");
        buffer.append("\t}\n\n");

        buffer.append("\tfor (i=0; i<TOTAL_ADJ_STATES; i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tOldStateVector[i] = adjStateVector[i];\n");
        buffer.append("\t}\n");
        buffer.append("\tlastTime = dblTime;\n\n");

        buffer.append("\tint status = 0;\n");
        buffer.append("\tj=1;\n\n");

        buffer.append("\t// Main ODE Solver loop\n");
        buffer.append("\twhile (dblTime < dblTSTOP)\n");
        buffer.append("\t{\n");
        buffer.append("\t\t// Calculate one step of the ODE Solution\n");
        buffer.append("\t\tstatus = gsl_odeiv_evolve_apply(pE,pC,pS,&sys,&dblTime,dblTSTOP,&dblTs,adjStateVector);\n");
        buffer.append("\t\tif (status != GSL_SUCCESS)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tprintf(\"ODE Solver loop failed at t = %g\\n\", dblTime);\n");
        buffer.append("\t\t\treturn(1);\n");
        buffer.append("\t\t}\n\n");

        buffer.append("\t\tif (status == GSL_SUCCESS)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tprintf(\"%g\\n\", dblTime);\n\n");

        buffer.append("\t\t\twhile (lastTime < TSIM[j] && dblTime >= TSIM[j])\n");
        buffer.append("\t\t\t{\n");
        buffer.append("\t\t\t\tfor (i=0; i<TOTAL_ADJ_STATES; i++)\n");
        buffer.append("\t\t\t\t{\n");
        buffer.append("\t\t\t\t\ttmp = OldStateVector[i] + (adjStateVector[i]-OldStateVector[i])*(TSIM[j]-lastTime)/(dblTime-lastTime);\n");
        buffer.append("\t\t\t\t\tfprintf(pDataFile,\"%g \", tmp);\n");
        buffer.append("\t\t\t\t}\n");
        buffer.append("\t\t\t\tfprintf(pDataFile,\"\\n\");\n\n");

        buffer.append("\t\t\t\tfflush(pDataFile);\n\n");

        buffer.append("\t\t\t\tj++;\n");
        buffer.append("\t\t\t}\n\n");

        buffer.append("\t\t\tfor (i=0; i<TOTAL_ADJ_STATES; i++)\n");
        buffer.append("\t\t\t{\n");
        buffer.append("\t\t\t\tOldStateVector[i] = adjStateVector[i];\n");
        buffer.append("\t\t\t}\n");
        buffer.append("\t\t\tlastTime = dblTime;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t}\n\n");

        buffer.append("\t// Close the output stream\n");
        buffer.append("\tfclose(pDataFile);\n\n");

        buffer.append("\t// Free gsl data\n");
        buffer.append("\tgsl_vector_free(Parameters.pRateConstantVector);\n");
        buffer.append("\tgsl_matrix_free(Parameters.pSTM);\n\n");

        buffer.append("\t// Free integrator memory\n");
        buffer.append("\tgsl_odeiv_evolve_free(pE);\n");
        buffer.append("\tgsl_odeiv_control_free(pC);\n");
        buffer.append("\tgsl_odeiv_step_free(pS);\n\n");

        buffer.append("\tprintf(\"Time elapsed: %f\\n\", ((double)clock() - start) / CLOCKS_PER_SEC);\n\n");

        buffer.append("\treturn(0);\n");
        buffer.append("}\n\n");

        buffer.append("static void getSTM(const char* pFilename, gsl_matrix *pSTM)\n");
        buffer.append("{\n");
        buffer.append("\tFILE *pFile = fopen(pFilename,\"r\");\n");
        buffer.append("\tgsl_matrix_fscanf(pFile,pSTM);		//no comments allowed in file! gsl gets mad\n");
        buffer.append("\tfclose(pFile);\n");
        buffer.append("}\n\n");

        buffer.append("static void getRateConstants(const char* pFilename, gsl_vector *pRateConstantVector)\n");
        buffer.append("{\n");
        buffer.append("\tFILE *pFile = fopen(pFilename,\"r\");\n");
        buffer.append("\tgsl_vector_fscanf(pFile, pRateConstantVector);\n");
        buffer.append("\tfclose(pFile);\n");
        buffer.append("}\n\n");

        buffer.append("static void getICs(const char* pFilename, double x[])\n");
        buffer.append("{\n");
        buffer.append("\tint i;\n");
        buffer.append("\tgsl_vector *tmp = gsl_vector_alloc(NUMBER_OF_STATES);\n\n");

        buffer.append("\tFILE *pFile = fopen(pFilename,\"r\");\n");
        buffer.append("\tgsl_vector_fscanf(pFile, tmp);\n");
        buffer.append("\tfclose(pFile);\n\n");

        buffer.append("\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tx[i] = gsl_vector_get(tmp,i);\n");
        buffer.append("\t}\n\n");

        buffer.append("\tgsl_vector_free(tmp);\n");
        buffer.append("}\n\n");

        buffer.append("// Solver functions\n\n"); 
    }

    public void buildMassBalanceBuffer(StringBuffer buffer) throws Exception {
        // this contains the main part of the gsl-c code.

    	// First things first - get the size of the system -
        int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 

        buffer.append("/*\n");
        buffer.append(" * Created 2009-06-19 15:32\n");
        buffer.append(" * Written by Robert Dromms\n");
        buffer.append(" */\n\n");

        buffer.append("#include <stdio.h>\n");
        buffer.append("#include <math.h>\n");
        buffer.append("#include <time.h>\n");
        buffer.append("#include <gsl/gsl_errno.h>\n");
        buffer.append("#include <gsl/gsl_matrix.h>\n");
        buffer.append("#include <gsl/gsl_odeiv.h>\n");
        buffer.append("#include <gsl/gsl_vector.h>\n");
        buffer.append("#include <gsl/gsl_blas.h>\n\n");

        buffer.append("//PROBLEM SPECIFIC VALUES\n");
        buffer.append("#define TOLERANCE\t\t1e-10\t\t// global error tolerance\n");
        buffer.append("#define NUMBER_OF_STATES\t"+NUMBER_OF_SPECIES+"\t\t// number of equations\n");
        buffer.append("#define NUMBER_OF_RATES\t\t"+NUMBER_OF_RATES+"\t\t// number of parameters\n\n");
        buffer.append("// Function prototypes\n\n");

// Function prototypes

        buffer.append("// Functions to grab the kinetics rate constants and ic's from files\n");
        buffer.append("static void getRateConstants(const char* pFilename, gsl_vector *pRateConstantVector);\n");
        buffer.append("static void getICs(const char* pFilename, double x[]);\n");
        buffer.append("static void getSTM(const char* pFilename, gsl_matrix *pSTM);\n");
        buffer.append("// Functions called by the solver\n");
        buffer.append("static int MassBalances(double t,const double x[],double f[],void * user_data);\n");
        buffer.append("static void Kinetics(double t, const double x[], gsl_vector *pRateConstantVector, gsl_vector *pRateVector);\n");
        buffer.append("static int Jacobian(double t, const double x[],double * dfdx, double dfdt[], void * user_data);\n\n");

        buffer.append("struct params\n");
        buffer.append("{\n");
        buffer.append("\tgsl_matrix *pSTM;\n");
        buffer.append("\tgsl_vector *pRateConstantVector;\n");
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
        buffer.append("\tint i, j, nTimes;\n");
        buffer.append("\tdouble lastTime, dblTime, dblTSTOP, dblTs, tmp, StateVector[NUMBER_OF_STATES], OldStateVector[NUMBER_OF_STATES];\n");
        buffer.append("\tFILE *pDataFile;\n");
        buffer.append("\tstruct params Parameters;\n\n");

        buffer.append("\t// assign info from arguments\n");
        buffer.append("\tchar *pOutputDataFile = argv[1];\t\t// Assign data output file\n");
        buffer.append("\tchar *pInputKineticsFile = argv[2];\t\t// Get kinetics datafile name\n");
        buffer.append("\tchar *pInputICFile = argv[3];\t\t\t// Get ic datafile name\n");
        buffer.append("\tchar *pSTMFile = argv[4];\t\t\t// Get STM datafile name\n");
        buffer.append("\tsscanf(argv[5], \"%lf\", &dblTime);\t\t// Start time\n");
        buffer.append("\tsscanf(argv[6], \"%lf\", &dblTSTOP);\t\t// Stop time\n");
        buffer.append("\tsscanf(argv[7], \"%lf\", &dblTs);\t\t\t// Time step size\n\n");

        buffer.append("\t//Allocate gsl_vectors, gsl_matrices\n");
        buffer.append("\tParameters.pRateConstantVector = gsl_vector_alloc(NUMBER_OF_RATES);\n");
        buffer.append("\tParameters.pSTM = gsl_matrix_alloc(NUMBER_OF_STATES,NUMBER_OF_RATES);\n\n");

        buffer.append("\t// Generate timestep array\n");
        buffer.append("\tnTimes = floor((dblTSTOP-dblTime)/dblTs)+1;\n");
        buffer.append("\tdouble TSIM[nTimes];\n");
        buffer.append("\tfor (i = 0; i<nTimes; i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tTSIM[i] = dblTime + i*dblTs;\n");
        buffer.append("\t}\n\n");

        buffer.append("\t// Load kinetics, IC's and STM\n");
        buffer.append("\tgetRateConstants(pInputKineticsFile, Parameters.pRateConstantVector);\n");
        buffer.append("\tgetICs(pInputICFile, StateVector);\n");
        buffer.append("\tgetSTM(pSTMFile, Parameters.pSTM);\n\n");

        buffer.append("\t/********************Set up the ODE solver*******************/\n\n");

        buffer.append("\tconst gsl_odeiv_step_type *pT = gsl_odeiv_step_bsimp;\n");
        buffer.append("\tgsl_odeiv_step *pS = gsl_odeiv_step_alloc(pT,NUMBER_OF_STATES);\n");
        buffer.append("\tgsl_odeiv_control *pC = gsl_odeiv_control_y_new(TOLERANCE,TOLERANCE);\n");
        buffer.append("\tgsl_odeiv_evolve *pE = gsl_odeiv_evolve_alloc(NUMBER_OF_STATES);\n\n");

        buffer.append("\t// Setup the GSL system\n");
        buffer.append("\tgsl_odeiv_system sys = {MassBalances,Jacobian,NUMBER_OF_STATES,&Parameters};\n\n");

        buffer.append("\t/*****************ODE Solver Setup Complete!*****************/\n\n");

        buffer.append("\t// Dump intitial state to output\n");
        buffer.append("\tif ((pDataFile = fopen(pOutputDataFile, \"w\")) == NULL)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tprintf (\"Error opening the datafile for writing.\\n\");\n");
        buffer.append("\t\treturn(1);\n");
        buffer.append("\t}\n");
        buffer.append("\telse\n");
        buffer.append("\t{\n");
        buffer.append("\t\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tfprintf(pDataFile,\"%g \", StateVector[i]);\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\tfprintf(pDataFile,\"\\n\");\n");
        buffer.append("\t}\n\n");

        buffer.append("\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tOldStateVector[i] = StateVector[i];\n");
        buffer.append("\t}\n");
        buffer.append("\tlastTime = dblTime;\n\n");
        buffer.append("\tint status = 0;\n");
        buffer.append("\tj=1;\n\n");

        buffer.append("\t// Main ODE Solver loop\n");
        buffer.append("\twhile (dblTime < dblTSTOP)\n");
        buffer.append("\t{\n");
        buffer.append("\t\t// Calculate one step of the ODE Solution\n");
        buffer.append("\t\tstatus = gsl_odeiv_evolve_apply(pE,pC,pS,&sys,&dblTime,dblTSTOP,&dblTs,StateVector);\n");
        buffer.append("\t\tif (status != GSL_SUCCESS)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tprintf(\"ODE Solver loop failed at t = %g\\n\", dblTime);\n");
        buffer.append("\t\t\treturn(1);\n");
        buffer.append("\t\t}\n\n");

        buffer.append("\t\tif (status == GSL_SUCCESS)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tprintf(\"%g\\n\", dblTime);\n\n");

        buffer.append("\t\t\twhile (lastTime < TSIM[j] && dblTime >= TSIM[j])\n");
        buffer.append("\t\t\t{\n");
        buffer.append("\t\t\t\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
        buffer.append("\t\t\t\t{\n");
        buffer.append("\t\t\t\t\ttmp = OldStateVector[i] + (StateVector[i]-OldStateVector[i])*(TSIM[j]-lastTime)/(dblTime-lastTime);\n");
        buffer.append("\t\t\t\t\tfprintf(pDataFile,\"%g \", tmp);\n");
        buffer.append("\t\t\t\t}\n");
        buffer.append("\t\t\t\tfprintf(pDataFile,\"\\n\");\n\n");

        buffer.append("\t\t\t\tfflush(pDataFile);\n\n");
        
        buffer.append("\t\t\t\tj++;\n");
        buffer.append("\t\t\t}\n\n");
/*
			for (i=0; i<NUMBER_OF_STATES; i++)
			{
				fprintf(pDataFile,"%g ", StateVector[i]);
			}
			fprintf(pDataFile,"\n");
			fflush(pDataFile);
*/
        buffer.append("\t\t\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
        buffer.append("\t\t\t{\n");
        buffer.append("\t\t\t\tOldStateVector[i] = StateVector[i];\n");
        buffer.append("\t\t\t}\n");
        buffer.append("\t\t\tlastTime = dblTime;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t}\n\n");

        buffer.append("\t// Close the output stream\n");
        buffer.append("\tfclose(pDataFile);\n\n");

        buffer.append("\t// Free gsl data\n");
        buffer.append("\tgsl_vector_free(Parameters.pRateConstantVector);\n");
        buffer.append("\tgsl_matrix_free(Parameters.pSTM);\n\n");

        buffer.append("\t// Free integrator memory\n");
        buffer.append("\tgsl_odeiv_evolve_free(pE);\n");
        buffer.append("\tgsl_odeiv_control_free(pC);\n");
        buffer.append("\tgsl_odeiv_step_free(pS);\n\n");

        buffer.append("\tprintf(\"Time elapsed: %f\\n\", ((double)clock() - start) / CLOCKS_PER_SEC);\n\n");

        buffer.append("\treturn(0);\n");
        buffer.append("}\n\n");

        buffer.append("static void getSTM(const char* pFilename, gsl_matrix *pSTM)\n");
        buffer.append("{\n");
        buffer.append("\tFILE *pFile = fopen(pFilename,\"r\");\n");
        buffer.append("\tgsl_matrix_fscanf(pFile,pSTM);		//no comments allowed in file! gsl gets mad\n");
        buffer.append("\tfclose(pFile);\n");
        buffer.append("}\n\n");

        buffer.append("static void getRateConstants(const char* pFilename, gsl_vector *pRateConstantVector)\n");
        buffer.append("{\n");
        buffer.append("\tFILE *pFile = fopen(pFilename,\"r\");\n");
        buffer.append("\tgsl_vector_fscanf(pFile, pRateConstantVector);\n");
        buffer.append("\tfclose(pFile);\n");
        buffer.append("}\n\n");

        buffer.append("static void getICs(const char* pFilename, double x[])\n");
        buffer.append("{\n");
        buffer.append("\tint i;\n");
        buffer.append("\tgsl_vector *tmp = gsl_vector_alloc(NUMBER_OF_STATES);\n\n");

        buffer.append("\tFILE *pFile = fopen(pFilename,\"r\");\n");
        buffer.append("\tgsl_vector_fscanf(pFile, tmp);\n");
        buffer.append("\tfclose(pFile);\n\n");

        buffer.append("\tfor (i=0; i<NUMBER_OF_STATES; i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tx[i] = gsl_vector_get(tmp,i);\n");
        buffer.append("\t}\n\n");
        
        buffer.append("\tgsl_vector_free(tmp);\n");
        buffer.append("}\n\n");
        buffer.append("// Solver functions\n\n");
    }

    private String getFileName(String xpath)
    {
    	String strTmpRaw = "";
    	
    	strTmpRaw = _xmlPropTree.getProperty(xpath);
    	int INT_2_DOT = strTmpRaw.indexOf(".");
    	String strName = strTmpRaw.substring(0, INT_2_DOT);
    	
    	return(strName);
    }
    
    private String getFullFileName(String xpath)
    {
    	String strTmpRaw = "";
    	strTmpRaw = _xmlPropTree.getProperty(xpath);	
    	return(strTmpRaw);
    }
    
    public void buildBuildFileBuffer(StringBuffer driver) throws Exception {

    	// Ok, so we need to get the exename from the modelname -
    	String strModelName = getFullFileName("//MassBalanceFunction/massbalance_filename/text()");
    	String strExeName = getFileName("//MassBalanceFunction/massbalance_filename/text()");
    	
    	driver.append("gcc -o ");
    	driver.append(strExeName);
    	driver.append(" ");
    	driver.append(strModelName);
    	driver.append(" ");
        driver.append("/usr/local/lib/libgsl.a /usr/local/lib/libgslcblas.a -lm\n");
    }


    public void buildSolveAdjBalBuffer(StringBuffer driver) throws Exception {

        driver.append("static int AdjBalances(double t, const double x[], double f[], void * user_data)\n");
        driver.append("{\n");
        driver.append("\t// Prep some stuff\n");
        driver.append("\tint i, j;\n");
        driver.append("\tdouble tmp, Jac[NUMBER_OF_STATES ^2];\n");
        driver.append("\tstruct params* Parameters = (struct params *) user_data;\t\t\t// Data Struct w/ STM, rates\n");
        driver.append("\tgsl_vector *pRateVector = gsl_vector_alloc(NUMBER_OF_RATES);\t\t\t// The kinetics vector\n");
        driver.append("\tgsl_matrix *PMatrix = gsl_matrix_alloc(NUMBER_OF_STATES,NUMBER_OF_RATES);\t// The df/dp matrix\n\n");

        driver.append("\t// Call the kinetics function\n");
        driver.append("\tKinetics(t, x, Parameters->pRateConstantVector, pRateVector);\n\n");


        driver.append("\t// Calculate dx/dt\n");
        driver.append("\tfor (i=0; i< NUMBER_OF_STATES; i++)\n");
        driver.append("\t{\n");
        driver.append("\t\ttmp = 0;\n");
        driver.append("\t\tfor (j=0; j < NUMBER_OF_RATES; j++)\n");
        driver.append("\t\t{\n");
        driver.append("\t\t\ttmp += gsl_matrix_get(Parameters->pSTM,i,j) * gsl_vector_get(pRateVector,j);\n");
        driver.append("\t\t}\n");
        driver.append("\t\tf[i] = tmp;\n");
        driver.append("\t}\n\n");

        driver.append("\t// Find dS/dt matrix\n");
        driver.append("\tJacobian(t, x, Jac, f, user_data);\n");
        driver.append("\tcalculatePMatrix(Parameters->pRateConstantVector, x, PMatrix);\n\n");

        driver.append("\tcalcDSDT(x, f, Jac, PMatrix);\n\n");
    	driver.append("\t// Free allocated mem -\n");
        driver.append("\tgsl_vector_free(pRateVector);\n");
        driver.append("\tgsl_matrix_free(PMatrix);\n\n");
        driver.append("\treturn(GSL_SUCCESS);\n");
        driver.append("}\n\n");
    }

    public void buildShellCommandBuffer(StringBuffer buffer) throws Exception {

    	// Ok, so we need to get a bunch of stuff from the propfile -
    	String strExeName = getFileName("//MassBalanceFunction/massbalance_filename/text()");
    	String strParameters = getFullFileName("//KineticParametersFileName/kineticparameters_filename/text()");
    	String strInitialCondtions = getFullFileName("//InitialConditionFileName/initialcondition_filename/text()");
    	String strSTMatrix = getFullFileName("//StoichiometricMatrix/stoichiometric_matrix_filename/text()");
    	String strOutputName = getFullFileName("//OutputFileName/output_filename/text()");
    	String strTimeFileName = getFullFileName("//OutputFileName/output_filename/text()");
    	
    	// Dumps RunModel.sh command
    	buffer.append("./");
    	buffer.append(strExeName);
    	buffer.append(" ");
    	buffer.append(strOutputName);
    	buffer.append(" ");
    	buffer.append(strTimeFileName);
    	buffer.append(" ");
    	buffer.append(strParameters);
    	buffer.append(" ");
    	buffer.append(strInitialCondtions);
    	buffer.append(" ");
    	buffer.append(strSTMatrix);
    	buffer.append(" $1 $2 $3\n");
    	
        //buffer.append("modelCode output.dat timestamp.dat kinetics.dat ic.dat stm.dat $1 $2 $3\n");
    }


    // uses Jacobian and PMatrix to find DSDT   (B)
    public void buildDSDTBuffer(StringBuffer buffer) throws Exception
    {
        // uses Jacobian and PMatrix to find DSDT   (B)

        // currently not used in gsl-c.

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

    public void buildJacobianBuffer(StringBuffer buffer,Vector vecReactions,Vector<Species> vecSpecies) throws Exception
    {

        
        // First things first - get the size of the system -
        int NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        int NUMBER_OF_RATES = (int)vecReactions.size();

        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NUMBER_OF_SPECIES][NUMBER_OF_RATES];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions,vecSpecies);
        
        // Ok, when I get here I have the stoichiometric matrix -
        // Initialize the array -
        String[][] strJacobian = new String[NUMBER_OF_SPECIES][NUMBER_OF_SPECIES];
        for (int counter_outer=0;counter_outer<NUMBER_OF_SPECIES;counter_outer++)
        {
            for (int counter_inner=0;counter_inner<NUMBER_OF_SPECIES;counter_inner++)
            {
                strJacobian[counter_outer][counter_inner]="0.0";
            }
        }


        StringBuffer tmpBuffer = new StringBuffer();
        Vector<String> vecConnect = new Vector<String>();
        Vector<String> vecSpeciesRate = new Vector<String>();
        for (int state_counter_outer=0;state_counter_outer<NUMBER_OF_SPECIES;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NUMBER_OF_SPECIES;state_counter_inner++)
            {
                // put jacobian logic here -
                strJacobian[state_counter_outer][state_counter_inner]=formulateJacobianElement(matrix,state_counter_outer,state_counter_inner);
            }
        }

        // Ok, so when I get here I have the Jacobian - we need to convert it into a string buffer

        buffer.append("static int Jacobian(double t, const double x[], double * dfdx, double dfdt[], void * user_data)\n");
        buffer.append("{\n");
        buffer.append("\t// Machine generated dfdx matrix (Jacobian)\n");
        buffer.append("\tint i, j;\n");
        buffer.append("\tgsl_vector *k;\n");
        buffer.append("\tstruct params* Parameters = (struct params *) user_data;\n");
        buffer.append("\tk = Parameters->pRateConstantVector;\n\n");

        buffer.append("\tfor (i=0;i<NUMBER_OF_STATES;i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tfor (j=0;j<NUMBER_OF_STATES;j++)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tdfdx[i*NUMBER_OF_STATES+j] = 0.0;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t}\n\n");

        //Start throwing in Jacobian entries
        for (int state_counter_outer=0;state_counter_outer<NUMBER_OF_SPECIES;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<NUMBER_OF_SPECIES;state_counter_inner++)
            {
                // skip this entry if it is zero
                if(!strJacobian[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\tdfdx[");
                    buffer.append(state_counter_outer);
                    buffer.append("*NUMBER_OF_STATES+");
                    buffer.append(state_counter_inner);

                    buffer.append("] = ");
                    buffer.append(strJacobian[state_counter_outer][state_counter_inner]);
                    buffer.append(";\n");
                }
            }
        }

        buffer.append("\nreturn(GSL_SUCCESS);\n\n");
        buffer.append("}\n\n");

    //TODO DSDT implementation for sensitivity
        //TODO Test DSDT buffer


        buffer.append("// DSDT RHS function\n");
        buffer.append("static void calcDSDT(const double x[], double dxdt[], const double Jac[], gsl_matrix *PMat)\n");
        buffer.append("{\n");
        buffer.append("\tint j;\n\n");

        buffer.append("\t for (j=0; j<NUMBER_OF_RATES; j++)\n");
        buffer.append("\t{\n");

        for (int state_counter_outer=0;state_counter_outer<NUMBER_OF_SPECIES;state_counter_outer++)
        {
            buffer.append("\t\tdxdt[NUMBER_OF_STATES+");
            buffer.append(state_counter_outer);
            buffer.append("*NUMBER_OF_RATES+j] =");
            for (int state_counter_inner=0;state_counter_inner<NUMBER_OF_SPECIES;state_counter_inner++)
            {
                // skip this entry if it is zero
                if(!strJacobian[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\n\t\t\t(Jac[NUMBER_OF_STATES*"+state_counter_outer+"+"+state_counter_inner+"]");
                    buffer.append("*x[NUMBER_OF_STATES+");
                    buffer.append(state_counter_inner);
                    buffer.append("*NUMBER_OF_RATES+j]) +");
                }
            }
            buffer.append("\n\t\t\tgsl_matrix_get(PMat, "+state_counter_outer+", j);\n");
        }

        buffer.append("\t}\n");
        buffer.append("}\n");


    }


    public void buildPMatrixBuffer(StringBuffer buffer,Vector vecReactions,Vector<Species> vecSpecies) throws Exception
    {
    	// Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
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
                strPMatrix[counter_outer][counter_inner]=formulatePMatrixElement(matrix,counter_outer,counter_inner);
            }
        }

        // Convert into string buffer -


        buffer.append("static void calculatePMatrix(gsl_vector *k, const double x[], gsl_matrix *PM)\n");
        buffer.append("{\n");
        buffer.append("\t// Machine generated dfdp matrix.\n\n");

        buffer.append("\tint i,j;\n");
        buffer.append("\tdouble tmp;\n\n");

        buffer.append("\tfor (i=0;i<NUMBER_OF_RATES;i++)\n");
        buffer.append("\t{\n");
        buffer.append("\t\tfor (j=0;j<NUMBER_OF_STATES;j++)\n");
        buffer.append("\t\t{\n");
        buffer.append("\t\t\tgsl_matrix_set(PM, i, j, 0.0);\n");
        buffer.append("\t\t}\n");
        buffer.append("\t}\n\n");

        for (int state_counter_outer=0;state_counter_outer<NROWS;state_counter_outer++)
        {
            for (int state_counter_inner=0;state_counter_inner<(NROWS+NCOLS);state_counter_inner++)
            {
                // if it is a zero entry, just skip it
                if(!strPMatrix[state_counter_outer][state_counter_inner].equals("0.0")){
                    // put the entries in the string buffer -
                    buffer.append("\ttmp = ");
                    buffer.append(strPMatrix[state_counter_outer][state_counter_inner]);
                    buffer.append(";\n");
                   
                    buffer.append("\tgsl_matrix_set(PM,");
                    buffer.append(state_counter_outer);
                    buffer.append(",");
                    buffer.append(state_counter_inner);
                    buffer.append(",tmp);\n\n");
                }
            }
        }
        buffer.append("}\n\n");
    }


    private String formulatePMatrixElement(double[][] matrix,int massbalance,int parameter)
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
                        buffer.append("x[");
                        buffer.append(state_counter);
                        buffer.append("]*");
                    }
                    // any thing else I need to raise to a power
                    else {
                        buffer.append("pow(x[");
                        buffer.append(state_counter);
                        buffer.append("],");
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
                    buffer.append("gsl_vector_get(k,");
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
                    buffer.append("gsl_vector_get(k,");
                    buffer.append(test_index);
                    buffer.append(")*x[");
                    buffer.append(state);
                    buffer.append("]");
                }
                // any thing else I need to raise to a power
                else {
                    buffer.append(matrix[massbalance][test_index]);
                    buffer.append("*");
                    buffer.append(tempStmElement);
                    buffer.append("*");
                    buffer.append("gsl_vector_get(k,");
                    buffer.append(test_index);
                    buffer.append(")*pow(x[");
                    buffer.append(state);
                    buffer.append("],");
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
                            buffer.append("*x[");
                            buffer.append(species_index);
                            buffer.append("]");
                        }
                        // any thing else I need to raise to a power
                        else {
                            buffer.append("*pow(x[");
                            buffer.append(species_index);
                            buffer.append("],");
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
