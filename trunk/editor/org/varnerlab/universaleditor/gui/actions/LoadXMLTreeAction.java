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
import org.varnerlab.universaleditor.gui.BioChemExpTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.service.SystemwideEventService;


/**
 *
 * @author jeffreyvarner
 */
public class LoadXMLTreeAction implements ActionListener {

    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    public void actionPerformed(ActionEvent e) {
       // Ok, so when I get here - I'm trying to load a properties file from disk -
       

        // First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           IVLTreeGUI windowFrame = (IVLTreeGUI)focusedComponent.getFocusCycleRootAncestor();

           // Open new file chooser
           JFileChooser fc=new JFileChooser();
           int rVal=fc.showOpenDialog(focusedComponent);

           if (rVal==JFileChooser.APPROVE_OPTION)
           {
              // Get the fc -
              File file=fc.getSelectedFile();

              // Load the root node -
              VLDomainComposite rootNode =(VLDomainComposite)Builder.doBuild(file.getAbsolutePath(),IDContentHandler.CHID_XMLTREE_HANDLER);

              // Ok, when I get here I have a reference to the rootNode - hand this to BioChemExpTool -
              windowFrame.setRootNode(rootNode);

              // Put the filename in session -
              UEditorSession session = (Launcher.getInstance()).getSession();
              session.setProperty("CURRENT_MODEL_PROP_FILENAME",file.getName());
           }

           // Fire the update -
           SystemwideEventService.fireSessionUpdateEvent();
        }
        catch (Exception error)
        {
            error.printStackTrace();
            System.out.println("ERROR in LocalXMLTreeAction: "+error.toString());
        }
    }

}
