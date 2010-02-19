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
            	String expression = "//"+strUserName+"[@name='"+strUserName+"']";
            	           	
            	try {
    				
            		
            		Node dirNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
            		System.out.println("Evaluating "+expression+" in "+this.getClass().getName()+" returned what = "+dirNode+" from document "+doc);
            		
    				if (dirNode!=null)
    				{
    					listModel.clear();    					
                        tool.processNodes((Node)dirNode, listModel,true);
    				}
    				else
    				{
    					System.out.println("The dirNode in "+this.getClass().toString()+" is null? WTF?");
    				}
            	}
            	catch (Exception error)
            	{
            		//error.printStackTrace();
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
                		
                		// if I get here then I've selected a project. Delete all trailing elements from the combo box -
                		// Get the number of elements currently in the combo box -
                		int NUMBER_OF_CURRENT_ELEMENTS = jComboBox.getItemCount();
                		int SELECTED_INDEX = jComboBox.getSelectedIndex();
                		
                		// Get the selected file -
                		Vector<File> tmpVector = new Vector<File>();
                		for (int index=0;index<2;index++)
                        {
                        	
                			tmpVector.addElement(((File)jComboBox.getItemAt(index)));
                        }
                		
                		// remove all items and then add back the correct stuff -
                		jComboBox.removeAllItems();
                		
                		for (int index=0;index<2;index++)
                		{
                			jComboBox.addItem(tmpVector.get(index));
                		}
                		
                		// set the selected item -
                		jComboBox.setSelectedItem(file);
                		
                	}
                	
                	System.out.println("I'm in the leaf section of "+this.getClass().toString()+" getting ready to read the freakin combo box...");
                	
                	
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
                	for (int index=0;index<INT_SELECTED_INDEX;index++)
                	{
                		// Get the raw path -
                		String strTmpRaw = tmpVector.get(index);
                	
                		// Get the name of the selected item -
                		int INT_LAST_SLASH = strTmpRaw.lastIndexOf("/");
                    	String strTmpNew = strTmpRaw.substring(INT_LAST_SLASH+1, strTmpRaw.length());
                    	
                    	if (index==0)
                    	{
                    		// Ok, add a Dir call to the xpath string -
                        	tmpBuffer.append(strUserName);
                    		tmpBuffer.append("[@name='");
                        	tmpBuffer.append(strTmpNew);
                        	tmpBuffer.append("']");
                    	}
                    	else
                    	{
                    		// Ok, add a Dir call to the xpath string -
                        	tmpBuffer.append("Directory[@name='");
                        	tmpBuffer.append(strTmpNew);
                        	tmpBuffer.append("']");
                    	}
                    	
                    	
                    	
                    	if (index<=NUMBER_OF_ITEMS-1)
                    	{
                    		tmpBuffer.append("/");
                    	}
                    }
                
                	System.out.println("Expression in "+this.getClass().getName()+" line 200 before I add the selected dir - "+tmpBuffer.toString());
                	
                    // Ok, so always add the currently selected dir to the xpath string (the jcombo box lags)
                	tmpBuffer.append("Directory[@name='");
                	tmpBuffer.append(strFileName);
                	tmpBuffer.append("']");
                    
                    // Ok, so we need to formulate the correct xpath string -
                    String expression = tmpBuffer.toString();
                    
                    
                    
                	//String expression = "//Directory[@name='"+strFileName+"']/*";
                	
                	// What is the xpath?
                	//PublishService.submitData("What is the xpath expression [leaf node] - "+expression);
                	
                	
                	try {
        				Node dirNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        				System.out.println("Xpath expression "+expression+" returned "+dirNode+" in "+this.getClass().toString()+" class actionPerformed method.");
        				
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
                		error.printStackTrace();
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
