package org.varnerlab.universaleditor.gui.widgets;

// Import statements
import javax.swing.JToolBar;
import javax.swing.JComponent;
import java.awt.Dimension;
import org.varnerlab.universaleditor.gui.VLImageLoader;

import java.awt.Graphics;

/**
 *  Akiva wrapper for a JToolBar functionality
 *  @author J.Varner
 */
public class VLToolBar extends JToolBar implements IVLWidget,IVLWidgetComposite {
    
    /** Constructor - no ar builds new AToolBar */
    public VLToolBar(){
        super();
        
        // Initialize me
        init();
    }
    
    public void init(){
        try {
        }
        catch (Exception error){
            error.printStackTrace();
        }
    }
    
    public void setWidgetIcon(String _iName) throws Exception {
    }    
    
    /**
     * Adds a child reference to a parent
     */
    public void addChild(IVLWidget _widget) throws Exception {
        super.add((JComponent)_widget);
    }
    
    public void setWidgetText(String _text) throws Exception {
    }
    
    /**
     * Sets the parent reference of a child
     */
    public void setParent(IVLWidget _widget) throws Exception {
    }
    
    public IVLWidget endSetup() {
        return(this);
    }
    
    /**
     * Adds a child reference to a parent
     */
    public void addChild(Object _widget) throws Exception {
    }

    public void setWidgetIconOn(String _iName) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWidgetIconOff(String _iName) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
