package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.gui.widgets.SheetDialogFrame;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CheckPropertiesFileAction implements ActionListener,PropertyChangeListener {
	private ModelCodeGeneratorFileEditor _tool = null;
	private UEditorSession _session = null;
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private JOptionPane optionPane;
	private SheetDialogFrame _frame;

	
	public void actionPerformed(ActionEvent e) {
		
		// Get the reference to the tool -
		_tool = (Launcher.getInstance()).getModelCodeGeneratorFileEditorRef();
		
		// Get the session -
		_session = (Launcher.getInstance()).getSession();
		
		// Ok, so let's get the root node and and check to see if all the required nodes have text -
        DefaultMutableTreeNode rootNode = _tool.getTreeRoot();
        
        // Model sections -
        VLTreeNode userGUIRoot = (VLTreeNode)rootNode.getUserObject();
        Node xmlNode = (Node)userGUIRoot.getProperty("XML_TREE_NODE");
        Vector<String> aList = new Vector<String>();
        
        // Check to see if this file is ok?
        if (!isFileOk(xmlNode,aList))
        {
        	// Get the list of issues -
        	int NUMBER_OF_PROBLEMS = aList.size();
        	StringBuffer buffer = new StringBuffer();
        	buffer.append("Missing items \n");
        	for (int index=0;index<NUMBER_OF_PROBLEMS;index++)
        	{
        		buffer.append(aList.get(index));
        		buffer.append("\n");
        	}
        	
        	//System.out.println("This file is ok ...");
        	optionPane = new JOptionPane(buffer.toString(),JOptionPane.WARNING_MESSAGE,JOptionPane.PLAIN_MESSAGE);
			optionPane.addPropertyChangeListener(this);
			JDialog jDialog = optionPane.createDialog(_frame,"Spank");
			_tool.showJDialogAsSheet(jDialog);
        }
        else
        {
        	optionPane = new JOptionPane("Universal properties file is complete.",JOptionPane.WARNING_MESSAGE,JOptionPane.PLAIN_MESSAGE);
			optionPane.addPropertyChangeListener(this);
			JDialog jDialog = optionPane.createDialog(_frame,"Spank");
			_tool.showJDialogAsSheet(jDialog);
        }
	}
	
	private boolean isFileOk(Node xmlNode,Vector<String> aList)
	{
		// method attributes -
		boolean isOk = true;
		
		// Clear out the issue log -
		aList.removeAllElements();
		
		// Get the required tags -
		String strReqdTags = "/descendant::*[@required='true']";
		
		// Get the list of required nodes -
		try {
			// tmpList -
			NodeList tmpList = (NodeList)_xpath.evaluate(strReqdTags,xmlNode,XPathConstants.NODESET);
			int NUMBER_LIST = tmpList.getLength();
			String strTextContent = "";
			for (int index=0;index<NUMBER_LIST;index++)
			{
				// Ok, get the data on this node -
				Node tmpNode = (Node)tmpList.item(index);
							
				// Does this tag have content?
				strTextContent = tmpNode.getTextContent();
				String strName = tmpNode.getNodeName();
				if (strTextContent.isEmpty())
				{
					aList.add(strName);
					isOk = false;
				}
			}
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// return -
		return(isOk);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals (JOptionPane.VALUE_PROPERTY)) 
		{
			_tool.hideSheet();
		}
		
	}

}
