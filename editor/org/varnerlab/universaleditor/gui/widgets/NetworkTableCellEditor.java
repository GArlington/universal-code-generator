/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author jeffreyvarner
 */
public class NetworkTableCellEditor implements IVLTableCellEditor {


    public DefaultCellEditor TableCellEditor(int row,int col,Vector _vecListItems,VLJTable table)
    {
        // Create a default cell editor -
      JComboBox comboBox = new JComboBox();

      // Get the table model -
      AbstractTableModel _tableModel = (AbstractTableModel)table.getModel();
      String current_row = (String)_tableModel.getValueAt(row, 0);

      if (current_row.equalsIgnoreCase("REVERSIBLE") && col==1)
      {

         // Add true/false to the drop down -
         comboBox.addItem("True");
         comboBox.addItem("False");

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
