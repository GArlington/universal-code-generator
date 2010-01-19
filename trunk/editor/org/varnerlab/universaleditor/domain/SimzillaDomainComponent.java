/*
 * SimzillaDomainComponent.java
 *
 * Created on July 15, 2006, 9:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.domain;

// import statements -
import java.util.Hashtable;

/**
 *
 * @author jeffreyvarner
 */
public class SimzillaDomainComponent extends Object implements IConfigurable {
    // Class/instance attributes
    Hashtable propTable;
    
    
    /** Creates a new instance of SimzillaDomainComponent */
    public SimzillaDomainComponent() {
    }
    
    
    public void setProperty(Object key,Object value)
    {
        // Do nothing impl
    }
    
    // Do nothing impl that does nothing, needs to be overriden by subclass
    public Object getProperty(Object key)
    {
        return(null);
    }

}
