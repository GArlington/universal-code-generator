package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NewDirectoryDialog;
import org.varnerlab.universaleditor.gui.widgets.SheetDialogFrame;
import org.varnerlab.universaleditor.service.SystemwideEventService;


public class FileTransferJPopupMenuActionListener implements ActionListener {
	// class/instance attributes -
	private JComboBox _jComboBox = null;
	private Component focusedComponent = null;
    private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    private FileTransferTool windowFrame;
    private SheetDialogFrame _frame;
    private JOptionPane optionPane;
	
	public void setJComboBoxReference(JComboBox jComboBox)
	{
		_jComboBox = jComboBox;
	}
	
	public void actionPerformed(ActionEvent e) {
	
		// Ok, so when I get here I need to create a new folder in the local file system -
		File selectedDir = (File)_jComboBox.getSelectedItem();
		
		// Ok, what file did I select?
		System.out.println("What file was selected - "+selectedDir);
		
		try 
		{
			// Get the currently focused component - file transfer tool -
	        Launcher _main = Launcher.getInstance();
	        windowFrame = _main.getFileTransferToolRef();
	        
	        NewDirectoryDialog dialog = new NewDirectoryDialog();
	        dialog.setWorkingDirectory(selectedDir.getAbsolutePath());
	        dialog.setParentFrame(windowFrame);
			//dialog.setVisible(true);
	        
	        // Prompt the user to enter a new local directory name -
	        //String dirName = optionPane.showInputDialog(windowFrame, "New local directory name?"));
	        //JDialog dialog = optionPane.createDialog (_frame, "irrelevant");
	        //windowFrame.showJDialogAsSheet (dialog);
	        windowFrame.showCustomPinnedDialog(dialog);
		}
		catch (Exception error)
		{
			// we should log this -
			System.out.println("Error in "+this.getClass().getName()+" "+error.toString());
		}
		
		
		
		/* v 1.0 - this just pops up a dialog. We want the dialog to popup on the FileTransferTool window (pinned to window)
		// Ok, so now I need to load the dialog box to capture the name of the dir -
		NewDirectoryDialog dialog = new NewDirectoryDialog();
		dialog.setWorkingDirectory(selectedDir.getAbsolutePath());
		dialog.setVisible(true);
		
		// Grab Launcher instance
        Launcher _main=Launcher.getInstance();

        // Add the tool to the workspace -
        _main.getContentPane().add(dialog);
        */
	}
	
	public void propertyChange(PropertyChangeEvent pce) {
		 
		
		if (pce.getPropertyName().equals (JOptionPane.VALUE_PROPERTY)) 
		{

        	int choice = ((Integer)pce.getNewValue()).intValue();
			if (choice==0)
			{
				//String strFileName = selectedFile.getName();
				//UEditorSession session = (Launcher.getInstance()).getSession();
				//session.setProperty("CURRENT_MODEL_PROP_FILENAME",strFileName);
				//SystemwideEventService.fireSessionUpdateEvent();
			}
	        
			// close the frame -
            windowFrame.hideSheet();
	     }
		
	}

}
