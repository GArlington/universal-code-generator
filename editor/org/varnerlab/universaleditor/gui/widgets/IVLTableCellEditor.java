/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.util.Vector;
import javax.swing.DefaultCellEditor;

/**
 *
 * @author jeffreyvarner
 */
public interface IVLTableCellEditor {


    public DefaultCellEditor TableCellEditor(int row,int col,Vector _vecListItems,VLJTable table);
}
