/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NoImageSplashScreen;
import org.varnerlab.universaleditor.gui.SplashScreen;
import org.varnerlab.universaleditor.gui.VLImageLoader;
import org.varnerlab.universaleditor.gui.widgets.InfiniteProgressPanel;
import org.varnerlab.universaleditor.gui.widgets.VLDesktopPane;
import org.varnerlab.universaleditor.gui.widgets.WaitThread;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.ServerJobTypes;
import org.varnerlab.universaleditor.service.SocketService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author jeffreyvarner
 */
public class GenerateCodeAction implements ActionListener {

	private Launcher _main;
	private UEditorSession session = null;
	private StringBuffer buffer = new StringBuffer();
	final InfiniteProgressPanel glassPane = new InfiniteProgressPanel();


	// Ok, so let's send a message to generate code to the server -
	public void actionPerformed(ActionEvent e) {
		// Method attributes -

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
	}

	private void doExecute()
	{
		// Get the session object -
		session = (Launcher.getInstance()).getSession();

		// When I get here I have the buffer all ready to go -
		String strIPAddress = (String) session.getProperty("SERVER_ADDRESS");
		String strPort = (String) session.getProperty("SERVER_PORT");

		// If I get here then I have a model.prop ready to rock -
		String strPropFileName = (String)session.getProperty("CURRENT_MODEL_PROP_FILENAME");
		if (strPropFileName!=null)
		{

			// Formulate the message -
			buffer.append("<universal>\n");
			buffer.append("\t <property modelpropfilename=\"");
			buffer.append(strPropFileName);
			buffer.append("\"/>\n");
			buffer.append("</universal>\n");

			// Send the message -
			try
			{
				// Send that mofo -
				PublishService.submitData("Sending "+buffer.toString());
				String strReturnString = SocketService.sendMessage(buffer.toString(), strIPAddress, strPort, session,ServerJobTypes.EXECUTE_UNIVERSAL_JOB);

				// Ok, when I get here I hace I have the directory -
				// If I get here that I should have a list of the files on the server -
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();

				// Ok, so we have a document -
				session = (Launcher.getInstance()).getSession();
				Document docTree = builder.parse(new InputSource(new StringReader(strReturnString)));
				session.setProperty("REMOTE_FILESYSTEM_TREE",docTree);
				
				// Update the session -
				SystemwideEventService.fireSessionUpdateEvent();
				
				// Refresh the view -
				FileTransferTool tool = (Launcher.getInstance()).getFileTransferToolRef();
				tool.refershProjectView();

				// Ok, so shutdown the glassPane -
				glassPane.stop();
			}
			catch (Exception error)
			{
				//glassPane.stop();
				PublishService.submitData("Spank me - some type of error "+error);
			}

			finally
			{
				glassPane.stop();
			}
		}
	}
}
