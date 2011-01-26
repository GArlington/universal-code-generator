/*
 * Translator.java
 *
 * Created on March 4, 2007, 7:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.server.localtransportlayer;

// import statements -
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.sbml.libsbml.*;

/**
 *
 * @author jeffreyvarner
 */
public class Translator {
    // Class/instance attributes -
    private Hashtable _propTable = null;
    private SBMLReader _sbmlReader = null;
    private Logger _logger = Logger.getLogger(Translator.class.getName());
    
    /** Creates a new instance of Translator */
    public Translator() {
    	// Load the hashtable -
        _propTable = new Hashtable();
    }
    
    // Set the reference to the properties object -
    public void setProperties(Hashtable prop)
    {
        this._propTable = prop;
    }
    
    public void doExecute(Object object) throws Exception
    {
        // Method attributes -
        String strClassNameInput = "";
        String strClassNameOutput = "";
        String strPackageNameInputPackage = "";
        String strPackageNameOutputPackage = "";
        String strFQClassNameInputPackage = "";
        String strFQClassNameOutputPackage = "";
        
        // grab the xmlProp instance -
        XMLPropTree xmlProp = (XMLPropTree)object;
        
        // load the sbml?
        // System.out.println("New - "+System.getProperty("java.library.path"));
        System.loadLibrary("sbmlj");
        
        // Ok, so when I get here I have to create the dirs in case they are not already there -
        ArrayList<String> arrList = new ArrayList<String>();
        arrList.add(xmlProp.getProperty(".//path[@symbol='UNIVERSAL_OUTPUT_PATH']/@path_location"));
        arrList.add(xmlProp.getProperty(".//path[@symbol='UNIVERSAL_NETWORK_OUTPUT_PATH']/@path_location"));
        arrList.add(xmlProp.getProperty(".//path[@symbol='UNIVERSAL_DEBUG_OUTPUT_PATH']/@path_location"));
        int NUM_OF_DIRS = arrList.size();
        for (int dir_index = 0;dir_index<NUM_OF_DIRS;dir_index++)
        {
        	// Get the dirname -
        	String strDirName = arrList.get(dir_index);
        	if (strDirName!=null && !strDirName.isEmpty())
            {
            	 // Ok, so we are ready to create custom network and src directories -
              
                // src dir -
                File srcFile = new File(strDirName);
                srcFile.mkdir();
            }
        }
        
        // Get the input and output handler class names -
        strClassNameInput = (String)xmlProp.getProperty(".//Handler/InputHandler/@input_classname");
        strClassNameOutput = (String)xmlProp.getProperty(".//Handler/OutputHandler/@output_classname");
              
        // Ok, so we need to get the package for the input and output handlers -
        String strPackageSymbolInputHandler = xmlProp.getProperty(".//Handler/InputHandler[@input_classname='"+strClassNameInput+"']/@package");
        String strPackageSymbolOutputHandler = xmlProp.getProperty(".//Handler/OutputHandler[@output_classname='"+strClassNameOutput+"']/@package");
        
        // Look up the package name -
        strPackageNameInputPackage = xmlProp.getProperty(".//package[@symbol='"+strPackageSymbolInputHandler+"']/@package_name");
        strPackageNameOutputPackage = xmlProp.getProperty(".//package[@symbol='"+strPackageSymbolOutputHandler+"']/@package_name");
        
        // Formulate the fully qualified java name -
        strFQClassNameInputPackage = strPackageNameInputPackage+"."+strClassNameInput;
        strFQClassNameOutputPackage = strPackageNameOutputPackage+"."+strClassNameOutput;
        
        // Create an instance of the factory and build the handlers -
        VarnerLabObjectFactory factory = VarnerLabObjectFactory.getInstance();
        IInputHandler inputHandler = (IInputHandler)factory.buildObject(strFQClassNameInputPackage);
        IOutputHandler outputHandler = (IOutputHandler)factory.buildObject(strFQClassNameOutputPackage);
        
        System.out.println("Created an instance of "+strFQClassNameInputPackage);
        System.out.println("Created an instance of "+strFQClassNameOutputPackage);
        
        
        // On this input handler, we need to excute the load method -
        inputHandler.setProperties(xmlProp);
        inputHandler.setLogger(_logger);
        outputHandler.setProperties(xmlProp);
        outputHandler.setLogger(_logger);
        
        // load the input files -
        inputHandler.loadResource(null);
        
        // Ok, when I get here I've loaded the model source. I need to create
        // and instance of the output handler and then execute the write command'
        
        // Before I call the output handler, I need to get the stuff from the resource 
        // and put it into our wrappers -
        Object model = inputHandler.getResource(null);
        
        // Ok, so we need to some checks on the model -
            
        // Ok, so we have the ModelContainer - dump it to the output handler
        outputHandler.writeResource(model);
        
        // Ok, last when I get here I'm going to zip the contents if requested -
        
        // Get the info for the zip file -
        //String strUserName = (String)xmlProp.getProperty("//username/text()");
        //String strVersion = (String)xmlProp.getProperty("//version/text()");
        //String strDate = (String)xmlProp.getProperty("//last_updated/text()");
        //String strModelName = (String)xmlProp.getProperty("//model_name/text()");
        //String strWorkingDir = (String)xmlProp.getProperty("//working_directory/text()");
        
        // Create the filename -
        //String strZipName = strWorkingDir+"/"+strModelName+"_"+strUserName+"_"+strDate.replaceAll(" ","_")+"_"+strVersion+".zip";
        //ZipProjectFolder zipFolder = new ZipProjectFolder();
        //zipFolder.zipProjectFolder(strWorkingDir,strZipName);
    }

    public void setProperties(Properties prop) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	public Object getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean processMessage(String strMessage, IConfigurable session) throws Exception {
		
		// Load the xml config files -
		XMLPropTree xmlProp = new XMLPropTree();
		
		// We need to get a couple of items from the session -
		String strPath = (String)session.getProperty("SUBPATH");
		String strFileName = (String)session.getProperty("FILENAME");
		String strModelFilePath = strPath+"/"+strFileName;
		
		//System.out.println("Loading file - "+strModelFilePath);
		
		xmlProp.loadResource(strModelFilePath);

		// Get stuff from the model properties file -
		String strSrcDirName = (String)xmlProp.getProperty("//ProjectLayout/source_directory_name/text()");
        String strNetDirName = (String)xmlProp.getProperty("//ProjectLayout/network_directory_name/text()");
        String strWorkingDirectoryName = (String)xmlProp.getProperty("//working_directory/text()");
        
        if (strSrcDirName!=null && !strSrcDirName.isEmpty())
        {
        	 // Ok, so we are ready to create custom network and src directories -
            String strSrcDir = strWorkingDirectoryName+"/"+strSrcDirName;
         
            // src dir -
            File srcFile = new File(strSrcDir);
            srcFile.mkdir();
        }
        
        if (strNetDirName!=null && !strNetDirName.isEmpty())
        {
        	String strNetDir = strWorkingDirectoryName+"/"+strNetDirName;
        	
        	// network dir -
            File netFile = new File(strNetDir);
            netFile.mkdir();
        }
       
        // call doExecute - we are ready to generate some code bitches...
        this.doExecute(xmlProp);
		
		// TODO Auto-generated method stub
		return false;
	}
    
}
