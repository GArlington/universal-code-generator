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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.varnerlab.universaleditor.gui.widgets.*;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.actions.*;

import java.io.*;


import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.service.IVLSystemwideEventListener;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jeffreyvarner
 */
public class BioChemExpTool extends javax.swing.JInternalFrame implements TableModelListener, IVLSystemwideEventListener {
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

	private BCXJTreePropertiesTableModel _tableModel = new BCXJTreePropertiesTableModel();
	private DefaultMutableTreeNode _guiRoot = null;
	private VLDialogGlassPane _glassPane = null;
	
	// Objects for the dialog -
	private JComponent sheet;
	private JPanel glass;

	private UEditorSession _session = null;
	private ButtonGroup buttonGrp = null;

	// Create a xpFactory/xpath obj (we'll use this a zillion times -)
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private static BioChemExpTool _this = null;
	private Node _vlRootTreeNode = null;
	private ArrayList<String> aList = new ArrayList<String>();


	// static accessor method
	public static BioChemExpTool getInstance(){
		if (_this==null){
			_this=new BioChemExpTool();
		}
		return(_this);
	}


	public void loadDefaultTree() throws Exception 
	{

		// Ok - load the tmplate file -
		String strTemplate = "./templates/BCXTemplate.xml";

		// details..
		File configFile = new File(strTemplate);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(configFile);
		doc.getDocumentElement().normalize();

		// Set the template in session -
		_session.setProperty("BCX_TEMPLATE_TREE", doc);		
		
		// Ok, so now we the doc, try to create a tree -
		setRootNode(doc);
		
		// Fire up the save as and load buttons -
		jButton1.setEnabled(true);
		jButton2.setEnabled(true);
		jButton3.setEnabled(true);
	}

	/** Creates new form BioChemExpTool */
	private BioChemExpTool() {
		// Call to super
		super("Biochemical Experiment Tool v1.0",false,true);

		// setup the list of containers -
		// These are container labels - 
		aList.add("Model");
		aList.add("server_options");
		aList.add("listOfExperiments");
		aList.add("experiment");
		aList.add("measurement_file");

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
		_propTable.setRowHeight(20);

		// Set some properties on the text label -
		jTextField1.putClientProperty("JTextField.variant", "search");

		buttonGrp = new ButtonGroup();

		int intWidth = 50;
		int intHeight = 50;
		Dimension sizeButton = new Dimension();
		sizeButton.setSize(intWidth, intHeight);
	}

	public void clearTree()
	{
		// Clear out the tree -
		jtreeXMLTree.removeAll();
	}

