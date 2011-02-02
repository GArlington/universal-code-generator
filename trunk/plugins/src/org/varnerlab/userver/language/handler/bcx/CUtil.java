package org.varnerlab.userver.language.handler.bcx;

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

// import -
import java.util.*;
import java.io.*;

import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.language.handler.GIOL;

public class CUtil {

	/**
	 * Reallocates an array with a new size of +1, and copies the contents
	 * of the old array to the new array and puts new element at bottom.
	 * @param oldArray  the old array, to be reallocated.
	 * @param newElem   the new element for the array
	 * @return          A new array with the same contents.
	 */
	public static int[] addElement (int[] oldArray, int newElem) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		int newSize = oldSize+1;
		int[] newArray = new int[newSize];
		System.arraycopy (oldArray,0,newArray,0,oldSize);
		newArray[oldSize] = newElem; // remember array index starts at zero
		return newArray; }

	//need generic way to repeat this for diffrent objects 
	public static double[] addElement (double[] oldArray, double newElem) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		int newSize = oldSize+1;
		double[] newArray = new double[newSize];
		System.arraycopy (oldArray,0,newArray,0,oldSize);
		newArray[oldSize] = newElem; // remember array index starts at zero
		return newArray; }

	public static Data[] addElement (Data[] oldArray, Data newElem) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		int newSize = oldSize+1;
		Data[] newArray = new Data[newSize];
		System.arraycopy (oldArray,0,newArray,0,oldSize);
		newArray[oldSize] = newElem; // remember array index starts at zero
		return newArray; }

	public static Stimulus[] addElement (Stimulus[] oldArray, Stimulus newElem) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		int newSize = oldSize+1;
		Stimulus[] newArray = new Stimulus[newSize];
		System.arraycopy (oldArray,0,newArray,0,oldSize);
		newArray[oldSize] = newElem; // remember array index starts at zero
		return newArray; }

	public static String[] addElement (String[] oldArray, String newElem) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		int newSize = oldSize+1;
		String[] newArray = new String[newSize];
		System.arraycopy (oldArray,0,newArray,0,oldSize);
		newArray[oldSize] = newElem; // remember array index starts at zero
		return newArray; }

	public static Exp[] addElement (Exp[] oldArray, Exp newElem) {
		int oldSize = java.lang.reflect.Array.getLength(oldArray);
		int newSize = oldSize+1;
		Exp[] newArray = new Exp[newSize];
		System.arraycopy (oldArray,0,newArray,0,oldSize);
		newArray[oldSize] = newElem; // remember array index starts at zero
		return newArray; }

	/** parses the given string around the given seperator and places the 
	 *  the final values in a string array
	 *  @param in       String, the input string to be parsed
	 *  @param sep      String, the string seperator to parse around
	 *  @return out     String[], the output array of values
	 */
	public static String[] stringParse(String in, String sep){
		String[] out = new String[0];
		StringTokenizer token = new StringTokenizer(in, sep);
		while(token.hasMoreTokens()){
			out = addElement(out,token.nextToken());
		}
		return(out);    
	}

	/**
	 *  Public static merthod that write StringBuffer to disk. Take two ars, the path and the buffer.
	 *  @param String Path
	 *  @param StringBuffer My Payload (JT Rules!)
	 *  @throws Exception
	 */
	public static void write(String path,StringBuffer buffer,XMLPropTree _xmlPropTree) throws Exception {
		// I have populated the string buffer, dump that mofo
        String strWorkingDir = _xmlPropTree.getProperty("//working_directory/text()");
        String strFileName = strWorkingDir+"/"+path;
        
        // dump the buffer -
        GIOL.write(strFileName,buffer);
		
	}
}
