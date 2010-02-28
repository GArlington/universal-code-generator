package org.varnerlab.userver.language.handler.bcx;

//import statements -
import java.util.Date;
import java.util.*;

import org.sbml.libsbml.*;
import org.varnerlab.server.transport.*;
import org.varnerlab.server.transport.*;
import org.varnerlab.userver.language.handler.*;


public class BCXGeneralBuilder {
	/** An array housing the info for a set of in silico experiments. */
	public Vector<Exp> expList = new Vector<Exp>();
	private int nSteps = 1000;
	private Date date;
	private BCXMatlabOctaveOutputSyntax outputSyntax;
	public String outputSyntaxString;
	private LoadXMLPropFile _xmlPropTree;

	/**
	 * Creates a new instance of BCXGenralBuilder
	 */
	public BCXGeneralBuilder(Vector expList, String outputSyntaxString,LoadXMLPropFile _xmlPropTree) {
		this._xmlPropTree = _xmlPropTree;
		this.expList = expList;
		Calendar pinUp = Calendar.getInstance();
		date = pinUp.getTime();
		this.outputSyntaxString = outputSyntaxString;

		 // Hard code the syntax - was giving an error before??
		 outputSyntax = new BCXMatlabOctaveOutputSyntax();
		 outputSyntax.setProperties(_xmlPropTree);
		
	}

