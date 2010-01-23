/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

// import -
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.service.FileSystemService;

/**
 *
 * @author jeffreyvarner
 */
public class TransferToolFocusListener implements InternalFrameListener {
	// Class/instance -
	private Hashtable _propTable = new Hashtable();
	
	public void setProperty(Object key,Object value)
	{
		_propTable.put(key, value);
	}
	
	public Object getProperty(Object key)
	{
		return(_propTable.get(key));
	}
	
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
        FileTransferTool intFrame = (FileTransferTool)e.getInternalFrame();

        // Get the image icon for on -
        ImageIcon icon = intFrame.getOnIcon();

        // Set the on-icon -
        intFrame.setFrameIcon(icon);
        
        // Get the combo box -
        JComboBox jcb = (JComboBox)getProperty("LOCAL_COMBOBOX");
        File selected_file = (File)jcb.getSelectedItem();
        
        // Populate the JList w/the current directory -
        Vector<File> _vecDir = new Vector();
        
        // Get the list model -
        DefaultListModel _listModelJList1 = (DefaultListModel)getProperty("LOCAL_JLIST_MODEL");
        _listModelJList1.clear();
        
        FileSystemService.getFileFromDir(selected_file, _vecDir);
        int NUMBER_OF_ELEMENTS = _vecDir.size();
        for (int pindex=0;pindex<NUMBER_OF_ELEMENTS;pindex++)
        {
            _listModelJList1.addElement(_vecDir.get(pindex));
        }

    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
        // Get the frame -
        FileTransferTool intFrame = (FileTransferTool)e.getInternalFrame();

        // Get the image icon for on -
        ImageIcon icon = intFrame.getOffIcon();

        // Set the on-icon -
        intFrame.setFrameIcon(icon);

        intFrame.repaint();
    }

}
