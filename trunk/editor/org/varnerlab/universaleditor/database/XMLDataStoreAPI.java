package org.varnerlab.universaleditor.database;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.BioChemExpTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.VLIOLib;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLDataStoreAPI {
	// Class/instance attributes -
	private Hashtable _propTable = new Hashtable();
	private UEditorSession _session = null;
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	ArrayList<String> aList = new ArrayList<String>();
	
	public void setProperty(Object key,Object value)
	{
		_propTable.put(key, value);
	}
	 
	public Object getProperty(Object key)
	{
		return(_propTable.get(key));
	}
	
	public XMLDataStoreAPI()
	{
		// Grab the session -
		_session = (Launcher.getInstance()).getSession();
		
		// populate the containers list -
		aList.add("listOfUsers");
		aList.add("listOfProjects");
		aList.add("projects");
		aList.add("UserData");
	}
	
	private void processMyKids(Node xmlNode,StringBuffer buffer) throws Exception
	{ 
		// Ok, we to get the node name -
		String strNodeName = xmlNode.getNodeName();
		if (!strNodeName.contains("#"))
		{
			// Ok, process my kids -
			if (!aList.contains(strNodeName))
			{
				// process me -
				buffer.append("\t<");
				buffer.append(strNodeName);
				//buffer.append(" ");

				int NUMBER_OF_ATTR = xmlNode.getAttributes().getLength();
				NamedNodeMap attList = xmlNode.getAttributes();
				if (NUMBER_OF_ATTR>0)
				{
					// Ok, process my attributes -
					for (int index=0;index<NUMBER_OF_ATTR;index++)
					{
						Node attributeNode = attList.item(index);

						// Set the value in the table -
						String strName = attributeNode.getNodeName();
						String strAtt = attributeNode.getNodeValue();

						// Add to buffer -
						buffer.append(" ");
						buffer.append(strName);
						buffer.append("=\"");
						buffer.append(strAtt);
						buffer.append("\"");			
					}

					buffer.append("/>\n");
					
					System.out.println("Hey now - bufer = "+buffer.toString());
				}
				else
				{
					// Ok, so if I get here then I have no attributes, but I can have data in elements -
					// If I get here, then my node has no attributes (oh yea, that's what she said...). Maybe I have data has a value
					String strName = xmlNode.getNodeName();
					String strNodeValue = xmlNode.getTextContent();

					System.out.println("Hey now - I'm looking at "+strName+" has "+strNodeValue);

					if (strNodeValue!=null)
					{
						// Ok, if I'm here then I have text content -
						buffer.append(">");
						buffer.append(strNodeValue);
						buffer.append("</");
						buffer.append(strName);
						buffer.append(">\n");
					}
				}
			}
			else
			{
				buffer.append("<");
				buffer.append(strNodeName);
				//buffer.append(" ");

				// process my attributes -
				processMyAttributes(xmlNode,buffer);

				int NUMBER_OF_KIDS = xmlNode.getChildNodes().getLength();
				NodeList kidsList = xmlNode.getChildNodes();
				for (int index=0;index<NUMBER_OF_KIDS;index++)
				{
					processMyKids(kidsList.item(index),buffer);
				}

				buffer.append("</");
				buffer.append(strNodeName);
				buffer.append(">\n");
			}           
		}
	}


	private void processMyAttributes(Node xmlNode,StringBuffer buffer) throws Exception
	{
		int NUMBER_OF_ATTR = xmlNode.getAttributes().getLength();
		NamedNodeMap attList = xmlNode.getAttributes();
		if (NUMBER_OF_ATTR>0)
		{
			// Ok, process my attributes -
			for (int index=0;index<NUMBER_OF_ATTR;index++)
			{
				Node attributeNode = attList.item(index);

				// Set the value in the table -
				String strName = attributeNode.getNodeName();
				String strAtt = attributeNode.getNodeValue();

				// Add to buffer -
				buffer.append(" ");
				buffer.append(strName);
				buffer.append("=\"");
				buffer.append(strAtt);
				buffer.append("\"");			
			}

			buffer.append(">\n");
		}
		else
		{
			buffer.append(">\n");
		}

	}

	private void doDump(File file,Document doc)
	{
		// Ok, so now I need to dump file to disk -

		try {
			
			// Create buffer for the file on disk -
			StringBuffer buffer = new StringBuffer();

			// put header string -
			buffer.append("<?xml version=\"1.0\"?>\n");

			// Build the tree -
			processMyKids(doc.getDocumentElement(),buffer);

			// Ok, let's replace the < and > chars so we can re-import
			String tmpBuffer = buffer.toString();
			String strTwo = tmpBuffer.replaceAll("<=", "&lt;=");
			String strThree = strTwo.replaceAll("=>", "=&gt;");
			String strFour = strThree.replaceAll("->", "-&gt;");

			// Dump to disk -
			VLIOLib.write(file.getPath(),strFour);
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
	}
	
	public void insertProjectInformation(String userName,String strSessionID,String strProjectName) throws Exception
	{
		// Method attributes -
		Document doc = (Document)_session.getProperty("XMLDATASTORE_DOM_TREE");
		
		// Get the filename -
		String strPath = (String)_session.getProperty("XML_DATASTORE_PATH");
		File fileUserData = new File(strPath);
		
		// Ok, so we need to insert a node in the project tree and then dump the tree to disk?
		
		// Create a project node and set its attributes -
		Element tmpNode = (Element)doc.createElement("project");
		tmpNode.setAttribute("projectname",strProjectName);
		tmpNode.setAttribute("sessionid", strSessionID);
		
		// Get the my project node -
		String strProjectsXPath = "//projects[@username='"+userName+"']";
		Node nodeList = (Node)_xpath.evaluate(strProjectsXPath,doc,XPathConstants.NODE);
		nodeList.appendChild(tmpNode);
		
		// Ok, let's dump this to disk -
		doDump(fileUserData,doc);
	}
	
	public Hashtable<String,String> getUserProjects(String strUserName) throws Exception
	{
		// Method attributes -
		Hashtable<String,String> translationTable = new Hashtable<String,String>();
		Document doc = (Document)_session.getProperty("XMLDATASTORE_DOM_TREE");
		
		// Get the project nodes associated with this user -
		String strProjectsXPath = "//projects[@username='"+strUserName+"']/project/@sessionid";
		NodeList nodeList = (NodeList)_xpath.evaluate(strProjectsXPath,doc,XPathConstants.NODESET);
		int NUMBER_OF_PROJECTS = nodeList.getLength();
		for (int project_index=0;project_index<NUMBER_OF_PROJECTS;project_index++)
		{
			// Get the project sessionid -
			Node tmpNode = nodeList.item(project_index);
			String strSessionID = tmpNode.getNodeValue();
			
			// Get the actual name -
			String strRealNameXPath = "//projects[@username='"+strUserName+"']/project[@sessionid='"+strSessionID+"']/@projectname";
			String strRealName = queryXMLTree(doc,strRealNameXPath);

			// put in the table -
			translationTable.put(strSessionID, strRealName);
		}
		
		// return the table -
		return(translationTable);
	}
	
	
	public boolean checkUserNameAndPassword(String strUserName,String strPassword) throws Exception
	{
		// Method attributes -
		boolean blnFlag = false;
		
		// Ok, we need to get the dom tree -
		Document doc = (Document)_session.getProperty("XMLDATASTORE_DOM_TREE");
		
		// Find password associated with this username -
		String strUserListXPath = "//listOfUsers/user/@name";
		NodeList nodeList = (NodeList)_xpath.evaluate(strUserListXPath,doc,XPathConstants.NODESET);
		int NUMBER_OF_USERS = nodeList.getLength();
		for (int user_index=0;user_index<NUMBER_OF_USERS;user_index++)
		{
			// Get the username -
			Node tmpNode = nodeList.item(user_index);
			String strUserNameLocal = tmpNode.getNodeValue();
			
			// Ok, so I need to checl to see if the username passed in is equal to one I just looked up -
			if (strUserName.equalsIgnoreCase(strUserNameLocal))
			{
				// If I get here then I have a match -
				// Need to check the password -
				String strXPathPassword = "//listOfUsers/user[@name='"+strUserNameLocal+"']/@password";
				String strPasswordLocal = queryXMLTree(doc,strXPathPassword);
				
				// Compare passwords?
				if (strPassword.equalsIgnoreCase(strPasswordLocal))
				{
					// Ok, I have a match ... 
					blnFlag = true;
					return(blnFlag);
				}
			}
		}
		
		// return false by default -
		return(blnFlag);
	}
	
	// Get a string -
	private String queryXMLTree(Document xmlTree,String strXPath)
	{
		// Method attributes -
		String strProp = "";
		
		try {
			Node propNode = (Node) _xpath.evaluate(strXPath, xmlTree, XPathConstants.NODE);
			strProp = propNode.getNodeValue();
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR: Property lookup failed on XMLTree. The following XPath "+strXPath+" resuled in an error - "+error.toString());
		}
		
		return(strProp);
	}
	
}
