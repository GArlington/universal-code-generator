package org.varnerlab.universaleditor.gui.actions;

// Import statements
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.widgets.InfiniteProgressPanel;
import org.varnerlab.universaleditor.gui.widgets.WaitThread;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *  Action that opens process explorer window
 *  @author J.Varner
 */
public class OpenToolAction implements ActionListener {

	private Launcher _main;
	final InfiniteProgressPanel glassPane = new InfiniteProgressPanel();


	/**
	 *  Executes the logic encapsulated by this action
	 *  @param ActionEvent Event object
	 */
	public void actionPerformed(ActionEvent e){

		// Grab Launcher instance
		_main=Launcher.getInstance();

		// I need to check to see if this tool is already built?
		if (true)
		{

			// config the glass pane -
			glassPane.setDoubleBuffered(true);
			int H = _main.getHeight();
			int W = _main.getWidth();
			Rectangle r = new Rectangle(W,H);
			glassPane.setBounds(r);

			// Set the glass pane -
			_main.setGlassPane(glassPane);

			// load the tool -
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					glassPane.start();
					Thread performer = new Thread(new Runnable() {
						public void run() {
							doExecute();
						}
					}, "Performer");
					performer.start();
				}
			});
		}
		else
		{
			doExecute();
		}
	}

	private void doExecute()
	{
		try {
			// Create process explorer
			ModelCodeGeneratorFileEditor _tool= ModelCodeGeneratorFileEditor.getInstance();
			_main.setModelCodeGeneratorFileEditor(_tool);

			// Set the frameIcon
			_tool.setFrameIcon(VLIconManagerService.getIcon("PROPERTYFILEEDITOR-ICON"));

			// Ok, we need to store the icons on the tool so I can switch w/out loading -
			_tool.setOffIcon(VLIconManagerService.getIcon("PROPERTYFILEEDITOR-GREY-ICON"));
			_tool.setOnIcon(VLIconManagerService.getIcon("PROPERTYFILEEDITOR-ICON"));

			// Add the tool to the workspace -
			_main.getContentPane().remove(_tool);
			_main.getContentPane().add(_tool);

			// Shutdown the spinning 
			WaitThread.manySec(1);
			glassPane.stop();
			
			// make the tool visible -
			_tool.setVisible(true);
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
	}
}



