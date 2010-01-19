package org.varnerlab.universaleditor.gui.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;


public class LoadTemplateModelPropertyFile implements ActionListener {

    // class/instance attributes
    Component focusedComponent = null;
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

    
    
    public void actionPerformed(ActionEvent e) {
    	
    	try {
    		
    		// Ok, we need to think about how to make this dynamic - for now just hardcode 
    		String strPath = "/Users/jeffreyvarner/dev/UniversalWeb/UniversalEdtior/conf/C-Octave.xml";
    		
    		// load the xml file -
    		File configFile = new File(strPath);
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        	dbFactory.setNamespaceAware(true);
        	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      	  	Document doc = dBuilder.parse(configFile);
      	  	doc.getDocumentElement().normalize();
      	  	
      	  	// Ok -- I have the DOMTree for the template file in memory, need to make the corresponding GUI tree w/a pointer to DOMTree node.
    		
    	}
    	catch (Exception error)
    	{
    		System.out.println("ERROR loading the template model properties file - "+error.toString());
    	}
    	
    }

}
