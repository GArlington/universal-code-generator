/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.domain;

// import statements -
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;
import java.util.*;

import org.varnerlab.universaleditor.gui.widgets.*;


/**
 *
 * @author jeffreyvarner
 */
public class VLFFReactionTableModel extends AbstractTableModel implements TreeSelectionListener  {
     // Header for the table -
    private String[] _strTableHeaderArr = {"Property keyname","Property value"};


    // Default data for table -
    private String[][] data=new String[2][5];

    private int currentRowCount = 0;
    private int currentCol = 0;

    private boolean _okToFire = false;


    public VLFFReactionTableModel()
    {
        data[0][0]=" ";
        data[0][1]=" ";
        data[0][2]=" ";
        data[0][3]=" ";
        data[0][4]=" ";

        data[1][0]=" ";
        data[1][1]=" ";
        data[1][2]=" ";
        data[1][3]=" ";
        data[1][4]=" ";

        
    }

    private void hotResizeDataArray(int rows,int cols)
    {
        String[][] localData = new String[rows][cols];

        // get the current size
        int nRowsCurrent = getRowCount();
        int nColsCurrent = getColumnCount();

        // Copy the contents of data to localData
        for (int row_index = 0;row_index<nRowsCurrent;row_index++)
        {
            for (int col_index = 0;col_index<nColsCurrent;col_index++)
            {
                localData[row_index][col_index]=data[row_index][col_index];
            }
        }

        // reset the data -
        data = localData;

        // reset the current row count -
        currentRowCount = nRowsCurrent+100;
    }


    // Implementations of these methods are required to be a table model -
    public int getRowCount() {
        int intLength = data.length;
        return(intLength);
    }

    public String getColumnName(int col) {
            return _strTableHeaderArr[col];
    }

    public int getColumnCount() {
        int intLength = _strTableHeaderArr.length;
        return(intLength);
    }

    public Object getValueAt(int rowIndex, int colIndex) {
        return (data[rowIndex][colIndex]);

    }

    // Can we edit the table cells?
    public boolean isCellEditable(int row, int col)
    {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
         if (col == 1)
         {
                return true;
         }
         else
         {
                return false;
         }
     }

    public void clearData()
    {
        int ROW_COUNT = this.getRowCount();
        int COL_COUNT = this.getColumnCount();

        for (int row_index = 0;row_index<ROW_COUNT;row_index++)
        {
            for (int col_index = 0;col_index<COL_COUNT;col_index++)
            {
                data[row_index][col_index] = new String("");
            }
        }


    }

    // This method is called when the tree selection changes -
    public void valueChanged(TreeSelectionEvent e)
    {
        // Do nothing for now .... 
    }

    public void toggleEditFlag()
    {
        if (_okToFire)
        {
            _okToFire = false;
        }
        else
        {
            _okToFire = true;
        }
    }

    public boolean okToFireEvent()
    {
        return(_okToFire);
    }

    public Class getColumnClass(int c)
    {
            Class rClass = null;

            rClass = String.class;

            return (rClass);
    }

    @Override
    public void setValueAt(Object obj,int row,int col)
    {
        // Check to make sure I have space in the array
        int nRowsCurrent = getRowCount();
        int nColsCurrent = getColumnCount();


        if (row==nRowsCurrent)
        {
            hotResizeDataArray((nRowsCurrent+1),nColsCurrent);

            // Check to see if string is a boolean -
            String tmp = (String)obj;

            if (tmp.equalsIgnoreCase("TRUE"))
            {
               data[row][col]= tmp;
            }
            else if (tmp.equalsIgnoreCase("FALSE"))
            {
                data[row][col]= tmp;
            }
            else
            {
                data[row][col] = tmp;
            }
        }
        else
        {
            // Check to see if string is a boolean -
            String tmp = (String)obj;

            if (tmp.equalsIgnoreCase("TRUE"))
            {
                //data[row][col]= new Boolean(true);
                data[row][col] = tmp;
            }
            else if (tmp.equalsIgnoreCase("FALSE"))
            {
                //data[row][col]= new Boolean(false);
                data[row][col] = tmp;
            }
            else
            {
                data[row][col] = tmp;
            }
        }

        // Change the flag -

        // Update the view ...
        this.fireTableDataChanged();
    }

}
