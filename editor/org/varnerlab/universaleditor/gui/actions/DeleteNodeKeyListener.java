/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author jeffreyvarner
 */
public class DeleteNodeKeyListener implements KeyListener {

    public void keyTyped(KeyEvent e) {
        System.out.println("Hey now -- ");
    }

    public void keyPressed(KeyEvent e) {

        // Check for the backspace - Not sure if this key index is apple specific ...
        if (e.getKeyCode()==KeyEvent.VK_BACK_SPACE)
        {
            // Create new action -
            DeleteTreeNodeAction actionObj = new DeleteTreeNodeAction();
            actionObj.actionPerformed();
        }
    }

    public void keyReleased(KeyEvent e) {
        System.out.println("Hey now -- ");
    }

}
