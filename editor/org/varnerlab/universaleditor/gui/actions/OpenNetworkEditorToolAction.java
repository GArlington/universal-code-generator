package org.varnerlab.universaleditor.gui.actions;

// Import statements
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *  Action that opens process explorer window
 *  @author J.Varner
 */
public class OpenNetworkEditorToolAction implements ActionListener {
   
    
    /**
     *  Executes the logic encapsulated by this action
     *  @param ActionEvent Event object
     */
    public void actionPerformed(ActionEvent e){
      
        // Grab Launcher instance
        Launcher _main=Launcher.getInstance();


        // Create process explorer
        NetworkEditorTool _tool= new NetworkEditorTool();
        _tool.setVisible(true);
        
        // Set the frameIcon
        _tool.setFrameIcon(VLIconManagerService.getIcon("NETWORKFILEEDITOR-GREY-ICON"));
            
        // Ok, we need to store the icons on the tool so I can switch w/out loading -
        _tool.setOffIcon(VLIconManagerService.getIcon("NETWORKFILEEDITOR-GREY-ICON"));
        _tool.setOnIcon(VLIconManagerService.getIcon("NETWORKFILEEDITOR-ICON"));


        // Add the tool to the workspace -
        _main.getContentPane().add(_tool);
    }
}



