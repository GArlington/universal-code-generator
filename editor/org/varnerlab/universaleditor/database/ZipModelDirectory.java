/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.database;

// Import statements -
import java.io.*;
import java.util.zip.*;

/**
 *
 * @author jeffreyvarner
 */
public class ZipModelDirectory {
    // Class/instance attributes -

    


    public void zipDirectory(String dir2zip,ZipOutputStream zos) throws Exception
    {
    
        try {
            
            //create a new File object based on the directory we need to zip -
            File zipDir = new File(dir2zip); 
        
            System.out.println("What dir am I zipping - "+dir2zip);
           
            //get a listing of the directory content 
            String[] dirList = zipDir.list(); 
            byte[] readBuffer = new byte[2156];  
        
            
            
            //loop through dirList, and zip the files 
            for(int i=0; i<dirList.length; i++) 
            { 
                File f = new File(dirList[i]); 
                
                System.out.println("What files are we trying to load -"+f.getName());
                
                //if we reached here, the File object f was not a directory 
                //create a FileInputStream on top of f 
                String tmpName = dir2zip+f.getName();
                FileInputStream fis = new FileInputStream(tmpName); 
                ZipEntry anEntry = new ZipEntry(f.getName()); 
            
                //place the zip entry in the ZipOutputStream object 
                zos.putNextEntry(anEntry); 

                //now write the content of the file to the ZipOutputStream 
                 int len;
                 while ((len = fis.read(readBuffer)) > 0) {
                    zos.write(readBuffer, 0, len);
                 }
 
                // Complete the entry
                fis.close();
                zos.closeEntry();
            }
        }
        catch (Exception error)
        {
            System.out.println("ZipFile ERROR - "+error);
            error.printStackTrace();;
        }
        
    }

}
