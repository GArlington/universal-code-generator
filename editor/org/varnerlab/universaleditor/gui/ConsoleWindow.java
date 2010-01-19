/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConsoleWindow.java
 *
 * Created on Mar 26, 2009, 6:45:23 AM
 */

package org.varnerlab.universaleditor.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.swing.ImageIcon;

import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.widgets.ConsoleToolFocusListener;
import org.varnerlab.universaleditor.service.PublishService;

/**
 *
 * @author jeffreyvarner
 */
public class ConsoleWindow extends javax.swing.JInternalFrame {
    static int openFrameCount=1;
    static final int xOffset=50;
    static final int yOffset=50;

    private StringBuffer _buffer = new StringBuffer();
    private StringBuffer _serverBuffer = new StringBuffer();

    private ImageIcon _imgIconOff = null;
    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconCurrent = null;
    
    // private MulticastSocket _socket = null;
    // private InetAddress _address = null;
    // private DatagramPacket _packet = null;
    private Thread _serverThread = null;
    private long DELAY = 10000;


    public void setOffIcon(ImageIcon imgIcon)
    {
        _imgIconOff = imgIcon;
    }

    public void setOnIcon(ImageIcon imgIcon)
    {
        _imgIconOn = imgIcon;
    }

    public ImageIcon getOffIcon()
    {
        return(_imgIconOff);
    }

    public ImageIcon getOnIcon()
    {
        return(_imgIconOn);
    }

    
  
    
    /** Creates new form ConsoleWindow */
    public ConsoleWindow() {
        

        // iterate window count
        ++openFrameCount;

        // Set the windows location
        setLocation(xOffset*openFrameCount,yOffset*openFrameCount);

        // Set TitleBar color when active/inactive
        setDoubleBuffered(true);

        // Setup -
        initComponents();

        //jScrollPane1.setBackground(new Color(100,100,100, 130));
        //jTextPane1.setBackground(new Color(100,100,100, 130));

        this.setOpaque(false);
        this.getContentPane().setBackground(new Color(0,0,0,185));

        // Add a focus/click listerner -
        this.addInternalFrameListener(new ConsoleToolFocusListener());      
    }

    
    public void postToConsole(String txt)
    {
        _buffer.append(txt);
        _buffer.append("\n");
        this.jTextArea1.setText(_buffer.toString());
    }
    
    public void postToServerConsole(String txt)
    {
        _serverBuffer.append(txt);
        _serverBuffer.append("\n");
        
        this.jTextArea2.setText(_serverBuffer.toString());
    }

    
    public void sendUpdateRequestToServer() 
    {
    	// Ok, when I get here I can start to monitor the server -
		DatagramSocket socket = null;
		InetAddress address = null;
		DatagramPacket packet = null;
		
		try {
		
			socket = new DatagramSocket();
		}
		catch (Exception error)
		{
			System.out.println("ERROR multicast connection issue - "+error.toString());	
		}

		
		try {
			
			
			byte[] buf_send = null;
			
			// Ok, we need to get the current selected project from session -
			UEditorSession session = Launcher.getInstance().getSession();
			String strUserName = (String)session.getProperty("VALIDATED_USERNAME");
			String strSessionID = (String)(String)session.getProperty("SELECTED_SESSION_ID");
			String strPath = "";
			if (strUserName!=null && strSessionID!=null && !strUserName.isEmpty() && !strSessionID.isEmpty())
			{
				strPath = strUserName+"/"+strSessionID;
			}
			else
			{
				strPath = ""; 
			}
				
			buf_send = strPath.getBytes();
			InetAddress server_address = InetAddress.getLocalHost();
			packet = new DatagramPacket(buf_send, buf_send.length,server_address,4446);
			socket.send(packet);
			
			
			// Get the response -
			byte[] buf = new byte[socket.getReceiveBufferSize()];
			packet = new DatagramPacket(buf,buf.length);
			socket.receive(packet);

			String received = new String(packet.getData(), 0, packet.getLength());
			//_serverBuffer.append(received);
			
			// System.out.println("What are we getting from the server? - "+received);
			postToServerConsole(received);
			
		}
		catch (Exception error)
		{
			// error.printStackTrace();
		}
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(100, 100, 100));
        setClosable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("Universal Console");
        
        jPanel1.setOpaque(false);
        jPanel1.setBackground(new Color(0,0,0,185));

        jScrollPane1.setBackground(new java.awt.Color(0, 0, 0));
        jScrollPane1.setOpaque(false);

        jTextArea1.setBackground(new java.awt.Color(0, 0, 0));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("Courier", 0, 10));
        jTextArea1.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTabbedPane1.addTab("Local", jScrollPane1);

        jScrollPane2.setBackground(new java.awt.Color(0, 0, 0));
        jScrollPane2.setOpaque(false);

        jTextArea2.setBackground(new java.awt.Color(0, 0, 0));
        jTextArea2.setColumns(20);
        jTextArea2.setEditable(false);
        jTextArea2.setFont(new java.awt.Font("Courier", 0, 10)); // NOI18N
        jTextArea2.setForeground(new java.awt.Color(255, 255, 255));
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jTabbedPane1.addTab("Server", jScrollPane2);

        jButton1.setText("Clear");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18))
        );
        
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	clearWindow(evt);
            }
        });

        pack();
    }// </editor-fold>

    /*
    @Override
    protected void paintComponent(Graphics g)
    {
        // set transparent color
        g.setColor(new Color(100,100,100,64));

        g.fillRect(0, 0, getWidth(), getHeight());
    }
     */

    private void clearWindow(java.awt.event.ActionEvent evt)
    {
    	// Ok, so we need to figure out which tab is open -
    	int intSelected = jTabbedPane1.getSelectedIndex();
    	if (intSelected!=-1)
    	{
    		// Ok, so one is selected -
    		if (intSelected == 0)
    		{
    			_buffer.delete(0,_buffer.length());
    			this.postToConsole("\n");
    		}
    		else
    		{
    			_serverBuffer.delete(0, _serverBuffer.length());
    			this.postToServerConsole("\n");
    		}
    	}
    	
    }
    
    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    // End of variables declaration


	

}