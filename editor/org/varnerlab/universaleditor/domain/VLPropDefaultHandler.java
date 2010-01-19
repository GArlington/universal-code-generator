package org.varnerlab.universaleditor.domain;

// import statements
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;
import java.util.Enumeration;
import java.io.Serializable;


public class VLPropDefaultHandler extends DefaultHandler implements Serializable {
    // Class/instance attributes
    private IConfigurable _factoryReference;            // Reference to the factory that I'm configuring here
    
    
    // set reference methods
    public void setFactory(IConfigurable factory){
        this._factoryReference=factory;
    }
    
    
    /**
     * startElement - XML Parser calls this method when the start element tag is reached
     */
    public void startElement(String URI, String localName, String rawName, Attributes attributes) throws SAXException {   
        // Method attributes
        Properties props=new Properties();
        
        // If localname is properties then skip as this is the first tag
        if (rawName.equalsIgnoreCase("property")){
            
            // Processing...
            System.out.println("Processing - "+rawName);
            
            
            // Get number of attributes
            int iAttrs = (attributes != null) ? attributes.getLength() : 0;
                
            // Grab attributes (repackage into properties object)
            props.clear();
            for (int q = 0; q < iAttrs; q++){
                props.put(attributes.getQName(q),attributes.getValue(q));
            }
            
            // Set the properties of the factory
            setProperties(_factoryReference,props);
            
            // end outer if
        }



    }
    
    // Protected method that sets the properties of an object
    protected void setProperties(IConfigurable factory,Properties props){
        
        try {
            // Iterate through properties for this component
            Enumeration enumKeys=props.keys();
            while (enumKeys.hasMoreElements()){
                // Get keyName
                String keyName=(String)enumKeys.nextElement();
              
                // Set property
                factory.setProperty(keyName.toUpperCase(),props.getProperty(keyName));
            }
        }
        catch (Exception error){
            // We'll need to put logging in here before I give to Pete
        }
    }
}
