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

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.varnerlab.universaleditor.domain.BCXDataGroup;
import org.varnerlab.universaleditor.domain.BCXExperiment;
import org.varnerlab.universaleditor.domain.BCXStimulus;
import org.varnerlab.universaleditor.domain.BCXSystem;
import org.varnerlab.universaleditor.domain.BCXValue;
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
import org.varnerlab.universaleditor.gui.actions.LoadSBMLTreeAction;
import org.varnerlab.universaleditor.gui.actions.SaveSBMLFileAction;
import org.varnerlab.universaleditor.gui.widgets.IVLTableCellEditor;
import org.varnerlab.universaleditor.gui.widgets.NetworkTableCellEditor;
import org.varnerlab.universaleditor.gui.widgets.NetworkToolFocusListener;
import org.varnerlab.universaleditor.gui.widgets.VLDialogGlassPane;
import org.varnerlab.universaleditor.gui.widgets.VLJTable;
import org.varnerlab.universaleditor.gui.widgets.VLJTreeCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.VLMoveGlassPane;
import org.varnerlab.universaleditor.gui.widgets.VLResizeGlassPane;
import org.varnerlab.universaleditor.gui.widgets.VLSBMLTreeCellRenderer;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *
 * @author jeffreyvarner
 */
public class NetworkEditorTool extends javax.swing.JInternalFrame implements TableModelListener {

    private ImageIcon _imgIconOff = null;
    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconCurrent = null;
    private VLDialogGlassPane _glassPane = null;
    private VLJTable _rxnTable = null;
    private IVLTableCellEditor _tblCellEditor = null;
    private XMLTreePropertiesTableModel _tableModel = new XMLTreePropertiesTableModel();


    private static int openFrameCount=1;
    private static final int xOffset=50;
    private static final int yOffset=50;

    // Stuff associated with the SBML tree -
    private VLDomainComposite _vlRootTreeNode = null;
    private DefaultMutableTreeNode _guiRoot = null;
    private Vector<String> _vecSpecies = new Vector();

    /** Creates new form NetworkEditorTool */
    public NetworkEditorTool() {
         // Call to super
        super("Network Editor Tool v1.0",false,true);

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

        // Register the TableCellEditor -
        _tblCellEditor = (IVLTableCellEditor) new NetworkTableCellEditor();
        _rxnTable.setVLTableCellEditor(_tblCellEditor);


    }

    public DefaultMutableTreeNode getTreeRoot()
    {
        return(_guiRoot);
    }

    // Set the root node of the XML tree -
    public void setRootNode(VLDomainComposite rootNode,String strNetwork) throws Exception
    {

        // Clear out the tree -
        jTree1.removeAll();

        this._vlRootTreeNode = rootNode;

        _guiRoot = populateJTree(rootNode);

        // add to the tree -
        DefaultTreeModel mod = new DefaultTreeModel (_guiRoot);
        jTree1.setModel (mod);
        jTree1.setCellRenderer(new VLSBMLTreeCellRenderer());

        // We need the table model to update when I clisk a node -
        jTree1.addTreeSelectionListener(_tableModel);
        jTree1.setRootVisible(true);


        // Add the tree to the scroll pane -
        jScrollPane1.setViewportView(jTree1);

        // Ok, if I get here I haven't exploded - then set the current network in session -
        UEditorSession session = (Launcher.getInstance()).getSession();
        session.setProperty("CURRENT_NETWORK",strNetwork);

        // Push to my bitches that I have loaded a network -
        SystemwideEventService.fireNetworkUpdateEvent();

        // Ok, so when I get here I have the species names loaded - put them in session and update anybobody listening for session changes -
        session.setProperty("SPECIES_NAME_VECTOR", this._vecSpecies);
        SystemwideEventService.fireSessionUpdateEvent();
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

        // We need to set the flag to make sure the classname is lowercase -
        vlNode.setProperty("CLASSNAME_IS_LOWERCASE", "TRUE");


        // Ok, I need to check the type of system -
        PublishService.submitData("The node is an instance of "+node);


        if (node instanceof model)
        {
            vlNode.setProperty("DISPLAY_LABEL","Model");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLImageLoader.getPNGImageIcon("VLYDisk-32-Grey.png"));
            vlNode.setProperty("OPENED_ICON", VLImageLoader.getPNGImageIcon("VLYDisk-32.png"));
        }

