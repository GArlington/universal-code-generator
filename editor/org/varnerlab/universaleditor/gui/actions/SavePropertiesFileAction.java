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


import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.domain.*;



/**
 *
 * @author jeffreyvarner
 */
public class SavePropertiesFileAction implements ActionListener {
    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    public void actionPerformed(ActionEvent e) {

        // First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try
        {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           ModelCodeGeneratorFileEditor windowFrame = (ModelCodeGeneratorFileEditor)focusedComponent.getFocusCycleRootAncestor();

           // Open new file chooser
           JFileChooser fc=new JFileChooser();
           int rVal=fc.showSaveDialog(focusedComponent);

           if (true)
           {

               // Ok, so now I need to dump file to disk -
               
               // Get the windowFrame -
               JTable table = windowFrame.getPropJTable();
               XMLTreePropertiesTableModel tableModel = (XMLTreePropertiesTableModel)table.getModel();

               // Get the dimension of the table -
               int NUM_OF_ROWS = tableModel.getRowCount();
               int NUM_OF_COLS = tableModel.getColumnCount();

               // Go through
               StringBuffer buffer = new StringBuffer();
               for (int row_index = 0;row_index<NUM_OF_ROWS;row_index++)
               {
                   for (int col_index=0;col_index<NUM_OF_COLS;col_index++)
                   {
                        buffer.append(tableModel.getValueAt(row_index, col_index));
                        
                        if (col_index==0)
                        {
                            buffer.append("=");
                        }
                   }

                   buffer.append("\n");
               }

               // Dump to disk -
               File file=fc.getSelectedFile();
               String tmp = file.getPath();
               System.out.println("File path -"+tmp);
               VLIOLib.write(file.getPath(), buffer);

           }
        }
        catch (Exception error)
        {
            System.out.println("Hey now - "+error);
            error.printStackTrace();
        }

    }

}
