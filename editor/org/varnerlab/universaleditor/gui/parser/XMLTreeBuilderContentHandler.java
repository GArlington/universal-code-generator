/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

import org.varnerlab.universaleditor.domain.*;

/**
 *
 * @author jeffreyvarner
 */
public class XMLTreeBuilderContentHandler extends ADFBuilderHandler {

    public XMLTreeBuilderContentHandler(ADFParser parser)
    {
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
    * Handles an XML element's closing tag.  Pops the top <code>Part</code>
    * from the stack.
    *
    * @param  strElementName the element name
    * @throws <code>java.lang.Exception</code> if an error occurs
    */
    /**
    * Handles an XML element's closing tag.  Pops the top <code>Part</code>
    * from the stack.
    *
    * @param  strElementName the element name
    * @throws <code>java.lang.Exception</code> if an error occurs
    */
   public void endElement(String strElementName,String str3,String str2) throws SAXException {

        try {
           if (!str2.equalsIgnoreCase("BCX")){

               VLDomainComponent comp=((VLDomainComponent)m_stack.pop()).endSetup();
               
               if (comp instanceof VLDomainComposite){
                   // Package equation peices into vector
                   m_constructs.addElement(comp);
               }
           }
       }
       catch (Exception error){
           System.out.println(error.toString());
       }
    }

    /**
     * Method called when document ends. Calls parser and passes a vector holding the constructed objects
     */
    public void endDocument() throws SAXException {
        // Callback parser and set vector of trees
        parser.setConstructs((Object)m_constructs);
    }


    /**
     * startElement() - Parse XML element calls helper method parseXMLAttribute
     */
    public void startElement(String URI, String rawName, String localName, Attributes attributes) throws SAXException {
        // Method attributes
        Properties m_propAttrs=new Properties();         // Properties object that holds Attribute names and values

        try {

            if (!localName.equalsIgnoreCase("BCX")){

                // Get number of attributes
                int iAttrs = (attributes != null) ? attributes.getLength() : 0;

                // Grab attributes
                m_propAttrs.clear();
                for (int q = 0; q < iAttrs; q++){
                    //System.out.println(attributes.getQName(q)+"="+attributes.getValue(q));
                    m_propAttrs.put(attributes.getQName(q),attributes.getValue(q));
                }

                // Ok, hard-code the full package name
                m_package=new StringBuffer();
                m_package.append("org.varnerlab.universaleditor.domain.");
                

                // Create AkivaObject
                //System.out.println("Setting localname - "+localName);
                VLDomainComponent _widget=createComponent(localName);

                // Return parent
                VLDomainComponent _parent=checkParent(_widget);

                // Populate object attributes
                setAttributes(_widget,m_propAttrs);

                // Add child to parent
                if (_parent != null){
                    _parent.addChild(_widget);
                }

                // Push part onto stack
                m_stack.push(_widget);

                // Clean-up
                m_package=null;
            }
            else
            {
                m_package=new StringBuffer();
                m_package.append("org.varnerlab.universaleditor.domain.");
            }
        }
        catch (Exception error){
            System.out.println(error.toString());
        }
    }

    

      /**
     * checkRateRoot() - Checks the root element of a rate tree
     */
    private VLDomainComponent checkParent(VLDomainComponent obj){
        // Method attributes
        VLDomainComponent parent=null;

        try {
            if (m_akoRoot == null){
                m_akoRoot = obj;
            }
            if (!m_stack.empty()){
                parent = (VLDomainComponent)m_stack.peek();
                obj.setParent(parent);
            }
        }
        catch (Exception error){
            System.out.println(error.toString());
        }
        return(parent);
    }

    /**
     * createObject() - Private helper method that takes XML start element and builds the corresponding AkivaObject
     */
    private VLDomainComponent createComponent(String name){
        // Method attributes
        VLDomainComponent comp=null;

        try {
            // Build kinetic component
            String className=m_package.append(name).toString();
            
            //System.out.println("what - "+className);
            
            comp=(VLDomainComponent)(Class.forName(className).newInstance());
        }
        catch (Exception error){
            System.out.println(error.toString());
        }
        return(comp);
    }


    /**
     * setAttributes() - Sets the attributes of the object passed in
     */
    private void setAttributes(Object part,Properties props){
        // Method attributes
        Class[] cArray={(new String()).getClass()};         // Everything takes a string

        try
        {

            Class pClass=part.getClass();

            // Iterate through properties for this component
            Enumeration pENUM=props.keys();
            while (pENUM.hasMoreElements()){
                // Get keyName
                String keyName=((String)pENUM.nextElement());
                String keyNameUC = keyName.toUpperCase();

                //System.out.println("What is the keyName ="+keyName+" value="+props.getPropert);


                //Object[] oArray={(Object)props.getProperty(keyName)};
                
                /* @todo This is a dumbass way to do this. Think */
                if (part instanceof VLDomainComponent){
                    // Set property
                    ((VLDomainComponent)part).setProperty(keyNameUC,props.getProperty(keyName));
                    //((VLDomainComponent)part).setProperty(keyName,"SPANK");
                }

            }

        }
        catch (Exception error){
            error.printStackTrace();
            System.out.println("Error in setAttributes method = "+error.toString());
        }
    }

     /**
     * characters() - Parsing logic for XML elements
     */
    public void characters(char[] ch, int start, int end) throws SAXException {
        String s=(new String(ch,start,end)).trim();
    }

}
