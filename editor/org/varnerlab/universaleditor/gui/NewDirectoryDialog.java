package org.varnerlab.universaleditor.gui;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.varnerlab.universaleditor.gui.actions.SetLocalDirectoryAction;


public class NewDirectoryDialog extends javax.swing.JInternalFrame {
	static int openFrameCount=1;
	static final int xOffset=50;
	static final int yOffset=50;
	private String _strDirName = "";
	private String _strWorkingDirectory = "";
	private SetLocalDirectoryAction _action = null;
	private FileTransferTool _tool = null;		// we need this to call hide sheet -

	/** Creates new form NewDirectoryDialog */
	public NewDirectoryDialog() {
		// iterate window count
		++openFrameCount;

		// Set the windows location
		setLocation(xOffset*openFrameCount,yOffset*openFrameCount);

		// Set TitleBar color when active/inactive
		setDoubleBuffered(true);

		// Load and configure the different mumbo jumbo -
		initComponents();

		// Set the listener -
		_action = new SetLocalDirectoryAction();
		_action.setDialogReference(this);
		jButton1.addActionListener(_action);

		// Set the closeable button -
		this.setClosable(true);
		this.setTitle("New directory dialog ...");
	}

	public void setParentFrame(FileTransferTool tool)
	{
		_tool = tool;
	}

	public JTextField getTextBox()
	{
		return(jTextField1);
	}

	public void setWorkingDirectory(String name)
	{
		_strWorkingDirectory = name;
	}

	public void setFileName(String name)
	{
		_strDirName = name;
	}

	public String getFileName()
	{
		return(_strDirName);
	}

	public String getWorkingDirectory()
	{
		return(_strWorkingDirectory);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jLabel1 = new javax.swing.JLabel();
		jTextField1 = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();

		setClosable(true);

		jLabel1.setText("Name:");

		jTextField1.setText("");

		// Have the textfield pop-up with focus - not sure why this works...never understood the invokeLater call
		SwingUtilities.invokeLater(new Runnable() 
		{  
			public void run() {  
				jTextField1.requestFocusInWindow();
			}
		});


		jTextField1.requestFocusInWindow();

		jButton1.setText("Ok");

		jButton2.setText("Close");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				closeDialogWindow(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addComponent(jLabel1)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))
										.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
												.addComponent(jButton2)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(jButton1)))
												.addContainerGap())
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGap(20, 20, 20)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel1))
								.addGap(12, 12, 12)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(jButton1)
										.addComponent(jButton2))
										.addContainerGap(15, Short.MAX_VALUE))
		);

		pack();
	}// </editor-fold>



	protected void closeDialogWindow(ActionEvent evt) {
		// TODO Auto-generated method stub
		_tool.hideSheet();
	}



	//Variables declaration - do not modify
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JTextField jTextField1;
	// End of variables declaration

}