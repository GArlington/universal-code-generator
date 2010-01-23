/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FileTransferTool.java
 *
 * Created on Mar 29, 2009, 6:27:04 PM
 */

package org.varnerlab.universaleditor.gui;

// import -
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.service.FileSystemService;
import org.varnerlab.universaleditor.service.IVLSystemwideEventListener;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.ServerJobTypes;
import org.varnerlab.universaleditor.service.SocketService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.actions.FileTransferJPopupMenuActionListener;
import org.varnerlab.universaleditor.gui.actions.GetFileFromServerAction;
import org.varnerlab.universaleditor.gui.actions.LoginToolAction;
import org.varnerlab.universaleditor.gui.widgets.BioChemExpToolFocusListener;
import org.varnerlab.universaleditor.gui.widgets.FileTransferJPopupMenuMouseAdapter;
import org.varnerlab.universaleditor.gui.widgets.IVLProcessTreeNode;
import org.varnerlab.universaleditor.gui.widgets.TransferToolFocusListener;
import org.varnerlab.universaleditor.gui.widgets.VLFileSystemListCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.VLRemoteComboBoxSelectionListener;
import org.varnerlab.universaleditor.gui.widgets.VLRemoteFileSystemListCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.XPathRemoteFileSystemSelectionListener;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jeffreyvarner
 */
public class FileTransferTool extends javax.swing.JInternalFrame implements ActionListener,ListSelectionListener,IVLSystemwideEventListener, IVLProcessTreeNode {
    
    // Class instance attributes -
    private static int openFrameCount=1;
    private static final int xOffset=50;
    private static final int yOffset=50;

    private Vector<File> _vecParents = new Vector();
    private Vector<File> _vecDir = new Vector();
    private DefaultListModel _listModelJList1 = new DefaultListModel();
    private DefaultListModel _listModelJList2 = new DefaultListModel();
    private UEditorSession _session = null;
    
    // popup menu -
    private JPopupMenu popup = null;
    private FileTransferJPopupMenuActionListener popupMenuListener = null;
    private FileTransferJPopupMenuMouseAdapter popupMouseAdapter = null;
    private LoginToolAction _loginTool = null;

    private ImageIcon _imgIconOff = null;
    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconCurrent = null;
    private VLRemoteFileSystemListCellRenderer remoteRender = new VLRemoteFileSystemListCellRenderer();
    private XPathRemoteFileSystemSelectionListener _remoteListener = new XPathRemoteFileSystemSelectionListener();
    private VLRemoteComboBoxSelectionListener _remoteJComboListener = new VLRemoteComboBoxSelectionListener();
    private TransferToolFocusListener _focusListener =  new TransferToolFocusListener();
    
    // Create a xpFactory/xpath obj (we'll use this a zillion times -)
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();

    public void setOffIcon(ImageIcon imgIcon)
    {
        _imgIconOff = imgIcon;
    }

    public void setOnIcon(ImageIcon imgIcon)
    {
        _imgIconOn = imgIcon;
    }

    public ImageIcon getOffIcon()
    {
        return(_imgIconOff);
    }

