/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

// import -
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.varnerlab.universaleditor.gui.*;

/**
 *
 * @author jeffreyvarner
 */
public class ProjectToolFocusListener implements InternalFrameListener {

    public void internalFrameOpened(InternalFrameEvent e) {
        
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        
    }

    public void internalFrameClosed(InternalFrameEvent e) {
        
    }

    public void internalFrameIconified(InternalFrameEvent e) {
        
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
        
    }

    public void internalFrameActivated(InternalFrameEvent e) {
        
        // Get the frame -
        ProjectTool intFrame = (ProjectTool)e.getInternalFrame();

        // Get the image icon for on -
        ImageIcon icon = intFrame.getOnIcon();

        // Set the on-icon -
        intFrame.setFrameIcon(icon);

    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
        // Get the frame -
        ProjectTool intFrame = (ProjectTool)e.getInternalFrame();

        // Get the image icon for on -
        ImageIcon icon = intFrame.getOffIcon();

        // Set the on-icon -
        intFrame.setFrameIcon(icon);
    }

}
