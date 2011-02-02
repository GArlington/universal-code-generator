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

public class Exp {

	 /** Experiental id, String used to uneqully define this experiment */
    public String expId;
    /** Citation for this experiment */
    public String cite;
    /** Array of data sets that corisponds to this experiment */
    public Data[] data = new Data[0];
    /** Array of stimuli that corisponds to thie experiment */
    public Stimulus[] stimulus = new Stimulus[0];
    /** This is f'ing sloppy but its the easyest way I can think of to do this,
     *  the question is, do we need to start at steady state, I have quite a 
     *  few experiments that require me to add IC and run to SS then do stuff
     *  so that is what this is for.
     *  defualt is no.
     */
    public boolean ss = false;
    /** The defult length of time the experiment is expected to run for
     */
    public double time = 0;
    
    /** Creates a new instance of Exp */
    public Exp() {
    }
    /** set the experimental id
     *  @param expId        String, used to uniquely define this experiment
     */
    public void setExpId(String expId){
        this.expId = expId;
    }
    /** adds a data set to this experiment
     *  @param data     Data, data set corisponding to this experiment
     */
    public void addData(Data data){
        this.data = CUtil.addElement(this.data, data);
    }
    /** adds a stimulus to this experiment 
     *  @param stimulus     Stimulus, stimulus corisponding to this experiment
     */
    public void addStimulus(Stimulus stimulus){
        this.stimulus = CUtil.addElement(this.stimulus, stimulus);
    }
    /** add a new time point at which the simulation is set to steady state
     *  @param ss     double, time of steady state
     */
    public void setSs(boolean ss){
        this.ss = ss;
    }
    /** sets the experiment time
     *  @param time     double, defult lenght of time the expirimnet is supposed to run for.
     */
    public void setTime(double time){
        this.time = time;
    }

     /** sets the experiment citation
     *  @param cite     String,  citation for this experiment
     */
    public void setCite(String cite){
        this.cite = cite;
    }

    /** gets the final time for this entier experiment
     *  @return Tfinal
     */
    public double calcTfinal(){
        // get the total run time of this experiment
        double Tfinal = this.time;
        // lets be sure that this is enough time to complete the experiment
        for(int j=0;j<this.stimulus.length;++j){
            // select the longest time
            if(Tfinal<this.stimulus[j].getTime()){
                Tfinal = this.stimulus[j].getTime();
            }
        }


        // lets also check to be sure that all data can be collected in this time
        for(int j=0;j<this.data.length;++j){

            // select the longest time
            if(Tfinal<this.data[j].time){
                Tfinal = this.data[j].time;
            }
            
        }
        return(Tfinal);
    }
}
