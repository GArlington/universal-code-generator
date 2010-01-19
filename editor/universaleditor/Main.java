/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package universaleditor;

// import statements -
import java.awt.image.BufferedImage;
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
import org.varnerlab.universaleditor.gui.*;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.service.VLIconManagerService;
import org.w3c.dom.Document;


/**
 *
 * @author jeffreyvarner
 */
public class Main {
    private static Launcher launcher =  null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    	
    	//SplashScreen splash = new SplashScreen(10000);
    	//splash.showSplashAndExit();
    	
        Main editor = new Main();
        launcher = Launcher.getInstance();

        // Update the session object from the editor -
        // Get the prop file path -
        String strPropPath = args[0];

        // Get the session object from launcher -
        UEditorSession session = (Launcher.getInstance()).getSession();
        
        try {
            editor.doInitializeSession(strPropPath,session);
            (Launcher.getInstance()).loadIcons();

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
            }


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
        
        // Fire up the SAX parser -
        SAXParserFactory factorySAX = SAXParserFactory.newInstance();
        SAXParser saxParser = factorySAX.newSAXParser();

        // Create and condigure a content handler -
        VLPropDefaultHandler handler = new VLPropDefaultHandler();
        handler.setFactory(session);

        // Ok, smack my bithch up...
        saxParser.parse(new File(strPath), handler);
        
        // Ok, so now we need to load the templates that we can do -
        String strTemplateFile = (String)session.getProperty("TEMPLATE_MAP_FILE");
        File configFile = new File(strTemplateFile);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  	  	Document doc = dBuilder.parse(configFile);
  	  	doc.getDocumentElement().normalize();	
        
  	  	// Ok, so now let's cach the DOM tree so I can use it later -
  	  	session.setProperty("TEMPLATE_DOM_TREE", doc);
  	  	
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
    }


    // Method to show the GUI -
    public void launchTheEditor()
    {
        //Create and set up the window.
        launcher.setVisible(true);
    }

}
