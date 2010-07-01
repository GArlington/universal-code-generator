/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.server.transport;

// import statements -
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import org.varnerlab.universal.server.*;

/**
 *
 * @author jeffreyvarner
 */
public class Main implements Runnable {
    // Class/instance attributes -
    private VLServerSession _serverSession;     // The session for this session 

    // Stuff reqd for thread -
    private Thread _serverThread = null;        // Thread that holds the server execution -
    private ServerSocket _serverSocket = null;  // Server socket used to listen to requests -
    private boolean _listen = true;             // Flag used to control the server socket loop -

    // Stuff required for logging -
    private FileHandler _handler=null;
    private static String _strLoggerName = Main.class.getName();
    private static Logger _logger=Logger.getLogger(_strLoggerName);
    private static FileHandler _fileHandlerLogger = null;
    private static SimpleFormatter _formatterTxt;
    private BufferedReader _in=null;
	private PrintWriter _out=null;
	
	// Create a xpFactory/xpath obj (we'll use this a zillion times -)
	private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();

    public Main()
    {
        // Create a new instance of the server session so I can track the session -
        _serverSession = new VLServerSession();
    }

    public void doExecute() throws Exception
    {
        // Method attribute -

        // Initialize the new thread and launch the listener -
        _serverThread = new Thread(this);
        _serverThread.start();
    }

    public VLServerSession getSession()
    {
        return(_serverSession);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Attributes in main -
        String strPropPath = args[0];

        try
        {
            // Create a new verion of main -
            Main server = new Main();

            // Get the session -
            VLServerSession session = server.getSession();

            // Initialize/configure the session -
            server.doInitializeSession(strPropPath, session);
            
            // Load the jars in the plugin dir -
            String strPluginDir = (String)session.getProperty("PLUGIN_DIR");
            File filePlugin = new File(strPluginDir);
            File[] jarFileArray = filePlugin.listFiles();
            int NUMBER_OF_FILES = jarFileArray.length;
            for (int index=0;index<NUMBER_OF_FILES;index++)
            {
            	File tmpFile = jarFileArray[index];
            	LoadPluginJarFiles.addFile(tmpFile);
            }

            // get the logfile name -
            String tmpFName = (String)session.getProperty("LOG_FILENAME");
            
            if (tmpFName!=null)
            {
            	// Load the file handler -
            	_fileHandlerLogger = new FileHandler(tmpFName);
            	_formatterTxt = new SimpleFormatter();
            	_fileHandlerLogger.setFormatter(_formatterTxt);
            	_logger.addHandler(_fileHandlerLogger);
            }
            else
            {
            	// WTF? We can't find the logfile name - set a stupido version here -
            	_fileHandlerLogger = new FileHandler("TMP.log");
            	_formatterTxt = new SimpleFormatter();
            	_fileHandlerLogger.setFormatter(_formatterTxt);
            	_logger.addHandler(_fileHandlerLogger);
            }

            // We have initialized the game board and players -
            // Let's go...
            server.doExecute();           
        }
        catch (Exception error)
        {
            // If I get here, then I have an issue in main -
            //System.out.println("We have an issue in main: "+error);
            _logger.log(Level.SEVERE,"Uh, that's not good. We have a malfunction being caught in Main of the DesignPathServer. The ERROR is: "+error.getMessage());
            error.printStackTrace();
        }
    }

