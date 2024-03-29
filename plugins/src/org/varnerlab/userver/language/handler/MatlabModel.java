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
 * Created on December 29, 2006, 3:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.language.handler;

// import statements -
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

/**
 *
 * @author jeffreyvarner
 */
public class MatlabModel {
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
	
    /** Creates a new instance of MatlabModel */
    public MatlabModel() {
    }
    
    public void buildLargeScaleMassBalanceBuffer(StringBuffer massbalances,Model model_wrapper,Vector<Reaction> vecReactions,Vector<Species> vecSpecies,XMLPropTree propTree) throws Exception
	{
		// Populate the large scale mass balances -
		MModelUtilities.buildLargeScaleMatlabMassBalanceBuffer(massbalances,model_wrapper,vecReactions,vecSpecies,propTree);
	}
   
    public void buildInputsBuffer(StringBuffer inputs) throws Exception
    {
        // Ok, so the build the input buffer -
		
    	ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("InputFunction");
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
        inputs.append(_xmlPropTree.getProperty(".//Model/@username"));
        inputs.append("\n");
        inputs.append("% Type: ");
        inputs.append(_xmlPropTree.getProperty(".//Model/@type"));
        inputs.append("\n");
        inputs.append("% Version: ");
        inputs.append(_xmlPropTree.getProperty(".//Model/@version"));
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
    
    public void buildDriverBuffer(StringBuffer driver,XMLPropTree propTree) throws Exception {
        
    	ArrayList<String> arrList = propTree.processFilenameBlock("DriverFile");
    	String strFunctionName = arrList.get(1);
    	
        driver.append("function [TSIM,X,OUTPUT] = ");
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
        
        // Call to the ODE solver -
        ArrayList<String> arrList2 = propTree.processFilenameBlock("MassBalanceFunction");
        String strMassBalanceFunctionName = arrList2.get(1);
        
        driver.append("% Call the ODE solver - the default is ODE15s\n");
        driver.append("[T,X]=ode15s(@");
        driver.append(strMassBalanceFunctionName);
        driver.append(",TSIM,IC,[],DF,S,kV);\n");
        driver.append("\n");
        driver.append("% Calculate the output - \n");
        driver.append("OUTPUT = X(:,MEASUREMENT_INDEX_VECTOR);\n");
        driver.append("\n");
        driver.append("% return to caller - \n");
        driver.append("return;\n");
    }
    
    public void buildKineticsBuffer(StringBuffer buffer,Model model_wrapper) throws Exception
    {
    	// Method attributes -
    	OctaveMModel octaveMModel = new OctaveMModel();
    	
    	// Ok, just call the octaveMModel impl for this (they are the same)
    	octaveMModel.buildKineticsBuffer(buffer, model_wrapper, _xmlPropTree);
    }
    
    public void buildMassBalanceBuffer(StringBuffer massbalances) throws Exception
    {
        
		// Get the massbalance name -
    	ArrayList<String> arrList2 = _xmlPropTree.processFilenameBlock("MassBalanceFunction");
        String strMassBalanceFunctionName = arrList2.get(1);
        
        // Put the header -
        massbalances.append("function [DXDT] = ");
        massbalances.append(strMassBalanceFunctionName);
        massbalances.append("(t,x,DF,S,kV)\n");
        massbalances.append("% ----------------------------------------------------------------------\n");
        massbalances.append("% ");
        massbalances.append(strMassBalanceFunctionName);
        massbalances.append(".m was generated using the UNIVERSAL code generator system.\n");
        massbalances.append("% Username: ");
        massbalances.append(_xmlPropTree.getProperty(".//Model/@username"));
        massbalances.append("\n");
        massbalances.append("% Type: ");
        massbalances.append(_xmlPropTree.getProperty(".//Model/@type"));
        massbalances.append("\n");
        massbalances.append("% Version: ");
        massbalances.append(_xmlPropTree.getProperty(".//Model/@version"));
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
        
        // Call the kinetics function -
        ArrayList<String> arrList = _xmlPropTree.processFilenameBlock("KineticsFunction");
        String strKineticesFunctionName = arrList.get(1);
        
        massbalances.append("[rV]=");
        massbalances.append(strKineticesFunctionName);
        massbalances.append("(t,x,kV);\n");
        massbalances.append("\n");
        massbalances.append("% Calculate the input vector\n");
        
        ArrayList<String> arrList3 = _xmlPropTree.processFilenameBlock("InputFunction");
        String strInputFunctionName = arrList3.get(1);
        
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
    
    public void buildAdjBalFntBuffer(StringBuffer buffer,Vector vecReactions,Vector<Species> vecSpecies,XMLPropTree propTree) throws Exception 
    {
    	// Call the utility class and populate the buffer -
    	MModelUtilities.buildMatlabAdjBalFntBuffer(buffer,vecReactions, vecSpecies, model_wrapper, propTree);
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
          driver.append("IC = [IC; zeros(NSTATES,1)];\n");
          driver.append("\n");
          //driver.append("% Call the ODE solver - the default is LSODE\n");

          //@todo should use the name the user passed in..
          //String strMassBalanceFunctionNameRaw = propTree.getProperty("//MassBalanceFunction/massbalance_filename/text()");
          //INT_2_DOT = strMassBalanceFunctionNameRaw.indexOf(".");
          //String strMassBalanceFunctionName = strMassBalanceFunctionNameRaw.substring(0, INT_2_DOT);

          ArrayList<String> arrList2 = propTree.processFilenameBlock("AdjointBalances");
          String strAdjointBalancesFunctionName = arrList2.get(1);

          //driver.append("f = @(x,t)");
          //driver.append(strAdjointBalancesFunctionName);
          //driver.append("(x,t,STM,kV,NRATES,NSTATES,pIndex);\n");
          //driver.append("[xOut]=ode15s(f,IC,TSIM);\n");
          //driver.append("\n");
          //driver.append("% Seperate xOut into state and sensitivity matrix\n");
          //driver.append("X = xOut(:,1:NSTATES);\n");
          //driver.append("S = xOut(:,NSTATES+1:end);\n");
          //driver.append("return;\n");
          
     
          driver.append("% Call the ODE solver - the default is ODE15s\n");
          driver.append("[TOUT,XOUT]=ode15s(@");
          driver.append(strAdjointBalancesFunctionName);
          driver.append(",TSIM,IC,[],STM,kV,NRATES,NSTATES,pIndex);\n");
          driver.append("\n");
          driver.append("% Seperate xOut into state and sensitivity matrix\n");
          driver.append("X = XOUT(:,1:NSTATES);\n");
          driver.append("S = XOUT:,NSTATES+1:end);\n");
          driver.append("return;\n");
      }
}
