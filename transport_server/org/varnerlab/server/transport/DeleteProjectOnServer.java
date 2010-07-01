package org.varnerlab.server.transport;

import java.io.File;

public class DeleteProjectOnServer implements IVLValidateServerJob {

	public Object validateJob(Object object) throws Exception {
		// Method attributes -
		VLServerSession session = (VLServerSession)object;
		
		// Get the path to the dir (project that I'm in...)
        String strDirName = (String)session.getProperty("WORKING_DIR");
        String strUserName = (String)session.getProperty("USERNAME");
        String strSessionID = (String)session.getProperty("SESSIONID");
		
        // Ok, lets create a new dir -
        String strProjectDir = strDirName+"/"+strUserName+"/"+strSessionID;
		
        // Ok, execute this mofo ...
        (new File(strProjectDir)).delete();
        
		// return
		return(null);
	}

}
