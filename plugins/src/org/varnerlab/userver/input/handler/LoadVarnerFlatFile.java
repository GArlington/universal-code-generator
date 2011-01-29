/*
 * LoadVarnerFlatFile.java
 *
 * Created on May 9, 2007, 5:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.input.handler;

// import statements -
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import java.io.*;

import org.varnerlab.server.localtransportlayer.*;

/**
 *
 * @author jeffreyvarner
 */
public class LoadVarnerFlatFile implements IInputHandler {
	// Class/instance attributes -
	private Hashtable<String,String> _propTable = null;
	private Vector<Record> _vecRecords = new Vector<Record>();			// Vector of records from the flat-file
	private Vector<Object> _vecSymbolOrder = new Vector<Object>();      // Vector of symbols to order
	private Vector<Object> _rxnVector = new Vector<Object>();           // Reaction vector
	private Vector<Object> _alphabetVector = new Vector<Object>();      // Alphabet vector
	private Model _modelWrapper = null;                 				// Model wrapper -
	//private SBMLDocument  _sbmlDoc = new SBMLDocument();

	// rate constants and initial conditions array -
	private double[] _dblRateConstant = null;         						// Rate constants
	private double[] _dblInitialCondition = null;           				// Initial conditions
	private Logger _logger = null;
	
	// load the lib -


	/** Creates a new instance of LoadVarnerFlatFile */
	public LoadVarnerFlatFile() {
		// Load the prop table -
		_propTable = new Hashtable<String,String>();
	}

