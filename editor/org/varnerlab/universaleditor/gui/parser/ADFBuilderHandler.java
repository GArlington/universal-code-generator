package org.varnerlab.universaleditor.gui.parser;

// Import statements
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParser;

import java.lang.reflect.Method;
import java.util.*;
import java.io.*;

/**
 * ADFBuilderHandler - Encapsulates the logic to build rate components
 */
public class ADFBuilderHandler extends ADFContentHandler {
    // Class/instance attributes
    Stack m_stack=null;                         // Object stack
    Object m_akoRoot = null;                    // AkivaObject root
    StringBuffer m_package=null;                // Buffer holding package name
    Vector m_constructs=null;                   // Vector of constructed objects
    ADFParser parser=null;                      // Reference to parser used for callbacks
    
     /** Constructor - no arg */
    public ADFBuilderHandler(ADFParser parser){
        super(parser);
        // Initialize me
        this.init();
        
        // Garb parser reference
        this.parser=parser;
        return;
    }
    
    /** init() - Initialization method */
    private void init(){
        // Initialize stack
        m_stack=new Stack();
        
        // Vector of constructed objects
        m_constructs=new Vector();
    }
    
    /**
     * startElement() - Parse XML element calls helper method parseXMLAttribute
     */
    public void startElement(String URI, String localName, String rawName, Attributes attributes) throws SAXException {   
    }
    
    /**
     * makeSetterName() - Returns the attribute name w/a set appended to it (name of the java method)
     */
    protected String makeSetterName(String strAttrName){
        // Method attributes
        StringBuffer stbfMeth = new StringBuffer("set" + strAttrName);
        
        // Sysout
        // System.out.println("Making setter for the attribure - "+strAttrName);
        
        // Capitalize the 3rd char
        stbfMeth.setCharAt(3, Character.toUpperCase(stbfMeth.charAt(3)));
        
        // Return setter method name
        return(stbfMeth.toString());
    }
    
    
    /**
    * Handles an XML element's closing tag.  Pops the top <code>Part</code>
    * from the stack.
    *
    * @param  strElementName the element name
    * @throws <code>java.lang.Exception</code> if an error occurs
    */
   public void endElement(String strElementName,String str2,String str3) throws SAXException {
    }
    
    /**
     * Method called when document ends. Calls parser and passes a vector holding the constructed objects
     */
    public void endDocument() throws SAXException {
        // Callback parser and set vector of trees
        parser.setConstructs((Object)m_constructs);
    }
    
    
     /**
     * characters() - Parsing logic for XML elements
     */
    public void characters(char[] ch, int start, int end) throws SAXException {
        String s=(new String(ch,start,end)).trim();
    }
    
}