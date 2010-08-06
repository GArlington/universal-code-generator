package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.BioChemExpTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;
import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CopyAndPasteTreeNodeKeyListener implements KeyListener,ActionListener,PropertyChangeListener  {
	// Class/instance attributes -
    private Component focusedComponent = null;
    private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	private BioChemExpTool _windowFrame = null;
	private Node kidXMLNode = null;
	private Node parentXMLNode = null;
	private VLTreeNode vltnChildNode = null;
    
	
	public void keyPressed(KeyEvent e) {
		// Method attributes -
		
	
		// What key is being pressed?
		int intKeyPressed = e.getKeyCode();
		int intModifierKey = e.getModifiers();
		
		String strModKey = KeyEvent.getKeyText(intKeyPressed);
		
		// Get the currently focused component -
        focusedComponent = manager.getFocusOwner();
        _windowFrame = (BioChemExpTool)focusedComponent.getFocusCycleRootAncestor();

		// check to see of command+c was pushed -
		if (strModKey.equalsIgnoreCase("C") && intModifierKey == 4)
		{
			// Get the tree and find the current selected node -
	        JTree jTree = _windowFrame.getTree();
	        DefaultTreeModel treeModel = (DefaultTreeModel)jTree.getModel();
	        
	        TreePath currentSelection = jTree.getSelectionPath();
            if (currentSelection != null) {
                // Get the current node -
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
                MutableTreeNode parent = (MutableTreeNode)(selectedNode.getParent());

                // Get the child xml node from this tree node -
                vltnChildNode = (VLTreeNode)selectedNode.getUserObject();
                kidXMLNode = (Node)vltnChildNode.getProperty("XML_TREE_NODE");  
            }
		}
		else if (strModKey.equalsIgnoreCase("V") && intModifierKey == 4)
		{
			// ok, so now I need to add a node - grab the session =
			UEditorSession session = (Launcher.getInstance()).getSession();
			
			// Get the currently selected node -
			// Get the tree and find the current selected node -
	        JTree jTree = _windowFrame.getTree();
	        DefaultTreeModel treeModel = (DefaultTreeModel)jTree.getModel();
	        
	        TreePath currentSelection = jTree.getSelectionPath();
            if (currentSelection != null) {
                // Get the current node -
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
                MutableTreeNode parent = (MutableTreeNode)(selectedNode.getParent());

                // Get the child xml node from this tree node -
                VLTreeNode vltnNodeToCopyTo = (VLTreeNode)selectedNode.getUserObject();
                Node xmlNodeToCopyTo = (Node)vltnNodeToCopyTo.getProperty("XML_TREE_NODE");  
                
                // Ok, so we need to determine which node can go where ..
                String strNodeToCopyTo = (String)vltnNodeToCopyTo.getProperty("KEYNAME");
                String strChildNode = (String)vltnChildNode.getProperty("KEYNAME");
                
                // Ok, so do a comparision -
                if (strChildNode.equalsIgnoreCase("EXPERIMENT") && strNodeToCopyTo.equalsIgnoreCase("LISTOFEXPERIMENTS"))
                {
                	// It is ok to add an experiment to a list of experiments -
                	addExperimentNodeToTree(xmlNodeToCopyTo,selectedNode,jTree);
                }
                else if (strChildNode.equalsIgnoreCase("stimulus") && strNodeToCopyTo.equalsIgnoreCase("EXPERIMENT"))
                {
                	// It is ok to add an experiment to a list of experiments -
                	addStimulusNodeToTree(xmlNodeToCopyTo,selectedNode,jTree);
                }
            }
		}
	}
	
	private void addExperimentNodeToTree(Node xmlNodeToCopyTo,DefaultMutableTreeNode selectedNode,JTree jTree)
	{
		// I've created a new user node ....
		VLTreeNode newNode = new VLTreeNode();
		newNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("EVALUE-12-GREY-ICON"));
		newNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("EVALUE-12-ICON"));
		newNode.setProperty("KEYNAME","experiment");
		newNode.setProperty("EDITABLE", "true");
		
		// Clone the node -
		Node clonedXMLTreeNode = kidXMLNode.cloneNode(true);
		xmlNodeToCopyTo.appendChild(clonedXMLTreeNode);
		newNode.setProperty("XML_TREE_NODE",clonedXMLTreeNode);

		// Create a new DefaultMutableTreeNode gui node -
		DefaultMutableTreeNode newGUINode = new DefaultMutableTreeNode();
		newGUINode.setUserObject(newNode);

		// Ok - update the tree model -
		DefaultTreeModel model = (DefaultTreeModel)jTree.getModel();
		model.insertNodeInto(newGUINode, selectedNode, selectedNode.getChildCount());

		// Grab the session -
		UEditorSession session = (Launcher.getInstance()).getSession();
		Document domTree = (Document)session.getProperty("BCX_DOM_TREE");
		
		// Make the new node visible -
		jTree.scrollPathToVisible(new TreePath(newGUINode.getPath()));
		
		try {
			// Reset the tree -
			_windowFrame.setRootNode(domTree);
		}
		catch (Exception error)
		{
			System.out.println("Error adding node to BCX tree ... "+error.toString());
		}
		finally
		{
			// Make the new node visible -
			jTree.scrollPathToVisible(new TreePath(newGUINode.getPath()));
		}
		
	}

	
	private void addStimulusNodeToTree(Node xmlNodeToCopyTo,DefaultMutableTreeNode selectedNode,JTree jTree)
	{
		// I've created a new user node ....
		VLTreeNode newNode = new VLTreeNode();
		newNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("PURPLE-12-GREY-ICON"));
		newNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("PURPLE-12-ICON"));
		newNode.setProperty("KEYNAME","stimulus");
		
		// Clone the node -
		Node clonedXMLTreeNode = kidXMLNode.cloneNode(true);
		xmlNodeToCopyTo.appendChild(clonedXMLTreeNode);
		newNode.setProperty("XML_TREE_NODE",clonedXMLTreeNode);

		// Create a new DefaultMutableTreeNode gui node -
		DefaultMutableTreeNode newGUINode = new DefaultMutableTreeNode();
		newGUINode.setUserObject(newNode);

		// Ok - update the tree model -
		DefaultTreeModel model = (DefaultTreeModel)jTree.getModel();
		model.insertNodeInto(newGUINode, selectedNode, selectedNode.getChildCount());

		// Make the new node visible -
		jTree.scrollPathToVisible(new TreePath(newGUINode.getPath()));
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}

}
