package org.varnerlab.userver.output.handler;

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

//import statements -
import java.util.Properties;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import org.sbml.libsbml.*;
import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.language.handler.*;
import org.varnerlab.userver.language.handler.bcx.BCXGeneralBuilder;
import org.varnerlab.userver.language.handler.bcx.Exp;


public class WriteBCXFiles implements IOutputHandler {
    private XMLPropTree _xmlPropTree = null;
    private Logger _logger = null;

    public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub

    }
    
    public void setProperties(XMLPropTree prop) {
            this._xmlPropTree = prop;
    }
    
	public void writeResource(Object[] expList) throws Exception 
	{
			
	}

	public void writeResource(Object object) throws Exception {
		
		String modelType = _xmlPropTree.getProperty("//Model/code_output_handler/text()"); 
		System.out.println("Ok, we are in the writeResources method of WriteBCXFile. Model type = "+modelType);
		
		// Ok, if all went went well then I have a vector of Experiments -
		Vector<Exp> vecExperiments = (Vector)object;
		
		
		
		if(modelType.equals("org.varnerlab.userver.output.handler.WriteOctaveCModel"))
		{
            String syntax = new String("BCXMatlabOctaveOutputSyntax");
            BCXGeneralBuilder builder = new BCXGeneralBuilder(vecExperiments,syntax,_xmlPropTree);
            builder.buildExpCode(false);
            builder.buildGrapherCode();
            builder.buildOptCode();
        }
        else if (modelType.equals("org.varnerlab.userver.output.handler.WriteOctaveMModel")){
            
        	String syntax = new String("BCXMatlabOctaveOutputSyntax");
            BCXGeneralBuilder builder = new BCXGeneralBuilder(vecExperiments,syntax,_xmlPropTree);
            builder.buildExpCode(false);
            builder.buildGrapherCode();
            builder.buildOptCode();
        }
        else if (modelType.equals("org.varnerlab.userver.output.handler.WriteMatlabMModel")){
        	
            String syntax = new String("BCXMatlabOctaveOutputSyntax");
            BCXGeneralBuilder builder = new BCXGeneralBuilder(vecExperiments,syntax,_xmlPropTree);
            builder.buildExpCode(false);
            builder.buildGrapherCode();
            builder.buildOptCode();
        }
        else {
            System.out.println("Some how you ended up with the wrong BCX output handler");
        }
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
}
