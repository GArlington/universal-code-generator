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


import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jeffreyvarner
 */
public class ModelCodeGeneratorFileEditor extends javax.swing.JInternalFrame implements IVLTreeGUI,IVLSystemwideEventListener,TableModelListener {
    // Class/instance attributes -
    private static ModelCodeGeneratorFileEditor _this = null;
	static int openFrameCount=1;
    static final int xOffset=50;
    static final int yOffset=50;

    private ImageIcon _imgIconOff = null;
    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconCurrent = null;

    private File _propFile = null;
    private String _strWorkingDir = "";

    // Stuff associated with the properties tree -
    private Node _vlRootTreeNode = null;
    private DefaultMutableTreeNode _guiRoot = null;
    private JTree jTree1 = null;
    private UEditorSession _session = null;

    private VLJTable _propTable = null;
    private XMLTreePropertiesTableModel _tableModel = new XMLTreePropertiesTableModel();
    private ModelPropertiesFileTableCellEditor _tableCellEditor = new ModelPropertiesFileTableCellEditor();
    private VLPropEditorComboBoxRenderer _comboBoxRenderer = new VLPropEditorComboBoxRenderer();
    private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
	private ArrayList<String> _aList = new ArrayList<String>();

	// static accessor method
    public static ModelCodeGeneratorFileEditor getInstance(){
        if (_this==null){
            _this=new ModelCodeGeneratorFileEditor();
        }
        return(_this);
    }
	

    /** Creates new form ModelCodeGeneratorFileEditor */
    private ModelCodeGeneratorFileEditor() {
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

        // Grab the session -
        _session = (Launcher.getInstance()).getSession();
        
        // Initialize this mofo -
        initComponents();

        // Config the table -
        configurePropTable();
        
        // Configure jComboBox model output type -
        configureComboBox();

        // Set some properties on the text label -
        jTextField1.putClientProperty("JTextField.variant", "search");


        // Register listeners -
        //this.addFocusListener(new ModelCodeGeneratorFileToolFocusListener());
        this.addInternalFrameListener(new ModelCodeGeneratorFileToolFocusListener());

        // Register me as a session listner =
        SystemwideEventService.registerSessionListener(this);
        SystemwideEventService.registerNetworkListener(this);
 
        // Set the working dir -
        this.updateUserObjectTree();

        // Set -
        //_tableCellEditor.setDocumentTree((Document)_session.getProperty("PROPERTY_TABLE_TREE"));
        _propTable.setVLTableCellEditor(_tableCellEditor);
        
        // configure the container list -
        // configureContainerList();
    }

    private void configureContainerList(Document doc)
    {
        String strXPath="//ContainerTags/tag/text()";
        
        try {
			// Get the item of this type and tag -
 	   		NodeList propNodeList = (NodeList) _xpath.evaluate(strXPath, doc, XPathConstants.NODESET);
			
			// How many?
 	   		int NUMBER_OF_ITEMS = propNodeList.getLength();
 	   		System.out.println("Searching tree with xpath = "+strXPath+" returned "+NUMBER_OF_ITEMS+" items");
 	   		
 	   		for (int index=0;index<NUMBER_OF_ITEMS;index++)
 	   		{
 	   			Node tmpNode = propNodeList.item(index);
 	   			String strName = tmpNode.getNodeValue();
 	   			
 	   			// Add to combobox -
 	   			_aList.add(strName);
 	   		}
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR: Property lookup failed. The following XPath "+strXPath+" resuled in an error - "+error.toString());
		}
    }
    
