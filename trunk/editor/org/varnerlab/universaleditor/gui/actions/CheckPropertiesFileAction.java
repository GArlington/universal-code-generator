package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;

public class CheckPropertiesFileAction implements ActionListener {
	private ModelCodeGeneratorFileEditor _tool = null;
	
	
	public void setToolReference(ModelCodeGeneratorFileEditor tool)
	{
		_tool = tool;
	}
	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub	
		Launcher window = Launcher.getInstance().getInstance();
		JOptionPane.showMessageDialog(window, "Checking the properties file...");
	}

}
