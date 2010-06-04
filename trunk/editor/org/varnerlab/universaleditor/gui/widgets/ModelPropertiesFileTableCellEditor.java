/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.table.AbstractTableModel;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jeffreyvarner
 */
public class ModelPropertiesFileTableCellEditor implements IVLTableCellEditor {
	// Class/instnace variables -
	String _strSelectedItem = "";
	private Document _docTree = null;
	private Document _templateTree = null;
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	
	public void setCurrentOutputType(String type)
	{
		_strSelectedItem = type;
	}
	
	public void setDocumentTree(Document doc)
	{
		_docTree = doc;
	}
	
	public void setTemplateTree(Document doc)
	{
		_templateTree = doc;
	}
	
	private DefaultCellEditor XPathCellEditor(int row,int col,Vector _vecListItems,VLJTable table,String type)
	{
		// Get the table model -
    	AbstractTableModel _tableModel = (AbstractTableModel)table.getModel();
    	String current_row = ((String)_tableModel.getValueAt(row, 0)).toUpperCase();
	
    	// Create a default cell editor -
 	   	JComboBox comboBox = new JComboBox();
 	   	
 	   	// Get the type
 	   	String strXPath = "//options/case[@tag='"+type+"']/type[@tag='"+current_row+"']/item/text()";  	
 	   	System.out.println("Query the tree with "+strXPath);
 	   		   	
 	   	try {
			// Get the item of this type and tag -
 	   		NodeList propNodeList = (NodeList) _xpath.evaluate(strXPath, _docTree, XPathConstants.NODESET);
			
			// How many?
 	   		int NUMBER_OF_ITEMS = propNodeList.getLength();
 	   		if (NUMBER_OF_ITEMS>0)
 	   		{
 	   			System.out.println("Searching tree with xpath = "+strXPath+" returned "+NUMBER_OF_ITEMS+" items");
 	   		
	 	   		for (int index=0;index<NUMBER_OF_ITEMS;index++)
	 	   		{
	 	   			Node tmpNode = propNodeList.item(index);
	 	   			String strName = tmpNode.getNodeValue();
	 	   			
	 	   			// Add to combobox -
	 	   			comboBox.addItem(strName);
	 	   		}
	 	   		
	 	   		// Wrap and return -
	 	        DefaultCellEditor ed = new DefaultCellEditor(comboBox);
	 	        return(ed);
 	   		}
 	   		else
 	   		{
 	   			return(table.getNominalTableCellEdititor(row, col));
 	   		}
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR: Property lookup failed. The following XPath "+strXPath+" resuled in an error - "+error.toString());
		}
		
		return null;
 	   	   	
	}
	
    public DefaultCellEditor TableCellEditor(int row,int col,Vector _vecListItems,VLJTable table)
    {
        
    	// Ok, so run specific logic for each *output* type - 
    	
    	// Get the type from the selected string -   	
    	if (_strSelectedItem.equalsIgnoreCase("Graphviz dot-file (*.dot)"))
    	{
    		String type="GRAPHVIZ-DOT";
    		return(XPathCellEditor(row,col,_vecListItems,table,type));
    	}
    	else if (_strSelectedItem.equalsIgnoreCase("Octave C-code (*.cc)"))
    	{
    		String type="OCTAVE-C";
    		return(XPathCellEditor(row,col,_vecListItems,table,type));
    	}
    	else if (_strSelectedItem.equalsIgnoreCase("File type translation (flatfile to SBML conversion)"))
    	{
    		String type="SBML";
    		return(XPathCellEditor(row,col,_vecListItems,table,type));
    	}
    	else if (_strSelectedItem.equalsIgnoreCase("File type translation (SBML to flatfile conversion)"))
    	{
    		String type="VFF";
    		return(XPathCellEditor(row,col,_vecListItems,table,type));
    	}
    	
    	else if (_strSelectedItem.equalsIgnoreCase("Sundials model (*.c)"))
    	{
    		String type="SUNDIALS";
    		return(XPathCellEditor(row,col,_vecListItems,table,type));
    	}
    	else if (_strSelectedItem.equalsIgnoreCase("Biochemical experiment specification file (*.xml)"))
    	{
    		String type="BCX";
    		return(XPathCellEditor(row,col,_vecListItems,table,type));
    	}
    	else
    	{
    		return (table.getNominalTableCellEdititor(row, col));
    	}
    }
}
