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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 *
 * @author jeffreyvarner
 */
public class XMLTreePropertiesTableModel extends AbstractTableModel implements TreeSelectionListener  {
     // Header for the table -
    private String[] _strTableHeaderArr = {"Property keyname","Property value"};
    private VLTreeNode _vltnNode = null;

    // Default data for table -
    private String[][] data=new String[2][2];

    private int currentRowCount = 0;
    private int currentCol = 0;

    private boolean _okToFire = false;


    public XMLTreePropertiesTableModel()
    {
        data[0][0]=" ";
        data[0][1]=" ";
        data[1][0]=" ";
        data[1][1]=" ";
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
        boolean rFlag = true;
    	
    	// Ok, so let's grab the user selected object -
    	if (_vltnNode!=null)
    	{
    		// Ok, so let's grab the ediatble property -
    		String strIsEditable = (String)_vltnNode.getProperty("EDITABLE");
    		if (strIsEditable!=null)
    		{
    			if (strIsEditable.equalsIgnoreCase("TRUE"))
    			{
    				rFlag = true;
    			}
    			else if (strIsEditable.equalsIgnoreCase("FALSE"))
    			{
    				rFlag = false;
    			}
    		}
    	}
    	else
    	{
    		 // do the default -
    		 // Note that the data/cell address is constant,
             // no matter where the cell appears onscreen.
             if (col == 1)
             {
                    rFlag = true;
             }
             else
             {
                    rFlag = false;
             }
    	}
    	
    	return (rFlag);
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
        
        // clear the current data -
        clearData();


        // Get the tree that called this method -
        JTree tree = (JTree)e.getSource();

        // Get the selected node -
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        if (selectedNode!=null)
        {

            // Ok, so I need to get the userobject from this mofo and then pull all the properties out -
            _vltnNode = (VLTreeNode)selectedNode.getUserObject();
            
            // Is the selected item a directory or leaf?
            String strDirectory = (String)_vltnNode.getProperty("DIRECTORY");
            
            // Need to get the data from the xml tree node -
            Node xmlTreeNode = (Node)_vltnNode.getProperty("XML_TREE_NODE");
            
            // If I get here, then my node has no attributes (oh yea, that's what she said...). Maybe I have data has a value
            String strNodeName = xmlTreeNode.getNodeName();
            String strNodeValue = xmlTreeNode.getTextContent();

            if (strNodeValue!=null)
            {
            	if (strDirectory.equalsIgnoreCase("FALSE"))
            	{
            		// Set the value in the table -
            		setValueAt(strNodeName,0,0);
            		setValueAt(strNodeValue,0,1);
            	}
            	else
            	{
            		// Set the value in the table -
            		setValueAt(strNodeName,0,0);
            		setValueAt(" ",0,1);
            	}
            }
            else
            {
            	setValueAt(strNodeName,0,0);
            	setValueAt("Missing ... WTF Kenneth?",0,1);
            }
        }
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

            if (tmp!=null)
            {
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
        }

        // Change the flag -

        // Update the view ...
        this.fireTableDataChanged();
    }

}
