package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComboBox;

import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NewDirectoryDialog;


public class FileTransferJPopupMenuActionListener implements ActionListener {
	// class/instance attributes -
	private JComboBox _jComboBox = null;
	
	public void setJComboBoxReference(JComboBox jComboBox)
	{
		_jComboBox = jComboBox;
	}
	
	public void actionPerformed(ActionEvent e) {
	
		// Ok, so when I get here I need to create a new folder in the local file system -
		File selectedDir = (File)_jComboBox.getSelectedItem();
		
		// Ok, what
		System.out.println("What file was selected - "+selectedDir);
		
		// Ok, so now I need to load the dialog box to capture the name of the dir -
		NewDirectoryDialog dialog = new NewDirectoryDialog();
		dialog.setWorkingDirectory(selectedDir.getAbsolutePath());
		dialog.setVisible(true);
		
		// Grab Launcher instance
        Launcher _main=Launcher.getInstance();

        // Add the tool to the workspace -
        _main.getContentPane().add(dialog);
	}

}
