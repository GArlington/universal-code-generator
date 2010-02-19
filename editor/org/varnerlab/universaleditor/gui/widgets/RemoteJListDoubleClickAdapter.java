package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NetworkEditorTool;
import org.varnerlab.universaleditor.service.SystemwideEventService;

public class RemoteJListDoubleClickAdapter extends MouseAdapter implements PropertyChangeListener {

	// class/instance variables =
	private JList _jList = null;
	private JOptionPane optionPane;
    private SheetDialogFrame _frame;
    private FileTransferTool windowFrame;
    private File selectedFile;
    
    private Component focusedComponent = null;
    private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

	
	public void setListReference(JList jList)
	{
		_jList = jList;
	}
	
	public void mouseClicked(MouseEvent e)
	{
		if (e.getClickCount() == 2)
		{
			try 
			{
			
				// Get the currently focused component -
		        focusedComponent = manager.getFocusOwner();
		        windowFrame = (FileTransferTool)focusedComponent.getFocusCycleRootAncestor();
				
				// Get the file that was selected?
				selectedFile = (File)_jList.getSelectedValue();
				String strMessage = "Select - "+selectedFile.getName()+" as your UNIVERSAL options file?";
				
				//JOptionPane.showMessageDialog(_jList, strMessage);
				
				// build JOptionPane dialog and hold onto it
		        optionPane = new JOptionPane (strMessage,
		                                      JOptionPane.QUESTION_MESSAGE,
		                                      JOptionPane.YES_NO_OPTION);
		        
		        optionPane.addPropertyChangeListener (this);
		        JDialog dialog = optionPane.createDialog (_frame, "irrelevant");
		        windowFrame.showJDialogAsSheet (dialog);
			}
			catch (Exception error)
			{
				error.printStackTrace();
			}
		}
	}

	public void propertyChange(PropertyChangeEvent pce) {
		 
		
		if (pce.getPropertyName().equals (JOptionPane.VALUE_PROPERTY)) 
		{

        	int choice = ((Integer)pce.getNewValue()).intValue();
			if (choice==0)
			{
				String strFileName = selectedFile.getName();
				UEditorSession session = (Launcher.getInstance()).getSession();
				session.setProperty("CURRENT_MODEL_PROP_FILENAME",strFileName);
				
				// Update the reference to the model.prop in the GUI -
				(Launcher.getInstance()).updateModelPropFile();
			}
	        
			// close the frame -
            windowFrame.hideSheet();
	     }
		
	}
	
}
