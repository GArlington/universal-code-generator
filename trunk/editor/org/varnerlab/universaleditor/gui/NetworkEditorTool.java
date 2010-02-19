/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NetworkEditorTool.java
 *
 * Created on Mar 18, 2009, 9:36:29 PM
 */

package org.varnerlab.universaleditor.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.BCXDataGroup;
import org.varnerlab.universaleditor.domain.BCXExperiment;
import org.varnerlab.universaleditor.domain.BCXStimulus;
import org.varnerlab.universaleditor.domain.BCXSystem;
import org.varnerlab.universaleditor.domain.BCXValue;
import org.varnerlab.universaleditor.domain.SBMLTreePropertiesTableModel;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.domain.VLDomainComponent;
import org.varnerlab.universaleditor.domain.VLDomainComposite;
import org.varnerlab.universaleditor.domain.VLFFReactionTableModel;
import org.varnerlab.universaleditor.domain.XMLTreePropertiesTableModel;
import org.varnerlab.universaleditor.domain.sbml.apply;
import org.varnerlab.universaleditor.domain.sbml.ci;
import org.varnerlab.universaleditor.domain.sbml.kineticLaw;
import org.varnerlab.universaleditor.domain.sbml.listOfParameters;
import org.varnerlab.universaleditor.domain.sbml.listOfProducts;
import org.varnerlab.universaleditor.domain.sbml.listOfReactants;
import org.varnerlab.universaleditor.domain.sbml.listOfReactions;
import org.varnerlab.universaleditor.domain.sbml.listOfSpecies;
import org.varnerlab.universaleditor.domain.sbml.math;
import org.varnerlab.universaleditor.domain.sbml.model;
import org.varnerlab.universaleditor.domain.sbml.parameter;
import org.varnerlab.universaleditor.domain.sbml.reaction;
import org.varnerlab.universaleditor.domain.sbml.sbml;
import org.varnerlab.universaleditor.domain.sbml.species;
import org.varnerlab.universaleditor.domain.sbml.speciesReference;
import org.varnerlab.universaleditor.domain.sbml.times;
import org.varnerlab.universaleditor.gui.actions.DirectSaveSBMLFileAction;
import org.varnerlab.universaleditor.gui.actions.DirectSaveXMLPropFileAction;
import org.varnerlab.universaleditor.gui.actions.LoadSBMLTreeAction;
import org.varnerlab.universaleditor.gui.actions.NewSBMLTreeAction;
import org.varnerlab.universaleditor.gui.actions.SaveSBMLFileAction;
import org.varnerlab.universaleditor.gui.widgets.IVLTableCellEditor;
import org.varnerlab.universaleditor.gui.widgets.InfiniteProgressPanel;
import org.varnerlab.universaleditor.gui.widgets.NetworkTableCellEditor;
import org.varnerlab.universaleditor.gui.widgets.NetworkToolFocusListener;
import org.varnerlab.universaleditor.gui.widgets.NewSBMLTreeLeafKeyAdaptor;
import org.varnerlab.universaleditor.gui.widgets.VLDialogGlassPane;
import org.varnerlab.universaleditor.gui.widgets.VLJPropTreeCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.VLJTable;
import org.varnerlab.universaleditor.gui.widgets.VLJTreeCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.VLMoveGlassPane;
import org.varnerlab.universaleditor.gui.widgets.VLResizeGlassPane;
import org.varnerlab.universaleditor.gui.widgets.VLSBMLJTreeCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.VLSBMLTreeCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;
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
public class NetworkEditorTool extends javax.swing.JInternalFrame implements TableModelListener {
	// Class/instance attributes -
	private static NetworkEditorTool _this = null;
	private ImageIcon _imgIconOff = null;
	private ImageIcon _imgIconOn = null;
	private ImageIcon _imgIconCurrent = null;
	private VLDialogGlassPane _glassPane = null;
	private VLJTable _rxnTable = null;
	private IVLTableCellEditor _tblCellEditor = null;
	private SBMLTreePropertiesTableModel _tableModel = new SBMLTreePropertiesTableModel();
	private VLSBMLJTreeCellRenderer _sbmlTreeRenderer = new VLSBMLJTreeCellRenderer();
	private static int openFrameCount=1;
	private static final int xOffset=50;
	private static final int yOffset=50;
	private UEditorSession _session = (Launcher.getInstance()).getSession();

