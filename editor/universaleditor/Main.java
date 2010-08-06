/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package universaleditor;

// import statements -
import java.awt.image.BufferedImage;

import com.sun.awt.AWTUtilities;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.Dimension;
import java.awt.GridLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.Enumeration;
import java.util.Properties;

import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.gui.widgets.WaitThread;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author jeffreyvarner
 */
public class Main {
    private static Launcher launcher =  null;
    private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    	
    	SplashScreen splashScreen = new SplashScreen(new ImageIcon(VLImageLoader.getPNGImage("UniversalLogo-256.png")));
    	splashScreen.setProgressMax(100);
    	splashScreen.setVisible(true);
    	splashScreen.setAlwaysOnTop(true);
    	
    	//splash.showSplashAndExit();
    	
        Main editor = new Main();
        launcher = Launcher.getInstance();
        splashScreen.setProgress(20);

        // Update the session object from the editor -
        // Get the prop file path -
        String strPropPath = args[0];

        // Get the session object from launcher -
        UEditorSession session = (Launcher.getInstance()).getSession();
        
        try {
            editor.doInitializeSession(strPropPath,session);
            splashScreen.setProgress(40);      
            (Launcher.getInstance()).loadIcons();
            splashScreen.setProgress(50);

            // Get the launcher and set its icon -
            //(Launcher.getInstance()).setIconImage(VLIconManagerService.getIcon("VLUNIVERSAL-ICON"));

            // When I get here -- I have loaded the platform info -
            String strPlatform = (String)session.getProperty("PLATFORM");
            if (strPlatform.equalsIgnoreCase("CROSSPLATFORM"))
            {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
            else if (strPlatform.equalsIgnoreCase("SYSTEM"))
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            else if (strPlatform.equalsIgnoreCase("MACOSX"))
            {

                //File file = new File("./images/UniversalLogo-256.png");
                //BufferedImage img = ImageLoader.createImage(file);
                // DockIcon.set(img);

                // Get the system L & F -
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Set the brushed metal L & F -
                System.setProperty("apple.awt.brushMetalLook", "true");
                System.setProperty("apple.awt.textantialiasing","on");
                System.setProperty("apple.awt.graphics.UseQuartz","true");
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Universal");
                System.setProperty("apple.awt.graphics.EnableQ2DX","true");
                
                // use smoother fonts (same as above?)
                System.setProperty("apple.awt.textantialiasing", "true");
            }


            // Update the progress and wait for a second -or- so -
            splashScreen.setProgress(60);
            WaitThread.manySec(1);
            
            String strPluginDir = "./lib";
            File filePlugin = new File(strPluginDir);
            File[] jarFileArray = filePlugin.listFiles();
            int NUMBER_OF_FILES = jarFileArray.length;
            for (int index=0;index<NUMBER_OF_FILES;index++)
            {
            	File tmpFile = jarFileArray[index];
            	LoadPluginJarFiles.addFile(tmpFile);
            }
            
            splashScreen.setProgress(80);
            WaitThread.manySec(1);
            splashScreen.setProgress(90);
            WaitThread.manySec(1);
            splashScreen.setProgress(100);
            WaitThread.manySec(1);
  
            // shut down the splash screen -
            splashScreen.setVisible(false);
            
            // Make visible -
            editor.launchTheEditor();
        }
        catch (Exception error)
        {
            // eat the expection for the moment -
        }

    }

    // Loads any configuration data required by the server -
    public void doInitializeSession(String strPath,UEditorSession session) throws Exception
    {
        
    	/*
        // Fire up the SAX parser -
        SAXParserFactory factorySAX = SAXParserFactory.newInstance();
        SAXParser saxParser = factorySAX.newSAXParser();

        // Create and condigure a content handler -
        VLPropDefaultHandler handler = new VLPropDefaultHandler();
        handler.setFactory(session);

        // Ok, smack my bithch up...
        saxParser.parse(new File(strPath), handler);
        */
    	
    	// The properties file is now parsed using a DOM parser -
    	File mainPropFile = new File(strPath);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  	  	Document doc = dBuilder.parse(mainPropFile);
  	  	doc.getDocumentElement().normalize();	
  	  	session.setProperty("UNIVERSAL_DOM_TREE",doc);
  	  	
  	  	System.out.println("Loaded the UNIVERSAL DOM tree ... loading templates.");
  	  	
  	  	// Ok, so we need to set the options in session that we used to when we used the DOM parser -
  	  	populateSessionObject(doc,session);
  	  	
        // Ok, so now we need to load the templates that we can do -
        String strTemplateFile = (String)session.getProperty("TEMPLATE_MAP_FILE");
        File configFile = new File(strTemplateFile);
    	dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	dBuilder = dbFactory.newDocumentBuilder();
  	  	Document template_doc = dBuilder.parse(configFile);
  	  	template_doc.getDocumentElement().normalize();	
          	  	
  	  	// Ok, so now let's cach the DOM tree so I can use it later -
  	  	session.setProperty("TEMPLATE_DOM_TREE", template_doc);
  	  	
  	  	
  	  	
  	  	/*
  	  	// Ok, so now we need to load the templates that we can do -
        String strPropertyFile = (String)session.getProperty("PROPERTY_MAP_FILE");
        configFile = new File(strPropertyFile);
    	//DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	//DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  	  	Document docJComboBoxProp = dBuilder.parse(configFile);
  	  	docJComboBoxProp.getDocumentElement().normalize();	
        
  	  	// Ok, so now let's cach the DOM tree so I can use it later -
  	  	session.setProperty("PROPERTY_TABLE_TREE", docJComboBoxProp);
  	  	*/
    }


