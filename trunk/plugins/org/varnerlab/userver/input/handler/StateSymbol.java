/*
 * StateSymbol.java
 *
 * Created on June 13, 2006, 5:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
