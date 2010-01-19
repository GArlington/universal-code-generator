/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.service.FileSystemService;
import org.varnerlab.universaleditor.service.PublishService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jeffreyvarner
 */
public class RemoteFileSystemSelectionListener implements ListSelectionListener {
    // Class/instance -
    private Hashtable _propTable = new Hashtable();

    public void setReferences(String key,Object obj)
    {
        _propTable.put(key, obj);
    }



    public void valueChanged(ListSelectionEvent e) {
        // Get the stuff from the hashtable -
        DefaultListModel listModel = (DefaultListModel)_propTable.get("LIST_MODEL");
        JList jList = (JList)_propTable.get("JLIST");
        UEditorSession session = (UEditorSession)_propTable.get("SESSION");
        VLRemoteFileSystemListCellRenderer renderer = (VLRemoteFileSystemListCellRenderer)_propTable.get("REMOTE_CELL_RENDERER");
        JComboBox jComboBox = (JComboBox)_propTable.get("REMOTE_COMBOBOX");
        String strFileSystemName = (String)_propTable.get("REMOTE_FILESYSTEM_TREE");
        VLListDoubleClickAdaptor _doubleClickAdapter = (VLListDoubleClickAdaptor)_propTable.get("MOUSE_ADAPTER");

        Document doc = null;

        // Get the selected file -
        File file = (File)jList.getSelectedValue();
        

        // Get the selected -
        //File file = (File)_doubleClickAdapter.getProperty("SELECTED_NODE");

        if (file!=null)
        {

            String strFileName = file.getName();

            // Check to see if this is a dir or a file -
            String strDirFlag = renderer.getDirectoryFlag(strFileName);

            // Ok, let's get the remote filesystem tree from the session object -
            if (strFileSystemName!=null)
            {
                doc = (Document)session.getProperty(strFileSystemName);
            }
            else
            {
                doc = (Document)session.getProperty("REMOTE_FILESYSTEM_TREE");
            }

            // Ok, get the root node -
            Node rootNode = doc.getDocumentElement();

            // Ok, I have the root -- I need to figure out which file has been selected - if it is not a dir, then do nothing for the moment -
            if (strDirFlag.equalsIgnoreCase("DIRECTORY"))
            {
                // Ok, I need to populate the listModel w/the new info -

                // First, clear it out -
                listModel.clear();




                // Get all the dirs -
                NodeList list = doc.getElementsByTagName("Directory");

                Boolean blnFlag = true;
                int counter = 0;
                while (blnFlag)
                {
                    // Get the node -
                    Node tmpNode = list.item(counter);


                    if (tmpNode!=null)
                    {
                        // Check to see if we have the correct node -
                        NamedNodeMap nodeAttributes = tmpNode.getAttributes();
                        String strTmp = nodeAttributes.getNamedItem("name").getNodeValue();

                        if (strTmp.equalsIgnoreCase(strFileName))
                        {
                            // Set the flag so that I don't go around again -
                            blnFlag = false;

                            // Update the combo box -
                            jComboBox.addItem(new File(strTmp));

                            
                            // Get the kids and add them to the listModel -
                            NodeList nodeList = tmpNode.getChildNodes();
                            int NUMBER = nodeList.getLength();
                            for (int index=0;index<NUMBER;index++)
                            {
                                Node childNode = nodeList.item(index);
                                String strNodeName = childNode.getNodeName();

                                NamedNodeMap childAttributes = childNode.getAttributes();
                                String strChildTmp = childAttributes.getNamedItem("name").getNodeValue();

                                // Reset the dir flag just in case it has not been set -
                                if (strNodeName.equalsIgnoreCase("DIRECTORY"))
                                {
                                    renderer.setDirectoryFlag(strChildTmp,"DIRECTORY");
                                    renderer.setBackground(Color.BLUE);
                                }
                                else
                                {
                                    renderer.setDirectoryFlag(strChildTmp,"FILE");
                                }

                                // Create a file -
                                PublishService.submitData("I'm looking at what node - "+childNode);
                                File tmpFile = new File(strChildTmp);
                                listModel.addElement(tmpFile);
                            }

                            // Update the listModel -
                            jList.setModel(listModel);
                        }
                        else
                        {
                            counter++;
                        }
                    }
                    else
                    {
                        counter++;
                        blnFlag = false;
                    }
                }
            }
            else
            {
                PublishService.submitData("I'm clicking on what - "+strFileName);
            }
        }       
    }
}