	// Ok, so this methods reads and processes our standard flat file structure -
	// Instead of building our model object model, build the sbml model -
	// this will come in handy in a little bit ... trust me and all will
	// become seen..
	public void loadResource(Object object) throws Exception {
		// Load Reaction file reader ...
		ReactionFileReader reader = new ReactionFileReader();

		// Get the name of the input files -
		//String strWorkingDir = (String)_propTable.get("WORKING_DIRECTORY");

		// Need to check to see if order file is there -
		String strOrderFileName = (String)_propTable.get("SYMBOL_FILENAME");
		String strOrderFileNamePath = (String)_propTable.get("SYMBOL_FILENAME_PATH");

		//System.out.println("What is going on ?? "+strOrderFileName+" what is the path "+strOrderFileNamePath);
		
		// Ok, load the order file if we have a pointer
		if (!strOrderFileName.isEmpty())
		{
			String strTmp = "";
			OrderFileReader orderReader = new OrderFileReader();
			if (!strOrderFileNamePath.isEmpty())
			{
				// Create a tmp path string -
				strTmp = strOrderFileNamePath+"/"+strOrderFileName;
			}
			else
			{
				// Create a tmp path string -
				strTmp = strOrderFileName;
			}

			// Log that we are going to load the order file -
			_logger.log(Level.INFO,"Going to load the following order file: "+strTmp);
			
			// read the symbol file name -
			orderReader.readFile(strTmp,this._vecSymbolOrder);
		}

		// Log what we are going -
		//_logger.log(Level.INFO,"Looking in dir in LVF for FF - "+strWorkingDir);

		// location of the rate constant, null if none:
		String strInitialConditionFilename = (String)_propTable.get("INITAL_CONDITION_FILENAME");
		String strInitialConditionFilenamePath = (String)_propTable.get("INITAL_CONDITION_FILENAME_PATH");
		if (!strInitialConditionFilename.isEmpty())
		{
			String strTmp = "";
			if (!strInitialConditionFilenamePath.isEmpty())
			{
				// Create a tmp path string -
				strTmp = strInitialConditionFilenamePath+"/"+strInitialConditionFilename;
			}
			else
			{
				// Create a tmp path string -
				strTmp = strInitialConditionFilename;
			}

			// Log that we are going to load the order file -
			_logger.log(Level.INFO,"Going to load the following initial condition file: "+strTmp);
			
			// Load the initial condition array -
			_dblInitialCondition = readSimpleFile(strTmp);
		}

		// location of the rate constant, null if none:
		String strRateConstantFilename = (String)_propTable.get("KINETIC_CONSTANTS_FILENAME");
		String strRateConstantFilenamePath = (String)_propTable.get("KINETIC_CONSTANTS_FILENAME_PATH");
		if (!strRateConstantFilename.isEmpty())
		{
			String strTmp = "";
			if (!strRateConstantFilenamePath.isEmpty())
			{
				// Create a tmp path string -
				strTmp = strRateConstantFilenamePath+"/"+strRateConstantFilename;
			}
			else
			{
				// Create a tmp path string -
				strTmp = strRateConstantFilename;
			}

			// Log that we are going to load the order file -
			_logger.log(Level.INFO,"Going to load the following rate constant file: "+strTmp);
			
			// Load the initial condition array -
			_dblRateConstant = readSimpleFile(strTmp);
		}

		// Get the Varner flat file -
		String strPathNetworkFile = (String)_propTable.get("NETWORK_FILENAME");
		String strPathNetworkFilePath = (String)_propTable.get("NETWORK_FILENAME_PATH");
		if (!strPathNetworkFile.isEmpty())
		{
			String strTmp = "";
			if (!strPathNetworkFilePath.isEmpty())
			{
				// Create a tmp path string -
				strTmp = strPathNetworkFilePath+"/"+strPathNetworkFile;
			}
			else
			{
				// Create a tmp path string -
				strTmp = strPathNetworkFile;
			}

			// Log that we are going to load the order file -
			_logger.log(Level.INFO,"Going to load the following flat-file: "+strTmp);
			
			// Read the varner flat file -
			reader.readData(strTmp,_vecRecords);
		}

		// Ok, so now I have a list of Record objects - 
		// I need to turn that into a SBML Model -
		Iterator<Record> iterRecords = _vecRecords.iterator();
		while (iterRecords.hasNext())
		{
			// Get the record
			Record record = (Record)iterRecords.next();
			
			// Ok, so here I need to check if I have a forward or reversable reaction
			String strReverseFlag = (String)record.getData(IReactionFile.REVERSE);
			String strRxnNameTest = (String)record.getData(IReactionFile.RXNNAME);

			if (strReverseFlag.equalsIgnoreCase("-inf") && strRxnNameTest!="")
			{
				// Ok if we here, then I have a reversible reaction
				String strReactants = (String)record.getData(IReactionFile.REACTANTS);
				String strProducts = (String)record.getData(IReactionFile.PRODUCTS);
				String strRxnName = (String)record.getData(IReactionFile.RXNNAME);

				// Create a 2 new records, one with the forward reaction and one for the reverse -
				Record recForward = new Record();
				recForward.setData(IReactionFile.REVERSE,"0");
				recForward.setData(IReactionFile.FORWARD,"inf;");
				recForward.setData(IReactionFile.REACTANTS,strReactants);
				recForward.setData(IReactionFile.PRODUCTS,strProducts);
				recForward.setData(IReactionFile.RXNNAME,strRxnName);
				recForward.setData(IReactionFile.TYPE,"ON-RATE");

				Record recReverse = new Record();
				recReverse.setData(IReactionFile.REVERSE,"0");
				recReverse.setData(IReactionFile.FORWARD,"inf;");
				recReverse.setData(IReactionFile.PRODUCTS,strReactants);
				recReverse.setData(IReactionFile.REACTANTS,strProducts);
				recReverse.setData(IReactionFile.RXNNAME,strRxnName+"_REV");
				recReverse.setData(IReactionFile.TYPE,"OFF-RATE");
				FFReactionObject rxnForward = new FFReactionObject();
				rxnForward.doExecute(recForward);

				FFReactionObject rxnReverse = new FFReactionObject();
				rxnReverse.doExecute(recReverse);

				// Store the rxnObject in a tmpVector
				_rxnVector.addElement(rxnForward);
				_rxnVector.addElement(rxnReverse);

			}

			else if (strRxnNameTest!="")
			{
				// Build new reaction object -
				FFReactionObject rxnObject = new FFReactionObject();

				// Before I add the record to the rxnObject - give it a type -
				record.setData(IReactionFile.TYPE,"CAT-RATE");
				rxnObject.doExecute(record);

				// Store the rxnObject in a tmpVector
				_rxnVector.addElement(rxnObject);
			}
		}

		// Ok, so now I have a list of reaction objects I can create a unique symbol alphabet
		createUniqueAlphabet();

		// At this point I could sort the alphabet (I won't do it now)'
		sortAlphabet();
		
		// Log that we are going to load the order file -
		_logger.log(Level.INFO,"Loaded the flat-file. We are now going to move to the output handler.");
	}



