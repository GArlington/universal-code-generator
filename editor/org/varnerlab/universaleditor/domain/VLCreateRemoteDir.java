/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.domain;

// Import statements -
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.net.*;
import java.io.*;


/**
 *
 * @author jeffreyvarner
 */
public class VLCreateRemoteDir {
    // Class/instance attributes =
    private Hashtable _propTable = new Hashtable();
    private Socket _socket = null;
    private PrintWriter _printWriter = null;
    
    public void setProperty(Object key,Object val)
    {
        _propTable.put(key, val);
    }
    
    public Object getProperty(Object key)
    {
        return(_propTable.get(key));
    }
    
    public void sendMessage(String xmlMessage) throws Exception
    {
        // Ok, I have the message, I need to open a socket and re-send that crazy mofo -
        // Create the socket -
        
        // Get the coordinates -
        String strIPAddress = (String)getProperty("IPADDRESS");
        String strPort = (String)getProperty("PORT");
        
        
        _socket = new Socket(strIPAddress, Integer.parseInt(strPort));
        _printWriter = new PrintWriter(_socket.getOutputStream(),true);
        

        // Formulate the meassge and send -
        _printWriter.println(xmlMessage);
        _printWriter.flush();
        _printWriter.close();
    }
    
    public String formulateXMLMessage()
    {
        
        // Method attributes -
        StringBuffer strBuffer = new StringBuffer();
        
        // Get the working dir -
        String strUserName = (String)getProperty("USERNAME");
        String strSessionID = (String)getProperty("SESSIONID");
        
        // Start the buffer -
        strBuffer.append("<?xml version=\"1.0\"?>\n");
        strBuffer.append("<universal>\n");
        
        // Set the username -
        strBuffer.append("\t<property username=\"");
        strBuffer.append(strUserName);
        strBuffer.append("\"/>\n");
        
        // Set the sessionid -
        strBuffer.append("\t<property sessionid=\"");
        strBuffer.append(strSessionID);
        strBuffer.append("\"/>\n");

        strBuffer.append("</universal>\n");

        // I'm sending -
        System.out.println(strBuffer.toString());
        
        // return -
        return(strBuffer.toString());
    }
    
}
