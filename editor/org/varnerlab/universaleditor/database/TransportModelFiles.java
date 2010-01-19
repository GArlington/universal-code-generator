/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.database;

// Import statements -
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.net.*;
import java.io.*;
import java.util.Vector;

/**
 * This class containts methods to parse the data in the input files into a format that can be displayed on the web page -
 * @author jeffreyvarner
 */
public class TransportModelFiles {

    // Class attributes -
    private Hashtable<String,String> _htblDataBlocks = new Hashtable<String,String>();
    private String _strModelType = "";  
    private String _strIPAddress = "";
    private String _strPort = "";
    private Socket _socket = null;
    private PrintWriter _printWriter = null;
    private Hashtable _propTable = new Hashtable();
    
    
    
    public void setProperty(Object key,Object val)
    {
        _propTable.put(key, val);
    }
    
    public Object getProperty(Object key)
    {
        return(_propTable.get(key));
    }
    
    public void addFiles(String key,String Data)
    {
        // Store the data blocks. We'll decide how to parse in a bit -
        _htblDataBlocks.put(key, Data);
    }
    
    public void setModelType(String strModelType)
    {
        _strModelType = strModelType;
        
        System.out.println("What the - "+_strModelType);
    }
    
    public void setAddress(String strIP,String strPort)
    {
        this._strIPAddress = strIP;
        this._strPort = strPort;
    }
    
    public void sendMessage(String xmlMessage) throws Exception
    {
        // Ok, I have the message, I need to open a socket and re-send that crazy mofo -
        // Create the socket -
        _socket = new Socket(_strIPAddress, Integer.parseInt(_strPort));
        _printWriter = new PrintWriter(_socket.getOutputStream(),true);
        
		
	// Formulate the meassge and send -
        _printWriter.println(xmlMessage);
        _printWriter.flush();
        _printWriter.close();
    }
    
    
    public String formulateMessage() throws Exception
    {
        // Method attributes -
        StringBuffer strBuffer = new StringBuffer();
        
        // Start the buffer -
        strBuffer.append("<?xml version=\"1.0\"?>\n");
        strBuffer.append("<universal>\n");
        
        if (_strModelType.equalsIgnoreCase("VARNER_FLAT_FILE"))
        {
            // Get the data from the hastable -
            String strData = _htblDataBlocks.get("FILENAME_NETWORK");
        
            // Formulate the string buffer -
            
            strBuffer.append("\t<payload>\n");
            
            // Ok, so we are going to need to parse
            strBuffer.append(strData);
            
            
            strBuffer.append("\t</payload>\n");
                      
        }
        else if (_strModelType.equalsIgnoreCase("SBML_FILE"))
        {
            
            // Get the sbml data -
            String strData = _htblDataBlocks.get("SBML_FILENAME");
            String strWorkingDir = (String)getProperty("CODEGEN_WORKING_DIR");
            String strJobDir = (String)getProperty("JOB_DIR");
            
            // Load the properties from the model -
            strBuffer.append("\t<property logic_handler=\"org.varnerlab.universal.base.Translator\" />\n");
            strBuffer.append("\t<property input_handler=\"org.varnerlab.universal.base.sbml.LoadSBMLFile\"/>\n");
            
            // Ok, when I get here I need to pull out the filenames from the vector -
            Vector tmpVec = (Vector)getProperty("FILENAME_VECTOR");
            
            // if we are SBML then I should have only one file -
            String strTmp = (String)tmpVec.get(0);
            strBuffer.append("\t<property sbml_file_path=\"");
            strBuffer.append(strTmp);
            strBuffer.append("\"/>\n");
            strBuffer.append("\t<property output_stm_filename=\"Network.net\"/>\n");
            strBuffer.append("\t<property codegen_working_dir=\"");
            strBuffer.append(strWorkingDir);
            strBuffer.append("\"/>\n");
            
            strBuffer.append("\t<property job_dir=\"");
            strBuffer.append(strJobDir);
            strBuffer.append("\"/>\n");
            
            String strOutputType = (String)getProperty("CODE_TYPE");
            if (strOutputType.equalsIgnoreCase("OCTAVE-C"))
            {
                strBuffer.append("\t<property output_handler=\"org.varnerlab.universal.base.octave.WriteOctaveCModel\"/>\n");
                strBuffer.append("\t<property output_massbalances_filename=\"MassBalanceFunctionSpank.cc\"/>\n");
            }
            else if (strOutputType.equalsIgnoreCase("MATLAB-M"))
            {
                strBuffer.append("\t<property output_handler=\"org.varnerlab.universal.base.matlab.WriteMatlabMModel\"/>\n");
                strBuffer.append("\t<property output_input_filename=\"Inputs.m\"/>\n");
                strBuffer.append("\t<property output_kinetics_filename=\"Kinetics.m\"/>\n");
                strBuffer.append("\t<property output_massbalances_filename=\"MassBalances.m\"/>\n");
            }
            
            strBuffer.append("\t<property output_driver_filename=\"SolveMassBalances.m\"/>\n");
            strBuffer.append("\t<property output_datafile_filename=\"DataFile.m\"/>\n");

          
        }
        
         
        strBuffer.append("</universal>\n");    
     
        System.out.println("Buffer - "+strBuffer.toString());
        
        
        
        // Ok, we have formulate the buffer -
        return(strBuffer.toString());
    }
}
