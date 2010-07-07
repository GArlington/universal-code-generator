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
		
		// How many total cols?
		return_index[1] = NUMBER_OF_DATA_COLS;
		
		// return -
		return(return_index);
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
	
	// Build the error buffer -
	public void buildErrorBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		// Populate the buffer -
		buffer.append("function [ERR]=ERR_");
		buffer.append(strExpID);
		buffer.append("(TSTART,TSTOP,Ts,DF,EDF)\n");
		buffer.append("\n");
		
		// Run the simulation -
		buffer.append("% Run the simulation -- \n");
		buffer.append("[TSIM,XSIM]=SIM_");
		buffer.append(strExpID);
		buffer.append("(TSTART,TSTOP,Ts,DF);\n");
		buffer.append("\n");
		
		// Load the experimental data -
		buffer.append("% Get the experimental data from the EDF -- \n");
		buffer.append("DATA = EDF.DATA_ARRAY_");
		buffer.append(strExpID);
		buffer.append(";\n");
		buffer.append("\n");
		
		// Setup the error -
		buffer.append("% Scale the experimental data -- \n");
		buffer.append("[NROW,NCOL]=size(DATA);\n");
		buffer.append("SCALED_DATA = [];\n");
		
		// We need to figure out what index time is (if any) -
		int[] index_time = findDataColIDIndex("Time",strExpID,bcxTree);
		int TMP_LEN = index_time[1];
		if (index_time[0]!=-1)
		{
			buffer.append("IDX_VEC = [");
			for (int tmp_index=0;tmp_index<TMP_LEN;tmp_index++)
			{
				if (tmp_index!=index_time[0])
				{
					buffer.append(tmp_index+1);
					buffer.append(" ");
				}
			}
			buffer.append("];\n");
		}
		else
		{
			buffer.append("IDX_VEC = 1:");
			buffer.append(TMP_LEN+1);
			buffer.append(";\n");
		}
		
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
		buffer.append("\t % Calculate the scaled data - \n");
		buffer.append("\t TMP = (TMP_COL - MIN)./(MAX - MIN);\n");
		buffer.append("\t SCALED_DATA = [SCALED_DATA TMP];\n");
		buffer.append("end;\n");
		buffer.append("\n");
		
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
				
				// Ok -
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
		}
		
		// Add the groups to the simulation array -
		buffer.append("% Add the groups to the simulation array -- \n");
		buffer.append("SIMULATION = GROUPS;\n");
		
		
		buffer.append("\n");
		buffer.append("% Calculate the VARIANCE array -- \n");
		for (int grp_index = 0;grp_index<NUMBER_OF_DATA_GROUPS;grp_index++)
		{
			// Get the name of the grp -
			String strGrpName = groupList.get(grp_index);
			
			buffer.append("VARIANCE(:,");
			buffer.append(grp_index+1);
			buffer.append(") = ");
			
			String strDataColXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+strGrpName+"']/@species";
			ArrayList<String> coeffVList = getList(strDataColXPath,bcxTree);
			int NUMBER_OF_CVS = coeffVList.size();
			for (int variance_index=0;variance_index<NUMBER_OF_CVS;variance_index++)
			{
				
				// Get the index of this species -
				String strSpeciesSymbol = coeffVList.get(variance_index);
				
				if (!strSpeciesSymbol.isEmpty())
				{
					int index_species = findSpeciesIndex(strSpeciesSymbol);
				
					// Query the tree to get the CVs -
					String strGetCVXPath = "//experiment[@id='"+strExpID+"']/measurement_file/data_column[@data_group='"+strGrpName+"']/@coefficient_of_variation |" +
						"//experiment[@id='"+strExpID+"']/measurement_file/data_column[@species='"+strSpeciesSymbol+"']/@coefficient_of_variation";
					String strCV = queryBCXTree(bcxTree,strGetCVXPath);
				
					buffer.append("(");
					buffer.append(strCV);
					buffer.append("*");
					buffer.append("XSIM(:,");
					buffer.append(index_species);
					buffer.append(")");
				
					if (variance_index==(NUMBER_OF_CVS-1))
					{
						buffer.append(").^2;\n");
					}
					else
					{
						buffer.append(").^2 + ");
					}	
				}
			}
		}
		
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
		buffer.append("\t % Calculate the scaled data - \n");
		buffer.append("\t TMP = (TMP_COL - MIN)./(MAX - MIN);\n");
		buffer.append("\t SCALED_SIMULATION_DATA = [SCALED_SIMULATION_DATA TMP];\n");
		buffer.append("end;\n");
		buffer.append("\n");
		
		buffer.append("% Calculate the error in the scale -- \n");
		for (int grp_index = 0;grp_index<NUMBER_OF_DATA_GROUPS;grp_index++)
		{
			// Get the name of the grp -
			String strGrpName = groupList.get(grp_index);
			
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
		
		
		buffer.append("% Interpolate the simulation to the experimental time scale -- \n");
		buffer.append("ISCALED_SIMULATION_DATA = interp(TSIM,SCALED_SIMULATION_DATA,TEXP);\n");
		buffer.append("\n");
		buffer.append("% Calculate the error - \n");
		buffer.append("for col_index=1:NCOL\n");
		buffer.append("\t SF = (1./VARIANCE(:,col_index));\n");
		buffer.append("\t RELATIVE_ERR = sum(SF.*((ISCALED_SIMULATION_DATA(:,col_index) - SCALED_DATA(:,col_index)).^2));\n");
		buffer.append("\t SCALE_ERR_GRP = SCALE_ERR(col_index);\n");
		buffer.append("\t ERR_ARR(col_index,1) = RELATIVE_ERR + SCALE_ERR_GRP;\n");
		buffer.append("end;\n");
		buffer.append("\n");
		buffer.append("% Compute ERR -- \n");
		buffer.append("ERR = sum(ERR_ARR);\n");
		buffer.append("\n");
		buffer.append("return;\n");
		
	}
	
	
	// Build the experimental simulation buffer -
	public void buildSimFileBuffer(StringBuffer buffer,Document bcxTree,LoadXMLPropFile _xmlPropTree,String strExpID) throws Exception
	{
		// Method attributes -
		
		// Populate the buffer -
		buffer.append("function [TSIM,XSIM]=SIM_");
		buffer.append(strExpID);
		buffer.append("(TSTART,TSTOP,Ts,DF)\n");
		buffer.append("\n");
		
		buffer.append("% Run the model to steady-state -- \n");
		buffer.append("[TSS,XSS] = FindSteadyState(DF);\n");
		buffer.append("IC = XSS(:,end);\n");
		buffer.append("DF.INITIAL_CONDITIONS = IC;\n");
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
			
			// Check to see of stimulus time equals 0 - 
			if (strStimulusTime.equalsIgnoreCase("0.0") || strStimulusTime.equalsIgnoreCase("0"))
			{
			
				// Find the index of this species -
				buffer.append("IC(");
				buffer.append(tmp_index);
				buffer.append(",1) = ");
				buffer.append(strStimulusValue);
				buffer.append("\n");
				buffer.append("\n");
			}
			else
			{
				// if I get here, then I'm adding stimulus after some period of time -
				buffer.append("% Run simulation from ");
				
			}
		}
				
			// Run the simulation -
			buffer.append("% Solve the mass-balance equations.\n");
			buffer.append("DF.INITIAL_CONDITIONS = IC;\n");
			buffer.append("[TSIM,XSIM]=LSODECallWrapper(TSTART,TSTOP,Ts,DF,'");
			buffer.append("SIM_");
			buffer.append(strExpID);
			buffer.append(".dat');\n");
			buffer.append("\n");
			buffer.append("return;\n");
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
			
			buffer.append("ERROR_STRUCT.FUNCTION(");
			buffer.append(exp_index+1);
			buffer.append(").POINTER = ");
			buffer.append("@ERR_");
			buffer.append(strExpId);
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
