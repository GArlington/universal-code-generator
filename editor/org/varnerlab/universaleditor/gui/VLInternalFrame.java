package org.varnerlab.universaleditor.gui;

// Import statements
import javax.swing.JInternalFrame;
import java.awt.Graphics;
import java.awt.*;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 *  Superclass of all tool windows
 *  @author J.Varner
 */
public abstract class VLInternalFrame extends JInternalFrame {
    // Class/instance attributes
    static int openFrameCount=0;
    static final int xOffset=30;
    static final int yOffset=30;
    
    /** Constructor - no arg builds new AkivaInternalFrame */
    public VLInternalFrame(String _name){
        // Call to super
        super(_name,true,true,true,true);
        
        // iterate window count
        ++openFrameCount;
        
        // Set window size
        setSize(300,300);
        
        // Set the windows location
        setLocation(xOffset*openFrameCount,yOffset*openFrameCount);
        
        // Set TitleBar color when active/inactive
        setDoubleBuffered(true);
         
        // Call initComponents();
        // initComponents();
    }
    
    /**
     * Initialize tool components with default values. This is an abstract method that must be implemented in subclasess.
     */
    public abstract void initComponents();
}
