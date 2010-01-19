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


import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.parser.*;
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.BioChemExpTool;


/**
 *
 * @author jeffreyvarner
 */
public class CreateNewXMLTreeAction implements ActionListener {

    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    public void actionPerformed(ActionEvent e) {
       // Ok, so when I get here - I'm trying to load a properties file from disk -
       

        // First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           BioChemExpTool windowFrame = (BioChemExpTool)focusedComponent.getFocusCycleRootAncestor();

           // Clear out the tree -
           windowFrame.clearTree();

           // Create a new tree of Domain objects and let the GUI wrap it -
           BCXSystem rootNode = new BCXSystem();
           rootNode.setProperty("DISPLAY_LABEL", "System");
           rootNode.setProperty("NETWORKFILE","");
           
           // Ok, get the username from session -
           UEditorSession session = (Launcher.getInstance()).getSession();
           String strUserName = (String)session.getProperty("VALIDATED_USERNAME");

           

           rootNode.setProperty("USERNAME",strUserName);
           rootNode.setProperty("CLOSED_ICON", VLImageLoader.getPNGImageIcon("JarBundler-10-Grey.png"));
           rootNode.setProperty("OPENED_ICON", VLImageLoader.getPNGImageIcon("JarBundler-10.png"));

           // Ok, when I get here I have a reference to the rootNode - hand this to BioChemExpTool -
           windowFrame.setRootNode(rootNode);
        }
        catch (Exception error)
        {
            error.printStackTrace();
            System.out.println("ERROR in LocalXMLTreeAction: "+error.toString());
        }
    }

}
