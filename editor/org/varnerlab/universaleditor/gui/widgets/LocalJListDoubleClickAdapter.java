package org.varnerlab.universaleditor.gui.widgets;


import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.FileTransferTool;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.NetworkEditorTool;
import org.varnerlab.universaleditor.gui.actions.LoadSBMLTreeFromDiskAction;
import org.varnerlab.universaleditor.gui.actions.OpenNetworkEditorToolAction;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


public class LocalJListDoubleClickAdapter extends MouseAdapter {
	
	private Component focusedComponent = null;
	private KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
	private FileTransferTool windowFrame;
	private File selectedFile;
	private JList _jList = null;
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();
		
	public void setListReference(JList jList)
	{
		_jList = jList;
	}
		
	public void mouseClicked(MouseEvent e)
	{
		if (e.getClickCount() == 2)
		{
			try 
			{
				// Get the currently focused component -
			    focusedComponent = manager.getFocusOwner();
			    windowFrame = (FileTransferTool)focusedComponent.getFocusCycleRootAncestor();
					
			    // Get the file that was selected?
				selectedFile = (File)_jList.getSelectedValue();
				
				if (selectedFile.isFile())
				{
					// Get the name of the selectedFile -- if it is an xml file then load from disk -
					String strFileName = selectedFile.getName();
					
					// Set the file reference -
					UEditorSession session = (Launcher.getInstance()).getSession();
					session.setProperty("LOCAL_SELECTED_FILE", selectedFile);
					
					
					int INT_2_DOT = strFileName.lastIndexOf(".");
					String strExtension = strFileName.substring(INT_2_DOT+1,strFileName.length());
					
					// Ok, so we have the extension - if it is xml then load the file -
					if (strExtension.equalsIgnoreCase("xml"))
					{
						// Ok, load the file and get the action command from the model tag -
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			          	dbFactory.setNamespaceAware(true);
			          	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			        	Document doc = dBuilder.parse(selectedFile.getAbsolutePath());
			        	
			        	// parse the tree and get the action command -
			        	String strXpath = "//Model/@action";
			        	Node actionNode = (Node)_xpath.evaluate(strXpath, doc,XPathConstants.NODE);
			        	String actionClassName = actionNode.getNodeValue();
			        	
			        	// Create an instance of the action class and an action event and then launch this mofo -
			        	ActionListener actionHandler = (ActionListener)Class.forName(actionClassName).newInstance();
			        	ActionEvent evt = new ActionEvent(selectedFile,-1,"spank_me");
			        	evt.setSource(selectedFile);
			        	actionHandler.actionPerformed(evt);
					}
					else if (strExtension.equalsIgnoreCase("sbml"))
					{
						// If I get here then I have an sbml file - load with the network editor tool -
						
						// Create an action event -
						ActionEvent evt = new ActionEvent(selectedFile,-1,"spank_me");
			        	evt.setSource(selectedFile);
						
			        	// Launch the network tool -
			        	OpenNetworkEditorToolAction netToolAction = new OpenNetworkEditorToolAction();
						netToolAction.actionPerformed(evt);
					
						// Load the sbml file from disk -
						LoadSBMLTreeFromDiskAction loadSBMLFileAction = new LoadSBMLTreeFromDiskAction();
						loadSBMLFileAction.actionPerformed(evt);
						
						// ok, so I need to activate the save as button -
						NetworkEditorTool _tool = (Launcher.getInstance()).getNetworkEditorToolRef();
						_tool.activateSaveAsButton();
						
						// Update the title bar to hold the filename -
						String strTitleName = "SBML Network Editor v1.0"+" ["+selectedFile.getName()+"]";
						_tool.setTitle(strTitleName);
					}
					else if (strExtension.equalsIgnoreCase("dot"))
					{
						// Get the path of the selected file and then try and open using a system edtior -
						String strFilePath = selectedFile.getAbsolutePath();
						
						// Ok, we need to get the name of the editor from the preference tree -
						String strGraphviz = (String)session.getProperty("GRAPHVIZ_INSTALLED");
						if (strGraphviz!=null && strGraphviz.equalsIgnoreCase("true"))
						{
							final String strCommand = "open -a GraphViz "+strFilePath;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									Thread performer = new Thread(new Runnable() {
										public void run() {
											try {
												Runtime.getRuntime().exec(strCommand);
											}
											catch (Exception error)
											{
												System.out.println("ERROR executing "+strCommand+" exception = "+error.toString());		
											}
										}
									}, "Performer");
									performer.start();
								}
							});
						}
					}
					else
					{
						// Ok, so if I get here then I have a file type that universal doesn't know how to handle -
						
						// check to see if this is word doc -
						if (!strExtension.equalsIgnoreCase("doc") || !strExtension.equalsIgnoreCase("docx"))
						{
							// Get the path of the selected file and then try and open using a system edtior -
							String strFilePath = selectedFile.getAbsolutePath();
							
							// Ok, we need to get the name of the editor from the preference tree -
							String strEditor = (String)session.getProperty("EDITOR");
							
							
							final String strCommand = "open -a "+strEditor+" "+strFilePath;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									Thread performer = new Thread(new Runnable() {
										public void run() {
											try {
												Runtime.getRuntime().exec(strCommand);
											}
											catch (Exception error)
											{
												System.out.println("ERROR executing "+strCommand+" exception = "+error.toString());		
											}
										}
									}, "Performer");
									performer.start();
								}
							});
						}
					}
				}
			}
			catch (Exception error)
			{
				error.printStackTrace();
			}
		}
	}
}
