package org.varnerlab.universaleditor.gui.widgets;

public class WaitThread {

	public static void oneSec() {
		try {
			Thread.currentThread();
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}  
	public static void manySec(long s) {
		try {
			Thread.currentThread();
			Thread.sleep(s * 1000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
