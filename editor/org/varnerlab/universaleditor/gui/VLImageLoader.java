package org.varnerlab.universaleditor.gui;

// Import statements
import java.awt.Image;
import java.io.InputStream;
import java.io.BufferedInputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class VLImageLoader extends Object {
   private static String m_strDIRECTORY
      = "/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/";
   private static VLImageLoader _instance = null;

   public static VLImageLoader instance()
   {
      if (_instance == null)
      {
         _instance = new VLImageLoader();
      }

      return _instance;
   }
   
   public static Image getPNGImage(String strFileName)
   {
      String      strSrc = m_strDIRECTORY + strFileName;
      
      System.out.println("Loading PNG file - "+strSrc);
      
      Image img = Toolkit.getDefaultToolkit().getImage(strSrc);
      //ImageIcon icon = new ImageIcon(img);

      return(img);
   }

   public static Icon getIcon(String strFileBaseName)
   {
      return getImageIcon(strFileBaseName);
   }

   public static Image getImage(String strFileBaseName)
   {
        if (strFileBaseName.endsWith(".png"))
        {
            return getPNGImage(strFileBaseName);
        }
        else
        {
            System.out.println("LOAD: "+strFileBaseName);
            return getImageIcon(strFileBaseName).getImage();
        }
   }


   // Quick hack to load png files from disk -
   public static ImageIcon getPNGImageIcon(String strFileBaseName)
   {
       Image img = getPNGImage(strFileBaseName);
       ImageIcon icon = new ImageIcon(img);
       return(icon);

   }




   public static ImageIcon getImageIcon(String strFileBaseName)
   {
      String      strSrc = m_strDIRECTORY + strFileBaseName;
      InputStream stream = ClassLoader.getSystemResourceAsStream(strSrc);
      ImageIcon   target = null;

      if (stream != null)
      {
         target = loadImage(stream);
      }
      else
      {
         //org.apache.log4j.Category.getInstance(MImageLoader.class).
            //warn("Image not found: " + strSrc);
         return null;
      }

      return target;
   }

   private static final ImageIcon loadImage(InputStream stream)
   {
      byte[]              ayData   = null;
      boolean             bClosing = false;
      BufferedInputStream buffer   = null;
      int                 iPos     = 0;
      int                 iRead    = 0;
      int                 iToRead  = 0;

      try
      {
         ayData = new byte[stream.available()];
         buffer = new BufferedInputStream(stream, ayData.length);
         for (iToRead = ayData.length; iToRead > 0; iPos += iRead, iToRead -= iRead)
         {
            iRead = buffer.read(ayData, iPos, iToRead);
         }
         bClosing = true;
         buffer.close();
      }
      catch (Exception e)
      {
         if (!bClosing)
         {
            try
            {
               buffer.close();
            }
            catch (Exception e2)
            {
               //org.apache.log4j.Category.getInstance(MImageLoader.class).
                  //error(e2, e2);
            }
         }
         throw new RuntimeException("Image access: " + e);
      }

      return new ImageIcon(ayData);
   }
}

