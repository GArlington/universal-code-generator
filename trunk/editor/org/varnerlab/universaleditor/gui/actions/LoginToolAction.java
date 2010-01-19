package org.varnerlab.universaleditor.gui.actions;

// Import statements
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.*;

/**
 *  Action that opens process explorer window
 *  @author J.Varner
 */
public class LoginToolAction implements ActionListener {
   
    
    /**
     *  Executes the logic encapsulated by this action
     *  @param ActionEvent Event object
     */
    public void actionPerformed(ActionEvent e){
      
        // Grab Launcher instance
        Launcher _main=Launcher.getInstance();

        // Get the session -
        UEditorSession session = _main.getSession();
          
        // Create process explorer
        LoginTool _tool= new LoginTool();
        
        // Get some session info -
        String strUserName = (String)session.getProperty("VALIDATED_USERNAME");
        String strUserPassword = (String)session.getProperty("PASSWORD");
        
        if (strUserName!=null)
        {
        	_tool.setUserName(strUserName);
        }
        
        if (strUserPassword!=null)
        {
        	_tool.setPassword(strUserPassword);
        }
        
        _tool.setVisible(true);

        // Set the frameIcon
        _tool.setFrameIcon((new ImageIcon(VLImageLoader.getPNGImage("Buddy-20-Grey.png"))));

        // Ok, we need to store the icons on the tool so I can switch w/out loading -
        _tool.setOffIcon((new ImageIcon(VLImageLoader.getPNGImage("Buddy-20-Grey.png"))));
        _tool.setOnIcon((new ImageIcon(VLImageLoader.getPNGImage("Buddy-20.png"))));

        // Add the tool to the workspace -
        _main.getContentPane().add(_tool);

        // Notify PublishService that something is going on -
        PublishService.submitData("Opening the Login Tool ...");
    }
}



