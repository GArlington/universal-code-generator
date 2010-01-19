/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;
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

            if (strFileName.equalsIgnoreCase("jdv27"))
            {
                /*
            	// Get the dom tree -
                //Document doc = (Document)session.getProperty("REMOTE_FILESYSTEM_TREE");

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
                    listModel.clear();
                    tool.processNodes(doc, listModel,true);
                }*/
                
                // So if I get here I have dir that is not the root -
            	// Ok, so let's fire up the xpath -
            	doc = (Document)session.getProperty("REMOTE_FILESYSTEM_TREE");
            	XPath xpath = XPathFactory.newInstance().newXPath();
            	String expression = "*";
            	
            	// What is the xapth?
            	PublishService.submitData("What is the xpath expression [clicked on jdv27] - "+expression);
            	
            	try {
    				Node dirNode = (Node) xpath.evaluate("*", doc, XPathConstants.NODE);
    				
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
            		
            	}

            }
            else
            {
            	
            	// Ok, so we selected on a *non-root* element -

                // Check to see if this is a dir or a file -
                String strDirFlag = renderer.getDirectoryFlag(strFileName);

                // Get the dom tree -
                //Document doc = (Document)session.getProperty("REMOTE_FILESYSTEM_TREE");

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
                	
                	String expression = "//Directory[@name='"+strFileName+"']/child::*";
                	
                	// What is the xpath?
                	PublishService.submitData("What is the xpath expression [leaf node] - "+expression);
                	
                	try {
        				Node dirNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        				
        				if (dirNode!=null)
        				{
        					listModel.clear();
                            //tool.updateViewWithXPath(expression, listModel, false);
        					// tool.processNodes(dirNode, listModel, true);
        					tool.processChildNodes(strFileName, listModel, false);
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
