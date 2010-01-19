/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BioChemExpTool.java
 *
 * Created on Feb 17, 2009, 10:13:38 PM
 */

package org.varnerlab.universaleditor.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
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
import javax.swing.ButtonGroup;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.*;
import org.varnerlab.universaleditor.service.IVLSystemwideEventListener;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *
 * @author jeffreyvarner
 */
public class BioChemExpTool extends javax.swing.JInternalFrame implements IVLTreeGUI, TableModelListener, IVLSystemwideEventListener {
    // Class/instance attributes -
     // Class/instance attributes -
    static int openFrameCount=1;
    static final int xOffset=50;
    static final int yOffset=50;

    private ImageIcon _imgIconOff = null;
    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconCurrent = null;

    private JTree jtreeXMLTree = null;
    private VLJTable _propTable = null;
    private IVLTableCellEditor _tblCellEditor = null;

    private XMLTreePropertiesTableModel _tableModel = new XMLTreePropertiesTableModel();
    private VLDomainComposite _vlRootTreeNode = null;
    private DefaultMutableTreeNode _guiRoot = null;
    private VLDialogGlassPane _glassPane = null;

    private UEditorSession _session = null;
    private ButtonGroup buttonGrp = null;

    /** Creates new form BioChemExpTool */
    public BioChemExpTool() {
         // Call to super
        super("Biochemical Experiment Tool v1.0",false,true);

        // iterate window count
        ++openFrameCount;

        // Set window size
        setSize(300,300);

        // Set the windows location
        setLocation(xOffset*openFrameCount,yOffset*openFrameCount);

        // Set TitleBar color when active/inactive
        setDoubleBuffered(true);

        // Get a reference to session -
        _session = (Launcher.getInstance()).getSession();


        // Initialize this mofo -
        initComponents();

        // Add a focus/click listerner -
        this.addInternalFrameListener(new BioChemExpToolFocusListener());

        // Add a drag gesture listener to the palette labels -
        VLResizeGlassPane.registerFrame(this);
        VLMoveGlassPane.registerFrame(this);

        // Glass pane for dialog messages -
        _glassPane = new VLDialogGlassPane(this);

        // configure the default xml tree -
        configureXMLTree();

        // Configure the proptable -
        configurePropertiesTable();


        // Add some system level action -
        SystemwideEventService.registerSessionListener(this);
        SystemwideEventService.registerUsernameListener(this);

        // Register the TableCellEditor -
        _tblCellEditor = (IVLTableCellEditor) new BCXTableCellEditor();
        _propTable.setVLTableCellEditor(_tblCellEditor);

        // Set some properties on the text label -
        jTextField1.putClientProperty("JTextField.variant", "search");

        buttonGrp = new ButtonGroup();
        
        int intWidth = 50;
        int intHeight = 50;
        Dimension sizeButton = new Dimension();
        sizeButton.setSize(intWidth, intHeight);
        
        /*
        jButton4.putClientProperty("JButton.buttonType", "segmentedCapsule");
        jButton4.putClientProperty("JButton.segmentPosition", "first");
        jButton4.setPreferredSize(sizeButton);
        jButton4.setMinimumSize(sizeButton);
        

        jButton4.setIcon(VLIconManagerService.getIcon("VLBEAKER-ICON"));
        jButton4.putClientProperty( "JComponent.sizeVariant", "regular" );
        buttonGrp.add(jButton4);

        jButton5.putClientProperty("JButton.buttonType", "segmentedCapsule");
        jButton5.putClientProperty("JButton.segmentPosition", "middle");
        jButton5.setPreferredSize(sizeButton);
        buttonGrp.add(jButton5);


        jButton7.putClientProperty("JButton.buttonType", "segmentedCapsule");
        jButton7.putClientProperty("JButton.segmentPosition", "middle");
        jButton7.setPreferredSize(sizeButton);
        buttonGrp.add(jButton7);


        jButton6.putClientProperty("JButton.buttonType", "segmentedCapsule");
        jButton6.putClientProperty("JButton.segmentPosition", "last");
        jButton6.setPreferredSize(sizeButton);
        buttonGrp.add(jButton6);




        /*
        VLGlassPanel glass = new VLGlassPanel();
        glass.setOpaque(false);
        this.setGlassPane(glass);
        glass.setVisible(true);
        glass.setEnabled(false);
         */

        /*
        this.setOpaque(false);
        this.getContentPane().setBackground(new Color(0,0,0,100));

        jPanel1.setOpaque(false);
        jPanel1.setBackground(new Color(0,0,0,100));

        jPanel2.setOpaque(false);
        jPanel2.setBackground(new Color(0,0,0,100));

        jPanel3.setOpaque(false);
        jPanel3.setBackground(new Color(0,0,0,100));

        jScrollPane1.setOpaque(false);
        jScrollPane1.setBackground(new Color(0,0,0,100));

        jScrollPane2.setOpaque(false);
        jScrollPane2.setBackground(new Color(0,0,0,100));

        jSplitPane1.setOpaque(false);
        jSplitPane1.setBackground(new Color(0,0,0,100));

        jSplitPane2.setOpaque(false);
        jSplitPane2.setBackground(new Color(0,0,0,100));

        jtreeXMLTree.setOpaque(false);
        jtreeXMLTree.setBackground(new Color(0,0,0,100));
         */
    }

