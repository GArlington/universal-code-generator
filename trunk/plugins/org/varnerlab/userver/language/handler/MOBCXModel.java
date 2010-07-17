package org.varnerlab.userver.language.handler;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.Species;
import org.varnerlab.server.transport.LoadXMLPropFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MOBCXModel {
	// Class/instance attributes -
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private SBMLReader _sbmlReader = null;
    private SBMLDocument _sbmlDocument = null;
    private Model _model = null;
	
	public MOBCXModel(){
		
		// Load the smbl lib -
		System.loadLibrary("sbmlj");   
	}
	
	private void loadSBMLModel(LoadXMLPropFile _xmlPropTree) throws Exception 
	{
		// Create an instance of the SBML reader -
        _sbmlReader = new SBMLReader();
        
        
        // Get the resource string -
        String strNetworkFileName = _xmlPropTree.getProperty("//NetworkFileName/input_network_filename/text()");
        String strNetworkFileNamePath = _xmlPropTree.getProperty("//NetworkFileName/input_network_path/text()");
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        
        String _strSBMLFile = "";
        if (strNetworkFileNamePath.isEmpty())
        {
        	_strSBMLFile = strWorkingDir+"/"+strNetworkFileName;
        }
        else
        {
        	_strSBMLFile = strWorkingDir+"/"+strNetworkFileNamePath+"/"+strNetworkFileName;
        }
        
        // Set the model reference -
        _sbmlDocument = _sbmlReader.readSBML(_strSBMLFile);
        _model = _sbmlDocument.getModel();
	}
	
	private int findSpeciesIndex(String strSpeciesSymbol) throws Exception
	{
		// Method attributes -
		int return_index = 0;
	
		// Ok, lets get the list of species -
		ListOfSpecies listOfSpecies = _model.getListOfSpecies();
		long NUMBER_OF_SPECIES = _model.getNumSpecies();
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            // Get the species -
        	Species species_tmp = (Species)listOfSpecies.get(scounter);
        	
        	// What is this species?
        	String strSpeciesTest = species_tmp.getId();
        	if (strSpeciesSymbol.equalsIgnoreCase(strSpeciesTest))
        	{
        		// need the +1 to counter the zero-based array issue
        		return_index = scounter+1;
        		break;
        	}
        }
		
		
		// return -
		return(return_index);
	}
	
	private int[] findDataColIDIndex(String strIDSymbol,String strExpID,Document bcxTree) throws Exception
	{
		// Method attributes -
		int[] return_index = new int[2];
		
		// The default value is -1 (if ID is not in the list);
		return_index[0] = -1;
		
		String strMeasurementXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column/@id";
		NodeList measurementNodeList = (NodeList) _xpath.evaluate(strMeasurementXPath, bcxTree, XPathConstants.NODESET);
		int NUMBER_OF_DATA_COLS = measurementNodeList.getLength();
		for (int col_index=0;col_index<NUMBER_OF_DATA_COLS;col_index++)
		{
		
			// Get the node -
			Node measurementNode = measurementNodeList.item(col_index);
			String strMeasurementID = measurementNode.getNodeValue();
			
			if (strMeasurementID.equalsIgnoreCase(strIDSymbol))
			{
				return_index[0] = col_index;
				break;
			}
		}
		
		// How many total cols? - Ok, we need to find the max of the column_index_in_file attributes -
		return_index[1] = NUMBER_OF_DATA_COLS;
		
		// return -
		return(return_index);
	}
	
	private ArrayList<String> getUniqueSpeciesList(String strSpeciesSymbol,String[] strAExcludeFragment) throws Exception
	{
		// Method attributes -
		ArrayList<String> arrayList = new ArrayList<String>();
		
		// Ok, lets get the list of species -
		ListOfSpecies listOfSpecies = _model.getListOfSpecies();
		long NUMBER_OF_SPECIES = _model.getNumSpecies();
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
        	// Get the species -
        	Species species_tmp = (Species)listOfSpecies.get(scounter);
        	String strSpeciesTest = species_tmp.getId();
        
        	
        	boolean blnFlag = strSpeciesTest.contains(strSpeciesSymbol);
        	
        	if (blnFlag)
        	{
        		boolean blnExclude = false;
        		
        		// do a second test - does this symbol contain any of the exclude fragments?
        		int NUMBER_EXCLUDE_FRAGMENTS = strAExcludeFragment.length;
        		for (int exclude_index=0;exclude_index<NUMBER_EXCLUDE_FRAGMENTS;exclude_index++)
        		{
        			if (strSpeciesTest.contains(strAExcludeFragment[exclude_index]))
        			{
        				blnExclude = true;
        				break;
        			}
        		}
        		

        		if (!arrayList.contains(strSpeciesTest) && !blnExclude)
        		{
        			arrayList.add(strSpeciesTest);
        			
        			System.out.println("Adding - "+strSpeciesTest+" for species = "+strSpeciesSymbol);
        			
        		}
        	}
        }
        
     
        
		
		// return -
		return(arrayList);
	}
	
	private ArrayList<String> getList(String strXPath,Document bcxTree) throws Exception
	{
		// Method attributes -
		ArrayList<String> arrayList = new ArrayList<String>();
		
		NodeList tmpNodeList = (NodeList) _xpath.evaluate(strXPath, bcxTree, XPathConstants.NODESET);
		int NUMBER_OF_ITEMS = tmpNodeList.getLength();
		
		for (int stimulus_index=0;stimulus_index<NUMBER_OF_ITEMS;stimulus_index++)
		{
			// Get the id string for this node -
			Node tmpNode = tmpNodeList.item(stimulus_index);
			String strNodeVal = tmpNode.getNodeValue();
			
			// put string in the arrayList -
			arrayList.add(strNodeVal);
		}
		
		// return -
		return(arrayList);
	}

	
	private ArrayList<String> getUniqueList(String strXPath,Document bcxTree) throws Exception
	{
		// Method attributes -
		ArrayList<String> arrayList = new ArrayList<String>();
		
		NodeList tmpNodeList = (NodeList) _xpath.evaluate(strXPath, bcxTree, XPathConstants.NODESET);
		int NUMBER_OF_ITEMS = tmpNodeList.getLength();
		
		for (int stimulus_index=0;stimulus_index<NUMBER_OF_ITEMS;stimulus_index++)
		{
			// Get the id string for this node -
			Node tmpNode = tmpNodeList.item(stimulus_index);
			String strNodeVal = tmpNode.getNodeValue();
			
			// put string in the arrayList -
			if (!arrayList.contains(strNodeVal) && !strNodeVal.isEmpty())
			{
				arrayList.add(strNodeVal);
			}
		}
		
		// return -
		return(arrayList);
	}
	
	
	
	// Build the SOSE buffer -
	public void buildSOSEBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree) throws Exception
	{
		// Method attributes -
		
		// Populate the buffer -
		buffer.append("function [ERR_VEC]=SOSE(DF,EDF,OBJ_INDEX,THREAD_SUFFIX)\n");
		buffer.append("\n");
		buffer.append("% Grab the objective function pointer -- \n");
		buffer.append("ERROR_STRUCT = EDF.ERROR_FUNCTION_ARRAY;\n");
		buffer.append("pObjectiveFunction = ERROR_STRUCT.FUNCTION(OBJ_INDEX).POINTER;\n");
		buffer.append("\n");
		buffer.append("% Grab the simulation time scale --\n");
		buffer.append("TSTART = ERROR_STRUCT.FUNCTION(OBJ_INDEX).TIME_START;\n");
		buffer.append("TSTOP = ERROR_STRUCT.FUNCTION(OBJ_INDEX).TIME_STOP;\n");
		buffer.append("Ts = ERROR_STRUCT.FUNCTION(OBJ_INDEX).TIME_STEP;\n");
		buffer.append("\n");
		buffer.append("% Evaluate the error function -- \n");
		buffer.append("ERR_EXP = feval(pObjectiveFunction,TSTART,TSTOP,Ts,DF,EDF,THREAD_SUFFIX);\n");
		buffer.append("ERR_VEC = ERR_EXP;\n");
		buffer.append("\n");
		buffer.append("return;\n");
	}
	
	// Build the 0 to 1 scaling -
	private void buildZeroToOneScalingBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		buffer.append("\n");
		buffer.append("% Scale the simulation data -- \n");
		buffer.append("[NROW,NCOL]=size(SIMULATION);\n");
		buffer.append("SCALED_SIMULATION_DATA = [];\n");
		buffer.append("for col_index=1:NCOL\n");
		buffer.append("\t % Grab a column - \n");
		buffer.append("\t TMP_COL = SIMULATION(:,col_index);\n");
		buffer.append("\n");
		buffer.append("\t % Calculate the MIN and the MAX -- \n");
		buffer.append("\t MIN = min(TMP_COL);\n");
		buffer.append("\t MAX = max(TMP_COL);\n");
		buffer.append("\n");
		buffer.append("\t % Compute the DIFF - \n");
		buffer.append("\t DIFF = min(MAX - MIN);\n");
		buffer.append("\n");
		buffer.append("\t % Check to see if DIFF is ok --\n");
		buffer.append("\t if (DIFF<EPS)\n");
		buffer.append("\t % Calculate the scaled data - \n");
		buffer.append("\t\t NROWS = length(TMP_COL);\n");
		buffer.append("\t\t DIFF = EPS*ones(NROWS,1);\n");
		buffer.append("\t end;\n");
		buffer.append("\n");
		buffer.append("\t % Calculate the scaled data - \n");
		buffer.append("\t TMP = (TMP_COL - MIN)./(DIFF);\n");
		buffer.append("\t SCALED_SIMULATION_DATA = [SCALED_SIMULATION_DATA TMP];\n");
		buffer.append("end;\n");
		buffer.append("\n");
	}
	
	// Build the BETA scaling -
	private void buildBetaScalingBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		// Get the group names -
		String strDataGroupNamesXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column/@data_group";
		ArrayList<String> groupNameList = getUniqueList(strDataGroupNamesXPath,bcxTree);
		int NUMBER_OF_GRP_NAMES = groupNameList.size();
		
		buffer.append("% Calculate the VARIANCE array -- \n");
		
		// We need to figure out what index time is (if any) -
		buffer.append("IDX_VEC = [");
		for (int index_grp_names = 0;index_grp_names<NUMBER_OF_GRP_NAMES;index_grp_names++)
		{
			// Get the column_index_in_file attribute from - 
			String strXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+groupNameList.get(index_grp_names)+"']/@column_index_in_file";
			String strColIDinFile = queryBCXTree(bcxTree,strXPath);
			buffer.append(strColIDinFile);
		
			if (index_grp_names==NUMBER_OF_GRP_NAMES-1)
			{
				buffer.append("];\n");
			}
			else
			{
				buffer.append(" ");
			}
		}
		
		for (int grp_index = 0;grp_index<NUMBER_OF_GRP_NAMES;grp_index++)
		{
			// Get the name of the grp -
			String strGrpName = groupNameList.get(grp_index);
			
			buffer.append("VARIANCE(:,");
			buffer.append(grp_index+1);
			buffer.append(") = ");
			
			String strDataColXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+strGrpName+"']/@coefficient_of_variation";
			
			// Get the strCV -
			String strCV = queryBCXTree(bcxTree,strDataColXPath);
			buffer.append("(");
			buffer.append(strCV);
			buffer.append("*");
			buffer.append("DATA(:,");
			buffer.append("IDX_VEC(");
			buffer.append(grp_index+1);
			buffer.append("))).^2;\n");
		}
		
		buffer.append("\n");
		
		// Check to see if variance is zero -
		buffer.append("% Check to see if variance is zero -\n");
		buffer.append("IDX_ZERO = find(VARIANCE==0);\n");
		buffer.append("VARIANCE(IDX_ZERO) = 1;\n");
		buffer.append("\n");
		
		buffer.append("% BETA scaling was selected - no scaling for the experimental data - \n");
		
		// We need to figure out what index time is (if any) -
		buffer.append("IDX_VEC = [");
		for (int index_grp_names = 0;index_grp_names<NUMBER_OF_GRP_NAMES;index_grp_names++)
		{
			// Get the column_index_in_file attribute from - 
			String strXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+groupNameList.get(index_grp_names)+"']/@column_index_in_file";
			String strColIDinFile = queryBCXTree(bcxTree,strXPath);
			buffer.append(strColIDinFile);
		
			if (index_grp_names==NUMBER_OF_GRP_NAMES-1)
			{
				buffer.append("];\n");
			}
			else
			{
				buffer.append(" ");
			}
		}
		
		buffer.append("SCALED_DATA = DATA(:,IDX_VEC);\n");
		buffer.append("\n");
		
		// Formulate SIMULATION array -
		// buildSimulationArray(buffer,bcxTree,_xmlPropTree,strExpID);
		
		buffer.append("\n");
		buffer.append("% Get the experimental time col - \n");
		String strXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@column_type='Time']/@column_index_in_file";
		String strColIDinFileTime = queryBCXTree(bcxTree,strXPath);
		buffer.append("TEXP = DATA(:,");
		buffer.append(strColIDinFileTime);
		buffer.append(");\n");
		buffer.append("\n");
		
		buffer.append("% Interpolate the simulation to the experimental time scale -- \n");
		buffer.append("ISIMULATION = interp1(TSIM,SIMULATION,TEXP);\n");
		buffer.append("\n");
		
		buffer.append("% Setup the BETA simulation data scaling - \n");
		buffer.append("\n");
		buffer.append("SCALED_SIMULATION_DATA = [];\n");
		buffer.append("NUMBER_OF_GROUPS = ");
		buffer.append(NUMBER_OF_GRP_NAMES);
		buffer.append(";\n");
		buffer.append("for col_index = 1:NUMBER_OF_GROUPS\n");
		buffer.append("\t % Setup the numerator - \n");
		buffer.append("\n");
		buffer.append("\t SF = (1./VARIANCE(:,col_index));\n");
		buffer.append("\t XM = DATA(:,IDX_VEC(col_index));\n");
		buffer.append("\t XS = ISIMULATION(:,col_index);\n");
		buffer.append("\t NUMERATOR = sum(SF.*(XM.*XS));\n");
		buffer.append("\n");
		buffer.append("\t % Setup the denominator - \n");
		buffer.append("\t STDEV = sqrt(VARIANCE(:,col_index));\n");
		buffer.append("\t TMP = (XS./STDEV).^2;\n");
		buffer.append("\t DENOMINATOR = sum(TMP);\n");
		buffer.append("\n");
		buffer.append("\t % Calculate the BETA - \n");
		buffer.append("\t BETA = NUMERATOR/DENOMINATOR;\n");
		buffer.append("\n");
		buffer.append("\t % Scaled the simulation data - \n");
		buffer.append("\t TMP_SIM_DATA = BETA*ISIMULATION(:,col_index);\n");
		buffer.append("\t SCALED_SIMULATION_DATA = [SCALED_SIMULATION_DATA TMP_SIM_DATA];\n");
		buffer.append("\n");
		buffer.append("end;\n");
		
	}
	
	// Build TEST_SIM_XX files -
	public void buildTestSimBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		// Get the experimental data filename -
		String strFileNameTotal = _xmlPropTree.getProperty("//experimental_data_structure_filename/text()");
		
		// Get the function -
        int last_dot = strFileNameTotal.lastIndexOf(".");
    	String strFncName = strFileNameTotal.substring(0,last_dot);
		
		// Populate the buffer -
		buffer.append("% Script to run the SIM_");
		buffer.append(strExpID);
		buffer.append(" -- \n");
		buffer.append("\n");
		buffer.append("% Load the experimental data -- \n");
		buffer.append("EDF = ");
		buffer.append(strFncName);
		buffer.append("(0,0,0,[]);\n");
		buffer.append("\n");
		buffer.append("% Get the experimental data from the EDF -- \n");
		buffer.append("DATA = EDF.DATA_ARRAY_");
		buffer.append(strExpID);
		buffer.append(";\n");
		buffer.append("\n");
		buffer.append("% Load the test parameters - \n");
		buffer.append("% EDIT THIS BLOCK \n");
		buffer.append("\n");
		buffer.append("% Set the simulation timescale - \n");
		
		// Get the time-scale -
		String strTSTARTXPath = "//experiment[@id='"+strExpID+"']/@simulation_time_start";
		String strTimeStart = queryBCXTree(bcxTree,strTSTARTXPath);
		
		String strTSTOPXPath = "//experiment[@id='"+strExpID+"']/@simulation_time_stop";
		String strTimeStop = queryBCXTree(bcxTree,strTSTOPXPath);
		
		String strTSXPath = "//experiment[@id='"+strExpID+"']/@simulation_time_step";
		String strTimeStep = queryBCXTree(bcxTree,strTSXPath);
		
		buffer.append("TSTART = ");
		buffer.append(strTimeStart);
		buffer.append(";\n");
		buffer.append("TSTOP = ");
		buffer.append(strTimeStop);
		buffer.append(";\n");
		buffer.append("Ts = ");
		buffer.append(strTimeStep);
		buffer.append(";\n");
		buffer.append("\n");
		
		buffer.append("% Find steady-state - \n");
		buffer.append("[TSS,XSS] = FindSteadyState(DF,'TEST_CALL');\n");
		buffer.append("IC = XSS(:,end);\n");
		buffer.append("DF.INITIAL_CONDITIONS = IC;\n");
		buffer.append("\n");
		buffer.append("% Call the SIM_x.m function - \n");
		buffer.append("[TSIM,XSIM]=SIM_");
		buffer.append(strExpID);
		buffer.append("(TSTART,TSTOP,Ts,DF,'TEST_CALL');\n");
		
		// Ok, so normally we would be done - but because I'm an outsanding teacher, advisor and all around great human being I will also provide
		// the scaled version to you bitches...
		
		// Check on the scaling -
		// XPath to determine scaling -
		String strScalingXPath = "//experiment[@id='"+strExpID+"']/@scaling";
		String strScaling = queryBCXTree(bcxTree,strScalingXPath);
		
		
		buffer.append("\n");
		// First, build the simulation array -
		buildSimulationArray(buffer,bcxTree,_xmlPropTree,strExpID);
		
		// Code for the ZERO_TO_ONE scaling option -
		if (strScaling.equalsIgnoreCase("ZERO_TO_ONE"))
		{
			// Process zero to one scaling -
			buildZeroToOneScalingBuffer(buffer,bcxTree,_xmlPropTree,strExpID);	
		}
		else if (strScaling.equalsIgnoreCase("BETA"))
		{
			// Process the BETA buffer -
			buildBetaScalingBuffer(buffer,bcxTree,_xmlPropTree,strExpID);	
		}
		
	}
	
	// Build the MOSE buffer -
	public void buildMOSEBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree) throws Exception
	{
		// Method attributes -
		
		// Populate the buffer -
		buffer.append("function [ERR_VEC]=MOSE(DF,EDF,THREAD_SUFFIX)\n");
		buffer.append("\n");
		buffer.append("% Grab the info about the array of error function pointers from the EDF - \n");
		buffer.append("NUMBER_OF_OBJECTIVES = EDF.NUMBER_OF_OBJECTIVES;\n");
		buffer.append("ERR_VEC = zeros(NUMBER_OF_OBJECTIVES,1);\n");
		buffer.append("ERROR_STRUCT = EDF.ERROR_FUNCTION_ARRAY;\n");
		buffer.append("\n");
		buffer.append("% main loop -- iterate through the objectives and get the error -- \n");
		buffer.append("for obj_index=1:NUMBER_OF_OBJECTIVES\n");
		buffer.append("\t % Grab the objective function pointer -- \n");
		buffer.append("\t pObjectiveFunction = ERROR_STRUCT.FUNCTION(obj_index).POINTER;\n");
		buffer.append("\n ");
		buffer.append("% Grab the simulation time scale --\n");
		buffer.append("\t TSTART = ERROR_STRUCT.FUNCTION(obj_index).TIME_START;\n");
		buffer.append("\t TSTOP = ERROR_STRUCT.FUNCTION(obj_index).TIME_STOP;\n");
		buffer.append("\t Ts = ERROR_STRUCT.FUNCTION(obj_index).TIME_STEP;\n");
		buffer.append("\n");
		buffer.append("\t % Evaluate the error function -- \n");
		buffer.append("\t ERR_EXP = feval(pObjectiveFunction,TSTART,TSTOP,Ts,DF,EDF,THREAD_SUFFIX);\n");
		buffer.append("\n");
		buffer.append("\t % Grab this error value and go around again -- \n");
		buffer.append("\t ERR_VEC(obj_index,1) = ERR_EXP;\n");
		buffer.append("end;\n");
		buffer.append("\n");
		
		buffer.append("return;\n");
	}
	
	private void buildSimulationArray(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		String strUseExactNameXPath = "//experiment[@id='"+strExpID+"']/measurement_file/@search_network";
		String strUseExactName = queryBCXTree(bcxTree,strUseExactNameXPath);
		
		// Get the exclude -
		String strExcludeXPath = "//experiment[@id='"+strExpID+"']/measurement_file/@exclude";
		String strExclude = queryBCXTree(bcxTree,strExcludeXPath);
		
		// Ok, we need to parse the exclude string -
		String[] strExcludeTokens = strExclude.split(",");
		
		// Get the cols -
		buffer.append("% Construct the SIMULATION array -- \n");
		buffer.append("SIMULATION = [];\n");
		buffer.append("\n");
		buffer.append("% Process data groups -- \n");
		String strDataGroupXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column/@data_group";
		ArrayList<String> groupList = getUniqueList(strDataGroupXPath,bcxTree);
		int NUMBER_OF_DATA_GROUPS = groupList.size();
		int counter = 1;
		for (int group_index=0;group_index<NUMBER_OF_DATA_GROUPS;group_index++)
		{
			String strGrpName = groupList.get(group_index);
			if (!strGrpName.isEmpty())
			{
				// Get species that are involved with this group --
				buffer.append("% ");
				buffer.append(strGrpName);
				buffer.append(" -- \n");
				buffer.append("TMP_GRP = [];\n");
				buffer.append("TMP_GRP = [TMP_GRP ");
			
				
				if (strUseExactName.equalsIgnoreCase("FALSE"))
				{
					String strDataGroupSpeciesXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+strGrpName+"']/@species";
					ArrayList<String> groupSpeciesList = getList(strDataGroupSpeciesXPath,bcxTree);
					int NUMBER_OF_SPECIES = groupSpeciesList.size();
					for (int grp_species_index=0;grp_species_index<NUMBER_OF_SPECIES;grp_species_index++)
					{
						// Get the species in this grp -
						String strSpeciesSymbol = groupSpeciesList.get(grp_species_index);
					
						// What is the index?
						int int_species_index = findSpeciesIndex(strSpeciesSymbol);
						buffer.append("XSIM(:,");
						buffer.append(int_species_index);
						buffer.append(") ");
					}
				
					// Ok, so lets close and sum -
					buffer.append("];\n");
					buffer.append("GROUP(:,");
					buffer.append(counter);
					buffer.append(") = sum(TMP_GRP,2);\n");
					buffer.append("\n");
					counter++;
				}
				else
				{
					String strDataGroupSpeciesXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+strGrpName+"']/@species";
					ArrayList<String> groupSpeciesList = getList(strDataGroupSpeciesXPath,bcxTree);
					ArrayList<String> localList = new ArrayList<String>();
					
					// Ok, so now we need to check for *like* species -
					int NUMBER_OF_SPECIES = groupSpeciesList.size();
					for (int grp_species_index=0;grp_species_index<NUMBER_OF_SPECIES;grp_species_index++)
					{
						// Get the species in this grp -
						String strSpeciesSymbol = groupSpeciesList.get(grp_species_index);
						
						// Get the a unique list -
						ArrayList<String> tmpLocalList = getUniqueSpeciesList(strSpeciesSymbol,strExcludeTokens);
						
						// Add this to the localList -
						localList.addAll(tmpLocalList);
					}
					
					// Ok, when I get here I have a localList -
					int NUMBER_SPECIES_LOCAL_LIST = localList.size();
					for (int local_index=0;local_index<NUMBER_SPECIES_LOCAL_LIST;local_index++)
					{
						// Get the species fragment -
						String strSpeciesFragment = localList.get(local_index);
					
						// What is the index?
						int int_species_index = findSpeciesIndex(strSpeciesFragment);
						buffer.append("XSIM(:,");
						buffer.append(int_species_index);
						buffer.append(") ");
					}
					
					// Ok, so lets close and sum -
					buffer.append("];\n");
					buffer.append("GROUP(:,");
					buffer.append(counter);
					buffer.append(") = sum(TMP_GRP,2);\n");
					buffer.append("\n");
					counter++;
				}
			}
		}
		
		buffer.append("% Set GROUPS to simulation - \n");
		buffer.append("SIMULATION = GROUP; \n");
		
	}
	
	// Process ZERO_TO_ONE scaling -
	private void buildZeroToOneBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		// Figure out all species?
		
		// Get the group names -
		String strDataGroupNamesXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column/@data_group";
		ArrayList<String> groupNameList = getUniqueList(strDataGroupNamesXPath,bcxTree);
		int NUMBER_OF_GRP_NAMES = groupNameList.size();
		
		// Check to determine which scaling -
		buffer.append("% Scale the experimental data -- \n");
		buffer.append("[NROW,NCOL]=size(DATA);\n");
		buffer.append("SCALED_DATA = [];\n");
		buffer.append("\n");
		
		buffer.append("% Calculate the VARIANCE array -- \n");
		
		// We need to figure out what index time is (if any) -
		buffer.append("IDX_VEC = [");
		for (int index_grp_names = 0;index_grp_names<NUMBER_OF_GRP_NAMES;index_grp_names++)
		{
			// Get the column_index_in_file attribute from - 
			String strXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+groupNameList.get(index_grp_names)+"']/@column_index_in_file";
			String strColIDinFile = queryBCXTree(bcxTree,strXPath);
			buffer.append(strColIDinFile);
		
			if (index_grp_names==NUMBER_OF_GRP_NAMES-1)
			{
				buffer.append("];\n");
			}
			else
			{
				buffer.append(" ");
			}
		}
		
		for (int grp_index = 0;grp_index<NUMBER_OF_GRP_NAMES;grp_index++)
		{
			// Get the name of the grp -
			String strGrpName = groupNameList.get(grp_index);
			
			buffer.append("VARIANCE(:,");
			buffer.append(grp_index+1);
			buffer.append(") = ");
			
			String strDataColXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+strGrpName+"']/@coefficient_of_variation";
			
			// Get the strCV -
			String strCV = queryBCXTree(bcxTree,strDataColXPath);
			buffer.append("(");
			buffer.append(strCV);
			buffer.append("*");
			buffer.append("DATA(:,");
			buffer.append("IDX_VEC(");
			buffer.append(grp_index+1);
			buffer.append("))).^2;\n");
		}
		
		buffer.append("\n");
		
		// Check to see if variance is zero -
		buffer.append("% Check to see if variance is zero -\n");
		buffer.append("IDX_ZERO = find(VARIANCE==0);\n");
		buffer.append("VARIANCE(IDX_ZERO) = 1;\n");
		buffer.append("\n");
		
		buffer.append("% Scale the experimental data -- \n");
		buffer.append("NCOL_MINUS_TIME = length(IDX_VEC);\n");
		buffer.append("for tmp_col_index=1:NCOL_MINUS_TIME\n");
		buffer.append("\t % Get the col index -- \n");
		buffer.append("\t col_index = IDX_VEC(tmp_col_index);\n");
		buffer.append("\n");
		buffer.append("\t % Grab a column - \n");
		buffer.append("\t TMP_COL = DATA(:,col_index);\n");
		buffer.append("\n");
		buffer.append("\t % Calculate the MIN and the MAX -- \n");
		buffer.append("\t MIN = min(TMP_COL);\n");
		buffer.append("\t MAX = max(TMP_COL);\n");
		buffer.append("\n");
		buffer.append("\t % Compute the DIFF - \n");
		buffer.append("\t DIFF = min(MAX - MIN);\n");
		buffer.append("\n");
		buffer.append("\t % Check to see if DIFF is ok --\n");
		buffer.append("\t if (DIFF<EPS)\n");
		buffer.append("\t % Calculate the scaled data - \n");
		buffer.append("\t\t NROWS = length(TMP_COL);\n");
		buffer.append("\t\t DIFF = EPS*ones(NROWS,1);\n");
		buffer.append("\t end;\n");
		buffer.append("\n");
		buffer.append("\t % Calculate the scaled data - \n");
		buffer.append("\t TMP = (TMP_COL - MIN)./(DIFF);\n");
		buffer.append("\t SCALED_DATA = [SCALED_DATA TMP];\n");
		buffer.append("end;\n");
		buffer.append("\n");
		
		// Formulate SIMULATION array -
		buildSimulationArray(buffer,bcxTree,_xmlPropTree,strExpID);
		
		// Add the groups to the simulation array -
		buffer.append("% Add the groups to the simulation array -- \n");
		buffer.append("SIMULATION = GROUP;\n");
		buffer.append("\n");
		
		buffer.append("\n");
		buffer.append("% Scale the simulation data -- \n");
		buffer.append("[NROW,NCOL]=size(SIMULATION);\n");
		buffer.append("SCALED_SIMULATION_DATA = [];\n");
		buffer.append("for col_index=1:NCOL\n");
		buffer.append("\t % Grab a column - \n");
		buffer.append("\t TMP_COL = SIMULATION(:,col_index);\n");
		buffer.append("\n");
		buffer.append("\t % Calculate the MIN and the MAX -- \n");
		buffer.append("\t MIN = min(TMP_COL);\n");
		buffer.append("\t MAX = max(TMP_COL);\n");
		buffer.append("\n");
		buffer.append("\t % Compute the DIFF - \n");
		buffer.append("\t DIFF = min(MAX - MIN);\n");
		buffer.append("\n");
		buffer.append("\t % Check to see if DIFF is ok --\n");
		buffer.append("\t if (DIFF<EPS)\n");
		buffer.append("\t % Calculate the scaled data - \n");
		buffer.append("\t\t NROWS = length(TMP_COL);\n");
		buffer.append("\t\t DIFF = EPS*ones(NROWS,1);\n");
		buffer.append("\t end;\n");
		buffer.append("\n");
		buffer.append("\t % Calculate the scaled data - \n");
		buffer.append("\t TMP = (TMP_COL - MIN)./(DIFF);\n");
		buffer.append("\t SCALED_SIMULATION_DATA = [SCALED_SIMULATION_DATA TMP];\n");
		buffer.append("end;\n");
		buffer.append("\n");
		
		buffer.append("% Calculate the error in the scale -- \n");
		for (int grp_index = 0;grp_index<NUMBER_OF_GRP_NAMES;grp_index++)
		{
			// Get the name of the grp -
			String strGrpName = groupNameList.get(grp_index);
			
			// Get the scale -
			String strScaleXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+strGrpName+"']/@scale";
			String strScale = queryBCXTree(bcxTree,strScaleXPath);
			
			// put the scale lines -
			buffer.append("SCALE_ERR(");
			buffer.append(grp_index+1);
			buffer.append(") = (1/");
			buffer.append(strScale);
			buffer.append(")*(");
			buffer.append(strScale);
			buffer.append(" - max(SIMULATION(:,");
			buffer.append(grp_index+1);
			buffer.append(")));\n");
		}
		buffer.append("\n");
		
		buffer.append("% Get the experimental time col - \n");
		String strXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@column_type='Time']/@column_index_in_file";
		String strColIDinFileTime = queryBCXTree(bcxTree,strXPath);
		buffer.append("TEXP = DATA(:,");
		buffer.append(strColIDinFileTime);
		buffer.append(");\n");
		buffer.append("\n");
		
		buffer.append("% Interpolate the simulation to the experimental time scale -- \n");
		buffer.append("ISCALED_SIMULATION_DATA = interp1(TSIM,SCALED_SIMULATION_DATA,TEXP);\n");
		buffer.append("\n");
		
		buffer.append("% Calculate the error - \n");
		buffer.append("for col_index=1:NCOL\n");
		buffer.append("\t SF = (1./VARIANCE(:,col_index));\n");
		buffer.append("\t RELATIVE_ERR = sum(SF.*((ISCALED_SIMULATION_DATA(:,col_index) - SCALED_DATA(:,col_index)).^2));\n");
		buffer.append("\t SCALE_ERR_GRP = (SCALE_ERR(col_index))^2;\n");
		buffer.append("\t ERR_ARR(col_index,1) = RELATIVE_ERR + SCALE_ERR_GRP;\n");
		buffer.append("end;\n");
		buffer.append("\n");	
	}
	
	// Build the BETA buffer -
	private void buildBetaBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		// Get the group names -
		String strDataGroupNamesXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column/@data_group";
		ArrayList<String> groupNameList = getUniqueList(strDataGroupNamesXPath,bcxTree);
		int NUMBER_OF_GRP_NAMES = groupNameList.size();
		
		buffer.append("% Calculate the VARIANCE array -- \n");
		
		// We need to figure out what index time is (if any) -
		buffer.append("IDX_VEC = [");
		for (int index_grp_names = 0;index_grp_names<NUMBER_OF_GRP_NAMES;index_grp_names++)
		{
			// Get the column_index_in_file attribute from - 
			String strXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+groupNameList.get(index_grp_names)+"']/@column_index_in_file";
			String strColIDinFile = queryBCXTree(bcxTree,strXPath);
			buffer.append(strColIDinFile);
		
			if (index_grp_names==NUMBER_OF_GRP_NAMES-1)
			{
				buffer.append("];\n");
			}
			else
			{
				buffer.append(" ");
			}
		}
		
		for (int grp_index = 0;grp_index<NUMBER_OF_GRP_NAMES;grp_index++)
		{
			// Get the name of the grp -
			String strGrpName = groupNameList.get(grp_index);
			
			buffer.append("VARIANCE(:,");
			buffer.append(grp_index+1);
			buffer.append(") = ");
			
			String strDataColXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+strGrpName+"']/@coefficient_of_variation";
			
			// Get the strCV -
			String strCV = queryBCXTree(bcxTree,strDataColXPath);
			buffer.append("(");
			buffer.append(strCV);
			buffer.append("*");
			buffer.append("DATA(:,");
			buffer.append("IDX_VEC(");
			buffer.append(grp_index+1);
			buffer.append("))).^2;\n");
		}
		
		buffer.append("\n");
		
		// Check to see if variance is zero -
		buffer.append("% Check to see if variance is zero -\n");
		buffer.append("IDX_ZERO = find(VARIANCE==0);\n");
		buffer.append("VARIANCE(IDX_ZERO) = 1;\n");
		buffer.append("\n");
		
		buffer.append("% BETA scaling was selected - no scaling for the experimental data - \n");
		
		// We need to figure out what index time is (if any) -
		buffer.append("IDX_VEC = [");
		for (int index_grp_names = 0;index_grp_names<NUMBER_OF_GRP_NAMES;index_grp_names++)
		{
			// Get the column_index_in_file attribute from - 
			String strXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+groupNameList.get(index_grp_names)+"']/@column_index_in_file";
			String strColIDinFile = queryBCXTree(bcxTree,strXPath);
			buffer.append(strColIDinFile);
		
			if (index_grp_names==NUMBER_OF_GRP_NAMES-1)
			{
				buffer.append("];\n");
			}
			else
			{
				buffer.append(" ");
			}
		}
		
		buffer.append("SCALED_DATA = DATA(:,IDX_VEC);\n");
		buffer.append("\n");
		
		// Formulate SIMULATION array -
		buildSimulationArray(buffer,bcxTree,_xmlPropTree,strExpID);
		
		buffer.append("\n");
		buffer.append("% Get the experimental time col - \n");
		String strXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@column_type='Time']/@column_index_in_file";
		String strColIDinFileTime = queryBCXTree(bcxTree,strXPath);
		buffer.append("TEXP = DATA(:,");
		buffer.append(strColIDinFileTime);
		buffer.append(");\n");
		buffer.append("\n");
		
		buffer.append("% Interpolate the simulation to the experimental time scale -- \n");
		buffer.append("ISIMULATION = interp1(TSIM,SIMULATION,TEXP);\n");
		buffer.append("\n");
		
		buffer.append("% Setup the BETA simulation data scaling - \n");
		buffer.append("\n");
		buffer.append("SCALED_SIMULATION_DATA = [];\n");
		buffer.append("NUMBER_OF_GROUPS = ");
		buffer.append(NUMBER_OF_GRP_NAMES);
		buffer.append(";\n");
		buffer.append("for col_index = 1:NUMBER_OF_GROUPS\n");
		buffer.append("\t % Setup the numerator - \n");
		buffer.append("\n");
		buffer.append("\t SF = (1./VARIANCE(:,col_index));\n");
		buffer.append("\t XM = DATA(:,IDX_VEC(col_index));\n");
		buffer.append("\t XS = ISIMULATION(:,col_index);\n");
		buffer.append("\t NUMERATOR = sum(SF.*(XM.*XS));\n");
		buffer.append("\n");
		buffer.append("\t % Setup the denominator - \n");
		buffer.append("\t STDEV = sqrt(VARIANCE(:,col_index));\n");
		buffer.append("\t TMP = (XS./STDEV).^2;\n");
		buffer.append("\t DENOMINATOR = sum(TMP);\n");
		buffer.append("\n");
		buffer.append("\t % Calculate the BETA - \n");
		buffer.append("\t BETA = NUMERATOR/DENOMINATOR;\n");
		buffer.append("\n");
		buffer.append("\t % Scaled the simulation data - \n");
		buffer.append("\t TMP_SIM_DATA = BETA*ISIMULATION(:,col_index);\n");
		buffer.append("\t SCALED_SIMULATION_DATA = [SCALED_SIMULATION_DATA TMP_SIM_DATA];\n");
		buffer.append("\n");
		buffer.append("end;\n");
		
		buffer.append("\n");
		buffer.append("% Calculate the error - \n");
		buffer.append("NUMBER_OF_GROUPS = ");
		buffer.append(NUMBER_OF_GRP_NAMES);
		buffer.append(";\n");
		buffer.append("for col_index=1:NUMBER_OF_GROUPS\n");
		buffer.append("\t SF = (1./VARIANCE(:,col_index));\n");
		buffer.append("\t RELATIVE_ERR = sum(SF.*((SCALED_SIMULATION_DATA(:,col_index) - SCALED_DATA(:,col_index)).^2));\n");
		buffer.append("\t ERR_ARR(col_index,1) = RELATIVE_ERR;\n");
		buffer.append("end;\n");
		buffer.append("\n");
		
	}
	
	
	// Build the error buffer -
	public void buildErrorBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		// Populate the buffer -
		buffer.append("function [ERR]=ERR_");
		buffer.append(strExpID);
		buffer.append("(TSTART,TSTOP,Ts,DF,EDF,THREAD_SUFFIX)\n");
		buffer.append("\n");
		
		// Run the simulation -
		buffer.append("% Set the EPS - \n");
		buffer.append("EPS = 1e-6;\n");
		buffer.append("\n");
		buffer.append("% Run the simulation -- \n");
		buffer.append("[TSIM,XSIM]=SIM_");
		buffer.append(strExpID);
		buffer.append("(TSTART,TSTOP,Ts,DF,THREAD_SUFFIX);\n");
		buffer.append("\n");
		
		// Load the experimental data -
		buffer.append("% Get the experimental data from the EDF -- \n");
		buffer.append("DATA = EDF.DATA_ARRAY_");
		buffer.append(strExpID);
		buffer.append(";\n");
		buffer.append("\n");
	
		// XPath to determine scaling -
		String strScalingXPath = "//experiment[@id='"+strExpID+"']/@scaling";
		String strScaling = queryBCXTree(bcxTree,strScalingXPath);
		
		
		// Code for the ZERO_TO_ONE scaling option -
		if (strScaling.equalsIgnoreCase("ZERO_TO_ONE"))
		{
			// Process zero to one scaling -
			buildZeroToOneBuffer(buffer,bcxTree,_xmlPropTree,strExpID);	
		}
		else if (strScaling.equalsIgnoreCase("BETA"))
		{
			// Process the BETA buffer -
			buildBetaBuffer(buffer,bcxTree,_xmlPropTree,strExpID);	
		}
		
		
		buffer.append("\n");		
		buffer.append("% Compute ERR -- \n");
		buffer.append("ERR = sum(ERR_ARR);\n");
		buffer.append("\n");
		buffer.append("return;\n");
		
	}
	
	private void populateSteadyStateSimulationBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		// Populate the buffer -
		buffer.append("function [TSIM,XSIM]=SIM_");
		buffer.append(strExpID);
		buffer.append("(TSTART,TSTOP,Ts,DF,THREAD_SUFFIX)\n");
		buffer.append("\n");
		
		// Get the initial conditions from the DF -
		buffer.append("% Get the initial conditions from the DataFile -\n");
		buffer.append("IC = DF.INITIAL_CONDITIONS;\n");
		buffer.append("\n");
		buffer.append("% Setup the stimulus -- \n");
		buffer.append("\n");
		
		// Get the species symbol -
		String strSpeciesXPath = "//experiment[@id='"+strExpID+"']/stimulus/@species";
		NodeList stimulusNodeList = (NodeList) _xpath.evaluate(strSpeciesXPath, bcxTree, XPathConstants.NODESET);
		int NUMBER_OF_STIMULUS_SPECIES = stimulusNodeList.getLength();
		
		// If we have a stimulus - then we need to load the sbml xml  -
		if (NUMBER_OF_STIMULUS_SPECIES!=0)
		{
			loadSBMLModel(_xmlPropTree);
		}
		
		// Tmp flag to make sure we only execute the ODE call once -
		boolean blnTmpFlag = true;
		
		for (int stimulus_index=0;stimulus_index<NUMBER_OF_STIMULUS_SPECIES;stimulus_index++)
		{
			// Get the id string for this experiment -
			Node stimulusNode = stimulusNodeList.item(stimulus_index);
			String strStimulusSpecies = stimulusNode.getNodeValue();
			
			// label -
			buffer.append("% ");
			buffer.append(strStimulusSpecies);
			buffer.append("\n");

			// Ok, what is the time and value for this species -
			String strStimulusTimeXPath = "//experiment[@id='"+strExpID+"']/stimulus[@species='" + strStimulusSpecies+"']/@time";
			String strStimulusValueXPath = "//experiment[@id='"+strExpID+"']/stimulus[@species='" + strStimulusSpecies+"']/@value";
			String strStimulusTime = queryBCXTree(bcxTree,strStimulusTimeXPath);
			String strStimulusValue = queryBCXTree(bcxTree,strStimulusValueXPath);
			
			// what is this index?
			int tmp_index = findSpeciesIndex(strStimulusSpecies);
			
			// Find the index of this species -
			buffer.append("IC(");
			buffer.append(tmp_index);
			buffer.append(",1) = ");
			buffer.append(strStimulusValue);
			buffer.append(";\n");
		}
		
		// Ok, when I get here I just need to call the FindSteadyState -
		buffer.append("DF.INITIAL_CONDITIONS = IC;\n");
		buffer.append("[TSS,XSS] = FindSteadyState(DF,THREAD_SUFFIX);\n");
		buffer.append("TSIM = TSS(end);\n");
		buffer.append("XSIM = transpose(XSS(end,:));\n");
		buffer.append("return;\n");
	}
	
	// Build the experimental simulation buffer -
	public void buildSimFileBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		String strStimulusValue = "";
		
		
		// Need to check to see if this experiment is a steady-state experiment -
		String strSSXPath = "//experiment[@id='"+strExpID+"']/@steady_state";
		String strSSValue = queryBCXTree(bcxTree,strSSXPath);
		if (strSSValue.equalsIgnoreCase("TRUE"))
		{
			// Populate the buffer for a *steady-state* simulation -
			populateSteadyStateSimulationBuffer(buffer,bcxTree,_xmlPropTree,strExpID);
		}
		else
		{
			// Populate the buffer -
			buffer.append("function [TSIM,XSIM]=SIM_");
			buffer.append(strExpID);
			buffer.append("(TSTART,TSTOP,Ts,DF,THREAD_SUFFIX)\n");
			buffer.append("\n");
			
			// Get the initial conditions from the DF -
			buffer.append("% Get the initial conditions from the DataFile -\n");
			buffer.append("IC = DF.INITIAL_CONDITIONS;\n");
			buffer.append("\n");
			buffer.append("% Setup the stimulus -- \n");
			buffer.append("\n");
			
			
			// Get the species symbol -
			String strSpeciesXPath = "//experiment[@id='"+strExpID+"']/stimulus/@species";
			NodeList stimulusNodeList = (NodeList) _xpath.evaluate(strSpeciesXPath, bcxTree, XPathConstants.NODESET);
			int NUMBER_OF_STIMULUS_SPECIES = stimulusNodeList.getLength();
			
			// If we have a stimulus - then we need to load the sbml xml  -
			if (NUMBER_OF_STIMULUS_SPECIES!=0)
			{
				loadSBMLModel(_xmlPropTree);
			}
			
			// Tmp flag to make sure we only execute the ODE call once -
			boolean blnTmpFlag = true;
			
			for (int stimulus_index=0;stimulus_index<NUMBER_OF_STIMULUS_SPECIES;stimulus_index++)
			{
				// Get the id string for this experiment -
				Node stimulusNode = stimulusNodeList.item(stimulus_index);
				String strStimulusSpecies = stimulusNode.getNodeValue();
				
				// label -
				buffer.append("% ");
				buffer.append(strStimulusSpecies);
				buffer.append("\n");

				// Ok, what is the time and value for this species -
				String strStimulusTimeXPath = "//experiment[@id='"+strExpID+"']/stimulus[@species='" + strStimulusSpecies+"']/@time";
				String strStimulusValueXPath = "//experiment[@id='"+strExpID+"']/stimulus[@species='" + strStimulusSpecies+"']/@value";
				String strStimulusTime = queryBCXTree(bcxTree,strStimulusTimeXPath);
				strStimulusValue = queryBCXTree(bcxTree,strStimulusValueXPath);
				
				// what is this index?
				int tmp_index = findSpeciesIndex(strStimulusSpecies);
				
				// Check to see of stimulus time equals 0 - 
				if (strStimulusTime.equalsIgnoreCase("0.0") || strStimulusTime.equalsIgnoreCase("0"))
				{
				
					// Find the index of this species -
					buffer.append("IC(");
					buffer.append(tmp_index);
					buffer.append(",1) = ");
					buffer.append(strStimulusValue);
					buffer.append(";\n");
					buffer.append("\n");
				}
				else
				{
					if (blnTmpFlag)
					{
						// if I get here, then I'm adding stimulus after some period of time -
						buffer.append("% Run simulation from 0 to ");
						buffer.append(strStimulusTime);
						buffer.append(" -- \n");
						buffer.append("\n");
						buffer.append("% Local time-scale -- \n");
						buffer.append("TSTART_LOCAL = 0;\n");
						buffer.append("TSTOP_LOCAL = ");
						buffer.append(strStimulusTime);
						buffer.append(" - Ts;\n");
						buffer.append("\n");
						buffer.append("% Call the ODESolver - \n");
						buffer.append("DF.INITIAL_CONDITIONS = IC;\n");
						buffer.append("[TSIM,XSIM]=LSODECallWrapper(TSTART_LOCAL,TSTOP_LOCAL,Ts,DF,'");
						buffer.append("SIM_");
						buffer.append(strExpID);
						buffer.append(".dat',THREAD_SUFFIX);\n");
						buffer.append("\n");
						
						buffer.append("% Reset the TSTART -\n");
						buffer.append("TSTART = ");
						buffer.append(strStimulusTime);
						buffer.append(";\n");
						buffer.append("\n");
						
						buffer.append("% Issue the stimulus - \n");
						buffer.append("IC = transpose(XSIM(end,:));\n");
						
						// set the flag to false, so when I come around again I don't get into this block -
						blnTmpFlag = false;
					}
				
					buffer.append("IC(");
					buffer.append(tmp_index);
					buffer.append(",1) = ");
					buffer.append(strStimulusValue);
					buffer.append(";\n");
				}
			}
					
			// Run the simulation -
			if (strStimulusValue.equalsIgnoreCase("0.0") || strStimulusValue.equalsIgnoreCase("0"))
			{
				buffer.append("% Recycle the steady-state because there is no stimulus.\n");
				buffer.append("% Setup the time scale - \n");
				buffer.append("TSIM = TSTART:Ts:TSTOP;\n");
				buffer.append("\n");
				buffer.append("% Formulate the XSIM - \n");
				buffer.append("NT = length(TSIM);\n");
				buffer.append("XSIM = ones(NT,1)*transpose(IC);\n");
				buffer.append("return;\n");
			}
			else
			{
				buffer.append("% Solve the mass-balance equations.\n");
				buffer.append("DF.INITIAL_CONDITIONS = IC;\n");
				buffer.append("[TSIM,XSIM]=LSODECallWrapper(TSTART,TSTOP,Ts,DF,'");
				buffer.append("SIM_");
				buffer.append(strExpID);
				buffer.append(".dat',THREAD_SUFFIX);\n");
				buffer.append("\n");
				buffer.append("return;\n");
			}
		}
	}
	
	
	// Build the Experimental struct buffer -
	public void buildExperimentalDataStructBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree) throws Exception
	{
		// Method attributes -
		ArrayList<String> expIDList = new ArrayList<String>();
		
		buffer.append("function EDF = ");
		
		// Get the filename -
		String strFileNameTotal = _xmlPropTree.getProperty("//experimental_data_structure_filename/text()");
		
		// Get the function -
        int last_dot = strFileNameTotal.lastIndexOf(".");
    	String strFncName = strFileNameTotal.substring(0,last_dot);
		
    	buffer.append(strFncName);
    	buffer.append("(TSTART,TSTOP,Ts)\n");
		buffer.append("% Load the files from disk that hold the experimental data.\n");
		buffer.append("\n");
		
		// Get the list of measurement files from the tree -
		String strExpXPath = "//experiment/@id";
		NodeList expNodeList = (NodeList) _xpath.evaluate(strExpXPath, bcxTree, XPathConstants.NODESET);
		int NUMBER_OF_EXPERIMENTS = expNodeList.getLength();
		for (int exp_index=0;exp_index<NUMBER_OF_EXPERIMENTS;exp_index++)
		{
			// Get the id string for this experiment -
			Node expNode = expNodeList.item(exp_index);
			String strExpId = expNode.getNodeValue();
			
			// Store this id for later ..
			expIDList.add(strExpId);
			
			// Formulate the comments above the data file. These comments will hold meta data so that we remember 
			// what is in the data file and where we got the data from -
			
			// Get the file name -
			String strFileNameXPath = "//experiment[@id='"+strExpId+"']/measurement_file/@filename";
			String strFileName = queryBCXTree(bcxTree,strFileNameXPath);
			
			buffer.append("% ");
			buffer.append(strFileName);
			buffer.append("\n");
			
			// Get any citations or comments -
			String strCiteXPath = "//experiment[@id='"+strExpId+"']/@cite";
			String strCite = queryBCXTree(bcxTree,strCiteXPath);
			buffer.append("% Citation: ");
			buffer.append(strCite);
			buffer.append("\n");
			
			// Get the file-type -
			String strFileTypeXPath = "//experiment[@id='"+strExpId+"']/measurement_file/@file_type";
			String strFileType = queryBCXTree(bcxTree,strFileTypeXPath);
			buffer.append("% File_type: ");
			buffer.append(strFileType);
			buffer.append("\n");
			
			// Get the column descriptions 
			String strColsXPath = "//experiment[@id='"+strExpId+"']/measurement_file/data_column/@id";
			NodeList dataColList = (NodeList) _xpath.evaluate(strColsXPath, bcxTree, XPathConstants.NODESET);
			int NUMBER_OF_DATACOLS = dataColList.getLength();
			for (int data_col_index = 0;data_col_index<NUMBER_OF_DATACOLS;data_col_index++)
			{
				// Get the id for this col -
				Node colNode = dataColList.item(data_col_index);
				String strColID = colNode.getNodeValue();
				
				// Is this col Time or species?
				String strColTypeXPath = "//experiment[@id='"+strExpId+"']/measurement_file/data_column[@id='"+strColID+"']/@column_type";
				String strColType = queryBCXTree(bcxTree,strColTypeXPath);
				
				if (strColType.equalsIgnoreCase("Time"))
				{
					buffer.append("% COL ");
					buffer.append(data_col_index);
					buffer.append(": ");
					buffer.append("time\n");
				}
				else
				{
					buffer.append("% COL ");
					buffer.append(data_col_index);
					buffer.append(": ");
					
					// Ok, If I get here then I need to get the species 
					String strSpeciesXPath = "//experiment[@id='"+strExpId+"']/measurement_file/data_column[@id='"+strColID+"']/@species";
					String strSpecies = queryBCXTree(bcxTree,strSpeciesXPath);
					buffer.append(strSpecies);
					buffer.append("\n");
				}
			}
			
	
			// write the load command -
			if (strFileType.equalsIgnoreCase("ascii"))
			{
				buffer.append(strExpId);
				buffer.append(" = ");
				buffer.append("load('");
				buffer.append(strFileName);
				buffer.append("');\n");
			}
			
			// Put a new line and go around again -
			buffer.append("\n");
		}
		
		// Add a function pointer array -
		buffer.append("% Setup the array of error function pointers -- \n");
		for (int exp_index=0;exp_index<NUMBER_OF_EXPERIMENTS;exp_index++)
		{
			// Get the id from the list -
			String strExpId = expIDList.get(exp_index);
			
			// Build the pointer reference -
			buffer.append("\n");
			buffer.append("% ");
			buffer.append(strExpId);
			buffer.append(" -- %\n");
			buffer.append("% Setup the function pointer - \n");
			buffer.append("ERROR_STRUCT.FUNCTION(");
			buffer.append(exp_index+1);
			buffer.append(").POINTER = ");
			buffer.append("@ERR_");
			buffer.append(strExpId);
			buffer.append(";\n");
			buffer.append("\n");
			
			// Build the time-scale reference  -
			buffer.append("% Setup the simulation time-scale for ");
			buffer.append(strExpId);
			buffer.append(" - \n");
			
			// Ok, so we need to get the time_start, time_stop and time_step -
			String strTStartXPath = "//experiment[@id='"+strExpId+"']/@simulation_time_start";
			String strTStopXPath = "//experiment[@id='"+strExpId+"']/@simulation_time_stop";
			String strTSXPath = "//experiment[@id='"+strExpId+"']/@simulation_time_step";
			
			String strTimeStart = queryBCXTree(bcxTree,strTStartXPath);
			String strTimeStop = queryBCXTree(bcxTree,strTStopXPath);
			String strTimeStep = queryBCXTree(bcxTree,strTSXPath);
			
			// Ok, here we go bitches ...
			buffer.append("ERROR_STRUCT.FUNCTION(");
			buffer.append(exp_index+1);
			buffer.append(").TIME_START = ");
			buffer.append(strTimeStart);
			buffer.append(";\n");
			
			buffer.append("ERROR_STRUCT.FUNCTION(");
			buffer.append(exp_index+1);
			buffer.append(").TIME_STOP = ");
			buffer.append(strTimeStop);
			buffer.append(";\n");
			
			buffer.append("ERROR_STRUCT.FUNCTION(");
			buffer.append(exp_index+1);
			buffer.append(").TIME_STEP = ");
			buffer.append(strTimeStep);
			buffer.append(";\n");
			
		}
		
		buffer.append("\n");
		buffer.append("% DO NOT EDIT BELOW THIS LINE.\n");
		
		for (int exp_index = 0;exp_index<NUMBER_OF_EXPERIMENTS;exp_index++)
		{
			// Get the id from the list -
			String strExpId = expIDList.get(exp_index);
			
			buffer.append("EDF.DATA_ARRAY_");
			buffer.append(strExpId);
			buffer.append(" = ");
			buffer.append(strExpId);
			buffer.append(";\n");
		}
		
		buffer.append("EDF.ERROR_FUNCTION_ARRAY = ERROR_STRUCT;");
		buffer.append("EDF.NUMBER_OF_OBJECTIVES = ");
		buffer.append(NUMBER_OF_EXPERIMENTS);
		buffer.append(";\n");
		buffer.append("\n");
		buffer.append("return");
	}
	
	
	private String queryBCXTree(Document bcxTree,String strXPath)
	{
		// Method attributes -
		String strProp = "";
		
		try {
			Node propNode = (Node) _xpath.evaluate(strXPath, bcxTree, XPathConstants.NODE);
			strProp = propNode.getNodeValue();
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR: Property lookup failed on BCXTree. The following XPath "+strXPath+" resuled in an error - "+error.toString());
		}
		
		return(strProp);
	}
	
	
}
