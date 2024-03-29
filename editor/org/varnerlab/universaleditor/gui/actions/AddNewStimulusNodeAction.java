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

import java.util.*;
import java.io.*;


import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.parser.*;
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;
import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 *
 * @author jeffreyvarner
 */
public class AddNewStimulusNodeAction implements ActionListener {

	// class/instance attributes
	Component focusedComponent = null;
	KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();

	public void actionPerformed(ActionEvent e) {
		// Ok, so when I get here - I'm trying to load a properties file from disk -


		// First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
		try {
			// Get the currently focused component -
			BioChemExpTool windowFrame = (BioChemExpTool)(Launcher.getInstance()).getBioChemExpToolRef();

			// ok, so now I need to add a node - grab the session =
			UEditorSession session = (Launcher.getInstance()).getSession();

			// Get the tree and find the current selected node -
			JTree jTree = windowFrame.getTree();

			// Get the current node -
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();

			// Ok, so I need to get the userobject from this mofo and then pull all the properties out -
			VLTreeNode vltnNode = (VLTreeNode)selectedNode.getUserObject();
			Node parentXMLNode = (Node)vltnNode.getProperty("XML_TREE_NODE");

			// Ok, so can I add an Experiment node to this node?
			String strClassName = (String)vltnNode.getProperty("KEYNAME");

			if (strClassName.equalsIgnoreCase("EXPERIMENT"))
			{
				// Ok, if I get here then I have the correct type -

				// I've created a new user node ....
				VLTreeNode newNode = new VLTreeNode();
				newNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("PURPLE-12-GREY-ICON"));
				newNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("PURPLE-12-ICON"));
				newNode.setProperty("KEYNAME","stimulus");

				// I need to create the xmlNode -
				String strXPStimulus = "//stimulus";
				Document bcxTmpDoc = (Document)session.getProperty("BCX_TEMPLATE_TREE");
				Node tmpNode = (Node)_xpath.evaluate(strXPStimulus, bcxTmpDoc, XPathConstants.NODE);

				// Clone the node -
				Node clonedXMLTreeNode = tmpNode.cloneNode(false);
				parentXMLNode.appendChild(clonedXMLTreeNode);
				newNode.setProperty("XML_TREE_NODE",clonedXMLTreeNode);

				// Create a new DefaultMutableTreeNode gui node -
				DefaultMutableTreeNode newGUINode = new DefaultMutableTreeNode();
				newGUINode.setUserObject(newNode);

				// Ok - update the tree model -
				DefaultTreeModel model = (DefaultTreeModel)jTree.getModel();
				model.insertNodeInto(newGUINode, selectedNode, selectedNode.getChildCount());

				// Make the new node visible -
				jTree.scrollPathToVisible(new TreePath(newGUINode.getPath()));

				//((DefaultTreeModel)jTree.getModel()).reload(selectedNode);
			}           
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR in LocalXMLTreeAction: "+error.toString());
		}
	}

}
