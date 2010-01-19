package org.varnerlab.universaleditor.gui.widgets;

// Import statements
import java.awt.event.ActionListener;
import org.varnerlab.universaleditor.gui.VLImageLoader;
import javax.swing.JButton;
import javax.swing.ImageIcon;

/**
 *  Akiva warpper for a JButton that gets added to the JToolBar
 *  @author J.Varner
 */
public class VLButton extends JButton implements IVLWidget {
    // Button attributes
    IVLWidget child=null;
    private String _strCodeName = "";
    private String _strIconNameOn = "";
    private String _strIconNameOff = "";

    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconOff = null;


    /** Constructor  - no arg constructs new AButton */
    public VLButton(){
        super.setBorderPainted(false);
        addMouseListener(new VLBorderPainter());

        
        // This is a super hack to get the 32 x 32 icons to appear correctly
        super.setSize(120,120);
    }


    public String getCodeName()
    {
        return(_strCodeName);
    }

    public void setCodename(String name)
    {
        _strCodeName = name;
    }

    public void setWidgetIconOff(String _iName) throws Exception {
        // Grab the icon name -
        _strIconNameOff = _iName;

        // Load the icon -
        if (_imgIconOff==null)
        {
            _imgIconOff = VLImageLoader.getPNGImageIcon(_iName);
        }

         super.setIcon(_imgIconOff);
    }

    public void setWidgetIconOn(String _iName) throws Exception {
        // Grab the icon name -
        _strIconNameOn = _iName;
         
        // Load the icon -
        if (_imgIconOn==null)
        {
            _imgIconOn = VLImageLoader.getPNGImageIcon(_iName);
        }
        
        super.setIcon(_imgIconOn);
    }

    
    public String getIconNameOff()
    {
        return(_strIconNameOff);
    }

    public String getIconNameOn()
    {
        return(_strIconNameOn);
    }


    public void setActionListener(String _name) throws Exception {
        
        StringBuffer _buffer=new StringBuffer();
        _buffer.append("org.varnerlab.universaleditor.gui.actions.");
        _buffer.append(_name);
        
        // Build new action as specified by name
        super.addActionListener((ActionListener)Class.forName(_buffer.toString()).newInstance());
        
    }
    
    /**
     * Adds a child reference to a parent
     */
    public void addChild(IVLWidget _widget) throws Exception {
        this.child=_widget;
    }
    
    public IVLWidget getChild(){
        return(child);
    }
    
    public void setWidgetText(String _text) throws Exception {
        super.setToolTipText(_text);
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

    public void setWidgetIcon(String _iName) throws Exception {
        // Call the off (default)
        this.setWidgetIconOff(_iName);
    }
    
}
