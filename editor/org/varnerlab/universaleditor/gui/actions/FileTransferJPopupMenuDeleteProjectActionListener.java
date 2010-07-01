package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Hashtable;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.widgets.SheetDialogFrame;
import org.varnerlab.universaleditor.service.SocketService;
import org.varnerlab.universaleditor.service.ServerJobTypes;

public class FileTransferJPopupMenuDeleteProjectActionListener implements ActionListener,PropertyChangeListener {
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
		// TODO Auto-generated method stub
		// Ok, so when I get here I need to create a new folder in the local file system -
		selectedDir = (File)_jComboBox.getSelectedItem();
		
		// Ok, what file did I select?
		System.out.println("What file was selected - "+selectedDir);
		
		try 
		{
			// Get the currently focused component - file transfer tool -
			focusedComponent = manager.getFocusOwner();
	        windowFrame = (FileTransferTool)focusedComponent.getFocusCycleRootAncestor();
	        
	        // Ok, so we need to use the human readable name -
	        
	        // Get the session from Launcher
			UEditorSession session = (Launcher.getInstance()).getSession();
	        
	        // ok, so I have a ssid in the name -
        	Hashtable tmpTable = (Hashtable)session.getProperty("PROJECT_TRANSLATION_TABLE");
        	
        	// lookup the project id -
        	String strHumanName = (String)tmpTable.get(selectedDir.getName());
	        String strMessage = "Delete - "+strHumanName+"?";
			
			//JOptionPane.showMessageDialog(_jList, strMessage);
			
			// build JOptionPane dialog and hold onto it
	        optionPane = new JOptionPane (strMessage,
	                                      JOptionPane.QUESTION_MESSAGE,
	                                      JOptionPane.YES_NO_OPTION);
	        
	        optionPane.addPropertyChangeListener (this);
	        JDialog dialog = optionPane.createDialog (windowFrame, "Delete project ... ");
	        dialog.show();
	        //windowFrame.showJDialogAsSheet(dialog);
		}
		catch (Exception error)
		{
			// we should log this -
			System.out.println("Error in "+this.getClass().getName()+" "+error.toString());
		}
	}

	public void propertyChange(PropertyChangeEvent pce) {
		// TODO Auto-generated method stub
		if (pce.getPropertyName().equals (JOptionPane.VALUE_PROPERTY)) 
		{

        	int choice = ((Integer)pce.getNewValue()).intValue();
			if (choice==0)
			{
				
				System.out.println("Going to kia - "+selectedDir.getName());
				
				// Ok, do some crazy insane magik (yep, it is so awesome that it warrants the k) -
				
				// Get the session from Launcher
				UEditorSession session = (Launcher.getInstance()).getSession();
				
				// Formulate the message and get stuff reqd by the socket service -
				String strIPAddress = (String)session.getProperty("SERVER_ADDRESS");
	    		String strPort = (String)session.getProperty("SERVER_PORT");
	    		
	    		StringBuffer buffer = new StringBuffer();
	    		buffer.append("<universal>\n");
	    		buffer.append("</universal>\n");
				
				// Ok, so we need to send a message to the server to nuke this dir -
				try {
					// Socket service - send a message -
					SocketService.sendMessage(buffer.toString(),strIPAddress, strPort,session, ServerJobTypes.DELETE_PROJECT_ON_SERVER);
					
					// Update the session -
					windowFrame.refershProjectView();
				}
				catch (Exception error)
				{
					System.out.println("Error sending message..."+error.toString());
				}
				
			}
	        
			// close the frame -
            //windowFrame.hideSheet();
	     }
	}

}
