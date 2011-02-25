/*
 * Copyright (c) 2011 Varnerlab, 
 * School of Chemical and Biomolecular Engineering, Cornell
 * University, Ithaca NY 14853 USA.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
