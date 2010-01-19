/*
 * SimzillaDomainComposite.java
 *
 * Created on July 15, 2006, 9:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.domain;

// import statements -
import java.util.Vector;

/**
 *
 * @author jeffreyvarner
 */
public class SimzillaDomainComposite extends SimzillaDomainComponent {
    // class/instance
    Vector children;
    
    /** Creates a new instance of SimzillaDomainComposite */
    public SimzillaDomainComposite() {
    }
    
     /**
     * Initailizes the DomainObject
     */
    public void init(){
        // Create new child container
        children=new Vector();
    }
    
    /**
     *  Adds children to this composite
     *  @param ADomainComponent Child
     */
    public void addChild(ADomainComponent child){
        children.addElement(child);
    }
}
