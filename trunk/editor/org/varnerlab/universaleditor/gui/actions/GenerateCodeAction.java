/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.varnerlab.universaleditor.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.SocketService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author jeffreyvarner
 */
public class GenerateCodeAction implements ActionListener {

    // Ok, so let's send a message to generate code to the server -
    public void actionPerformed(ActionEvent e) {
        // Method attributes -
        UEditorSession session = null;
        StringBuffer buffer = new StringBuffer();

        // Get the session object -
        session = (Launcher.getInstance()).getSession();


        // When I get here I have the buffer all ready to go -
        String strIPAddress = (String) session.getProperty("SERVER_ADDRESS");
        String strPort = (String) session.getProperty("CODEGEN_SERVER_PORT");

        // If I get here then I have a model.prop ready to rock -
        String strPropFileName = (String)session.getProperty("CURRENT_MODEL_PROP_FILENAME");
        if (strPropFileName!=null)
        {
        
            // Formulate the message -
            buffer.append("<universal>\n");
            buffer.append("\t <property modelpropfilename=\"");
            buffer.append(strPropFileName);
            buffer.append("\"/>\n");
            buffer.append("</universal>\n");

            // Send the message -
            try
            {
                // Send that mofo -
                PublishService.submitData("Sending "+buffer.toString());
                String strReturnString = SocketService.sendMessage(buffer.toString(), strIPAddress, strPort, session);

                // Ok, when I get here I hace I have the directory -
                // If I get here that I should have a list of the files on the server -
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                // Ok, so we have a document -
                session = (Launcher.getInstance()).getSession();
                session.setProperty("REMOTE_FILESYSTEM_TREE", builder.parse(new InputSource(new StringReader(strReturnString))));

                // Update the session -
                SystemwideEventService.fireSessionUpdateEvent();

            }
            catch (Exception error)
            {
                PublishService.submitData("Spank me - some type of error "+error);
            }
        }

    }
}
