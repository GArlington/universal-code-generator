/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

// Imports -
import javax.swing.TransferHandler;
import javax.swing.JComponent;
import java.awt.event.*;


/**
 *
 * @author jeffreyvarner
 */
public class DragMouseAdapter extends MouseAdapter {

    public void mousePressed(MouseEvent evt)
    {
        JComponent c = (JComponent)evt.getSource();
        TransferHandler handler = c.getTransferHandler();
        handler.exportAsDrag(c, evt, TransferHandler.COPY);
    }

}
