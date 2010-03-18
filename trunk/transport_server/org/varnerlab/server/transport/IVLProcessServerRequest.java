/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.server.transport;

// import statements -
import java.io.*;
import java.net.*;
import java.util.logging.Logger;

/**
 *
 * @author jeffreyvarner
 */
public interface IVLProcessServerRequest {

    

    // This method is implemented by logic written by the user to process the request -
    public boolean processMessage(String strMessage,VLServerSession session,Logger log) throws Exception;
    public Object getResources();

}