    // Loads any configuration data required by the server -
    public void doInitializeSession(String strPath,VLServerSession session) throws Exception
    {

    	// Load the XML prop file -
    	File configFile = new File(strPath);
    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	dbFactory.setNamespaceAware(true);
    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
  	  	Document doc = dBuilder.parse(configFile);
  	  	doc.getDocumentElement().normalize();
    	
  	  	// Ok, bitches let's parse the prop file dom tree and get some stuff ...
  	  	
  	  	// server address -
  	  	XPathExpression expr = _xpath.compile("//address/text()");
  	  	NodeList addressNode = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
  	  	String strServerAddress = addressNode.item(0).getNodeValue();
  	  	
  	  	// server port -
  	  	expr = _xpath.compile("//port/text()");
	  	NodeList portNode = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
	  	String strServerPort = portNode.item(0).getNodeValue();
  	  	
	  	// server logfile -
	  	expr = _xpath.compile("//logfile/text()");
	  	NodeList logNode = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
	  	String logFileName = logNode.item(0).getNodeValue();
	  
	  	// plugin dir -
	  	expr = _xpath.compile("//plugin_dir/text()");
	  	NodeList pluginNode = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
	  	String pluginFilePath = pluginNode.item(0).getNodeValue();
	  	
	  	// working directory path -
	  	expr = _xpath.compile("//working_dir/text()");
	  	NodeList workingDirNode = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
	  	String workingDirPath = workingDirNode.item(0).getNodeValue();
	  
	  	
	  	// Ok, so in theory we have the port and ipaddress -
	  	session.setProperty("PORT", strServerPort);
	  	session.setProperty("SERVERNAME", strServerAddress);
	  	session.setProperty("LOG_FILENAME",logFileName);
	  	session.setProperty("WORKING_DIR",workingDirPath);
	  	session.setProperty("PLUGIN_DIR",pluginFilePath);
	  	
	  	// Ok, so cache the DOM tree -
	  	session.setProperty("DOM_TREE",doc);
    }

    private void processMessageHeader(BufferedReader input, VLServerSession session) throws Exception
    {
    	// Get the message -
    	StringBuffer _tmpBuffer=new StringBuffer();
        String strRoutingInfo = "";
        String line = "";
        int counter = 1;
        while ((line=_in.readLine())!=null)
        {
            if (counter==1)
            {
                // Grab the routing information -
                strRoutingInfo = line;

                // increment the counter -
                counter++;
            }
            else
            {
                // Grab the line and put in a buffer -
                if (!line.equalsIgnoreCase("<universal>") && !line.equalsIgnoreCase("</universal>"))
                {
                	_tmpBuffer.append(line);
                	_tmpBuffer.append("\n");

                	// System.out.println("WTF - "+line);
                }
            }

            if (line.equalsIgnoreCase("</universal>"))
            {
                break;
            }

        }
        
        // System.out.println("Made it out ...");
        
        // ok, store the message -
        session.setProperty("MESSAGE_BUFFER", _tmpBuffer);
        
        // Ok, so when I get here I have the routing header from the message - I need to load up the message handlers and process the message -
        _logger.log(Level.INFO,"Routing information - "+strRoutingInfo);
        _logger.log(Level.INFO,"Payload information - "+_tmpBuffer.toString());
        
        // Parse the routing info to get the username and session id for this request -
        StringTokenizer tokenizer = new StringTokenizer(strRoutingInfo,"::");
        counter = 1;
        while (tokenizer.hasMoreTokens())
        {
            if (counter==1)
            {
                // If I get here, then I have the user name -
                session.setProperty("USERNAME",tokenizer.nextToken());
                counter++;

            }
            else if (counter==2)
            {
                // If I get here then I have the sessionid -
                session.setProperty("SESSIONID",tokenizer.nextToken());
                counter++;
            }
            else if (counter==3)
            {
                // If I get here then I have the subdir 0
                session.setProperty("SUBPATH",tokenizer.nextToken());
                counter++;
            }
            else if (counter==4)
            {
                // If I get here then I have the filename 0
                session.setProperty("FILENAME",tokenizer.nextToken());
                counter++;
            }
            else if (counter==5)
            {
            	// If I get here then I have the job_type 0
                session.setProperty("JOB_TYPE",tokenizer.nextToken());   
            }
        }
    }
    

