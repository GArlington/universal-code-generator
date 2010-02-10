/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui;

// import statements -
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Enumeration;
import java.util.Vector;

import org.varnerlab.universaleditor.service.IVLPublishClient;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.gui.widgets.*;
import org.varnerlab.universaleditor.gui.parser.*;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.actions.OpenConsoleAction;
import org.varnerlab.universaleditor.service.IVLSystemwideEventListener;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *
 * @author jeffreyvarner
 */
public class Launcher extends JFrame implements IVLPublishClient, IVLSystemwideEventListener {
    // Class/instance attributes -
    private static Launcher _this;
    private VLDesktopPane _desktop;
    private JToolBar _mainToolBar;
    private VLStatusBar _jStatusBar;
    private ConsoleWindow console = null;
    private SplashScreen _splash = null;
    
    // set references to tools -
    private ModelCodeGeneratorFileEditor _modelCodeGeneratorFileEditor = null;
    private NetworkEditorTool _networkEditorTool = null;
    private FileTransferTool _fileTransferTool = null;
    
    // Session object -
    private UEditorSession _sessionObj = null;

    // Public path stuff used by tools and the builder
    public static final String _HOME=System.getProperty("user.home");
    public static final String _CURRENT=System.getProperty("user.dir");
    public static final String _SLASH=System.getProperty("file.separator");
    public static final String builderPath=Builder.instance().getPath();


    public void setFileTransferToolRef(FileTransferTool tool)
    {
    	_fileTransferTool = tool;
    }
    
    public FileTransferTool getFileTransferToolRef()
    {
    	return(_fileTransferTool);
    }
    
    public void setModelCodeGeneratorFileEditor(ModelCodeGeneratorFileEditor ref)
    {
    	_modelCodeGeneratorFileEditor = ref;
    }
      
    public ModelCodeGeneratorFileEditor getModelCodeGeneratorFileEditorRef()
    {
    	return(_modelCodeGeneratorFileEditor);
    }
    
    public void setNetworkFileEditor(NetworkEditorTool ref)
    {
    	_networkEditorTool = ref;
    }
      
    public NetworkEditorTool getNetworkEditorToolRef()
    {
    	return(_networkEditorTool);
    }
    
    // static accessor method
    public static Launcher getInstance(){
        if (_this==null){
            _this=new Launcher();

            // Set the frame icon
            //_this.setIconImage(VLImageLoader.getImage("office32.png"));
        }
        return(_this);
    }


     /**
     * Adds a component to the desktop
     */
    public void addDesktopItem(JComponent _item) {
        Container _pane=this.getContentPane();
        _pane.add(_item);
        this.setContentPane(_pane);
    }

    public ConsoleWindow getConsoleWindow()
    {
        return(console);
    }

    public void setConsoleWindow(JInternalFrame consoleWindow)
    {
        console = (ConsoleWindow)consoleWindow;
    }
    
    private Launcher()
    {
        super("Universal Code Generator Suite v1.0 - Varnerlab Cornell University");
        

        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");

            // Set Font
            setFont((new Font("Dialog",Font.PLAIN,12)));

        }
        catch (Exception error){
            error.printStackTrace();
        }
        
