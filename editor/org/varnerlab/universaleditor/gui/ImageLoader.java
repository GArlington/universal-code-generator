/*
 * @(#)ImageLoader.java
 *
 * $Date: 2009-02-22 13:56:32 -0600 (Sun, 22 Feb 2009) $
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/** This class can convert an abstract <code>Image</code> into a ARGB <code>BufferedImage</code>.
 * <P>It was written to replace the <code>MediaTracker</code>; on my Macs this class is faster
 * and more efficient.  It has come to my attention that on Linux this may not be the case
 * (see my <A HREF="http://javagraphics.blogspot.com/2007/04/images-studying-mediatracker.html">blog</A>
 * for more discussion.)
 * <P>Also this class has the added advantage of always returning an RGB image.  Using other
 * methods (such as ImageIO) may return an arbitrary image type.
 * (And this class doesn't require a <code>java.awt.Component</code> to initialize; why
 * does the MediaTracker do that?  It's just a strange animal.)
 * 
 * @version 1.1 this includes a few bug fixes for IndexColorModel-based images.  Also
 * this removes the "load" option, which it turns out didn't really do what it was supposed to.
 */
public class ImageLoader {
	private static boolean debug = false;
	private static final DirectColorModel ARGBModel = (DirectColorModel)ColorModel.getRGBdefault();
	private static final DirectColorModel RGBModel = new DirectColorModel(32,0xff0000,0xff00,0xff,0);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame();
			FileDialog fd = new FileDialog(frame);
			fd.show();
			if(fd.getFile()==null)
				throw new RuntimeException("User cancelled");
			File file = new File(fd.getDirectory()+fd.getFile());
			
			System.out.println("ImageLoader Tests:");
			System.out.println(System.getProperty("java.version")+", "+System.getProperty("os.version"));
			System.out.println("File: "+file.getAbsolutePath());

			Image img = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
			BufferedImage bi = createImage(img);
			System.out.println("Dimensions: "+bi.getWidth()+"x"+bi.getHeight());
			
			int SIZE = 100; //the number of tests
			
			long[] mem = new long[SIZE];
			long[] time = new long[mem.length];
			long[] mem2 = new long[mem.length];
			long[] time2 = new long[mem.length];
			long[] mem3 = new long[mem.length];
			long[] time3 = new long[mem.length];
			int imageLoaderType = -1;
			int imageIOType = -1;

			for(int a = 0; a<mem2.length; a++) {
				mem2[a] = Runtime.getRuntime().freeMemory();
				time2[a] = System.currentTimeMillis();
				Image i = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
				BufferedImage bi2 = createImage(i);
				time2[a] = System.currentTimeMillis()-time2[a];
				mem2[a] = mem2[a]-Runtime.getRuntime().freeMemory();
				imageLoaderType = bi2.getType();
				i.flush();
				bi2.flush();
				
				System.gc();
				System.runFinalization();
				System.gc();
			}
			Arrays.sort(mem2);
			Arrays.sort(time2);
			
			for(int a = 0; a<mem.length; a++) {
				mem[a] = Runtime.getRuntime().freeMemory();
				time[a] = System.currentTimeMillis();
				Image i = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
				createImageWithMediaTracker(i,frame).flush();
				time[a] = System.currentTimeMillis()-time[a];
				mem[a] = mem[a]-Runtime.getRuntime().freeMemory();
				i.flush();
				
				System.gc();
				System.runFinalization();
				System.gc();
			}
			Arrays.sort(mem);
			Arrays.sort(time);

			for(int a = 0; a<mem.length; a++) {
				mem3[a] = Runtime.getRuntime().freeMemory();
				time3[a] = System.currentTimeMillis();
				BufferedImage bi2 = javax.imageio.ImageIO.read(file);
				time3[a] = System.currentTimeMillis()-time3[a];
				mem3[a] = mem3[a]-Runtime.getRuntime().freeMemory();
				imageIOType = bi2.getType();
				bi2.flush();
				
				System.gc();
				System.runFinalization();
				System.gc();
			}
			Arrays.sort(mem3);
			Arrays.sort(time3);

			System.out.println("\tCreating a BufferedImage:");
			System.out.println("MediaTracker took "+time[SIZE/2]+" ms, "+mem[SIZE/2]+" bytes");
			
