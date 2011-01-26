package org.varnerlab.userver.output.handler;

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
