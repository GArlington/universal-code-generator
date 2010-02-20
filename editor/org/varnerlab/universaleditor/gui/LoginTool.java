/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * LoginTool.java
 *
 * Created on Mar 17, 2009, 10:08:50 AM
 */

package org.varnerlab.universaleditor.gui;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.varnerlab.universaleditor.service.PublishService;
import org.varnerlab.universaleditor.service.ServerJobTypes;
import org.varnerlab.universaleditor.service.SocketService;
import org.varnerlab.universaleditor.service.SystemwideEventService;
import org.varnerlab.universaleditor.database.DatabaseAPI;
import org.varnerlab.universaleditor.database.GConnectionFactory;
import org.varnerlab.universaleditor.database.IGFactory;
import org.varnerlab.universaleditor.domain.*;
import org.varnerlab.universaleditor.gui.actions.CheckUserNameAndPasswordAction;
import org.varnerlab.universaleditor.gui.widgets.LoginToolFocusListener;
import org.varnerlab.universaleditor.gui.widgets.VLDesktopPane;
import org.varnerlab.universaleditor.gui.widgets.VLDialogGlassPane;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author jeffreyvarner
 */
public class LoginTool extends javax.swing.JInternalFrame {
    // Class/instance attributes -
    static int openFrameCount=1;
    static final int xOffset=50;
    static final int yOffset=50;

    private ImageIcon _imgIconOff = null;
    private ImageIcon _imgIconOn = null;
    private ImageIcon _imgIconCurrent = null;
    private DatabaseAPI dbAPI = null;
    private UEditorSession _session = null;
    private VLDialogGlassPane _glassPane = null;
    private XPathFactory  _xpFactory = XPathFactory.newInstance();
	private XPath _xpath = _xpFactory.newXPath();

   
	private void processPropertyAttributes(String strXPath,Document doc,IGFactory session) throws Exception
    {
    	NodeList propNodeList = (NodeList)_xpath.evaluate(strXPath, doc, XPathConstants.NODESET);
    	int NUMBER_PATH_NODES = propNodeList.getLength();
    	for (int index=0;index<NUMBER_PATH_NODES;index++)
    	{
    		// Get the current node -
    		Node tmpNode = propNodeList.item(index);
    		
    		// Process the attributes of this node ..
    		NamedNodeMap map = tmpNode.getAttributes();
    		int NUMBER_OF_ATTRIBUTES = map.getLength();
    		for (int att_index=0;att_index<NUMBER_OF_ATTRIBUTES;att_index++)
    		{
    			// Ok bitches, so I should get the attribute name (capitalize it) and key the value 
    			Node attNode = map.item(att_index);
    			String keyName = ((String)attNode.getNodeName()).toUpperCase();
    			String strValue = attNode.getNodeValue();
    			
    			// store the key,value pair in the session object -
    			

    			
    			session.setProperty(keyName, strValue);
    		}
    	}
    }
    
    public void setUserName(String name)
    {
    	txtFldUserName.setText(name);
    }
    
    public void setPassword(String password)
    {
    	pwdTxtField.setText(password);
    }

    /** Creates new form LoginTool */
    public LoginTool() {

         // Call to super
        super("Login to UNIVERSAL",false,true);


        // Set the windows location
        setLocation(xOffset*openFrameCount,yOffset*openFrameCount);
        
        // Super-awesome
        //getRootPane().putClientProperty("Window.alpha", new Float(0.8f));

        // Set TitleBar color when active/inactive
        setDoubleBuffered(true);

         // Add a focus/click listerner -
        this.addInternalFrameListener(new LoginToolFocusListener());

        // Get the session object -
        _session = (Launcher.getInstance()).getSession();

        // Ok, so I need to establish a connection with the database -
        connectToDatabase();

        _glassPane = new VLDialogGlassPane(this,(new ImageIcon(VLImageLoader.getPNGImage("agt_update_critical-32.png"))),60);
        
        // Build this mofo -
        initComponents();

        //this.setOpaque(false);
        this.getContentPane().setBackground(new Color(0,0,0));

        //jPanel1.setOpaque(false);
        jPanel1.setBackground(new Color(0,0,0));
        
        // Have the login button have the focus -
        jButton1.requestFocusInWindow();
    }

