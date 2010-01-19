/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

// import statements -
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.ImageIcon;


/**
 *
 * @author jeffreyvarner
 */
public class VLJPropTreeCellRenderer extends DefaultTreeCellRenderer  {


    public VLJPropTreeCellRenderer()
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
	            //imgIcon = (ImageIcon)vltnTreeNode.getProperty("OPENED_ICON");
	        }
	
	        // Get the key name for this icon -
	        String strName = (String)vltnTreeNode.getProperty("KEYNAME");
	        if (strName!=null)
	        {
	            this.setText(strName);
	        }
	        
	        this.setIcon(imgIcon);
        }
        return this;
    }


}
