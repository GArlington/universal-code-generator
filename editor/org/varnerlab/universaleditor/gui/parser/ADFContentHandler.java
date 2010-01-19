/*
* ADFContentHandler.java 1.0 Wed Jun 13 21:03:13 EST 2001
*
* Copyright 2001 by Akiva Software LLc,
* 8512 Cabana Dr. Suite 208, Fishers IN 46038, U.S.A 
* All rights reserved.
* 
* This software is the confidential and proprietary infomation of Akiva Software LLc 
* ("Confidential Information"). You shall not disclose such Confidential Information 
* and shall use it only in accordance with the terms of the license agreement that you 
* entered into with Akiva. 
*/

package org.varnerlab.universaleditor.gui.parser;

// Import statements
import java.io.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParser;
import java.util.*;
import universaleditor.VLObject;

/**
 * ADFContentHandler - Contains logic for parsing ADF XML
 */
public class ADFContentHandler extends VLObject implements ContentHandler {
    
    /**
     * ADFContentHandler() - Constructor
     */
    public ADFContentHandler(ADFParser parser){
        super();
    }
    
    
    /** Do nothing constructor */
    public ADFContentHandler(){
    }

    /**
     * startElement() - Parse XML element calls helper method parseXMLAttribute
     */
    public void startElement(String URI, String localName, String rawName, Attributes attributes) throws SAXException {   
    }
    
    /**
     * parseXMLAttributes - Helper method with specialized logic for stoichiometry matrix
     */
    protected void parseXML(String localName,String rawName,Attributes atts) throws SAXException {
    }
    
    /**
     * characters() - Parsing logic for XML elements
     */
    public void characters(char[] ch, int start, int end) throws SAXException {
    }

    public void endDocument() throws SAXException {
    }
    
    public void ignorableWhitespace(char[] values, int param, int param2) throws SAXException {
    }
    
    public void endElement(String str, String str1, String str2) throws SAXException {
    }
    
    public void skippedEntity(String str) throws SAXException {
    }
    
    public void processingInstruction(String str, String str1) throws SAXException {
    }
    
    public void endPrefixMapping(String str) throws SAXException {
    }
    
    public void startPrefixMapping(String str, String str1) throws SAXException {
    }
    
    public void setDocumentLocator(Locator locator) {
    }
    
    public void startDocument() throws SAXException {
    }
}
