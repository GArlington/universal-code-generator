package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VLPropEditorComboBoxRenderer extends DefaultListCellRenderer {
	private Document _doc = null;
	
	public void setDocumentRoot(Document doc)
	{
		_doc = doc;
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
		// Get the selected string -
		String strName = (String)value;
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		Node node = null;
		XPath xpath = XPathFactory.newInstance().newXPath();
    	String expression = "//mapping/display[@name=\""+strName+"\"]/parent::mapping/@type";
		try {
	    	node = (Node) xpath.evaluate(expression, _doc, XPathConstants.NODE);
		}
		catch (Exception error)
		{
			System.out.println("ERROR: Failed to lookup the type attribute from the expression "+expression+" error = "+error.toString());
		}
		
		// Get the code extension -
        String strFExt = node.getNodeValue();
		
        System.out.println("Extension - "+strFExt);
        
		// Check -
		if (strFExt.equalsIgnoreCase("OCTAVE-C"))
		{
			
			setIcon(VLIconManagerService.getIcon("CPPFILE-8-ICON"));
			setText(strName);
		}
		else if (strFExt.equalsIgnoreCase("MATLAB-M"))
		{
			setIcon(VLIconManagerService.getIcon("MATLAB-8-ICON"));
			setText(strName);
		}
		else if (strFExt.equalsIgnoreCase("SBML"))
		{
			setIcon(VLIconManagerService.getIcon("SBMLFILE-8-ICON"));
			setText(strName);
		}
		else
		{
			setIcon(VLIconManagerService.getIcon("TXTFILE-8-ICON"));
			setText(strName);
		}
		
		
		// return -
		return(this);
    }
	
}
