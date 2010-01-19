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
    public static String sendMessage(String data,String strIPAddress,String strPort,UEditorSession session) throws Exception
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
        String strRoutingInformation = (String)session.getProperty("VALIDATED_USERNAME")+"::"+(String)session.getProperty("SESSION_ID")+"::"+(String)session.getProperty("FILENAME");

        PublishService.submitData(strRoutingInformation);

        // Formulate the meassge and send -
        String strData = strRoutingInformation+"\n"+data;


        //PublishService.submitData(strData);


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
