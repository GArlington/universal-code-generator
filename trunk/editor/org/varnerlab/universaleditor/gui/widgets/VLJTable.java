/*
 * MW4ServerSeeker, a small utility written in Java to publish and find MW4
 * Servers. Copyright (C) 2006 MekTek.
 * 
 * This software is the confidential intellectual property of the MekTek; it is
 * copyrighted and licensed, not sold. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.varnerlab.universaleditor.gui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.varnerlab.universaleditor.domain.UEditorSession;
import org.varnerlab.universaleditor.gui.Launcher;
import org.varnerlab.universaleditor.service.IVLSystemwideEventListener;
import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 * @author Daniel Lopez (GreenEyed)
 * 
 * Based on samples, tids and bits found on the web
 * Specially this one from elliotth's blog: "Alternating row colors in JTable"
 * http://elliotth.blogspot.com/2004/09/alternating-row-colors-in-jtable.html
 */
public class VLJTable extends JTable implements IVLSystemwideEventListener
{
  private Color AlternateColor = new Color(237, 243, 254);
  private Color MarkedColor = UIManager.getColor("Table.selectionBackground");
  private int columnCount;
  private int toolTipColumn;
  private UEditorSession _session = null;
  private Vector<String> _vecSpeciesNames = new Vector();
  private IVLTableCellEditor _cellVLEditor = null;

  /**
   * 
   */
  private static final long serialVersionUID = -2159218024266183736L;

  public VLJTable()
  {
    super();

    // Grab an instance of session -
    _session = (Launcher.getInstance()).getSession();
  }

  public VLJTable(TableModel theTM)
  {
    super(theTM);
    columnCount = theTM.getColumnCount();
    toolTipColumn = columnCount;

    // Grab an instance of session -
    _session = (Launcher.getInstance()).getSession();
  }

  public void setVLTableCellEditor(IVLTableCellEditor editor)
  {
    _cellVLEditor = editor;
  }

 


  /**
   * Paints empty rows too, after letting the UI delegate do its painting.
   */
  public void paint(Graphics g)
  {
    super.paint(g);
    paintEmptyRows(g);
  }

  /**
   * Paints the backgrounds of the implied empty rows when the table model is
   * insufficient to fill all the visible area available to us. We don't involve
   * cell renderers, because we have no data.
   */
  protected void paintEmptyRows(Graphics g)
  {
    final int rowCount = getRowCount();
    final Rectangle clip = g.getClipBounds();
    double height = clip.y + clip.height;
    if (rowCount * rowHeight < height)
    {
      for (int i = rowCount; i <= height / rowHeight; ++i)
      {
        g.setColor(colorForRow(i));
        g.fillRect(clip.x, i * rowHeight, clip.width, rowHeight);
      }
    }
  }

  /**
   * Changes the behavior of a table in a JScrollPane to be more like the
   * behavior of JList, which expands to fill the available space. JTable
   * normally restricts its size to just what's needed by its model.
   */
  public boolean getScrollableTracksViewportHeight()
  {
    if (getParent() instanceof JViewport)
    {
      JViewport parent = (JViewport) getParent();
      return (parent.getHeight() > getPreferredSize().height);
    }
    return false;
  }


  // This returns the cell editor -
  public TableCellEditor getCellEditor(int row,int col)
  {

      return(_cellVLEditor.TableCellEditor(row, col, _vecSpeciesNames, this));
     
  }

  public DefaultCellEditor getNominalTableCellEdititor(int row,int col)
  {
      return (DefaultCellEditor) super.getCellEditor(row,col);
  }


  /**
   * Returns the appropriate background color for the given row.
   */
  protected Color colorForRow(int row)
  {
    return (row % 2 == 0) ? AlternateColor : getBackground();
  }

  /**
   * Shades alternate rows in different colors.
   */
  public Component prepareRenderer(TableCellRenderer renderer, int row,
      int column)
  {
    Component c = super.prepareRenderer(renderer, row, column);
    JComponent jc = (JComponent) c;
    if (!c.getBackground().equals(MarkedColor))
    {
      if (isCellSelected(row, column) == false)
      {
        c.setBackground(colorForRow(row));
        c.setForeground(UIManager.getColor("Table.foreground"));
      }
      else
      {
        if (column == 0)
        {
          jc.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
        }
        else if (column == columnCount - 1)
        {
          jc.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
        }
        else
        {
          jc.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.GRAY));
        }
        c.setBackground(UIManager.getColor("Table.selectionBackground"));
        c.setForeground(UIManager.getColor("Table.selectionForeground"));
      }
    }
    return c;
  }

  /**
   * @return Returns the alternateColor.
   */
  public Color getAlternateColor()
  {
    return AlternateColor;
  }

  /**
   * @param alternateColor
   *          The alternateColor to set.
   */
  public void setAlternateColor(Color alternateColor)
  {
    AlternateColor = alternateColor;
  }

  /**
   * @return Returns the markedColor.
   */
  public Color getMarkedColor()
  {
    return MarkedColor;
  }

  /**
   * @param markedColor
   *          The markedColor to set.
   */
  public void setMarkedColor(Color markedColor)
  {
    MarkedColor = markedColor;
  }

  public String getToolTipText(MouseEvent e)
  {
    String toolTip = null;
    java.awt.Point p = e.getPoint();
    int rowIndex = rowAtPoint(p);
    // int colIndex = columnAtPoint(p);
    // int realColumnIndex = convertColumnIndexToModel(colIndex);
    TableModel model = getModel();
    if (rowIndex >= 0 && rowIndex <= model.getRowCount())
    {
      toolTip = (String) model.getValueAt(rowIndex, toolTipColumn);
    }
    return toolTip;
  }

  /**
   * @return Returns the toolTipColumn.
   */
  protected int getToolTipColumn()
  {
    return toolTipColumn;
  }

  /**
   * @param toolTipColumn
   *          The toolTipColumn to set.
   */
  protected void setToolTipColumn(int toolTipColumn)
  {
    this.toolTipColumn = toolTipColumn;
  }

    public void updateComponent() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateSession() {
        // Get instance of session -
        _session = (Launcher.getInstance()).getSession();

        // Get the vector of species names from the session -
        Vector<String> tmpVec = (Vector)_session.getProperty("SPECIES_NAME_VECTOR");
        if (tmpVec!=null)
        {
            int SIZE = tmpVec.size();
            for (int index=0;index<SIZE;index++)
            {
                _vecSpeciesNames.addElement(tmpVec.get(index));
            }
        }
    }

    public void updateNetwork() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
