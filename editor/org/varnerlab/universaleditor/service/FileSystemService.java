/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.service;

import java.io.File;
import java.util.Vector;
import org.varnerlab.universaleditor.gui.Launcher;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jeffreyvarner
 */
public class FileSystemService {



    public static void getFileFromDir(File dir, Vector<File> results)
    {
        // get all the files and directories
        File [] children = dir.listFiles();
        String strSep = Launcher._SLASH;

        // loop over the file/directory listing
        for(File file : children)
        {

            // Get the last index -
        	// Ok, so we need to see if this file *starts* w/a .
        	String strFileName = file.getName();
        	int INT_DOT = strFileName.indexOf(".");
        	if (INT_DOT!=0)
        	{
        		// add the match to the results list
                results.add(file);
        	}
        }
    }


    public static void traverseUp(File dir,Vector<File> results)
    {
        // Get the parent
        File parent = dir.getParentFile();

        // If the parent is not null -
        if (parent!=null)
        {
            // Store the current parent -
            results.add(parent);

            // Check for a new parent -
            traverseUp(parent,results);
        }
    }

    public static void traverseUpDOMTree(Node node,Vector<File> results)
    {
        // Ok, when I get here I have to walk up the tree --

        Node parent = node.getParentNode();
        NodeList pTmpList = parent.getChildNodes();
        String strNodeName = parent.getNodeName();

        // If the parent is not null -
        if (!strNodeName.equalsIgnoreCase("jdv27"))
        {
            // Get the pathname -
            NamedNodeMap map = parent.getAttributes();
            System.out.println("Map: "+map+" length "+map.getLength());

            if (map!=null && map.getLength()>0)
            {
                Node tmpNode = map.getNamedItem("name");
                System.out.println("Node: "+tmpNode);
                if (tmpNode!=null)
                {

                    String strTmp = tmpNode.getNodeValue();

                    if (strTmp!=null)
                    {
                        // Create file -
                        File file = new File(strTmp);

                        System.out.println("Hey now - "+strTmp);

                        // Store the file -
                        results.addElement(file);

                        // Check for a new parent -
                        traverseUpDOMTree(parent,results);
                    }
                }
            }
        }
        else
        {
            // If I get here do nothing ...
        }
    }

}

