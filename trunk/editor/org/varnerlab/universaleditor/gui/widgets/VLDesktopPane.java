/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;

/**
 *
 * @author jeffreyvarner
 */
public class VLDesktopPane extends JDesktopPane {
   ImageIcon image;

   private String strUserLoggedIn = "No user is logged in.";
   private String strNetworkSelected = "No network has been selected.";
   private String strPropFile = "No model properties file has been selected.";
   
   public VLDesktopPane( ImageIcon image ) {
      super();
      this.image = image;
   }

   public void paintComponent(Graphics g) {
      super.paintComponent(g);

        super.paintComponent(g);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		Dimension size = getSize();

		Font font = g.getFont();
		int arc = 0;

		int h = size.height;
		int w = size.width;

		if (size.width > 300)
		{
			font = font.deriveFont(Font.PLAIN,48);
			arc = 20;
		}
		else if (size.width > 150)
		{
			font = font.deriveFont(Font.PLAIN,24);
			arc = 10;
		}
		else
		{
			font = font.deriveFont(Font.PLAIN,12);
			arc = 3;
		}

		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		

		g.setColor(Color.WHITE);
		//x = (size.width - (int)stringBounds.getWidth()) / 2;
        //y = (size.height / 2) + ((metrics.getAscent()- metrics.getDescent()) / 2);

		//g.drawString(text,x,y);

        g.drawImage( image.getImage(), (size.width)/11, (size.height)/11, null );


        // Ok, so now I need to draw a status bar at the bottom of the screen -
        String text = "User: "+strUserLoggedIn;
        String textNetwork = "Network selected: "+strNetworkSelected;
        String textModelPropFile = "Model properties file selected: "+strPropFile;
        
		int x = (size.width - w);
        font = font.deriveFont(Font.PLAIN,12);
        g.setFont(font);
        int xoffset = 10;
        int yoffset = 50;
        g.drawString(text,xoffset,(size.height)-yoffset);
        
        // Reset the yoffset

        Rectangle2D stringBounds = metrics.getStringBounds(text+textNetwork,g);

		int preferredWidth = (int)stringBounds.getWidth()+metrics.getHeight();
		int preferredHeight = (int)stringBounds.getHeight()+metrics.getHeight();

		w = Math.min(preferredWidth,w);
		h = Math.min(preferredHeight,h);

        yoffset = 30;
        g.drawString(textNetwork,xoffset,(size.height)-yoffset);

        yoffset = 10;
        g.drawString(textModelPropFile,xoffset,(size.height)-yoffset);

   }

   public void setUserName(String str)
   {
       this.strUserLoggedIn = str;
       repaint();
   }

   public void setNetworkName(String str)
   {
       this.strNetworkSelected = str;
       repaint();
   }

   public void setModelPropName(String str)
   {
       this.strPropFile = str;
       repaint();
   }

}
