package org.varnerlab.universaleditor.gui;

import java.awt.*;

import javax.swing.*;
import com.sun.awt.AWTUtilities;

public class NoImageSplashScreen extends JWindow {

	// class/instance variables -
	BorderLayout borderLayout1 = new BorderLayout();
	JLabel messageLabel = new JLabel();
	JPanel southPanel = new JPanel();
	FlowLayout southPanelFlowLayout = new FlowLayout();
	JProgressBar progressBar = new JProgressBar();
	ImageIcon imageIcon;


	public NoImageSplashScreen() {
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
		
		// set the opacity of the window -
		AWTUtilities.setWindowOpacity(this, (float) 0.85);
		
		// Set the location of the splash screen -
		int inset_X=700;
		int inset_Y=400;
        Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset_X,inset_Y,screenSize.width-inset_X*2,screenSize.height-inset_Y*2);
		
        // Set the icon and blah blah about the panel -
		messageLabel.setText("Message will go here ...");
		messageLabel.setBackground(Color.DARK_GRAY);
		messageLabel.setForeground(Color.white);
		setBackground(new Color(100,100,100));
		
		this.getContentPane().setLayout(borderLayout1);
		southPanel.setLayout(southPanelFlowLayout);
		//southPanel.setBackground(new Color(100,100,100));
		southPanel.setBackground(Color.DARK_GRAY);
		this.getContentPane().add(messageLabel, BorderLayout.CENTER);
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