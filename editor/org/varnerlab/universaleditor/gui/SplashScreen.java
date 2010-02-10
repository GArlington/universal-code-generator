package org.varnerlab.universaleditor.gui;

import java.awt.*;

import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;
import javax.swing.*;
import com.sun.awt.AWTUtilities;

public class SplashScreen extends JWindow {

	// class/instance variables -
	BorderLayout borderLayout1 = new BorderLayout();
	JLabel imageLabel = new JLabel();
	JPanel southPanel = new JPanel();
	FlowLayout southPanelFlowLayout = new FlowLayout();
	JProgressBar progressBar = new JProgressBar();
	ImageIcon imageIcon;


	public SplashScreen(ImageIcon imageIcon) {
		this.imageIcon = imageIcon;
		try {
			jbInit();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	// note - this class created with JBuilder
	void jbInit() throws Exception {
		
		// Configure the size and have a % shown in the bar -
		progressBar.setStringPainted(true);
		Dimension prefSize = progressBar.getPreferredSize();
		prefSize.width = 500;
		progressBar.setPreferredSize(prefSize);
		progressBar.setBorderPainted(true);
		
		
		// Set the location of the splash screen -
		int inset_X=700;
		int inset_Y=400;
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset_X,inset_Y,screenSize.width-inset_X*2,screenSize.height-inset_Y*2);
        
        // We need to wrap this in some kind of try catch ot do a check to make sure we can support the transluent window -
        if (AWTUtilities.isTranslucencySupported(AWTUtilities.Translucency.TRANSLUCENT))
        {
        	AWTUtilities.setWindowOpacity(this, (float) 0.85);
        }
        
        
        // set the opacity and shape of the window -	
		//RoundRectangle2D shape = new RoundRectangle2D.Double((double)inset_X,(double)inset_Y,(double)screenSize.width-inset_X*2,(double)screenSize.height-inset_Y*2,10.0,10.0);
		//AWTUtilities.setWindowShape(this,shape);
		
		
        // Set the icon and blah blah about the panel -
		imageLabel.setIcon(imageIcon);
		imageLabel.setBackground(Color.DARK_GRAY);
		imageLabel.setForeground(Color.DARK_GRAY);
		setBackground(new Color(100,100,100));
		
		this.getContentPane().setLayout(borderLayout1);
		southPanel.setLayout(southPanelFlowLayout);
		//southPanel.setBackground(new Color(100,100,100));
		southPanel.setBackground(Color.DARK_GRAY);
		this.getContentPane().add(imageLabel, BorderLayout.CENTER);
		this.getContentPane().add(southPanel, BorderLayout.SOUTH);
		southPanel.add(progressBar, null);
		setFont((new Font("Dialog",Font.PLAIN,12)));
		this.pack();
		
		
	}

	public void setProgressMax(int maxProgress)
	{
		progressBar.setMaximum(maxProgress);
	}

	public void setProgress(int progress)
	{
		final int theProgress = progress;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(theProgress);
			}
		});
	}

	public void setProgress(String message, int progress)
	{
		final int theProgress = progress;
		final String theMessage = message;
		setProgress(progress);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(theProgress);
				setMessage(theMessage);
			}
		});
	}

	public void setScreenVisible(boolean b)
	{
		final boolean boo = b;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setVisible(boo);
			}
		});
	}

	private void setMessage(String message)
	{
		if (message==null)
		{
			message = "";
			progressBar.setStringPainted(false);
		}
		else
		{
			progressBar.setStringPainted(true);
		}
		progressBar.setString(message);
	}
}