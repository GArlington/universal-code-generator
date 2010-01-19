/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.server.transport;

// import =
import java.util.*;
//import org.varnerlab.universal.base.IConfigurable;

/**
 *
 * @author jeffreyvarner
 */
public class VLServerSession implements IConfigurable {

    // Class/instance attributes -
    private Hashtable _propTable = null;
    private String _strSessionID = null;

    public VLServerSession()
    {
        // Create a new Hashtable to hold the session properties -
        _propTable = new Hashtable();
    }


    public void setProperty(Object key,Object value)
    {
       
        System.out.println("Setting key - "+key+" with value -"+value);

        _propTable.put(key, value);
    }

    public Object getProperty(Object key)
    {
        return(_propTable.get(key));
    }

    
    // Generate a unique ID and store it -
    public void generateSessionID()
    {
        // Generate and store the ID -
        if (_strSessionID == null)
        {
            _strSessionID = UUID.randomUUID().toString();
            this.setProperty("SESSION_ID", _strSessionID);
        }

    }

    public void setProperty(String key, String value) {
        _propTable.put(key, value);
    }

}
