/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ModelCodeGeneratorFileEditor.java
 *
 * Created on Feb 13, 2009, 3:21:20 PM
 */

package org.varnerlab.universaleditor.gui;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.varnerlab.universaleditor.gui.widgets.*;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.actions.*;

import java.io.*;


import javax.swing.ImageIcon;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.varnerlab.universaleditor.service.IVLSystemwideEventListener;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *
 * @author jeffreyvarner
 */
public class ModelCodeGeneratorFileEditor extends javax.swing.JInternalFrame implements IVLTreeGUI,IVLSystemwideEventListener,TableModelListener {
    // Class/instance attributes -
    static int openFrameCount=1;
    static final int xOffset=50;
    static final int yOffset=50;

    private ImageIcon _imgIconOff = null;
    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconCurrent = null;

    private File _propFile = null;
    private String _strWorkingDir = "";

    // Stuff associated with the properties tree -
    private VLDomainComposite _vlRootTreeNode = null;
    private DefaultMutableTreeNode _guiRoot = null;
    private JTree jTree1 = null;
    private UEditorSession _session = null;

    private VLJTable _propTable = null;
    private XMLTreePropertiesTableModel _tableModel = new XMLTreePropertiesTableModel();
    private ModelPropertiesFileTableCellEditor _tableCellEditor = new ModelPropertiesFileTableCellEditor();


    /** Creates new form ModelCodeGeneratorFileEditor */
    public ModelCodeGeneratorFileEditor() {
        //super("Property File Editor Tool v1.0");

        // Call to super
        super("Model properties file editor tool v1.0",false,true);

        // iterate window count
        ++openFrameCount;

        // Set window size
        setSize(300,300);

        // Set the windows location
        setLocation(xOffset*openFrameCount,yOffset*openFrameCount);

        // Set TitleBar color when active/inactive
        setDoubleBuffered(true);

        // Initialize this mofo -
        initComponents();

        // Config the table -
        configurePropTable();

        // Set some properties on the text label -
        jTextField1.putClientProperty("JTextField.variant", "search");


        // Register listeners -
        //this.addFocusListener(new ModelCodeGeneratorFileToolFocusListener());
        this.addInternalFrameListener(new ModelCodeGeneratorFileToolFocusListener());

        // Register me as a session listner =
        SystemwideEventService.registerSessionListener(this);

        // Grab the session -
        _session = (Launcher.getInstance()).getSession();

        // Set the working dir -
        this.setWorkingDirectory();

        // Set -
        _propTable.setVLTableCellEditor(_tableCellEditor);
        

       
    }

    public JTree getTree()
    {
        return(jTree1);
    }

    private void configurePropTable()
    {
        // Create a new instance of the _propTable -

        _propTable = new VLJTable();

        // Set the default table model -
        _propTable.setModel(_tableModel);

        // Ok, so let's set the renderer on the JTable -
        //TableColumn col = _propTable.getColumnModel().getColumn(1);
        // col.setCellEditor(new PropertiesTableCellEditor());

        _propTable.setRowHeight(22);

        // Register the table listener
        _tableModel.addTableModelListener(this);

        // Add to the scroll panel -
        jScrollPane2.setViewportView(_propTable);
        System.out.print("Hey now - "+_propTable.getCellEditor());
    }

    public void clearTree()
    {
        // Clear out the tree -
        jTree1.removeAll();
    }

    public DefaultMutableTreeNode getTreeRoot()
    {
        return(_guiRoot);
    }

    public void setRootNode(VLDomainComposite rootNode) throws Exception
    {

        if (jTree1==null)
        {
            jTree1 = new JTree();

            // set some properties on the tree -
            jTree1.setShowsRootHandles(true);
            jTree1.putClientProperty("Quaqua.Tree.style", "striped");
        }


        // Clear out the tree -
        jTree1.removeAll();

        this._vlRootTreeNode = rootNode;
        _guiRoot = populateJTree(rootNode);

        // add to the tree -
        DefaultTreeModel mod = new DefaultTreeModel (_guiRoot);
        jTree1.setModel (mod);
        jTree1.setCellRenderer(new VLJPropTreeCellRenderer());

        // We need the table model to update when I clisk a node -
        jTree1.addTreeSelectionListener(_tableModel);

        // I need to see if

        // Add the tree to the scroll pane -
        jScrollPane1.setViewportView(jTree1);
    }