	/** creates an array of string buffers each containing octave code 
	 *  for the simulation of a single experiment.  The buffers are then 
	 *  saved to file
	 */
	public void buildExpCode(boolean Sens) throws Exception{
		// make a temp string
		String temp = new String("");

		System.out.println("Supposed to be generating simulation code ... how many experiments have been loaded? "+expList.size());
		
		// we have to make one buffer for each experiment in the system
		for(int i=0;i<expList.size();i++){

			// track times
			double Tstart = 0;
			double Tstop = 0;
			double Tstep = 0;
			
			// create the buffer to hold the code for this experiment
			StringBuffer buffer = new StringBuffer();
			// get the next experiment form the list
			Exp exp = expList.get(i);

			// set up the step so that we have 1001 data points
			double Tfinal = exp.calcTfinal();
			if(Tfinal > 1){
				Tstep = Tfinal/nSteps;
			}
			else{
				Tstep = Tfinal/2;
			}

			// keep track of stimulus that have already been added
			int[] addedStim = new int[0];
			
			// title page anyone?
			// we need the header to be used in this file, 
			// that is output type specific so lets get it
			if(Sens){
				buffer = outputSyntax.getSensExpHeaderBuffer(buffer,exp.expId,date,exp.cite);
			}
			else{
				buffer = outputSyntax.getExpHeaderBuffer(buffer,exp.expId,date,exp.cite);
			}
		
			
			// check to see if any stimulus is added at the begening 
			for(int j=0;j<exp.stimulus.length;j++){
				//set up a temp stimulus
				Stimulus stimulus = exp.stimulus[j];
				if(stimulus.getTime()<1E-5&&stimulus.getTime()>-1E-5){
					// if we are here lets add this stuff
					// lets put this in the list of done stuff
					addedStim = CUtil.addElement(addedStim,j);
					// now lets add the output specific code for adding stimulus
					buffer = outputSyntax.getAddStimulusBuffer(buffer,String.valueOf(stimulus.paramIndex),String.valueOf(stimulus.value),stimulus.basis);
				}
			}
			
			// now that inital conditions are done lets check to see
			// if we need to run to steady state
			if(exp.ss){
				if(Sens){
					buffer = outputSyntax.getSensExpSteadyStateBuffer(buffer);
				}
				else{
					buffer = outputSyntax.getExpSteadyStateBuffer(buffer);
				}
			}

			// now in rotating order until all stimulus have been added
			while(addedStim.length<exp.stimulus.length){
				// find the time at which the next stimulus is to be added
				double nextTime = 1E20;
				for(int j=0;j<exp.stimulus.length;j++){
					if(exp.stimulus[j].getTime()<nextTime){
						// if we are here this is the next cannadate
						// make sure we have not yet added it
						boolean addedIt = false;
						for(int k=0;k<addedStim.length;k++){
							if(addedStim[k]==j){
								addedIt = true;
								break;
							}
						}
						if(!addedIt){nextTime = exp.stimulus[j].getTime();}                        
					}
				}
				// now we have the time at which the next stimulus is added
				// lets set up the Time stuff
				// The last stop, is now the start
				Tstart = Tstop;
				Tstop = nextTime;
				// lets run the simulation to this point
				double[] T = {Tstart, Tstop, Tstep};
				if(Sens){
					buffer = outputSyntax.getSensExpRunBuffer(buffer,T);
				}
				else{
					buffer = outputSyntax.getExpRunBuffer(buffer,T);
				}
				
				// now record the experimental results, which is code specific
				if(Sens){
					buffer = outputSyntax.getSensExpUpdateResultsBuffer(buffer);
				}
				else{
					buffer = outputSyntax.getExpUpdateResultsBuffer(buffer);
				}

				// now that we is here its time to add the next set of stimuli
				// lets start by looping through all the possible stimulus
				for(int j=0;j<exp.stimulus.length;j++){
					//set up a temp stimulus
					Stimulus stimulus = exp.stimulus[j];
					if(stimulus.getTime()<(nextTime+1E-5)&&stimulus.getTime()>(nextTime-1E-5)){
						// if we are here lets add this stuff
						// lets put this in the list of done stuff
						buffer.append("% Add the next stimulus\n");
						addedStim = CUtil.addElement(addedStim,j);
						// lets add it using the code specific output
						buffer = outputSyntax.getAddStimulusBuffer(buffer,String.valueOf(stimulus.paramIndex),String.valueOf(stimulus.value),stimulus.basis);
					}
				}
			}// end of stimulus loop
			// ok now that we are here we may want to run for some more time
			Tstart = Tstop;
			Tstop = Tfinal;
			// last run
			double[] T = {Tstart, Tstop, Tstep};
			if(Sens){
				buffer = outputSyntax.getSensExpRunBuffer(buffer,T);
			}
			else{
				buffer = outputSyntax.getExpRunBuffer(buffer,T);
			}

			// now record the experimental results
			if(Sens){
				buffer = outputSyntax.getSensExpUpdateResultsBuffer(buffer);
			}
			else{
				buffer = outputSyntax.getExpUpdateResultsBuffer(buffer);
			}
			// now place the footer, which is obviouslly code specific:

			buffer = outputSyntax.getExpFooterBuffer(buffer);

			// lets save this experiment 
			// We need to get the correct path -
			
			
			try{
				if(Sens)
				{
					outputSyntax.writeSensExpBuffer(buffer,exp.expId);
				}
				else
				{
					
					System.out.println("What is in the buffer..."+buffer.toString());
					outputSyntax.writeExpBuffer(buffer,exp.expId);
				}
			}
			catch(Exception e){
				Exception eio = new Exception("Error saveing Exp to file", e);
				throw eio;
			}

		} // end the exp loop

	}




