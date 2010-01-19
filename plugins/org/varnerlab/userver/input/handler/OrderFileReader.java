/*
 * OrderFileReader.java
 *
 * Created on June 14, 2006, 4:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.input.handler;

// import statements
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

// import the transport interfaces so we can interact w/the server -
import org.varnerlab.server.transport.*;

/**
 *
 * @author jeffreyvarner
 */
public class OrderFileReader extends Object {
    
    /** Creates a new instance of OrderFileReader */
    public OrderFileReader() {
    }
    
    public void readFile(String fName,Vector list) throws Exception
    {
        // Load the order file
    	FileReader FR = new FileReader(fName);
        BufferedReader r = new BufferedReader(FR);
        String s;
        while ((s=r.readLine())!=null)
        {
        	// Load the symbols into the list -
            list.addElement(s);
        }
    }
    
}