	private void sortAlphabet() throws Exception
	{
		// Ok, so now I need to reorder the alphabet
		Iterator<Object> iter = _vecSymbolOrder.iterator();
		while (iter.hasNext())
		{
			// Get the strTestSymbol
			String strTestSymbol = (String)iter.next();

			// Ok, there is an issue...if I have names in the order
			// file that are *not* used in the network, then I get
			// some crazy ass shitnitz...

			// Check to see if test symbol is in the alphabet vector before
			// we do anything else -
			if (this._alphabetVector.contains(strTestSymbol))
			{
				// When I get here, I have a legal symbol...

				// Ok, so lets remove the strTestSymbol and then add it
				// to the end
				this._alphabetVector.remove(strTestSymbol);

				if (!_alphabetVector.contains(strTestSymbol))
				{
					_alphabetVector.addElement(strTestSymbol);
				}
			}
		}
	}

	private void createUniqueAlphabet() throws Exception 
	{

		// Get the iterator of reaction objects
		Iterator<Object> iterReactions = _rxnVector.iterator();
		while (iterReactions.hasNext())
		{
			// Ok, get the reactants and products -
			FFReactionObject rxnObject = (FFReactionObject)iterReactions.next();
			Iterator<Object> iterReactants = rxnObject.getReactants();
			while (iterReactants.hasNext())
			{
				// Get StateSymbol object
				StateSymbol symbolReactants = (StateSymbol)iterReactants.next();

				String testSymbol = symbolReactants.getSymbol();
				if (!_alphabetVector.contains(testSymbol) && !testSymbol.equalsIgnoreCase("[]"))
				{
					_alphabetVector.addElement(testSymbol);
				}
			}

			Iterator iterProducts = rxnObject.getProducts();
			while (iterProducts.hasNext())
			{
				// Get StateSymbol object
				StateSymbol symbolProducts = (StateSymbol)iterProducts.next();

				String testSymbol = symbolProducts.getSymbol();
				if (!_alphabetVector.contains(testSymbol) && !testSymbol.equalsIgnoreCase("[]"))
				{
					_alphabetVector.addElement(testSymbol);
				}
			}
		}
	}



