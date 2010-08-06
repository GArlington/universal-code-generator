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
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.parser.*;
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;
import org.w3c.dom.Node;


/**
 *
 * @author jeffreyvarner
 */
public class DeleteTreeNodeAction {

    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    public void actionPerformed() {
       // Ok, so when I get here - I'm trying to load a properties file from disk -

        System.out.println("Hey now - why is this not working? Riddle me that looser...");

        // First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           BioChemExpTool windowFrame = (BioChemExpTool)focusedComponent.getFocusCycleRootAncestor();

           // ok, so now I need to add a node -

           // Get the tree and find the current selected node -
           JTree jTree = windowFrame.getTree();
           DefaultTreeModel treeModel = (DefaultTreeModel)jTree.getModel();

           TreePath currentSelection = jTree.getSelectionPath();
            if (currentSelection != null) {
                // Get the current node -
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
                MutableTreeNode parent = (MutableTreeNode)(selectedNode.getParent());

                // Process the xml tree connections -
                // Get the child xml node -
                VLTreeNode vltnChildNode = (VLTreeNode)selectedNode.getUserObject();
    			Node kidXMLNode = (Node)vltnChildNode.getProperty("XML_TREE_NODE");  
    			Node parentXMLNode = kidXMLNode.getParentNode();
    			parentXMLNode.removeChild(kidXMLNode);
                
                // Process the GUI tree -
                if (parent != null)
                {
                    treeModel.removeNodeFromParent(selectedNode);
                   
                    return;
                }
            }

        }
        catch (Exception error)
        {
            error.printStackTrace();
            System.out.println("ERROR in LocalXMLTreeAction: "+error.toString());
        }
    }

}
