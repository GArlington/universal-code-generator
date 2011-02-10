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


// import statements 
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class GraphvizModel {
	// Class/instance members -
	private Model model_wrapper = null;
	private XMLPropTree _xmlPropTree = null;

	public GraphvizModel()
	{
		// load the sbml?
		System.loadLibrary("sbmlj");    
	}

	public void setModel(Model model)
	{
		model_wrapper = model;
	}

	public void setProperties(XMLPropTree prop) {
		this._xmlPropTree = prop;
	}

	public void buildDotFileHeader(StringBuffer buffer,Model model_wrapper,Vector<Reaction> vecReactions) throws Exception
	{
		// Method attributes -
		double dblValue = 0.0;

		// First line -
		buffer.append("digraph G {\n");
		buffer.append("\t");
		buffer.append("size=\"");

		// Get the graph size -
		String strGraphSize = _xmlPropTree.getProperty("//graph_size/text()");

		buffer.append(strGraphSize);
		buffer.append(",");
		buffer.append(strGraphSize);
		buffer.append("\"");
		buffer.append(";\n");
		buffer.append("\tratio=fill;\n");
		buffer.append("\tnode[");
		buffer.append("color=");

		// Get the terminal node color -
		String strTerminalNodeColor = _xmlPropTree.getProperty("//TerminalNodeProperties/terminal_node_color/text()");

		buffer.append(strTerminalNodeColor);
		buffer.append(",");
		buffer.append("fontcolor=");

		// Get the terminal node font color -
		String strTerminalNodeFontColor = _xmlPropTree.getProperty("//TerminalNodeProperties/terminal_node_font_color/text()");

		buffer.append(strTerminalNodeFontColor);
		buffer.append(",");
		buffer.append("style=filled,");
		buffer.append("shape=");

		// Get the terminal node shape -
		String strTerminalNodeShape = _xmlPropTree.getProperty("//TerminalNodeProperties/terminal_node_shape/text()");

		buffer.append(strTerminalNodeShape);
		buffer.append("]\n");

		/*
        // Get the dimension of the system -
        int NROWS = (int)model_wrapper.getNumSpecies();
        int NCOLS = (int)vecReactions.size();

        // Create a local copy of the stoichiometric matrix -
        double[][] matrix = new double[NROWS][NCOLS];
        SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions);
		 */        
	}

	public void buildGraphvizReactionList(StringBuffer buffer,Model model_wrapper,Vector<Reaction> vecReactions) throws Exception
	{
		// Get the edge properties -
		String strEdgeStyle = _xmlPropTree.getProperty("//EdgeProperties/edge_style/text()");
		String strEdgeColor = _xmlPropTree.getProperty("//EdgeProperties/edge_color/text()");

		// Get the reactions -
		int NUMBER_OF_REACTIONS = vecReactions.size();
		for (int index=0;index<NUMBER_OF_REACTIONS;index++)
		{
			// Get reaction object -
			Reaction rxn = vecReactions.get(index);

			// Get the list of reactions -
			ListOf reactantsList = rxn.getListOfReactants();
			int NUMBER_OF_REACTANTS = (int)reactantsList.size();
			for (int reactant_index=0;reactant_index<NUMBER_OF_REACTANTS;reactant_index++)
			{
				// Get the species reactants -
				SpeciesReference speciesReactant = (SpeciesReference)reactantsList.get(reactant_index);

				// Check to see if this is the []
				String strTestReactant = speciesReactant.getSpecies();

				if (!strTestReactant.equalsIgnoreCase("[]"))
				{

					// Get the productsList -
					ListOf productsList = rxn.getListOfProducts();
					int NUMBER_OF_PRODUCTS = (int)productsList.size();
					if (NUMBER_OF_PRODUCTS>0)
					{
						for (int product_index=0;product_index<NUMBER_OF_PRODUCTS;product_index++)
						{
							SpeciesReference speciesProduct = (SpeciesReference)productsList.get(product_index);
							String strTestProduct = speciesProduct.getSpecies();
							if (!strTestProduct.equalsIgnoreCase("[]"))
							{
								buffer.append(speciesReactant.getSpecies());
								buffer.append("->");
								buffer.append(speciesProduct.getSpecies());
								buffer.append(" [style=");
								buffer.append(strEdgeStyle);
								buffer.append(",color=");
								buffer.append(strEdgeColor);
								buffer.append("];\n");
							}
						}
					}
					else
					{
						buffer.append(speciesReactant.getSpecies());
						buffer.append("->");
						buffer.append("NULL");
						buffer.append(" [style=");
						buffer.append(strEdgeStyle);
						buffer.append(",color=");
						buffer.append(strEdgeColor);
						buffer.append("];\n");
					}
				}
			}
		}
	}

	public void buildGraphizNodeList(StringBuffer buffer,Model model_wrapper,Vector<Reaction> vecReactions,Vector<Species> vecSpecies) throws Exception
	{
		// Default color
		double dblValue = 0.0;

		// Get the dimension of the system -
		int NROWS = (int)vecSpecies.size();
		int NCOLS = (int)vecReactions.size();

		// Create a local copy of the stoichiometric matrix -
		double[][] matrix = new double[NROWS][NCOLS];
		SBMLModelUtilities.buildStoichiometricMatrix(matrix, model_wrapper,vecReactions,vecSpecies);

		// Get the list of species -
		ListOf listOfSpecies = (ListOf)model_wrapper.getListOfSpecies();
		int NUMBER_OF_SPECIES = (int)listOfSpecies.size();
		for (int index=0;index<NUMBER_OF_SPECIES;index++)
		{
			// Get the reference -
			Species speciesObj = (Species)listOfSpecies.get(index);
			String strSpecies = speciesObj.getId();

			buffer.append("\t");
			buffer.append(strSpecies);
			buffer.append(" [style=");

			// Get the graph size -
			String strNodeStyle = _xmlPropTree.getProperty("//NodeProperties/node_style/text()");     
			buffer.append(strNodeStyle);
			buffer.append(",");

			String strNodeFontSize = _xmlPropTree.getProperty("//NodeProperties/node_font_size/text()");
			buffer.append("fontsize=");
			buffer.append(strNodeFontSize);
			buffer.append(",");

			String strNodeShape = _xmlPropTree.getProperty("//NodeProperties/node_shape/text()");
			buffer.append("shape=");
			buffer.append(strNodeShape);
			buffer.append(",");

			// Ok, we need to compute the color -
			dblValue = countConnections(matrix,index,NCOLS);
			buffer.append("color=\"");

			String strGradDirection = _xmlPropTree.getProperty("//GradientProperties/gradient_direction/text()");
			if (strGradDirection.equalsIgnoreCase("DECREASING"))
			{
				dblValue = 1.0 - dblValue;
			}

			// Get the default gradient color -
			String strGradRed = _xmlPropTree.getProperty("//GradientProperties/gradient_default_color/red/text()");
			String strGradGreen = _xmlPropTree.getProperty("//GradientProperties/gradient_default_color/green/text()");
			String strGradBlue = _xmlPropTree.getProperty("//GradientProperties/gradient_default_color/blue/text()");

			// Figure out what color to change -
			String gradFlag = _xmlPropTree.getProperty("//GradientProperties/gradient_color/text()");
			if (gradFlag.equalsIgnoreCase("RED"))
			{
				buffer.append(dblValue);
				buffer.append(" ");
				buffer.append(strGradGreen);
				buffer.append(" ");
				buffer.append(strGradBlue);
			}
			else if (gradFlag.equalsIgnoreCase("BLUE"))
			{

				buffer.append(strGradRed);
				buffer.append(" ");
				buffer.append(strGradGreen);
				buffer.append(" ");
				buffer.append(dblValue);

			}
			else if (gradFlag.equalsIgnoreCase("GREEN"))
			{
				buffer.append(strGradRed);
				buffer.append(" ");
				buffer.append(dblValue);
				buffer.append(" ");
				buffer.append(strGradBlue);
			}

			buffer.append("\",");

			String strNodeTextColor = _xmlPropTree.getProperty("//NodeProperties/node_text_color/text()");
			buffer.append("fontcolor=");
			buffer.append(strNodeTextColor);
			buffer.append("];\n");
		}
	}

	private double countConnections(double[][] stArr,int row_index,int NUMBER_OF_COLS) throws Exception
	{
		double dblValue = 0.0;

		// Ok,I need to compute the fraction of connections that this node has -

		// count the number of nonzero elements 
		for (int col_index=0;col_index<NUMBER_OF_COLS;col_index++)
		{
			if (stArr[row_index][col_index]!=0)
			{
				dblValue = dblValue+1.0;
			}
		}

		dblValue = dblValue/NUMBER_OF_COLS;

		// return the double value -
		return(dblValue);
	}
}
