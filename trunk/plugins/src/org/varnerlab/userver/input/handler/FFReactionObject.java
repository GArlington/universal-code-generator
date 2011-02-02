

/*
 * Copyright (c) 2011 Varnerlab, 
 * School of Chemical and Biomolecular Engineering, Cornell
 * University, Ithaca NY 14853 USA.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * FFReactionObject.java
 *
 * Created on June 13, 2006, 5:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.varnerlab.userver.input.handler;

// import statements
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Iterator;

/**
 *
 * @author jeffreyvarner
 */
public class FFReactionObject extends Object implements IReactionFile {
    // Class attributes
    private Vector _vecReactants;
    private Vector _vecProducts;
    private String _strReverse = "";
    private String _strForward = "";
    private String _strFormatedRxnString = "";
    private Record _objRecord = null;
    
    
    /**
     * Creates a new instance of FFReactionObject
     */
    public FFReactionObject() {
        this.init();
    }
    
    private void init()
    {
        // Create new instances of the reactants and products  
        _vecReactants = new Vector();
        _vecProducts = new Vector();
    }
    
    public Record getRecord()
    {
        return(_objRecord);
    }
    
    public void doExecute(Record record)
    {
        try {
            // Before we do anything, keep this record -
            _objRecord = record;
            
            this.doExecute((String)record.getData(record.REACTANTS),(String)record.getData(record.PRODUCTS));
            
            // Grad the forward and reverse strings from the record -
            this._strForward=(String)record.getData(record.FORWARD);
            this._strReverse=(String)record.getData(record.REVERSE);
            
            // Formulate the formatted reaction string -
            this._strFormatedRxnString=(String)record.getData(record.REACTANTS)+" = "+(String)record.getData(record.PRODUCTS);

        }
        catch (Exception error)
        {
            // eat the exception
        }
    }
    
    public void doExecute(String rxnStringReactants,String rxnStringProducts)
    {
        try {
            // Ok, so I have the reaction strings, cut them apart
            parseString(rxnStringReactants,_vecReactants,false);
            parseString(rxnStringProducts,_vecProducts,true);
        }
        catch (Exception error)
        {
            // eat the error for the moment -
        }
    }
    
    public double getCoefficientValue(String symbol)
    {
        // Method attributes
        double coeff = 0.0;
        
        // Ok, so here is the hard part -
        
        // Go through the reactants -
        Iterator iterReactants = this.getReactants();
        while (iterReactants.hasNext())
        {
            // Get the state symbol 
            StateSymbol tmp = (StateSymbol)iterReactants.next();
            String tmpString = tmp.getSymbol();
            
            if (tmpString.equalsIgnoreCase(symbol))
            {
                // If I get here then I have a match -
                coeff = tmp.getCoefficient();
            }
        }
        
        // Go through the products -
        Iterator iterProducts = this.getProducts();
        while (iterProducts.hasNext())
        {
            // Get the state symbol 
            StateSymbol tmp = (StateSymbol)iterProducts.next();
            String tmpString = tmp.getSymbol();
            
            if (tmpString.equalsIgnoreCase(symbol))
            {
                // If I get here then I have a match -
                coeff = tmp.getCoefficient();
            }
        }
        
        // return coeff to the caller
        return(coeff);
    }
    
    public String getBoundsString()
    {
        String strTmp = "";
        
        strTmp = this._strReverse+"\t"+this._strForward;
        
        return(strTmp);
    }
    
    public String getReactionString()
    {
        return(this._strFormatedRxnString);
    }
    
    // Get methods for the reactants and products vectors
    public Iterator getReactants()
    {
        return(_vecReactants.iterator());
    }
    
    public Iterator getProducts()
    {
        return(_vecProducts.iterator());
    }
    
    private void parseString(String frag,Vector vector,boolean isProduct) throws Exception
    {
        // Ok, this method contains the logic to cut up the reaction strings -
        
        // Cut around the +'s'
        StringTokenizer tokenizer=new StringTokenizer(frag,"+",false);
        while (tokenizer.hasMoreElements())
        {
            // Get a data from the tokenizer -
            Object dataChunk=tokenizer.nextToken();
            
            // Create new symbol wrapper
            StateSymbol symbol = new StateSymbol();
            
            // Check to see if this dataChunk string contains a *
            if (((String)dataChunk).contains("*"))
            {
                // If I get here, then the string contains a stoichiometric coefficient
                
                // Cut around the *'s
                StringTokenizer tokenizerCoeff=new StringTokenizer((String)dataChunk,"*",false);
                int intCoeffCounter = 1;
                while (tokenizerCoeff.hasMoreElements())
                {
                    Object dataCoeff=tokenizerCoeff.nextToken();
                    
                    if (intCoeffCounter==1)
                    {
                        if (isProduct){
                            symbol.setCoefficient(Double.parseDouble(((String)dataCoeff)));
                        }
                        else
                        {
                            double dblTmp = Double.parseDouble(((String)dataCoeff));
                            symbol.setCoefficient(-1.0*dblTmp);
                        }
    
                        // Update the counter
                        intCoeffCounter++;
                    }
                    else if (intCoeffCounter==2)
                    {
                        symbol.setSymbol((String)dataCoeff);
                    }
                }
            }
            else
            {
                // If I get here, then no coefficient
                if (isProduct)
                {
                    // If this metabolite is in a product string, then coeff is positive
                    symbol.setSymbol((String)dataChunk);
                    symbol.setCoefficient(1.0);
                }
                else
                {
                    // If this metabolite is in a reactant string, then coeff is negative
                    symbol.setSymbol((String)dataChunk);
                    symbol.setCoefficient(-1.0);
                }
                
            }
            
            // Add to symbol wrapper to the vector -
            vector.addElement(symbol);
        }
        
    }
    
    
}
