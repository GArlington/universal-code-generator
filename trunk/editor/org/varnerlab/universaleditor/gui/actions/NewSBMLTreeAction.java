package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NetworkEditorTool;
import org.w3c.dom.Document;

public class NewSBMLTreeAction implements ActionListener {

	// class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	
	public void actionPerformed(ActionEvent e) {
		// ok, when I get here I need to load the SBML model template file from disk -
		try 
		{
			// Get the currently focused component -
	        focusedComponent = manager.getFocusOwner();
	        NetworkEditorTool windowFrame = (NetworkEditorTool)focusedComponent.getFocusCycleRootAncestor();
	        
			// Load the session from the launcher -
            UEditorSession session = Launcher.getInstance().getSession();
            
            // Set the path of the template file -
            String strConfDir = (String)session.getProperty("CONFIGURATION_WORKING_DIR");
            String strTemplateFile = strConfDir+"/"+"NewSBMLModel.xml";
            File file = new File(strTemplateFile);
            
            // Load the document builder and read the tree from disk -
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        	dbFactory.setNamespaceAware(true);
        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      	  	Document doc = dBuilder.parse(file);
      	  	
      	  	// Ok, so now let's cache the DOM tree so I can use it later -
      	  	session.setProperty("SBML_NETWORK_DOM_TREE", doc);
      	  	
      	  	// Ok, when I get here I have a reference to the rootNode - hand this to NetworkEditor -
      	  	windowFrame.setRootNode(doc,file.getName());
		}
		catch (Exception error)
		{
			// throw an error - this should be replaced with a log call-
			error.printStackTrace();
		}
	}

}
