package org.varnerlab.universaleditor.gui.widgets;

// Import statements 
import javax.swing.JSeparator;
import java.awt.Dimension;

public class VLMFCToolBarSeparator extends JSeparator implements IVLWidget
   {
      Dimension m_size = new Dimension(5,16);

      public VLMFCToolBarSeparator()
      {
         this(null);
      }
      
      public VLMFCToolBarSeparator(Dimension size)
      {
         super(JSeparator.VERTICAL);
         setSeparatorSize(size);
      }

      public void setSeparatorSize( Dimension size )
      {
         if (size != null)
         {
            m_size = size;
         }
         else
         {
            super.updateUI();
         }
         
         this.invalidate();
      }

      public Dimension getPreferredSize()
      {
         return m_size;
      }

      public Dimension getMinimumSize()
      {
         return getPreferredSize();
      }

      public Dimension getMaximumSize()
      {
         return getPreferredSize();
      }
      
      public void setWidgetIcon(String _iName) throws Exception {
      }
      
      /**
       * Adds a child reference to a parent
       */
      public void addChild(IVLWidget _widget) throws Exception {
      }
      
      public void setWidgetText(String _text) throws Exception {
      }
      
      /**
       * Sets the parent reference of a child
       */
      public void setParent(IVLWidget _widget) throws Exception {
      }
      
      public IVLWidget endSetup() {
        super.setVisible(true);
        return(this);
      }
      
      /**
       * Adds a child reference to a parent
       */
      public void addChild(Object _widget) throws Exception {
      }

    public void setWidgetIconOn(String _iName) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWidgetIconOff(String _iName) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
      
   }
