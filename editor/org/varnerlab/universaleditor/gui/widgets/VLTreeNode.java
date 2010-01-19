/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

// import
import java.util.*;

/**
 *
 * @author jeffreyvarner
 */
public class VLTreeNode {
    // Class/instance attributes -
    private Hashtable _propTable = null;

    public VLTreeNode()
    {
        // Create the prop hashtable -
        _propTable = new Hashtable();
    }

    public void setProperty(Object key,Object value)
    {
        _propTable.put(key, value);
    }


    public Object getProperty(Object key)
    {
        return(_propTable.get(key));
    }

    public Enumeration getKeys()
    {
        return(_propTable.keys());
    }


    @Override
    public String toString()
    {
        return((String)_propTable.get("DISPLAY_LABEL"));
    }

    public void writeTree(StringBuffer buffer,int intKids) throws Exception
    {
        // Ok, so when I get here I need to write my props out and then call writeTree on my kids -

        //System.out.println("Im here, I'm queer...hey now...tmi");
        String strClassName = "";
        String strPrefix = (String)this.getProperty("VLPREFIX");
        if (strPrefix==null)
        {
           strClassName = this.getProperty("DISPLAY_LABEL").toString();
        }
        else
        {
           strClassName = strPrefix+this.getProperty("DISPLAY_LABEL").toString();
        }

        // Get my props -
        String strLeadingSmall = (String)this.getProperty("CLASSNAME_IS_LOWERCASE");
        if (strLeadingSmall!=null)
        {
            if (strLeadingSmall.equalsIgnoreCase("TRUE"))
            {
                
                if (strClassName.equalsIgnoreCase("CI"))
                {
                    // If I get here then I have CI -
                    strClassName = strClassName.toLowerCase();
                }
                else
                {
                    // Change the first letter to lowercase -
                    StringBuffer tmpBuffer = new StringBuffer(strClassName);
                    tmpBuffer.setCharAt(0, Character.toLowerCase(tmpBuffer.charAt(0)));
                    strClassName = tmpBuffer.toString();
                }
            }
        }
        
        

        // First, my DISPLAY_LABEL is my object type -
        buffer.append("<");
        buffer.append(strClassName);
        buffer.append(" ");

        // Ok, get my keys and lets rock -
        Enumeration keys = this.getKeys();
        while (keys.hasMoreElements())
        {
            // Get the key-value pair -
            Object key = keys.nextElement();
            Object value = this.getProperty(key);


            // Ok, let's check to make sure we don't have the LABEL or ICON info in the file -
            String tmp = key.toString();

            // Get the location of VL properties -
            int intIcon = tmp.indexOf("ICON");
            int intLabel = tmp.indexOf("LABEL");
            int intClassName = tmp.indexOf("CLASSNAME");

            if (intIcon==-1 && intLabel==-1 && intClassName==-1)
            {
                String strTmpVal = value.toString();
                String tmpLowerCase = tmp.toLowerCase();

                buffer.append(tmpLowerCase);
                buffer.append("=\"");
                buffer.append(strTmpVal);
                buffer.append("\" ");
            }
        }

        if (intKids==0)
        {
            buffer.append("/>\n");
        }
        else
        {
            buffer.append(">\n");
        }

        //System.out.println("What is the Buffer now Keneth - "+buffer.toString());

        /*
        // Ok, when I get here I have all the kids - close this bitch -
        buffer.append("</");
        buffer.append(strClassName);
        buffer.append(">\n");
         */
    }

}
