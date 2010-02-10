package org.varnerlab.universaleditor.gui.actions;

// Import statements
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *  Action that opens process explorer window
 *  @author J.Varner
 */
public class OpenToolAction implements ActionListener {
   
    
    /**
     *  Executes the logic encapsulated by this action
     *  @param ActionEvent Event object
     */
    public void actionPerformed(ActionEvent e){
      
        // Grab Launcher instance
        Launcher _main=Launcher.getInstance();

       // Create process explorer
       ModelCodeGeneratorFileEditor _tool= ModelCodeGeneratorFileEditor.getInstance();
       _main.setModelCodeGeneratorFileEditor(_tool);
        
        try {
			_tool.setSelected(true);
		} catch (PropertyVetoException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        _tool.moveToFront();
        
        //_tool.requestFocus();
        _tool.setVisible(true);
        
        // Set the frameIcon
        _tool.setFrameIcon(VLIconManagerService.getIcon("PROPERTYFILEEDITOR-ICON"));
        
        
        // Ok, we need to store the icons on the tool so I can switch w/out loading -
        _tool.setOffIcon(VLIconManagerService.getIcon("PROPERTYFILEEDITOR-GREY-ICON"));
        _tool.setOnIcon(VLIconManagerService.getIcon("PROPERTYFILEEDITOR-ICON"));


        // Add the tool to the workspace -
        _main.getContentPane().add(_tool);
    }
}



