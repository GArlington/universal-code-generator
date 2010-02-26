package org.varnerlab.universaleditor.gui.actions;

// Import statements
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.widgets.InfiniteProgressPanel;
import org.varnerlab.universaleditor.gui.widgets.WaitThread;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *  Action that opens process explorer window
 *  @author J.Varner
 */
public class OpenProjectViewAction implements ActionListener {

	private Launcher _main;
	final InfiniteProgressPanel glassPane = new InfiniteProgressPanel();

	/**
	 *  Executes the logic encapsulated by this action
	 *  @param ActionEvent Event object
	 */
	public void actionPerformed(ActionEvent e){

		// Grab Launcher instance
		_main=Launcher.getInstance();

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

	private void doExecute()
	{
		// Create process explorer
		FileTransferTool _tool= FileTransferTool.getInstance();
		_main.setFileTransferToolRef(_tool);

		// Set the frameIcon
		_tool.setFrameIcon(VLIconManagerService.getIcon("PROJECT-ICON"));

		// Ok, we need to store the icons on the tool so I can switch w/out loading -
		_tool.setOffIcon(VLIconManagerService.getIcon("PROJECT-GREY-ICON"));
		_tool.setOnIcon(VLIconManagerService.getIcon("PROJECT-ICON"));

		// Set the title w/the username -
		UEditorSession session = (Launcher.getInstance()).getSession();
		String strUserName = (String)session.getProperty("VALIDATED_USERNAME");
		
		
		String strTitleName = "";
		if (strUserName!=null && !strUserName.isEmpty())
		{
			strTitleName = "Universal project tool v1.0 [user:"+strUserName+" is logged in]";
		}
		else
		{
			strTitleName = "Universal project tool v1.0 [no user is logged in]";
		}
		
		_tool.setTitle(strTitleName);
		
		// load the window -
		_tool.launchProjectTool();

		// Add the tool to the workspace -
		_main.getContentPane().remove(_tool);
		_main.getContentPane().add(_tool);
		//_main.setConsoleWindow(_tool);

		// Shutdown the spinning -
		glassPane.stop();
		
		// Make the tool visible -
		_tool.setVisible(true);
	}
}



