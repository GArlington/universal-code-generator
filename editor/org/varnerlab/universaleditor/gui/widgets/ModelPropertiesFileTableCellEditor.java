/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jeffreyvarner
 */
public class ModelPropertiesFileTableCellEditor implements IVLTableCellEditor {


    public DefaultCellEditor TableCellEditor(int row,int col,Vector _vecListItems,VLJTable table)
    {
        // Create a default cell editor -
      JComboBox comboBox = new JComboBox();

      // Get the table model -
      AbstractTableModel _tableModel = (AbstractTableModel)table.getModel();
      String current_row = (String)_tableModel.getValueAt(row, 0);

      if (current_row.equalsIgnoreCase("LOGIC_HANDLER") && col==1)
      {

         // Add true/false to the drop down -
         comboBox.addItem("org.varnerlab.universal.base.Translator");
        

        // Wrap and return -
        DefaultCellEditor ed = new DefaultCellEditor(comboBox);
        return(ed);
      }
      else if (current_row.equalsIgnoreCase("INPUT_HANDLER") && col==1)
      {

         // Add true/false to the drop down -
         comboBox.addItem("org.varnerlab.universal.base.flatfile.LoadVarnerFlatFile");
         comboBox.addItem("org.varnerlab.universal.base.sbml.LoadSBMLFile");


        // Wrap and return -
        DefaultCellEditor ed = new DefaultCellEditor(comboBox);
        return(ed);
      }
      
      else if (current_row.equalsIgnoreCase("OUTPUT_HANDLER") && col==1)
      {

         // Add true/false to the drop down -
         comboBox.addItem("org.varnerlab.universal.base.flatfile.WriteVarnerFlatFile");
         comboBox.addItem("org.varnerlab.universal.base.sbml.WriteSBMLFile");
         comboBox.addItem("org.varnerlab.universal.base.octave.WriteOctaveCModel");
         comboBox.addItem("org.varnerlab.universal.base.gsl.WriteGSLModel");

        // Wrap and return -
        DefaultCellEditor ed = new DefaultCellEditor(comboBox);
        return(ed);
      }
      else
      {
          return (table.getNominalTableCellEdititor(row, col));
      }
    }
}
