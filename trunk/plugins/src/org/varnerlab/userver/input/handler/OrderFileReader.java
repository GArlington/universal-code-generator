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
 * 
 * OrderFileReader.java
 *
 * Created on June 14, 2006, 4:23 AM
 */

package org.varnerlab.userver.input.handler;

// import statements
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

// import the transport interfaces so we can interact w/the server -
import org.varnerlab.server.localtransportlayer.*;

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
