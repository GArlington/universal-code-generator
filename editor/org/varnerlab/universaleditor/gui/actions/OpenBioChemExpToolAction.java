package org.varnerlab.universaleditor.gui.actions;

// Import statements
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *  Action that opens process explorer window
 *  @author J.Varner
 */
public class OpenBioChemExpToolAction implements ActionListener {


	/**
	 *  Executes the logic encapsulated by this action
	 *  @param ActionEvent Event object
	 */
	public void actionPerformed(ActionEvent e){

		// Grab Launcher instance
		Launcher _main=Launcher.getInstance();


		// Create process explorer
		BioChemExpTool _tool= BioChemExpTool.getInstance();
		_main.setBioChemExpTool(_tool);
		
		// Set the frameIcon
		_tool.setFrameIcon(VLIconManagerService.getIcon("BCXFILEEDITOR-ICON"));


		// Ok, we need to store the icons on the tool so I can switch w/out loading -
		_tool.setOffIcon(VLIconManagerService.getIcon("BCXFILEEDITOR-GREY-ICON"));
		_tool.setOnIcon(VLIconManagerService.getIcon("BCXFILEEDITOR-ICON"));

		try {
			// load the default tree -
			_tool.loadDefaultTree();
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
		
		
		// Add the tool to the workspace -
		_tool.setVisible(true);
		_main.getContentPane().remove(_tool);
		_main.getContentPane().add(_tool);
		
		// need to check if the console windo is up - if so I need to add -
		ConsoleWindow console = _main.getConsoleWindow();
		if (console!=null)
		{
			console.postToConsole("Launching BioChemExp tool...");
		}
	}
}



