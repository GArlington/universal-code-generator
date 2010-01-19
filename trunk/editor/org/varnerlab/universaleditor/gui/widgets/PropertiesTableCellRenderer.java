/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

// import -
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.*;


/**
 *
 * @author jeffreyvarner
 */
public class PropertiesTableCellRenderer implements TableCellRenderer  {



    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        // Method attributes -
        Component comp = null;

        // Get the current renderere -
        TableCellRenderer tableRenderer = table.getCellRenderer(row, column);

        comp = table.prepareRenderer(tableRenderer, row, column);

        if (row%2==0)
        {
            comp.setBackground(Color.GRAY);
        }

        return(comp);
    }



  

}
