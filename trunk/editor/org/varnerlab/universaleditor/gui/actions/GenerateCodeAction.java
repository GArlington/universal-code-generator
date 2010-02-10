/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
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

	private Component focusedComponent = null;
	private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	
    // Ok, so let's send a message to generate code to the server -
    public void actionPerformed(ActionEvent e) {
        // Method attributes -
        UEditorSession session = null;
        StringBuffer buffer = new StringBuffer();
        InfiniteProgressPanel glassPane = new InfiniteProgressPanel();
        focusedComponent = manager.getFocusOwner();
        VLDesktopPane windowFrame = windowFrame = (VLDesktopPane)focusedComponent.getFocusCycleRootAncestor();
        
        SplashScreen _splashScreen = new SplashScreen(new ImageIcon(VLImageLoader.getPNGImage("UniversalLogo-256.png")));
    
        try {
        
	        // Get the session object -
	        session = (Launcher.getInstance()).getSession();
	        windowFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	        //_splashScreen.setProgressMax(100);
	    	//_splashScreen.setVisible(true);
	    	//_splashScreen.setAlwaysOnTop(true);
	        
	    	_splashScreen.setProgress(10);
            WaitThread.manySec(1);
	
	        // When I get here I have the buffer all ready to go -
	        String strIPAddress = (String) session.getProperty("SERVER_ADDRESS");
	        String strPort = (String) session.getProperty("SERVER_PORT");
	
	        // If I get here then I have a model.prop ready to rock -
	        String strPropFileName = (String)session.getProperty("CURRENT_MODEL_PROP_FILENAME");
	        if (strPropFileName!=null)
	        {
	        
	        	_splashScreen.setProgress(20);
	            WaitThread.manySec(1);
	        	
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
	                
	                _splashScreen.setProgress(30);
	                WaitThread.manySec(1);
	                
	                String strReturnString = SocketService.sendMessage(buffer.toString(), strIPAddress, strPort, session,ServerJobTypes.EXECUTE_UNIVERSAL_JOB);
	
	                // Ok, when I get here I hace I have the directory -
	                // If I get here that I should have a list of the files on the server -
	                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	                DocumentBuilder builder = factory.newDocumentBuilder();
	
	                // Ok, so we have a document -
	                session = (Launcher.getInstance()).getSession();
	                session.setProperty("REMOTE_FILESYSTEM_TREE", builder.parse(new InputSource(new StringReader(strReturnString))));
	
	                // Update the session -
	                SystemwideEventService.fireSessionUpdateEvent();
	                
	                _splashScreen.setProgress(90);
	                WaitThread.manySec(1);
	                
	                //glassPane.stop();
	
	            }
	            catch (Exception error)
	            {
	                //glassPane.stop();
	            	PublishService.submitData("Spank me - some type of error "+error);
	            }
	        }
        }
        finally
        {
        	_splashScreen.setVisible(false);
        	windowFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
