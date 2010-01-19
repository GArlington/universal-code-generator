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
import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.parser.*;
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.BioChemExpTool;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;
import org.varnerlab.universaleditor.service.VLIconManagerService;


/**
 *
 * @author jeffreyvarner
 */
public class AddNewPropertyLeafAction implements ActionListener {

    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    public void actionPerformed(ActionEvent e) {
       // Ok, so when I get here - I'm trying to load a properties file from disk -
       DefaultMutableTreeNode selectedNode = null;

        System.out.println("Hey now - why is this not working? Riddle me that looser...");

        // First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           ModelCodeGeneratorFileEditor windowFrame = (ModelCodeGeneratorFileEditor)focusedComponent.getFocusCycleRootAncestor();

           // ok, so now I need to add a node -

           // Get the tree and find the current selected node -
           JTree jTree = windowFrame.getTree();

           if (jTree!=null)
           {
                // Get the current node -
                selectedNode = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
           }
           

           if (selectedNode!=null)
           {
               // Ok, so I need to get the userobject from this mofo and then pull all the properties out -
               VLTreeNode vltnNode = (VLTreeNode)selectedNode.getUserObject();

               // Ok, so can I add an Experiment node to this node?
               String strClassName = (String)vltnNode.getProperty("CLASSNAME");

               System.out.println("What is the parent node - "+strClassName);
               if (strClassName.equalsIgnoreCase("org.varnerlab.universaleditor.domain.VLUniversal"))
               {
                    // Ok, if I get here then I have the correct type -

                    // I've created a new user node ....
                    VLTreeNode newNode = new VLTreeNode();
                    newNode.setProperty("DISPLAY_LABEL","<Add new property key>");
                    newNode.setProperty("CLASSNAME", "org.varnerlab.universaleditor.domain.VLProperty");
                    newNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-GREY-ICON"));
                    newNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-ICON"));

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
        }
        catch (Exception error)
        {
            error.printStackTrace();
            System.out.println("ERROR in LocalXMLTreeAction: "+error.toString());
        }
    }

}
