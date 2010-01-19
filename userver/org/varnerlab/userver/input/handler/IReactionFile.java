/*
 * IReactionFile.java
 *
 * Created on June 13, 2006, 4:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.input.handler;

/**
 *
 * @author jeffreyvarner
 */
public interface IReactionFile {
    
    // Interface that holds the type of data that I'm going to grab from the reaction file '
    public static final String RXNNAME = "REACTION_NAME";
    public static final String REACTANTS = "REACTANTS";
    public static final String PRODUCTS = "PRODUCTS";
    public static final String REVERSE = "REVERSE";
    public static final String FORWARD = "FORWARD";
    public static final String TYPE = "RTYPE";
}
