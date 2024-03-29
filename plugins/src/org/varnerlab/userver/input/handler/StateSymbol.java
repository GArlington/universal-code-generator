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
 * StateSymbol.java
 *
 * Created on June 13, 2006, 5:26 PM
 *
 */

package org.varnerlab.userver.input.handler;

/**
 *
 * @author jeffreyvarner
 */
public class StateSymbol {
    // Class/instance attributes
    private String _strSymbol = "";
    private double _bdlCoeff = 0.0;
    private String _strLocation = "";
    
    
    /** Creates a new instance of StateSymbol */
    public StateSymbol() {
    }
    
    
    public void setLocation(String location)
    {
        _strLocation = location;
    }
    
    public String getLocation()
    {
        return(_strLocation);
    }
    
    
    public void setSymbol(String symbol)
    {
        this._strSymbol=symbol;
    }
    
    public void setCoefficient(double value)
    {
        this._bdlCoeff=value;
    }
    
    public String getSymbol()
    {
        return (this._strSymbol);
    }
    
    public double getCoefficient()
    {
        return(this._bdlCoeff);
    }
}
