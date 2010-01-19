/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.domain;

// import statements -
import java.util.*;

/**
 * This is the *equivalent* of a HTTP session object. When main is started, every user gets a new session object -
 * @author jeffreyvarner
 */
public class UEditorSession implements IConfigurable {
    // Class/instance attributes -
    private Hashtable _propTable = null;
    private String _strSessionID = null;

    public UEditorSession()
    {
        // Create a new Hashtable to hold the session properties -
        _propTable = new Hashtable();
    }


    public void setProperty(Object key,Object value)
    {
       
        System.out.println("Setting key - "+key+" with value -"+value);

        _propTable.put(key, value);
    }

    public Enumeration getKeys()
    {
        return(_propTable.keys());
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
            // Generate a session id -
            _strSessionID = UUID.randomUUID().toString();

            // replace the - with _
            _strSessionID = "ssid_"+_strSessionID.replaceAll("-", "_");

            this.setProperty("SESSION_ID", _strSessionID);
        }

    }

}
