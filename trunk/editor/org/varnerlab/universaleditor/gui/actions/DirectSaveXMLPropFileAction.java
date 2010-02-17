/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

// Import statements
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.tree.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.util.*;
import java.io.*;


import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.widgets.*;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 *
 * @author jeffreyvarner
 */
public class DirectSaveXMLPropFileAction implements ActionListener,PropertyChangeListener {
	// class/instance attributes
	Component focusedComponent = null;
	KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private UEditorSession _session = (Launcher.getInstance()).getSession();
	ArrayList<String> _aList = new ArrayList<String>();
	private JOptionPane optionPane;
	private SheetDialogFrame _frame;

	public DirectSaveXMLPropFileAction()
	{
		// These are container labels - 
		_aList.add("Model");
		_aList.add("options");
		_aList.add("case");
		_aList.add("type");
	}

	public void actionPerformed(ActionEvent e) {

		// First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
		try
		{
			// Get the currently focused component -
			focusedComponent = manager.getFocusOwner();
			ModelCodeGeneratorFileEditor windowFrame = (Launcher.getInstance()).getModelCodeGeneratorFileEditorRef();
			
			// Get the file -
			File file = (File)_session.getProperty("LOCAL_SELECTED_FILE");

			// We need to check to see if the file exists -
			if (file.exists())
			{
				
				// Get the tmp file -
				String tmp = file.getPath();
				String strMessage = "Overwrite "+file.getName()+" ?";
				optionPane = new JOptionPane(strMessage,JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION);
				optionPane.addPropertyChangeListener(this);
				JDialog jDialog = optionPane.createDialog(_frame,"Spank");
				windowFrame.showJDialogAsSheet(jDialog);
				
			}
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR: We have an issue saving the file - "+error.toString());
		}

	}
	
	private void doDump(File file, Document doc) throws Exception
	{
		// We need to check to make sure the user wants to override the file?
		
		ModelCodeGeneratorFileEditor windowFrame = (Launcher.getInstance()).getModelCodeGeneratorFileEditorRef();
		
		
		// Get the windowFrame -
		DefaultMutableTreeNode rootNode = windowFrame.getTreeRoot();

		// Create buffer for the file on disk -
		StringBuffer buffer = new StringBuffer();

		// Setup the XML file -
		buffer.append("<?xml version=\"1.0\"?>\n");
		buffer.append("<Template editable=\"false\">\n");

		// Ok, so I need to put in the options and ContainerTags
		String strXPContainer = "/Template/ContainerTags/*";
		buffer.append("\t\t<ContainerTags>\n");
		processNonModelBlocks(doc,strXPContainer,buffer);
		buffer.append("\t\t</ContainerTags>\n");
		buffer.append("\n");

		// Get the options block -
		String strXPOptions = "//options";
		Node optionsNodes = (Node)_xpath.evaluate(strXPOptions, doc,XPathConstants.NODE);
		processMyKids(optionsNodes,buffer);
		buffer.append("\n");

		// Model sections -
		VLTreeNode userGUIRoot = (VLTreeNode)rootNode.getUserObject();

		// Process me -
		// Ok, get the xmlNode that is attached to the userRoot -
		populateContainerList(doc);
		Node xmlNode = (Node)userGUIRoot.getProperty("XML_TREE_NODE");
		processMyModelKids(xmlNode,buffer);

		// close tag 
		buffer.append("</Template>\n");


		// dump the file to disk -
		VLIOLib.write(file.getPath(), buffer);

		// Put the filename in session -
		UEditorSession session = (Launcher.getInstance()).getSession();
		session.setProperty("CURRENT_MODEL_PROP_FILENAME",file.getName());
		SystemwideEventService.fireSessionUpdateEvent();
	}

	private void populateContainerList(Document doc) throws Exception
	{
		String strXPath="//ContainerTags/tag/text()";

		try {
			// Get the item of this type and tag -
			NodeList propNodeList = (NodeList) _xpath.evaluate(strXPath, doc, XPathConstants.NODESET);

			// How many?
			int NUMBER_OF_ITEMS = propNodeList.getLength();
			//System.out.println("Searching tree with xpath = "+strXPath+" returned "+NUMBER_OF_ITEMS+" items");

			for (int index=0;index<NUMBER_OF_ITEMS;index++)
			{
				Node tmpNode = propNodeList.item(index);
				String strName = tmpNode.getNodeValue();

				// Add to combobox -
				_aList.add(strName);
			}
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR: Property lookup failed. The following XPath "+strXPath+" resuled in an error - "+error.toString());
		}
	}

