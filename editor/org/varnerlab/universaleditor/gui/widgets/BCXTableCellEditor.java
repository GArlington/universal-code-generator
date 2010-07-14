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
		else if (current_row.equalsIgnoreCase("code_output_handler"))
		{
			// Get the list of supported output handlers -
			String strXPathOutputHandler = "//listOfOutputHandlers/handler";

			// Ok, so now let's get the template DOM tree
			Document doc = (Document)session.getProperty("TEMPLATE_DOM_TREE");
			try {
				NodeList tmpList = (NodeList)_xpath.evaluate(strXPathOutputHandler, doc, XPathConstants.NODESET);
				int N = tmpList.getLength();
				if (N>0)
				{
					for (int index=0;index<N;index++)
					{
						Node item = tmpList.item(index);
						if (item!=null)
						{
							String strValue = item.getTextContent();
							if (strValue!=null && !strValue.isEmpty())
							{
								// Add true/false to the drop down -
								comboBox.addItem(strValue);
							}
						}
						else
						{
							comboBox.addItem("No output handlers?");
						}
					}
				}
				else
				{
					comboBox.addItem("No output handlers are registered...");
				}

			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				comboBox.addItem("Error!...Run....");
			}

			// Wrap and return -
			DefaultCellEditor ed = new DefaultCellEditor(comboBox);
			return(ed);
		}
		else if (current_row.equalsIgnoreCase("scaling"))
		{
			// Add scaling options -
			comboBox.addItem("BETA");
			comboBox.addItem("ZERO_TO_ONE");

			// Wrap and return -
			DefaultCellEditor ed = new DefaultCellEditor(comboBox);
			return(ed);
		}
		else if (current_row.equalsIgnoreCase("column_type"))
		{
			// Add true/false to the drop down -
			comboBox.addItem("Time");
			comboBox.addItem("Concentration, mass or other");

			// Wrap and return -
			DefaultCellEditor ed = new DefaultCellEditor(comboBox);
			return(ed);
		}
		else if (current_row.equalsIgnoreCase("search_network"))
		{
			// Add true/false to the drop down -
			comboBox.addItem("TRUE");
			comboBox.addItem("FALSE");

			// Wrap and return -
			DefaultCellEditor ed = new DefaultCellEditor(comboBox);
			return(ed);
		}
		else if (current_row.equalsIgnoreCase("file_type"))
		{
			// Add true/false to the drop down -
			comboBox.addItem("ascii");
			comboBox.addItem("mat-binary");

			// Wrap and return -
			DefaultCellEditor ed = new DefaultCellEditor(comboBox);
			return(ed);
		}
		else if (current_row.equalsIgnoreCase("steady_state") || current_row.equalsIgnoreCase("exact_name"))
		{
			// Add true/false to the drop down -
			comboBox.addItem("TRUE");
			comboBox.addItem("FALSE");

			// Wrap and return -
			DefaultCellEditor ed = new DefaultCellEditor(comboBox);
			return(ed);
		}
		else if (current_row.equalsIgnoreCase("basis"))
		{
			// Add true/false to the drop down -
			comboBox.addItem("ABSOLUTE");
			comboBox.addItem("FRACTION");

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
