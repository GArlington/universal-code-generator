package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.widgets.SheetDialogFrame;
import org.varnerlab.universaleditor.service.ServerJobTypes;
import org.varnerlab.universaleditor.service.SocketService;

public class DeleteRemoteFileKeyListener implements KeyListener,ActionListener,PropertyChangeListener  {
	// Class/instance attributes -
	private JList _jList= null;
	private File[] selectedFileArr;
	private FileTransferTool windowFrame;
	private JOptionPane optionPane;
	private SheetDialogFrame _frame;
	private File _fileLocal = null;
	
	private Component focusedComponent = null;
    private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	
    public void keyTyped(KeyEvent e) {
        System.out.println("Hey now -- remote jlist2");
    }

    public void setJListReference(JList jList)
	{
		_jList = jList;
	}
    
    public void keyPressed(KeyEvent e) {

        // Check for the backspace - Not sure if this key index is apple specific ...
        if (e.getKeyCode()==KeyEvent.VK_BACK_SPACE)
        {
        	// Get the focused component and then update the list -
        	// Get the currently focused component -
	        focusedComponent = manager.getFocusOwner();
	        windowFrame = (FileTransferTool)focusedComponent.getFocusCycleRootAncestor();
        	
	        System.out.println("keyPressed on remote...");
	        
	        // Ok, so we need to add a dialog to make sure it is ok to delete what ever I'm about to delete
	        
        	// Ok, so when I get here I'm going to delete remote files?
    		Object[] objArr = _jList.getSelectedValues();
    		int NITEMS = objArr.length;
    		
    		
    		
    		for (int index=0;index<NITEMS;index++)
    		{
    			File tmpFile = (File)objArr[index];
    			
    			System.out.println("How many items ... "+NITEMS+" what has been selected - "+tmpFile.getName()+" is this a file? "+tmpFile.isFile());
    			
    			if (tmpFile!=null)
    			{
    				// what file is selected -
    				_fileLocal = tmpFile;
    				
    				// Pop the dialog?
    				String tmp = tmpFile.getPath();
    				String strMessage = "Delete \""+tmpFile.getName()+"\"?\nDo you really want to delete this?\n" +
    						"This action can not be undone.";
    				optionPane = new JOptionPane(strMessage,JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION);
    				optionPane.addPropertyChangeListener(this);
    				JDialog jDialog = optionPane.createDialog(_frame,"Spank");
    				windowFrame.showJDialogAsSheet(jDialog);
    			}
    		}
        }
    }

    public void keyReleased(KeyEvent e) {
        System.out.println("Hey now -- ");
    }

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals (JOptionPane.VALUE_PROPERTY)) 
		{		
			FileTransferTool windowFrame = (Launcher.getInstance()).getFileTransferToolRef();
			int value = ((Integer)optionPane.getValue()).intValue();
			
			if (value==0)
			{
			
				try {				
					
					// Ok - this is where we launch...
					// Get the session from Launcher
					UEditorSession session = (Launcher.getInstance()).getSession();
					
					// Formulate the message and get stuff reqd by the socket service -
					String strIPAddress = (String)session.getProperty("SERVER_ADDRESS");
		    		String strPort = (String)session.getProperty("SERVER_PORT");
		    		
		    		// Ok, so let's create the path string -
		    		String strFile = _fileLocal.getName();
		    		String strSelectedRemotePath = (String)session.getProperty("SELECTED_REMOTE_PATH");
		    		String strPath = strSelectedRemotePath+"/"+strFile;
		    		
		    		// Formulate the message buffer -
		    		StringBuffer buffer = new StringBuffer();
		    		buffer.append("<?xml version=\"1.0\"?>\n");
		    		buffer.append("<universal>\n");
		    		buffer.append("\t<property remotefilename=\"");
		    		buffer.append(strPath);
		    		buffer.append("\"/>\n");
		    		buffer.append("</universal>\n");
		    			
					// Ok, so we need to send a message to the server to nuke this dir -
					
					// Socket service - send a message -
					SocketService.sendMessage(buffer.toString(),strIPAddress, strPort,session, ServerJobTypes.DELETE_FILE_ON_SERVER);
						
					// Update the session -
					windowFrame.refershProjectView();
				}
				catch (Exception error)
				{
					error.printStackTrace();
				}
				finally
				{
					windowFrame.hideSheet();
				}
			}
			else
			{
				windowFrame.hideSheet();
			}
		}
	}
}
