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
public class VLSBMLTreeCellRenderer extends DefaultTreeCellRenderer  {


    public VLSBMLTreeCellRenderer()
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

        tree.setRowHeight(22);

        // Get the VLTreeNode from the GUI wrapper -
        VLTreeNode vltnTreeNode = (VLTreeNode)node.getUserObject();
        
        if (sel)
        {
            imgIcon = (ImageIcon)vltnTreeNode.getProperty("OPENED_ICON");
        }
        else
        {
            imgIcon = (ImageIcon)vltnTreeNode.getProperty("CLOSED_ICON");
        }

         // Ok, now lets set the text -
        String strTmp = (String)vltnTreeNode.getProperty("NAME");
        

        // Set the text and the icon -
        if (strTmp!=null)
        {



            this.setText(strTmp);
        }

        
        this.setIcon(imgIcon);
        return this;
    }


}
