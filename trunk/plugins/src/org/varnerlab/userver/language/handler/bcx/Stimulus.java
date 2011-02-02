package org.varnerlab.userver.language.handler.bcx;

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


public class Stimulus {

	/** an id that uniquely defines this stimulus */
	public String stimId = "";
	/** The name of the species being altered since we are currently only considering alterations in species.*/
	public String species = "";
	/** The paramIndex of the stimulus.  Currentlly we are using the parameter index
	 *  associated with the VLab Universal software and are only considering changes in species levels.
	 */
	public int paramIndex = 0;
	/** The value for this stimulus */
	public double value = 0;
	/** The basis for the concentration change:
	 *  0) relative change;
	 *  1) absolute change;
	 *  2) absolute value;
	 */
	public int basis = 0;
	/** The time at which this stumulus takes place */
	private double time = 0;
	/**
	 * Creates a new instance of Stimulus, 
	 *  by defult we assume that the change is reletive to 
	 *  the current value, and we assume that the stimulus takes place
	 *  as a change in inital condition.
	 */
	public Stimulus() {

	}
	/** Sets the id for this stimulus.
	 *  @param stimId       String, unique id for this stimulus
	 */
	public void setStimId(String stimId){
		this.stimId = stimId;
	}
	/** sets the param id for the corisponding stimulus.  Currentlly we are using the parameter index
	 *  associated with the VLab Universal software.  This should eventually be
	 *  changed to a geniric string id form and a translator should be used 
	 *  to interprate this data for use with specific modeling software.
	 *  @param paramIndex      int, refering to parameter index
	 */
	public void setParamIndex(int paramIndex){
		this.paramIndex = paramIndex;
	}
	/** set the value of the change for this stimulus
	 *  @param value        double, value of stimulus
	 */
	public void setValue(double value){
		this.value = value;
	}
	/** sets the basis for the change as determined by an intiger.
	 *  excepts ABSOLUTE or RELATIVE
	 *  @param basis        String, basis of change.
	 */
	public void setBasis(String basis){
		if(basis.equals("ABSOLUTE")){
			this.basis = 2;
		}
		if(basis.equals("RELATIVE")){
			this.basis = 0;
		}

	}
	/** Sets time at which the stimulus occurs
	 *  @param time         double, time of stimulus
	 */
	public void setTime(double time){
		this.time = time;
	}
	
	public double getTime()
	{
		return(time);
	}
}