	// Ok, so when I get here I need to translate the FF object model to the SBML obj model -
	public Object getResource(Object object) throws Exception {
		// Class/instance attributs -


		// Load the sbml lib -
		int counter;
		//System.out.println("New - "+System.getProperty("java.library.path"));
		
		System.loadLibrary("sbmlj");

		// Create a new modelWrapper -
		_modelWrapper = new Model(2,4);

		// Set the name of the model -
		String strModelName = _propTable.get("MODEL_NAME");
		if (strModelName.isEmpty())
		{
			_modelWrapper.setName("TEST_MODEL");
		}
		else
		{
			_modelWrapper.setName(strModelName);
		}

		// Ok, we need to set a default compartment -
		Compartment tmpCompartment = _modelWrapper.createCompartment();
		tmpCompartment.setName(strModelName);
		_modelWrapper.addCompartment(tmpCompartment);

		// Ok, get the iterator for species so I can add these to the model -
		Iterator species_iter = this._alphabetVector.iterator();

		counter = 0;
		while (species_iter.hasNext())
		{
			String spName = (String)species_iter.next();
			
			System.out.println("Processing "+spName);

			// We need to check for - 
			//spName = spName.replace('-', '_');

			// Configure the species object -
			Species newSpecies = _modelWrapper.createSpecies();
			newSpecies.setName(spName);
			newSpecies.setId(spName);
			newSpecies.setCompartment("model");

			// if the initial condition is there, use it
			// we are assuming the ic file is in same order as this
			double temp = 0.0;
			if(_dblInitialCondition != null){
				newSpecies.setInitialConcentration(_dblInitialCondition[counter]);
			}
			else
			{
				newSpecies.setInitialAmount(temp);
			}

			// Add the configured species to the model -
			_modelWrapper.addSpecies(newSpecies);
			counter++;
		}
		
		// How many species are in the model?
		//System.out.println("How many species are in the model? "+_modelWrapper.getNumSpecies());

		// Get the iterator of FFReactionObjects -
		Iterator rxn_iter = this._rxnVector.iterator();
		counter = 0; // reinitalize
		String rateLawS; // used to make up the rate law
		Double temp; // used below 
		while (rxn_iter.hasNext())
		{
			// Create a new SBML reaction object -
			Reaction rxnSBML = _modelWrapper.createReaction();

			// flat files always are irreversible -
			rxnSBML.setReversible(false);

			// Get the ffRxnObj -
			FFReactionObject ffRxnObj = (FFReactionObject)rxn_iter.next();

			// Ok, so create a parameter object for the model -
			Parameter parameterObj = _modelWrapper.createParameter();

			//naming the parameter the rxn string means I cannot refer to it later
			//parameterObj.setName(ffRxnObj.getReactionString());
			String strRxnNameTest = (String)(ffRxnObj.getRecord()).getData(IReactionFile.RXNNAME);
			parameterObj.setName("k_"+counter);
			parameterObj.setId(strRxnNameTest);

			// check to see if this is a forward or a reverse -
			// Get the underlying record -
			Record rec = ffRxnObj.getRecord();
			String rType = (String)rec.getData(IReactionFile.TYPE);

			double rValue = Math.random();
			// if the rate constant is known use it
			if (_dblRateConstant != null){
				parameterObj.setValue(_dblRateConstant[counter]);
			}
			else{
				if (rType.equals("ON-RATE"))
				{
					parameterObj.setValue((10*rValue));
				}
				else if (rType.equals("OFF-RATE"))
				{
					parameterObj.setValue(rValue);
				}
				else if (rType.equals("CAT-RATE"))
				{
					parameterObj.setValue(rValue);
				}
			}

			// Add the parameter to the model -
			_modelWrapper.addParameter(parameterObj);

			// Configure the reaction rate - add the reactants -
			rxnSBML.setName((String)rec.getData(IReactionFile.RXNNAME));
			Iterator iter_reactants = ffRxnObj.getReactants();

			// create a kinetic law formula using mass action
			rateLawS = new String(parameterObj.getName()); // recall this rxn is with previous param
			// it is assumed that each new parameter is matched one to one with new rxn
			while (iter_reactants.hasNext())
			{
				// Get the state symbol -
				StateSymbol symbol = (StateSymbol)iter_reactants.next();

				// we need to check this symbol -
				String strSymbol = symbol.getSymbol();
				//strSymbol = strSymbol.replace('-', '_');

				// Create a new SpeciesRef -
				SpeciesReference specRef = new SpeciesReference(2,4);
				specRef.setSpecies(strSymbol);

				if (!strSymbol.equalsIgnoreCase("[]"))
				{	
					// add this to rate law
					rateLawS = new String(rateLawS +"*"+strSymbol); 		// add reactant to rate law
				}
				else
				{
					rateLawS = new String(rateLawS);
				}

				temp = ffRxnObj.getCoefficientValue(strSymbol);

				// We need the st. coeff w/no sign because SBMLUtil generates the stmatix => the signs are determined from membership
				// in the reactant or product lists -
				specRef.setStoichiometry(Math.abs(temp));

				if (temp!=0.0 && temp!=-1.0)
				{
					rateLawS = new String(rateLawS +"^"+String.valueOf(-1*temp)); 	// add exponent to rate law (is neg)
				}


				// Add the species ref -
				rxnSBML.addReactant(specRef);
			}

			// Configure the reaction rate - add the products -
			Iterator iter_products = ffRxnObj.getProducts();
			while (iter_products.hasNext())
			{
				// Get the state symbol -
				StateSymbol symbol = (StateSymbol)iter_products.next();

				// Create a new SpeciesRef -
				SpeciesReference specRef = new SpeciesReference(2,4);
				specRef.setSpecies(symbol.getSymbol());
				specRef.setStoichiometry(ffRxnObj.getCoefficientValue(symbol.getSymbol()));

				// Add the species ref -
				rxnSBML.addProduct(specRef);
			}

			//create the rate law out of the string rate law
			// law not made this way
			//KineticLaw rateLaw = new KineticLaw(rateLawS);
			//KineticLaw rateLaw = _modelWrapper.createKineticLaw();
			//rateLaw.setFormula(rateLawS);
			
			//rate not defined locally
			//rateLaw.addParameter(parameterObj); // recall that we have defined this parameter already

			//rxnSBML.setKineticLaw(rateLaw); // add this rateLaw to the current rxn
			rxnSBML.setName(ffRxnObj.getReactionString());
			rxnSBML.setId("R_"+counter);

			// add the reaction to the model -
			_modelWrapper.addReaction(rxnSBML);

			// update the counter -
			counter++;

		}

		return(_modelWrapper);
	}

