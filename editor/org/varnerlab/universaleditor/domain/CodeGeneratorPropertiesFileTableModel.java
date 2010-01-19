/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.domain;

// Import statements -
import javax.swing.table.AbstractTableModel;
import org.varnerlab.universaleditor.gui.Launcher;

/**
 *
 * @author jeffreyvarner
 */
public class CodeGeneratorPropertiesFileTableModel extends AbstractTableModel {
    // Class/instance attributes -
    

    // Header for the table -
    private String[] _strTableHeaderArr = {"Property keyname","Property value"};


    // Default data for table -
    private String[][] data=new String[2][2];

    private int currentRowCount = 0;
    private int currentCol = 0;

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

    public CodeGeneratorPropertiesFileTableModel()
    {
        data[0][0]=" ";
        data[0][1]=" ";
        data[1][0]=" ";
        data[1][1]=" ";
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

        // Update the view ...
        this.fireTableDataChanged();
    }

    public void printDebugData()
    {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
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


    public void hideWorkingDirInfo()
    {
        

        try
        {
            // Ok, I need to go through the data array and remove the working dir info -
            int NUMBER_OF_ROWS = this.getRowCount();
            for (int row_index = 0;row_index<NUMBER_OF_ROWS;row_index++)
            {
                Object row_value = data[row_index][1];

                // I need to check to see if this is a string -
                if (row_value instanceof String)
                {
                    String tmp = (String)row_value;

                    // Find the last instance of "/" - should repl
                    String sep = Launcher._SLASH;
                    int intLastSlash = tmp.lastIndexOf(sep);
                    int intLength = tmp.length();

                    // Grab the last chunk of the string - substr is not efficient -
                    // @todo - fix the substring call;
                    String lastChunck = tmp.substring(intLastSlash+1, intLength);
                 

                    // Put back in the table -
                    this.setValueAt(lastChunck, row_index, 1);
                }
                else
                {
                    // do nothing for now -
                }
            }
        }
        catch (Exception error)
        {

        }

       
    }

    //
}
