/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.server.transport;

import java.util.Hashtable;
import java.io.*;
//import org.varnerlab.universal.server.IVLValidateServerJob;
//import org.varnerlab.universal.server.VLServerSession;

/**
 *
 * @author jeffreyvarner
 */
public class RemoteDirLookup implements IVLValidateServerJob {
    // Class/instance attributes -
    private Hashtable _propTable = null;


    // We need to modify this so that dirs are displayed -
    public Object validateJob(Object object) throws Exception {
        // Method attributes -
        // Ok, write this mofo -
        VLServerSession session = (VLServerSession)object;
        StringBuffer buffer = new StringBuffer();
        String strNewDir = "";
        
        // Get the path to the new dir -
        String strDirName = (String)session.getProperty("WORKING_DIR");
        String strUserName = (String)session.getProperty("USERNAME");
        String strSessionID = (String)session.getProperty("SESSIONID");
        String strSubDirName = (String)session.getProperty("SUBPATH");
        
        System.out.println("What is the val and length of strSubDir - "+strSubDirName+" and "+strSubDirName.length());
        
        
        // Ok, lets create a new dir -
        // String strNewDir = strDirName+"/"+strUserName+"/"+strSessionID;
        if (!strSubDirName.equalsIgnoreCase("-1"))
        {
        	strNewDir = strSubDirName;
        }
        else
        {
        	strNewDir = strDirName+"/"+strUserName;
        }
        
        
        System.out.println("Going to read the dir -"+strNewDir);

        // Get the files in the dir -
        File file = new File(strNewDir);
        File[] files = file.listFiles();

        
        buffer.append("<?xml version=\"1.0\"?>");
        buffer.append("<");
        buffer.append(strUserName);
        buffer.append(" name=\"");
        buffer.append(strUserName);
        buffer.append("\" path=\"");
        buffer.append(file.getPath());
        buffer.append("\">");
        // Ok, let's get file names and put them in a string buffer -
        int NUMBER_OF_FILES = files.length;
        for (int index=0;index<NUMBER_OF_FILES;index++)
        {
            // Get the file -
            getFileName(files[index],buffer);
        }

        buffer.append("</");
        buffer.append(strUserName);
        buffer.append(">");
        
        // Return a string buffer to caller -
        return(buffer);
    }

    private void getFileName(File file,StringBuffer buffer)
    {
        // Method attributes -
       

        // Ok, so when I get I need to check to see if the file is a dir -
        if (file.isDirectory())
        {
            buffer.append("<Directory name=\"");
            buffer.append(file.getName());
            buffer.append("\" path=\"");
            buffer.append(file.getPath());
            buffer.append("\">");
            buffer.append("");

            File[] kids = file.listFiles();
            int NUMBER_OF_KIDS = kids.length;
            for (int index=0;index<NUMBER_OF_KIDS;index++)
            {
                // Get the files in my dir -
                getFileName(kids[index],buffer);
            }

            // Close out the dir -
            buffer.append("</Directory>");

        }
        else
        {
            // If I get here, then I do *not* have a dir - grab the name and return -
            buffer.append("<File name=\"");
            buffer.append(file.getName());
            buffer.append("\" path=\"");
            buffer.append(file.getPath());
            buffer.append("\"/>");
            
        }

    }

}
