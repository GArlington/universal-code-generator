package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NetworkEditorTool;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;

public class LoadSBMLTreeFromDiskAction implements ActionListener {

	
	public void actionPerformed(ActionEvent e) {
		
		// Ok, so we need to get a reference to NetworkEditorTool -
		NetworkEditorTool windowFrame = Launcher.getInstance().getNetworkEditorToolRef();
		
		try 
		{
			// Get the source of this event (a file)
        	File file=(File)e.getSource();
			
			// Load the session from the launcher -
			UEditorSession session = Launcher.getInstance().getSession();

			// Load the document builder and read the tree from disk -
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file.getAbsolutePath());

			// Ok, so now let's cache the DOM tree so I can use it later -
			session.setProperty("SBML_NETWORK_DOM_TREE", doc);

			// Ok, when I get here I have a reference to the rootNode - hand this to BioChemExpTool -
			windowFrame.setRootNode(doc,file.getName());
			
			// set the quick save button -
			windowFrame.enableQuickSaveButton();
			
			// Lastly, we need set the network filename so I can get to it later -
			session.setProperty("CURRENT_NETWORK", file.getAbsolutePath());
			SystemwideEventService.fireNetworkUpdateEvent();
		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
	}

}
