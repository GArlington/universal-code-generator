package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Color;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NameList;
import org.w3c.dom.NodeList;


import javax.xml.xpath.*;

public class XPathRemoteFileSystemSelectionListener implements ListSelectionListener {
	// Class/instance -
	private Hashtable _propTable = new Hashtable();

	public void setReferences(String key,Object obj)
	{
		_propTable.put(key, obj);
	}

	private String getPathString(String strPath)
	{
		String strTmp = "";

		// Get the session -
		UEditorSession session = (UEditorSession)_propTable.get("SESSION");
		String strUserName = (String)session.getProperty("VALIDATED_USERNAME");

		// Get the index of the username -
		int INT_USERNAME = strPath.indexOf(strUserName);
		int INT_LAST_SLASH = strPath.lastIndexOf("/");

		// Get the selected dir -
		strTmp = strPath.substring(INT_USERNAME,INT_LAST_SLASH);

		return(strTmp);
	}

	private void checkPaths(Document doc, String strPath)
	{
		// Method attibutes -
		JComboBox jComboBox = (JComboBox)_propTable.get("REMOTE_COMBOBOX");

		// Get the selected item -
		int INT_N_ITEMS = jComboBox.getItemCount();
		for (int index=0;index<INT_N_ITEMS;index++)
		{
			String strSelectedItemName = ((File)jComboBox.getItemAt(index)).getName();
			System.out.println("item["+index+"] = "+strSelectedItemName);
		}


		System.out.println("Pathstring = "+strPath);

	}

