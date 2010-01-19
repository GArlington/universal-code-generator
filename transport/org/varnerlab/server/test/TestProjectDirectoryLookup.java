package org.varnerlab.server.test;

//import statements -
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;


public class TestProjectDirectoryLookup {

	/**
	 * Sends a test message to the server to help in debugging and to check to see if it is installed correctly -
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// Method attributes -
		String _strSessionID = UUID.randomUUID().toString();
		String _strUserName = "jdv27";
		String _strIPAddress = "127.0.0.1";
		String _strPort = "4442";
		String _strRoutingLine = "";
		
		StringBuffer tmpBuffer = new StringBuffer();
        Socket socket = null;
        PrintWriter printWriter = null;
        BufferedReader inputStream = null;
        
        // Ok, get the job_type -
        String strJobType = "PROJECT_DIRECTORY_LOOKUP";
		
		try {
			
			// correct the spaces -
            _strSessionID = "ssid_"+_strSessionID.replaceAll("-", "_");
            
            // Create a new socket to send messages -
            socket = new Socket(_strIPAddress, Integer.parseInt(_strPort));

            // Get the input and output streams -
            printWriter = new PrintWriter(socket.getOutputStream(),true);
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Formulate the routing line -
            StringBuffer routingBuffer = new StringBuffer();
            routingBuffer.append(_strUserName);	//	1 - username
            routingBuffer.append("::");
            routingBuffer.append("-1");			//	2 - sessionid
            routingBuffer.append("::");		
            routingBuffer.append("-1");			//	3 - subdir
            routingBuffer.append("::");
            routingBuffer.append("-1");			//  4 - filename
            routingBuffer.append("::");
            routingBuffer.append(strJobType);	//  5 - JobType
            
            
            // That should be it. 
            _strRoutingLine = routingBuffer.toString();
            
            String strUData = "<universal>\n<property take_it_2_the_next_level=\"yes\"/>\n</universal>\n";
            
            
            // Make the message -
            String strData = _strRoutingLine+"\n"+strUData;
			
            System.out.println("Sending - "+strData);
            
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
            System.out.println(tmpBuffer.toString());
            
		}
		catch (Exception error)
		{
			System.out.println("ERROR:Error in SendTestMessage - "+error.toString());
		}
	}

}