	private void configurePropertiesTable()
	{
		// Instantiate the table -
		_propTable = new VLJTable();

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
		jtreeXMLTree.setCellRenderer(new BCXJTreeCellRenderer());
		jtreeXMLTree.addKeyListener(new DeleteNodeKeyListener());
		jtreeXMLTree.addKeyListener(new CopyAndPasteTreeNodeKeyListener());
		jtreeXMLTree.setShowsRootHandles(true);
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
		jButton8 = new javax.swing.JButton();
		jButton9 = new javax.swing.JButton();

		setIconifiable(true);
		setResizable(true);
		setPreferredSize(new java.awt.Dimension(645, 660));

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
				
				try {
					clearTree();
					loadDefaultTree();
				}
				catch (Exception error)
				{
					error.printStackTrace();
				}
			}
		});
		
		org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(300, Short.MAX_VALUE)
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton2)
                    .add(jButton3)
                    .add(jButton1))
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
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 582, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
        );

		jButton7.setToolTipText("Add a stimulus to the current experiment");
		jButton7.setIcon(VLIconManagerService.getIcon("PURPLE-16-GREY-ICON"));
		jButton7.setRolloverIcon(VLIconManagerService.getIcon("PURPLE-16-ICON"));
		jButton7.setBorderPainted(false);
		jButton7.setDoubleBuffered(true);
		jButton7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		jButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jButton7.setPreferredSize(new java.awt.Dimension(148, 79));
		jButton7.setRolloverEnabled(true);
		jButton7.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addStimulusNode(evt);
			}
		});

		jButton6.setToolTipText("Add a value to the current experiment");
		jButton6.setIcon(VLIconManagerService.getIcon("EVALUE-16-GREY-ICON"));
		jButton6.setRolloverIcon(VLIconManagerService.getIcon("EVALUE-16-ICON"));
		jButton6.setBorderPainted(false);
		jButton6.setDoubleBuffered(true);
		jButton6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		jButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jButton6.setPreferredSize(new java.awt.Dimension(148, 79));
		jButton6.setRolloverEnabled(true);
		jButton6.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addNewValueNode(evt);
			}
		});

		jButton4.setToolTipText("Add a new experiment");
		jButton4.setIcon(VLIconManagerService.getIcon("BEAKER-18-GREY-ICON"));
		jButton4.setRolloverIcon(VLIconManagerService.getIcon("BEAKER-18-ICON"));
		jButton4.setBorderPainted(false);
		jButton4.setDoubleBuffered(true);
		jButton4.setMaximumSize(new java.awt.Dimension(100, 29));
		jButton4.setMinimumSize(new java.awt.Dimension(100, 29));
		jButton4.setPreferredSize(new java.awt.Dimension(148, 79));
		jButton4.setRolloverEnabled(true);
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addNewExperiment(evt);
			}
		});

		jButton5.setToolTipText("Add a results file to the selected experiment");
		jButton5.setIcon(VLIconManagerService.getIcon("EGROUP-16-GREY-ICON"));
		jButton5.setRolloverIcon(VLIconManagerService.getIcon("EGROUP-16-ICON"));
		jButton5.setBorderPainted(false);
		jButton5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jButton5.setPreferredSize(new java.awt.Dimension(148, 79));
		jButton5.setRolloverEnabled(true);
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addDataGroupNode(evt);
			}
		});
		
		jButton9.setToolTipText("Add a data file description ...");
		jButton9.setIcon(VLIconManagerService.getIcon("FLASH-16-GREY-ICON"));
		jButton9.setRolloverIcon(VLIconManagerService.getIcon("FLASH-16-ICON"));
		jButton9.setBorderPainted(false);
		jButton9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jButton9.setPreferredSize(new java.awt.Dimension(148, 79));
		jButton9.setRolloverEnabled(true);
		jButton9.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addDataFileReferenceNode(evt);
			}
		});

		jTextField1.setText("Search ...");

		jButton8.setToolTipText("Save this BCX file ... ");
		jButton8.setIcon(new javax.swing.ImageIcon("./images/ShoppingCart-18-Grey.png"));
		jButton8.setRolloverIcon(new javax.swing.ImageIcon("./images/ShoppingCart-18.png")); // NOI18N
		jButton8.setBorderPainted(false);
		jButton8.setDoubleBuffered(true);
		jButton8.setEnabled(false);
		jButton8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		jButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jButton8.setPreferredSize(new java.awt.Dimension(148, 79));
		jButton8.setRolloverEnabled(true);
		jButton8.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				quickSaveBCXFile(evt);
			}
		});

		org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jButton4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jButton8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(jButton4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .add(jButton5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .add(jButton7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .add(jButton6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(jButton8)
                .add(jButton9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(15, 15, 15)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .add(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(20, 20, 20))
        );

        pack();
	}// </editor-fold>//GEN-END:initComponents

	private void quickSaveBCXFile(ActionEvent evt) {
		// Create a new action instance -
		DirectSaveBCXFileAction saveBCXFile = new DirectSaveBCXFileAction();

		// Ok, so I need to get the file has been selected -
		File selectedFile = (File)_session.getProperty("LOCAL_SELECTED_FILE");
		evt.setSource(selectedFile);
		saveBCXFile.actionPerformed(evt);
	}


	private void addDataFileReferenceNode(ActionEvent evt) {	
		AddDataColumnNodeAction action = new AddDataColumnNodeAction();
		action.actionPerformed(evt);
	}


	public void enableQuickSaveButton()
	{
		jButton8.setEnabled(true);
	}
	
	private void loadXMLTree(java.awt.event.ActionEvent evt) 
	{//GEN-FIRST:event_loadXMLTree
		LoadBCXFileTreeAction xmlLoader = new LoadBCXFileTreeAction();
		xmlLoader.actionPerformed(evt);
	}//GEN-LAST:event_loadXMLTree

	private void createNewXMLTree(java.awt.event.ActionEvent evt) 
	{//GEN-FIRST:event_createNewXMLTree

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

		SaveBCXFileAction saveFile = new SaveBCXFileAction();
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
	private javax.swing.JButton jButton8;
	private javax.swing.JButton jButton9;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JTextField jTextField1;
	// End of variables declaration//GEN-END:variables


	public void tableChanged(TableModelEvent e) {

		// Ok, when I get here the table model has changed - I need to update the values in the tree -

		// Get the selected row -
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

				// Ok, so here is the trick bit -
				Node xmlTreeNode = (Node)vltnNode.getProperty("XML_TREE_NODE");

				// Set the properties of the object -
				//vltnNode.setProperty(key.toString(), val.toString());
				NamedNodeMap attList = xmlTreeNode.getAttributes();
				int NUM_OF_ATTRIBUTES = attList.getLength(); 
				for (int index=0;index<NUM_OF_ATTRIBUTES;index++)
				{
					// Get the attribute node -
					Node attributeNode = attList.item(index);

					// Get the name of this attribute -
					String strArrtributeName = attributeNode.getNodeName();

					// Compare with the currently selected key -
					String tmpKeyName = key.toString();
					System.out.println("UPDATE key = "+key+" VALUE="+tmp);    
					if (tmpKeyName.equalsIgnoreCase(strArrtributeName))
					{
						// Ok, if I'm here then I have a match -- update the xml node and break out of the loop-
						attributeNode.setNodeValue(tmp);						            		
						break;
					}
				}


				// Let's overright the old node -
				selectedNode.setUserObject(vltnNode);
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
		// Get a new session - 
		_session = (Launcher.getInstance()).getSession();
		
		// Set the working dir, etc from session
		this.updateUserObjectTree();
	}
	
	private void updateUserObjectTree()
	{
		// Ok, we need to get the username and current session ID -
		String strUserName = (String)_session.getProperty("VALIDATED_USERNAME");
		String strSessionID = (String)_session.getProperty("SESSION_ID");
		String strRemotePath = (String)_session.getProperty("SELECTED_REMOTE_PATH");

		Date objDate = new Date();
		String strObjDate = objDate.toString();
		
		// Ok, if we have both of these mofo's I can create a working DIR -
		if (strUserName!=null && strRemotePath!=null)
		{
			// If I get here then I can set the working dir -

			// Get the tree model -
			if (jtreeXMLTree!=null)
			{
				DefaultTreeModel mod = (DefaultTreeModel) jtreeXMLTree.getModel();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode)mod.getRoot();
				int NUMBER_OF_KIDS = root.getChildCount();
				for (int kid_index=0;kid_index<NUMBER_OF_KIDS;kid_index++)
				{
					// Get the kid and his user obj -
					DefaultMutableTreeNode kid = (DefaultMutableTreeNode) root.getChildAt(kid_index);
					VLTreeNode userObj = (VLTreeNode)kid.getUserObject();
					
					// Get the keyname -
					String keyName = (String)userObj.getProperty("KEYNAME");

					System.out.println("Checking keyName - "+keyName);

					// Ok - look for the options node -
					if (keyName.equalsIgnoreCase("SERVER_OPTIONS"))
					{
						// Ok, so I have the server_options tag - get its child and spank it ... ooooh yea you know what I'm talkin about..tired
						DefaultMutableTreeNode optionsSwingNode = (DefaultMutableTreeNode)kid.getChildAt(0);
						VLTreeNode userObjOptions = (VLTreeNode)optionsSwingNode.getUserObject();
						
						// Get the underlying xmlNode and set the data -
						Node xmlNode = (Node)userObjOptions.getProperty("XML_TREE_NODE");
						
						// Ok, so now I have the corresponding tree node - set some data
						// Username
						Node userName = xmlNode.getAttributes().getNamedItem("username");
						userName.setNodeValue(strUserName);
						
						// Working dir -
						Node workingDir = xmlNode.getAttributes().getNamedItem("working_dir");
						workingDir.setNodeValue(strRemotePath);
						
						// Date updated -
						Node lastUpdated = xmlNode.getAttributes().getNamedItem("last_updated");
						lastUpdated.setNodeValue(strObjDate);
						
						break;
					}
				}
			}
		}
	}

	public void updateNetwork() {

	}

	public void setRootNode(Document doc) throws Exception {
		// Clear out the tree -
		jtreeXMLTree.removeAll();
		Node rootNode = null;

		// Get the root node and its attributes -
		String expression = "/Model";

		// Grab the root node -
		rootNode = (Node)_xpath.evaluate(expression, doc, XPathConstants.NODE);
		this._vlRootTreeNode = rootNode;

		// Populate the jTree w/the Node -
		_guiRoot = populateJTree(_vlRootTreeNode);

		// add to the tree -
		DefaultTreeModel mod = new DefaultTreeModel (_guiRoot);
		jtreeXMLTree.setModel (mod);
		jtreeXMLTree.addTreeSelectionListener(_tableModel);
		
		// I need to see if
		_tableModel.fireTableDataChanged();

		// Add the tree to the scroll pane -
		jScrollPane2.setViewportView(jtreeXMLTree);
	}
	
	

	// Populate the jtree -
	private DefaultMutableTreeNode populateJTree(Node xmlNode) throws Exception
	{

		// OK, starting w/the root node I need to populate the JTree -
		DefaultMutableTreeNode guiNode = new DefaultMutableTreeNode();

		// Create a VarnerLab node wrapper -
		VLTreeNode vlNode = new VLTreeNode();

		// Get some info from the xmlNode -
		String strNodeName = (String)xmlNode.getNodeName();
		String strNodeValue = (String)xmlNode.getTextContent();

		int COMMENT = strNodeName.indexOf("#");
		if (COMMENT==-1)
		{
			if (strNodeValue==null)
			{
				strNodeValue = "";
			}

			// Ok, so when I get here I have the name and value -
			// Configure the node -


			if (aList.contains(strNodeName))
			{
				vlNode.setProperty("DISPLAY_LABEL",strNodeName);
				vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
				vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));
				vlNode.setProperty("KEYNAME",strNodeName);
				vlNode.setProperty("XML_TREE_NODE", xmlNode);
				
				// Get the editable attribute -
				Node leafNode = xmlNode.getAttributes().getNamedItem("editable");
				vlNode.setProperty("EDITABLE",leafNode.getNodeValue());
			}
			else
			{
				// Ok, so If I get here then I have a leaf node -

				// Set the display label -
				String strDisplayLabel = "";
				String strAttributeID = "";

				// Check to see if we have an id attribute -
				Node leafNode = xmlNode.getAttributes().getNamedItem("id");

				if (leafNode!=null)
				{
					strAttributeID = leafNode.getNodeValue();
				}
				else
				{
					//System.out.println("leadNode named "+strNodeName+" has id = null");
				}

				if (!strAttributeID.isEmpty())
				{
					strDisplayLabel = strAttributeID;
				}
				else
				{
					strDisplayLabel = strNodeName;
				}

				vlNode.setProperty("DISPLAY_LABEL",strDisplayLabel);
				vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-GREY-ICON"));
				vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-ICON"));
				vlNode.setProperty("XML_TREE_NODE", xmlNode);

				// Get editable flag -
				// Node leafNode = xmlNode.getAttributes().getNamedItem("editable");        	

				// Set the editable flag -
				vlNode.setProperty("EDITABLE","TRUE");

				// System.out.println("Leaf node = "+strNodeName+" is editable? "+leafNode.getNodeValue());

				// ok -- hard code some properties -
				vlNode.setProperty("KEYNAME",strNodeName);
				vlNode.setProperty(strNodeName,strNodeValue);
			}

			// set the node linkage -
			guiNode.setUserObject(vlNode);
			
			// ok, let's process this guys kids -
			NodeList kids = xmlNode.getChildNodes();
			int NUMBER_OF_KIDS = kids.getLength();
			for (int index = 0;index<NUMBER_OF_KIDS;index++)
			{
				Node tmpNode = kids.item(index);

				DefaultMutableTreeNode guiKidNode = populateJTree(tmpNode);
				if (guiKidNode!=null)
				{
					guiNode.add(guiKidNode);
				}
			}

			// return the guiNode -
			return(guiNode);
		}
		else
		{
			// if I get here i havea comment -
			return(null);
		}
	}
	
	public JComponent showJDialogAsSheet (JDialog dialog) {

		// need to cjeck to see if I have already addded something to the glass pane -
		if (getGlassPane() instanceof InfiniteProgressPanel)
		{
			glass = new JPanel();
			glass.setOpaque(false);
			setGlassPane(glass);
		}


		glass = (JPanel)this.getGlassPane();

		sheet = (JComponent) dialog.getContentPane();
		sheet.setOpaque(false);
		sheet.setBackground (new Color(0,0,0,225));

		glass.setLayout (new GridBagLayout());
		sheet.setBorder (new LineBorder(Color.GRAY, 1));

		glass.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		glass.add (sheet, gbc);
		gbc.gridy=1;
		gbc.weighty = Integer.MAX_VALUE;
		glass.add (Box.createGlue(), gbc);
		glass.setVisible(true);
		return sheet;
	}

	public void hideSheet() {
		glass.setVisible(false);
	}

}
