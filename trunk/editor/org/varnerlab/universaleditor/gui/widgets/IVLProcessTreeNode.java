/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import javax.swing.DefaultListModel;
import org.w3c.dom.Node;

/**
 *
 * @author jeffreyvarner
 */
public interface IVLProcessTreeNode {

    public void processNodes(Node node,DefaultListModel model);

}
