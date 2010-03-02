package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NewDirectoryDialog;
import org.varnerlab.universaleditor.gui.widgets.SheetDialogFrame;
import org.varnerlab.universaleditor.service.SystemwideEventService;


public class FileTransferJPopupMenuDeleteLocalActionListener implements ActionListener,PropertyChangeListener {
	// class/instance attributes -
	private JComboBox _jComboBox = null;
	private Component focusedComponent = null;
    private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    private FileTransferTool windowFrame;
    private File selectedDir;
    private SheetDialogFrame _frame;
    private JOptionPane optionPane;
	
	public void setJComboBoxReference(JComboBox jComboBox)
	{
		_jComboBox = jComboBox;
	}
	
	public void actionPerformed(ActionEvent e) {
	
		// Ok, so when I get here I need to create a new folder in the local file system -
		selectedDir = (File)_jComboBox.getSelectedItem();
		
		// Ok, what file did I select?
		System.out.println("What file was selected - "+selectedDir);
		
		try 
		{
			// Get the currently focused component - file transfer tool -
			focusedComponent = manager.getFocusOwner();
	        windowFrame = (FileTransferTool)focusedComponent.getFocusCycleRootAncestor();
	        
	        String strMessage = "Delete - "+selectedDir.getName()+"?";
			
			//JOptionPane.showMessageDialog(_jList, strMessage);
			
			// build JOptionPane dialog and hold onto it
	        optionPane = new JOptionPane (strMessage,
	                                      JOptionPane.QUESTION_MESSAGE,
	                                      JOptionPane.YES_NO_OPTION);
	        
	        optionPane.addPropertyChangeListener (this);
	        JDialog dialog = optionPane.createDialog (windowFrame, "irrelevant");
	        dialog.show();
	        //windowFrame.showJDialogAsSheet(dialog);
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
				
				System.out.println("Going to kia - "+selectedDir.getName());
				
				// Ok, do some crazy insane magic -
				File parent = selectedDir.getParentFile();
				_jComboBox.setSelectedItem(parent);
				_jComboBox.removeItem(selectedDir);
				processDirectoryTree(selectedDir);
				windowFrame.updateSelectedDirectory();
			}
	        
			// close the frame -
            //windowFrame.hideSheet();
	     }
		
	}
	
	private void processDirectoryTree(File dir)
	{
		// Ok, let's figure out if this dir has kids -
		File[] contentsArr = dir.listFiles();
		int NFILES = contentsArr.length;
		if (NFILES==0)
		{
			// ok, if I get here then I have an empty list -
			dir.delete();
		}
		else
		{
			for (int index=0;index<NFILES;index++)
			{
				// Get file -
				File tmpFile = contentsArr[index];
				if (tmpFile.isDirectory())
				{
					// if I get here then I have a dir that I need to kia -
					processDirectoryTree(tmpFile);
				}
				else
				{
					// if I get here then I have a file - delete
					tmpFile.delete();
				}
			}
			
			// lastly we need to delete me -
			processDirectoryTree(dir);
		}
	}

}