    public void run()
    {
        // Method attributes
        Socket tmpSocket=null;

        try {
            // Get Port and box info
            String port=(String)_serverSession.getProperty("PORT");
            String box=(String)_serverSession.getProperty("SERVERNAME");

            // Startup the ServerSocket
            _logger.log(Level.INFO,"Start UniversalServer on socket on - "+port+" on machine "+box);
            _serverSocket=new ServerSocket(Integer.parseInt(port));
        }
        catch (Exception error)
        {
            _logger.log(Level.WARNING,"Major malfunction: Something happened with the server socket.",error);
        }

        // main loop for the server -
        while (_listen)
        {

            try
            {

                // Get the tmp socket -- the server will block on this
                // System.out.println("Is ss null?? - "+_serverSocket);
                tmpSocket=_serverSocket.accept();

                // Get the input and output -
                // Get the input stream from the socket
                _in=new BufferedReader(new InputStreamReader(tmpSocket.getInputStream()));
                _out=new PrintWriter(tmpSocket.getOutputStream(),true);
                
                // Ok, bitches = let's read the message header 
                processMessageHeader(_in,_serverSession);
                
                // Log the progress -
                _logger.log(Level.INFO,"Completed processing the message header. Configuring job for user "+_serverSession.getProperty("USERNAME"));
                
                // Ok, I need to look the classname for the handlers -
                Document doc = (Document)_serverSession.getProperty("DOM_TREE");
                
                // Get the JOB_TYPE flag from the serverSession =
                String strJobType = (String)_serverSession.getProperty("JOB_TYPE");
                
                // Ok, compose the xpath string -
                String strXPathLogicSearch = "//JobConfiguration[@code=\""+strJobType+"\"]/logic/text()";
                String strXPathValidationSearch = "//JobConfiguration[@code=\""+strJobType+"\"]/validation/text()";
                
                // Create the xpath instructions -
                XPathExpression logic_expr = _xpath.compile(strXPathLogicSearch);
                XPathExpression validation_expr = _xpath.compile(strXPathValidationSearch);
                 
                // run the xpath queries -
          	  	NodeList logicNode = (NodeList)logic_expr.evaluate(doc, XPathConstants.NODESET);
          	  	NodeList validationNode = (NodeList)validation_expr.evaluate(doc, XPathConstants.NODESET);
          	  	
          	  	// Ok, let's get the handler classes and add them to the session object -
          	  	String strLogicClass = logicNode.item(0).getNodeValue();
          	  	String strValidationClass = validationNode.item(0).getNodeValue();
          	  	
          	  	// Cache the names in the session object -
          	  	_serverSession.setProperty("CLASSNAME_LOGIC_HANDLER", strLogicClass);
          	  	_serverSession.setProperty("CLASSNAME_VALIDATION_HANDLER", strValidationClass);
          	  	
          	  	// Ok, let's send something out so I can see -
          	  	_logger.log(Level.INFO,"LOGIC = "+strLogicClass+" VALIDATION = "+strValidationClass);
          	  	
          	  	// Get the buffer -
          	  	StringBuffer tmpBuffer = (StringBuffer)_serverSession.getProperty("MESSAGE_BUFFER");
          	  	
                // Pass the tmp socket to the worker thread and go...
                MessageServerWorker worker = new MessageServerWorker(tmpBuffer,_out,_strLoggerName,this.getSession());
                worker.startWorker();
                
                // the validation code use to be here, but we've moved it into the MessageServerWorker -
                _out.flush();

                // Close the reader -
                _in.close();
                _out.close();
                tmpSocket.close();

            }
            catch (Exception error){
                _logger.log(Level.WARNING,"Failed to allocate child socket.",error);
            }
        }

        // If we get to here, then somehow the _listen flag is false.
        // kill the serversocket
        try
        {

            _logger.log(Level.INFO,"Shutting down UniversalServer. Over and out.");

            //System.out.println("Why am I closing the socket>");
            _serverSocket.close();
        }
        catch (Exception error)
        {
            _logger.log(Level.WARNING,"Failed to close serversocket.",error);
        }
    }
}
