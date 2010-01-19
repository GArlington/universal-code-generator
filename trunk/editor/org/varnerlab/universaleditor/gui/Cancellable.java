/*
 * @(#)Cancellable.java
 *
 * $Date: 2009-02-20 01:34:41 -0600 (Fri, 20 Feb 2009) $
 *
 * Copyright (c) 2009 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * http://javagraphics.blogspot.com/
 * 
 * And the latest version should be available here:
 * https://javagraphics.dev.java.net/
 */

package org.varnerlab.universaleditor.gui;

/** An interface for operations that can be cancelled. */
public interface Cancellable {
  public void cancel();
  public boolean isCancelled();
}