    // Method to show the GUI -
    public void launchTheEditor()
    {
        //Create and set up the window.
        launcher.setVisible(true);
    }
    
    private void processPropertyAttributes(String strXPath,Document doc,UEditorSession session) throws Exception
    {
    	NodeList propNodeList = (NodeList)_xpath.evaluate(strXPath, doc, XPathConstants.NODESET);
    	int NUMBER_PATH_NODES = propNodeList.getLength();
    	for (int index=0;index<NUMBER_PATH_NODES;index++)
    	{
    		// Get the current node -
    		Node tmpNode = propNodeList.item(index);
    		
    		// Process the attributes of this node ..
    		NamedNodeMap map = tmpNode.getAttributes();
    		int NUMBER_OF_ATTRIBUTES = map.getLength();
    		for (int att_index=0;att_index<NUMBER_OF_ATTRIBUTES;att_index++)
    		{
    			// Ok bitches, so I should get the attribute name (capitalize it) and key the value 
    			Node attNode = map.item(att_index);
    			String keyName = ((String)attNode.getNodeName()).toUpperCase();
    			String strValue = attNode.getNodeValue();
    			
    			// store the key,value pair in the session object -
    			session.setProperty(keyName, strValue);
    		}
    	}
    }

    private void populateSessionObject(Document doc,UEditorSession session) throws Exception
    {
    	// Ok, so let's get some stuff - we'll process in the order of the blocks in the file -
    	
    	// What data store are we using?
    	String strDataStoreXPath = "/UniversalCodeGenerator/@datastore";
    	Node dataStroreNode = (Node)_xpath.evaluate(strDataStoreXPath, doc, XPathConstants.NODE);
    	session.setProperty("DATASTORE_TYPE",dataStroreNode.getNodeValue());
    	
    	// Load the XML data store file information -
    	String strXMLFileInformationXPath = "//xmldatastrore_information/xmlstore/@filename";
    	Node dataXMLStroreNode = (Node)_xpath.evaluate(strXMLFileInformationXPath, doc, XPathConstants.NODE);
    	session.setProperty("XML_DATASTORE_PATH",dataXMLStroreNode.getNodeValue());
    	
    	// Architecture -
    	String strXPArchitecture = "//architecture/property/@platform";
    	Node architectureNode = (Node)_xpath.evaluate(strXPArchitecture, doc, XPathConstants.NODE);
    	session.setProperty("PLATFORM",architectureNode.getNodeValue());
    	
    	String strXPArchitectureEditor = "//architecture/property/@editor";
    	Node editorNode = (Node)_xpath.evaluate(strXPArchitectureEditor, doc, XPathConstants.NODE);
    	session.setProperty("EDITOR",editorNode.getNodeValue());
    	
    	String strXPArchitectureGraph = "//architecture/property/@graphviz_installed";
    	Node graphvizNode = (Node)_xpath.evaluate(strXPArchitectureGraph, doc, XPathConstants.NODE);
    	session.setProperty("GRAPHVIZ_INSTALLED",graphvizNode.getNodeValue());
    	
    	// Paths -
    	String strXPPaths = "//universal_paths/property";
    	processPropertyAttributes(strXPPaths,doc,session);
    	
    	// Server options
    	String strXPServerOptions = "//server_options/property";
    	processPropertyAttributes(strXPServerOptions,doc,session);
    	
    	// Database information -
    	String strXPDatabase = "//database_information/property";
    	processPropertyAttributes(strXPDatabase,doc,session);
    	
    	// Icons -
    	String strXPIcons = "//universal_icons/property";
    	processPropertyAttributes(strXPIcons,doc,session);	
    }
}
