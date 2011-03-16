package org.varnerlab.server.localtransportlayer;

/*
 * Copyright (c) 2011 Varnerlab, 
 * School of Chemical and Biomolecular Engineering, Cornell
 * University, Ithaca NY 14853 USA.
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
 */

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
	
	// put some type strings here -
	public static final String FileNameWithExtension = "FILENAME_WITH_EXTENSION";
	public static final String FunctionName = "FUNCTION_NAME";
	public static final String FilePath = "FILENAME_PATH";
	public static final String FullyQualifiedPath = "FULLY_QUALIFIED_PATH";
	

	/**
	 * Returns an instance of the XML document edited in the GUI
	 * @param null
	 */
	public Object getResource(Object object) throws Exception {
		// TODO Auto-generated method stub
		return (_docPropTree);
	}

	/**
	 * Return the string value obtained from executing the XPath query passed in as an argument
	 * @param String strXPath
	 * @return String - get property from uxml tree by executing string in strXPath
	 */
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
	
	/**
	 * Return the path for the keyname passed in as an argument. Executes a xpath lookup in the path
	 * block of the specification tree:
	 * 
	 * ".//ListOfPaths/path[@symbol='"+keyName+"']/@path_location"
	 *
	 * @param String keyName
	 * @throws Exception
	 * @return - Get path for keyName
	 */
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
	
	/**
	 * Return the package name for the keyName. Executes a xpath lookup in the package block
	 * of the specification tree:
	 * 
	 * ".//ListOfPackages/package[@symbol='"+keyName+"']/@package_name";
	 * 
	 * @param String keyName
	 * @throws Exception
	 * @return String - Package name
	 */
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
	
	/**
	 * Returns a Hashatble<String,String> with path and file name information for a specific blockname.
	 * 
	 * Hashtable keys:
	 * FILENAME_WITH_EXTENSION	-	Returns a filename with the extension
	 * FUNCTION_NAME			-	Returns the function name (filename w/no extension)
	 * FILENAME_PATH			-	Returns the path of the file (lookup on the path block)
	 * FULLY_QUALIFIED_PATH		-	Returns the fully qualified filename w/path
	 * 
	 * @param String blockName
	 * @throws Exception
	 * @Hashtable<String,String> - Hashtable holding path information
	 */
	public Hashtable<String,String> buildFilenameBlockDictionary(String blockName) throws Exception
	{
		// Method attributes -
		Hashtable<String,String> hashtable = new Hashtable<String,String>();
		
		// Get the filename for the data -
		String strXPathFilename = ".//"+blockName+"/@filename";
		Node filenameNode = (Node) _xpath.evaluate(strXPathFilename, _docPropTree, XPathConstants.NODE);
		
		// ok, the first element of ArrayList is the filename -
		String tmpString = filenameNode.getNodeValue();
		if (!tmpString.isEmpty())
		{
			// Get the filename -
			hashtable.put("FILENAME_WITH_EXTENSION",tmpString);
			
			// Get the "function name"
			int INDEX_OF_DOT = tmpString.indexOf(".");
			hashtable.put("FUNCTION_NAME",tmpString.substring(0, INDEX_OF_DOT));
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
			hashtable.put("FILENAME_PATH",strPath);
		}
		
			
		if (!tmpString.isEmpty() && !strPath.isEmpty())
		{
		
			System.out.println("I'm inside the if - "+strPath+" is the path and "+tmpString+" is the filename.");
			
			// Lastly fully qualified path -
			hashtable.put(XMLPropTree.FullyQualifiedPath,strPath+"/"+tmpString);
			
			System.out.println("What is in the hashatble? - "+hashtable.get(XMLPropTree.FullyQualifiedPath));
		}
		
		// return -
		return hashtable;
	}
	
	/**
	 * Returns information held in the block with name blockName
	 * 
	 * Index 0: Holds filename
	 * Index 1: Holds the path to the file
	 * Index 2: Fully qualified path
	 * 
	 * My advice: Use the hashtable based method. We may yank this ...
	 * 
	 * @param String blockName 
	 * @throws Exception
	 * @return ArrayList<String> - List of path information
	 */
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
	
	/**
	 * Load the uxnml specification file from disk
	 * 
	 * @param object - Path to the uxml file (called internally by UNIVERSAL)
	 * @throws Exception
	 * @return void
	 */
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

	/*
	 * 
	 */
	public void setLogger(Logger log) {
		_logger = log;
	}
	
}
