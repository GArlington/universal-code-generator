package org.varnerlab.server.transport;

import java.io.File;

public class DeleteProjectOnServer implements IVLValidateServerJob {

	public Object validateJob(Object object) throws Exception {
		// Method attributes -
		VLServerSession session = (VLServerSession)object;
		StringBuffer buffer = new StringBuffer();
		
		// Get the path to the dir (project that I'm in...)
        String strDirName = (String)session.getProperty("WORKING_DIR");
        String strUserName = (String)session.getProperty("USERNAME");
        String strProjectDir = (String)session.getProperty("SUBPATH");
		
        // Ok, lets create a new dir -
        //String strProjectDir = strDirName+"/"+strUserName+"/"+strSessionID;
		
        // Wrap the target dir in a File -
        File targetProjectDir = new File(strProjectDir);
        
        // Ok, so lets see if the dir exists?
        boolean blnFlag = targetProjectDir.exists();
        
        if (blnFlag)
        {
        	
        	// Ok, did this job completo ... I don't know Angel, this looks pretty completo to me...name that show?
            buffer.append("Directory - ");
            buffer.append(strProjectDir);
            buffer.append(" was deleted? ");
        	
        	// Ok, execute this mofo ...
        	processDirectoryTree(targetProjectDir);
        }
        else
        {
        	// Ok, did this job completo ... I don't know Angel, this looks pretty completo to me...name that show?
            buffer.append("Directory - ");
            buffer.append(strProjectDir);
            buffer.append(" doesn't exist? WTF Kennenth? ");
        }
        
        System.out.println(buffer.toString());
        
		// return
		return(buffer);
	}
	
	private void processDirectoryTree(File dir)
	{
		// Ok, let's figure out if this dir has kids -
		File[] contentsArr = dir.listFiles();
		int NFILES = contentsArr.length;
		if (NFILES==0)
		{
			// ok, if I get here then I have an empty list -
			dir.delete();
		}
		else
		{
			for (int index=0;index<NFILES;index++)
			{
				// Get file -
				File tmpFile = contentsArr[index];
				if (tmpFile.isDirectory())
				{
					// if I get here then I have a dir that I need to kia -
					processDirectoryTree(tmpFile);
				}
				else
				{
					// if I get here then I have a file - delete
					tmpFile.delete();
				}
			}
			
			// lastly we need to delete me -
			processDirectoryTree(dir);
		}
	}

}
