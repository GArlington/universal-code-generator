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



/**
 *
 * @author jeffreyvarner
 */
public class SaveXMLFileAction implements ActionListener {
    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    public void actionPerformed(ActionEvent e) {

        // First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try
        {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           BioChemExpTool windowFrame = (BioChemExpTool)focusedComponent.getFocusCycleRootAncestor();

           // Open new file chooser
           JFileChooser fc=new JFileChooser();
           int rVal=fc.showSaveDialog(focusedComponent);

           System.out.println("Hey now -");

           if (true)
           {

               // Ok, so now I need to dump file to disk -
               
               // Get the windowFrame -
               DefaultMutableTreeNode rootNode = windowFrame.getTreeRoot();

               // Create buffer for the file on disk -
               StringBuffer buffer = new StringBuffer();

               buffer.append("<?xml version=\"1.0\"?>\n");
               buffer.append("<BCX>\n");

               // Build the tree -
               processMyKids(rootNode,buffer);

               // Add the close tag -
               buffer.append("</BCX>");

               // Dump to disk -
               File file=fc.getSelectedFile();
               String tmp = file.getPath();
               //System.out.println("File path -"+tmp);
               VLIOLib.write(file.getPath(), buffer);

           }
        }
        catch (Exception error)
        {
            // Do nothing -
        }

    }

    private void processMyKids(DefaultMutableTreeNode rootNode,StringBuffer buffer) throws Exception
    {
           // Populate the string buffer -
           VLTreeNode userRoot = (VLTreeNode)rootNode.getUserObject();
           int NUMBER_OF_KIDS = rootNode.getChildCount();
           
           userRoot.writeTree(buffer,NUMBER_OF_KIDS);

           // Add a tab -
           buffer.append("\t");

            // get the number of kids that I have -
           
           for (int kid_index=0;kid_index<NUMBER_OF_KIDS;kid_index++)
           {
                // Get the kid -
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)rootNode.getChildAt(kid_index);

                buffer.append("\t");

                // Call this method on my kids -
                processMyKids(node,buffer);

           }

           if (NUMBER_OF_KIDS==0)
           {

               // If I gete here I'm a leaf - let's remove the last char and replace w/the proper ending -
               int LENGTH = buffer.length();
               //buffer = buffer.replace(LENGTH-1, LENGTH,"/>\n");
           }
           else
           {
                String strClassName = "BCX"+userRoot.getProperty("DISPLAY_LABEL").toString();
                buffer.append("</");
                buffer.append(strClassName);
                buffer.append(">\n");
           }
    }

}
