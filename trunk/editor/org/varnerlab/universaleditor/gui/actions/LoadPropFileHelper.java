/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

import javax.swing.JTable;
import javax.swing.table.*;

import java.util.*;
import java.io.*;


import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.domain.*;



/**
 *
 * @author jeffreyvarner
 */
public class LoadPropFileHelper {



    public void populateTableModel(File file,AbstractTableModel model) throws Exception
    {
        // Load the props file
        Properties propTable = new Properties();
        propTable.load(new FileInputStream(file));



         // Ok, when I get here I have all the data loaded into a properties object -
         // I need to get a hold of the jtable, get a reference to its model and put the data into the model-

         // Get the TableModel -
         CodeGeneratorPropertiesFileTableModel tableModel = (CodeGeneratorPropertiesFileTableModel)model;
         tableModel.clearData();


         // Iterate through the prop keys and values
         Enumeration iter = propTable.keys();
         int row_counter = 0;
         while (iter.hasMoreElements())
         {
            // Get the key and the value and put into the table model -
            String key = (String)iter.nextElement();
            String value = propTable.getProperty(key);

             // Jam into the model
            tableModel.setValueAt(key, row_counter, 0);


            // Check to see if value is true -or- false 
            if (value.equalsIgnoreCase("FALSE"))
            {
                Boolean blnVal = new Boolean(value);
                tableModel.setValueAt(blnVal,row_counter, 1);
            }
            else
            {
                tableModel.setValueAt(value,row_counter, 1);
            }


            // Update the counter -
            row_counter++;

            //System.out.println("I should be setting data -"+key+" and "+value);
         }

         tableModel.printDebugData();

    }
}