    public ImageIcon getOnIcon()
    {
        return(_imgIconOn);
    }

    
    private void configureJPopupMenu() 
    {
    	try
    	{
    		// Setup the menu item -
    		
    		popup = new JPopupMenu();
    		
    		// Setup the menu listener -
    		popupMenuListener = new FileTransferJPopupMenuActionListener();
    		popupMouseAdapter = new FileTransferJPopupMenuMouseAdapter();
    		_loginTool = new LoginToolAction();
    		
    		// Set the reference to the combo box -
    		popupMenuListener.setJComboBoxReference(jComboBox1);
    		
    		// Setup the new folder -
    		JMenuItem item = new JMenuItem("New local folder ... ",VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));
    	    item.setHorizontalTextPosition(JMenuItem.RIGHT);
    	    item.addActionListener(popupMenuListener);
    	    popup.add(item);
    	    
    	    // Setup the new project menu action -
    	    JMenuItem item_new_project = new JMenuItem("New project ... ",VLIconManagerService.getIcon("PROJECT-ICON"));
    	    item_new_project.setHorizontalTextPosition(JMenuItem.RIGHT);
    	    item_new_project.addActionListener(_loginTool);
    	    popup.add(item_new_project);
    	    
    	    // Ok, configure the mouse adapter -
    	    popupMouseAdapter.setJPopupReference(popup);
    	    popupMouseAdapter.setToolReference(this);
    	    
    	    this.addMouseListener(popupMouseAdapter);    
    	}
    	catch (Exception error)
    	{
    		error.printStackTrace();
    	}
    }

    /** Creates new form FileTransferTool */
    public FileTransferTool() {

        // iterate window count
        ++openFrameCount;

        // Set the windows location
        setLocation(xOffset*openFrameCount,yOffset*openFrameCount);

        // Set TitleBar color when active/inactive
        setDoubleBuffered(true);

        // Get the session object -
        _session = (Launcher.getInstance()).getSession();
        
        // set the session pointer -
        remoteRender.setSession(_session);

        // load the components -
        initComponents();

        // Get the parents of my home directory -
        File userHome = new File(Launcher._CURRENT);
        FileSystemService.traverseUp(userHome, _vecParents);

        // Populate the ComboBox -
        int NUMBER_OF_PARENTS = _vecParents.size();
        for (int pindex=0;pindex<NUMBER_OF_PARENTS;pindex++)
        {
            jComboBox1.addItem(_vecParents.get(pindex));
        }

        // Setup the combobox -
        jComboBox1.addActionListener(this);
        jComboBox2.addActionListener(_remoteJComboListener);


        // Populate the JList w/the current directory -
        FileSystemService.getFileFromDir(userHome, _vecDir);
        int NUMBER_OF_ELEMENTS = _vecDir.size();
        for (int pindex=0;pindex<NUMBER_OF_ELEMENTS;pindex++)
        {
            _listModelJList1.addElement(_vecDir.get(pindex));
        }

        jList1.setModel(_listModelJList1);
        jList2.setModel(_listModelJList2);

        // Set the listener -
        jList1.addListSelectionListener(this);
        jList2.addListSelectionListener(_remoteListener);
        
        
        // add this to the Systemwide -
        SystemwideEventService.registerUsernameListener(this);
        SystemwideEventService.registerSessionListener(this);

        // Add some custom render -
        jComboBox1.setRenderer((new VLFileSystemListCellRenderer()));
        jList1.setCellRenderer((new VLFileSystemListCellRenderer()));
        jList2.setCellRenderer(remoteRender);
        jComboBox2.setRenderer(remoteRender);

        //jList1.putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,Boolean.TRUE );
        //jList2.putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,Boolean.TRUE );
        //jComboBox1.putClientProperty(com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,Boolean.TRUE );

        // Add a focus/click listerner -
        _focusListener.setProperty("LOCAL_JLIST_MODEL", _listModelJList1);
        _focusListener.setProperty("LOCAL_COMBOBOX", jComboBox1);
        this.addInternalFrameListener(_focusListener);

        // Setup the remote file system list selection -
        _remoteListener.setReferences("JLIST", jList2);
        _remoteListener.setReferences("LIST_MODEL",_listModelJList2);
        _remoteListener.setReferences("SESSION",_session);
        _remoteListener.setReferences("REMOTE_CELL_RENDERER",remoteRender);
        _remoteListener.setReferences("REMOTE_COMBOBOX",jComboBox2);
        _remoteListener.setReferences("REMOTE_FILESYSTEM_TREE","REMOTE_FILESYSTEM_TREE");

        // Setup the remote file system list selection -
        _remoteJComboListener.setReferences("JLIST", jList2);
        _remoteJComboListener.setReferences("LIST_MODEL",_listModelJList2);
        _remoteJComboListener.setReferences("SESSION",_session);
        _remoteJComboListener.setReferences("REMOTE_CELL_RENDERER",remoteRender);
        _remoteJComboListener.setReferences("REMOTE_COMBOBOX",jComboBox2);
        _remoteJComboListener.setReferences("FILE_TRANSFER_TOOL",this);
        
        // configure the popup menu -
        configureJPopupMenu();
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
 // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jComboBox1 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jButton2 = new javax.swing.JButton();
        jComboBox2 = new javax.swing.JComboBox();

        setClosable(true);
        setIconifiable(true);
        setTitle("Universal file transfer tool");

        jScrollPane1.setViewportView(jList1);

        jButton1.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/TransferFile-20-Grey.png")); // NOI18N
        jButton1.setToolTipText("Transfer files to the server");
        jButton1.setBorderPainted(false);
        jButton1.setDoubleBuffered(true);
        jButton1.setEnabled(false);
        jButton1.setRolloverEnabled(true);
        jButton1.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/TransferFile-20.png")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendFileToServer(evt);
            }
        });

        jList2.setDoubleBuffered(true);
        jScrollPane2.setViewportView(jList2);

        jButton2.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/TransferFileBack-20-Grey.png")); // NOI18N
        jButton2.setToolTipText("Transfer files to the server");
        jButton2.setBorderPainted(false);
        jButton2.setEnabled(false);
        jButton2.setRolloverEnabled(true);
        jButton2.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/TransferFileBack-20.png")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getFileFromServer(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(31, 31, 31)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBox1, 0, 297, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jComboBox2, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE))
                .add(33, 33, 33))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(136, 136, 136)
                        .add(jButton1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton2))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(37, 37, 37)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))
                        .add(36, 36, 36)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>                  

    private void sendFileToServer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendFileToServer
        // When I get here I have a file that I want to send to the server -
        Document document = null;
        StringBuffer buffer = new StringBuffer();
        
        // Get the file -
        Object[] objArr = jList1.getSelectedValues();
        
        // Let everyone else know that I have updated session -
        SystemwideEventService.fireSessionUpdateEvent();
        
        // Get the number of selected files -
        int NUMBER_OF_FILES = objArr.length;
        for (int index=0;index<NUMBER_OF_FILES;index++)
        {
        	// Get the file -
        	File file = (File)objArr[index];
        	
        	// Ok, so I need to update the session with the file that is going to be sent -
            _session.setProperty("FILENAME",file.getName());
        	
        	// Get the file and send to the server -
        	// Wrap the message in universal tags -
            buffer.append("<universal>");
            buffer.append("\n");
            
            // If the file is not a dir then spank it ...
            if (!file.isDirectory())
            {
                try
                {
                    // Load the file -
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String s="";
                    while ((s=reader.readLine())!=null)
                    {
                        buffer.append(s);
                        buffer.append("\n");
                    }
                }
                catch (Exception ex) {
                    PublishService.submitData("Spank me - some type of error "+ex);
                }
            }

            buffer.append("</universal>");
            buffer.append("\n");
            
            // When I get here I have the buffer all ready to go -
            String strIPAddress = (String)_session.getProperty("SERVER_ADDRESS");
            String strPort = (String)_session.getProperty("SERVER_PORT");

            // Send message to console -
            PublishService.submitData("Trying to contact - "+strIPAddress+" on port "+strPort);
            try 
            {
                // Send that mofo -
                String strReturnString = SocketService.sendMessage(buffer.toString(), strIPAddress, strPort, _session,ServerJobTypes.TRANSFER_FILE_TO_SERVER);
                PublishService.submitData("Rcvd - "+strReturnString);

                // Ok, check to see if return string is null -
                if (strReturnString!=null)
                {
                    // Clear out the list model -
                    _listModelJList2.clear();

                    // If I get here that I should have a list of the files on the server -
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();

                    // Ok, so we have a document -
                    document = builder.parse(new InputSource(new StringReader(strReturnString)));
                    processNodes(document,_listModelJList2,false);

                    // Add model to jList2 -
                    jList2.setModel(_listModelJList2);

                    // Set the tree in session -
                    _session.setProperty("REMOTE_FILESYSTEM_TREE", document);
                }
            }
            catch (Exception ex)
            {
                PublishService.submitData("Spank me - some type of error "+ex);
                ex.printStackTrace();
            }
            
            // clear out the buffer and go around again -
            buffer.delete(0, buffer.length());         
        }
    }//GEN-LAST:event_sendFileToServer

    private void getFileFromServer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getFileFromServer
        // Method attributes -
        GetFileFromServerAction getFile = new GetFileFromServerAction();
        Vector<File> vecFile = new Vector();

        // Configure -
        getFile.setReferences("SESSION",_session);
        getFile.setReferences("LOCAL_COMBOBOX",jComboBox1);
        getFile.setReferences("JLIST", jList2);

        // Fire the event -
        getFile.actionPerformed(evt);

        // Ok, this should update the local dir -
        String strSelectedFileLocal = ((File)jComboBox1.getSelectedItem()).getPath();

        PublishService.submitData("What the heck - local file = "+strSelectedFileLocal);

        
        // Clear out the list model -
        _listModelJList1.clear();
        File userHome = new File(strSelectedFileLocal);
        FileSystemService.getFileFromDir(userHome, vecFile);
        int NUMBER_OF_ELEMENTS = vecFile.size();
        for (int pindex=0;pindex<NUMBER_OF_ELEMENTS;pindex++)
        {
            _listModelJList1.addElement(vecFile.get(pindex));
        }        

}//GEN-LAST:event_getFileFromServer


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    
    public void updateViewWithXPath(String strXPath,DefaultListModel model,boolean blnUpdateComboBox) throws Exception
    {
    	model.clear();
    	String strTmp = "";
    	File fileOnServer = null;
    	
    	Document doc = (Document)_session.getProperty("REMOTE_FILESYSTEM_TREE");
    	XPathExpression expr = _xpath.compile(strXPath);
  	  	NodeList dirNodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
  	  	
    	// Ok, so we have the dir nodes -
  	  	int NUMBER_OF_DIRS = dirNodes.getLength();
  	  	for (int index = 0;index<NUMBER_OF_DIRS;index++)
  	  	{
  	  		// Get the node -
  	  		Node tmpNode = (Node)dirNodes.item(index);
  	  		
  	  		// Get the name of this dir - (we'll update it in the renderer)
  	  		strTmp = tmpNode.getNodeValue();
  	  		
  	  		// Get the name -
  	  		fileOnServer = new File(strTmp);
  	  		remoteRender.setDirectoryFlag(fileOnServer.getName(), "DIRECTORY");
  	  		model.addElement(fileOnServer);
  	  		
  	  		// Grab the string -
  	  		System.out.println(tmpNode.getNodeValue());
  	  	}
    }
    
    public void processNodesWithXPath(Node node,DefaultListModel model,boolean blnUpdateComboBox) throws Exception
    {
    	// Method attributes -
    	String strXPathDir = "";
    	File fileOnServer = null;
    	String strTmp = "";
    	
    	if (blnUpdateComboBox)
    	{
    		// update the combox box -
    		jComboBox2.removeAllItems();
    		File mainFile = new File("jdv27");
    		remoteRender.setDirectoryFlag("jdv27","DIRECTORY");
    		jComboBox2.addItem(mainFile);
    	}

        // clearout the list model -
        model.clear();
    	
    	// Ok, let's get the directories that are children of the current node -
    	
    	// where am I?
    	System.out.println("Starting XPath Query...");
    	
    	// Formulate the XPath string for directories under the current node -
    	strXPathDir = "/jdv27/Directory/@name";
    	
    	XPathExpression expr = _xpath.compile(strXPathDir);
  	  	NodeList dirNodes = (NodeList)expr.evaluate(node, XPathConstants.NODESET);
  	  	
    	// Ok, so we have the dir nodes -
  	  	int NUMBER_OF_DIRS = dirNodes.getLength();
  	  	for (int index = 0;index<NUMBER_OF_DIRS;index++)
  	  	{
  	  		// Get the node -
  	  		Node tmpNode = (Node)dirNodes.item(index);
  	  		
  	  		// Get the name of this dir - (we'll update it in the renderer)
  	  		strTmp = tmpNode.getNodeValue();
  	  		
  	  		// Get the name -
  	  		fileOnServer = new File(strTmp);
  	  		remoteRender.setDirectoryFlag(fileOnServer.getName(), "DIRECTORY");
  	  		model.addElement(fileOnServer);
  	  		
  	  		// Grab the string -
  	  		PublishService.submitData(tmpNode.getNodeValue());
  	  	}
  	  	
  	  	// Formulate the XPath string for directories under the current node -
    	strXPathDir = "/jdv27/File/@name";
    	
    	XPathExpression file_expr = _xpath.compile(strXPathDir);
  	  	NodeList fileNodes = (NodeList)file_expr.evaluate(node, XPathConstants.NODESET);
  	  	
  	  	// Ok, so we have the dir nodes -
  	  	int NUMBER_OF_FILES = fileNodes.getLength();
  	  	for (int index = 0;index<NUMBER_OF_FILES;index++)
  	  	{
  	  		// Get the node -
  	  		Node tmpNode = (Node)fileNodes.item(index);
  	  		
  	  		// Get the name of this file - (we'll update the name in the renderer)
  	  		strTmp = tmpNode.getNodeValue();
  	  		
  	  		// Get the name -
  	  		fileOnServer = new File(strTmp);
  	  		remoteRender.setDirectoryFlag(fileOnServer.getName(), "FILE");
  	  		model.addElement(fileOnServer);
  	  		
  	  		// Grab the string -
  	  		// System.out.println(tmpNode.getNodeValue());
  	  	}
  	  	
    }

    public void processNodes(Node node,DefaultListModel model,boolean blnUpdateComboBox)
    {
    	
    	try {
    		// this is for debugging -
    		processNodesWithXPath(node,model,blnUpdateComboBox);
    	}
    	catch (Exception error)
    	{
    		System.out.println("ERROR WITH XPATH - "+error);
    	}
    }

    

    // Updates the jList when a dir was selected -
    public void actionPerformed(ActionEvent e) {
        // Get the combox box -
        JComboBox cb = (JComboBox)e.getSource();

        // Clear out the dir -
        _vecDir.clear();
        _listModelJList1.clear();

        // Repopulate the dir -
        FileSystemService.getFileFromDir((File)cb.getSelectedItem(), _vecDir);
        int NUMBER_OF_ELEMENTS = _vecDir.size();
        for (int pindex=0;pindex<NUMBER_OF_ELEMENTS;pindex++)
        {
            _listModelJList1.addElement(_vecDir.get(pindex));
        }

        jList1.setModel(_listModelJList1);

    }

    // Updates the jComboBox and jList -
    public void valueChanged(ListSelectionEvent e)
    {
        
        // Get the index of the selected item -
        File file = (File)jList1.getSelectedValue();
     
        if (file!=null && file.isDirectory())
        {
            // Clear out the dir -
            _vecDir.clear();
            _listModelJList1.clear();

            // Repopulate the dir -
            FileSystemService.getFileFromDir(file, _vecDir);
            int NUMBER_OF_ELEMENTS = _vecDir.size();
            for (int pindex=0;pindex<NUMBER_OF_ELEMENTS;pindex++)
            {
                _listModelJList1.addElement(_vecDir.get(pindex));
            }

            // Update the listModel -
            jList1.setModel(_listModelJList1);

            // I need to check to see of the item is already in the combobox -
            Vector<String> tmpVector = new Vector<String>();
            int NUMBER_OF_ITEMS = jComboBox1.getItemCount();
            for (int index=0;index<NUMBER_OF_ITEMS;index++)
            {
            	tmpVector.addElement(((File)jComboBox1.getItemAt(index)).getAbsolutePath());
            }
            
            // Ok, so I've put the items into the vector - let's use the contains option
            String strPath = file.getAbsolutePath();
            if (!tmpVector.contains(strPath))
            {
            	// Add the dir to drop down -
                jComboBox1.addItem(file);
                jComboBox1.setSelectedItem(file);
            }
            else
            {
            	// Ok, so we already have this item just set the selected item -
            	jComboBox1.setSelectedItem(file);
            }
        }
   }

    public void updateComponent() {
        // Update the jButtons -


        // Look at session and see if there is a validated username -
        String strUserName = (String)_session.getProperty("VALIDATED_USERNAME");
        if (strUserName!=null)
        {
            // Enable components -
            jButton1.setEnabled(true);
            jButton2.setEnabled(true);
        }

    }

    // Load the projects tree from the server -
    private void populateProjectList()
    {
        // When I get here I have the buffer all ready to go -
        // Get the address and the port name of the server -
        String strIPAddress = (String)_session.getProperty("SERVER_ADDRESS");
        String strPort = (String)_session.getProperty("SERVER_PORT");
        Vector<String> vecTmp = new Vector<String>();
        Document document = null;
        File tmpFile = null;


        // Formulate a dumb message --
        StringBuffer strBuffer = new StringBuffer();

         // Get the working dir -
        String strUserName = (String)_session.getProperty("USERNAME");
        //String strSessionID = (String)_session.getProperty("SESSIONID");

        // Start the buffer -
        strBuffer.append("<?xml version=\"1.0\"?>\n");
        strBuffer.append("<universal>\n");

        // Set the username -
        strBuffer.append("\t<property username=\"");
        strBuffer.append(strUserName);
        strBuffer.append("\"/>\n");

        // Set the sessionid -
        //strBuffer.append("\t<property sessionid=\"");
        //strBuffer.append(strSessionID);
        //strBuffer.append("\"/>\n");

        strBuffer.append("</universal>\n");

        try
        {
            // Send that mofo -
            String strReturnString = SocketService.sendMessage(strBuffer.toString(), strIPAddress, strPort, _session,ServerJobTypes.PROJECT_DIRECTORY_LOOKUP);
            PublishService.submitData("Rcvd - "+strReturnString);

             // Ok, check to see if return string is null -
            if (strReturnString!=null && !strReturnString.isEmpty())
            {
                // Clear out the list model -
                _listModelJList2.clear();

                // If I get here that I should have a list of the files on the server -
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                // Ok, so we have a document -
                document = builder.parse(new InputSource(new StringReader(strReturnString)));
                processNodes(document.getFirstChild(),_listModelJList2,true);

                // Add model to jList2 -
                jList2.setModel(_listModelJList2);

                // Set the tree in session -
                _session.setProperty("REMOTE_FILESYSTEM_TREE", document);
                SystemwideEventService.fireSessionUpdateEvent();
            }
        }
        catch (Exception error)
        {
            PublishService.submitData("Spank me - some type of error "+error);
            error.printStackTrace();
        }

    }

    public void launchProjectTool()
    {
        // Method attributes -

        // Populate the projectTree -
        this.populateProjectList();

        // Need to make sur we are pointed to the correct tree in memory -
         //_remoteListener.setReferences("REMOTE_FILESYSTEM_TREE","PROJECT_FILESYSTEM_TREE");

        // Update the jlist display -
        // Ok, so let's get the doc out of session -
        Document document = (Document)_session.getProperty("REMOTE_FILESYSTEM_TREE");
        if (document!=null)
        {
            _listModelJList2.clear();
            this.processNodes(document.getFirstChild(), _listModelJList2,true);
        }
    }



    public void updateSession() {
        // Get the new session -
        _session = (Launcher.getInstance()).getSession();

        // Look at session and see if there is a validated username -
        String strUserName = (String)_session.getProperty("VALIDATED_USERNAME");

        PublishService.submitData("FTT has username - "+strUserName);

        if (strUserName!=null)
        {
            // Enable components -
            jButton1.setEnabled(true);
            jButton2.setEnabled(true);
        }
        
        // Ok, so let's get the doc out of session -
        Document document = (Document)_session.getProperty("REMOTE_FILESYSTEM_TREE");
        if (document!=null)
        {
            _listModelJList2.clear();
            this.processNodes(document.getFirstChild(), _listModelJList2,false);
        }
        
        // Ok, so I need to update the local directory structure -
        String strNewLocalDirName = (String)_session.getProperty("NEW_LOCAL_DIRECTORY_PATH");
        if (strNewLocalDirName!=null)
        {
        	_listModelJList1.addElement(new File(strNewLocalDirName));
        }
    }

    public void updateNetwork() {
    }

    
	public void processChildNodes(String strPath, DefaultListModel model, boolean blnUpdate) {
		// Method attributes -
    	String strXPathDir = "";
    	File fileOnServer = null;
    	String strTmp = "";
    	
    	if (blnUpdate)
    	{
    		// update the combox box -
    		jComboBox2.removeAllItems();
    		File mainFile = new File("jdv27");
    		remoteRender.setDirectoryFlag("jdv27","DIRECTORY");
    		jComboBox2.addItem(mainFile);
    	}

        // clearout the list model -
        model.clear();
    	
        try {
	        // Ok, so let's get the doc out of session -
	        Document document = (Document)_session.getProperty("REMOTE_FILESYSTEM_TREE");
	        if (document!=null)
	        {
	        	
	        	strXPathDir = "//Directory[@name='"+strPath+"']/@name";
	        	XPathExpression expr = _xpath.compile(strXPathDir);
	      	  	NodeList dirNodes = (NodeList)expr.evaluate(document, XPathConstants.NODESET);
	      	  	
	        	// Ok, so we have the dir nodes -
	      	  	int NUMBER_OF_DIRS = dirNodes.getLength();
	      	  	for (int index = 0;index<NUMBER_OF_DIRS;index++)
	      	  	{
	      	  		// Get the node -
	      	  		Node tmpNode = (Node)dirNodes.item(index);
	      	  		
	      	  		// Get the name of this dir - (we'll update it in the renderer)
	      	  		strTmp = tmpNode.getNodeValue();
	      	  		
	      	  		// Get the name -
	      	  		fileOnServer = new File(strTmp);
	      	  		remoteRender.setDirectoryFlag(fileOnServer.getName(), "DIRECTORY");
	      	  		model.addElement(fileOnServer);
	      	  		
	      	  		// Grab the string -
	      	  		PublishService.submitData(tmpNode.getNodeValue());
	      	  	}
	      	  	
	      	  	// Formulate the XPath string for directories under the current node -
	        	strXPathDir = "//File[@name='"+strPath+"']/@name";
	        	
	        	XPathExpression file_expr = _xpath.compile(strXPathDir);
	      	  	NodeList fileNodes = (NodeList)file_expr.evaluate(document, XPathConstants.NODESET);
	      	  	
	      	  	// Ok, so we have the dir nodes -
	      	  	int NUMBER_OF_FILES = fileNodes.getLength();
	      	  	for (int index = 0;index<NUMBER_OF_FILES;index++)
	      	  	{
	      	  		// Get the node -
	      	  		Node tmpNode = (Node)fileNodes.item(index);
	      	  		
	      	  		// Get the name of this file - (we'll update the name in the renderer)
	      	  		strTmp = tmpNode.getNodeValue();
	      	  		
	      	  		// Get the name -
	      	  		fileOnServer = new File(strTmp);
	      	  		remoteRender.setDirectoryFlag(fileOnServer.getName(), "FILE");
	      	  		model.addElement(fileOnServer);
	      	  		
	      	  		// Grab the string -
	      	  		// System.out.println(tmpNode.getNodeValue());
	      	  	}
	        }
		}
    	catch (Exception error)
    	{
    		error.printStackTrace();
    	}
	}
}