			System.out.println("ImageLoader took "+time2[SIZE/2]+" ms, "+mem2[SIZE/2]+" bytes (creating "+getType(imageLoaderType)+")");
			System.out.println("(So ImageLoader took "+(time2[SIZE/2]*100/time[SIZE/2])+"% of the time, and "+(mem2[SIZE/2]*100/mem[SIZE/2])+"% of the memory)");

			System.out.println("ImageIO took "+time3[SIZE/2]+" ms, "+mem3[SIZE/2]+" bytes (creating "+getType(imageIOType)+")");
			System.out.println("(So ImageLoader took "+(time2[SIZE/3]*100/time3[SIZE/2])+"% of the time, and "+(mem2[SIZE/2]*100/mem3[SIZE/2])+"% of the memory)");
			
			System.exit(0);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/** For the main() tests, this returns the name of the BufferedImage type provided */
	private static String getType(int imageType) {
		Class c = BufferedImage.class;
		try {
			Field[] f = c.getFields();
			for(int a = 0; a<f.length; a++) {
				if((f[a].getModifiers() & Modifier.STATIC) > 0) {
					Object value = f[a].get(null);
					if(value instanceof Number && 
							f[a].getName().startsWith("TYPE_") && 
							((Number)value).intValue()==imageType) {
						return f[a].getName();
					}
				}
			}
		} catch(Throwable t) {}
		return "Unknown";
	}
	
	private static BufferedImage createImageWithMediaTracker(Image i,Component c) {
		MediaTracker tracker = new MediaTracker(c);
		tracker.addImage(i,0);
		try {
			tracker.waitForAll();
		} catch(InterruptedException e) {
			throw new RuntimeException("MediaTracker failed");
		}
		BufferedImage bi = new BufferedImage(i.getWidth(null),i.getHeight(null),BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.drawImage(i,0,0,null);
		g.dispose();
		return bi;
	}

	public static BufferedImage createImage(URL url) {
		return createImage(Toolkit.getDefaultToolkit().createImage(url));
	}

	public static BufferedImage createImage(File file) {
		return createImage(Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath()));
	}
	
