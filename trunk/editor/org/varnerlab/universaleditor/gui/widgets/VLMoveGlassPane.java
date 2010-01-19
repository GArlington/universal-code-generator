package org.varnerlab.universaleditor.gui.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;

/**
 * A glasspane that can be used to notify the user (for a specified time)
 * what the x and y coordinates of their JFrame (window) are after they
 * have moved it.
 *
 * <p/>
 * Copyright (C) 2005 by Jon Lipsky
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. Y
 * ou may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software d
 * istributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class VLMoveGlassPane extends JPanel implements ComponentListener, ActionListener
{
	// ------------------------------------------------------------------------------------------------------------------
	//  Fields
	// ------------------------------------------------------------------------------------------------------------------

	private JInternalFrame frame;
	private boolean installed = false;
	private Component previousGlassPane;
	private Timer timer;
	private int delay = 3000;
	private Point lastLocation;
    

	// ------------------------------------------------------------------------------------------------------------------
	//  Constructors and Getter/Setters
	// ------------------------------------------------------------------------------------------------------------------

	public VLMoveGlassPane(JInternalFrame aFrame)
	{
		frame = aFrame;
		frame.addComponentListener(this);
		setOpaque(false);
	}

	public int getDelay()
	{
		return delay;
	}

	public void setDelay(int aDelay)
	{
		delay = aDelay;
	}

	// ------------------------------------------------------------------------------------------------------------------
	//  Implementation of the methods from ComponentListener
	// ------------------------------------------------------------------------------------------------------------------

	public void componentHidden(ComponentEvent e)
	{
		// Do nothing
	}

	public void componentMoved(ComponentEvent e)
	{
		lastLocation = frame.getLocation();
		repaint();

		if (!installed)
		{
			previousGlassPane = frame.getGlassPane();
			frame.setGlassPane(this);
			setVisible(true);
			installed = true;
		}

		if (timer == null)
		{
			timer = new Timer(delay, this);
		}
		else
		{
			timer.stop();
			timer.setDelay(delay);
		}

		timer.start();
	}

	public void componentResized(ComponentEvent e)
	{
		// Do nothing
	}

	public void componentShown(ComponentEvent e)
	{
		// Do nothing
	}

	// ------------------------------------------------------------------------------------------------------------------
	//  Implementation of the methods from ComponentListener
	// ------------------------------------------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		timer.stop();

		installed = false;
		setVisible(false);
		frame.setGlassPane(previousGlassPane);
		previousGlassPane = null;
	}

	// ------------------------------------------------------------------------------------------------------------------
	//  Override methods of JPanel
	// ------------------------------------------------------------------------------------------------------------------

	protected void paintComponent(Graphics g)
	{
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

		String text = lastLocation.x +","+lastLocation.y;

		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		Rectangle2D stringBounds = metrics.getStringBounds(text,g);

		int preferredWidth = (int)stringBounds.getWidth()+metrics.getHeight();
		int preferredHeight = (int)stringBounds.getHeight()+metrics.getHeight();

		w = Math.min(preferredWidth,w);
		h = Math.min(preferredHeight,h);

		int x = (size.width - w) / 2;
		int y = (size.height - h) / 2;

		Color vColor = new Color(0, 0, 0, 90);
		g.setColor(vColor);
		g.fillRoundRect(x, y, w, h, arc, arc);

		g.setColor(Color.WHITE);
		x = (size.width - (int)stringBounds.getWidth()) / 2;
		y = (size.height / 2) + ((metrics.getAscent()- metrics.getDescent()) / 2);

		g.drawString(text,x,y);
	}

	// ------------------------------------------------------------------------------------------------------------------
	//  Utility Methods
	// ------------------------------------------------------------------------------------------------------------------

	public static void registerFrame(JInternalFrame aFrame)
	{
		new VLMoveGlassPane(aFrame);
	}
}
