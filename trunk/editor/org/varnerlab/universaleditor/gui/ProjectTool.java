/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ProjectTool.java
 *
 * Created on May 7, 2009, 4:35:36 PM
 */

package org.varnerlab.universaleditor.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.varnerlab.universaleditor.database.DatabaseAPI;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.widgets.IVLProcessTreeNode;
import org.varnerlab.universaleditor.gui.widgets.ProjectToolFocusListener;
import org.varnerlab.universaleditor.gui.widgets.RemoteFileSystemSelectionListener;
import org.varnerlab.universaleditor.gui.widgets.VLFileSystemListCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.VLListDoubleClickAdaptor;
import org.varnerlab.universaleditor.gui.widgets.VLRemoteComboBoxSelectionListener;
import org.varnerlab.universaleditor.gui.widgets.VLRemoteFileSystemListCellRenderer;
import org.varnerlab.universaleditor.service.FileSystemService;
import org.varnerlab.universaleditor.service.IVLSystemwideEventListener;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SocketService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author jeffreyvarner
 */
public class ProjectTool extends javax.swing.JInternalFrame implements ListSelectionListener,IVLSystemwideEventListener,IVLProcessTreeNode {
    // Class/instance attributes -
    static int openFrameCount=1;
    static final int xOffset=50;
    static final int yOffset=50;

    private ImageIcon _imgIconOff = null;
    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconCurrent = null;

    // Define the project list model -
    
    private VLRemoteFileSystemListCellRenderer _remoteRender = new VLRemoteFileSystemListCellRenderer();
    private RemoteFileSystemSelectionListener _remoteListener = new RemoteFileSystemSelectionListener();
    private VLRemoteComboBoxSelectionListener _remoteJComboListener = new VLRemoteComboBoxSelectionListener();
    private VLListDoubleClickAdaptor _doubleClickAdapter = new VLListDoubleClickAdaptor();
    private DefaultListModel _listModelProjectModel = new DefaultListModel();

    private UEditorSession _session = null;
    private Vector<File> _vecDir = new Vector();

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


    /** Creates new form ProjectTool */
    public ProjectTool() {

         // iterate window count
        ++openFrameCount;

        // Set the windows location
        setLocation(xOffset*openFrameCount,yOffset*openFrameCount);

        // Set TitleBar color when active/inactive
        setDoubleBuffered(true);

        // initialize all -
        initComponents();

        // Ok, get the session -
        _session = (Launcher.getInstance()).getSession();

        // Add a focus listner to shift the icon -
        this.addInternalFrameListener(new ProjectToolFocusListener());

         // Ok, we need to populate this bitch -
        populateProjectList();

        // Setup the list and the selection mumbo jumbo -
        jList1.setCellRenderer(_remoteRender);
        jList1.addMouseListener(_doubleClickAdapter);

        // Add a change listener -
        jList1.addListSelectionListener(_remoteListener);
        jComboBox1.addActionListener(_remoteJComboListener);
        jComboBox1.setRenderer(_remoteRender);
        
        // Setup the remote file system list selection -
        _remoteListener.setReferences("JLIST", jList1);
        _remoteListener.setReferences("LIST_MODEL",_listModelProjectModel);
        _remoteListener.setReferences("SESSION",_session);
        _remoteListener.setReferences("REMOTE_CELL_RENDERER",_remoteRender);
        _remoteListener.setReferences("REMOTE_COMBOBOX",jComboBox1);
        _remoteListener.setReferences("REMOTE_FILESYSTEM_TREE","PROJECT_FILESYSTEM_TREE");
        _remoteListener.setReferences("MOUSE_ADAPTER", _doubleClickAdapter);
        
        // Setup the remote file system list selection -
        _remoteJComboListener.setReferences("JLIST", jList1);
        _remoteJComboListener.setReferences("LIST_MODEL",_listModelProjectModel);
        _remoteJComboListener.setReferences("SESSION",_session);
        _remoteJComboListener.setReferences("REMOTE_CELL_RENDERER",_remoteRender);
        _remoteJComboListener.setReferences("REMOTE_COMBOBOX",jComboBox1);
        _remoteJComboListener.setReferences("FILE_TRANSFER_TOOL",this);
        _remoteJComboListener.setReferences("REMOTE_FILESYSTEM_TREE","PROJECT_FILESYSTEM_TREE");

        // Setup the click adapter -
        _doubleClickAdapter.setProperty("JLIST", jList1);
        _doubleClickAdapter.setProperty("LIST_MODEL",_listModelProjectModel);



    }

