package org.varnerlab.universaleditor.gui.widgets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;

public class FileTransferJPopupMenuMouseAdapter extends MouseAdapter {
	// class/instance attributes -
	private JPopupMenu _popup = null;			// Reference to the popup menu -
	private JInternalFrame _tool = null;		// Reference to the tool that is launching the popup -
	
	
	public void setJPopupReference(JPopupMenu popup)
	{
		_popup = popup;
	}
	
	public void setToolReference(JInternalFrame frame)
	{
		_tool = frame;
	}
	
	public void mousePressed(MouseEvent e) {
		//System.out.println("What is going on?? "+e); 
		checkPopup(e);
	}

	public void mouseClicked(MouseEvent e) {
		//System.out.println("What is going on?? "+e);   
		checkPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		//System.out.println("What is going on?? "+e);   
		checkPopup(e);
	}

	private void checkPopup(MouseEvent e) {
		//System.out.println("What is going on?? "+e);
		if (e.isPopupTrigger())
		{
			_popup.show(_tool, e.getX(), e.getY());
		}
	}
	
}
