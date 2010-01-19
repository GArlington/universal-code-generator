package org.varnerlab.universaleditor.gui.widgets;


/**
 * Tagging interface for Akiva UI widgets
 */
public interface IVLWidget {

    
    public void setWidgetText(String _text) throws Exception;
    public void setWidgetIcon(String _iName) throws Exception;

    // To do fancy mouse overs w/the icons -
    public void setWidgetIconOn(String _iName) throws Exception;
    public void setWidgetIconOff(String _iName) throws Exception;


    public IVLWidget endSetup();
     
    /**
     * Sets the parent reference of a child
     */
    public void setParent(IVLWidget _widget) throws Exception;
    
    /**
     * Adds a child reference to a parent
     */
    public void addChild(IVLWidget _widget) throws Exception;
    /**
     * Adds a child reference to a parent
     */
    public void addChild(Object _widget) throws Exception;
    
}

