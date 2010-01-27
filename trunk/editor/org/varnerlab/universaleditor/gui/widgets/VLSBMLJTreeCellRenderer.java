package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.w3c.dom.Node;

public class VLSBMLJTreeCellRenderer extends DefaultTreeCellRenderer {

	
	public VLSBMLJTreeCellRenderer()
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
	        if (strName.equalsIgnoreCase("compartment"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Check to see if we have an id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("id");
	        	this.setText(leafNode.getNodeValue());
	        }
	        else if (strName.equalsIgnoreCase("reaction"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Get the id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("name");
	        	this.setText(leafNode.getNodeValue());
	        	
	        }
	        else if (strName.equalsIgnoreCase("unit"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Get the id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("kind");
	        	this.setText(leafNode.getNodeValue());
	        }
	        else if (strName.equalsIgnoreCase("speciesReference"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Get the id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("species");
	        	this.setText(leafNode.getNodeValue());
	        }
	        else if (strName.equalsIgnoreCase("parameter"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Get the id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("id");
	        	this.setText(leafNode.getNodeValue());
	        }
	        else if (strName.equalsIgnoreCase("species"))
	        {
	        	// Get the node -
	        	Node xmlNode = (Node)vltnTreeNode.getProperty("XML_TREE_NODE");
	        	
	        	// Check to see if we have an id attribute -
	        	Node leafNode = xmlNode.getAttributes().getNamedItem("name");
	        	this.setText(leafNode.getNodeValue());
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
