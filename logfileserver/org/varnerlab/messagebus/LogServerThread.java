/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package org.varnerlab.messagebus;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LogServerThread extends Thread {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean moreQuotes = true;
    
    private String _strWorkingDir = null;
    private Document _propTreeXML = null;
    private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();

    public LogServerThread(Document doc) throws IOException {
    	super("LogServerThread");
    	
    	// Grab the document tree -
    	_propTreeXML = doc;
    	
    	// Process the info in the tree and initialize the socket -
    	configureThread();
    }

    private void configureThread()
    {
    	try
    	{
    		// Load the tmpfile that I'm watching -
    		String strXPathPort = "//MessageServer/port/text()";
    		Node portNode = (Node)_xpath.evaluate(strXPathPort, _propTreeXML, XPathConstants.NODE);
			int PORT = Integer.parseInt(portNode.getNodeValue());
			
			String strXPathWorkDir = "//MessageServer/working_directory/text()";
    		Node wdNode = (Node)_xpath.evaluate(strXPathWorkDir, _propTreeXML, XPathConstants.NODE);
    		_strWorkingDir = wdNode.getNodeValue();
			
    		/*
			String strXPathTopic = "//MessageServer/Topic";
    		NodeList topicList = (NodeList)_xpath.evaluate(strXPathTopic, _propTreeXML, XPathConstants.NODESET);
    		int NUMBER_OF_TOPICS = topicList.getLength();
    		String strTmp = "";
    		*/
    		
    		// Create the socket -
    		socket = new DatagramSocket(PORT);
    	}
    	catch (Exception error)
    	{
    		error.printStackTrace();
    	}
    }
    
    public void run() {
    	
    	boolean isOk = true;
    	
    	System.out.println("Started message server - ");
    	
    	while (isOk) 
    	{
            try 
            {
                	
            	byte[] buf_send = null;
            	byte[] buf_rcv = new byte[1024];
            	StringBuffer tmpBuffer = new StringBuffer();

                // receive request
                DatagramPacket packet = new DatagramPacket(buf_rcv, buf_rcv.length);
                socket.receive(packet);
                
                // Ok, when I get here I have rcvd a packet (the socket.receive(..) blocks). Need to get some data from the incoming packet -
                String strClientMessage = new String(packet.getData(),0,packet.getLength());
                
                System.out.println("We have rcvd the following request - "+strClientMessage);        
                
                // The client message should contain the working directory that we need to look at -
                if (!strClientMessage.isEmpty())
                {
                	// Get the path -
                	String strPath = _strWorkingDir+strClientMessage+"/"+".tmp/job.log";
                	
            
                	tmpBuffer.append("START ==********************************************************************************************* START \n");
                	
                	// Ok, we need to put some logic here to see if I can load the file -
                	File fileTmp = new File(strPath);
                	if (fileTmp.exists())
                	{
                		// Load the file -
                		in = new BufferedReader(new FileReader(strPath));
                	
                		String s = "";
                		while ((s = in.readLine())!=null)
                		{
                			tmpBuffer.append(s);
                			tmpBuffer.append("\n");
                		}
                	}
                	else
                	{
                		tmpBuffer.append("ERROR: ");
                		tmpBuffer.append("File not found - ");
                		tmpBuffer.append(strClientMessage);
                		tmpBuffer.append("\n");
                	}
                }
                else
                {
                	tmpBuffer.append("ERROR: ");
                	tmpBuffer.append(" Request from the client is null? ");
                	tmpBuffer.append("\n");
                }
                
                tmpBuffer.append("END ************************************************************************************************* END");
                
                // Send the last line -
                buf_send = tmpBuffer.toString().getBytes();
                
                // send the response to the client at "address" and "port"
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf_send, buf_send.length, address, port);
                socket.send(packet);
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
    	socket.close();
    }

}