	private void processMyKids(Node xmlNode,StringBuffer buffer) throws Exception
	{ 
		// Ok, we to get the node name -
		String strNodeName = xmlNode.getNodeName();
		if (!strNodeName.contains("#"))
		{
			// Ok, process my kids -
			if (!_aList.contains(strNodeName))
			{
				// process me -
				buffer.append("\t\t\t<");
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
				buffer.append("\t\t<");
				buffer.append(strNodeName);
				//buffer.append(" ");

				// process my attributes -
				processMyAttributes(xmlNode,buffer);

				int NUMBER_OF_KIDS = xmlNode.getChildNodes().getLength();
				if (NUMBER_OF_KIDS>0)
				{
					//buffer.append("\t");
					NodeList kidsList = xmlNode.getChildNodes();
					for (int index=0;index<NUMBER_OF_KIDS;index++)
					{
						processMyKids(kidsList.item(index),buffer);
					}
				}

				buffer.append("\t\t</");
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

	private void processNonModelBlocks(Document doc,String strXPath,StringBuffer buffer)
	{
		// Need to process those blocks from the template tree that have nothing to do with the Model
		ArrayList<String> aList = new ArrayList<String>();
		aList.add("case");
		aList.add("type");


		NodeList propNodeList = null;
		try {
			propNodeList = (NodeList)_xpath.evaluate(strXPath, doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int NUMBER_PATH_NODES = propNodeList.getLength();
		for (int index=0;index<NUMBER_PATH_NODES;index++)
		{
			// Get the current node -
			Node tmpNode = propNodeList.item(index);
			buffer.append("\t\t\t<");
			buffer.append(tmpNode.getNodeName());

			// Process the attributes of this node ..
			NamedNodeMap map = tmpNode.getAttributes();
			int NUMBER_OF_ATTRIBUTES = map.getLength();

			if (NUMBER_OF_ATTRIBUTES==0)
			{
				// Ok, bitches, so I have no attributes. I do the best with what god gave me ....
				buffer.append(">");
				buffer.append(tmpNode.getTextContent());
				buffer.append("</");
				buffer.append(tmpNode.getNodeName());
				buffer.append(">\n");
			}
			else
			{
				// Ok, so I have attributes to deal with ... 
				for (int att_index=0;att_index<NUMBER_OF_ATTRIBUTES;att_index++)
				{
					// Ok, so I should get the attribute name (capitalize it) and key the value 
					Node attNode = map.item(att_index);
					String keyName = ((String)attNode.getNodeName());
					String strValue = attNode.getNodeValue();

					// dump the name=value in the attributes into the buffer 
					buffer.append(" ");
					buffer.append(keyName);
					buffer.append("=\"");
					buffer.append(strValue);
					buffer.append("\" ");
				}
			}
		}
	}

	private void processMyModelKids(Node xmlNode,StringBuffer buffer) throws Exception
	{ 
		// Ok, we to get the node name -
		String strNodeName = xmlNode.getNodeName();
		if (!strNodeName.contains("#"))
		{
			// Ok, process my kids -
			if (!_aList.contains(strNodeName))
			{
				// process me - I'm not a container nor am I a #text node -
				buffer.append("\t<");
				buffer.append(strNodeName);

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

					// Ok, so I just processed my attributes - do I have data?
					String strNodeValue = xmlNode.getTextContent();

					System.out.println("Hey now - I'm looking at "+strNodeName+" has "+strNodeValue);

					if (strNodeValue!=null)
					{
						// Ok, if I'm here then I have text content -
						buffer.append(">");
						buffer.append(strNodeValue);
						buffer.append("</");
						buffer.append(strNodeName);
						buffer.append(">\n");
					}
					else
					{
						// No data - close this motha..
						buffer.append("/>\n");
					}
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
				processMyModelAttributes(xmlNode,buffer);

				int NUMBER_OF_KIDS = xmlNode.getChildNodes().getLength();
				NodeList kidsList = xmlNode.getChildNodes();
				for (int index=0;index<NUMBER_OF_KIDS;index++)
				{
					processMyModelKids(kidsList.item(index),buffer);
				}

				buffer.append("</");
				buffer.append(strNodeName);
				buffer.append(">\n");
			}           
		}
	}


	private void processMyModelAttributes(Node xmlNode,StringBuffer buffer) throws Exception
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

	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals (JOptionPane.VALUE_PROPERTY)) 
		{
					
			ModelCodeGeneratorFileEditor windowFrame = (Launcher.getInstance()).getModelCodeGeneratorFileEditorRef();
			int value = ((Integer)optionPane.getValue()).intValue();
			
			if (value==0)
			{
			
				// Get the template tree from session -
				Document doc = (Document)_session.getProperty("MODEL_TEMPLATE_FILE_TREE");
				File file = (File)_session.getProperty("LOCAL_SELECTED_FILE");
			
				try {
					// Do the dump -
					doDump(file,doc);
				}
				catch (Exception error)
				{
					error.printStackTrace();
				}
				finally
				{
					windowFrame.hideSheet();
				}
			}
			else
			{
				windowFrame.hideSheet();
			}
		}
	}

}
