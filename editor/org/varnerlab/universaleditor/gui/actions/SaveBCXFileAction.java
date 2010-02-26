/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

// Import statements
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.tree.*;

import java.util.*;
import java.io.*;


import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.widgets.*;
import org.varnerlab.universaleditor.domain.*;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 *
 * @author jeffreyvarner
 */
public class SaveBCXFileAction implements ActionListener {
	// class/instance attributes
	Component focusedComponent = null;
	KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	private ArrayList<String> aList = new ArrayList<String>();

	public SaveBCXFileAction()
	{
		aList.add("Model");
		aList.add("server_options");
		aList.add("listOfExperiments");
		aList.add("experiment");
		aList.add("measurement_file");
	}

	public void actionPerformed(ActionEvent e) {

		// First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
		try
		{
			// Get the currently focused component -
			BioChemExpTool windowFrame = (BioChemExpTool)(Launcher.getInstance()).getBioChemExpToolRef();
			UEditorSession session = (Launcher.getInstance()).getSession();

			// Open new file chooser
			JFileChooser fc=new JFileChooser();
			
			String strLocalSelectPath = (String)session.getProperty("LOCAL_SELECTED_PATH");
			if (strLocalSelectPath!=null && !strLocalSelectPath.isEmpty())
			{
				fc.setCurrentDirectory(new File(strLocalSelectPath));
			}
			else
			{
				fc.setCurrentDirectory(new File(Launcher._CURRENT));
			}

			int rVal=fc.showSaveDialog(windowFrame);

			System.out.println("Hey now -");

			if (true)
			{

				// Ok, so now I need to dump file to disk -

				// Get the windowFrame -
				DefaultMutableTreeNode rootNode = windowFrame.getTreeRoot();

				// Create buffer for the file on disk -
				StringBuffer buffer = new StringBuffer();

				// Populate the string buffer -
				VLTreeNode userRoot = (VLTreeNode)rootNode.getUserObject();

				// Process me -
				// Ok, get the xmlNode that is attached to the userRoot -
				Node xmlNode = (Node)userRoot.getProperty("XML_TREE_NODE");

				// put header string -
				buffer.append("<?xml version=\"1.0\"?>\n");

				// Build the tree -
				processMyKids(xmlNode,buffer);

				// Ok, let's replace the < and > chars so we can re-import
				String tmpBuffer = buffer.toString();
				String strTwo = tmpBuffer.replaceAll("<=", "&lt;=");
				String strThree = strTwo.replaceAll("=>", "=&gt;");
				String strFour = strThree.replaceAll("->", "-&gt;");

				// Dump to disk -
				File file=fc.getSelectedFile();
				if (file!=null)
				{
					VLIOLib.write(file.getPath(),strFour);
				}

			}
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}

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
}