	/** Creats a stringbuffer for Octave code that can be used
	 *  to graph the simulation results alongside of scaled
	 *  experimental data, and saves the buffer to file
	 */
	public void buildGrapherCode() throws Exception{
		// create the buffer to hold the code for this set of experiments
		StringBuffer buffer = new StringBuffer();
		int[] addedExp = new int[0];
		
		// lets do the header now --> code specific
		buffer = outputSyntax.getMSEGraphFntHeader(buffer,date);
		
		// Go through and run all of the experiments
		buffer = outputSyntax.getMSEFntRun(buffer,expList);
		
		// now all of the data is collected, and time for the "fun" stuff
		// we need to find a scalling factor for all the data that is relative
		// to do this lets go through all of the data and identify relative groups
		String[] didit = {"self","NA",""};
		for(int i=0;i<expList.size();++i){
			for(int q=0; q<expList.get(i).data.length;++q){

				String group = "";
				// first off we need to find the next group
				// go through remaining experiments
				boolean gotIt = false;
				for(int j=i; j<expList.size(); ++j){
					// go through all of the data in this exp
					for(int k=0;k<expList.get(j).data.length;++k){
						// look at the group 
						group = expList.get(j).data[k].groupId;
						// we have a canadae
						gotIt = true;
						// see if its been done
						for(int l=0; l<didit.length;++l){
							if(group.equalsIgnoreCase(didit[l])){
								gotIt = false;
								break;
							}
						}
						if(gotIt){break;}
					}
					if(gotIt){break;}
				}
				// we either went through the remaining list and found nothing new,
				// in which case we are done with groups:
				if(!gotIt){break;}

				// or we have a new group
				// let put it in the did it pile!
				didit = CUtil.addElement(didit,group);
				// lets set up octave to collect the right information
				buffer.append("\t% Collect all of the data for group "+group+"\n");
				buffer.append("\tdata=[\n");
				// go back through and collect all data relevant to this group
				for(int j=i; j<expList.size();++j){
					for(int k=0; k<expList.get(j).data.length; ++k){
						// lets grab the fianl time
						double Tfinal = expList.get(j).calcTfinal();
						// lets calc the step time
						double Tstep = 0;
						if(Tfinal>1){
							Tstep = Tfinal/nSteps;
						}
						else{
							Tstep = Tfinal/2;
						}
						if(expList.get(j).data[k].groupId.equalsIgnoreCase(group)){
							// ok this data point is from the current group
							// for simplicity
							Data data = expList.get(j).data[k];

							// put up the double tab
							buffer.append("\t\t");
							// lets calculate the time index of this point
							long Tindex = Math.round(data.time/Tstep) + 1;

							// make sure its not over
							if(Tindex>(nSteps+1)){
								Tindex = nSteps+1;
							}
							// fucking crist we have to do another loop
							for(int z=0; z<data.speciesIndex.length;++z){
								// if this is not the first time add a plus
								if(z!=0){
									buffer.append("+");
								}
								// now add up the stoch * value for each X in this observation!!
								buffer.append(data.speciesCoef[z]+"*X"+
										expList.get(j).expId+"("+Tindex+
										","+data.speciesIndex[z]+")");
							}
							// now all of that shit is added up
							buffer.append(","+data.value+","+data.valueErr+";\n");

						}
					}  
				}
				// end the varriable
				buffer.append("\t];\n");
				// ok we have the octave data varriable set up
				// now we can calculate the scalling
				buffer.append("\t% Now do the scaling for group "+group+"\n");
				buffer.append("\tscale = sum(data(:,1).*data(:,2)./(data(:,3).^2))/sum((data(:,1)./data(:,3)).^2);\n");
				// now that we have the scale lets throw all this in the totData
				buffer.append("\tdata(:,1) = scale*data(:,1);\n");
				buffer.append("\t% Place the new data in the total data for MSE\n");
				buffer.append("\ttotData = [totData; data];\n");

				// now we can do the ploting
				// lets setup the color varriable
				int color = 1;
				// set up the octave plot call
				buffer.append("\t% Now plot all of the stuff for group"+group+"\n");
				buffer.append("\tfigure, plot(");
				// ok plot all of the stuff relevant to this group
				// keep track of what first
				boolean firstFlag = true;
				for(int j=i; j<expList.size();++j){
					for(int k=0; k<expList.get(j).data.length; ++k){
						if(expList.get(j).data[k].groupId.equalsIgnoreCase(group)){
							// ok this data set is from the current group
							// for simplicity
							Data data = expList.get(j).data[k];
							// if we are first lets update the flag
							if(firstFlag){
								firstFlag = false;
							}
							else{
								// if we are here we need a camma
								buffer.append(",");
							}
							// let put the X trajectory in 
							// first the time and the scale factor
							buffer.append("T"+expList.get(j).expId+",");
							// now the species
							for(int z=0; z<data.speciesIndex.length;++z){
								// if this is not the first time add a plus
								if(z!=0){
									buffer.append("+");
								}
								// now add up the stoch * value for each X in this observation!!
								buffer.append("scale*"+data.speciesCoef[z]+"*X"+
										expList.get(j).expId+"(:,"+data.speciesIndex[z]+")");
							}
							// lets set the color to be used for this observation
							if(color > 6){
								color = 1;
							}
							// buffer in the color and the start of the data
							buffer.append(",'-"+color+"',[");
							// ok now the times

							// now all of that shit is added up
							buffer.append(data.time);

							buffer.append("],[");
							// ok now the data

							// now all of that shit is added up
							buffer.append(data.value);

							// ok now that is over
							// lets do color and data label
							buffer.append("],'*"+color+";"+data.dataId+";'");
							// incriment the color
							color += 1;
						}
					}  
				}
				// now end the plot call
				buffer.append(");\n");
			}
		}





		// let go through an look or self and NA




		for(int i=0;i<expList.size();++i){
			String group = "";
			// first off we need to find the next set
			// lets set up octave to collect the right information

			// go back through and collect all data relevant to this group

			for(int k=0; k<expList.get(i).data.length; ++k){
				// check to see what the group is
				group = expList.get(i).data[k].groupId;
				if(group.equalsIgnoreCase("self")||group.equals("")||group.equalsIgnoreCase("NA")){
					buffer.append("\t% Now collect the data related to "+expList.get(i).data[k].dataId+"\n");

					// ready the data stuff
					buffer.append("\tdata=[\n");
					// lets grab the fianl time
					double Tfinal = expList.get(i).calcTfinal();
					// lets calc the step time
					double Tstep = 0;
					if(Tfinal>1){
						Tstep = Tfinal/nSteps;
					}
					else{
						Tstep = Tfinal/2;
					}

					// for simplicity
					Data data = expList.get(i).data[k];
					// go throughh the whole data set

					// put up the double tab
					buffer.append("\t\t");
					// lets calculate the time index of this point
					long Tindex = Math.round(data.time/Tstep) + 1;
					// make sure its not over

					// fucking crist we have to do another loop
					for(int z=0; z<data.speciesIndex.length;++z){
						// if this is not the first time add a plus
						if(z!=0){
							buffer.append("+");
						}
						// now add up the stoch * value for each X in this observation!!
						buffer.append(data.speciesCoef[z]+"*X"+
								expList.get(i).expId+"("+Tindex+
								","+data.speciesIndex[z]+")");
					}
					// now all of that shit is added up
					buffer.append(","+data.value+","+data.valueErr+";\n");




					// end the varriable
					buffer.append("\t];\n");
					// ok we have the octave data varriable set up
					// now we can calculate the scalling
					if(group.equalsIgnoreCase("NA")){
						buffer.append("\t% Data related to "+expList.get(i).data[k].dataId+" has no scaleing\n");
						buffer.append("\tscale = 1;\n");
					}
					else{
						buffer.append("\t% find scaleing for the data related to "+expList.get(i).data[k].dataId+"\n");
						buffer.append("\tscale = sum(data(:,1).*data(:,2)./(data(:,3).^2))/sum((data(:,1)./data(:,3)).^2);\n");
					}
					// now that we have the scale lets throw all this in the totData
					buffer.append("\tdata(:,1) = scale*data(:,1);\n");
					buffer.append("\ttotData = [totData; data];\n");





					// now we can do the ploting
					// set up the octave plot call
					buffer.append("\t% Plot the data related to "+expList.get(i).data[k].dataId+"\n");
					buffer.append("\tfigure, plot(");

					// let put the X trajectory in 
					// first the time and the scale factor
					buffer.append("T"+expList.get(i).expId+",");
					// now the species
					for(int z=0; z<data.speciesIndex.length;++z){
						// if this is not the first time add a plus
						if(z!=0){
							buffer.append("+");
						}
						// now add up the stoch * value for each X in this observation!!
						buffer.append("scale*"+data.speciesCoef[z]+"*X"+
								expList.get(i).expId+"(:,"+data.speciesIndex[z]+")");
					}

					// buffer in the start of the data
					buffer.append(",[");
					// ok now the times


					// now all of that shit is added up
					buffer.append(data.time);

					buffer.append("],[");
					// ok now the data


					// now all of that shit is added up
					buffer.append(data.value);

					// ok now that is over
					buffer.append("],"+data.dataId+");\n");
				}
			}
		}

		// now calculate the MSE and end
		buffer.append("\t% Now we can get the MSE\n");
		buffer.append("\tMSE = mean(((totData(:,1) - totData(:,2))./totData(:,3)).^2);\n");
		buffer.append("return;\n");




		// now get the name of this file:
		//String workingDir = _xmlPropTree.getProperty("//Model/working_directory/text()");
		String fileName = _xmlPropTree.getProperty("//Model/grapher_filename/text()");
		//String filePath = new String(workingDir+"/"+fileName);
		// now that everything is plotted lets save it
		try{
			CUtil.write(fileName,buffer,_xmlPropTree);
		}
		catch(Exception e){
			Exception eio = new Exception("Error saveing Exp to file", e);
			throw eio;
		}




	}


