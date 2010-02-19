package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NewDirectoryDialog;
import org.varnerlab.universaleditor.gui.widgets.SheetDialogFrame;
import org.varnerlab.universaleditor.service.SystemwideEventService;


public class RefreshProjectViewJPopupMenuActionListener implements ActionListener {
	// class/instance attributes -
	private JComboBox _jComboBox = null;
	private Component focusedComponent = null;
    private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    private FileTransferTool windowFrame;
    private SheetDialogFrame _frame;
    private JOptionPane optionPane;
	
	public void setJComboBoxReference(JComboBox jComboBox)
	{
		_jComboBox = jComboBox;
	}
	
	public void actionPerformed(ActionEvent e) {
		// Get the project tool -
		FileTransferTool _tool = (Launcher.getInstance()).getFileTransferToolRef();
		_tool.refershProjectView();
	}
}
