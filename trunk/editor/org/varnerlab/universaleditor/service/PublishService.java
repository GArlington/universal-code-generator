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
public class PublishService {
    // Class/instance attributes -
    private static Vector<IVLPublishClient> _vecDataClients = new Vector();          // Vector of references that publish the data -


    public static void registerClients(IVLPublishClient client)
    {
        _vecDataClients.addElement(client);
    }


    public static void submitData(Object data) {

        // Get the clients -
        int NUMBER_OF_CLIENTS = _vecDataClients.size();
        for (int index=0;index<NUMBER_OF_CLIENTS;index++)
        {
            IVLPublishClient client = _vecDataClients.elementAt(index);
            client.publishData(data.toString());
        }

    }

}