        // Make window size
        int inset=200;
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset,inset,screenSize.width-inset*2,screenSize.height-inset*2);
        
        // Quit this app
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                // Dump system preferences

                try {
                    // We'll need to persist that state -
                	// put some data here about session and current open projects?
                }
                catch (Exception error){
                    error.printStackTrace();
                }

                finally {
                    // Shutdown this mofo
                    System.exit(0);
                }
            }
        });


        // Ok, so I need to call out to the builder and have him return some shiznatz...
        try {
            // Load and construct toolbar from blueprint file
            _mainToolBar=(VLToolBar)Builder.doBuild(builderPath+"ToolBar.xml",IDContentHandler.CHID_WIDGET_HANDLER);
            _mainToolBar.setBounds(0, 0,screenSize.width,36);
            _mainToolBar.setVisible(true);


            // Create the status bar -
            _jStatusBar = new VLStatusBar();
            _jStatusBar.setVisible(true);

        }
        catch (Exception error){
            error.printStackTrace();
        }

        // Create a new session -
        _sessionObj = new UEditorSession();
        _sessionObj.generateSessionID();

        // Add stuff ...
        _desktop=new VLDesktopPane(new ImageIcon(VLImageLoader.getPNGImage("UniversalLogo.png")));

        // Make dragging faster
        _desktop.putClientProperty("JDesktopPane.dragMode","outline");
        _desktop.setBackground(new Color(100,100,100));

        // Set the content pane -
        _desktop.add(_mainToolBar,javax.swing.JLayeredPane.DEFAULT_LAYER);

        setContentPane(_desktop);

        // register the launcher with PublishService -
        PublishService.registerClients(this);

        // Register me as a network listner -
        SystemwideEventService.registerNetworkListener(this);
        SystemwideEventService.registerSessionListener(this);

        /*
        // Configure the ImageService (think about making this autofuckinmated...)
        VLIconManagerService.registerIcon("DISK-8", (new ImageIcon(VLImageLoader.getPNGImage("Disk-8.png"))));
        VLIconManagerService.registerIcon("FOLDER-8", (new ImageIcon(VLImageLoader.getPNGImage("Folder-8.png"))));
        VLIconManagerService.registerIcon("FILE-8", (new ImageIcon(VLImageLoader.getPNGImage("File-8.png"))));
        VLIconManagerService.registerIcon("XMLFILE-8", (new ImageIcon(VLImageLoader.getPNGImage("XMLFile-8.png"))));
        VLIconManagerService.registerIcon("PROPFILE-8", (new ImageIcon(VLImageLoader.getPNGImage("PropFile-8.png"))));
        VLIconManagerService.registerIcon("HOME-8", (new ImageIcon(VLImageLoader.getPNGImage("Home-8.png"))));
        VLIconManagerService.registerIcon("INFO-32-GREY", (new ImageIcon(VLImageLoader.getPNGImage("Info-32-Grey.png"))));
        VLIconManagerService.registerIcon("INFO-32", (new ImageIcon(VLImageLoader.getPNGImage("Info-32.png"))));
        VLIconManagerService.registerIcon("TRANSFER-32-GREY", (new ImageIcon(VLImageLoader.getPNGImage("Transfer-32-Grey.png"))));
        VLIconManagerService.registerIcon("TRANSFER-32", (new ImageIcon(VLImageLoader.getPNGImage("Transfer-32.png"))));
         */
      
    }

    public void loadIcons()
    {
        // Get the session - and go through looking for ICON
        Enumeration propKeys = _sessionObj.getKeys();
        while (propKeys.hasMoreElements())
        {
            // Get the key -
            String strKey = (String)propKeys.nextElement();

            if (strKey.contains("ICON"))
            {
                // Get the iconname -
                String strIconName = (String)_sessionObj.getProperty(strKey);
                
              

                // Load the icon -
                VLIconManagerService.registerIcon(strKey, (new ImageIcon(VLImageLoader.getPNGImage(strIconName))));
            }

        }
    }

    public UEditorSession getSession()
    {
        return(this._sessionObj);
    }

    public void publishData(Object object) {
        // need to check if the console windo is up - if so I need to add -
        if (console!=null)
        {
            console.postToConsole(object.toString());
        }
    }

    public void updateComponent() {

    }

    public void updateSession() {

        // If I get here then I may have a new model prop name -
        
        // Get the model prop file name -
        String strModelPropName = (String)_sessionObj.getProperty("CURRENT_MODEL_PROP_FILENAME");
        if (strModelPropName!=null)
        {
            // Set the file name on the desktop -
            VLDesktopPane desktop = (VLDesktopPane)(Launcher.getInstance()).getContentPane();
            desktop.setModelPropName(strModelPropName);
        }

    }

    public void updateNetwork() {
        // Get the session and the network file name -
        String strNet = (String)_sessionObj.getProperty("CURRENT_NETWORK");

        // Set the file name on the desktop -
        VLDesktopPane desktop = (VLDesktopPane)(Launcher.getInstance()).getContentPane();
        desktop.setNetworkName(strNet);
    }

}
