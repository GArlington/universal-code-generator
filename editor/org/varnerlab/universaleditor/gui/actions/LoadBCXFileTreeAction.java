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
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.*;
import java.io.*;


import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.parser.*;
import org.varnerlab.universaleditor.gui.widgets.InfiniteProgressPanel;
import org.varnerlab.universaleditor.gui.widgets.WaitThread;
import org.varnerlab.universaleditor.gui.BioChemExpTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NetworkEditorTool;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;


/**
 *
 * @author jeffreyvarner
 */
public class LoadBCXFileTreeAction implements ActionListener {

    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    ModelCodeGeneratorFileEditor windowFrame = null;
    Document doc = null;
    File file = null;
    final InfiniteProgressPanel glassPane = new InfiniteProgressPanel();
    

    public void actionPerformed(ActionEvent e)
    {
    	// First, you'll need to load the file chooser - hey by the way, I'm Rick Jamessss Bit*h!
        try {
           // Get the currently focused component -
           focusedComponent = manager.getFocusOwner();
           BioChemExpTool windowFrame = (BioChemExpTool)(Launcher.getInstance()).getBioChemExpToolRef();

           // Open new file chooser
           JFileChooser fc=new JFileChooser();
           int rVal=fc.showOpenDialog(focusedComponent);

           if (rVal==JFileChooser.APPROVE_OPTION)
           {
              // Get the fc -
              File file=fc.getSelectedFile();

              // Load the session from the launcher -
              UEditorSession session = Launcher.getInstance().getSession();
              
              // Load the document builder and read the tree from disk -
              DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
          	  dbFactory.setNamespaceAware(true);
          	  DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        	  Document doc = dBuilder.parse(file);
        	  // doc.getDocumentElement().normalize();	
              
        	  // Ok, so now let's cache the DOM tree so I can use it later -
        	  session.setProperty("BCX_DOM_TREE", doc);
              
              // Load the root node -
              // VLDomainComposite rootNode =(VLDomainComposite)Builder.doBuild(file.getAbsolutePath(),IDContentHandler.CHID_SBMLTREE_HANDLER);

              // Ok, when I get here I have a reference to the rootNode - hand this to BioChemExpTool -
        	  windowFrame.setRootNode(doc);
           }
        }
        catch (Exception error)
        {
            error.printStackTrace();
            System.out.println("ERROR in LoadBCXFileTreeAction: "+error.toString());
        }
    }
}
