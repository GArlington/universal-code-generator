package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.JTextField;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NewDirectoryDialog;
import org.varnerlab.universaleditor.service.SystemwideEventService;

public class SetLocalDirectoryAction implements ActionListener {
	// class/instance attributes -
	private NewDirectoryDialog _dialog = null;
	private String _strNewDirName = "";
	private String _strWorkingDir = "";
	
	public void setDialogReference(NewDirectoryDialog txtBox)
	{
		_dialog = txtBox;
	}
		
	public void actionPerformed(ActionEvent e) {
		// Method attributes -
		Launcher launcher = Launcher.getInstance();
		
		// Get the session -
		UEditorSession session = launcher.getSession();
		
		// Get the text field -
		JTextField txtFld = _dialog.getTextBox();
		
		// Ok, grab the data from the text box -
		_strNewDirName = txtFld.getText();
		
		// Ok, get working dir name from the dialog box -
		_strWorkingDir = _dialog.getWorkingDirectory();
		
		// Ok, what are we going to do?
		// Create a dir -
		String strNewDirName = _strWorkingDir+"/"+_strNewDirName;
		
		// If the new dir name is not null ... make a new directory
		if (!strNewDirName.isEmpty())
		{
			File srcFile = new File(strNewDirName);
			srcFile.mkdir();
		}
		
		// Ok, I created the directory bitches, so let's put the new directory name in session and then  launch a session update event -
		session.setProperty("NEW_LOCAL_DIRECTORY_PATH",strNewDirName);
		SystemwideEventService.fireUsernameUpdateEvent();
		
        // close the dialog?
        /*
        try {
			_dialog.setClosed(true);
		} catch (PropertyVetoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
	}

}
