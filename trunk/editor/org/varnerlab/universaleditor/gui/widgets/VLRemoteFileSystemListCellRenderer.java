/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.Hashtable;
import java.util.Properties;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.VLImageLoader;
import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *
 * @author jeffreyvarner
 */
public class VLRemoteFileSystemListCellRenderer extends DefaultListCellRenderer {
    // Class/instance attributes
    private Hashtable _propTable = new Hashtable();
    private UEditorSession _session = null;

    public void setDirectoryFlag(String file,String dirFlag)
    {
        _propTable.put(file, dirFlag);
    }

    public String getDirectoryFlag(String fileName)
    {
        return((String)_propTable.get(fileName));
    }
    
    // set the session reference -
    public void setSession(UEditorSession session)
    {
    	_session = session;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {

        // Get the selected index -
        File sFile = null;

        // Super fucking hack...
        if (value instanceof String)
        {
            sFile = new File((String)value);
            PublishService.submitData("WTF??? "+value);
        }
        else
        {
            sFile = (File)value;
        }


        // Get the name of the selected file -
        
        if (sFile!=null)
        {

            String strName = sFile.getName();
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);


            // Set the proper icon -
            if (isSelected)
            {
                setBackground(Color.GRAY);
                setForeground(Color.WHITE);
            }


            // Get the dir flag -
            String dirFlag = (String)_propTable.get(strName);

            PublishService.submitData("Dir flag - "+dirFlag+" FILENAME -"+strName);

            if (dirFlag.equalsIgnoreCase("DIRECTORY"))
            {
                // Check to see if we have the root -or- just another dir -
                setIcon(VLIconManagerService.getIcon("FOLDER-8-ICON"));
                
                // ok, we need to check to see if the name has a ssid_ in it - if so, use the fake name -
                int index_ssid = strName.indexOf("ssid_");
                if (index_ssid==-1)
                {
                	setText(strName);
                }
                else
                {
                	// ok, so I have a ssid in the name -
                	Hashtable tmpTable = (Hashtable)_session.getProperty("PROJECT_TRANSLATION_TABLE");
                	
                	// lookup the project id -
                	String strHumanName = (String)tmpTable.get(strName);
                	setText(strHumanName);
                }
            }
            else
            {

                // Check to see if this is an xml file -
                int index2Dot = strName.lastIndexOf(".");
                int intLength = strName.length();
                String strFExt = strName.substring(index2Dot+1, intLength);

                if (strFExt.equalsIgnoreCase("xml"))
                {
                    setIcon(VLIconManagerService.getIcon("XMLFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("prop"))
                {
                    setIcon(VLIconManagerService.getIcon("PROPFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("cc"))
                {
                    setIcon(VLIconManagerService.getIcon("CPPFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("pdf"))
                {
                    setIcon(VLIconManagerService.getIcon("PDFFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("png"))
                {
                    setIcon(VLIconManagerService.getIcon("PNGFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("doc") || strFExt.equalsIgnoreCase("docx") || strFExt.equalsIgnoreCase("rtf"))
                {
                    setIcon(VLIconManagerService.getIcon("DOCFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("xls") || strFExt.equalsIgnoreCase("xlsx"))
                {
                    setIcon(VLIconManagerService.getIcon("XLSFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("ppt"))
                {
                    setIcon(VLIconManagerService.getIcon("PPTFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("m"))
                {
                    setIcon(VLIconManagerService.getIcon("MATLAB-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("txt") || strFExt.equalsIgnoreCase("dat"))
                {
                    setIcon(VLIconManagerService.getIcon("TXTFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("sbml"))
                {
                    setIcon(VLIconManagerService.getIcon("SBMLFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("pages"))
                {
                    setIcon(VLIconManagerService.getIcon("PAGESFILE-8-ICON"));
                }
                else if (strFExt.equalsIgnoreCase("key"))
                {
                    setIcon(VLIconManagerService.getIcon("KEYNOTEFILE-8-ICON"));
                }
                else
                {
                    setIcon(VLIconManagerService.getIcon("FILE-8-ICON"));
                }

                setText(strName);

            }
        }

        return(this);
    }

}
