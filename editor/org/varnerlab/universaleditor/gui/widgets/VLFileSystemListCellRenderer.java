/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.gui.VLImageLoader;
import org.varnerlab.universaleditor.service.VLIconManagerService;

/**
 *
 * @author jeffreyvarner
 */
public class VLFileSystemListCellRenderer extends DefaultListCellRenderer {
    // Class/instance attributes



    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {

        // Get the selected index -
        File sFile = (File)value;

        // Get the name of the selcted file -
        String strName = sFile.getName();

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Set the proper icon -
        if (sFile.isDirectory())
        {
            // Check to see if we have the root -or- just another dir -
            String parent = sFile.getParent();
            if (parent!=null)
            {
                
                // When I get here I have a dir - let's see if this is the user home -
                String strUserHome = (Launcher._HOME);
                String strCurrentDir = sFile.getPath();
                
                if (strCurrentDir.equalsIgnoreCase(strUserHome))
                {
                    setIcon(VLIconManagerService.getIcon("HOME-8-ICON"));
                }
                else
                {
                
                    setIcon(VLIconManagerService.getIcon("FOLDER-8-ICON"));
                }

                // Set the text -
                setText(strName);
            }
            else
            {


                setIcon(VLIconManagerService.getIcon("DISK-8-ICON"));
                setText(strName);
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
            else if (strFExt.equalsIgnoreCase("m"))
            {
                setIcon(VLIconManagerService.getIcon("MATLAB-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("pdf"))
            {
                setIcon(VLIconManagerService.getIcon("PDFFILE-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("png"))
            {
                setIcon(VLIconManagerService.getIcon("PNGFILE-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("ppt"))
            {
                setIcon(VLIconManagerService.getIcon("PPTFILE-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("txt") || strFExt.equalsIgnoreCase("dat"))
            {
                setIcon(VLIconManagerService.getIcon("TXTFILE-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("sbml"))
            {
                setIcon(VLIconManagerService.getIcon("SBMLFILE-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("key"))
            {
                setIcon(VLIconManagerService.getIcon("KEYNOTEFILE-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("pages"))
            {
                setIcon(VLIconManagerService.getIcon("PAGESFILE-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("xls") || strFExt.equalsIgnoreCase("xlsx") )
            {
                setIcon(VLIconManagerService.getIcon("XLSFILE-8-ICON"));
            }
            else if (strFExt.equalsIgnoreCase("doc") || strFExt.equalsIgnoreCase("docx") || strFExt.equalsIgnoreCase("rtf"))
            {
                setIcon(VLIconManagerService.getIcon("DOCFILE-8-ICON"));
            }
            else
            {
                setIcon(VLIconManagerService.getIcon("FILE-8-ICON"));
            }

            setText(strName);

        }

        return(this);
    }

}
