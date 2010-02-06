package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NetworkEditorTool;
import org.varnerlab.universaleditor.gui.actions.DeleteSBMLTreeNodeAction;
import org.varnerlab.universaleditor.gui.actions.DeleteTreeNodeAction;
import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NewSBMLTreeLeafKeyAdaptor extends KeyAdapter {

	// class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	
	public void keyPressed(KeyEvent e)
	{
		//System.out.println("What key is being pressed? "+e.getKeyCode()+" what modifier? "+e.getModifiers());
		
		// Check for the backspace - Not sure if this key index is apple specific ...
        if (e.getKeyCode()==KeyEvent.VK_BACK_SPACE)
        {
            // Create new action -
            DeleteSBMLTreeNodeAction actionObj = new DeleteSBMLTreeNodeAction();
            actionObj.actionPerformed();
        }
        else if (e.getKeyCode()==61 && e.getModifiers()==4)
        {
        	
        	// Get the currently focused component -
	        focusedComponent = manager.getFocusOwner();
	        NetworkEditorTool windowFrame = (NetworkEditorTool)focusedComponent.getFocusCycleRootAncestor();
        	
        	// Ok, so here we are going to try and add a node -
            UEditorSession session = Launcher.getInstance().getSession();
           
            // Get the tree and find the current selected node -
            JTree jTree = windowFrame.getTree();
            DefaultTreeModel treeModel = (DefaultTreeModel)jTree.getModel();
            TreePath currentSelection = jTree.getSelectionPath();
            
             if (currentSelection != null) {
                 // Get the current node -
                 DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
                 DefaultMutableTreeNode parent = (DefaultMutableTreeNode)(selectedNode.getParent());
                 
                 // Get the level -
                 int intLevel = jTree.getRowForPath(currentSelection);
                 
                 // Ok, so we need to create a new xml node -
                 VLTreeNode vlNode = (VLTreeNode)selectedNode.getUserObject();
                 
                 // Ok, so here is the trick bit -
                 Node xmlTreeNode = (Node)vlNode.getProperty("XML_TREE_NODE");
                 Node xmlParentNode = xmlTreeNode.getParentNode();
                 
                 // Get some info from the xmlNode -
                 String strNodeName = (String)xmlTreeNode.getNodeName();
                 String strNodeValue = (String)xmlTreeNode.getTextContent();
                 
                 // Set the display label -
 	        	 String strDisplayLabel = "";
 	        	 String strAttributeID = "";
 	        	
 	        	 // Check to see if we have an id attribute -
 	        	 Node leafNode = xmlTreeNode.getAttributes().getNamedItem("id");
 	        		        	
 	        	 if (leafNode!=null)
 	        	 {
 	        		strAttributeID = leafNode.getNodeValue();
 	        		//System.out.println("not null leadNode named "+strNodeName+" has id = "+strAttributeID);

 	        	 }
 	        	 else
 	        	 {
 	        		System.out.println("leadNode named "+strNodeName+" has id = null");
 	        	 }
 	        	
 	        	 if (!strAttributeID.isEmpty())
 	        	 {
 	        		strDisplayLabel = strAttributeID;
 	        	 }
 	        	 else
 	        	 {
 	        		strDisplayLabel = strNodeName;
 	        	 }
                 
 	        	 // Create a clone and hook to the tree -
                 Node xmlCloneNode = xmlTreeNode.cloneNode(true);
                 xmlParentNode.appendChild(xmlCloneNode);
                 
                 
                 // Reload the tree - this is going to be super slow for a huge tree ...
                 Document doc = (Document)session.getProperty("SBML_NETWORK_DOM_TREE");
                 try {
					windowFrame.setRootNode(doc,intLevel);
					jTree.scrollRowToVisible(intLevel);
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			     
                 /* 
                 // Create a new vltree node and the associated GUI node -
                 VLTreeNode newNode = new VLTreeNode();
                 newNode.setProperty("DISPLAY_LABEL",strDisplayLabel);
 	        	 newNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-GREY-ICON"));
 	        	 newNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-ICON"));
                 newNode.setProperty("XML_TREE_NODE",xmlCloneNode);
                 newNode.setProperty("EDITABLE","true");
                 
                 // Set the display label 
                 newNode.setProperty("KEYNAME",strDisplayLabel);                
 	        	 newNode.setProperty(strDisplayLabel,xmlTreeNode.getTextContent());
                 
                 // 
                 DefaultMutableTreeNode newGUINode = new DefaultMutableTreeNode();   
                 newGUINode.setUserObject(newNode);
                 
                 // Ok - update the tree model -
                 treeModel.insertNodeInto(newGUINode, parent, parent.getChildCount());
                          
                 // Make the new node visible -
                 jTree.scrollPathToVisible(new TreePath(newGUINode.getPath()));
                 */
             }
        }
	}
}
