/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

import org.varnerlab.universaleditor.domain.VLDomainComposite;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author jeffreyvarner
 */
public interface IVLTreeGUI {


    public void setRootNode(VLDomainComposite rootNode) throws Exception;
    public void setRootNode(Node rootNode) throws Exception;
    public void setRootNode(Document doc) throws Exception;
}
