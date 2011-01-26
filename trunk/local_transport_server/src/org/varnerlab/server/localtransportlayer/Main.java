package org.varnerlab.server.localtransportlayer;

import java.io.File;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Main implements IConfigurable {
	// Class attributes -
	private static String _strLoggerName = Main.class.getName();
	private static Logger _logger=Logger.getLogger(_strLoggerName);
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private Hashtable<String,String> _propTable = new Hashtable<String,String>();
	
	// Set a property -
	public void setProperty(String key,String value)
	{
		// put the key and value in the table -
		_propTable.put(key, value);
	}
	
	
	// Get a property -
	public String getProperty(String key)
	{
		return (_propTable.get(key));
	}
	
	public void doInitializeServer(String strPath, IConfigurable session) throws Exception
	{
		// Load the XML prop file -
    	File configFile = new File(strPath);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  	  	Document doc = dBuilder.parse(configFile);
  	  	doc.getDocumentElement().normalize();
  	  	
  	  	// Load the plugin directory -
  	  	XPathExpression expr = _xpath.compile("//path[@symbol='UNIVERSAL_PLUGIN_JAR_DIRECTORY']/@path_location");
	  	NodeList pluginNode = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
	  	String pluginFilePath = pluginNode.item(0).getNodeValue();
	  	
	  	// Store some properties -
	  	session.setProperty("PLUGIN_DIR",pluginFilePath);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Method attributes --
		XMLPropTree xmlPropTree = new XMLPropTree();			// Object wrapper for the properties tree -
		Translator translationObject = new Translator();		// Translator object -
		Main server = new Main();						// Instance of the server -
		
		// Check to see if the user passed in the correct file -
		if (args[0]!=null)
		{	
			// If I get here, then I have a non-null control file path -
			try 
			{
				// Grab the path --
				String strConfigFilePath = args[0];
				
				// Configure the server -
				server.doInitializeServer(strConfigFilePath,(IConfigurable)server);
				
				// Make sure the plugin dir is on the classpath - load the jars 
	            String strPluginDir = (String)server.getProperty("PLUGIN_DIR");
	            
	            System.out.println("What is this PLUGIN_DIR - "+strPluginDir);
	            
	            File filePlugin = new File(strPluginDir);
	            File[] jarFileArray = filePlugin.listFiles();
	            
	            
	            int NUMBER_OF_FILES = jarFileArray.length;
	            for (int index=0;index<NUMBER_OF_FILES;index++)
	            {
	            	File tmpFile = jarFileArray[index];
	            	LoadPluginJarFiles.addFile(tmpFile);
	            }
				
				// Load the control file -
				xmlPropTree.loadResource(strConfigFilePath);
				
				// Execute the translation job -
				translationObject.doExecute(xmlPropTree);
				System.out.println("Completed the code transformation ...");
			}
			catch (Exception error)
			{
				// Print out error 
				System.out.println("MAJOR MALFUNCTION: Local transport layer encountered an error on launch. ERROR: "+error.getMessage());
				
				// Send out stack trace 
				System.out.println("---------------------------------- STARTING STACKTRACE ------------------------------- \n");
				error.printStackTrace();
			}
		}
		else
		{
			// If I get here, then I have a missing or null control file ...
			System.out.println("ERROR: Missing or null control file path. Please pass in a valid path for the control file.");
		}
	}

}