    private DefaultMutableTreeNode populateJTree(VLDomainComponent node) throws Exception
    {

        // OK, statrting w/the root node I need to populate the JTree -
        DefaultMutableTreeNode guiNode = new DefaultMutableTreeNode();

        // Create a VarnerLab node warpper -
        VLTreeNode vlNode = new VLTreeNode();

        // Create a VLNode w/this nodes props -
        Enumeration keys = node.getKeys();
        while (keys.hasMoreElements())
        {
            // Get properties -
            Object key =keys.nextElement();
            Object val = node.getProperty(key);

            // Add them to the current vlNode -
            vlNode.setProperty(key, val);
            vlNode.setProperty("KEYNAME",key);

        }

        // We need to set the VLPREFIX -
        vlNode.setProperty("VLPREFIX", "VL");

        if (node instanceof VLProperty)
        {
            vlNode.setProperty("DISPLAY_LABEL","Property");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-ICON"));
        }

        if (node instanceof VLUniversal)
        {
            vlNode.setProperty("DISPLAY_LABEL","Universal");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));
        }


        // OK, when I get here I have a configured vlNode - add it to the GUI node -
        guiNode.setUserObject(vlNode);

        // Ok, we need to handle the kids -
        int NUMBER_OF_KIDS = node.getNumberOfChildren();
        for (int index = 0;index<NUMBER_OF_KIDS;index++)
        {
            // Ok, when I get here I need to get a child and then configure that mofo -
            VLDomainComponent childNode = (VLDomainComponent)node.getChildAt(index);

            // Configure that bee....atch ...
            DefaultMutableTreeNode myFingKid = populateJTree(childNode);

            // Add my kids to the current parent node -
            guiNode.add(myFingKid);
        }

        // return the configured GUI node -
        return(guiNode);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();

        setIconifiable(true);
        setDoubleBuffered(true);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        jButton1.setText("Save As ...");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savePropertiesFile(evt);
            }
        });

        jButton2.setText("Load ...");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadModelPropFileAction(evt);
            }
        });

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setTopComponent(jScrollPane1);
        jSplitPane1.setRightComponent(jScrollPane2);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBox1, 0, 482, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                            .add(jButton2)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(jButton1)
                            .addContainerGap())
                        .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 460, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jButton2))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jButton4.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/YellowFile-12-Grey.png")); // NOI18N
        jButton4.setToolTipText("Add new property");
        jButton4.setBorderPainted(false);
        jButton4.setDoubleBuffered(true);
        jButton4.setEnabled(false);
        jButton4.setRolloverEnabled(true);
        jButton4.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/YellowFile-12.png")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewProperty(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Delete-12-Grey.png")); // NOI18N
        jButton5.setToolTipText("Delete current property");
        jButton5.setBorderPainted(false);
        jButton5.setEnabled(false);
        jButton5.setMaximumSize(new java.awt.Dimension(49, 45));
        jButton5.setMinimumSize(new java.awt.Dimension(49, 45));
        jButton5.setPreferredSize(new java.awt.Dimension(49, 45));
        jButton5.setRolloverEnabled(true);
        jButton5.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Delete-12.png")); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePropertyNode(evt);
            }
        });

        jTextField1.setText("Search ...");

        jButton6.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/YellowFile-12-Grey.png")); // NOI18N
        jButton6.setToolTipText("Add new property");
        jButton6.setBorderPainted(false);
        jButton6.setDoubleBuffered(true);
        jButton6.setEnabled(false);
        jButton6.setRolloverEnabled(true);
        jButton6.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/YellowFile-12.png")); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6createNewProperty(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jButton6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 355, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(15, 15, 15))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        System.out.println("Focus gained");
    }//GEN-LAST:event_formFocusGained

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        System.out.println("Focus lost");
    }//GEN-LAST:event_formFocusLost

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        
    }//GEN-LAST:event_formMouseClicked

    private void LoadModelPropFileAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadModelPropFileAction
        // This stud was generated by that motha*fu**?in gui code gen - call out to my handler -
        LoadXMLTreeAction action = new LoadXMLTreeAction();
        action.actionPerformed(evt);

        // set the button enabled -
        jButton4.setEnabled(true);
        jButton5.setEnabled(true);
        jButton1.setEnabled(true);

    }//GEN-LAST:event_LoadModelPropFileAction

    public void setPropFileRef(File fileName)
    {
        this._propFile = fileName;
    }


    private void savePropertiesFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savePropertiesFile
        // TODO add your handling code here:
        SaveXMLPropFileAction saveAction = new SaveXMLPropFileAction();
        saveAction.actionPerformed(evt);
    }//GEN-LAST:event_savePropertiesFile

    private void createNewProperty(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewProperty
        // TODO add your handling code here:
        AddNewPropertyLeafAction action = new AddNewPropertyLeafAction();
        action.actionPerformed(evt);
    }//GEN-LAST:event_createNewProperty

    private void deletePropertyNode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePropertyNode
        // TODO add your handling code here:
        DeletePropertyNodeAction action = new DeletePropertyNodeAction();
        action.actionPerformed();
    }//GEN-LAST:event_deletePropertyNode

    private void jButton6createNewProperty(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6createNewProperty
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6createNewProperty

    
    public VLJTable getPropJTable()
    {
        return(_propTable);
    }

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    public void updateComponent() {

    }

    public void updateSession() {
         // Get the new session -
        _session = (Launcher.getInstance()).getSession();

        // Set the working dir -
        this.setWorkingDirectory();
    }

    private void setWorkingDirectory()
    {
        // Ok, we need to get the username and current session ID -
        String strUserName = (String)_session.getProperty("VALIDATED_USERNAME");
        String strSessionID = (String)_session.getProperty("SESSION_ID");

        // Ok, if we have both of these mofo's I can create a working DIR -
        if (strUserName!=null && strSessionID!=null)
        {
            // If I get here then I can set the working dir -
            String strWorkingDir = strUserName+"/"+strSessionID+"/";

             // Get the tree model -
             if (jTree1!=null)
             {
                DefaultTreeModel mod = (DefaultTreeModel) jTree1.getModel();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode)mod.getRoot();
                int NUMBER_OF_KIDS = root.getChildCount();
                for (int kid_index=0;kid_index<NUMBER_OF_KIDS;kid_index++)
                {
                     // Get the kid and his user obj -
                    DefaultMutableTreeNode kid = (DefaultMutableTreeNode) root.getChildAt(kid_index);
                    VLTreeNode userObj = (VLTreeNode)kid.getUserObject();

                    // Get the keyname -
                    String keyName = (String)userObj.getProperty("KEYNAME");

                    PublishService.submitData("Checking keyName - "+keyName);

                    // Check the keyname -- is it the one wee need? Ha Ha ... I said wee
                    if (keyName.equalsIgnoreCase("WORKING_DIRECTORY_NAME"))
                    {
                         userObj.setProperty(keyName,strWorkingDir);
                    }

                }
             }
        }
    }

    public void updateNetwork() {

    }

    public void tableChanged(TableModelEvent e) {

         // Ok, when I get here the table model has changed - I need to update the values in the tree -

        // System.out.println("Hey now - going to update the prop table...");

        int ROW_INDEX = _propTable.getSelectedRow();

        if (ROW_INDEX!=-1)
        {
            // Get the data from the table model -
            Object key = _tableModel.getValueAt(ROW_INDEX, 0);
            Object val = _tableModel.getValueAt(ROW_INDEX, 1);
            String tmp = val.toString();

            if (key!=null || !tmp.equalsIgnoreCase(" "))
            {

                // Get the current node -
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jTree1.getLastSelectedPathComponent();

                // Get the index of the selected node

                // Ok, so I need to get the userobject from this mofo and then pull all the properties out -
                VLTreeNode vltnNode = (VLTreeNode)selectedNode.getUserObject();

                // Set the properties of the object -
                vltnNode.setProperty(key.toString(), val.toString());

                // System.out.println("UPDATE key = "+key+" VALUE="+val);

                // Let's overright the old node -
                // selectedNode.setUserObject(vltnNode);
                DefaultTreeModel treeModel = (DefaultTreeModel)jTree1.getModel();
                treeModel.nodeChanged(selectedNode);
            }
        }
    }

}