	public void setProperties(Hashtable prop) {
		_propTable = prop;
	}

	// reads a file of doubles (one on each line) and returns them in an array
	private double[] readSimpleFile(String path) throws Exception{

		FileReader FR = new FileReader(path);
		BufferedReader r = new BufferedReader(FR);
		double[] data = new double[0];
		String s;
		while ((s=r.readLine())!=null){
			double[] temp = new double[data.length + 1];
			if(data.length>0){
				System.arraycopy (data,0,temp,0,data.length);
			}
			data = temp;
			data[data.length-1] = Double.parseDouble(s);
		}
		r.close();
		return(data);     
	}

	// Ok, so we are going to a hack to make the old code work with the new formulation -
	public void setProperties(XMLPropTree propTree) {

		// put the props from the tree into the hashtable so I don't grab junk from the tree all over the place (hack because we used a hashtable prop system)
		String strFileName = "";
		String strFilePath = "";
		
		try
		{
			// get the working directory -
			//_propTable.put("WORKING_DIRECTORY",propTree.getProperty("//working_directory/text()"));
	
			// initial conditions -
			ArrayList<String> icFileList = propTree.processFilenameBlock("InitialConditionFile");
			strFileName = icFileList.get(0);
			strFilePath = icFileList.get(2);
			
			if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
			{
				_propTable.put("INITAL_CONDITION_FILENAME",strFileName);
				_propTable.put("INITAL_CONDITION_FILENAME_PATH",strFilePath);
			}
			else
			{
				_propTable.put("INITAL_CONDITION_FILENAME","");
				_propTable.put("INITAL_CONDITION_FILENAME_PATH","");
			}
	
			// rate constants -
			ArrayList<String> kVFileList = propTree.processFilenameBlock("KineticParameterFile");
			strFileName = kVFileList.get(0);
			strFilePath = kVFileList.get(2);	
			if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
			{
				_propTable.put("KINETIC_CONSTANTS_FILENAME",strFileName);
				_propTable.put("KINETIC_CONSTANTS_FILENAME_PATH",strFilePath);
			}
			else
			{
				_propTable.put("KINETIC_CONSTANTS_FILENAME","");
				_propTable.put("KINETIC_CONSTANTS_FILENAME_PATH","");
			}
	
			// network filename -
			ArrayList<String> networkFileList = propTree.processFilenameBlock("NetworkFile");
			strFileName = networkFileList.get(0);
			strFilePath = networkFileList.get(2);	
			if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
			{
				_propTable.put("NETWORK_FILENAME",strFileName);
				_propTable.put("NETWORK_FILENAME_PATH",strFilePath);
			}
			else
			{
				throw new Exception("ERROR: Network filename is missing. Please check your network settings.");
			}
			
			// order filename -
			ArrayList<String> orderFileList = propTree.processFilenameBlock("OrderFile");
			strFileName = orderFileList.get(0);
			strFilePath = orderFileList.get(2);	
			if (!strFileName.equalsIgnoreCase("EMPTY") && !strFilePath.equalsIgnoreCase("EMPTY"))
			{
				_propTable.put("SYMBOL_FILENAME",strFileName);
				_propTable.put("SYMBOL_FILENAME_PATH",strFilePath);
			}
			else
			{
				_propTable.put("SYMBOL_FILENAME","");
				_propTable.put("SYMBOL_FILENAME_PATH","");
			}
			
			// Grab the model name -
			String strModelName = propTree.getProperty("//Model/@name");
			if (!strModelName.equalsIgnoreCase("EMPTY"))
			{
				_propTable.put("MODEL_NAME",strModelName);
			}
			else
			{
				// Throw new exception -
				throw new Exception("ERROR: Missing model name. Please set the model name");
			}
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
	}

	public void setLogger(Logger log) {
		_logger = log;
	} 

}
