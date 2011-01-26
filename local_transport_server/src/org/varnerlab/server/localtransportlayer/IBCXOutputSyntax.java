package org.varnerlab.server.localtransportlayer;

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
