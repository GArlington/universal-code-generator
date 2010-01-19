package org.varnerlab.universaleditor.domain;

// Import statements
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;
import java.util.StringTokenizer;

import java.io.Serializable;

/**
 *  Parent of all Domain objects in the ADF. This is the highest class in a composite pattern.
 *  @author J.Varner
 */
public class ADomainComponent extends AComponent implements Serializable {
    // Class/instance attributes
    Properties propMap;
    String xmlName;
    StringBuffer txtBuffer;
    
    /** Constructor - builds new instance of <code>ADomainComponent</code> */
    public ADomainComponent(){
        // Initialize me
        init();
    }
    
    public void setXmlName(String xName){
        try {
            if (xName!=null){
                // Store xmlName
                setProperty("xmlName",xName);
            }
        }
        catch (Exception error){
            error.printStackTrace();
        }
    }
    
    /**
     * Initailizes the DomainObject
     */
    public void init(){
        // Create new string buffer
        // txtBuffer=new StringBuffer();
        
        // Create new properties map
        propMap=new Properties();
        
        // Debug
        System.out.println("Created propMap");
        
    }
    
    /**
     *  Stores a name-value pair where the name is an attribute name and the value is the corresponding value
     *  value of that attribute.
     *  @param String name of an attribute
     *  @param String value of an attribute
     *  @throws Exception
     */
    public void setProperty(String name,String value) {
        // Value is comma delim list (value,canEdit,isPath,isHidden)
        StringTokenizer split=new StringTokenizer(value,",");
        Vector propV=new Vector();
        boolean canEdit=false;
        boolean isPath=false;
        boolean isHidden=false;
        APropertiesContainer propC=null;
        
        // Split up input
        while (split.hasMoreTokens()){
            propV.addElement(split.nextToken());
        }
        
        if (propV.size()==4){
            // Debug
            System.out.println("Inside where I shoule be");
            
            // Setup container
            propC=new APropertiesContainer(propV.elementAt(0).toString());
        
            // Get boolean flags
            canEdit=(new Boolean(propV.elementAt(1).toString())).booleanValue();
            isPath=(new Boolean(propV.elementAt(2).toString())).booleanValue();
            isHidden=(new Boolean(propV.elementAt(3).toString())).booleanValue();
            
            // Debug
            System.out.println("Value="+propC.getValue()+" canEdit="+canEdit+" isPath="+isPath+" isHidden="+isHidden);
        
            // Set flags
            propC.setCanEdit(canEdit);
            propC.setIsPath(isPath);
            propC.setIsHidden(isHidden);
        }
        
        else {
            // Debug
            System.out.println("Why am I here - value="+value);
            
            // Need to see if a propertiesContaimer already exists w/this name
            if (propMap.containsKey(name)){
                // Get propConatiner
                APropertiesContainer oldC=(APropertiesContainer)propMap.get(name);
                
                // Get old properties from container
                canEdit=oldC.getCanEdit();
                isPath=oldC.getIsPath();
                isHidden=oldC.getIsHidden();
            }
            
            // Setup container
            propC=new APropertiesContainer(value);
            propC.setIsPath(isPath);
            propC.setIsHidden(isHidden);
            propC.setCanEdit(canEdit);
        }
        
        // Put propC into propMap
        propMap.put(name,propC);
    }
    
    /**
     *  Stores a name-value pair where the name is an attribute name and the value is the corresponding value
     *  value of that attribute.
     *  @param Object name of an attribute
     *  @param Object value of an attribute
     */
    public void setProperty(Object name, Object value) {
        // Debug
        // System.out.println("Setting propMap property["+name+"]="+value);
        propMap.put(name,value);
    }
    
    public Object getProperty(Object keyName){
        return(getProperty(keyName.toString()));
    }
    
    
    
    public String getProperty(String keyName) {
        APropertiesContainer container=(APropertiesContainer)propMap.get(keyName);
        return(container.getValue());
    }
    
    /**
     *  Logic for writing the code to disk is encapsulated here
     *  @return StringBuffer Text for this domain object
     */
    public String doMessage() throws Exception {
        // Build new buffer
        txtBuffer=new StringBuffer();
        String rString="";
        
        // Construct required xml string
        txtBuffer.append("<");
        txtBuffer.append(getProperty("xmlName"));
        txtBuffer.append(" ");
        
        // Populate attribute name value pairs from attribute map
        Enumeration ENUM=propMap.keys();
        while (ENUM.hasMoreElements()){
            Object name=ENUM.nextElement();
            txtBuffer.append(name.toString());
            txtBuffer.append("=");
            txtBuffer.append("\"");
            txtBuffer.append(((APropertiesContainer)propMap.get(name)).getValue());
            txtBuffer.append("\"");
            txtBuffer.append(" ");
        }
        
        // Final brace
        txtBuffer.append("/>");
        txtBuffer.append("\n");
        
        // Build new string
        rString=txtBuffer.toString();
        
        // Kill buffer
        txtBuffer=null;
        return(rString);
    }
}
