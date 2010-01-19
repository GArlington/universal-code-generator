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
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.service.SystemwideEventService;

/**
 *
 * @author jeffreyvarner
 */
public class LoadModelPropFileAction implements ActionListener {
    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    public void actionPerformed(ActionEvent e) {
        // Ok, so when I get here - I'm trying to load a properties file from disk -
        LoadPropFileHelper helper = new LoadPropFileHelper();

        // First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           ModelCodeGeneratorFileEditor windowFrame = (ModelCodeGeneratorFileEditor)focusedComponent.getFocusCycleRootAncestor();

           // Open new file chooser
           JFileChooser fc=new JFileChooser();
           int rVal=fc.showOpenDialog(focusedComponent);

           if (rVal==JFileChooser.APPROVE_OPTION)
           {
              File file=fc.getSelectedFile();

              // Get the table model -
              JTable jTable = ((ModelCodeGeneratorFileEditor)windowFrame).getPropJTable();
              CodeGeneratorPropertiesFileTableModel tableModel = (CodeGeneratorPropertiesFileTableModel)jTable.getModel();

              if (tableModel==null)
              {

                // Set new table model -
                jTable.setModel(new CodeGeneratorPropertiesFileTableModel());

              }

               // Ok, load the file -
               helper.populateTableModel(file, tableModel);

              
               // Store the file in case we need to load later - we should just pass the string of the path info (lighter)
               //@tdodo fix the file pass. Too heavy
                windowFrame.setPropFileRef(file);


                

           }

           


        }
        catch (Exception error)
        {
            System.out.println("ERROR in OpenLocalFileAction: "+error.toString());
        }
    }

}
