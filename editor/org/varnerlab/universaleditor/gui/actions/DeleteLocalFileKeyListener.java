/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.gui.widgets.SheetDialogFrame;
import org.w3c.dom.Document;

/**
 *
 * @author jeffreyvarner
 */
public class DeleteLocalFileKeyListener implements KeyListener,ActionListener,PropertyChangeListener  {
	// Class/instance attributes -
	private JList _jList= null;
	private File[] selectedFileArr;
	private FileTransferTool windowFrame;
	private JOptionPane optionPane;
	private SheetDialogFrame _frame;
	private File _fileLocal = null;
	
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
        	
	        // Ok, so we need to add a dialog to make sure it is ok to delete what ever I'm about to delete
	        
	        
        	// Ok, so when I get here I'm going to delete local files?
    		Object[] objArr = _jList.getSelectedValues();
    		int NITEMS = objArr.length;
    		for (int index=0;index<NITEMS;index++)
    		{
    			File tmpFile = (File)objArr[index];
    			if (tmpFile.isFile())
    			{
    				// what file is selected -
    				_fileLocal = tmpFile;
    				
    				// Pop the dialog?
    				String tmp = tmpFile.getPath();
    				String strMessage = "Delete \""+tmpFile.getName()+"\"?\nDo you really want to delete this?\n" +
    						"Deleting this file or directory can not be undone.";
    				optionPane = new JOptionPane(strMessage,JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION);
    				optionPane.addPropertyChangeListener(this);
    				JDialog jDialog = optionPane.createDialog(_frame,"Spank");
    				windowFrame.showJDialogAsSheet(jDialog);
    			}
    		}
        }
    }

    public void keyReleased(KeyEvent e) {
        System.out.println("Hey now -- ");
    }

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals (JOptionPane.VALUE_PROPERTY)) 
		{
					
			FileTransferTool windowFrame = (Launcher.getInstance()).getFileTransferToolRef();
			int value = ((Integer)optionPane.getValue()).intValue();
			
			if (value==0)
			{
			
				try {				
					// delete the file -
					_fileLocal.delete();
					
					// update the GUI =
		    		windowFrame.updateSelectedDirectory();
				}
				catch (Exception error)
				{
					error.printStackTrace();
				}
				finally
				{
					windowFrame.hideSheet();
				}
			}
			else
			{
				windowFrame.hideSheet();
			}
		}
	}

}
