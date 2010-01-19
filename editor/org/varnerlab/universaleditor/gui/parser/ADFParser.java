/*
* ADFParser.java 1.0 Wed Jun 13 21:03:13 EST 2001
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
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import java.io.IOException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import java.io.*;
import java.util.*;

import universaleditor.VLObject;

/**
 * ADFParser - XML parser used to process ADF configuration files
 */
public class ADFParser extends VLObject implements IDContentHandler {
    // Class/instance attributes
    private static ADFParser m_this=null;                   // Internal reference used for singleton
    private Vector m_constructs=null;                       // AkivaComposite constructed from XML
    private int[] m_intDimension=null;                      // int array containing matrix dimension
    
    /** Constructor - no arg */
    private ADFParser(){
        super();
    }
    
    /**
     * setDimension() - Sets the dimension of an array
     */
    public void setDimension(int[] dim){
        this.m_intDimension=dim;
    }
    
    /**
     * getDimension() - Returns int array holding dimension of array
     */
    public int[] getDimension(){
        return(m_intDimension);
    }
    
    /**
     * getInstance() - Static accessor method
     */
    public static ADFParser getInstance(){
        if (m_this==null){
            m_this=new ADFParser();
        }
        return(m_this);
    }
    
    /**
     * getConstructs() - Method called Builder to get constructed trees
     */
    public Vector getConstructs(){
        return(m_constructs);
    }
    
    /**
     * getConstructChildren() - Returns an enumeration of the children created by the parser
     */
     public Enumeration getConstructChildren(){
        return(m_constructs.elements());
     }
    
    /**
     * setConstructs() - Method called by ContentHandler to set collection of constructed trees
     */
    public void setConstructs(Object vec){
        this.m_constructs.addElement(vec);
    }
    
    /**
     * parse() - Parsing method
     */
    public void doParse(String xmlPacket,int type){
        // Method attributes
        ContentHandler contentHandler=null;
        boolean isStartup=false;
        InputSource xmlInput=null;
        
        SAXParserFactory saxPFactory=null;
        SAXParser saxParser=null;
        XMLReader parser=null;
        
        try {
             // init
            m_constructs=new Vector();
            
            // Get instances of our handlers
            switch (type){
                case (IDContentHandler.CHID_STARTUP_HANDLER):
                    //contentHandler=new ADFStartupHandler(this);
                    break;
                case (IDContentHandler.CHID_BUILDER_HANDLER):
                    //contentHandler=new ADFBuilderHandler(this);
                    break;
                case (IDContentHandler.CHID_PARAMETER_HANDLER):
                    //contentHandler=new ADFParameterHandler(this);
                    break;
                case (IDContentHandler.CHID_MATRIX_HANDLER):
                    //contentHandler=new ADFMatrixHandler(this);
                    break;
                case (IDContentHandler.CHID_STARTUP_BUILDER_HANDLER):
                    //contentHandler=new ADFStartupBuilderHandler(this);
                    
                    // Set the startup flag
                    isStartup=false;
                   
                    // Break out of this case
                    break;
                case (IDContentHandler.CHID_ARRAY_HANDLER):
                    //contentHandler=new ADFArrayHandler(this);
                    break;
                case (IDContentHandler.CHID_WIDGET_HANDLER):
                    contentHandler=new WidgetBuilderContentHandler(this);
                    break;
                case (IDContentHandler.CHID_XMLTREE_HANDLER):
                    contentHandler=new XMLTreeBuilderContentHandler(this);
                    break;
                case (IDContentHandler.CHID_SBMLTREE_HANDLER):
                    contentHandler=new SBMLTreeBuilderContentHandler(this);
                    break;
            }
            
             // Startup XMLReader
            saxPFactory=SAXParserFactory.newInstance();
            saxPFactory.setValidating(false);
            saxParser=saxPFactory.newSAXParser();
            parser=saxParser.getXMLReader();
            
            
            // Register contentHandler
            parser.setContentHandler(contentHandler);
            
            // Wrap reader in InputSource    
            if (isStartup){
                
                // Reset startup flag
                isStartup=false;
            }
            else {
                // Input for not simulation inputs
                xmlInput=new InputSource(new FileReader(new File(xmlPacket)));
                
                // Parse xmlData
                parser.parse(xmlInput);
            }
        }
        catch (Exception error){
           error.printStackTrace();
        }
    }
    
     /**
     * parse() - Parsing method
     */
    public void doParse(Reader xmlPacket,ContentHandler contentHandler){
        try {
             // Startup XMLReader
            SAXParserFactory saxPFactory=null;
            SAXParser saxParser=null;
            XMLReader parser=null;
        
            saxPFactory=SAXParserFactory.newInstance();
            saxPFactory.setValidating(false);
            saxParser=saxPFactory.newSAXParser();
            parser=saxParser.getXMLReader();
            
            
            // Register contentHandler
            parser.setContentHandler(contentHandler);
            
            // Wrap reader in InputSource
            InputSource xmlInput=new InputSource(xmlPacket);
            
            // Parse xmlData
            parser.parse(xmlInput);
        }
        catch (Exception error){
           debug(error.toString()); 
        }
    }
    
    /**
     *  Helper method that returns the simulation file name
     *  @param Full path to simulation file
     *  @throws Exception
     */
    private String getSimFileName(String path) throws Exception {
        Vector tmp=new Vector();
        StringTokenizer cut=new StringTokenizer(path,System.getProperty("file.separator"));
        
        // Load vector w/chunks
        while (cut.hasMoreTokens()){
            tmp.addElement(cut.nextToken());
        }
        
        // Get last chunk
        String strTmp=(String)tmp.lastElement();
        
        // Cut off .sim
        int idx=strTmp.indexOf(".");
        strTmp=strTmp.substring(0,idx);
        
        // Debug
        System.out.println(strTmp);
        
        // Return
        return(strTmp);
    }
    
    
    /**
     * main() - Test harness
     */
    public static void main(String[] args){
        ADFParser t=ADFParser.getInstance();
        
        try {
            // Reader
            t.doParse("//home//jvarner//dev//akiva//work//glucose.sim",IDContentHandler.CHID_STARTUP_BUILDER_HANDLER);
            //t.doParse("c:\\model\\bin\\flux.xml",IDContentHandler.CHID_BUILDER_HANDLER);
            
        }
        catch (Exception error){
            System.out.println(error.toString());
        }
    }
}