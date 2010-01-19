/*
* AkivaObject.java 1.0 Wed Jun 13 21:03:09 EST 2001
*
* Copyright 2001 by Akiva Software LLc,
* 8512 Cabana Dr. Suite 208, Fishers IN 46038, U.S.A 
* All rights reserved.
* 
* This software is the confidential and proprietary infomation of Akiva Software LLc 
* ("Confidential Information"). You shall not disclose such Confidential Information 
* and shall use it only in accordance with the terms of the license agreement that you 
* entered into with Akiva. 
*/
package universaleditor;

// Import statements
import java.io.*;

/**
 * AkivaObject - Superclass of all adf objects
 */
public class VLObject extends Object implements Serializable {
    
    /**
     * debug() - Prints message to sysout
     */
    protected void debug(String msg){
        //System.out.println(msg);
    }
    
    /**
     * Constructor - 
     */
    public VLObject(){
        // FILL ME IN
    }
    
    /**
     * Constructor - takes boolean flag
     */
    public VLObject(boolean pFlag){
    }
}