/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.domain;

// Import statements -
import java.util.*;


/**
 *
 * @author jeffreyvarner
 */
public class VLDomainComponent {
    // Class/instance attributes -
    Hashtable _propTable = null;
    Vector _vecChildren = null;
    VLDomainComponent _parent = null;

    // Get and set methods -
    public void setProperty(Object key,Object value)
    {
        System.out.println("key = "+key+" value="+value);

        _propTable.put(key, value);
    }

    public VLDomainComponent()
    {
        _propTable = new Hashtable();
        _vecChildren = new Vector();
    }

    public Object getProperty(Object key)
    {
        return(_propTable.get(key));
    }

    public Enumeration getKeys()
    {
        return(_propTable.keys());
    }

    public void addChild(Object child)
    {
        _vecChildren.add(child);
    }

    public Iterator getChildren()
    {
        return(_vecChildren.iterator());
    }

    public int getNumberOfChildren()
    {
        return(_vecChildren.size());
    }

    public Object getChildAt(int index)
    {
        return(_vecChildren.get(index));
    }

    public void setParent(VLDomainComponent parent)
    {
        _parent = parent;
    }

    public VLDomainComponent endSetup() {
        return(this);
    }

    

}
