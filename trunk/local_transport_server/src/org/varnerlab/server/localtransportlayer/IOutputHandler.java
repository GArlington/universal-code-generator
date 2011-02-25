package org.varnerlab.server.localtransportlayer;

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


import java.util.Hashtable;
import java.util.logging.Logger;


/**
 * Interface to be implemented for all UNIVERSAL OutputHandler classes
 * @author jeffreyvarner
 */
public interface IOutputHandler {
	public void setProperties(Hashtable prop);
    // dump the resource -
    
	/**
     * Generates and writes OutputHandler specific code for a given model.
     * The given model is passed in as an SBML model object.
     * @param object    an SBML model object that represents the current model
     * @throws Exception
     */
    public void writeResource(Object object) throws Exception;
    
    /**
     * Used by Universal to pass required properties to the OutputHandler.
     * The XMPPropTree is a warpper for the XML tree edited in the GUI.
     * For more see the javadoc associated with XMLPropTree class.
     * @param prop  an XMLPropTree object containing various properties
     */
    public void setProperties(XMLPropTree prop);
    
    /**
     * Passes an instance of the UNIVERSAL Logger instance to the OutputHandler 
      * The GUI captures all standard out and standard error 
     * @param log   Logger instance created by UNIVERSAL
     */
    public void setLogger(Logger log);
}

