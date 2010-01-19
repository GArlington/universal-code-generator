/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Component;
import javax.swing.*;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import java.awt.Color;




/**
 *
 * @author jeffreyvarner
 */
public class PropertiesTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    // Class/instance attributes -
    private DefaultCellEditor _jtxtField = new DefaultCellEditor(new JTextField());
    private DefaultCellEditor _jcbTrueFalse = new DefaultCellEditor(new JComboBox());
    private DefaultCellEditor _currComp = null;


    public PropertiesTableCellEditor()
    {
    }

    public Object getCellEditorValue() {
        
        return(_currComp.getCellEditorValue());
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        // The unnderlying data model is string -
        String tmp = (String)value;
        

        System.out.println("I'm trying to set the editor ....");

        if (tmp.equalsIgnoreCase("TRUE"))
        {

            ((JComboBox)_jcbTrueFalse.getComponent()).removeAllItems();
            ((JComboBox)_jcbTrueFalse.getComponent()).addItem("FALSE");
            ((JComboBox)_jcbTrueFalse.getComponent()).addItem("TRUE");

            _currComp = _jcbTrueFalse;

            return(_jcbTrueFalse.getComponent());
        }
        else if (tmp.equalsIgnoreCase("FALSE"))
        {
            ((JComboBox)_jcbTrueFalse.getComponent()).removeAllItems();
            ((JComboBox)_jcbTrueFalse.getComponent()).addItem("TRUE");
            ((JComboBox)_jcbTrueFalse.getComponent()).addItem("FALSE");

            _currComp = _jcbTrueFalse;

            return(_jcbTrueFalse.getComponent());
        }

        else
        {
            
            ((JTextField)_jtxtField.getComponent()).setText(tmp);
            ((JTextField)_jtxtField.getComponent()).setSelectedTextColor(new Color(0,0,0));
            ((JTextField)_jtxtField.getComponent()).setSelectionColor(new Color(153,255,153));
            ((JTextField)_jtxtField.getComponent()).setBackground(new Color(153,255,153));

           
            _currComp = _jtxtField;

            return(_jtxtField.getTableCellEditorComponent(table, value, isSelected, row, column));
        }
    }





}
