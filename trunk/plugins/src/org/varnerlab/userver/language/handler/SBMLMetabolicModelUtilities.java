package org.varnerlab.userver.language.handler;

/*
 * Copyright (c) 2011 Varnerlab, 
 * School of Chemical and Biomolecular Engineering, 
 * Cornell University, Ithaca NY 14853 USA.
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
 */

// import statements -
import java.util.Properties;
import java.util.Vector;

import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;

public class SBMLMetabolicModelUtilities {

	public static void dumpBoundsFileToDisk(Properties _propTable,StringBuffer buffer) throws Exception {
        // Ok, dump this to disk -
        
		// Create the path string -
		String strPath = _propTable.getProperty("PATH_NETWORK_DIRECTORY")+"/"+_propTable.getProperty("OUTPUT_BOUNDS_FILENAME");
		GIOL.write(strPath,buffer);
    }
	
	
	public static void dumpDataFileToDisk(StringBuffer buffer,Properties propTable) throws Exception
	{
		// Method attributes -
		String strPath = "";
		
		// Get the path -
		strPath = propTable.getProperty("PATH_SRC_DIRECTORY")+"/"+propTable.getProperty("OUTPUT_DATAFILE_FILENAME");
        
		// Dump buffer to disk -
		GIOL.write(strPath,buffer);
	}
	
	public static void dumpDriverFileToDisk(StringBuffer buffer,Properties propTable) throws Exception
	{
		// Method attributes -
		String strPath = "";
		
		// Get the path -
		strPath = propTable.getProperty("PATH_SRC_DIRECTORY")+"/"+propTable.getProperty("DRIVER_FILENAME");
		
		System.out.println("Going to dump driver - "+strPath);
        
		// Dump buffer to disk -
		GIOL.write(strPath,buffer);
	}
	
