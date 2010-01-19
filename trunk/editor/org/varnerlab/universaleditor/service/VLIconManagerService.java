/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.service;

// import statements -
import java.util.Hashtable;
import javax.swing.ImageIcon;

/**
 *
 * @author jeffreyvarner
 */
public class VLIconManagerService {
    // Class/instance attributes -
    private static Hashtable<Object,ImageIcon> _iconTable = new Hashtable();

    public static void registerIcon(Object key,ImageIcon icon)
    {
        _iconTable.put(key, icon);
    }

    public static ImageIcon getIcon(Object key)
    {
        return(_iconTable.get(key));
    }

}