	public void valueChanged(ListSelectionEvent e) {
		// Get the stuff from the hashtable -
		DefaultListModel listModel = (DefaultListModel)_propTable.get("LIST_MODEL");
		JList jList = (JList)_propTable.get("JLIST");
		UEditorSession session = (UEditorSession)_propTable.get("SESSION");
		VLRemoteFileSystemListCellRenderer renderer = (VLRemoteFileSystemListCellRenderer)_propTable.get("REMOTE_CELL_RENDERER");
		JComboBox jComboBox = (JComboBox)_propTable.get("REMOTE_COMBOBOX");
		String strFileSystemName = (String)_propTable.get("REMOTE_FILESYSTEM_TREE");
		VLListDoubleClickAdaptor _doubleClickAdapter = (VLListDoubleClickAdaptor)_propTable.get("MOUSE_ADAPTER");
		Document doc = null;

		// Get the selected file -
		File file = (File)jList.getSelectedValue();

		if (file!=null)
		{     	
			// Check to see if this is a dir or a file -
			String strFileName = file.getName();
			String strDirFlag = renderer.getDirectoryFlag(strFileName);

			if (!strDirFlag.equalsIgnoreCase("DIRECTORY"))
			{
				return;
			}

			// Ok, let's get the remote filesystem tree from the session object -
			if (strFileSystemName!=null)
			{
				doc = (Document)session.getProperty(strFileSystemName);
			}
			else
			{
				doc = (Document)session.getProperty("REMOTE_FILESYSTEM_TREE");
			}

			// Populate the combobox ------------------------------------------- //
			// I need to check to see of the item is already in the combobox -
			Vector<String> tmpVector = new Vector<String>();
			int NUMBER_OF_ITEMS = jComboBox.getItemCount();
			for (int index=0;index<NUMBER_OF_ITEMS;index++)
			{
				tmpVector.addElement(((File)jComboBox.getItemAt(index)).getAbsolutePath());
			}

			
			// ------------------------------------------------------------------- //
			

			// first thing is we need to delete items on the combo box that are below me -
			// trim the list -
			int INT_SELECTED_ITEM = jComboBox.getSelectedIndex();
			int ITEM_COUNT = jComboBox.getItemCount();

			// Generate the xpath string -
			StringBuffer tmpBuffer = new StringBuffer();
			tmpBuffer.append("//");
			int INT_SELECTED_INDEX = jComboBox.getSelectedIndex();
			for (int index=0;index<ITEM_COUNT;index++)
			{
				// Get the raw path -
				String strTmpRaw = tmpVector.get(index);

				// Get the name of the selected item -
				int INT_LAST_SLASH = strTmpRaw.lastIndexOf("/");
				String strTmpNew = strTmpRaw.substring(INT_LAST_SLASH+1, strTmpRaw.length());

				
				if (index==0)
            	{
            		// Ok, add a Dir call to the xpath string -
                	tmpBuffer.append("jdv27[@name='");
                	tmpBuffer.append(strTmpNew);
                	tmpBuffer.append("']");
            	}
				else
				{
				
					// Ok, add a Dir call to the xpath string -
					tmpBuffer.append("Directory[@name='");
					tmpBuffer.append(strTmpNew);
					tmpBuffer.append("']");
				}

				if (index<=NUMBER_OF_ITEMS-1)
				{
					tmpBuffer.append("/");
				}
			}

			// Ok, so always add the currently selected dir to the xpath string (the jcombo box lags)
			tmpBuffer.append("Directory[@name='");
			tmpBuffer.append(strFileName);
			tmpBuffer.append("']");

			// Ok, so we need to formulate the correct xpath string -
			String expression = tmpBuffer.toString();

			// Ok, so let's fire up the xpath -
			XPath xpath = XPathFactory.newInstance().newXPath();

			// Try and grab the dirNode corresponding to this session -
			try {
				
				System.out.println("Evaluating "+expression+" in "+this.getClass().getName());
				Node dirNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);

				if (dirNode!=null)
				{
					// clear the model -
					listModel.clear();

					// Update the combo box -
					NamedNodeMap dirNodeAttributes = dirNode.getAttributes();
					String strDirNodeTmp = dirNodeAttributes.getNamedItem("name").getNodeValue();

					// what is the value of the path attribute?
					String strPath = dirNodeAttributes.getNamedItem("path").getNodeValue();

					// Ok, so we need to set the current selected node in the session -
					session.setProperty("SELECTED_REMOTE_PATH", strPath);
					SystemwideEventService.fireNetworkUpdateEvent();
					SystemwideEventService.fireSessionUpdateEvent();

					// Ok, so I have a directory node - get the kids which are directories -
					String strDirXPath = expression+"/Directory";
					NodeList children = (NodeList) xpath.evaluate(strDirXPath, dirNode, XPathConstants.NODESET);
					int NUMBER_OF_KIDS = children.getLength();
					for (int index = 0;index<NUMBER_OF_KIDS;index++)
					{
						Node childNode = children.item(index);
						NamedNodeMap childAttributes = childNode.getAttributes();
						String strChildTmp = childAttributes.getNamedItem("name").getNodeValue();

						// Check for hidden directory -
						int INT_DOT = strChildTmp.indexOf(".");
						if (INT_DOT!=0)
						{

							// Reset the dir flag just in case it has not been set -
							renderer.setDirectoryFlag(strChildTmp,"DIRECTORY");
							renderer.setBackground(Color.BLUE);

							// Create a file -
							File tmpFile = new File(strChildTmp);
							listModel.addElement(tmpFile);
						}
					}

					// Ok, so I have a directory node - get the kids which are files -
					NodeList children_files = (NodeList) xpath.evaluate(expression+"/File", dirNode, XPathConstants.NODESET);
					int NUMBER_OF_FILES = children_files.getLength();
					for (int index = 0;index<NUMBER_OF_FILES;index++)
					{
						Node childNode = children_files.item(index);
						NamedNodeMap childAttributes = childNode.getAttributes();
						String strChildTmp = childAttributes.getNamedItem("name").getNodeValue();

						// Check for hidden file -
						int INT_DOT = strChildTmp.indexOf(".");
						if (INT_DOT!=0)
						{

							// System.out.println("Name of child = "+strChildTmp);

							// put in the model -

							// Reset the dir flag just in case it has not been set -
							renderer.setDirectoryFlag(strChildTmp,"FILE");
							renderer.setBackground(Color.BLUE);                   

							// Create a file -
							File tmpFile = new File(strChildTmp);
							listModel.addElement(tmpFile);
						}
					}
					
					// Ok, so I've put the items into the vector - let's use the contains option
					String strFilePath = file.getAbsolutePath();
					System.out.println("Inside the "+this.getClass().toString()+" class just above the combox box update - "+strFilePath+" is not yet visible.");
					if (!tmpVector.contains(strFilePath))
					{
						// We need one extra bit of logic - 
						// We are having a problem with peer dirs - we don't have both peers in the list?
						// checkPaths(doc,strFilePath);

						// Add the dir to drop down -
						jComboBox.addItem(file);
						jComboBox.setSelectedItem(file);
					}
					else
					{
						// Ok, so we already have this item just set the selected item -
						jComboBox.setSelectedItem(file);
					}
					
					System.out.println("Inside the "+this.getClass().toString()+" class just below the combox box update - "+strFilePath+" should be visible.");
				}
				else
				{
					System.out.println("I'm lost... "+expression+" gave me nada!");

					// One issue is the I have a problem w/the combo box .. it doens't handle peer dirs correctly. We can assume that when we get here 
					// we have that issue and try and fix - 
					//StringBuffer hackBuffer = new StringBuffer();

					// Generate a hack -
					//generateHackXPath(hackBuffer,jComboBox,tmpVector,strFileName);
					
					//String strHackExpression = hackBuffer.toString();
					//System.out.println("I'm realy lost... running hack "+strHackExpression);
					//populateList(strHackExpression,doc,listModel,session,renderer);
					
				}
			} 
			catch (XPathExpressionException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
	}

	private void populateList(String expression,Document doc,DefaultListModel listModel,UEditorSession session,VLRemoteFileSystemListCellRenderer renderer)
	{
		try {

			// Ok, so let's fire up the xpath -
			XPath xpath = XPathFactory.newInstance().newXPath();
			Node dirNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);

			if (dirNode!=null)
			{
				// clear the model -
				listModel.clear();

				// Update the combo box -
				NamedNodeMap dirNodeAttributes = dirNode.getAttributes();
				String strDirNodeTmp = dirNodeAttributes.getNamedItem("name").getNodeValue();

				// what is the value of the path attribute?
				String strPath = dirNodeAttributes.getNamedItem("path").getNodeValue();

				// Ok, so we need to set the current selected node in the session -
				session.setProperty("SELECTED_REMOTE_PATH", strPath);
				SystemwideEventService.fireNetworkUpdateEvent();

				// Ok, so I have a directory node - get the kids which are directories -
				String strDirXPath = expression+"/Directory";
				NodeList children = (NodeList) xpath.evaluate(strDirXPath, dirNode, XPathConstants.NODESET);
				int NUMBER_OF_KIDS = children.getLength();
				for (int index = 0;index<NUMBER_OF_KIDS;index++)
				{
					Node childNode = children.item(index);
					NamedNodeMap childAttributes = childNode.getAttributes();
					String strChildTmp = childAttributes.getNamedItem("name").getNodeValue();

					// Check for hidden directory -
					int INT_DOT = strChildTmp.indexOf(".");
					if (INT_DOT!=0)
					{

						//System.out.println("Name of child = "+strChildTmp);

						// put in the model -

						// Reset the dir flag just in case it has not been set -
						renderer.setDirectoryFlag(strChildTmp,"DIRECTORY");
						renderer.setBackground(Color.BLUE);

						// Create a file -
						PublishService.submitData("I'm looking at what node - "+childNode);
						File tmpFile = new File(strChildTmp);
						listModel.addElement(tmpFile);
					}
				}

				// Ok, so I have a directory node - get the kids which are files -
				NodeList children_files = (NodeList) xpath.evaluate(expression+"/File", dirNode, XPathConstants.NODESET);
				int NUMBER_OF_FILES = children_files.getLength();
				for (int index = 0;index<NUMBER_OF_FILES;index++)
				{
					Node childNode = children_files.item(index);
					NamedNodeMap childAttributes = childNode.getAttributes();
					String strChildTmp = childAttributes.getNamedItem("name").getNodeValue();

					// Check for hidden file -
					int INT_DOT = strChildTmp.indexOf(".");
					if (INT_DOT!=0)
					{

						// System.out.println("Name of child = "+strChildTmp);

						// put in the model -

						// Reset the dir flag just in case it has not been set -
						renderer.setDirectoryFlag(strChildTmp,"FILE");
						renderer.setBackground(Color.BLUE);                   

						// Create a file -
						PublishService.submitData("I'm looking at what node - "+childNode);
						File tmpFile = new File(strChildTmp);
						listModel.addElement(tmpFile);
					}
				}
			}
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
	}

	private void generateHackXPath(StringBuffer tmpBuffer,JComboBox jComboBox,Vector<String> tmpVector,String strFileName)
	{
		// Generate the xpath string -
		tmpBuffer.append("//");
		int INT_SELECTED_INDEX = jComboBox.getSelectedIndex();
		int NUMBER_OF_ITEMS = jComboBox.getItemCount();
		for (int index=1;index<INT_SELECTED_INDEX-1;index++)
		{
			// Get the raw path -
			String strTmpRaw = tmpVector.get(index);

			// Get the name of the selected item -
			int INT_LAST_SLASH = strTmpRaw.lastIndexOf("/");
			String strTmpNew = strTmpRaw.substring(INT_LAST_SLASH+1, strTmpRaw.length());

			// Ok, add a Dir call to the xpath string -
			tmpBuffer.append("Directory[@name='");
			tmpBuffer.append(strTmpNew);
			tmpBuffer.append("']");

			if (index<=NUMBER_OF_ITEMS-1)
			{
				tmpBuffer.append("/");
			}
		}

		// Ok, so always add the currently selected dir to the xpath string (the jcombo box lags)
		tmpBuffer.append("Directory[@name='");
		tmpBuffer.append(strFileName);
		tmpBuffer.append("']");

	}

}
