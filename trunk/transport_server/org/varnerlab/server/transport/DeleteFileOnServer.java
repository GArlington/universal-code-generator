package org.varnerlab.server.transport;

import java.io.File;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

public class DeleteFileOnServer implements IVLValidateServerJob {

	
	public Object validateJob(Object object) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer();
		VLServerSession session = (VLServerSession)object;
		
		// Get the xml -
		String strXML = ((StringBuffer)session.getProperty("MESSAGE_BUFFER")).toString();
		
		// Get the XML message from the sever session -
		// Fire up the SAX parser -
        SAXParserFactory factorySAX = SAXParserFactory.newInstance();
        SAXParser saxParser = factorySAX.newSAXParser();

        // Create and condigure a content handler -
        VLPropDefaultHandler handler = new VLPropDefaultHandler();
        VLSimulationDescriptor descriptor = new VLSimulationDescriptor();
        handler.setFactory((IConfigurable) descriptor);
        saxParser.parse(new InputSource(new StringReader(strXML)),handler);
        
        // Get the pathinfo -
        String strFileNamePath = (String)descriptor.getProperty("REMOTEFILENAME");

        // Delete the file -
        File targetFile = new File(strFileNamePath);
        if (targetFile.isFile() && targetFile.exists())
        {
        	targetFile.delete();
        }
		
		
		return(buffer);
	}

}