	/** Creats a stringbuffer for Octave code that can be used
	 *  to take parameters and give there MSE, which is calculated via
	 *  experimental data and scaled simulation values.  Also a buffer is made 
	 *  for the function which calls this in an optimization loop.  All the buffers saved to file
	 */
	public void buildOptCode() throws Exception{
		// create the buffer to hold the code for this set of experiments
		StringBuffer buffer = new StringBuffer();
		int[] addedExp = new int[0];
		buffer.append("function [MSE] = optFnt(lnP,DF)\n");
		buffer.append("% --Machine Made via BioChemExp on "+date+"--\n");
		buffer.append("% This code was designed to calculate MSE\n");
		buffer.append("% given the ln of parameters for optimization\n");
		buffer.append("% ----------------------------------------\n");
		// parameters are always positive so opt will deal with the ln values
		buffer.append("\t% Get the POSITIVE simulation parameters\n");
		buffer.append("\tP = exp(lnP);\n");
		// put the current parameters in the DF
		buffer.append("\tDF.PARAMETER_VECTOR = P;\n");
		buffer.append("\tDF.INITIAL_CONDITIONS = P(DF.NUMBER_PARAMETERS+1:end);\n");
		// set up the total data variable to calculate the MSE
		buffer.append("\t% initialize the total data vector for MSE\n");
		buffer.append("\ttotData = [];\n");
		// Go through and run all of the experiments
		buffer.append("\t% Run the experiments\n");
		for(int i=0;i<expList.size();++i){
			String id = expList.get(i).expId;
			buffer.append("\t[T"+id+", X"+id+"] = exp_"+id+"(DF);\n");

		}
		// now all of the data is collected, and time for the "fun" stuff
		// we need to find a scalling factor for all the data that is relative
		// to do this lets go through all of the data and identify relative groups
		String[] didit = {"self","NA",""};
		for(int i=0;i<expList.size();++i){
			for(int q=0; q<expList.get(i).data.length;++q){


				String group = "";
				// first off we need to find the next group
				// go through remaining experiments
				boolean gotIt = false;
				for(int j=i; j<expList.size(); ++j){
					// go through all of the data in this exp
					for(int k=0;k<expList.get(j).data.length;++k){
						// look at the group 
						group = expList.get(j).data[k].groupId;
						// we have a canadae
						gotIt = true;
						// see if its been done
						for(int l=0; l<didit.length;++l){
							if(group.equalsIgnoreCase(didit[l])){
								gotIt = false;
								break;
							}
						}
						if(gotIt){break;}
					}
					if(gotIt){break;}
				}
				// we either went through the remaining list and found nothing new,
				// in which case we are done with groups:
				if(!gotIt){break;}

				// or we have a new group
				// let put it in the did it pile!
				didit = CUtil.addElement(didit,group);
				// lets set up octave to collect the right information
				buffer.append("\t% Collect Data for group "+group+"\n");
				buffer.append("\tdata=[\n");
				// go back through and collect all data relevant to this group
				for(int j=i; j<expList.size();++j){
					for(int k=0; k<expList.get(j).data.length; ++k){
						// lets grab the fianl time
						double Tfinal = expList.get(j).calcTfinal();
						// lets calc the step time
						double Tstep = 0;
						if(Tfinal>1){
							Tstep = Tfinal/nSteps;
						}
						else{
							Tstep = Tfinal/2;
						}
						if(expList.get(j).data[k].groupId.equalsIgnoreCase(group)){
							// ok this data set is from the current group
							// for simplicity
							Data data = expList.get(j).data[k];
							// go throughh the whole data set

							// put up the double tab
							buffer.append("\t\t");
							// lets calculate the time index of this point
							long Tindex = Math.round(data.time/Tstep) + 1;


							// fucking crist we have to do another loop
							for(int z=0; z<data.speciesIndex.length;++z){
								// if this is not the first time add a plus
								if(z!=0){
									buffer.append("+");
								}
								// now add up the stoch * value for each X in this observation!!
								buffer.append(data.speciesCoef[z]+"*X"+
										expList.get(j).expId+"("+Tindex+
										","+data.speciesIndex[z]+")");
							}
							// now all of that shit is added up
							buffer.append(","+data.value+","+data.valueErr+";\n");

						}
					}  
				}
				// end the varriable
				buffer.append("\t];\n");
				// ok we have the octave data varriable set up
				// now we can calculate the scalling
				buffer.append("\t% Calculate the scaling for group "+group+"\n");
				buffer.append("\tscale = sum(data(:,1).*data(:,2)./(data(:,3).^2))/sum((data(:,1)./data(:,3)).^2);\n");
				// now that we have the scale lets throw all this in the totData
				buffer.append("\tdata(:,1) = scale*data(:,1);\n");
				buffer.append("\ttotData = [totData; data];\n");


			}
		}





		// let go through and look for self and NA




		for(int i=0;i<expList.size();++i){
			String group = "";
			// first off we need to find the next set
			// lets set up octave to collect the right information

			// go back through and collect all data relevant to this group

			for(int k=0; k<expList.get(i).data.length; ++k){
				// check to see what the group is
				group = expList.get(i).data[k].groupId;
				if(group.equalsIgnoreCase("self")||group.equals("")||group.equalsIgnoreCase("NA")){

					// ready the data stuff
					buffer.append("\t% Collect Data relevant to "+expList.get(i).data[k].dataId+"\n");
					buffer.append("\tdata=[\n");
					// lets grab the fianl time
					double Tfinal = expList.get(i).calcTfinal();
					// lets calc the step time
					double Tstep = 0;
					if(Tfinal>1){
						Tstep = Tfinal/nSteps;
					}
					else{
						Tstep = Tfinal/2;
					}

					// for simplicity
					Data data = expList.get(i).data[k];


					// put up the double tab
					buffer.append("\t\t");
					// lets calculate the time index of this point
					long Tindex = Math.round(data.time/Tstep) + 1;
					// make sure its not over

					// fucking crist we have to do another loop
					for(int z=0; z<data.speciesIndex.length;++z){
						// if this is not the first time add a plus
						if(z!=0){
							buffer.append("+");
						}
						// now add up the stoch * value for each X in this observation!!
						buffer.append(data.speciesCoef[z]+"*X"+
								expList.get(i).expId+"("+Tindex+
								","+data.speciesIndex[z]+")");
					}
					// now all of that shit is added up
					buffer.append(","+data.value+","+data.valueErr+";\n");




					// end the varriable
					buffer.append("\t];\n");
					// ok we have the octave data varriable set up
					// now we can calculate the scalling
					if(group.equalsIgnoreCase("NA")){
						buffer.append("\t% no scaling relevant to "+expList.get(i).data[k].dataId+"\n");
						buffer.append("\tscale = 1;\n");
					}
					else{
						buffer.append("\t% Calculate scaling relevant to "+expList.get(i).data[k].dataId+"\n");
						buffer.append("\tscale = sum(data(:,1).*data(:,2)./(data(:,3).^2))/sum((data(:,1)./data(:,3)).^2);\n");
					}
					// now that we have the scale lets throw all this in the totData
					buffer.append("\tdata(:,1) = scale*data(:,1);\n");
					buffer.append("\ttotData = [totData; data];\n");


				}
			}
		}

		// now calculate the MSE and end
		buffer.append("\t% Calculate MSE\n");
		buffer.append("\tMSE = mean(((totData(:,1) - totData(:,2))./totData(:,3)).^2);\n");
		buffer.append("return;\n");

		//String workingDir = _xmlPropTree.getProperty("//Model/working_directory/text()");
		String fileName = _xmlPropTree.getProperty("//Model/optimization_function_filename/text()");
		//String filePath = new String(workingDir+"/"+fileName);

		// now that everything is done lets save it
		try{
			CUtil.write(fileName,buffer,_xmlPropTree);

		}
		catch(Exception e){
			Exception eio = new Exception("Error saveing Exp to file", e);
			throw eio;
		}


	}  
}


