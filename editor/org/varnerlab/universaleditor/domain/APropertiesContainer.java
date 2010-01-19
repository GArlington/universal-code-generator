package org.varnerlab.universaleditor.domain;

// Import statements
import java.io.Serializable;
import javax.swing.JComboBox;
import org.varnerlab.universaleditor.gui.widgets.*;


/**
 *  Encapsulation of a domain object property.
 *  @author J.Varner
 */
public class APropertiesContainer extends Object implements Serializable {
    // Class/instance variables
    private String pName;
    private String pValue;
    private boolean isPath;
    private boolean isHidden;
    private boolean canEdit;
    private IVLWidget _widget;
    
   
 
    /**
     * Constructor - builds new APropertiesContainer
     */
    public APropertiesContainer(String pValue){
        // Grab stuff passed in
        this.pValue=pValue;
        
        // Debug
        //System.out.println("APropertiesContainer has value - "+pValue);
    }
    
    /**
     * No arg constructor
     */
    public APropertiesContainer(){
    }
    
    // Setup components
    public void configureWidget(String cName,Object[] items) throws Exception {
        // Create new instance of class
        _widget=(IVLWidget)Class.forName(cName).newInstance();
        
        // Add items array to class
        int len=items.length;
        for (int q=0;q<len;q++){
            _widget.addChild(items[q]);
        }
    }
    
    public IVLWidget getWidget(){
        return(_widget);
    }
    
    public void setCanEdit(boolean falg){
        // Debug
        //System.out.println("APropertiesContainer has value - "+pValue+" canEdit="+falg);
        canEdit=falg;
    }
    
    public boolean getCanEdit(){
        return(canEdit);
    }
    
    public void setIsPath(boolean flag){
        //System.out.println("APropertiesContainer has value - "+pValue+" isPath="+flag);
        isPath=flag;
    }
    
    public void setIsHidden(boolean flag){
        //System.out.println("APropertiesContainer has value - "+pValue+" isHidden="+flag);
        isHidden=flag;
    }
    
    public boolean getIsHidden(){
        return(isHidden);
    }
    
    public boolean getIsPath(){
        return(isPath);
    }
    
    
    public String getValue(){
        return(pValue);
    }
    
    
    public void setValue(String value){
        pValue=value;
    }
}