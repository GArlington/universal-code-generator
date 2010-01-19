package org.varnerlab.universaleditor.gui.widgets;

// Import statements
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import java.awt.Color;

import org.varnerlab.universaleditor.gui.VLImageLoader;


/**
 *  Specialized class used to paint border of a button when mouse passes over it. 
 *  This class was originally done by S. Johnson as an inner class. I have stolen it
 *  and made it my own.
 *  @author S.Johnson and J.Varner 
 */
public class VLBorderPainter extends MouseAdapter { 
    /*
    private static final Color MAC_FOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR = new Color(0x7daaea);
    private static final Color MAC_UNFOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR = new Color(0xe0e0e0);
    
    private static final Color MAC_UNFOCUSED_SELECTED_CELL_BACKGROUND_COLOR = new Color(0xc0c0c0);
    
    private static final Color MAC_FOCUSED_UNSELECTED_VERTICAL_LINE_COLOR = new Color(0xd9d9d9);
    private static final Color MAC_FOCUSED_SELECTED_VERTICAL_LINE_COLOR = new Color(0x346dbe);
    private static final Color MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR = new Color(0xd9d9d9);
    private static final Color MAC_UNFOCUSED_SELECTED_VERTICAL_LINE_COLOR = new Color(0xacacac);
     */
    
    public void mouseEntered(MouseEvent e) 
    { 
       AbstractButton b = (AbstractButton)e.getSource(); 
       if ( b.isEnabled() ) 
       { 
          
           VLButton button = (VLButton)b;
           //button.setBorderPainted(true);

           try 
           {
                // Ok, so If I'm here I'm over the top of a button. Let's highligth the icon -
                String strTmpIcon = button.getIconNameOn();
                button.setWidgetIconOn(strTmpIcon);
           }
           catch (Exception error)
           {
               // eat the exception for now ...
           }
       }
    } 

    public void mouseExited(MouseEvent e) 
    { 
       AbstractButton b = (AbstractButton)e.getSource(); 

       VLButton button = (VLButton)b;
       //button.setBorderPainted(false);

       try
       {
            // Ok, so If I'm here I'm over the top of a button. Let's highligth the icon -
            String strTmpIcon = button.getIconNameOff();
            button.setWidgetIconOff(strTmpIcon);
        }
        catch (Exception error)
        {
               // eat the exception for now ...
        }
       //b.setBackground(MAC_UNFOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR);
    } 
} 


