package org.varnerlab.universaleditor.gui.widgets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

// special import from sun that allows translucency -
import com.sun.awt.AWTUtilities;

public class SheetDialogFrame extends JWindow {

    JComponent sheet;
    JPanel glass;

    public SheetDialogFrame (String name) {
        glass = (JPanel) getGlassPane();
    }

    public JComponent showJDialogAsSheet (JDialog dialog) {
        sheet = (JComponent) dialog.getContentPane();
        sheet.setBackground (Color.red);
        glass.setLayout (new GridBagLayout());
       
        //sheet.setBorder (new LineBorder(Color.black, 1));
        AWTUtilities.setWindowOpacity(dialog, (float)0.5);
        sheet.setOpaque(false);
        glass.setOpaque(false);
        sheet.setBackground(new Color(100,100,100,50));
        glass.setBackground(new Color(100,100,100,50));
        dialog.setBackground(Color.DARK_GRAY);
        setBackground(new Color(100,100,100));
        
        glass.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        glass.add (sheet, gbc);
        gbc.gridy=1;
        gbc.weighty = Integer.MAX_VALUE;
        glass.add (Box.createGlue(), gbc);
        glass.setVisible(true);
        return sheet;
    }

    public void hideSheet() {
        glass.setVisible(false);
    }
}
