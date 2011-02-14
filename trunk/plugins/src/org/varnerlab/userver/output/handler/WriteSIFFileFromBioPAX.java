package org.varnerlab.userver.output.handler;

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
 */

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.varnerlab.server.localtransportlayer.IOutputHandler;
import org.varnerlab.server.localtransportlayer.XMLPropTree;
import org.varnerlab.userver.language.handler.GIOL;

public class WriteSIFFileFromBioPAX implements IOutputHandler {
	// Class/instance attributes =
	private Logger _logger = null;
	private XMLPropTree _xmlPropTree = null;
	private Model _bioPAXModel = null;
	
	
	@Override
	public void setLogger(Logger log) {
		
		// grab the logger -
		_logger = log;
		
	}

	@Override
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProperties(XMLPropTree prop) {
		
		// Grab the prop tree -
		_xmlPropTree = prop;
		
	}

	@Override
	public void writeResource(Object object) throws Exception {
		// Method attributes -
		StringBuffer buffer = new StringBuffer();
		
		// Check to make sure we have an instance of a biopax model
		if (object instanceof Model)
		{
			// Ok, if I get here then I have a BioPAX model -
			
			// Grab the instance -
			_bioPAXModel = (Model)object;
			
			// We need to dump this to an SIF file.
			// Get the file name -
			Hashtable<String,String> pathTable = _xmlPropTree.buildFilenameBlockDictionary("OutputFile");
			String strPath = pathTable.get("FULLY_QUALIFIED_PATH");
			
			// Get rules for level 3 -
			List<InteractionRule> lvl3Rules = SimpleInteractionConverter.getRules(BioPAXLevel.L2);
			
			// Ok, create a converter -
			int NUMBER_OF_RULES = lvl3Rules.size();
			for (int rule_index=0;rule_index<NUMBER_OF_RULES;rule_index++)
			{
				// What is the current rule?
				InteractionRule currentRule = lvl3Rules.get(rule_index);
				
				// Get a converter for rule at rule_index 
				SimpleInteractionConverter converter = new SimpleInteractionConverter(currentRule);
				
				// Ok, get the interactions for this role -
				Set<SimpleInteraction> tmpRuleSet = converter.inferInteractions(_bioPAXModel);
				
				 
				// For now, let's just dump this to a string?
				Iterator<SimpleInteraction> iter = tmpRuleSet.iterator();
				while (iter.hasNext())
				{
					// Get the next interaction
					SimpleInteraction interaction = iter.next();
					
					// Get the strings -
					String strSource = interaction.getANameForSIF(interaction.getSource());
					String strTarget = interaction.getANameForSIF(interaction.getTarget());
					String strDescription = interaction.getType().getTag();
					
					// Put them in a buffer -
					buffer.append(strSource);
					buffer.append(" ");
					buffer.append(strDescription);
					buffer.append(" ");
					buffer.append(strTarget);
					buffer.append("\n");
				}
				
			}
			
			// Dump the SIF to disk -
			GIOL.write(strPath, buffer);
			
		}
		else
		{
			throw new Exception("SIF export from source types other than BioPAX models is not currently supported.");
		}
		
	}
	
	// private helper method to get SIF sets

}
