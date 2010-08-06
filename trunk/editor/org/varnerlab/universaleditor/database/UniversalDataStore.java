package org.varnerlab.universaleditor.database;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.service.PublishService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UniversalDataStore {
	// Class/instance attributes -
	private static UniversalDataStore _this;
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private UEditorSession _session = null;
	private DatabaseAPI dbAPI = null;
	private XMLDataStoreAPI xmlAPI = null;
	
	// Access method -
	public static UniversalDataStore getDataStore()
	{
		if (_this==null)
		{
			return (new UniversalDataStore());
		}
		else
		{
			return(_this);
		}
	}
	
	// Private constructor -
	private UniversalDataStore()
	{
		// Ok, we need to initialize the DataStore -
		init();
	}

	
	// initialize method -
	private void init()
	{
		// Method attributes -
			
		try {
		
			// Ok, we need to determine which data source I'm going to use -
			
			// Get the session object -
			_session = (Launcher.getInstance()).getSession();
			
			// What type of data store are we dealing with?
			String strDataStoreType = (String)_session.getProperty("DATASTORE_TYPE");
			if (strDataStoreType.equalsIgnoreCase("XML"))
			{
				// Ok, If I get here then I'm storing user project information in a local XML file --
				loadXMLDataStore();
			}
			else if (strDataStoreType.equalsIgnoreCase("DATABASE"))
			{
				// Ok, If I get here then I'm using a remote or local mysql database -
				connectToDatabase();
			}
			else
			{
				throw new Exception("Hey Assmunch - that is not a supported DataStoreType! Either XML -or- DATABASE");
			}
		}
		catch (Exception error)
		{
			System.out.println("ERROR: We have a problem initializing the DataStore. "+error.toString());
		}
	}
	
	
	private void loadXMLDataStore() throws Exception
	{
		// Method attributes -
		
		// Get the xml file and load the file -
		String strXMLFileName = (String)_session.getProperty("XML_DATASTORE_PATH");
		
		// Load the file -
		File mainPropFile = new File(strXMLFileName);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  	  	Document doc = dBuilder.parse(mainPropFile);
  	  	doc.getDocumentElement().normalize();
  	  	
  	  	// Store the DOM tree in the session object -
  	  	_session.setProperty("XMLDATASTORE_DOM_TREE",doc);
  	  	
  	  	// Load the XML data store API -
  	  	xmlAPI = new XMLDataStoreAPI();
	}
		
	public DatabaseAPI getDatabaseAPIInstance()
	{
		if (dbAPI!=null)
		{
			return(dbAPI);
		}
		else
		{
			connectToDatabase();
			return(dbAPI);
		}
	}
	
	public XMLDataStoreAPI getXMLDataStoreAPIInstance()
	{
		if (xmlAPI!=null)
		{
			return(xmlAPI);
		}
		else
		{
			try {
				loadXMLDataStore();
				return(xmlAPI);
			}
			catch (Exception error)
			{
				// If we have a malfunction then return null -
				error.printStackTrace();
				return(null);
			}
		}
	}
	
	private void connectToDatabase() {
        try
        {
            // Ok, we need to check to see if the database connection is in Session -
            dbAPI = (DatabaseAPI)_session.getProperty("DATABASE_CONNECTION");

            // Check if connection -
            if (dbAPI == null)
            {

                PublishService.submitData("No current db connection. Let's try to create a new database connection -");

                // Ok, create a connection factory object -
                IGFactory tmpFactory = new GConnectionFactory();
                
                // Configure the connection factory -
                
                // Get the prop_tree out of memory -
                Document doc = (Document)_session.getProperty("UNIVERSAL_DOM_TREE");
                
                // Database information -
            	String strXPDatabase = "//database_information/property";
            	processPropertyAttributes(strXPDatabase,doc,tmpFactory);
            	
            	 // If I get here, then I don't have a db component. Load one -
                dbAPI = new DatabaseAPI();
                dbAPI.setProperty("CONNECTION_FACTORY", tmpFactory);

                // Load the config and establish the connection -
                dbAPI.loadConnection();

                // Put dbAPI into session -
                _session.setProperty("DATABASE_CONNECTION", dbAPI);

                PublishService.submitData("New db connection established and cached in session");
            }
        }
        catch (Exception error)
        {
        	System.out.println("Problem loading the Datastore connection - "+error.toString());
        }
    }
	
	private void processPropertyAttributes(String strXPath,Document doc,IGFactory session) throws Exception
    {
    	NodeList propNodeList = (NodeList)_xpath.evaluate(strXPath, doc, XPathConstants.NODESET);
    	int NUMBER_PATH_NODES = propNodeList.getLength();
    	for (int index=0;index<NUMBER_PATH_NODES;index++)
    	{
    		// Get the current node -
    		Node tmpNode = propNodeList.item(index);
    		
    		// Process the attributes of this node ..
    		NamedNodeMap map = tmpNode.getAttributes();
    		int NUMBER_OF_ATTRIBUTES = map.getLength();
    		for (int att_index=0;att_index<NUMBER_OF_ATTRIBUTES;att_index++)
    		{
    			// Ok bitches, so I should get the attribute name (capitalize it) and key the value 
    			Node attNode = map.item(att_index);
    			String keyName = ((String)attNode.getNodeName()).toUpperCase();
    			String strValue = attNode.getNodeValue();
    			
    			// store the key,value pair in the session object -	
    			session.setProperty(keyName, strValue);
    		}
    	}
    }
	
}