    private void configureComboBox() {
		// Ok, so we need to get options in the combo box -
    	
    	// clear out the entries on the combo box -
    	jComboBox1.removeAllItems();
    	
    	// set the renderer -
    	jComboBox1.setRenderer(_comboBoxRenderer);
    	
    	// Ok, get the template dom tree from session -
    	Document doc = (Document)_session.getProperty("TEMPLATE_DOM_TREE");
    	
    	// Set the doc root so I can run x-path calls in the renderer -
    	_comboBoxRenderer.setDocumentRoot(doc);
    	
    	// get the mapping information from the dom tree using xpath -
    	XPath xpath = XPathFactory.newInstance().newXPath();
    	String expression = "/Template/mapping/display/@name";
    	
    	try {
    		
    		NodeList nodeList = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
    		int NUMBER_OF_KIDS = nodeList.getLength();
    		for (int index=0;index<NUMBER_OF_KIDS;index++)
    		{
    			Node tmpNode = nodeList.item(index);
    			jComboBox1.addItem(tmpNode.getNodeValue());
    		}
    		
    	}
    	catch (Exception error)
    	{
    		System.out.println("ERROR: No bueno on getting the template data from the DOM tree. Sad face. "+error.toString());
    	}
    	
    	/*
    	// add the output types -
    	jComboBox1.addItem("Sundials C-code (*.cc)");
    	jComboBox1.addItem("Octave M-code (*.m)");
    	jComboBox1.addItem("Octave C-code (*.cc)");
    	jComboBox1.addItem("Matlab M-code (*.m)");
    	jComboBox1.addItem("GSL C-code (*.c)");
    	jComboBox1.addItem("Scilab M-code (*.m)");
    	jComboBox1.addItem("SMBL file (*.sbml)");
    	jComboBox1.addItem("Varnerlab flat-file (*.net)");
    	jComboBox1.addItem("Graphviz dot-file (*.dot)");
    	*/
    	
    	
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

    public void setRootNode(Document doc) throws Exception
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
        
        // Get the tablemodel -
        //XMLTreePropertiesTableModel _tableModel = (XMLTreePropertiesTableModel)_propTable.getModel();
        
        // OK, set lets grab the rootNode from the dom tree -
        XPath xpath = XPathFactory.newInstance().newXPath();
    	String expression = "/Template/Model";
    	Node rootNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
        this._vlRootTreeNode = rootNode;
        _guiRoot = populateJTree(_vlRootTreeNode);

        // add to the tree -
        DefaultTreeModel mod = new DefaultTreeModel (_guiRoot);
        jTree1.setModel (mod);
        jTree1.setCellRenderer(new VLJPropTreeCellRenderer());

        // We need the table model to update when I clisk a node -
        jTree1.addTreeSelectionListener(_tableModel);

        // I need to see if
        _tableModel.fireTableDataChanged();
        
        // Add the tree to the scroll pane -
        jScrollPane1.setViewportView(jTree1);
    }

