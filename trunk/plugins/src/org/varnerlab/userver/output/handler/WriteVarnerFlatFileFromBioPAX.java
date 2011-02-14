package org.varnerlab.userver.output.handler;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.physicalInteraction;

import org.varnerlab.server.localtransportlayer.IOutputHandler;
import org.varnerlab.server.localtransportlayer.XMLPropTree;

public class WriteVarnerFlatFileFromBioPAX implements IOutputHandler {
	// Class/instance attributes -
	private Logger _logger = null;
	private XMLPropTree _xmlPropTree = null;
	private Model _bioPAXModel = null;
	
	
	@Override
	public void setLogger(Logger log) {
		
		// Grab the logger -
		_logger = log;
		
	}

	@Override
	public void setProperties(Hashtable prop) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProperties(XMLPropTree prop) {
		
		// Grab the tree -
		_xmlPropTree = prop;
		
	}
	
	// private helper methods to proces different reaction types -
	private void processBiochemicalReactions(StringBuffer buffer,biochemicalReaction rxn) throws Exception
	{
		// Method attributes -
		
		// Ok, so when I get here I have a biochemical reaction. Process the left and right
		
	}

	@Override
	public void writeResource(Object object) throws Exception {
		// Method attribute -
		StringBuffer buffer = new StringBuffer();
		
		// Ok, so we need to check to make sure we have a BioPAX model coming in (not an SBML) -
		// Check to make sure we have an instance of a biopax model
		if (object instanceof Model)
		{
			// Ok, if I get here then I have a BioPAX model -
			
			// Grab the instance -
			_bioPAXModel = (Model)object;
			
			// Ok, so we need to generate a VFF from the biopax tree -
			Set<BioPAXElement> elements = _bioPAXModel.getObjects();
			
			// Process this set -
			Iterator<BioPAXElement> iter = elements.iterator();
			while (iter.hasNext())
			{
				// Ok, when I get here, I need to get an interaction -
				BioPAXElement tmpElement = iter.next();
				
				// Check to see if we have a level2 interaction -- this seems to be the situation for most files on the net?
				if (tmpElement instanceof interaction)
				{
					interaction tmpInteraction = (interaction)tmpElement;
				
					if (tmpInteraction instanceof physicalInteraction)
					{
						// add the name to the buffer -
						buffer.append(tmpInteraction.getNAME());
						buffer.append(",");
						
						// Ok, get the participants for this interaction -
						Set<InteractionParticipant> setOfPlayers = (tmpInteraction.getPARTICIPANTS());
						Iterator<InteractionParticipant> iterSetOfPlayers = setOfPlayers.iterator();
						while (iterSetOfPlayers.hasNext())
						{
						
							// Ok, see what we have -
							InteractionParticipant tmpPlayer = iterSetOfPlayers.next();
							if (tmpPlayer instanceof  physicalEntityParticipant)
							{
								// Get the physical entity -
								physicalEntity physicalPlayer = ((physicalEntityParticipant)tmpPlayer).getPHYSICAL_ENTITY();
								
								if (physicalPlayer!=null)
								{
									if (physicalPlayer.getSHORT_NAME()!=null)
									{
										buffer.append(physicalPlayer.getSHORT_NAME());
									}
									else
									{
										buffer.append(physicalPlayer.getNAME());
									}
								}
							
								buffer.append("+");
							}
						}
						
						buffer.append(",MONKEY,-inf,inf;\n");
					}
				}	
			}	
		
			System.out.println("RXN - "+buffer.toString());
			
		}
	}
}
