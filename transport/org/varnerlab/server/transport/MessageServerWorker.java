package org.varnerlab.server.transport;

// import statements
import java.io.*;
import java.net.*;

import java.util.logging.*;
import java.util.*;


public class MessageServerWorker extends Object {
	// Class/instance attributes
	private Thread _workerThread=null;	
	private Socket _socket=null;
	BufferedReader _in=null;
	PrintWriter _out=null;
	private StringBuffer _tmpBuffer = null;
      
    private String _strLName = null;
    private VLServerSession _propTable = null;
        
    // a handle to the Octave process
	private Process p;
	
	// Get logger (configure it to find the local .tmp directory)
	private Logger _logger=null;
	private FileHandler _fileHandlerLogger = null;
    private SimpleFormatter _formatterTxt;
	
	public MessageServerWorker(StringBuffer input,PrintWriter output,String lName,VLServerSession session){
		// Grab input and output -
		_tmpBuffer = input;
        _out = output;

        // Capture the session -
        _propTable = session;
        
        // Grab the socket -
        //_socket = tmpSocket;
		
		// Setup logging -- grab simzilla's logger
		_logger=LogManager.getLogManager().getLogger(lName);
		setupLogger();

        // Store the logger name -
         _strLName = lName;
                
         // put the logname in the session so I can get it -
         _propTable.setProperty("LOG_FILENAME",_strLName);
	}
	
	private void setupLogger()
	{
		// Ok, so we need to get some some stuff from the session -
        String strDirName = (String)_propTable.getProperty("WORKING_DIR");
        String strUserName = (String)_propTable.getProperty("USERNAME");
        String strSessionID = (String)_propTable.getProperty("SESSIONID");
        
        String strNewHiddenDir = strDirName+"/"+strUserName+"/"+strSessionID+"/"+".tmp";
        
        // Ok, I need to check to see if the tmp dir exists -
        File testFile = new File(strNewHiddenDir);
        if (testFile.exists())
        {
        	String fName = strNewHiddenDir+"/"+"job.log";
        	
        	try {
        		_fileHandlerLogger = new FileHandler(fName);
        		_formatterTxt = new SimpleFormatter();
        		_fileHandlerLogger.setFormatter(_formatterTxt);
        		_logger.addHandler(_fileHandlerLogger);
        	}
        	catch (Exception error)
        	{
        		// eat the exception ... for now
        	}
        }
        else
        {
        	// Ok, so if I get here then I do *not* have the tmp dir (perhaps we have not created it yet)
        	// Use the old logger -
        	_logger.log(Level.INFO, "Have not yet created *.tmp directory. Using main log file.");
        }
        
        
	}
	
	public String getLogName()
	{
		return(_strLName);
	}
	
	public void startWorker(){
		/*
        // Create the thread and go...
		_workerThread=new Thread(this);
		_workerThread.start();
         */
        this.run();
	}
        
    public String getProperty(String key)
    {
            return((String)_propTable.getProperty(key));
    }
	
	// This method holds the logic that gets executed by on the background thread
	public void run(){
        try
        {
                
            //_logger.log(Level.INFO,"Connected to ..."+_socket.getInetAddress().getHostAddress());

            // Ok so I'm here
            //System.out.println("Fuck you - 0");
     
            /* ver alpha code - 
            //System.out.println("Fuck you - 2");
            // This is where I need the specific logic -
            // Get the class name of the handler from the server session -
            // 
            */
            
            // Ok, now that the GUI sends a JOB_TYPE flag in the header information, we need to do a lookup here on a classname that is mapped to that job_type -
            //String strClassName = (String)_propTable.getProperty(_propTable.getProperty("JOB_TYPE"));
        	
        	String strClassName = (String)_propTable.getProperty("CLASSNAME_LOGIC_HANDLER");
        	if (strClassName!=null)
            {
                // Create a new instance of the handler and call the processMessage method -
                IVLProcessServerRequest logicHandler = (IVLProcessServerRequest)Class.forName(strClassName).newInstance();

                // Log message 
                _logger.log(Level.INFO,"Job starting. Created an instance of "+strClassName);
                           
                // Class the processMessage method and get flag back 
                boolean blnFlag = logicHandler.processMessage(_tmpBuffer.toString(),_propTable);

            }
        	else
        	{
        		// Log message 
                _logger.log(Level.WARNING,"Failed to create an instance of the logic handler in MessageServerWorker thread.");
        	}
            
         
            String strValidationClassName = (String)_propTable.getProperty("CLASSNAME_VALIDATION_HANDLER");
            if (strValidationClassName!=null)
            {
                // Create new instance of handler -
                IVLValidateServerJob objValidate = (IVLValidateServerJob)Class.forName(strValidationClassName).newInstance();
                StringBuffer buffer = (StringBuffer)objValidate.validateJob(_propTable);
            
                // Log message 
                _logger.log(Level.INFO,"Job completed. Running the validation code. Creating an instance of "+strValidationClassName);
                
                // When I get here I have the buffer - send back to caller -
                _out.print(buffer.toString());
                
                // Log message 
                _logger.log(Level.INFO,"Validation job completed.");
            }
            else
            {
                // Ok, when I get here spank my monkey -
                _logger.log(Level.WARNING,"Failed to create an instance of the validation handler in MessageServerWorker thread.");
            }
            
        }
        catch (Exception error){
                error.printStackTrace();
                _logger.log(Level.WARNING,"Something happend in the worker - "+error);
        }
    }
}
