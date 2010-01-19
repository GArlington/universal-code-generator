package org.varnerlab.universaleditor.gui.actions;

// Import statements
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *  Action that closes down the application via a system exit call
 *  @author J.Varner
 */
public class ExitAction implements ActionListener {
    
    /**
     *  Executes the logic encapsulated by this action
     *  @param ActionEvent Event object
     */
    public void actionPerformed(ActionEvent e){
        System.exit(0);
    }
}



