/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jeffreyvarner
 */
public class VLRemoteComboBoxSelectionListener implements ActionListener {
     // Class/instance -
    private Hashtable _propTable = new Hashtable();

    public void setReferences(String key,Object obj)
    {
        _propTable.put(key, obj);
    }
    
    // Ok, when I get here I have selected a combox directory. I need to rebuild the list -
    public void actionPerformed(ActionEvent e) {
        // Method attributes and references -
        DefaultListModel listModel = (DefaultListModel)_propTable.get("LIST_MODEL");
        JList jList = (JList)_propTable.get("JLIST");
        UEditorSession session = (UEditorSession)_propTable.get("SESSION");
        VLRemoteFileSystemListCellRenderer renderer = (VLRemoteFileSystemListCellRenderer)_propTable.get("REMOTE_CELL_RENDERER");
        JComboBox jComboBox = (JComboBox)_propTable.get("REMOTE_COMBOBOX");
        IVLProcessTreeNode tool = (IVLProcessTreeNode)_propTable.get("FILE_TRANSFER_TOOL");
        String strFileSystemName = (String)_propTable.get("REMOTE_FILESYSTEM_TREE");
        Document doc = null;

         // Clear out the dir -
        listModel.clear();

        // Repopulate the dir starting from the current dir -
        File file = (File)jComboBox.getSelectedItem();
        
        if (file!=null)
        {

            // Get the name of the selected file -
            String strFileName = file.getName();
            String strUserName = (String)session.getProperty("VALIDATED_USERNAME");
            if (strFileName.equalsIgnoreCase(strUserName))
            {
                
                // So if I get here I have dir that is the root -
            	// Ok, so let's fire up the xpath -
            	doc = (Document)session.getProperty("REMOTE_FILESYSTEM_TREE");
            	XPath xpath = XPathFactory.newInstance().newXPath();
            	String expression = "*";
            	           	
            	try {
    				Node dirNode = (Node) xpath.evaluate("//*", doc, XPathConstants.NODE);
    				
    				if (dirNode!=null)
    				{
    					listModel.clear();
                        tool.processNodes((Node)dirNode, listModel,true);
    				}
    				else
    				{
    					PublishService.submitData("The dirNode is null? WTF?");
    				}
            	}
            	catch (Exception error)
            	{
            		error.printStackTrace();
            	}
            }
            else
            {
            	
            	// Ok, so we selected on a *non-root* element -

                // Check to see if this is a dir or a file -
                String strDirFlag = renderer.getDirectoryFlag(strFileName);

                if (strFileSystemName!=null)
                {
                    doc = (Document)session.getProperty(strFileSystemName);
                }
                else
                {
                    doc = (Document)session.getProperty("REMOTE_FILESYSTEM_TREE");
                }

                if (doc!=null)
                {
                    
                	// So if I get here I have dir that is not the root -
                	// Ok, so let's fire up the xpath -
                	XPath xpath = XPathFactory.newInstance().newXPath();
                	
                	// OK, if the dirname has a ssid_ in it, then set the current project flag -
                	int intIndex = strFileName.indexOf("ssid_");
                	if (intIndex!=-1)
                	{
                		session.setProperty("SELECTED_SESSION_ID",strFileName);
                		SystemwideEventService.fireSessionUpdateEvent();
                	}
                	
                	Vector<String> tmpVector = new Vector<String>();
                    int NUMBER_OF_ITEMS = jComboBox.getItemCount();
                    for (int index=0;index<NUMBER_OF_ITEMS;index++)
                    {
                    	tmpVector.addElement(((File)jComboBox.getItemAt(index)).getAbsolutePath());
                    }
                	
                	// Generate the xpath string -
                    StringBuffer tmpBuffer = new StringBuffer();
                    tmpBuffer.append("//");
                    int INT_SELECTED_INDEX = jComboBox.getSelectedIndex();
                	for (int index=1;index<INT_SELECTED_INDEX;index++)
                	{
                		// Get the raw path -
                		String strTmpRaw = tmpVector.get(index);
                	
                		// Get the name of the selected item -
                		int INT_LAST_SLASH = strTmpRaw.lastIndexOf("/");
                    	String strTmpNew = strTmpRaw.substring(INT_LAST_SLASH+1, strTmpRaw.length());
                    	
                    	// Ok, add a Dir call to the xpath string -
                    	tmpBuffer.append("Directory[@name='");
                    	tmpBuffer.append(strTmpNew);
                    	tmpBuffer.append("']");
                    	
                    	if (index<=NUMBER_OF_ITEMS-1)
                    	{
                    		tmpBuffer.append("/");
                    	}
                    }
                
                    // Ok, so always add the currently selected dir to the xpath string (the jcombo box lags)
                	tmpBuffer.append("Directory[@name='");
                	tmpBuffer.append(strFileName);
                	tmpBuffer.append("']");
                    
                    // Ok, so we need to formulate the correct xpath string -
                    String expression = tmpBuffer.toString();
                    
                	//String expression = "//Directory[@name='"+strFileName+"']/*";
                	
                	// What is the xpath?
                	//PublishService.submitData("What is the xpath expression [leaf node] - "+expression);
                	System.out.println("What is the xpath expression [leaf node] - "+expression);
                	
                	try {
        				Node dirNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        				
        				if (dirNode!=null)
        				{
        					listModel.clear();
        					tool.processChildNodes(expression, listModel, false);
        				}
        				else
        				{
        					System.out.println("The dirNode is null? WTF?");
        				}
                	}
                	catch (Exception error)
                	{
                		
                	}
                	
                	/*
                	NodeList list = doc.getElementsByTagName("Directory");
                    Boolean blnFlag = true;
                    int counter = 0;
                    while (blnFlag)
                    {
                        // Get a node -
                        Node node = list.item(counter);

                        if (node==null)
                        {
                            break;
                        }

                        // Get the <attributes> name -
                        NamedNodeMap map = node.getAttributes();

                        // Get the name for comparison -
                        String strTestNode = map.getNamedItem("name").getNodeValue();

                        if (strTestNode.equalsIgnoreCase(strFileName))
                        {
                            listModel.clear();
                            tool.processNodes(node, listModel);
                            blnFlag = false;
                        }
                        else
                        {
                            counter++;
                        }
                    }*/
                }
            }
        }
    }
}
