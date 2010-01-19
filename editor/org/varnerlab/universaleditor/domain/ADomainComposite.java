package org.varnerlab.universaleditor.domain;

// Import statements
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;
import org.varnerlab.universaleditor.gui.widgets.*;

/**
 *  Parent of all Domain objects in the ADF. This is the highest class in a composite pattern.
 *  @author J.Varner
 */
public class ADomainComposite extends ADomainComponent implements IVLWidgetComposite {
    // Class/instance attributes
    Vector children;
    StringBuffer txtBuffer;
    
    /** Constructor - builds new instance of <code>ADomainComponent</code> */
    public ADomainComposite(){
        // Call to super
        super();
        
        // Initialize me
        init();
    }
    
    
    /**
     * Initailizes the DomainObject
     */
    public void init(){
        // Create new child container
        children=new Vector();
        
        // Setup prop container
        propMap=new Properties();
        
    }
    
    /**
     *  Adds children to this composite
     *  @param ADomainComponent Child
     */
    public void addChild(ADomainComponent child){
        children.addElement(child);
    }
    
     /**
     *  Logic for writing the code to disk is encapsulated here
     *  @return StringBuffer Text for this domain object
     */
    public String doMessage() throws Exception {
        // New text buffer
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
        
        txtBuffer.append(">");
        txtBuffer.append("\n");
        
        // Return string
        rString=txtBuffer.toString();
        
        // Kill buffer
        txtBuffer=null;
        return(rString);
    }
}
