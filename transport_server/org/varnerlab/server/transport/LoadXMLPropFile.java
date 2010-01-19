package org.varnerlab.server.transport;

// import statements -
import java.util.Hashtable;
import java.util.Properties;

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

public class LoadXMLPropFile implements IInputHandler {
	// Class attributes -
	Properties _propTable = new Properties();
	
	// Create a xpFactory/xpath obj (we'll use this a zillion times -)
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private Document _docPropTree = null;
	

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
			System.out.println("ERROR: No bueno. We have a malfunction loading the Job properties file. "+error.toString());
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

	public void setProperties(LoadXMLPropFile prop) {
		// TODO Auto-generated method stub
		
	}
	
}
