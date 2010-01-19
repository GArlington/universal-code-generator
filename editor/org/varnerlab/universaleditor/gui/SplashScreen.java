package org.varnerlab.universaleditor.gui;

import java.awt.*;
import javax.swing.*;

public class SplashScreen extends JWindow implements Runnable {
	    
	    private int duration;
	    private Thread _thread = null;
	    private JLabel copyrt = null;
	    
	    public SplashScreen(int d) {
	        duration = d;
	        _thread = new Thread();
	    }
	    
	    public void setMessage(String msg)
	    {
	    	copyrt.setText(msg);
	    }
	    
	    // A simple little method to show a title screen in the center
	    // of the screen for the amount of time given in the constructor
	    public void showSplash() {
	    	JPanel content = (JPanel)getContentPane();
	        content.setBackground(Color.white);
	        
	        // Set the window's bounds, centering the window
	        int width =  450;
	        int height = 450;
	        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	        int x = (screen.width-width)/2;
	        int y = (screen.height-height)/2;
	        setBounds(x,y,width,height);
	        
	        // Build the splash screen
	        JLabel label = new JLabel(new ImageIcon("/Users/jeffreyvarner/dev/UniversalWeb/UniversalEditor/images/UniversalLogo-256.png"));
	        copyrt = new JLabel
	                ("Copyright 2002, O'Reilly & Associates", JLabel.CENTER);
	        copyrt.setFont(new Font("Sans-Serif", Font.PLAIN, 12));
	        content.add(label, BorderLayout.CENTER);
	        content.add(copyrt, BorderLayout.SOUTH);
	        Color oraRed = new Color(156, 20, 20,  255);
	        //content.setBorder(BorderFactory.createLineBorder(oraRed, 10));
	        
	        // Display it
	        setVisible(true);
	        
	        // Wait a little while, maybe while loading resources
	        try {Thread.sleep(duration); } catch (Exception e) {}
	        
	        setVisible(false);        
	    }
	    
	    public void showSplashAndExit() {
	        _thread.start();
	    }

		public void run() {
			this.showSplash();
		}
}