    private DefaultMutableTreeNode populateJTree(Node xmlNode) throws Exception
    {
    	
    	// OK, starting w/the root node I need to populate the JTree -
        DefaultMutableTreeNode guiNode = new DefaultMutableTreeNode();

        // Create a VarnerLab node wrapper -
        VLTreeNode vlNode = new VLTreeNode();
           	
		/*
        // Super stupid - list the containers -
        aList.add("Model");
        aList.add("EdgeProperties");
        aList.add("NodeProperties");
        aList.add("TerminalNodeProperties");
        aList.add("GradientProperties");
        aList.add("NetworkFileName");
        aList.add("default_color");
        aList.add("NetworkFileName");
        aList.add("SymbolFileName");
        aList.add("OutputDOTFileName");
        aList.add("OutputFileName");
        aList.add("DataFile");
        aList.add("MassBalanceFunction");
        aList.add("DriverFile");
        aList.add("StoichiometricMatrix");
        aList.add("BoundsVector");
        aList.add("ProjectLayout");
        aList.add("InitialConditionFileName");
        aList.add("KineticParametersFileName");
        aList.add("OrderFileName");
        */
        
        // Get some info from the xmlNode -
        String strNodeName = (String)xmlNode.getNodeName();
        String strNodeValue = (String)xmlNode.getTextContent();
        String strContent = xmlNode.getTextContent();
        
        // Get the editable -
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//@editable";
        Node editNode = null;
        
        try {
        	 editNode = (Node) xpath.evaluate(expression,xmlNode, XPathConstants.NODE);
        }
        catch (Exception error)
        {
        	System.out.println("ERROR: No editable flag or we are nor able to get the attribute? "+error.toString());
        }
        
        String strEditable = editNode.getNodeValue();
        
        System.out.println("Node = "+strNodeName+" is editable? "+strEditable);
        
        int COMMENT = strNodeName.indexOf("#");
        if (COMMENT==-1)
        {
	        if (strNodeValue==null)
	        {
	        	strNodeValue = "";
	        }
	        
	        // Ok, so when I get here I have the name and value -
	        // Configure the node -
	        
	        
	        if (_aList.contains(strNodeName))
	        {
	        	vlNode.setProperty("DISPLAY_LABEL",strNodeName);
	        	vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
	        	vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));
	        	vlNode.setProperty("KEYNAME",strNodeName);
	        	vlNode.setProperty("EDITABLE",strEditable);
	        }
	        else
	        {
	        	vlNode.setProperty("DISPLAY_LABEL",strNodeName);
	        	vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-GREY-ICON"));
	        	vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLPROPERTY-32-ICON"));
	        	
	        	// Get editable flag -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("editable");        	
	        	
	        	// Set the editable flag -
	        	vlNode.setProperty("EDITABLE",leafNode.getNodeValue());
	        	
	        	System.out.println("Leaf node = "+strNodeName+" is editable? "+leafNode.getNodeValue());
	        	
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
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

        jButton4.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Control-panel-12-Grey.png")); // NOI18N
        jButton4.setToolTipText("Test this properties file...");
        jButton4.setBorderPainted(false);
        jButton4.setDoubleBuffered(true);
        jButton4.setEnabled(false);
        jButton4.setRolloverEnabled(true);
        jButton4.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Control-panel-12.png")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testThisPropertiesFile(evt);
            }
        });

        
        jButton5.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Control-panel-12-Grey.png")); // NOI18N
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

        jButton6.setIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Newspaper-Feed-12-Grey.png")); // NOI18N
        jButton6.setToolTipText("Create new property file template");
        jButton6.setBorderPainted(false);
        jButton6.setDoubleBuffered(true);
        jButton6.setEnabled(true);
        jButton6.setRolloverEnabled(true);
        jButton6.setRolloverIcon(new javax.swing.ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/Newspaper-Feed-12.png")); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewTree(evt);
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
    }// </editor-fold>


    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        System.out.println("Focus gained");
    }//GEN-LAST:event_formFocusGained

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        System.out.println("Focus lost");
    }//GEN-LAST:event_formFocusLost

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        
    }//GEN-LAST:event_formMouseClicked
    
    private void createNewTree(java.awt.event.ActionEvent evt) {
    	// Ok, so now we need to open a template that is dependent upon the selected item -
    	String strSelectedItem = (String)jComboBox1.getSelectedItem();
    	String strPath = "";
    	
		try {
			// Get the template DOM tree -
	    	Document template_tree = (Document)_session.getProperty("TEMPLATE_DOM_TREE");
	    	
	    	// Set the table cell editor -
	    	_tableCellEditor.setCurrentOutputType(strSelectedItem);
			
			// Get the template file from the DOM tree -
			XPath xpath = XPathFactory.newInstance().newXPath();
	    	String expression_filename = "//mapping/display[@name=\""+strSelectedItem+"\"]/following-sibling::filename/text()";
	    	
	    	Node filenameNode = (Node) xpath.evaluate(expression_filename, template_tree, XPathConstants.NODE);
	    	String strTmpFileName = filenameNode.getNodeValue();
			
			// Get the working dir for this file from the DOM tree -
	    	String expression_working_dir = "//mapping/display[@name=\""+strSelectedItem+"\"]/parent::mapping/@working_dir";
	    	Node workingDirNode = (Node) xpath.evaluate(expression_working_dir, template_tree, XPathConstants.NODE);
	    	String strTmpWorkingDir = workingDirNode.getNodeValue();
	    	
	    	// Formulate the path string -
	    	strPath = strTmpWorkingDir+strTmpFileName;
	    	
			// load the xml file -
			File configFile = new File(strPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  	  		Document doc = dBuilder.parse(configFile);
  	  		doc.getDocumentElement().normalize();
  	  		
  	  		// Ok, so we have the correct file loaded - let's populate some stuff that we'll need later.
  	  		// First, let's do _aList
  	  		configureContainerList(doc);
  	  		
  	  		// Second, we need to populate the options list for the drop downs -
  	  		_tableCellEditor.setDocumentTree(doc);
  	  		
  	  		// Ok, so now we the doc, try to create a tree -
  	  		setRootNode(doc);
  	  		
  	  		// Ok, so I've loaded the tree - allow the user to save it back out again -
  	  		jButton1.setEnabled(true);
  	  		
		}
		catch (Exception error)
		{
			error.printStackTrace();
			System.out.println("ERROR: We have an issue with loading the Template JTree - "+error.toString());
		}
  	  	
  	  	// Ok -- I have the DOMTree for the template file in memory, need to make the corresponding GUI tree w/a pointer to DOMTree node.
    	
    }

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

    private void loadNewPropFileAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadNewPropFileAction
        // TODO add your handling code here:
        NewPropertiesFileListAction newPropListAction = new NewPropertiesFileListAction();
        newPropListAction.actionPerformed(evt);
        
    }//GEN-LAST:event_loadNewPropFileAction

    private void testThisPropertiesFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewProperty
        // TODO add your handling code here:
        // AddNewPropertyLeafAction action = new AddNewPropertyLeafAction();
        // action.actionPerformed(evt);
    	
    	// need an action to check to see if all the required fields have been filled -
    	CheckPropertiesFileAction action = new CheckPropertiesFileAction();
    	action.setToolReference(this);
    	action.actionPerformed(evt);	
    	
    }//GEN-LAST:event_createNewProperty

    private void deletePropertyNode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePropertyNode
        // TODO add your handling code here:
        DeletePropertyNodeAction action = new DeletePropertyNodeAction();
        action.actionPerformed();
    }//GEN-LAST:event_deletePropertyNode

    
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
    // Variables declaration - do not modify
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
        
        PublishService.submitData("Updating the table- ");

        // Ok update the table w/the stuff in the tree -
        
        
        // Ok, if we have both of these mofo's I can create a working DIR -
        if (strUserName!=null && strRemotePath!=null)
        {
            // If I get here then I can set the working dir -

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
                    if (keyName.equalsIgnoreCase("WORKING_DIRECTORY"))
                    {
                        userObj.setProperty(keyName,strRemotePath);
                    }
                    else if (keyName.equalsIgnoreCase("USERNAME"))
                    {
                    	userObj.setProperty(keyName,strUserName);
                    }
                    else if (keyName.equalsIgnoreCase("DATE_CREATED"))
                    {
                    	userObj.setProperty(keyName,strObjDate);
                    }
                    else if (keyName.equalsIgnoreCase("VERSION"))
                    {
                    	String strVer = (String)userObj.getProperty(keyName);
                    	if (!strVer.isEmpty())
                    	{
                    		double dblVer = (Double.parseDouble(strVer))+0.1;
                    		userObj.setProperty(keyName,String.valueOf(dblVer));
                    	}
                    	
                    }
                }
             }
        }
    }

    public void updateNetwork() {
    	 // Get the new session -
        _session = (Launcher.getInstance()).getSession();

        System.out.println("Rcvd a network update event...");
        
        // Set the working dir -
        this.updateUserObjectTree();
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

	public void setRootNode(VLDomainComposite rootNode) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void setRootNode(Node rootNode) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