    private void connectToDatabase() {
        try
        {
            // Ok, we need to check to see if the database connection is in Session -
            dbAPI = (DatabaseAPI)_session.getProperty("DATABASE_CONNECTION");

            // Check if connection -
            if (dbAPI == null)
            {

                PublishService.submitData("No current db connection. Let's try to create a new database connection -");

                // Ok, create a connection factory object -
                IGFactory tmpFactory = new GConnectionFactory();
                
                // Configure the connection factory -
                
                // Get the prop_tree out of memory -
                Document doc = (Document)_session.getProperty("UNIVERSAL_DOM_TREE");
                
                // Database information -
            	String strXPDatabase = "//database_information/property";
            	processPropertyAttributes(strXPDatabase,doc,tmpFactory);
            	
            	 // If I get here, then I don't have a db component. Load one -
                dbAPI = new DatabaseAPI();
                dbAPI.setProperty("CONNECTION_FACTORY", tmpFactory);

                // Load the config and establish the connection -
                dbAPI.loadConnection();

                // Put dbAPI into session -
                _session.setProperty("DATABASE_CONNECTION", dbAPI);

                PublishService.submitData("New db connection established and cached in session");
            }
        }
        catch (Exception error)
        {

        }
    }


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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        pwdTxtField = new javax.swing.JPasswordField();
        txtFldUserName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        txtFldUserName1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Username");

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Password");

