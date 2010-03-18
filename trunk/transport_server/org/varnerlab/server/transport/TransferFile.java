/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.server.transport;

import java.util.Hashtable;
import java.util.logging.Logger;
import java.io.*;

/**
 *
 * @author jeffreyvarner
 */
public class TransferFile implements IVLProcessServerRequest {
    // Class/instance attributes -
    private VLServerSession _propTable = null;


    private void writeResource(Object object) throws Exception {

        // Ok, write this mofo -
        StringBuffer tmp = (StringBuffer)object;
        String strNewDir = "";
        
        // Get the path to the new dir -
        String strDirName = (String)_propTable.getProperty("WORKING_DIR");
        String strUserName = (String)_propTable.getProperty("USERNAME");
        String strSessionID = (String)_propTable.getProperty("SESSIONID");
        String strFileName = (String)_propTable.getProperty("FILENAME");
        String strSubDirName = (String)_propTable.getProperty("SUBPATH");
        
        System.out.println("What is the val and length of strSubDir - "+strSubDirName+" and "+strSubDirName.length());
        
        // Ok, lets create a new dir -
        if (!strSubDirName.equalsIgnoreCase("-1"))
        {
        	strNewDir = strSubDirName+"/"+strFileName;
        }
        else
        {
        	strNewDir = strDirName+"/"+strUserName+"/"+strSessionID+"/"+strFileName;
        }
        

        System.out.println("Trying to write to - "+strNewDir);
        
        // Write this file to disk -
        GIOL.write(strNewDir, tmp);
    }

	public Object getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean processMessage(String strMessage, VLServerSession session,Logger log)
			throws Exception {
		
		// ok, bitches - grab session and send the strMessage into the writeResources -
		_propTable = session;
		
		// you heard the word...we are oscar mike
		// Create a string buffer - (hack because of legacy code...)
		StringBuffer tmpBuffer = new StringBuffer(strMessage);	
		writeResource(tmpBuffer);
		
		// we never really implemented this ... doesn't matter what it returns -
		return false;
	}

}
