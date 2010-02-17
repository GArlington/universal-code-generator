package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.varnerlab.universaleditor.domain.CodeGeneratorPropertiesFileTableModel;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;


public class LoadModelPropFileFromDiskAction implements ActionListener {
	    // class/instance attributes
	    Component focusedComponent = null;
	    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

	    public void actionPerformed(ActionEvent e) 
	    {
	        
	        // First, we'll need to get the model prop file editor and then set its tree - hey by the way, I'm Rick Jamessss Bit*h!
	        try 
	        {
	        	// Get the currently focused component -
	        	Launcher launcherObj = Launcher.getInstance();
	        	ModelCodeGeneratorFileEditor windowFrame = launcherObj.getModelCodeGeneratorFileEditorRef();
	        	
	        	// Get the source of this event (a file)
	        	File file=(File)e.getSource();

	        	File configFile = new File(file.getAbsolutePath());
	        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        	dbFactory.setNamespaceAware(true);
	        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        	Document doc = dBuilder.parse(configFile);
	        	doc.getDocumentElement().normalize();

	        	// Ok, so now we the doc, try to create a tree -
	        	windowFrame.setRootNode(doc);
	        	windowFrame.setSaveAsButtonEnabled();

	        	// Put the filename in session -
	        	UEditorSession session = (Launcher.getInstance()).getSession();
	        	session.setProperty("CURRENT_MODEL_PROP_FILENAME",file.getName());
	        	
	        	// Load the model tree into memory -
	        	session.setProperty("MODEL_TEMPLATE_FILE_TREE", doc);

	        	// Fire the update -
	        	SystemwideEventService.fireSessionUpdateEvent();
	        }
	        catch (Exception error)
	        {
	            System.out.println("ERROR in OpenLocalFileAction: "+error.toString());
	        }
	    }
}
