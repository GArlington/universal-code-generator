package org.varnerlab.server.localtransportlayer;

// import statements -
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.*;

import java.io.File;
import java.io.FileReader;

public class XMLPropTree implements IInputHandler {
	// Class attributes -
	Properties _propTable = new Properties();
	
	// Create a xpFactory/xpath obj (we'll use this a zillion times -)
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private Document _docPropTree = null;
	private Logger _logger = null;
	

	public Object getResource(Object object) throws Exception {
		// TODO Auto-generated method stub
		return (_docPropTree);
	}

	public String getProperty(String strXPath)
	{
		// Method attributes -
		String strProp = "";
		
		try {
			Node propNode = (Node) _xpath.evaluate(strXPath, _docPropTree, XPathConstants.NODE);
			strProp = propNode.getNodeValue();
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR: Property lookup failed. The following XPath "+strXPath+" resuled in an error - "+error.toString());
		}
		
		return(strProp);
	}
	
	public String getPathForKey(String keyName) throws Exception
	{
		// Method attributes --
		String pathString = "";
		
		String strXPath = ".//ListOfPaths/path[@symbol='"+keyName+"']/@path_location";
		Node pathNode = (Node) _xpath.evaluate(strXPath, _docPropTree, XPathConstants.NODE);
		pathString = pathNode.getNodeValue();
		
		// return the path string -
		return pathString;
	}
	
	public String getPackageForKey(String keyName) throws Exception
	{
		// Method attributes --
		String packageString = "";
		
		String strXPath = ".//ListOfPackages/package[@symbol='"+keyName+"']/@package_name";
		Node pathNode = (Node) _xpath.evaluate(strXPath, _docPropTree, XPathConstants.NODE);
		packageString = pathNode.getNodeValue();
		
		// return the path string -
		return packageString;
	}
	
	public ArrayList<String> processFilenameBlock(String blockName) throws Exception
	{
		// Method attributes -
		ArrayList<String> arrList = new ArrayList<String>();
		
		// Get the filename for the data -
		String strXPathFilename = ".//"+blockName+"/@filename";
		Node filenameNode = (Node) _xpath.evaluate(strXPathFilename, _docPropTree, XPathConstants.NODE);
		
		// ok, the first element of ArrayList is the filename -
		String tmpString = filenameNode.getNodeValue();
		if (!tmpString.isEmpty())
		{
			// Get the filename -
			arrList.add(tmpString);
			
			// Get the "function name"
			int INDEX_OF_DOT = tmpString.indexOf(".");
			arrList.add(tmpString.substring(0, INDEX_OF_DOT));
		}
		else
		{
			// If we have a null filename - then we put in empty strings ...
			arrList.add("EMPTY");
			arrList.add("EMPTY");
		}
		
		// Ok, so we need to get path -
		String strXPathPathKey = ".//"+blockName+"/@path_symbol";
		Node pathSymbolKeyNode = (Node) _xpath.evaluate(strXPathPathKey, _docPropTree, XPathConstants.NODE);
		String strPathKey = pathSymbolKeyNode.getNodeValue();
		
		// Formulate the xpath -
		String strXPathPathString = ".//path[@symbol='"+strPathKey+"']/@path_location";
		Node pathNode = (Node) _xpath.evaluate(strXPathPathString, _docPropTree, XPathConstants.NODE);
		String strPath = pathNode.getNodeValue();
		
		// package the path in the last element of the array -
		if (!strPath.isEmpty())
		{
			arrList.add(strPath);
		}
		else
		{
			arrList.add("EMPTY");
		}
		
		// return the list -
		return arrList;
	}
	
	public void loadResource(Object object) throws Exception {
	
		try {
			// Grab the model file -
			String strPath = (String)object;
			
			// Ok, bitches, let's load the model file and then hand the DOM tree back -or- allow them to run xpath hits against the tree -
			File configFile = new File(strPath);
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	dbFactory.setNamespaceAware(true);
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	_docPropTree = dBuilder.parse(configFile);
	    	_docPropTree.getDocumentElement().normalize();		
		}
		catch (Exception error)
		{
			System.out.println("ERROR: No bueno. We have a malfunction loading the properties file. "+error.toString());
		}
	}

	public void setProperties(Properties prop) {	
	}
	
	public void setProperty(String key,String value)
	{
		// Set the properties on the prop table -
		_propTable.setProperty(key, value);
	}


	public void setProperty(Object key, Object value) {
		// TODO Auto-generated method stub
		
	}

    public void setProperties(Hashtable prop) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	public void setProperties(XMLPropTree prop) {
		// TODO Auto-generated method stub
		
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
	
}
