/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JList;

import org.varnerlab.universaleditor.gui.FileTransferTool;

/**
 *
 * @author jeffreyvarner
 */
public class DeleteLocalFileKeyListener implements KeyListener {
	// Class/instance attributes -
	private JList _jList= null;
	private File[] selectedFileArr;
	private FileTransferTool windowFrame;
	
	private Component focusedComponent = null;
    private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	
    public void keyTyped(KeyEvent e) {
        System.out.println("Hey now -- ");
    }

    public void setJListReference(JList jList)
	{
		_jList = jList;
	}
    
    public void keyPressed(KeyEvent e) {

        // Check for the backspace - Not sure if this key index is apple specific ...
        if (e.getKeyCode()==KeyEvent.VK_BACK_SPACE)
        {
        	// Get the focused component and then update the list -
        	// Get the currently focused component -
	        focusedComponent = manager.getFocusOwner();
	        windowFrame = (FileTransferTool)focusedComponent.getFocusCycleRootAncestor();
        	
        	// Ok, so when I get here I'm going to delete local files?
    		Object[] objArr = _jList.getSelectedValues();
    		int NITEMS = objArr.length;
    		for (int index=0;index<NITEMS;index++)
    		{
    			File tmpFile = (File)objArr[index];
    			tmpFile.delete();
    		}
    		
    		// update the GUI =
    		windowFrame.updateSelectedDirectory();
        }
    }

    public void keyReleased(KeyEvent e) {
        System.out.println("Hey now -- ");
    }

}
