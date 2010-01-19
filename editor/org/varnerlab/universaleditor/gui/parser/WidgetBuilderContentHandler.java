package org.varnerlab.universaleditor.gui.parser;

// Import statements
import org.varnerlab.universaleditor.gui.widgets.*;
import org.varnerlab.universaleditor.domain.*;

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
 * Content handler used bu the builder to construct UI components (widgets).
 */
public class WidgetBuilderContentHandler extends ADFBuilderHandler {
     
    /** Construct new <code>ADFStartupBuilderHandler</code> */
    public WidgetBuilderContentHandler(ADFParser parser){
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
    public void startElement(String URI, String rawName, String localName, Attributes attributes) throws SAXException {   
        // Method attributes
        Properties m_propAttrs=new Properties();         // Properties object that holds Attribute names and values
        try {
            
            if (!localName.equalsIgnoreCase("widget")){
                // Get number of attributes
                int iAttrs = (attributes != null) ? attributes.getLength() : 0;
                
                // Grab attributes
                m_propAttrs.clear();
                for (int q = 0; q < iAttrs; q++){
                    System.out.println(attributes.getQName(q)+"="+attributes.getValue(q));
                    m_propAttrs.put(attributes.getQName(q),attributes.getValue(q));
                }
                
                m_package=new StringBuffer();
                
                // Grab package name from properties
                if (m_propAttrs.containsKey("package")){
                    
                    // Note - because of package,canEdit,isPath,isHidden
                    String packName=m_propAttrs.getProperty("package");
                    StringTokenizer split=new StringTokenizer(packName,",");
                    
                    // Get number of tokens
                    int num=split.countTokens();
                    for (int q=0;q<num;q++){
                        if (q==0){
                            m_package.append(split.nextToken().toString());
                            break;
                        }
                    }
                    
                    // Append the dot
                    m_package.append(".");
                }
                else {
                    m_package.append("org.varnerlab.universaleditor.gui.widgets.");
                }
                
                // Create AkivaObject
                //System.out.println("Setting localname - "+localName);
                IVLWidget _widget=createComponent(localName);

                // Return parent
                IVLWidget _parent=checkParent(_widget);
                
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
        }
        catch (Exception error){
            System.out.println(error.toString());
        }
    }
    
    /**
    * Handles an XML element's closing tag.  Pops the top <code>Part</code>
    * from the stack.
    *
    * @param  strElementName the element name
    * @throws <code>java.lang.Exception</code> if an error occurs
    */
   public void endElement(String strElementName,String str3,String str2) throws SAXException {
       
        try {
           if (!str2.equalsIgnoreCase("widget")){
               IVLWidget comp=((IVLWidget)m_stack.pop()).endSetup();
               if (comp instanceof IVLWidgetComposite){
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
     * checkRateRoot() - Checks the root element of a rate tree
     */
    private IVLWidget checkParent(IVLWidget obj){
        // Method attributes
        IVLWidget parent=null;
        
        try {
            if (m_akoRoot == null){
                m_akoRoot = obj;
            }
            if (!m_stack.empty()){
                parent = (IVLWidget)m_stack.peek();
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
    private IVLWidget createComponent(String name){
        // Method attributes
        IVLWidget comp=null;
        try {
            // Build kinetic component
            String className=m_package.append(name).toString();
            
            //System.out.println("what - "+className);
            
            comp=(IVLWidget)(Class.forName(className).newInstance());
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
        
        try {
            Class pClass=part.getClass();
            
            // Iterate through properties for this component
            Enumeration pENUM=props.keys();
            while (pENUM.hasMoreElements()){
                // Get keyName
                String keyName=(String)pENUM.nextElement();
                
                //System.out.println("What is the keyName ="+keyName);
                
                Object[] oArray={(Object)props.getProperty(keyName)};
                
                String name=this.makeSetterName(keyName);
            
                /* @todo This is a dumbass way to do this. Think */
                if (part instanceof ADomainComponent){
                    // Set property
                    ((ADomainComponent)part).setProperty(keyName,props.getProperty(keyName));
                    
                    
                }
                else {
                    // Call setter method
     
                    ((Method)pClass.getMethod(name,cArray)).invoke(part,oArray);
                }
                
            }
            
        }
        catch (Exception error){
            System.out.println("Error in setAttributes method = "+error.toString());
        }
    }
}

