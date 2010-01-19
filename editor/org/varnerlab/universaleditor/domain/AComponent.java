package org.varnerlab.universaleditor.domain;

// Import statements
import org.varnerlab.universaleditor.gui.widgets.*;


/**
 * Superclass of everything that implements <code>IAWidget</code>
 */
public class AComponent implements IVLWidget {
    
    public void setWidgetIcon(String _iName) throws Exception {
    }
    
    /**
     * Adds a child reference to a parent
     */
    public void addChild(IVLWidget _widget) throws Exception {
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