	// Objects for the dialog -
	private JComponent sheet;
	private JPanel glass;

	// Stuff associated with the SBML tree -
	private Node _vlRootTreeNode = null;
	private DefaultMutableTreeNode _guiRoot = null;
	private Vector<String> _vecSpecies = new Vector();
	private ArrayList<String> aList = new ArrayList<String>();

	// Create a xpFactory/xpath obj (we'll use this a zillion times -)
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();

	// static accessor method
	public static NetworkEditorTool getInstance(){
		if (_this==null){
			_this=new NetworkEditorTool();
		}
		return(_this);
	}


	public void activateSaveAsButton()
	{
		jButton1.setEnabled(true);
	}

	/** Creates new form NetworkEditorTool */
	private NetworkEditorTool() {
		// Call to super
		super("SBML Network Editor v1.0",false,true);

		// set the namespace on the xpath -
		_xpath.setNamespaceContext(new SBMLNamespaceContext());

		// setup the list of containers -
		// These are container labels - 
		aList.add("sbml");
		aList.add("model");
		aList.add("listOfSpecies");
		aList.add("listOfParameters");
		aList.add("listOfCompartments");
		aList.add("listOfUnits");
		aList.add("listOfUnitDefinitions");
		aList.add("listOfParameters");
		aList.add("unitDefinition");
		aList.add("listOfReactions");
		aList.add("listOfRules");
		aList.add("notes");
		aList.add("listOfReactants");
		aList.add("listOfProducts");
		aList.add("reaction");
		aList.add("math");
		aList.add("kineticLaw");

		// iterate window count
		++openFrameCount;

		// Set window size
		setSize(300,300);

		// Set the windows location
		setLocation(xOffset*openFrameCount,yOffset*openFrameCount);

		// Set TitleBar color when active/inactive
		setDoubleBuffered(true);

		// initialize all the components -
		initComponents();

		// Add a focus/click listerner -
		this.addInternalFrameListener(new NetworkToolFocusListener());

		// Add a drag gesture listener to the palette labels -
		VLResizeGlassPane.registerFrame(this);
		VLMoveGlassPane.registerFrame(this);

		// Glass pane for dialog messages -
		_glassPane = new VLDialogGlassPane(this);

		// Configure the reaction table -
		configurePropertiesTable();

		// Set some properties on the text label -
		jTextField1.putClientProperty("JTextField.variant", "search");

		// Register the TableCellEditor -
		_tblCellEditor = (IVLTableCellEditor) new NetworkTableCellEditor();
		_rxnTable.setVLTableCellEditor(_tblCellEditor);

		// Add the renderer to the sbml tree -
		jTree1.setCellRenderer(_sbmlTreeRenderer);

	}

	public DefaultMutableTreeNode getTreeRoot()
	{
		return(_guiRoot);
	}

	// Set the root node of the XML tree -
	public void setRootNode(Document doc,String strNetwork) throws Exception
	{
		// Clear out the tree -
		jTree1.removeAll();
		Node rootNode = null;

		// Get the model node and its attributes -
		// Ok, sometimes this node is null because the namespace is incorrect - super hack...need to think about this
		Vector<String> vecNameSpaces = new Vector<String>();
		vecNameSpaces.add("/sbml:sbml");
		vecNameSpaces.add("/sbml21:sbml");
		vecNameSpaces.add("/sbml22:sbml");
		vecNameSpaces.add("/sbml23:sbml");
		vecNameSpaces.add("/sbml24:sbml");
		vecNameSpaces.add("/sbml25:sbml");
		
		int NOFNAMES = vecNameSpaces.size();
		for (int index=0;index<NOFNAMES;index++)
		{
			String expression = vecNameSpaces.get(index);
			rootNode = (Node)_xpath.evaluate(expression, doc, XPathConstants.NODE);
			
			if (rootNode!=null)
			{
				this._vlRootTreeNode = rootNode;
				_guiRoot = populateJTree(_vlRootTreeNode);

				// add to the tree -
				DefaultTreeModel mod = new DefaultTreeModel (_guiRoot);
				jTree1.setModel (mod);

				// We need the table model to update when I clisk a node -
				jTree1.addTreeSelectionListener(_tableModel);
				jTree1.addKeyListener(new NewSBMLTreeLeafKeyAdaptor());

				// I need to see if
				_tableModel.fireTableDataChanged();

				// Add the tree to the scroll pane -
				jScrollPane1.setViewportView(jTree1);
				
				break;
			}
		}
	}

