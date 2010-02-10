package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.widgets.VLDesktopPane;

public class LoadPropFileAndToolFromDiskAction implements ActionListener {

	private Component focusedComponent = null;
	private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	// FileTransferTool windowFrame = (FileTransferTool) focusedComponent.getFocusCycleRootAncestor();
	
	
	public void actionPerformed(ActionEvent e) {
		
		try {

			// set the busy cursor -
			// windowFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			// Open up the prop tool -
			OpenToolAction openTool = new OpenToolAction();
			openTool.actionPerformed(e);

			// Ok, now that I have the tool open -
			LoadModelPropFileFromDiskAction loadFile = new LoadModelPropFileFromDiskAction();
			loadFile.actionPerformed(e);
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
		finally
		{
			//windowFrame.setCursor(Cursor.getDefaultCursor());
		}
	}

}
