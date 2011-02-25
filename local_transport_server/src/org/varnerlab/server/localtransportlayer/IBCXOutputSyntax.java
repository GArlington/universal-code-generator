package org.varnerlab.server.localtransportlayer;

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


import java.util.*;

public interface IBCXOutputSyntax {

	public StringBuffer getExpHeaderBuffer(StringBuffer buffer, String expId, Date date, String cite);
    public StringBuffer getSensExpHeaderBuffer(StringBuffer buffer, String expId, Date date, String cite);
    public StringBuffer getAddStimulusBuffer(StringBuffer buffer,String paramId, String value, int basis);
    public StringBuffer getExpSteadyStateBuffer(StringBuffer buffer);
    public StringBuffer getSensExpSteadyStateBuffer(StringBuffer buffer);
    public StringBuffer getExpRunBuffer(StringBuffer buffer, double[] T);
    public StringBuffer getSensExpRunBuffer(StringBuffer buffer, double[] T);
    public StringBuffer getExpUpdateResultsBuffer(StringBuffer buffer);
    public StringBuffer getSensExpUpdateResultsBuffer(StringBuffer buffer);
    public StringBuffer getExpFooterBuffer(StringBuffer buffer);
    public StringBuffer getSensExpFooterBuffer(StringBuffer buffer);
    public void writeExpBuffer(StringBuffer buffer, String expId) throws Exception;
    public void writeSensExpBuffer(StringBuffer buffer, String expId) throws Exception;
    public StringBuffer getMSEGraphFntHeader(StringBuffer buffer,Date date);
    public StringBuffer getMSEFntRun(StringBuffer buffer,Object[] expList);
    public StringBuffer getMSEFntRun(StringBuffer buffer,Vector expList);
}
