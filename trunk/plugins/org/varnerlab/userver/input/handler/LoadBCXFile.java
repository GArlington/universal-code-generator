package org.varnerlab.userver.input.handler;

import org.varnerlab.server.transport.IBCXInputDriver;
import org.varnerlab.server.transport.IInputHandler;
import org.sbml.libsbml.*;
import org.varnerlab.server.transport.*;
import org.varnerlab.userver.language.handler.*;
import org.varnerlab.userver.language.handler.bcx.*;

import java.io.File;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.util.Hashtable;
import java.util.Vector;

public class LoadBCXFile implements IInputHandler {

	// ths is imported from the transport package -
	private LoadXMLPropFile _xmlPropTree = null;
	private Model _model = null;
	private Exp[] expObjList = new Exp[0];
	private Document doc = null;
	private Vector<Exp> _vecExperiments = new Vector<Exp>();

	public LoadBCXFile() {

	}

	public void setModel(Model _model){
		this._model = _model;
	}

	public void setProperties(LoadXMLPropFile prop) {
		this._xmlPropTree = prop;
	}

	public void setProperties(String filePath) {
		try{
			// build the new XML document
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			this.doc = docBuilder.parse (new File(filePath));
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setProperties(Document doc){
		this.doc = doc;
	}

	public void setProperties(Hashtable prop) {
	}

	public Object getResource(Object object) throws Exception
	{
		if (_vecExperiments!=null)
		{
			return(_vecExperiments);
		}
		else
		{
			throw new Exception("ERROR: experiment list not loaded");
		}
	}

	
	/** Used to create an array lisitng the experiments represented
	 *  in the input file.
	 *  @param filePath         String, path to XML type input file for experiments
	 *  @return exp             Exp[], list of experiment described by input.
	 */ 
	private void loadResource(Document bcxDOMTree) throws Exception {
		// Some needed temp variables

		
		// Load the sbml lib -
		System.loadLibrary("sbmlj");

		try {
			// normalize text representation
			
			//System.out.println ("Root element of the doc is " + doc.getDocumentElement().getNodeName());

			// get the list of experiments
			NodeList listOfExp = bcxDOMTree.getElementsByTagName("experiment");
			int totalExps = listOfExp.getLength();
			System.out.println("Total no of experiments : " + totalExps);

			// loop through all of the experiments
			for(int s=0; s<listOfExp.getLength() ; s++){

				//set up the next experiment

				// get the node for the first experiment
				Node expNode = listOfExp.item(s);
				if(expNode.getNodeType() == Node.ELEMENT_NODE){

					Exp expObj = new Exp();

					// throw the node into an element
					Element expElement = (Element)expNode;

					// get the element ID
					String idName = expElement.getAttribute("id").trim();

					// get the value for this id
					if(idName!=null){
						System.out.println("Exp id : " +idName);
						expObj.setExpId(idName);
					}

					// get the ss element
					String ssCondition = expElement.getAttribute("steady_state").trim().toLowerCase();

					// get the value for this ss
					if(ssCondition!=null){
						System.out.println("Exp ss : " + ssCondition);
						if(ssCondition.equals("true")){
							expObj.setSs(true);
						}
					}

					// get the time element
					String time = expElement.getAttribute("time").trim();

					// get the value for this time
					if(time!=null){
						System.out.println("Exp time : " + time);
						expObj.setTime(Double.parseDouble("0.0"));
					}


					// get the citation element
					String cite = expElement.getAttribute("cite").trim().toLowerCase();

					// get the value for this ss
					if(cite!=null){
						System.out.println("Exp cite : " + cite);
						expObj.setCite(cite);
					}

					// get the list of data
					NodeList dataList = expElement.getElementsByTagName("data_point");
					/*******************************
					 **loop through the data list***
					 *******************************/
					for(int i=0;i<dataList.getLength(); i++){
						// setup a new data object
						Data dataObj = new Data();

						// get the ith data element
						Element dataElement = (Element)dataList.item(i);

						// get the element id for this data element
						String dataID = dataElement.getAttribute("id").trim();
						// get the id value for this data element
						if(dataID!=null){
							dataObj.setDataId(dataID);
						}
						// get the species element for this data element
						String dataSpecies = dataElement.getAttribute("species").trim();
						if(dataSpecies!=null){
							dataObj.setSpeciesId(dataSpecies);
						}

						// get whether the data element is exact
						String dataExact = dataElement.getAttribute("exact_name").trim();
						if(dataExact!=null){
							if(dataExact.equalsIgnoreCase("true")){
								dataObj.setFree(true);
							}
						}

						// Set all of the species indexes:
						int[] speciesIndexes = getSpeciesIndexes(dataSpecies,dataExact);
						for(int z = 0; z<speciesIndexes.length; z++){
							dataObj.addSpeciesIndex(speciesIndexes[z]);
						}

						// get the cite element for this data element
						String dataCite = dataElement.getAttribute("cite").trim();

						// get the value for this cite element
						if(dataCite!=null){
							System.out.println("Data cite : " + dataCite);
							dataObj.setCite(dataCite);
						}

						// get the group element for this data element
						String dataGroup = dataElement.getAttribute("scaling_group_name").trim();

						// get the value for this group element
						if(dataGroup!=null){
							System.out.println("Data group : " + dataGroup);
							dataObj.setGroupId(dataGroup);
						}

						String strValue = dataElement.getAttribute("value").trim();
						String strTime = dataElement.getAttribute("time").trim();
						String strError = dataElement.getAttribute("error").trim();

						if(strTime!=null && strValue!=null){
							System.out.println("Value time : " + strTime);
							System.out.println("Value concentration : " + strValue);
							System.out.println("Value Error : " + strError);

							if (strError.equals("") || strError==null){
								dataObj.addValue(Double.parseDouble(strTime),Double.parseDouble(strValue));
							}
							else {
								dataObj.addValue(Double.parseDouble(strTime),Double.parseDouble(strValue), Double.parseDouble(strError));
							}
						}



						// append this data to the experiment
						expObj.addData(dataObj);
					} // done with data

					// get the list of stimulus
					NodeList stimulusList = expElement.getElementsByTagName("stimulus");
					/************************************
					 ** loop through the stimulus list **
					 ************************************/
					for(int i=0;i<stimulusList.getLength(); i++){
						// setup a new stimulus object
						Stimulus stimulusObj = new Stimulus();

						// get the ith stimulus element
						Element stimulusElement = (Element)stimulusList.item(i);

						// get the element id for this stimulus element
						String stimulusIdList = stimulusElement.getAttribute("id").trim();
						// get the id value for this stimulus element
						if(stimulusIdList!=null){
							System.out.println("Stimulus id : " +stimulusIdList);
							stimulusObj.setStimId(stimulusIdList);
						}

						// get the element param for this stimulus element
						String stimSpecies = stimulusElement.getAttribute("species").trim();

						// get the param value for this stimulus element
						if(stimSpecies!=null){
							// get the species index
							int[] stimSpeciesIndex = getSpeciesIndexes(stimSpecies,"true");
							System.out.println("Stimulus param : " +stimSpecies);
							stimulusObj.setParamIndex(stimSpeciesIndex[0]);
						}

						// get the element basis for this stimulus element
						String basisList = stimulusElement.getAttribute("basis").trim();
						// get the basis value for this stimulus element
						if(basisList!=null){
							System.out.println("Stimulus basis : " +basisList);
							stimulusObj.setBasis(basisList);
						}

						// get the element time for this stimulus element
						String timeList = stimulusElement.getAttribute("time").trim();
						// get the time value for this stimulus element
						if(timeList!=null){
							System.out.println("Stimulus time : " + timeList);
							stimulusObj.setTime(Double.parseDouble(timeList));
						}

						// get the element value for this stimulus element
						String sValueList = stimulusElement.getAttribute("value").trim();
						// get the time value for this stimulus element
						if(sValueList!=null){
							System.out.println("Stimulus value : " + sValueList);
							stimulusObj.setValue(Double.parseDouble(sValueList));
						}

						// append this stimulus object to the experiment
						expObj.addStimulus(stimulusObj);
					} // done with stimulus

					// now that we have everyting for an experiment lets add it to the list!!!
					_vecExperiments.addElement(expObj);
					//expObjList = CUtil.addElement(expObjList,expObj);
				} //end of if clause
			}//done with exp
		}

		catch (Throwable t) {
			t.printStackTrace();
			throw (new Exception("Error in loading input",t));
		}
	}//end

	private int[] getSpeciesIndexes(String dataSpecies,String dataExact){
		int[] speciesIndexes = new int[0];
		int numSpecies = (int) _model.getNumSpecies();
		ListOfSpecies listSpecies = _model.getListOfSpecies();
		for(int j=0;j<numSpecies;j++)
		{
			Species species = listSpecies.get(j);
			
			if (species!=null)
			{
			
				String id = species.getId();
				if(dataExact.equalsIgnoreCase("true"))
				{
					if(id.equals(dataSpecies))
					{
						speciesIndexes = CUtil.addElement(speciesIndexes, j+1);
					}
				}
				if(dataExact.equalsIgnoreCase("false"))
				{
					if(id.contains(dataSpecies))
					{
						speciesIndexes = CUtil.addElement(speciesIndexes, j+1);
					}
				}
			}
		}
		return(speciesIndexes);
	}

	public void loadResource(Object object) throws Exception {
		
		// Load the bcx file -
		String strPath = "";
		
		// Ok, so we need load the sbml for this experiment list -
		LoadSBMLFile sbmlReader = new LoadSBMLFile();
		sbmlReader.setProperties(_xmlPropTree);
		sbmlReader.loadResource(null);
		_model = (Model)sbmlReader.getResource(null);
		
		// Get the resource string -
        String strFileName = _xmlPropTree.getProperty("//Model/bcx_datafilename/text()");
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
 
        // Formulate the path -
        strPath = strWorkingDir+"/"+strFileName;
        
        System.out.println("Going to load - "+strPath);
       
		// Ok, bitches, let's load the bcx file and then hand the DOM tree to Ryan's loadResources method -
		File configFile = new File(strPath);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	Document _bcxPropTree = dBuilder.parse(configFile);
    	_bcxPropTree.getDocumentElement().normalize();
    	
    	// go ...
    	loadResource(_bcxPropTree);
	}
}
