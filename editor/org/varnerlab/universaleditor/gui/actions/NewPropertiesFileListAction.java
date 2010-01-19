/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

// Import statements
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.varnerlab.universaleditor.gui.*;

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


import javax.swing.table.TableModel;
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.domain.*;

/**
 *
 * @author jeffreyvarner
 */
public class NewPropertiesFileListAction implements ActionListener {
    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    // Ok, so when I get here I have I have populate the JTable with the appropriate options for my file type -
    public void actionPerformed(ActionEvent e) {
        // Method attributes -


        try
        {
           // Ok, so I need to get the table from the tool and replace the table model -
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           ModelCodeGeneratorFileEditor windowFrame = (ModelCodeGeneratorFileEditor)focusedComponent.getFocusCycleRootAncestor();

           // Get the table -
           JTable table = windowFrame.getPropJTable();

           // Ok, now I need to the new Prop -
           table.setModel(new NewCodeGeneratorPropertiesFileTableModel());




        }
        catch (Exception error)
        {
            // eat the assumption for now ...
        }

    }

}
