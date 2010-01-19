/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.server.transport;

import java.util.Hashtable;
import java.io.*;

/**
 *
 * @author jeffreyvarner
 */
public class SendStringBufferOnSocket implements IVLValidateServerJob {
    // Class/instance attributes -
    private Hashtable _propTable = null;


    // We need to modify this so that dirs are displayed -
    public Object validateJob(Object object) throws Exception {
        // Method attributes -
        // Ok, write this mofo -
        VLServerSession session = (VLServerSession)object;
        
        StringBuffer buffer = (StringBuffer)session.getProperty("DATA_STRING_BUFFER");

        // System.out.println("Ok, this is it - I'm going to send the following - "+buffer.toString());
        
        // Return a string buffer to caller -
        return(buffer);
    }

}
