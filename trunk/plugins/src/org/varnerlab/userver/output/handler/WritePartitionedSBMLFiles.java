package org.varnerlab.userver.output.handler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.varnerlab.server.localtransportlayer.IOutputHandler;
import org.varnerlab.server.localtransportlayer.XMLPropTree;
import org.varnerlab.userver.language.handler.GIOL;

public class WritePartitionedSBMLFiles implements IOutputHandler {
	// Class/instance attributes -
	private Hashtable _propTable = null;
    private XMLPropTree _xmlPropTree = null;
    private Logger _logger = null;
	
    
    public void setHashtable(Hashtable prop) {
        _propTable = prop;
    }

    public void setProperties(Hashtable prop) {
        this._propTable = prop;
    }

    // reads a file of doubles (one on each line) and returns them in an array
	private int[] readPartitionFile(String path) throws Exception{
		// Method attributes -
		FileReader FR = new FileReader(path);
		BufferedReader r = new BufferedReader(FR);
		int[] data = new int[0];
		
		String s;
		while ((s=r.readLine())!=null){
			
			int[] temp = new int[data.length + 1];
			if(data.length>0){
				System.arraycopy (data,0,temp,0,data.length);
			}
			
			data = temp;
			data[data.length-1] = Integer.parseInt(s);
		}
		
		// Close the file -
		r.close();
		
		// Reutrn the data -
		return(data);     
	}

    
	public void setProperties(XMLPropTree propTree) {
		
		// Grab some stuff -
		_propTable = new Properties();
		this._xmlPropTree = propTree;
	
		try {
			
			// Extract a few items from the prop tree -
			String strNumberOfPartitions = propTree.getProperty(".//NumberOfPartitions/@symbol");
			String strInputPath = propTree.getProperty(".//path[@symbol=\"UNIVERSAL_INPUT_PATH\"]/@path_location");
			String strOutputPath = propTree.getProperty(".//path[@symbol=\"UNIVERSAL_SOURCE_OUTPUT_PATH\"]/@path_location");
		
			Hashtable<String,String> partitionDictionary = propTree.buildFilenameBlockDictionary("PartitionFile");
			Hashtable<String,String> outputSBMLDictionary = propTree.buildFilenameBlockDictionary("PartitionFile");
			
			String strPartitionFile = partitionDictionary.get("FULLY_QUALIFIED_PATH");
			String strSBMLFileName = outputSBMLDictionary.get("FUNCTION_NAME");
			
			// Put properties in the _propTable -
			_propTable.put("NUMBER_OF_PARTITIONS",strNumberOfPartitions);
			_propTable.put("INPUT_PATH", strInputPath);
			_propTable.put("OUTPUT_PATH",strOutputPath);
			_propTable.put("PARTITION_FILE_PATH",strPartitionFile);
			_propTable.put("SBML_FILENAME",strSBMLFileName);
		}
		catch (Exception error)
		{
			System.out.println("ERROR: We have a malfunction setting properties in WritePartitionedSBMLFile");
		}
		
		
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
    
	@Override
	public void writeResource(Object object) throws Exception {
		// Get the resource type -
        Model master_model_wrapper = (Model)object;
        
        // Load the lib -
        System.loadLibrary("sbmlj");
        
       // Load the partition file -
       String strPartitionFileName = (String)_propTable.get("PARTITION_FILE_PATH");
       int[] partitionArray = readPartitionFile(strPartitionFileName);
       
       System.out.println("How many reactions are we partitioning? "+partitionArray.length);
       
        // Get info on the SBML file name -
        String strSBMLFileMaster = (String)_propTable.get("SBML_FILENAME"); 
        String strOutputPath = (String)_propTable.get("OUTPUT_PATH");
        
        // Ok, so we need to get the number of partitions - and process each one ...
        String strNumberOfPartitons = (String)_propTable.get("NUMBER_OF_PARTITIONS");
        int NUMBER_OF_PARTITIONS = Integer.parseInt(strNumberOfPartitons);
       
        
        for (int partition_index = 0;partition_index<NUMBER_OF_PARTITIONS;partition_index++)
        {
        	// Process partition j -
        	  
        	// Create reaction list for the current partition -
        	Vector<Reaction> vecReactions = new Vector<Reaction>();
        	Vector<Parameter> vecParameters = new Vector<Parameter>();
        	
        	System.out.println("Processing partition number "+partition_index);
        	
        	// Get the reactions and parameters for this parition -
        	partitionReactionList(master_model_wrapper,vecReactions,vecParameters,partitionArray,partition_index);
        	        	
        	// Create new string buffer -
        	StringBuffer tmpBuffer = new StringBuffer();
        	
        	// Add compartment to model -
    		addCompartmentsToModel(tmpBuffer,master_model_wrapper);
    		
    		System.out.println("Added compartments to partition number "+partition_index);
        	
        	// Add the species to the model -
    		addSpeciesToModel(tmpBuffer,master_model_wrapper);
    		
    		System.out.println("Added species to partition number "+partition_index);
    	
    		// Add the parameters to the model -
    		addParametersToModel(tmpBuffer,vecParameters);
    		
    		System.out.println("Added parameters to partition number "+partition_index);
    		
    		// Add the reactions to the model -
    		addReactionsToModel(tmpBuffer,vecReactions);
    		        	
    		System.out.println("Added reactions to partition number "+partition_index);
    		
    		// Create the filename -
    		String strFullSBMLFileName = strSBMLFileMaster+"_PARTITION.xml."+partition_index;
    		String strFinalPath = strOutputPath+"/"+strFullSBMLFileName;
    		
    		StringBuffer buffer = new StringBuffer();
            buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            buffer.append("<sbml xmlns=\"http://www.sbml.org/sbml/level3/version1/core\" level=\"3\" version=\"1\">\n");
            buffer.append("<model name=\"PARTITION_"+partition_index+"\">\n");
            buffer.append(tmpBuffer.toString());
            buffer.append("\n");
            buffer.append("</model>\n");
            buffer.append("</sbml>");
            
        
    		// Dump to disk -
    		GIOL.write(strFinalPath,buffer.toString());
        }
	}
	
	private void addCompartmentsToModel(StringBuffer buffer,Model master_model_wrapper) throws Exception
	{
		ListOfCompartments compartments = master_model_wrapper.getListOfCompartments();
		int NUMBER_OF_COMPARTMENTS = (int)master_model_wrapper.getNumCompartments();
		buffer.append("<listOfCompartments>\n");
		for (int compartment_index = 0;compartment_index<NUMBER_OF_COMPARTMENTS;compartment_index++)
		{
			// Get the compartment -
			Compartment current_compartment = compartments.get(compartment_index);
		
			buffer.append("\t");
			buffer.append(current_compartment.toSBML());
			buffer.append("\n");
		}
		buffer.append("</listOfCompartments>\n");
	}
	
	private void addParametersToModel(StringBuffer buffer,Vector<Parameter> vecParameters) throws Exception
	{
		Iterator iter = vecParameters.iterator();
		
		System.out.println("How many parameters do we have? "+vecParameters.size());
		
		
		buffer.append("<listOfParameters>\n");
		while (iter.hasNext())
		{
			// Get the tmpObject -
			Parameter tmpObject = (Parameter)iter.next();
			
			buffer.append("\t");
			
			if (tmpObject!=null)
			{
				buffer.append(tmpObject.toSBML());
			}
			else
			{
				buffer.append("DANGER - NULL PARAMETER?");
			}
			
			buffer.append("\n");
			
		}
		buffer.append("</listOfParameters>\n");
	}
	
	private void addReactionsToModel(StringBuffer buffer,Vector<Reaction> vecReactions) throws Exception
	{
		Iterator iter = vecReactions.iterator();
		
		buffer.append("<listOfReactions>\n");
		while (iter.hasNext())
		{
			// Get the tmpObject -
			Reaction tmpObject = (Reaction)iter.next();
		
			if (tmpObject!=null)
			{
				buffer.append(tmpObject.toSBML());
			}
			else
			{
				buffer.append("DANGER - NULL REACTION?");
			}
			
			
			buffer.append("\n");
			
		}
		buffer.append("</listOfReactions>\n");
	}
	
	private void addSpeciesToModel(StringBuffer buffer,Model master_model_wrapper) throws Exception
	{
		
		
		buffer.append("<listOfSpecies>\n");
		ListOfSpecies list_species = master_model_wrapper.getListOfSpecies();
		long NUMBER_OF_SPECIES = master_model_wrapper.getNumSpecies();
		for (long species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
		{
			// Get the tmpObject -
			Species tmpObject = list_species.get(species_index);
			
			// Add to partioned wrapper -
			buffer.append("\t");
			buffer.append(tmpObject.toSBML());
			buffer.append("\n");
		}
		
		buffer.append("</listOfSpecies>\n");
	}
	
	// Partition the reactions into lists -
	private void partitionReactionList(Model model_wrapper,Vector<Reaction> vecReactions,Vector<Parameter> vecParameters,int[] arrPartitions,int intPartitionIndex) throws Exception
	{
		// Method attributes -
		ListOfReactions list_reactions = model_wrapper.getListOfReactions();
		ListOfParameters list_parameters = model_wrapper.getListOfParameters();
				
		// Find intPartitionIndex in the vector -
		int NUMBER_OF_RATES = arrPartitions.length;
		for (int rate_index = 0;rate_index<NUMBER_OF_RATES;rate_index++)
		{
			// Get the index -
			int current_partition_index = arrPartitions[rate_index];
			
			if (current_partition_index == intPartitionIndex)
			{
				// So when I get here I have a reaction for this partition index -
				Reaction tmpReaction = list_reactions.get(rate_index);
				Parameter tmpParameter = list_parameters.get(rate_index);
				
				// Add to the vector -
				vecReactions.add(tmpReaction);
				vecParameters.add(tmpParameter);
			}
		}
				   
	}

}
