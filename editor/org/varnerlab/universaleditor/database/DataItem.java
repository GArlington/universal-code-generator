/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.database;

// import statements -
import java.util.Hashtable;


/**
 *
 * @author jeffreyvarner
 */
public class DataItem {
    // Class/instance attributes -
    private Hashtable _propTable = new Hashtable();
    
    public void setProperty(Object key, Object val)
    {
        _propTable.put(key, val);
    }
    
    
    public Object getProperty(Object key)
    {
        return(_propTable.get(key));
    }

}
