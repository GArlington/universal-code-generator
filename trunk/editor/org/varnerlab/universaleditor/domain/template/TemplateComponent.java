package org.varnerlab.universaleditor.domain.template;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class TemplateComponent {
	// Class attributes -
	Hashtable _propTable = null;
    Vector _vecChildren = null;
    TemplateComponent _parent = null;
    
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

    public void setParent(TemplateComponent parent)
    {
        _parent = parent;
    }

    public TemplateComponent endSetup() {
        return(this);
    }
}
