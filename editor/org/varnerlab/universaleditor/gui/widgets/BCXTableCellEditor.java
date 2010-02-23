/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.SBMLNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jeffreyvarner
 */
public class BCXTableCellEditor implements IVLTableCellEditor {

	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();


	public DefaultCellEditor TableCellEditor(int row,int col,Vector _vecListItems,VLJTable table)
	{
		// Create a default cell editor -
		JComboBox comboBox = new JComboBox();
		
		
		// Get the table model -
		AbstractTableModel _tableModel = (AbstractTableModel)table.getModel();
		String current_row = (String)_tableModel.getValueAt(row, 0);
		UEditorSession session = (Launcher.getInstance()).getSession();

		// set the namespace on the xpath -
		_xpath.setNamespaceContext(new SBMLNamespaceContext());

		if (current_row.equalsIgnoreCase("SPECIES") && col==1)
		{
			Vector<String> _vecSpecies = (Vector)session.getProperty("LIST_OF_SPECIES");
			
			if (_vecSpecies!=null)
			{
				int SIZE = _vecSpecies.size();
				if (SIZE!=0)
				{
					for (int index=0;index<SIZE;index++)
					{
						comboBox.addItem(_vecSpecies.elementAt(index));
					}
				}
				else
				{
					comboBox.addItem("No species are loaded. ");
				}
			}
			else
			{
				comboBox.addItem("No species are loaded. ");
			}

			// Wrap and return -
			DefaultCellEditor ed = new DefaultCellEditor(comboBox);
			return(ed);
		}
		else if (current_row.equalsIgnoreCase("column_type"))
		{
			// Add true/false to the drop down -
			comboBox.addItem("Time");
			comboBox.addItem("Concentration");

			// Wrap and return -
			DefaultCellEditor ed = new DefaultCellEditor(comboBox);
			return(ed);
		}
		else if (current_row.equalsIgnoreCase("steady_state"))
		{
			// Add true/false to the drop down -
			comboBox.addItem("TRUE");
			comboBox.addItem("FALSE");

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
