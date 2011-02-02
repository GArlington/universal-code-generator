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


public class Data {

	 /** Data id, a string that unequlley defines this data set */
    public String dataId = "";
    /** Species ID, name of species */
    public String speciesId;
    /** Free species or complexed */
    public boolean freeSpecies;
    /** Array of speciesIndexs to identify all of the species to be summed to
     *  obtain the observable.  Currentlly we are using the species index
     *  associated with the VLab Universal software.  
     *
     */
    public int[] speciesIndex = new int[0];
    /** Array of coefficents that multiply corisponding species in speciesIndex
     * to obtain the sum for the observable, the stochiometry.
     */
    public int[] speciesCoef = new int[0];
    /** Value for single measurment of the given observable */
    public double value;
    /** Time that corisponds to the value of the
     * given observable */
    public double time;
    /** Error in mesurment, this houses the error in the corisponding value */
    public double valueErr;
    /** This is the data group ID which is used to to determine other 
     *  data sets which are relative to this trajectory.  "NA" is used to 
     *  indicate the fact that these values are absolute, and not relative values.
     *  "self" or the blank set, "", is used to indicate that these values are 
     *  only relitive to eachother and will not be compared to any other datasets 
     */ 
    public String groupId = "";
    /** The citation for the data. */
    public String cite = "";
    
    /** Creates a new instance of Data 
     *  by defult assume this is only self relative.
     */
    public Data() {
    }
    /** Sets the data id, which is a string that unequlley defines this 
     *  data set.
     *  @param dataId       String, unique data set identifier
     */
    public void setDataId(String dataId){
        this.dataId = dataId;
    }
    /** Sets the actual name of the species
     *
     * @param speciesId     String, name of species
     */
    public void setSpeciesId(String speciesId){
        this.speciesId = speciesId;
    }
    /** Sets the free species marker
     *
     * @param free      Boolean, this will be true if we are only considering free species 
     */
    public void setFree(boolean free){
        this.freeSpecies = free;
    }
    /** Add a species to summation list for the observable.  An observable 
     *  can be a summation over diffrent species for cases when total protein
     *  X is required given that X has bound, free, modified, and/or unmodifed
     *  forms.
     *  @param speciesIndex        int, Currentlly we are using the species index associated with the VLab Universal software.
     *  @param speciesCoef      int, coefficents that multiply corisponding species in speciesIndex to obtain the sum for the observable, the stochiometry
     */
    public void addSpeciesIndex(int speciesIndex, int speciesCoef){
        this.speciesCoef = CUtil.addElement(this.speciesCoef,speciesCoef);
        this.speciesIndex = CUtil.addElement(this.speciesIndex,speciesIndex);
    }
    /** Add a species to summation list for the observable.  An observable 
     *  can be a summation over diffrent species for cases when total protein
     *  X is required given that X has bound, free, modified, and/or unmodifed
     *  forms.  This assumes the stoch is unity.
     *  @param speciesIndex        int, Currentlly we are using the species index associated with the VLab Universal software.
     */
    public void addSpeciesIndex(int speciesIndex){
        addSpeciesIndex(speciesIndex,1);
    }
    /** Add a value to the list of time course values for this data set.
     *  @param value        double, value of observable
     *  @param time         double, absolute model time at which value is observed
     *  @param valueErr     double, error in value (same units as value)
     */
    public void addValue(double time, double value, double valueErr){
        this.value = value;
        this.time = time;
        this.valueErr = valueErr;
    }
    /** Add a value to the list of time course values for this data set.
     *  Assumes error of 10%
     *  @param value        double, value of observable
     *  @param time         double, absolute model time at which value is observed
     */
    public void addValue( double time, double value){
        addValue(time, value, .1*value);
    }
    /** Set the group ID, which is used to to determine other 
     *  data sets which are relative to this trajectory.  "NA" is used to 
     *  indicate the fact that these values are absolute, and not relative values.
     *  "self" or the blank set, "", is used to indicate that these values are 
     *  only relitive to eachother and will not be compared to any other datasets
     *  @param groupId      String, a unique relative dataset identifier
     */
    public void setGroupId(String groupId){
        this.groupId = groupId;
    }
    /** Set the citation from which this data set came from 
     *  @param cite     Stirng, generic dscription of citation
     */
    public void setCite(String cite){
        //ATTENTION may want to add some structure here to make better use of this feild!!
        // maybe a citation object that has feild similar to that of latex???
        this.cite = cite;
    }
	
}
