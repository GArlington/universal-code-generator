/*
 * VarnerLabObjectFactory.java
 *
 * Created on March 4, 2007, 9:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.server.localtransportlayer;

/**
 *
 * @author jeffreyvarner
 */
public class VarnerLabObjectFactory {
    // Class/instance attributes -
    private static VarnerLabObjectFactory _this = null;
    
    /**
     * Creates a new instance of VarnerLabObjectFactory
     */
    private VarnerLabObjectFactory() {
    }
    
    public static VarnerLabObjectFactory getInstance()
    {
        if (_this == null)
        {
            _this = new VarnerLabObjectFactory(); 
        }
        else
        {
            return(_this);
        }
            
        // return the object -
        return(_this);
    }
    
    public Object buildObject(String str) throws Exception
    {
        // Method attributes -
        Object obj = null;              // return this object -
        
        
        // Load the object -
        obj = Class.forName(str).newInstance();
        
        // return the object -
        return(obj);
    }
    
    // method to configure an object 0
    public void configureObject() throws Exception 
    {
        
    }
    
}
