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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.CodeGeneratorPropertiesFileTableModel;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.ModelCodeGeneratorFileEditor;
import org.varnerlab.universaleditor.gui.widgets.ModelPropertiesFileTableCellEditor;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class LoadModelPropFileFromDiskAction implements ActionListener {
	    // class/instance attributes
	    Component focusedComponent = null;
	    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	    private XPathFactory  _xpFactory = XPathFactory.newInstance();
		private XPath _xpath = _xpFactory.newXPath();

	    public void actionPerformed(ActionEvent e) 
	    {
	        
	        // First, we'll need to get the model prop file editor and then set its tree - hey by the way, I'm Rick Jamessss Bit*h!
	        try 
	        {
	        	// Get the currently focused component -
	        	Launcher launcherObj = Launcher.getInstance();
	        	UEditorSession session = (Launcher.getInstance()).getSession();
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
	        	
	        	// Ok, so we need to do a lookup to figure out what type of file this is ... and then set the selected item on the jcombo box -
	        	Document templateDOMTree = (Document)session.getProperty("TEMPLATE_DOM_TREE");

	        	String strXPathFileType = "//Model/@type";
	        	Node modelTypeNode = (Node)_xpath.evaluate(strXPathFileType,doc,XPathConstants.NODE);
	        	
	        	if (modelTypeNode!=null)
	        	{
	        		// Ok, so we need to look up this type in the template tree and get the display for the combo box -
	        		String strTypeNode = modelTypeNode.getNodeValue();
	        		      		
	        		// Ok, so do the lookup -
	        		String strXPathNameLookup = "//mapping[@type='"+strTypeNode+"']/display/@name";
	        		
	        		// Run the xpath ... make it so #1 (a little Next Gen for your ass)
	        		Node displayName = (Node)_xpath.evaluate(strXPathNameLookup,templateDOMTree,XPathConstants.NODE);
	        		
	        		if (displayName!=null)
	        		{
	        			windowFrame.setSelectedJComboItem(displayName.getNodeValue());
	        			
	        			// reset the type -
		        		ModelPropertiesFileTableCellEditor _tableCellEditor = windowFrame.getTableCellEditor();
		        		_tableCellEditor.setCurrentOutputType(displayName.getNodeValue());
	        		}
	        	}
	        	
	        	// Lastly we need to update the title on the window -
	        	String strCurrentTitle = windowFrame.getTitle();
	        	String strNewTitle = "Universal properties file editor v1.0"+" ["+file.getName()+"]";
	        	windowFrame.setTitle(strNewTitle);
	        	
	        	// Put the filename in session -
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
