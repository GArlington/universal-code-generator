/*
 * IConfigurable.java
 *
 * Created on July 15, 2006, 9:12 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.domain;

/**
 *
 * @author jeffreyvarner
 */
public interface IConfigurable {
    
   
    public void setProperty(Object key,Object value);
    public Object getProperty(Object key);
    
}
