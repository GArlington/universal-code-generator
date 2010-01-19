/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.service.FileSystemService;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SocketService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author jeffreyvarner
 */
public class GetFileFromServerAction implements ActionListener {
    // Method attributes -
    private Hashtable _propTable = new Hashtable();


    public void setReferences(Object key,Object val)
    {
        _propTable.put(key, val);
    }

   
    private Node getSelectedNode(String fileName,Document doc)
    {
        // Method attributes -
        Node rNode = null;
        String tString = "";

        // Ok, so I need to find the node w/the filename equal to the string passed in -
        NodeList nodeList = doc.getElementsByTagName("File");
        int NUMBER = nodeList.getLength();
        for (int index=0;index<NUMBER;index++)
        {
            // So when I get here I'm going through the nodes and looking for the one w/fileName -
            Node testNode = nodeList.item(index);
            NamedNodeMap map = testNode.getAttributes();

            // Get the test string -
            tString = map.getNamedItem("name").getNodeValue();

            // Check to see if the strings match -
            if (fileName.equalsIgnoreCase(tString))
            {
                rNode = testNode;
                break;
            }
        }

        // return -
        return(rNode);
    }

    public void actionPerformed(ActionEvent e) {
        // Class/instance attributes -
        Vector<File> vecFile = new Vector();
        StringBuffer pathBuffer = new StringBuffer();
        
        // Get some stuff ...
        UEditorSession session = (UEditorSession)_propTable.get("SESSION");
        JList jList = (JList)_propTable.get("JLIST");
        JComboBox jComboBox = (JComboBox)_propTable.get("LOCAL_COMBOBOX");

        
        // Get the selected file from the jList -
        Object[] objArr = jList.getSelectedValues();
        int NUMBER_OF_SELECTED = objArr.length;

        System.out.println("How many files have been selected? - "+NUMBER_OF_SELECTED);

        for (int file_index=0;file_index<NUMBER_OF_SELECTED;file_index++)
        {

            // Get the file -
            File file = (File)objArr[file_index];
            String strFile = file.getName();

            // Get the string of which tree I need to be operating on?
            Document remoteTree = (Document)session.getProperty("REMOTE_FILESYSTEM_TREE");

            // Let's get the correct node in the tree -
            Node treeNode = this.getSelectedNode(strFile, remoteTree);

            // Ok, I have the treeNode - get the parents -
            FileSystemService.traverseUpDOMTree(treeNode, vecFile);

            // Ok, finally - I have the path to the file -- create the path string -
            int NUM_PARENTS = vecFile.size();
            for (int counter=0;counter<NUM_PARENTS;counter++)
            {
                // Get path info from the file -
                File fileTmp = vecFile.get(counter);
                pathBuffer.append(fileTmp.getName());
                pathBuffer.append("/");
            }

            // Add the file to the ned -
            pathBuffer.append(strFile);

            // Formulate the message buffer -
            StringBuffer buffer = new StringBuffer();
            buffer.append("<universal>\n");
            buffer.append("\t<property remotefilename=\"");
            buffer.append(pathBuffer.toString());
            buffer.append("\"/>\n");
            buffer.append("</universal>\n");


            // Go w/the launch -
            // When I get here I have the buffer all ready to go -
            String strIPAddress = (String)session.getProperty("SERVER_ADDRESS");
            String strPort = (String)session.getProperty("RETURN_FILETRANSFER_SERVER_PORT");

            // Send message to console -
            PublishService.submitData("Trying to contact - "+strIPAddress+" on port "+strPort);

            try
            {
                // Send that mofo -
                String strReturnString = SocketService.sendMessage(buffer.toString(), strIPAddress, strPort, session);
                PublishService.submitData("Rcvd - "+strReturnString);

                // return string holds the contents of the file. I need to make a DOM tree and get the data -
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                // Ok, so we have a document -
                Document document = builder.parse(new InputSource(new StringReader(strReturnString)));

                // Get the root node -
                NodeList dataNodes = document.getElementsByTagName("Data");

                // Get the number of files that I have pack'd up -
                int NUMBER_OF_FILES = dataNodes.getLength();
                for (int index=0;index<NUMBER_OF_FILES;index++)
                {

                    // Get the kid from the root -
                    Node payloadNode = dataNodes.item(index);

                    // Ok, let's get the attributes -
                    NamedNodeMap attributes = payloadNode.getAttributes();

                    // Get the value of the filename -
                    String strFileName = attributes.getNamedItem("name").getNodeValue();

                    // Get the payload -
                    Element ele = (Element)payloadNode;
                    String strPayload = ele.getTextContent();

                    // Dump the payload to disk

                    // I need to get the path -
                    String strSelectedFile = ((File)jComboBox.getSelectedItem()).getPath();

                    String strFinalPath = strSelectedFile+"/"+strFileName;

                    //PublishService.submitData("Filename to save - "+strFinalPath);
                    //PublishService.submitData("Byffer - "+strPayload);


                    // Create writer
                    File oFile=new File(strFinalPath);
                    BufferedWriter writer=new BufferedWriter(new FileWriter(oFile));

                    // Write buffer to file system and close writer
                    writer.write(strPayload);
                    writer.close();
                }


            }
            catch (Exception error)
            {
                PublishService.submitData("ERROR: Sending file to server - "+error);
            }

            // reset the buffers =
            pathBuffer.delete(0, pathBuffer.length());
            buffer.delete(0, buffer.length());
            vecFile.clear();
        }
    }

}