	/** This returns an ARGB BufferedImage depicting the argument <cod>i</code>.
	 * <P>Note that if <code>i</code> is already an ARGB BufferedImage, then it
	 * is immediately returned and this method does NOT duplicate it.
	 * 
	 * @param i
	 * @return an ARGB BufferedImage identical to the argument.
	 */
	public static BufferedImage createImage(Image i) {
		if(i instanceof BufferedImage) {
			BufferedImage bi = (BufferedImage)i;
			int type = bi.getType();
			if(type==BufferedImage.TYPE_INT_ARGB)
				return bi;
			BufferedImage newImage = new BufferedImage(bi.getWidth(),bi.getHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = newImage.createGraphics();
			g.drawImage(bi,0,0,null);
			g.dispose();
			return newImage;
		}
		ImageLoader l = new ImageLoader(i.getSource(),null,null);
		return l.getImage();
	}
	
	/** This checks to see if two DirectColorModels are identical.
	 * Apparently the "equals" method in DirectColorModel doesn't really work.
	 */
	private static boolean equals(DirectColorModel d1,DirectColorModel d2) {
		if(d1.getAlphaMask()!=d2.getAlphaMask())
			return false;
		if(d1.getGreenMask()!=d2.getGreenMask())
			return false;
		if(d1.getRedMask()!=d2.getRedMask())
			return false;
		if(d1.getBlueMask()!=d2.getBlueMask())
			return false;
		if(d1.getColorSpace()!=d2.getColorSpace())
			return false;
		if(d1.isAlphaPremultiplied()!=d2.isAlphaPremultiplied())
			return false;
		if(d1.getTransferType()!=d2.getTransferType())
			return false;
		if(d1.getTransparency()!=d2.getTransparency())
			return false;
		return true;
	}
	
	InnerImageConsumer consumer;
	Cancellable cancellable;
	java.util.List listeners;
	ImageProducer producer;
	float progress = 0;
	
	/** This constructs an ImageLoader.  As soon as an ImageLoader is constructed
	 * the <code>ImageProducer</code> is asked to start producing data.  (However
	 * constructing this object will not block waiting on the image data.)
	 * 
	 * @param p the source of this image
	 * @param c an optional <code>Cancellable</code> object.
	 * @param changeListener an optional <code>ChangeListener</code>.  This will be notified
	 * when a change occurs.  It can be added here in the constructor because loading the image
	 * begins immediately; depending on how <code>ImageProducer.startProduction</code> is implemented
	 * this <i>may</i> be a blocking call so the <code>ChangeListener</code> needs to be added
	 * before the loading begins
	 */
	public ImageLoader(ImageProducer p,Cancellable c,ChangeListener changeListener) {
		cancellable = c;
		producer = p;
		addChangeListener(changeListener);
		consumer = new InnerImageConsumer();
		p.startProduction(consumer);
	}
	
	/** Adds a ChangeListener to this loader.
	 * This listener will be notified either when the image is fully loaded/created,
	 * or when <code>getProgress()</code> changes value.
	 */
	public void addChangeListener(ChangeListener l) {
		if(l==null)
			return;
		
		if(listeners==null)
			listeners = new Vector();
		if(listeners.contains(l))
			return;
		listeners.add(l);
	}
	
	/** Removes a ChangeListener from this loader.
	 */
	public void removeChangeListener(ChangeListener l) {
		if(listeners==null) return;
		listeners.remove(l);
	}
	
	/** Returns a float from [0,1] <i>approximating</i> how much of the image
	 * is loaded.  If your image were a text document, this is basically telling
	 * you where the text cursor was last placed.  However the <code>ImageProducer</code>
	 * may make several different iterations over the image to deliver the complete
	 * image, so it is completely possible that this value will range from 0 to 1 a few
	 * different times.
	 * <P>I wish this could be more precise, but I don't see how more precision is
	 * possible given the way the <code>ImageProducer</code>/<code>ImageConsumer</code> model works.
	 * <P>For the most part, this will be a straight-forward 1-pass system.  If you limit
	 * yourself to certain types of images (like PNGs) this is probably the case.
	 */
	public float getProgress() {
		return progress;
	}
	
	/** This indicates whether this <code>ImageLoader</code> is finished.
	 * <P>Unlike <code>getProgress()</code>, this is guaranteed to be 100%
	 * accurate.
	 */
	public boolean isDone() {
		return finished;
	}
	
	/** Returns the dimension of this image, or null if the dimensions are not yet known.
	 */
	public Dimension getSize() {
		if(size==null) return null;
		return new Dimension(size.width,size.height);
	}
	
	/** Fires all change listeners */
	protected void fireChangeListeners() {
		if(listeners==null)
			return;
		for(int a = 0; a<listeners.size(); a++) {
			ChangeListener l = (ChangeListener)listeners.get(a);
			try {
				l.stateChanged(new ChangeEvent(this));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/** This blocks until the image has finished loading, an error occurs, or the operation has been cancelled.
	 * @return the ARGB image represented in the original ImageProducer, or <code>null</code> if the operation was cancelled.
	 * @throws RuntimeException if an error occurred while loading this image
	 */
	public BufferedImage getImage() {
		
		block();
		
		if(cancellable!=null && cancellable.isCancelled()) {
			producer.removeConsumer(consumer);
			if(finished) //extremely unlikely, but just in case:
				return dest;
			return null;
		}
		if(status==ImageConsumer.IMAGEERROR)
			throw new RuntimeException("An error occurred.");
		if(status==ImageConsumer.IMAGEABORTED)
			throw new RuntimeException("The operation was aborted.");
		
		return dest;
	}

	private Thread waitingThread;
	private Vector waitingThreads;
	/** Blocks this thread until the image is finished
	 * 
	 */
	private void block() {
		Thread t = Thread.currentThread();
		int i = 0;
		synchronized(this) {
			if(waitingThreads==null) {
				//there is no list defined
				if(waitingThread==null) {
					waitingThread = t;
				} else {
					waitingThreads = new Vector();
					waitingThreads.add(waitingThread);
					waitingThreads.add(t);
					i = 1;
					waitingThread = null;
				}
			} else {
				i = waitingThreads.size();
				waitingThreads.add(t);
			}
		}
		while(!finished && (cancellable==null || cancellable.isCancelled()==false)) {
			try {
				t.wait(500);
			} catch(Exception e) {
				Thread.yield();
			}
		}
		synchronized(this) {
			t.interrupted();
			if(waitingThread==t) {
				waitingThread = null;
			}
			if(waitingThreads!=null) {
				waitingThreads.remove(i);
			}
		}
	}
	
	private void unblock() {
		synchronized(this) {
			if(waitingThread!=null) {
				waitingThread.interrupt();
			}
			if(waitingThreads!=null) {
				for(int a = 0; a<waitingThreads.size(); a++) {
					Thread t = (Thread)waitingThreads.get(a);
					t.interrupt();
				}
			}
		}
	}


	boolean finished = false;
	int status;
	Dimension size;
	Properties properties;
	BufferedImage dest;
	
	class InnerImageConsumer implements ImageConsumer {
		public InnerImageConsumer() {}
		
		public void imageComplete(int completionStatus) {
			if(debug) {
				System.err.println("imageComplete(): ");
				if((completionStatus==IMAGEABORTED))
					System.err.println("\tIMAGEABORTED");
				if((completionStatus==IMAGEERROR))
					System.err.println("\tIMAGEERROR");
				if((completionStatus==SINGLEFRAMEDONE))
					System.err.println("\tSINGLEFRAMEDONE");
				if((completionStatus==STATICIMAGEDONE))
					System.err.println("\tSTATICIMAGEDONE");
			}

			producer.removeConsumer(this);
			
			status = completionStatus;
			finished = true;
			unblock();
			fireChangeListeners();
		}
		
		int[] indexed;
		public void setColorModel(ColorModel cm) {
			if(debug) System.err.println("setColorModel( "+cm+" )");
			lastCM = cm;
			indexed = null;
			if(cm instanceof IndexColorModel) {
				IndexColorModel i = (IndexColorModel)cm;
				if(i.getMapSize()<=256) {
					indexed = new int[i.getMapSize()];
					for(int a = 0; a<indexed.length; a++) {
						int r = i.getRed(a);
						int g = i.getGreen(a);
						int b = i.getBlue(a);
						indexed[a] = (255 << 24)+(r << 16)+(g << 8) + b;
					}
					int t = i.getTransparentPixel();
					if(i.hasAlpha() && t>=0 && t<indexed.length) {
						if(debug)
							System.err.println("i.getTransparentPixel: "+i.getTransparentPixel());
						indexed[t] = 0;
					}
				}
			}
		}

		private transient int[] row;
		public void setDimensions(int w, int h) {
			if(debug)
				System.err.println("setDimensions("+w+","+h+")");
			if(w<=0) throw new IllegalArgumentException("Width must be greater than zero.  ("+w+")");
			if(h<=0) throw new IllegalArgumentException("Height must be greater than zero.  ("+h+")");
			if(size!=null) {
				//eh?  already exists?
				if(size.width==w && size.height==h)
					return;
				if(dest!=null) {
					throw new RuntimeException("An image of "+(size.getWidth())+"x"+size.getHeight()+" was already created.  Illegal attempt to call setDimensions("+w+","+h+")");
				}
			}
			size = new Dimension(w,h);
			dest = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
			row = new int[w];
			fireChangeListeners();
		}

		public void setHints(int hints) {
			if(debug) {
				System.err.println("setHints():");
				if((hints & COMPLETESCANLINES) > 0)
					System.err.println("\tCOMPLETESCANLINES");
				if((hints & RANDOMPIXELORDER) > 0)
					System.err.println("\tSINGLEFRAME");
				if((hints & SINGLEFRAME) > 0)
					System.err.println("\tSINGLEFRAME");
				if((hints & SINGLEPASS) > 0)
					System.err.println("\tSINGLEPASS");
				if((hints & TOPDOWNLEFTRIGHT) > 0)
					System.err.println("\tTOPDOWNLEFTRIGHT");
			}
		}

		public void setPixels(int x, int y, int w, int h, ColorModel cm, byte[] data, int offset, int scanSize) {
			if(cancellable!=null && cancellable.isCancelled()) {
				if(debug) System.err.println("removing this consumer because the Cancellable was activated");
				producer.removeConsumer(this);
				unblock();
			}
			
			if(size==null) throw new RuntimeException("The dimensions of this image are not yet defined.  Cannot write image data until the dimensions of the image are known.");

			if(debug)
				System.err.println(Thread.currentThread().getName()+" setPixels("+x+" ,"+y+" ,"+w+" ,"+h+", "+cm+", ..., "+offset+", "+scanSize+") (byte[])");
			
			if(cm==lastCM && indexed!=null) {
				int argb;
				byte k = 0;
				int k2 = 0;
				for(int n = y; n<y+h; n++) {
					for(int m = x; m<x+w; m++) {
						k = data[(n-y)*scanSize+(m-x)+offset];
						if(k>=0) {
							k2 = k;
						} else {
							k2 = k+256;
						}
						argb = indexed[k2];
						row[m-x] = argb;
					}
					dest.getRaster().setDataElements(x,n,w,1,row);
				}
			} else {
				int transIndex = (cm instanceof IndexColorModel) ?
					((IndexColorModel)cm).getTransparentPixel() : -1;
			
				int argb;
				for(int n = y; n<y+h; n++) {
					for(int m = x; m<x+w; m++) {
						byte k = data[(n-y)*scanSize+(m-x)+offset];
						int k2 = k & 0xff;
						if(k2==transIndex) {
							argb = 0;
						} else {
							argb = cm.getRGB(k2);
						}
						row[m-x] = argb;
					}
					dest.getRaster().setDataElements(x,n,w,1,row);
				}
			}
			setProgress(x+w,y+h);
		}

		private boolean lastCMwasRGB = false;
		private boolean lastCMwasARGB = false;
		ColorModel lastCM = null;
		public void setPixels(int x, int y, int w, int h, ColorModel cm, int[] data, int offset, int scanSize) {
			if(cancellable!=null && cancellable.isCancelled()) {
				if(debug) System.err.println("removing this consumer because the Cancellable was activated");
				producer.removeConsumer(this);
				unblock();
			}

			if(debug)
				System.err.println("setPixels("+x+" ,"+y+" ,"+w+" ,"+h+", "+cm+", ..., "+offset+", "+scanSize+") (int[])");
			
			if(size==null) throw new RuntimeException("The dimensions of this image are not yet defined.  Cannot write image data until the dimensions of the image are known.");
			if(cm instanceof DirectColorModel) {
				//don't call "equals" all the time; that's just a waste
				//this is a little uglier, but it will save hundreds of method calls:
				boolean quickRGB = (cm==lastCM && lastCMwasRGB);
				boolean quickARGB = (cm==lastCM && lastCMwasARGB);
				
				DirectColorModel d = (DirectColorModel)cm;
				if(quickARGB || ((!quickRGB) && ImageLoader.equals(d,ARGBModel))) {
					for(int n = y; n<y+h; n++) {
						int k = (n-y)*scanSize-x+offset;
						System.arraycopy(data,k,row,0,w);
						dest.getRaster().setDataElements(x,n,w,1,row);
					}
					lastCMwasRGB = false;
					lastCMwasARGB = true;
					lastCM = cm;
					return;
				} else if(quickRGB || ImageLoader.equals(d,RGBModel)) {
					//same thing, but no alpha:
					for(int n = y; n<y+h; n++) {
						int k = (n-y)*scanSize-x+offset;
						System.arraycopy(data,k,row,0,w);
						//add alpha to every pixel:
						for(int a = 0; a<w; a++) {
							row[a] = (row[a] & 0xffffff)+0xff000000;
						}
						dest.getRaster().setDataElements(x,n,w,1,row);
					}
					lastCMwasRGB = true;
					lastCMwasARGB = false;
					lastCM = cm;
					return;
				}
			}
			int argb;
			//If you know what your ColorModel is,
			//it'd be good to make a special-case loop
			//to deal with that data (like the if/then above),
			//because a million calls to getRGB() is NOT cheap.
			for(int n = y; n<y+h; n++) {
				for(int m = x; m<x+w; m++) {
					argb = cm.getRGB(data[(n-y)*scanSize+(m-x)+offset]);
					row[m-x] = argb;
				}
				dest.getRaster().setDataElements(x,n,w,1,row);
			}
			lastCMwasRGB = false;
			lastCMwasARGB = false;
			lastCM = cm;
			
			setProgress(x+w,y+h);
		}

		public void setProperties(Hashtable p) {
			//TODO: one day integrate these
			//it wouldn't be hard to extend BufferedImage
			//and override the methods regarding
			//"getProperty" if you need to.
			
			if(properties==null)
				properties = new Properties();
			properties.putAll(p);
			if(debug) {
				System.err.println("setProperties():");
				properties.list(System.err);
			}
		}
	}
	
	private void setProgress(int x,int y) {
		progress = ((float)(y*size.width+x))/((float)(size.width*size.height));
		fireChangeListeners();
	}
}
