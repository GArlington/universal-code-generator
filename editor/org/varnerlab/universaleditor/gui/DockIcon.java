/*
 * @(#)DockIcon.java
 *
 * $Date: 2009-02-20 01:34:41 -0600 (Fri, 20 Feb 2009) $
 *
 * Copyright (c) 2009 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * http://javagraphics.blogspot.com/
 * 
 * And the latest version should be available here:
 * https://javagraphics.dev.java.net/
 */
package org.varnerlab.universaleditor.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

import javax.imageio.ImageIO;

/** This uses reflection to set the dock icon.
 * If you're using Mac 10.5+, this uses the new
 * methods mentioned here:
 * http://developer.apple.com/releasenotes/Java/JavaLeopardUpdate2RN/ResolvedIssues/chapter_3_section_13.html
 * 
 * <P>Otherwise this tries to use the older Cocoa classes
 * (which are ideal for Mac 10.4-ish).
 * 
 * <P>Note that the "correct" way to set the dock icon
 * changed once, and it may change again, so I would
 * be reluctant to use this feature as a vital element
 * in my program.  (That is: it may fail in coming years.)
 * 
 * <P>The Cocoa classes are deprecated, but if you have
 * users running 10.4 it's probably the best way to go.
 * You must add "System/Library/Java" to your classpath
 * (either in Eclipse, your app's Info.plist, etc.) for
 * the Cocoa classes to work.
 */
public class DockIcon {
	private static boolean isMac = (System.getProperty("os.name").toLowerCase().indexOf("mac")!=-1);
	
	/** These relate to the Mac OS 10.5+ model: */
	private static Object theApplication;
	private static Method setDockIconImage;
	private static Object defaultImage;
	
	/** These relate to the pre Mac OS 10.5 approach: */
	private static Constructor NSImageURLConstructor;
	private static Object theNSApplication;
	private static Object defaultNSImage;
	private static Method setApplicationIconImageMethod;
	
	private static boolean initialized = false;
	private static boolean working = false;
	private static boolean debugging = false;

	private static void init() {
		if(initialized)
			return;
		try {
			if(isMac==false)
				return;
			
			Throwable error1 = null;
			try {
				Class appClass = Class.forName("com.apple.eawt.Application");
				theApplication = appClass.getMethod("getApplication", new Class[] {}).invoke(null, new Object[] {});
				setDockIconImage = appClass.getMethod("setDockIconImage", new Class[] { Image.class });
				
				Method getMethod = appClass.getMethod("getDockIconImage", new Class[] {});
				defaultImage = getMethod.invoke(theApplication, new Object[]{});
				
				working = true;
				if(debugging)
					System.out.println("Using Application.setDockImage()");
				return;
			} catch(Throwable t) {
				error1 = t;
			}
			try {
				Class NSImageClass = Class.forName("com.apple.cocoa.application.NSImage");
				NSImageURLConstructor = NSImageClass.getConstructor(new Class[] { URL.class });
				Class NSApplicationClass = Class.forName("com.apple.cocoa.application.NSApplication");
				Method sharedMethod = NSApplicationClass.getMethod("sharedApplication",new Class[0]);
				theNSApplication = sharedMethod.invoke(null,new Object[0]);
				setApplicationIconImageMethod = NSApplicationClass.getMethod("setApplicationIconImage",new Class[] {NSImageClass});
				
				Method currentAppImage = NSApplicationClass.getMethod("applicationIconImage",new Class[0]);
				defaultNSImage = currentAppImage.invoke(theNSApplication,null);
				
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						set(null);
					}
				});
				working = true;
				if(debugging)
					System.out.println("Using NSApplication.setApplicationIconImage()");
			} catch(Throwable t) {
				//do nothing
			}
			
			if(! working) {
				handleError(error1);
			}
		} finally {
			initialized = true;
		}
	}
	
	/** @return <code>true</code> if this class can modify the dock icon.
	 */
	public static boolean isActive() {
		init();
		return working;
	}
	
	private static void handleError(Throwable t) {
		//you may want to deal with this differently?
		if(debugging)
			t.printStackTrace();
	}
	
	/** Using the Cocoa classes requires we create a lot of files.
	 * We'll delete the old ones as we go:
	 */
	private static File lastFile;
	
	/** This tries to reassign the icon of this application
	 * in the dock.
	 * @param i the image to set the dock icon to.  If this is
	 * null, then the default icon is restored.
	 * @return <code>true</code> if it appears this call worked.
	 */
	public static synchronized boolean set(BufferedImage i) {
		init();
		if(working==false) return false;
		
		if(setDockIconImage!=null && theApplication!=null) {
			Image image = i;
			if(i==null)
				image = (Image)defaultImage;
			try {
				setDockIconImage.invoke(theApplication, new Object[] {image});
				return true;
			} catch(Throwable t) {
				handleError(t);
			}
		} else if(theNSApplication!=null && NSImageURLConstructor!=null) {
			try {
				Object theImage = defaultNSImage;
				File newFile = null;
				if(i!=null) {
					newFile = File.createTempFile("dockIcon", ".png");
					newFile.deleteOnExit();
					ImageIO.write(i, "png", newFile);
					theImage = NSImageURLConstructor.newInstance(new Object[] { newFile.toURL() });
				}
				setApplicationIconImageMethod.invoke(theNSApplication,new Object[] {theImage});
				
				if(lastFile!=null) {
					lastFile.delete();
				}
				
				lastFile = newFile;
				
				return true;
			} catch(Throwable t) {
				handleError(t);
			}
		}
		return false;
	}
}
