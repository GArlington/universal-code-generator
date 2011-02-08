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
 * WriteVarnerFlatFile.java
 *
 * Created on December 6, 2007, 10:10 AM
 */

package org.varnerlab.userver.output.handler;

// import statements -
import java.util.Hashtable;
import java.util.logging.Logger;

import org.sbml.libsbml.*;


import org.varnerlab.server.localtransportlayer.*;
import org.varnerlab.userver.language.handler.GIOL;


/**
 *
 * @author jeffreyvarner
 */
public class WriteVarnerFlatFile implements IOutputHandler {
    // Class/instance attributes -
    private Hashtable _propTable = null;
    private StringBuffer _buffer = null;
    private XMLPropTree _xmlPropTree = null;
    private Logger _logger = null;
    
    /** Creates a new instance of WriteVarnerFlatFile */
    public WriteVarnerFlatFile()
    {
        // do nothing constructor -
    }
    
    public void writeResource(Object object) throws Exception {
        // Get the resource type -
        Model model_wrapper = (Model)object;
        
        // Load the lib -
        System.loadLibrary("sbmlj");
        
        // Create a new string buffer and populate it with model details -
        _buffer = new StringBuffer();
        populateStringBuffer(model_wrapper);
        
        // Get the path to where I'm going to dump the VFF file -
        Hashtable<String,String> pathTable = _xmlPropTree.buildFilenameBlockDictionary("OutputFile");
        String strPath = pathTable.get("FULLY_QUALIFIED_PATH");
        
        // Dump the buffer to disk -
        GIOL.write(strPath,_buffer);
    }
    
    public void setHashtable(Hashtable prop) {
        _propTable = prop;
    }
    
    private void populateStringBuffer(Model model_wrapper) throws Exception
    {
        // Method attributes -
        ListOfReactions list_reactions = model_wrapper.getListOfReactions();
        
        // Now that we have the reaction list, I need to populate the buffer in the Varner lab flat file format, 
        // then I can transform that later ---
        long NUMBER_OF_REACTIONS = list_reactions.size();
        for (long reaction_index=0;reaction_index<NUMBER_OF_REACTIONS;reaction_index++)
        {
            // Get the Reaction object -
            Reaction reaction = (Reaction)list_reactions.get(reaction_index);
            
            // put the name in the buffer -
            String strReactionName = "R_"+reaction_index;
            _buffer.append(strReactionName);
            _buffer.append(",");
            
            // Get the list of reactants -
            ListOfSpeciesReferences list_reactants = reaction.getListOfReactants();
            long NUMBER_OF_REACTANTS = list_reactants.size();
            
            if (NUMBER_OF_REACTANTS==0)
            {
                throw new Exception("Missing reactants - "+strReactionName);
            }
            
            for (long reactant_index=0;reactant_index<NUMBER_OF_REACTANTS;reactant_index++)
            {
                // Get the species reference and pull out information -
                SpeciesReference species = (SpeciesReference)list_reactants.get(reactant_index);
                
                // Get the stoichiometry and the species symbol -
                double dblStoichiometry = (species.getStoichiometry());
                String strSymbol = species.getSpecies();
                
         
                
                // populate the buffer -
                if (reactant_index<(NUMBER_OF_REACTANTS-1))
                {
                    // Ok, I'm not on the last reactant so I need to terminate this string with a '+'
                    if (dblStoichiometry!=1.0)
                    {
                        _buffer.append(dblStoichiometry);
                        _buffer.append("*");
                    }
                    
                    if (strSymbol.isEmpty())
                    {
                    	_buffer.append("[]");
                    	_buffer.append("+");
                    }
                    else
                    {
                    	_buffer.append(strSymbol);
                    	_buffer.append("+");
                    }
                }
                else
                {
                    if (dblStoichiometry!=1.0)
                    {
                        _buffer.append(dblStoichiometry);
                        _buffer.append("*");
                    }
                    
                    if (strSymbol.isEmpty())
                    {
                    	_buffer.append("[]");
                    	_buffer.append(",");
                    }
                    else
                    {
                    	_buffer.append(strSymbol);
                    	_buffer.append(",");
                    }
                }
            }
            
            // Get the list of products -
            ListOfSpeciesReferences list_products = reaction.getListOfProducts();
            long NUMBER_OF_PRODCUTS = list_products.size();
            
            if (NUMBER_OF_PRODCUTS==0)
            {
                throw new Exception("Missing products - "+strReactionName);
            }
            
            for (long product_index=0;product_index<NUMBER_OF_PRODCUTS;product_index++)
            {
                // Get the species reference and pull out information -
                SpeciesReference product = (SpeciesReference)list_products.get(product_index);
                
                // Get the stoichiometry and the species symbol -
                double dblStoichiometryProduct = (product.getStoichiometry());
                String strSymbolProduct = product.getSpecies();
                
                // populate the buffer -
                if (product_index<(NUMBER_OF_PRODCUTS-1))
                {
                    // Ok, I'm not on the last reactant so I need to terminate this string with a '+'
                    if (dblStoichiometryProduct!=1.0)
                    {
                        _buffer.append(dblStoichiometryProduct);
                        _buffer.append("*");
                    }
                    
                    if (strSymbolProduct.isEmpty())
                    {
                    	_buffer.append("[]");
                    	_buffer.append("+");
                    }
                    else
                    {
                    	_buffer.append(strSymbolProduct);
                    	_buffer.append("+");
                    }
                    
                    //_buffer.append(strSymbolProduct);
                    //_buffer.append("+");
                }
                else
                {
                    if (dblStoichiometryProduct!=1.0)
                    {
                        _buffer.append(dblStoichiometryProduct);
                        _buffer.append("*");
                    }
                    
                    if (strSymbolProduct.isEmpty())
                    {
                    	_buffer.append("[]");
                    	_buffer.append(",");
                    }
                    else
                    {
                    	_buffer.append(strSymbolProduct);
                    	_buffer.append(",");
                    }
                    
                    //_buffer.append(strSymbolProduct);
                    //_buffer.append(",");
                }
            }
            
            // Ok we need to check for reversible -
            if (reaction.getReversible())
            {
                _buffer.append("-inf,inf;\n");
            }
            else
            {
                _buffer.append("0,inf;\n");
            }
            
            
        }
    }

    public void setProperties(Hashtable prop) {
        this._propTable = prop;
    }

	public void setProperties(XMLPropTree prop) {
		_xmlPropTree = prop;
	}

	public void setLogger(Logger log) {
		_logger = log;
	}
}