        if (node instanceof listOfSpecies)
        {
            vlNode.setProperty("DISPLAY_LABEL","ListOfSpecies");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));
        }

        if (node instanceof listOfReactions)
        {
            vlNode.setProperty("DISPLAY_LABEL","ListOfReactions");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));
        }

        if (node instanceof reaction)
        {
            vlNode.setProperty("DISPLAY_LABEL","Reaction");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));
            
        }

        if (node instanceof speciesReference)
        {
            vlNode.setProperty("DISPLAY_LABEL","SpeciesReference");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-ICON"));
        }

        if (node instanceof listOfReactants)
        {
            vlNode.setProperty("DISPLAY_LABEL","ListOfReactants");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());

            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));

          
        }

        if (node instanceof listOfProducts)
        {
            vlNode.setProperty("DISPLAY_LABEL","ListOfProducts");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));


            
        }

        if (node instanceof kineticLaw)
        {
            vlNode.setProperty("DISPLAY_LABEL","KineticLaw");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFUNCTION-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFUNCTION-32-ICON"));
        }

        if (node instanceof math)
        {
            vlNode.setProperty("DISPLAY_LABEL","Math");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-ICON"));
        }

        if (node instanceof apply)
        {
            vlNode.setProperty("DISPLAY_LABEL","Apply");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-ICON"));
        }

        if (node instanceof times)
        {
            vlNode.setProperty("DISPLAY_LABEL","Times");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-ICON"));
        }

        if (node instanceof ci)
        {
            vlNode.setProperty("DISPLAY_LABEL","CI");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-ICON"));
        }

        if (node instanceof listOfParameters)
        {
            vlNode.setProperty("DISPLAY_LABEL","ListOfParameters");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLFOLDERSBML-32-ICON"));
        }

        if (node instanceof parameter)
        {
            vlNode.setProperty("DISPLAY_LABEL","Parameter");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLDOCUMENT-32-ICON"));
        }


        if (node instanceof species)
        {
            vlNode.setProperty("DISPLAY_LABEL","Species");
            vlNode.setProperty("CLASSNAME", node.getClass().getName());
            vlNode.setProperty("CLOSED_ICON", VLIconManagerService.getIcon("VLSPECIES-32-GREY-ICON"));
            vlNode.setProperty("OPENED_ICON", VLIconManagerService.getIcon("VLSPECIES-32-ICON"));

            // Ok, so I need to get some props from the species -
            String strNameLocal = (String)vlNode.getProperty("NAME");
            this._vecSpecies.addElement(strNameLocal);
        }

        // OK, when I get here I have a configured vlNode - add it to the GUI node -
        guiNode.setUserObject(vlNode);

        // Ok, we need to handle the kids -
        int NUMBER_OF_KIDS = node.getNumberOfChildren();

        PublishService.submitData("Number of kids - "+NUMBER_OF_KIDS);

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
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();

        setClosable(true);
        setIconifiable(true);

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

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.setRootVisible(false);
        jTree1.setShowsRootHandles(true);
        jScrollPane1.setViewportView(jTree1);

        jSplitPane1.setTopComponent(jScrollPane1);
        jSplitPane1.setRightComponent(jScrollPane2);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(265, 265, 265)
                .add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton1)
                .add(5, 5, 5))
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jButton2)
                    .add(jButton3)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loadNetworkFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadNetworkFile
        // Ok, when I get here -- I need to load a file with an action that is
        // dependent upon the jcombobox -

        // Method attributes -
        LoadSBMLTreeAction action = new LoadSBMLTreeAction();
        action.actionPerformed(evt);

        // Enable the save as button -
        jButton1.setEnabled(true);

    }//GEN-LAST:event_loadNetworkFile

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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    public void tableChanged(TableModelEvent e) {
    
        // Ok, when I get here the table model has changed - I need to update the values in the tree -

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

                // Set the properties of the object -
                vltnNode.setProperty(key.toString(), val.toString());

                System.out.println("UPDATE key = "+key+" VALUE="+val);

                // Let's overright the old node -
                // selectedNode.setUserObject(vltnNode);
                DefaultTreeModel treeModel = (DefaultTreeModel)jTree1.getModel();
                treeModel.nodeChanged(selectedNode);
            }
        }

    }

}
