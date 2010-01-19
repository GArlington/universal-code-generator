/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.service;

// import statements -
import java.util.*;

/**
 *
 * @author jeffreyvarner
 */
public class SystemwideEventService {

    // Class/instance attributes -
    private static Vector<IVLSystemwideEventListener> _vecUsernameListener = new Vector();
    private static Vector<IVLSystemwideEventListener> _vecNetworkListener = new Vector();
    private static Vector<IVLSystemwideEventListener> _vecSessionListener = new Vector();


    // Register Username listners -
    public static void registerUsernameListener(IVLSystemwideEventListener obj)
    {
        // Register -
        _vecUsernameListener.addElement(obj);

        // Fire the update -
        SystemwideEventService.fireUsernameUpdateEvent();
    }

    // Register Username listners -
    public static void registerSessionListener(IVLSystemwideEventListener obj)
    {
        // Register -
        _vecSessionListener.addElement(obj);

        // Fire the update -
        SystemwideEventService.fireUsernameUpdateEvent();
    }

    // Register Username listners -
    public static void registerNetworkListener(IVLSystemwideEventListener obj)
    {
        _vecNetworkListener.addElement(obj);
    }



    // Fire a Username event update -
    public static void fireUsernameUpdateEvent()
    {
        int NUMBER_OF_ITEMS = _vecUsernameListener.size();
        for (int index=0;index<NUMBER_OF_ITEMS;index++)
        {
            IVLSystemwideEventListener item = (IVLSystemwideEventListener)_vecUsernameListener.get(index);
            item.updateComponent();
        }
    }

    // Fire a Username event update -
    public static void fireSessionUpdateEvent()
    {
        int NUMBER_OF_ITEMS = _vecSessionListener.size();
        for (int index=0;index<NUMBER_OF_ITEMS;index++)
        {
            IVLSystemwideEventListener item = (IVLSystemwideEventListener)_vecSessionListener.get(index);
            item.updateSession();
        }
    }

    // Fire a Username event update -
    public static void fireNetworkUpdateEvent()
    {
        int NUMBER_OF_ITEMS = _vecNetworkListener.size();
        for (int index=0;index<NUMBER_OF_ITEMS;index++)
        {
            IVLSystemwideEventListener item = (IVLSystemwideEventListener)_vecNetworkListener.get(index);
            item.updateNetwork();
        }
    }

}
