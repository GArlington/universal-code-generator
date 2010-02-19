/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NetworkEditorTool;
import org.varnerlab.universaleditor.gui.widgets.InfiniteProgressPanel;
import org.varnerlab.universaleditor.service.FileSystemService;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.ServerJobTypes;
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
    private Component focusedComponent = null;
    private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    final InfiniteProgressPanel glassPane = new InfiniteProgressPanel();
 
    // Create a xpFactory/xpath obj (we'll use this a zillion times -)
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();


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

    private void doFileTransfer()
    {
    	Vector<File> vecFile = new Vector();
    	StringBuffer pathBuffer = new StringBuffer();
    	String strXPath = "";
    	String strPath = "";

    		
    	// Get some stuff ...
    	UEditorSession session = (UEditorSession)_propTable.get("SESSION");
    	JList jList = (JList)_propTable.get("JLIST");
    	JComboBox jComboBox = (JComboBox)_propTable.get("LOCAL_COMBOBOX");
    	
    	// Get a ref to file transfer tool -
    	FileTransferTool _tool = (Launcher.getInstance()).getFileTransferToolRef();

    	// Get the selected file from the jList -
    	Object[] objArr = jList.getSelectedValues();
    	int NUMBER_OF_SELECTED = objArr.length;

    	System.out.println("How many files have been selected? - "+NUMBER_OF_SELECTED);

    	for (int file_index=0;file_index<NUMBER_OF_SELECTED;file_index++)
    	{

    		// Get the file -
    		File file = (File)objArr[file_index];
    		String strFile = file.getName();

    		// Ok, so let's create the path string -
    		String strSelectedRemotePath = (String)session.getProperty("SELECTED_REMOTE_PATH");
    		strPath = strSelectedRemotePath+"/"+strFile;
    		
    		// Formulate the message buffer -
    		StringBuffer buffer = new StringBuffer();
    		buffer.append("<universal>\n");
    		buffer.append("\t<property remotefilename=\"");
    		buffer.append(strPath);
    		buffer.append("\"/>\n");
    		buffer.append("</universal>\n");


    		// Go w/the launch -
    		// When I get here I have the buffer all ready to go -
    		String strIPAddress = (String)session.getProperty("SERVER_ADDRESS");
    		String strPort = (String)session.getProperty("SERVER_PORT");

    		// Send message to console -
    		PublishService.submitData("Trying to contact - "+strIPAddress+" on port "+strPort);

    		try
    		{
    			// Send that mofo -
    			String strReturnString = SocketService.sendMessage(buffer.toString(), strIPAddress, strPort, session,ServerJobTypes.TRANSFER_FILE_FROM_SERVER);
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

    				// I need to get the path -
    				String strSelectedFile = ((File)jComboBox.getSelectedItem()).getPath();

    				String strFinalPath = strSelectedFile+"/"+strFileName;

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
    	
    	
    	
    	
    	// ok, so when I get here I need to close down the glassPane -
    	glassPane.stop();
    	
    	// update the view -
    	_tool.updateSelectedDirectory();
    }
    
    public void actionPerformed(ActionEvent e) {
        // Class/instance attributes -
       
        focusedComponent = manager.getFocusOwner();
        FileTransferTool windowFrame = windowFrame = (FileTransferTool)focusedComponent.getFocusCycleRootAncestor();
        glassPane.setDoubleBuffered(true);
        int H = windowFrame.getHeight();
        int W = windowFrame.getWidth();
        Rectangle r = new Rectangle(W,H);
        glassPane.setBounds(r);
        
        windowFrame.setGlassPane(glassPane);
               
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                glassPane.start();
	            Thread performer = new Thread(new Runnable() {
	                public void run() {
	                	doFileTransfer();
	                }
	            }, "Performer");
	            performer.start();
            }
        });
        
    }

}
