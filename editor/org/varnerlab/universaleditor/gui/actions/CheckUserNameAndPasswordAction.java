/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.actions;

// Import statements
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JTable;

import java.util.*;
import java.io.*;


import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.varnerlab.universaleditor.database.DatabaseAPI;
import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.parser.*;
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.BioChemExpTool;
import org.varnerlab.universaleditor.gui.widgets.VLTreeNode;


/**
 *
 * @author jeffreyvarner
 */
public class CheckUserNameAndPasswordAction implements ActionListener {

    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    private Hashtable _propTable = new Hashtable();
    private boolean _isOkToLoadThePage = false;

    public void setProperty(Object key,Object value)
    {
        _propTable.put(key, value);
    }

    public Object getProperty(Object key)
    {
        return(_propTable.get(key));
    }

    public void actionPerformed(ActionEvent e) {
       // Ok, so when I get here - I'm trying to load a properties file from disk -

        System.out.println("Hey now - why is this not working? Riddle me that looser...");

        // First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try
        {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           LoginTool windowFrame = (LoginTool)focusedComponent.getFocusCycleRootAncestor();

           // ok, I need to check the database to see if this username and login are cool ...

           // Get username and password -
           String strUserName = (String)this.getProperty("USERNAME");
           String strPassword = (String)this.getProperty("PASSWORD");
           DatabaseAPI dbAPI = (DatabaseAPI)this.getProperty("DATABASE_CONNECTION");

           // Ok, when I get here I need to check the database -
           _isOkToLoadThePage = dbAPI.checkUserInformation(strUserName, strPassword);           
           
        }
        catch (Exception error)
        {
            error.printStackTrace();
            System.out.println("ERROR in CheckUserNameAndPasswordAction: "+error.toString());
        }
    }

    public boolean isOkToLogIn()
    {
        return(_isOkToLoadThePage);
    }

}