	// Set the root node of the XML tree -
	public void setRootNode(Document doc, int level) throws Exception
	{
		// Clear out the tree -
		jTree1.removeAll();

		// Get the model node and its attributes -
		String expression = "/sbml:sbml";

		Node rootNode = (Node)_xpath.evaluate(expression, doc, XPathConstants.NODE);
		this._vlRootTreeNode = rootNode;
		_guiRoot = populateJTree(_vlRootTreeNode);

		// add to the tree -
		DefaultTreeModel mod = new DefaultTreeModel (_guiRoot);
		jTree1.setModel (mod);

		// We need the table model to update when I clisk a node -
		jTree1.addTreeSelectionListener(_tableModel);
		jTree1.addKeyListener(new NewSBMLTreeLeafKeyAdaptor());

		// I need to see if
		_tableModel.fireTableDataChanged();

		// Add the tree to the scroll pane -
		jScrollPane1.setViewportView(jTree1);
		jTree1.expandRow(0);  
		jTree1.expandRow(level-1);
		jTree1.setSelectionRow(level);
	}

	public JTree getTree()
	{
		return(jTree1);
	}


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
				vlNode.setProperty("EDITABLE","FALSE");
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
					System.out.println("not null leadNode named "+strNodeName+" has id = "+strAttributeID);

				}
				else
				{
					System.out.println("leadNode named "+strNodeName+" has id = null");
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
				vlNode.setProperty("EDITABLE","true");

				// System.out.println("Leaf node = "+strNodeName+" is editable? "+leafNode.getNodeValue());

				// ok -- hard code some properties -
				vlNode.setProperty("KEYNAME",strNodeName);
				vlNode.setProperty(strNodeName,strNodeValue);

				//_tableModel.setValueAt(strNodeName, 0, 0);
				//_tableModel.setValueAt(strNodeValue, 0, 1);
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




	private void configurePropertiesTable()
	{
		// Instantiate the table -
		_rxnTable = new VLJTable();

		// Add the table model -
		_rxnTable.setModel(_tableModel);
		_tableModel.addTableModelListener(this);

		// Add to the scroll panel -
		jScrollPane2.setViewportView(_rxnTable);
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
		quickSaveButton = new javax.swing.JButton();
		jSplitPane1 = new javax.swing.JSplitPane();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTree1 = new javax.swing.JTree();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTextField1 = new javax.swing.JTextField();

		setClosable(true);
		setIconifiable(true);
		setResizable(true);


		// setup the quick save button -
		quickSaveButton.setIcon(new javax.swing.ImageIcon("./images/ShoppingCart-18-Grey.png")); // NOI18N
		quickSaveButton.setToolTipText("Save this properties file ... ");
		quickSaveButton.setBorderPainted(false);
		quickSaveButton.setEnabled(true);
		//jButton5.setVisible(false);
		quickSaveButton.setMaximumSize(new java.awt.Dimension(49, 45));
		quickSaveButton.setMinimumSize(new java.awt.Dimension(49, 45));
		quickSaveButton.setPreferredSize(new java.awt.Dimension(49, 45));
		quickSaveButton.setRolloverEnabled(true);
		quickSaveButton.setRolloverIcon(new javax.swing.ImageIcon("./images/ShoppingCart-18.png")); // NOI18N
		quickSaveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveThisFile(evt);
			}
		});


		jButton1.setText("Save As ...");
		jButton1.setDoubleBuffered(true);
		jButton1.setEnabled(false);
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveSBMLFileToDisk(evt);
			}
		});

		jButton2.setText("Load ...");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loadNetworkFile(evt);
			}
		});

		jButton3.setText("New");
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buildNewNetworkFile(evt);
			}
		});


		jSplitPane1.setDividerLocation(300);
		jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

		javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
		jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
		jTree1.setRootVisible(false);
		jTree1.setShowsRootHandles(true);
		jScrollPane1.setViewportView(jTree1);

		jSplitPane1.setTopComponent(jScrollPane1);
		jSplitPane1.setRightComponent(jScrollPane2);

		jTextField1.setText("Search ...");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGap(24, 24, 24)
						.addComponent(quickSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
						.addGap(12, 12, 12))
						.addGroup(layout.createSequentialGroup()
								.addGap(176, 176, 176)
								.addComponent(jButton3)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jButton2)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jButton1)
								.addContainerGap(16, Short.MAX_VALUE))
								.addGroup(layout.createSequentialGroup()
										.addGap(16, 16, 16)
										.addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(21, Short.MAX_VALUE))
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGap(9, 9, 9)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(quickSaveButton)
								.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jButton3)
										.addComponent(jButton2)
										.addComponent(jButton1))
										.addContainerGap(33, Short.MAX_VALUE))
		);

		pack();

		/*
        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(281, 281, 281)
                .add(jButton3)
                .add(5, 5, 5)
                .add(jButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1)
                .add(18, 18, 18))
            .add(jPanel1Layout.createSequentialGroup()
                .add(14, 14, 14)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(quickSaveButton)
                	.add(org.jdesktop.layout.GroupLayout.LEADING, jTextField1)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(quickSaveButton)
                .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 443, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton3)
                    .add(jButton2)
                    .add(jButton1))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(quickSaveButton, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
            		.add(quickSaveButton)
            		.add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();*/
	}// </editor-fold>


	// Dump this file to disk (overwrite) -
	private void saveThisFile(ActionEvent evt) {
		// Create a new action instance -
		DirectSaveSBMLFileAction saveSBMLFile = new DirectSaveSBMLFileAction();

		// Ok, so I need to get the file has been selected -
		File selectedFile = (File)_session.getProperty("LOCAL_SELECTED_FILE");
		evt.setSource(selectedFile);
		saveSBMLFile.actionPerformed(evt);
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

	private void loadNetworkFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadNetworkFile
		// Ok, when I get here -- I need to load a file with an action that is
		// dependent upon the jcombobox -

		// Method attributes -
		LoadSBMLTreeAction action = new LoadSBMLTreeAction();
		action.actionPerformed(evt);

		// Enable the save as button -
		jButton1.setEnabled(true);

	}//GEN-LAST:event_loadNetworkFile

	// This gets called when we need to load a *new* file -
	private void buildNewNetworkFile(java.awt.event.ActionEvent evt)
	{
		NewSBMLTreeAction action = new NewSBMLTreeAction();
		action.actionPerformed(evt);

		// Enable the save as button -
		jButton1.setEnabled(true);
	}

	private void saveSBMLFileToDisk(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSBMLFileToDisk
		SaveSBMLFileAction saveFile = new SaveSBMLFileAction();
		saveFile.actionPerformed(evt);
	}//GEN-LAST:event_saveSBMLFileToDisk

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
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton quickSaveButton;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JTree jTree1;
	private javax.swing.JTextField jTextField1;
	// End of variables declaration//GEN-END:variables

	public void tableChanged(TableModelEvent e) {

		// Ok, when I get here the table model has changed - I need to update the values in the tree -

		// Get the selected row -
		int ROW_INDEX = _rxnTable.getSelectedRow();
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
					if (tmpKeyName.equalsIgnoreCase(strArrtributeName))
					{
						// Ok, if I'm here then I have a match -- update the xml node and break out of the loop-
						attributeNode.setNodeValue(tmp);
						System.out.println("UPDATE key = "+key+" VALUE="+tmp);                		
						break;
					}
				}


				// Let's overright the old node -
				//selectedNode.setUserObject(vltnNode);
				DefaultTreeModel treeModel = (DefaultTreeModel)jTree1.getModel();
				treeModel.nodeChanged(selectedNode); 
			}
		}

	}

}
