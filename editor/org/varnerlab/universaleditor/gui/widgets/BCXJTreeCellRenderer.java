package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.w3c.dom.Node;

public class BCXJTreeCellRenderer extends DefaultTreeCellRenderer {

	
	public BCXJTreeCellRenderer()
	{
		super();
        setSize(120,120);
	}
	
	@Override
    public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus)
    {
        ImageIcon imgIcon = null;

        // Call the super -
        super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);


        // Ok, so I need to set the icon depending on node type -
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        tree.setRowHeight(25);

        // Get the VLTreeNode from the GUI wrapper -
        VLTreeNode vltnTreeNode = (VLTreeNode)node.getUserObject();
        
        if (vltnTreeNode!=null)
        {
        
	        if (sel)
	        {
	            imgIcon = (ImageIcon)vltnTreeNode.getProperty("OPENED_ICON");
	        }
	        else
	        {
	            imgIcon = (ImageIcon)vltnTreeNode.getProperty("CLOSED_ICON");
	        }
	
	        // ok, the keyname for this tree element is a type. So we can make rendering decisions based on this value -
	        String strName = (String)vltnTreeNode.getProperty("KEYNAME");
	        
	        // Render the compartments -
	        if (strName.equalsIgnoreCase("experiment"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Check to see if we have an id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("id");
	        	this.setText(leafNode.getNodeValue());
	        	
	        	// reset the icon to make it selection specific -
	        	if (sel)
		        {
		            imgIcon = VLIconManagerService.getIcon("BEAKER-12-ICON");
		        }
		        else
		        {
		            imgIcon = VLIconManagerService.getIcon("BEAKER-12-GREY-ICON");
		        }
	        	
	        }
	        else if (strName.equalsIgnoreCase("data_point"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Check to see if we have an id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("id");
	        	this.setText(leafNode.getNodeValue());
	        	
	        	// reset the icon to make it selection specific -
	        	if (sel)
		        {
		            imgIcon = VLIconManagerService.getIcon("EVALUE-12-ICON");
		        }
		        else
		        {
		            imgIcon = VLIconManagerService.getIcon("EVALUE-12-GREY-ICON");
		        }
	        	
	        }
	        else if (strName.equalsIgnoreCase("measurement_file"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Check to see if we have an id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("id");
	        	this.setText(leafNode.getNodeValue());
	        	
	        	// reset the icon to make it selection specific -
	        	if (sel)
		        {
		            imgIcon = VLIconManagerService.getIcon("EGROUP-12-ICON");
		        }
		        else
		        {
		            imgIcon = VLIconManagerService.getIcon("EGROUP-12-GREY-ICON");
		        }
	        	
	        }
	        else if (strName.equalsIgnoreCase("data_column"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Check to see if we have an id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("id");
	        	this.setText(leafNode.getNodeValue());
	        	
	        	// reset the icon to make it selection specific -
	        	if (sel)
		        {
		            imgIcon = VLIconManagerService.getIcon("FLASH-12-ICON");
		        }
		        else
		        {
		            imgIcon = VLIconManagerService.getIcon("FLASH-12-GREY-ICON");
		        }
	        	
	        }
	        else if (strName.equalsIgnoreCase("stimulus"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Check to see if we have an id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("id");
	        	this.setText(leafNode.getNodeValue());
	        	
	        	// reset the icon to make it selection specific -
	        	if (sel)
		        {
		            imgIcon = VLIconManagerService.getIcon("PURPLE-12-ICON");
		        }
		        else
		        {
		            imgIcon = VLIconManagerService.getIcon("PURPLE-12-GREY-ICON");
		        }
	        	
	        }
	        else if (strName.equalsIgnoreCase("listOfExperiments"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Get the id attribute -
	        	this.setText(xmlNode.getNodeName());
	        }
	        else if (strName.equalsIgnoreCase("server_options"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Get the id attribute -
	        	this.setText(xmlNode.getNodeName());
	        }
	        else
	        {
	        	// Get the key name for this icon -
		        if (strName!=null)
		        {
		            this.setText(strName);
		        }
	        }
	        	        
	        this.setIcon(imgIcon);
        }
        return this;
    }
	
}
