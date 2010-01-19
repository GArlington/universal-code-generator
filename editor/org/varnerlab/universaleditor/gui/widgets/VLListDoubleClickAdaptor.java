/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Hashtable;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.service.SystemwideEventService;

/**
 *
 * @author jeffreyvarner
 */
public class VLListDoubleClickAdaptor extends MouseAdapter {

    private Hashtable _propTable = new Hashtable();
    private UEditorSession _session = null;

    public void setProperty(String file,Object obj)
    {
        _propTable.put(file, obj);
    }

    public Object getProperty(String fileName)
    {
        return(_propTable.get(fileName));
    }

    
    public VLListDoubleClickAdaptor()
    {
        
    }
    
    @Override
    public void mouseClicked(MouseEvent e)
    {

        JList list = (JList)_propTable.get("JLIST");

        if(e.getClickCount() == 2)
        {
            // Get the list -
    
            int index = list.locationToIndex(e.getPoint());
            DefaultListModel listModel = (DefaultListModel)_propTable.get("LIST_MODEL");
            
            File item = (File)listModel.getElementAt(index);
            //list.ensureIndexIsVisible(index);

            // Get the session -
            _session = (UEditorSession)_propTable.get("SESSION");

            // Get the selected index -
            _session.setProperty("SELECTED_PROJECT_FILE", item);

            // Fire a session update event -
            SystemwideEventService.fireSessionUpdateEvent();

            // ok, so I need to figure out what file was clicked and then load the correct editor -

            System.out.println("Double clicked on " + item);
        }
        else if (e.getClickCount()==1)
        {
            // do nothing -
            
        }
   }


}