    private void populateProjectList()
    {
        // When I get here I have the buffer all ready to go -
        // Get the address and the port name of the server -
        String strIPAddress = (String)_session.getProperty("SERVER_ADDRESS");
        String strPort = (String)_session.getProperty("PROJECT_DIRECTORY_SERVER_PORT");
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
            String strReturnString = SocketService.sendMessage(strBuffer.toString(), strIPAddress, strPort, _session);
            PublishService.submitData("Rcvd - "+strReturnString);

             // Ok, check to see if return string is null -
            if (strReturnString!=null)
            {
                // Clear out the list model -
                _listModelProjectModel.clear();

                // If I get here that I should have a list of the files on the server -
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                // Ok, so we have a document -
                document = builder.parse(new InputSource(new StringReader(strReturnString)));
                processNodes(document.getFirstChild(),_listModelProjectModel);
   

                // Add model to jList2 -
                jList1.setModel(_listModelProjectModel);

                // Set the tree in session -
                _session.setProperty("PROJECT_FILESYSTEM_TREE", document);
            }
        }
        catch (Exception error)
        {
            PublishService.submitData("Spank me - some type of error "+error);
            error.printStackTrace();
        }

    }

    public void processNodes(Node node,DefaultListModel model)
    {
        File fileOnServer = null;
        NodeList nodeList = node.getChildNodes();
        int NUMBER_OF_NODES = nodeList.getLength();
        Hashtable table = null;

        // Put the main node in the dir -
        File mainFile = new File("Main");
        jComboBox1.removeAllItems();
        _remoteRender.setDirectoryFlag("Main","DIRECTORY");
        jComboBox1.addItem(mainFile);

        // clearout the list model -
        model.clear();

        // I need to get the translation table -
        DatabaseAPI dbAPI = (DatabaseAPI)_session.getProperty("DATABASE_CONNECTION");

        // Get the table -
        String strValidatedUserName = (String)_session.getProperty("VALIDATED_USERNAME");
        try {
            table = dbAPI.getUserProjects(strValidatedUserName);
        } catch (Exception ex) {
            Logger.getLogger(ProjectTool.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Cache the translation table -
        _session.setProperty("TRANSLATION_TABLE", table);
               
        for (int index=0;index<NUMBER_OF_NODES;index++)
        {
            // Get the node and add to the list -
            Node tmpNode = nodeList.item(index);
            PublishService.submitData("NodeName = "+tmpNode);
            NamedNodeMap nodeAttributes = tmpNode.getAttributes();

            String strTmp = nodeAttributes.getNamedItem("name").getNodeValue();
            PublishService.submitData("NodeName = "+strTmp);


            // Ok, I need to check to see if this is a dir or not -
            String xmlName = tmpNode.getNodeName();
            if (xmlName.equalsIgnoreCase("Directory"))
            {

                String strNewName = (String)table.get(strTmp);

                if (strNewName!=null)
                {
                    //fileOnServer = new File(strNewName);
                    fileOnServer = new File(strTmp);
                }
                else
                {
                    fileOnServer = new File(strTmp);
                }

                
                _remoteRender.setDirectoryFlag(fileOnServer.getName(), "DIRECTORY");
                model.addElement(fileOnServer);

            }
            else
            {
                fileOnServer = new File(strTmp);
                _remoteRender.setDirectoryFlag(fileOnServer.getName(), "FILE");
                model.addElement(fileOnServer);
            }

            //processNodes(tmpNode,model);

        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jComboBox1 = new javax.swing.JComboBox();

        setClosable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("Project explorer v1.0");
        setDoubleBuffered(true);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setCellRenderer(null);
        jList1.setDoubleBuffered(true);
        jScrollPane1.setViewportView(jList1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jComboBox1, 0, 368, Short.MAX_VALUE)
                        .add(23, 23, 23))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    public void valueChanged(ListSelectionEvent e) {
        // What happens when we select an item on the list?
        // Get the index of the selected item -
        //File file = (File)jList1.getSelectedValue();


        PublishService.submitData("Hitting the value changed -");

        // Get the selected -
        File file = (File)_doubleClickAdapter.getProperty("SELECTED_NODE");

        if (file!=null && file.isDirectory())
        {
            // Clear out the dir -
            _vecDir.clear();

            _listModelProjectModel.clear();

            // Repopulate the dir -
            FileSystemService.getFileFromDir(file, _vecDir);
            int NUMBER_OF_ELEMENTS = _vecDir.size();
            for (int pindex=0;pindex<NUMBER_OF_ELEMENTS;pindex++)
            {
                _listModelProjectModel.addElement(_vecDir.get(pindex));
            }

            // Update the listModel -
            jList1.setModel(_listModelProjectModel);
        }
        else
        {
            // do nothing =
        }
    }

    public void updateComponent() {
        // Fill me in -
    }

    public void updateSession() {
        // Fill me in -
    }

    public void updateNetwork() {
        // Fill me in -
    }

}