        jCheckBox1.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox1.setText("Create new project?");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        jCheckBox1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jCheckBox1PropertyChange(evt);
            }
        });
        jCheckBox1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBox1StateChanged(evt);
            }
        });

        txtFldUserName1.setEditable(false);
        txtFldUserName1.setEnabled(false);

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Project name");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jCheckBox1)
                .addContainerGap())
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, txtFldUserName1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, txtFldUserName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pwdTxtField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel3))
                .add(134, 134, 134))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtFldUserName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pwdTxtField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBox1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtFldUserName1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3)))
        );

        jButton1.setText("Login");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkUserNameAndPassword(evt);
            }
        });

        jButton2.setText("Clear");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearTextFields(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jButton1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 287, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(26, 26, 26)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(45, 45, 45)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButton2)
                            .add(jButton1))
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void checkUserNameAndPassword(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkUserNameAndPassword
        // Create the action -
        CheckUserNameAndPasswordAction newLoginAction = new CheckUserNameAndPasswordAction();
        boolean isOkToLogin = false;

        PublishService.submitData("Checking username and password.");

        // Set the username, password and database connection -
        newLoginAction.setProperty("DATABASE_CONNECTION", dbAPI);

        // Get the text from txt and password fields -
        String strUserName = txtFldUserName.getText();
        String strPassword = new String(pwdTxtField.getPassword());

        newLoginAction.setProperty("USERNAME", strUserName);
        newLoginAction.setProperty("USER_PASSWORD", strPassword);

        // Check the username -
        newLoginAction.actionPerformed(evt);

        // Get the login flaq -
        isOkToLogin = newLoginAction.isOkToLogIn();

        // If something went wrong w/the login. Notify the user -
        if (!isOkToLogin)
        {
            PublishService.submitData("Hey monkey spank - what is your major malfunction?");
            _glassPane.setMessage("Login Failed!");
            _glassPane.blowMe();
        }
        else
        {
            PublishService.submitData("Just want to do something special ...");
            PublishService.submitData("for all the ladies of the world.");
            PublishService.submitData("Your in. Enjoy the flight.");

            // If I get here then I need to let the user know that they are logged on -
            // How should I do this??
            UEditorSession session = (Launcher.getInstance()).getSession();
            session.setProperty("VALIDATED_USERNAME",strUserName);
            session.setProperty("USER_PASSWORD", strPassword);

            VLDesktopPane desktop = (VLDesktopPane)(Launcher.getInstance()).getContentPane();
            desktop.setUserName(strUserName);
           
            

            // Ok, I need to check to see if the check box is enabled -
            if (jCheckBox1.isSelected())
            {

                // Ok, so I also need to contact the backend and drop some knowledge on it ...
                VLCreateRemoteDir remoteDir = new VLCreateRemoteDir();
                remoteDir.setProperty("IPADDRESS", session.getProperty("SERVER_ADDRESS"));
                remoteDir.setProperty("PORT", session.getProperty("SERVER_PORT"));
                remoteDir.setProperty("USERNAME",strUserName);
                remoteDir.setProperty("SESSIONID",session.getProperty("SESSION_ID"));

                // Formute the message -
                String strMsg = remoteDir.formulateXMLMessage();

                try {
                    // Send the message -
                    String strIPAddress = (String)session.getProperty("SERVER_ADDRESS");
                    String strPort = (String)session.getProperty("SERVER_PORT");

                    PublishService.submitData("Trying to contact - "+strIPAddress+" on port "+strPort);

                    String rFlag = SocketService.sendMessage(strMsg, strIPAddress, strPort,session,ServerJobTypes.MAKE_NEW_REMOTE_DIRECTORY);
                  
                    PublishService.submitData("Creating a directory on the server - server issued "+rFlag);

                    //remoteDir.sendMessage(strMsg);

                    // Launch -
                    SystemwideEventService.fireUsernameUpdateEvent();
                    SystemwideEventService.fireSessionUpdateEvent();

                    // Update the database -
                    String strSessionID = (String)session.getProperty("SESSION_ID");
                    String strUName = (String)session.getProperty("VALIDATED_USERNAME");
                    String strProjectName = (String)txtFldUserName1.getText();

                    // put in the db -
                    dbAPI.insertProjectInformation(strUName, strSessionID, strProjectName);

                    // ok, now that we have inserted the project-info into the db, we need to grab the translation table -
                    session.setProperty("PROJECT_TRANSLATION_TABLE", dbAPI.getUserProjects(strUName));
                    
                    // Close the name dialog -
                    this.dispose();

                }
                catch (Exception error)
                {
                    PublishService.submitData("What the duck...."+error);
                    this.dispose();
                }
            }
            else
            {
                
            	// ok, now that we have inserted the project-info into the db, we need to grab the translation table -
                try {
                	session.setProperty("PROJECT_TRANSLATION_TABLE", dbAPI.getUserProjects(strUserName));
                }
                catch (Exception error)
                {
                	System.out.println("Trying to get the translation table - wtf? "+error.toString());
                }
            	
            	
            	
                // Update the session and then shutdown -
                SystemwideEventService.fireSessionUpdateEvent();
                this.dispose();
            }
            
        }

    }//GEN-LAST:event_checkUserNameAndPassword

    private void clearTextFields(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearTextFields
        txtFldUserName.setText("");
        pwdTxtField.setText("");
    }//GEN-LAST:event_clearTextFields

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:

        // Set the enambled -
        txtFldUserName1.setEnabled(true);
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jCheckBox1PropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1PropertyChange

    private void jCheckBox1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBox1StateChanged
        // TODO add your handling code here:

        if (jCheckBox1.isSelected())
        {
            txtFldUserName1.setEnabled(true);
            txtFldUserName1.setEditable(true);
        }
        else
        {
            txtFldUserName1.setEnabled(false);
            txtFldUserName1.setEditable(false);
        }

    }//GEN-LAST:event_jCheckBox1StateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPasswordField pwdTxtField;
    private javax.swing.JTextField txtFldUserName;
    private javax.swing.JTextField txtFldUserName1;
    // End of variables declaration//GEN-END:variables

}
