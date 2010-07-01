/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.service;

// import -
import java.net.*;
import java.io.*;
import org.varnerlab.universaleditor.domain.UEditorSession;


/**
 *
 * @author jeffreyvarner
 */
public class SocketService {


    // static -
    public static String sendMessage(String data,String strIPAddress,String strPort,UEditorSession session,ServerJobTypes type) throws Exception
    {
        // Method attributes -
        StringBuffer tmpBuffer = new StringBuffer();
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader inputStream = null;


        // Create a new socket to send messages -
        socket = new Socket(strIPAddress, Integer.parseInt(strPort));

        // Get the input and output streams -
        printWriter = new PrintWriter(socket.getOutputStream(),true);
        inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Need to formulate the routing string -
        StringBuffer buffer = new StringBuffer();
        String strRoutingInformation = "";
        switch (type)
        {
        	case MAKE_NEW_REMOTE_DIRECTORY:
        		buffer.append((String)session.getProperty("VALIDATED_USERNAME"));	// 
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SESSION_ID"));
        		buffer.append("::");
        		buffer.append("-1");				// subdir is null -
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append("MAKE_NEW_REMOTE_DIRECTORY");
        		break;
        	
        	case DELETE_PROJECT_ON_SERVER:
        		buffer.append((String)session.getProperty("VALIDATED_USERNAME"));	// 
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SESSION_ID"));
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SELECTED_REMOTE_PATH"));
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append("DELETE_PROJECT_ON_SERVER");
        		break;
        	
        	case DELETE_FILE_ON_SERVER:
        		buffer.append((String)session.getProperty("VALIDATED_USERNAME"));	// 
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SESSION_ID"));
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SELECTED_REMOTE_PATH"));
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append("DELETE_FILE_ON_SERVER");
        		break;
        		
        	case PROJECT_DIRECTORY_LOOKUP:
        		buffer.append((String)session.getProperty("VALIDATED_USERNAME"));	// 
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append("-1");				
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append("PROJECT_DIRECTORY_LOOKUP");
        		break;
        	
        	case TRANSFER_FILE_TO_SERVER:
        		buffer.append((String)session.getProperty("VALIDATED_USERNAME"));	// 
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SESSION_ID"));
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SELECTED_REMOTE_PATH"));				// subdir is null -
        		buffer.append("::");
        		buffer.append((String)session.getProperty("FILENAME"));
        		buffer.append("::");
        		buffer.append("TRANSFER_FILE_TO_SERVER");
        		break;
        		
        	case TRANSFER_FILE_FROM_SERVER:
        		buffer.append((String)session.getProperty("VALIDATED_USERNAME"));	// 
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append("-1");				// subdir is null -
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append("TRANSFER_FILE_FROM_SERVER");
        		break;
        	
        	case REMOTE_DIRECTORY_LOOKUP:
        		buffer.append((String)session.getProperty("VALIDATED_USERNAME"));	// 
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SESSION_ID"));
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SELECTED_REMOTE_PATH"));				// subdir is null -
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append("REMOTE_DIRECTORY_LOOKUP");
        		break;
        	
        	case EXECUTE_UNIVERSAL_JOB:
        		buffer.append((String)session.getProperty("VALIDATED_USERNAME"));	// 
        		buffer.append("::");
        		buffer.append("-1");
        		buffer.append("::");
        		buffer.append((String)session.getProperty("SELECTED_REMOTE_PATH"));				// subdir is null -
        		buffer.append("::");
        		buffer.append((String)session.getProperty("CURRENT_MODEL_PROP_FILENAME"));
        		buffer.append("::");
        		buffer.append("EXECUTE_UNIVERSAL_JOB");
        		break;
        }
        

        // grab the buffer and put into a string -
        strRoutingInformation = buffer.toString();
        
        //PublishService.submitData(strRoutingInformation);

        // Formulate the meassge and send -
        String strData = strRoutingInformation+"\n"+data;

        printWriter.println(strData);
        printWriter.flush();
        
        // Read the response -
        String line = "";
        while ((line=inputStream.readLine())!=null)
        {
            tmpBuffer.append(line);
            tmpBuffer.append("\n");
        }
        
        //tmpBuffer.append(inputStream.readLine());
        
        // Close the input and output -
        printWriter.close();
        inputStream.close();
        //socket.close();

        // return -
        return(tmpBuffer.toString());
    }


}