	public static void dumpStoichiometricMatrixToDisk(Properties _propTable,int NR,int NC,double[][] dblSTMatrix) throws Exception
    {
        // Method attributes -
        String strPath = "";
        StringBuffer buffer = new StringBuffer();
        
        for (int scounter=0;scounter<NR;scounter++)
        {
            for (int rcounter=0;rcounter<NC;rcounter++)
            {
                buffer.append(dblSTMatrix[scounter][rcounter]);
                buffer.append("\t");
            }
            
            buffer.append("\n");
        }
              
        // Get the path and dump -2- disk 
        // Create the path string -
		strPath = _propTable.getProperty("PATH_NETWORK_DIRECTORY")+"/"+_propTable.getProperty("OUTPUT_STM_FILENAME");
        GIOL.write(strPath,buffer);
    }
	
	
	public static void organizeSpeciesByCompartment(Properties _propTable,Model model_wrapper,Vector<Species> vecSpecies) throws Exception
	{
		// Get species -
		ListOfSpecies list_species = model_wrapper.getListOfSpecies();
		long NUMBER_OF_SPECIES = model_wrapper.getNumSpecies();
		
		// Get the list of compartments -
		ListOfCompartments compartments = model_wrapper.getListOfCompartments();
		long NUMBER_OF_COMPARTMENTS = model_wrapper.getNumCompartments();
		
		System.out.println("How many compartments dowe have? "+NUMBER_OF_COMPARTMENTS);

		for (long index=0;index<NUMBER_OF_COMPARTMENTS;index++)
		{
			// Get the current compartment -
			Compartment current_compartment = compartments.get(index);
			String strCompartmentID = current_compartment.getId();
			
			System.out.println("Processing "+strCompartmentID);
			
			// Collect the species in this compartment -
			for (long species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
			{
				// Get the species -
				Species tmp = list_species.get(species_index);
				
				// Check the compartment -
				String strCompartment = tmp.getCompartment();
				if (strCompartment.equalsIgnoreCase(strCompartmentID))
				{
					vecSpecies.addElement(tmp);
				}
			}
		}
	}
	
	
	// Util method to dump species list to disk -
	public static void dumpSpeciesToDisk(Properties _propTable,Model model_wrapper) throws Exception
	{
		// Method attributes =
		StringBuffer buffer = new StringBuffer();
		Vector<Species> vecSpecies = new Vector<Species>();
		
		// Get species -
		ListOfSpecies list_species = model_wrapper.getListOfSpecies();
		long NUMBER_OF_SPECIES = model_wrapper.getNumSpecies();
		
		// Get the list of compartments -
		ListOfCompartments compartments = model_wrapper.getListOfCompartments();
		long NUMBER_OF_COMPARTMENTS = model_wrapper.getNumCompartments();

		int outer_counter = 1;
		for (long index=0;index<NUMBER_OF_COMPARTMENTS;index++)
		{
			// Get the current compartment -
			Compartment current_compartment = compartments.get(index);
			String strCompartmentID = current_compartment.getId();
			
			// Collect the species in this compartment -
			for (long species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
			{
				// Get the species -
				Species tmp = list_species.get(species_index);
				
				// Check the compartment -
				String strCompartment = tmp.getCompartment();
				if (strCompartment.equalsIgnoreCase(strCompartmentID))
				{
					vecSpecies.addElement(tmp);
				}
			}
			
			// Ok, when I get here I have collected all the species in the current compartment - write them to the buffer
			int NUM_SPECIES_IN_COMPARTMENT = vecSpecies.size();
			int inner_counter = 1;
			for (int buffer_index=0;buffer_index<NUM_SPECIES_IN_COMPARTMENT;buffer_index++)
			{
				// Get the species -
				Species tmp = vecSpecies.get(buffer_index);
				buffer.append(tmp.getId());
				buffer.append("\t");
				buffer.append(inner_counter);
				buffer.append("\t");
				buffer.append(outer_counter);
				buffer.append("\t");
				buffer.append(strCompartmentID);
				buffer.append("\n");
				
				// update the counter -
				inner_counter++;
				
				// Update the outer_counter -
				outer_counter++;
			}
			
			// Ok, clear out the species vec and go around again -
			vecSpecies.removeAllElements();
		}
		
		// Get the path and dump -2- disk 
		// Create the path string -
		String strPath = _propTable.getProperty("PATH_NETWORK_DIRECTORY")+"/"+_propTable.getProperty("OUTPUT_SPECIES_FILENAME");
        GIOL.write(strPath,buffer);
	}
		
	
	// Build the stoichiometric matrix -
    public static void buildStoichiometricMatrix(double[][] dblSTMatrix,Model model_wrapper) throws Exception
    {
        
        // Get the dimension of the system -
        int NUMBER_OF_SPECIES = 0; 
        int NUMBER_OF_RATES = 0;
        Vector<Species> vecSpecies = new Vector<Species>();
        
        // Get the system dimension -
        NUMBER_OF_SPECIES = (int)model_wrapper.getNumSpecies(); 
        NUMBER_OF_RATES = (int)model_wrapper.getNumReactions(); 
        
        // Go through and put everything as zeros by default -
        for (int scounter=0;scounter<NUMBER_OF_SPECIES;scounter++)
        {
            for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
            {
                dblSTMatrix[scounter][rcounter]=0.0;
            }
        }
         
        // When I get here, I have a st. matrix w/all zeros - 
        // put in the correct values - 
        ListOfReactions listRates = model_wrapper.getListOfReactions();
        ListOfSpecies listSpecies = model_wrapper.getListOfSpecies();
       
        // Get the list of compartments -
		ListOfCompartments compartments = model_wrapper.getListOfCompartments();
		long NUMBER_OF_COMPARTMENTS = model_wrapper.getNumCompartments();
		for (long compartment_index=0;compartment_index<NUMBER_OF_COMPARTMENTS;compartment_index++)
		{
			// Get the current compartment -
			Compartment current_compartment = compartments.get(compartment_index);
			String strCompartmentID = current_compartment.getId();
			
			// Collect the species in this compartment -
			for (long species_index=0;species_index<NUMBER_OF_SPECIES;species_index++)
			{
				// Get the species -
				Species tmp = listSpecies.get(species_index);
				
				// Check the compartment -
				String strCompartment = tmp.getCompartment();
				if (strCompartment.equalsIgnoreCase(strCompartmentID))
				{
					vecSpecies.addElement(tmp);
				}
			}
		}
			
		// Go through the species list and build st matrix for elements in this compartment -
		int NUM_SPECIES_COMPARTMENT = vecSpecies.size();		
		for (int scounter=0;scounter<NUM_SPECIES_COMPARTMENT;scounter++)
        {
            // Get the species reference -
            Species species = (Species)vecSpecies.get(scounter);
            String strSpecies = species.getId();
   
            
            System.out.println("\t Processing "+strSpecies+" counter="+scounter+" of "+NUMBER_OF_SPECIES);
            	
        	// Ok, I need to go through the rates and determine if this species is involved -
            for (int rcounter=0;rcounter<NUMBER_OF_RATES;rcounter++)
            {          		
            	// Get the Reaction object -
                Reaction rxn_obj = (Reaction)listRates.get(rcounter);
                
                // Get the 'radius' of this rate -
                int NUMBER_OF_REACTANTS = (int)rxn_obj.getNumReactants();
                int NUMBER_OF_PRODUCTS = (int)rxn_obj.getNumProducts();
                
                // Get the list of reactants and products -
                ListOf reactant_list = rxn_obj.getListOfReactants();
                ListOf product_list = rxn_obj.getListOfProducts();
                
                // go through the reactants of this reaction -
                for (int reactant_index=0;reactant_index<NUMBER_OF_REACTANTS;reactant_index++)
                {
                    // Get the species reference -
                    SpeciesReference species_ref = (SpeciesReference)reactant_list.get(reactant_index);
                    String strReactant = species_ref.getSpecies();
                    
                    if (strReactant.equalsIgnoreCase(strSpecies))
                    {
                       
                        double tmp = species_ref.getStoichiometry();
                        if (tmp>=0.0)
                        {
                            dblSTMatrix[scounter][rcounter]=-1.0*tmp;
                        }
                        else
                        {
                            dblSTMatrix[scounter][rcounter]=tmp;
                        }
                    }
                    
                }
                
                // go through the products of this reaction -
                for (int product_index=0;product_index<NUMBER_OF_PRODUCTS;product_index++)
                {
                    // Get the species reference -
                    SpeciesReference species_ref = (SpeciesReference)product_list.get(product_index);
                    String strProduct = species_ref.getSpecies();
                    
                    //System.out.println("Comparing NP="+NUMBER_OF_PRODUCTS+" to "+strProduct+"="+strSpecies+"?");
                    
                    if (strProduct.equalsIgnoreCase(strSpecies))
                    {
                    	dblSTMatrix[scounter][rcounter]=species_ref.getStoichiometry();
                    }
                }
            }
        }
    }
	
	// check for reversible rates -
    public static void convertReversibleRates(Model model_wrapper) throws Exception
    {
        // We need to treat the reversible reactions -
        int NUMBER_OF_RATES_INITIAL = (int)model_wrapper.getNumReactions();
        ListOf rate_list_initial = model_wrapper.getListOfReactions();
        for (int rate_counter=0;rate_counter<NUMBER_OF_RATES_INITIAL;rate_counter++)
        {
            // Get Reaction =
            Reaction rxn_local = (Reaction)rate_list_initial.get(rate_counter);
            
            
            if (rxn_local.getReversible())
            {
                // If the rate is reversible then I need to split -
           
                // Create a new reaction object -
                Reaction rxn_new = model_wrapper.createReaction();
                
                // Add the reactants to the products list and vice-versa -
                int NPRODUCTS_LOCAL = (int)rxn_local.getNumProducts();
                int NREACTANTS_LOCAL = (int)rxn_local.getNumReactants();
                ListOf reactants_list_local = rxn_local.getListOfReactants();
                ListOf products_list_local = rxn_local.getListOfProducts();
                
                for (int index =0;index<NPRODUCTS_LOCAL;index++)
                {
                    rxn_new.addReactant((SpeciesReference)products_list_local.get(index));
                }
                
                for (int index =0;index<NREACTANTS_LOCAL;index++)
                {
                    rxn_new.addProduct((SpeciesReference)reactants_list_local.get(index));
                }
               
                
                // Ok, I need to add the reaction to the model_wrapper -
                model_wrapper.addReaction(rxn_new);
            }
        }
    }
}
