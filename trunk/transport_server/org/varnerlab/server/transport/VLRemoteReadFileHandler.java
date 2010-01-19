/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.server.transport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringReader;

import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author jeffreyvarner
 */
public class VLRemoteReadFileHandler implements IVLProcessServerRequest {

    public boolean processMessage(String buffer,VLServerSession session) throws Exception {
        // Class/instance attributes -
        boolean rFlag = true;
        String line;
        Hashtable table = new Hashtable();
        String strTmp = "";
        StringBuffer tmpBuffer = new StringBuffer();

        // Fire up the SAX parser -
        SAXParserFactory factorySAX = SAXParserFactory.newInstance();
        SAXParser saxParser = factorySAX.newSAXParser();

        // Create and condigure a content handler -
        VLPropDefaultHandler handler = new VLPropDefaultHandler();
        VLSimulationDescriptor descriptor = new VLSimulationDescriptor();
        handler.setFactory((IConfigurable) descriptor);

        saxParser.parse(new InputSource(new StringReader(buffer)),handler);

        // Ok, when I get here I have the filename and am ready to load the file -
        // Get the working dir -
        String strCodePath = (String)session.getProperty("WORKING_DIR");
        String strUserName = (String)session.getProperty("USERNAME");
        String strSessionID = (String)session.getProperty("SESSIONID");

        // Get the pathinfo -
        String strFileNamePath = (String)descriptor.getProperty("REMOTEFILENAME");

        // ver alpha of the code -
        // Ok, lets create a new dir -
        // String strNewDir = strCodePath+"/"+strUserName+"/"+strSessionID+"/"+strFileNamePath;
        // String strNewDir = strCodePath+"/"+strFileNamePath;
        
        System.out.println("Going to load - "+strFileNamePath);
        // String dataBuffer = (GIOL.readNewLine(strFileNamePath)).toString();

        File iFile=new File(strFileNamePath);
        BufferedReader reader=new BufferedReader(new FileReader(iFile));
        String s="";
        while ((s=reader.readLine())!=null)
        {
        	tmpBuffer.append(s);
            tmpBuffer.append("\n");
        }
        // Ok, when I get here what do I do? --
        String dataBuffer = tmpBuffer.toString();
        
        // Cut out the file name -
        int last_slash = strFileNamePath.lastIndexOf("/");
        if (last_slash!=-1)
        {
            strTmp = strFileNamePath.substring(last_slash+1, strFileNamePath.length());
        }
        else
        {
            strTmp = strFileNamePath;
        }

        // Wrap -
        StringBuffer rBuffer = new StringBuffer();
        rBuffer.append("<?xml version=\"1.0\"?>");
        rBuffer.append("<VLUniversal>");
        rBuffer.append("<Data name=\"");
        rBuffer.append(strTmp);
        rBuffer.append("\">");
        rBuffer.append("<![CDATA[");
        rBuffer.append(dataBuffer);
        rBuffer.append("]]>");
        rBuffer.append("</Data>");
        rBuffer.append("</VLUniversal>");

        // Put the buffer in the session object -
        session.setProperty("DATA_STRING_BUFFER", rBuffer);

        // return -
        return(rFlag);
    }

    public Object getResources() {
        return(null);
    }

}
