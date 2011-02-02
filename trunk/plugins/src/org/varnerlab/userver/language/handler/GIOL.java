package org.varnerlab.userver.language.handler;

/*
 * Copyright (c) 2011 Varnerlab, 
 * School of Chemical and Biomolecular Engineering, 
 * Cornell University, Ithaca NY 14853 USA.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Import statements
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

/**
 *  Helper class that contains a set of simple static io routines.
 *  @author J.Varner
 */
public class GIOL extends Object {
    
   /** 
    *  Public static method holding logic for reading a file. 
    *  @returns StringBuffer
    *  @throws Exception
    */
    public static StringBuffer read(String path) throws Exception {
        // method attributes
        StringBuffer buffer=new StringBuffer();
        
        // Create new buffered reader and load file
        File iFile=new File(path);
        BufferedReader reader=new BufferedReader(new FileReader(iFile));
        String s="";
        while ((s=reader.readLine())!=null){
            // Check for comments
            int idxCPP=s.indexOf("//");
            int idxCOpen=s.indexOf("/*");
            int idxCClose=s.indexOf("*/");
            boolean idxStar=s.startsWith("*");
            int idxMatlab=s.indexOf("%");
            int whiteSpace=s.length();
            
            // If any of the comments indexes are not -1 then we have a comment line
            if (idxCPP==-1 && idxCOpen==-1 && idxCClose==-1 && !idxStar && idxMatlab==-1){
                if (whiteSpace!=0){
                    buffer.append(s);
                    //buffer.append("\n");
                }
            }
        }
        
        // Close reader
        reader.close();
        
        // return buffer to caller
        return(buffer);
    }
    
    public static StringBuffer readNewLine(String path) throws Exception {
        // method attributes
        StringBuffer buffer=new StringBuffer();
        
        // Create new buffered reader and load file
        File iFile=new File(path);
        BufferedReader reader=new BufferedReader(new FileReader(iFile));
        String s="";
        while ((s=reader.readLine())!=null){
            // Check for comments
            int idxCPP=s.indexOf("//");
            int idxCOpen=s.indexOf("/*");
            int idxCClose=s.indexOf("*/");
            boolean idxStar=s.startsWith("*");
            int idxMatlab=s.indexOf("%");
            int whiteSpace=s.length();
            
            // If any of the comments indexes are not -1 then we have a comment line
            if (idxCPP==-1 && idxCOpen==-1 && idxCClose==-1 && !idxStar && idxMatlab==-1){
                if (whiteSpace!=0){
                    buffer.append(s);
                    buffer.append("\n");
                }
            }
        }
        
        // Close reader
        reader.close();
        
        // return buffer to caller
        return(buffer);
    }
    
    
    /**
     *  Public static merthod that write StringBuffer to disk. Take two ars, the path and the buffer.
     *  @param String Path
     *  @param StringBuffer My Payload (JT Rules!)
     *  @throws Exception
     */
    public static void write(String path,StringBuffer buffer) throws Exception {
        // Create writer
        File oFile=new File(path);
        BufferedWriter writer=new BufferedWriter(new FileWriter(oFile));
        
        // Write buffer to file system and close writer
        writer.write(buffer.toString());
        writer.close();
    }
    
    public static void write(String path,String buffer) throws Exception {
        // Create writer
        File oFile=new File(path);
        BufferedWriter writer=new BufferedWriter(new FileWriter(oFile));
        
        // Write buffer to file system and close writer
        writer.write(buffer);
        writer.close();
    }
}