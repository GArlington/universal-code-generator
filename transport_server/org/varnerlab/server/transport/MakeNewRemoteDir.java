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
public class MakeNewRemoteDir implements IVLProcessServerRequest {
    // Class/instance attributes -
    private VLServerSession _propTable = null;


    private void writeResource(Object object) throws Exception {

        // Ok, write this mofo -
    	_propTable = (VLServerSession)object;
    	
        // Get the path to the new dir -
        String strDirName = (String)_propTable.getProperty("WORKING_DIR");
        String strUserName = (String)_propTable.getProperty("USERNAME");
        String strSessionID = (String)_propTable.getProperty("SESSIONID");

        // Ok, lets create a new dir -
        String strNewDir = strDirName+"/"+strUserName+"/"+strSessionID;

        System.out.println("Going to make the dir -"+strNewDir);

        // Create new dir -
        (new File(strNewDir)).mkdirs();
        
        // Create a hidden tmp dir -
        String strNewHiddenDir = strDirName+"/"+strUserName+"/"+strSessionID+"/"+".tmp";
        (new File(strNewHiddenDir)).mkdirs();
    }

	public Object getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean processMessage(String strMessage, VLServerSession session,Logger log) throws Exception {
		
		// ok - call the writeResource method -
		writeResource(session);
		
		return true;
	}

}