    public void clearTree()
    {
        // Clear out the tree -
        jtreeXMLTree.removeAll();
    }

    public void setRootNode(VLDomainComposite rootNode) throws Exception
    {
        
        // Clear out the tree -
        jtreeXMLTree.removeAll();

        this._vlRootTreeNode = rootNode;
        _guiRoot = populateJTree(rootNode);

        // add to the tree -
        DefaultTreeModel mod = new DefaultTreeModel (_guiRoot);
        jtreeXMLTree.setModel (mod);
        jtreeXMLTree.setCellRenderer(new VLJTreeCellRenderer());

        // We need the table model to update when I clisk a node -
        jtreeXMLTree.addTreeSelectionListener(_tableModel);

        // Add the tree to the scroll pane -
        jScrollPane2.setViewportView(jtreeXMLTree);
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
        }

        // We need to set the VLPREFIX -
        vlNode.setProperty("VLPREFIX", "BCX");


        // Ok, I need to check the type of system -
        if (node instanceof BCXSystem)
        {
            vlNode.setProperty("DISPLAY_LABEL","System");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLImageLoader.getPNGImageIcon("JarBundler-10-Grey.png"));
            vlNode.setProperty("OPENED_ICON", VLImageLoader.getPNGImageIcon("JarBundler-10.png"));
        }

        if (node instanceof BCXExperiment)
        {
            vlNode.setProperty("DISPLAY_LABEL","Experiment");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLImageLoader.getPNGImageIcon("Flask-10-Grey.png"));
            vlNode.setProperty("OPENED_ICON", VLImageLoader.getPNGImageIcon("Flask-10.png"));
        }

        if (node instanceof BCXDataGroup)
        {
            vlNode.setProperty("DISPLAY_LABEL","DataGroup");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLImageLoader.getPNGImageIcon("ExpFolderBrown-10-Grey.png"));
            vlNode.setProperty("OPENED_ICON", VLImageLoader.getPNGImageIcon("ExpFolderBrown-10.png"));
        }

        if (node instanceof BCXValue)
        {
            vlNode.setProperty("DISPLAY_LABEL","Value");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLImageLoader.getPNGImageIcon("Value-10-Grey.png"));
            vlNode.setProperty("OPENED_ICON", VLImageLoader.getPNGImageIcon("Value-10.png"));
        }
        if (node instanceof BCXStimulus)
        {
            vlNode.setProperty("DISPLAY_LABEL","Stimulus");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLImageLoader.getPNGImageIcon("Signal-10-Grey.png"));
            vlNode.setProperty("OPENED_ICON", VLImageLoader.getPNGImageIcon("Signal-10.png"));
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

    private void configurePropertiesTable()
    {
        // Instantiate the table -
        _propTable = new VLJTable();
        SystemwideEventService.registerSessionListener(_propTable);

        // Add the table model -
        _propTable.setModel(_tableModel);

        _tableModel.addTableModelListener(this);
        
        // Add to the scroll panel -
        jScrollPane1.setViewportView(_propTable);
    }

    private void configureXMLTree()
    {
        // Instantiate the the tree -
        jtreeXMLTree = new JTree();
        jtreeXMLTree.addKeyListener(new DeleteNodeKeyListener());
        jtreeXMLTree.setShowsRootHandles(true);


        /*
        VLTreeNode vltnRootNode = new VLTreeNode();
        vltnRootNode.setProperty("DISPLAY_LABEL", "System");

        // Create the root node icon -
        vltnRootNode.setProperty("CLOSED_ICON", VLImageLoader.getPNGImageIcon("FolderExperiments-10-Grey.png"));
        vltnRootNode.setProperty("OPENED_ICON", VLImageLoader.getPNGImageIcon("FolderExperiments-10.png"));
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(vltnRootNode);
        DefaultTreeModel mod = new DefaultTreeModel (root);
        jtreeXMLTree.setModel (mod);
        jtreeXMLTree.setCellRenderer(new VLJTreeCellRenderer());
         */

        // We need the table model to update when I clisk a node -
        //jtreeXMLTree.addTreeSelectionListener(_tableModel);

        // Add the tree to the scroll pane -
        //jScrollPane2.setViewportView(jtreeXMLTree);
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


    public DefaultMutableTreeNode getTreeRoot()
    {
        return(_guiRoot);
    }

    public JTree getTree()
    {
        return(jtreeXMLTree);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();

        setIconifiable(true);
        setResizable(true);
        setPreferredSize(new java.awt.Dimension(651, 662));

        jButton1.setText("Save As ...");
        jButton1.setToolTipText("Save BCX file");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveXMLFileToDisk(evt);
            }
        });

        jButton2.setText("Load ...");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadXMLTree(evt);
            }
        });

        jButton3.setText("New");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewXMLTree(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(314, Short.MAX_VALUE)
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton1))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jButton2)
                    .add(jButton3))
                .addContainerGap())
        );

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setDividerSize(3);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setBottomComponent(jScrollPane1);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(100, 100));
        jSplitPane1.setLeftComponent(jScrollPane2);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
        );

        jButton7.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Signal-12-Grey.png")); // NOI18N
        jButton7.setToolTipText("Add a stimulus ");
        jButton7.setBorderPainted(false);
        jButton7.setDoubleBuffered(true);
        jButton7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton7.setPreferredSize(new java.awt.Dimension(148, 79));
        jButton7.setRolloverEnabled(true);
        jButton7.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Signal-12.png")); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addStimulusNode(evt);
            }
        });

        jButton6.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Value-12-Grey.png")); // NOI18N
        jButton6.setToolTipText("Add a Value to the current experiment");
        jButton6.setBorderPainted(false);
        jButton6.setDoubleBuffered(true);
        jButton6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton6.setPreferredSize(new java.awt.Dimension(148, 79));
        jButton6.setRolloverEnabled(true);
        jButton6.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Value-12.png")); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewValueNode(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Beaker-12-Grey.png")); // NOI18N
        jButton4.setToolTipText("Add a new experiment");
        jButton4.setBorderPainted(false);
        jButton4.setDoubleBuffered(true);
        jButton4.setMaximumSize(new java.awt.Dimension(100, 29));
        jButton4.setMinimumSize(new java.awt.Dimension(100, 29));
        jButton4.setPreferredSize(new java.awt.Dimension(100, 29));
        jButton4.setRolloverEnabled(true);
        jButton4.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Beaker-12.png")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewExperiment(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/ExpFolderBrown-12-Grey.png")); // NOI18N
        jButton5.setToolTipText("Add a new DataGroup to the selected experiment");
        jButton5.setBorderPainted(false);
        jButton5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton5.setPreferredSize(new java.awt.Dimension(148, 79));
        jButton5.setRolloverEnabled(true);
        jButton5.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/ExpFolderBrown-12.png")); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDataGroupNode(evt);
            }
        });

        jTextField1.setText("Search ...");

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(7, 7, 7)
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 60, Short.MAX_VALUE)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 376, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                .add(jButton5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                .add(jButton7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                .add(jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadXMLTree(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadXMLTree
        LoadXMLTreeAction xmlLoader = new LoadXMLTreeAction();
        xmlLoader.actionPerformed(evt);
    }//GEN-LAST:event_loadXMLTree

    private void createNewXMLTree(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewXMLTree

        // Ok, so I need to check to see if a user is logged on -
        UEditorSession session = (Launcher.getInstance()).getSession();
        String strUserName = (String)session.getProperty("VALIDATED_USERNAME");

        if (strUserName==null)
        {
            _glassPane.setMessage("No user is associated with this session.");
            _glassPane.blowMe();
        }
        else
        {

            // Fire the session update event to make sure I have the latest version of session -
            SystemwideEventService.fireSessionUpdateEvent();

            // Create the new tree -
            CreateNewXMLTreeAction newTree = new CreateNewXMLTreeAction();
            newTree.actionPerformed(evt);
        }
    }//GEN-LAST:event_createNewXMLTree

    private void saveXMLFileToDisk(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveXMLFileToDisk

        SaveXMLFileAction saveFile = new SaveXMLFileAction();
        saveFile.actionPerformed(evt);

    }//GEN-LAST:event_saveXMLFileToDisk

    private void addNewExperiment(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewExperiment
        // TODO add your handling code here:
        AddNewExperimentNodeAction addNode = new AddNewExperimentNodeAction();
        addNode.actionPerformed(evt);

    }//GEN-LAST:event_addNewExperiment

    private void addDataGroupNode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataGroupNode
        // TODO add your handling code here:
        AddNewDataGroupNodeAction addNode = new AddNewDataGroupNodeAction();
        addNode.actionPerformed(evt);
    }//GEN-LAST:event_addDataGroupNode

    private void addNewValueNode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewValueNode
        AddValueNodeAction addNode = new AddValueNodeAction();
        addNode.actionPerformed(evt);
    }//GEN-LAST:event_addNewValueNode

    private void addStimulusNode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addStimulusNode
        AddNewStimulusNodeAction addNode = new AddNewStimulusNodeAction();
        addNode.actionPerformed(evt);
    }//GEN-LAST:event_addStimulusNode


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables


    /*
    @Override
    protected void paintComponent(Graphics g)
    {
        // set transparent color
        g.setColor(new Color(255,255,255,64));

        g.fillRect(0, 0, getWidth(), getHeight());
    }
     */

    public void tableChanged(TableModelEvent e) {
        // Ok, when I get here the table model has changed - I need to update the values in the tree -

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
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)jtreeXMLTree.getLastSelectedPathComponent();

                // Get the index of the selected node

                // Ok, so I need to get the userobject from this mofo and then pull all the properties out -
                VLTreeNode vltnNode = (VLTreeNode)selectedNode.getUserObject();

                // Set the properties of the object -
                vltnNode.setProperty(key.toString(), val.toString());

                System.out.println("UPDATE key = "+key+" VALUE="+val);

                // Let's overright the old node -
                // selectedNode.setUserObject(vltnNode);
                DefaultTreeModel treeModel = (DefaultTreeModel)jtreeXMLTree.getModel();
                treeModel.nodeChanged(selectedNode);
            }
        }
            
        

    }

    public void updateComponent() {

        // See if there is a validated username -
        String strUserName = (String)_session.getProperty("VALIDATED_USERNAME");

        PublishService.submitData("Checking for a username - squishy monkey love...username is "+strUserName);


        // Check to see if his bitch is null -
        if (strUserName!=null)
        {
            jButton1.setEnabled(true);
            jButton2.setEnabled(true);
            jButton3.setEnabled(true);
        }
        else
        {
            //_glassPane.setMessage("No user is associated with this session.");
            //_glassPane.blowMe();
        }


    }

    public void updateSession() {
        _session = (Launcher.getInstance()).getSession();
    }

    public void updateNetwork() {
        
    }

}
