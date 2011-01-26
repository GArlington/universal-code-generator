package org.varnerlab.userver.language.handler.bcx;

import java.util.Date;
import java.util.Vector;

import org.varnerlab.server.localtransportlayer.*;


public class BCXMatlabOctaveOutputSyntax implements IBCXOutputSyntax {
    
	private XMLPropTree _xmlPropTree = null;
	
	public void setProperties(XMLPropTree prop) {
        this._xmlPropTree = prop;
	}
    
    public BCXMatlabOctaveOutputSyntax(){
    }
    
      
    public StringBuffer getExpHeaderBuffer(StringBuffer buffer, String expId, Date date, String cite){
        buffer.append("function [Tsim, Xsim] = exp_"+expId+"(DF)\n");
        buffer.append("% --Machine Made via BCX on "+date.toString()+"--\n");
        buffer.append("% This code was designed to mimic experiment "+expId+".\n");
        buffer.append("% Ref: "+cite+"\n");
        buffer.append("% -----------------------------------------\n");
        buffer.append("% Initailize vars\n");
        buffer.append("\tnP = DF.NUMBER_PARAMETERS;\n");
        buffer.append("\tnX = DF.NUMBER_OF_STATES;\n");
        buffer.append("\tXsim = zeros(1,nX);\n");
        buffer.append("\tTsim = [0];\n");
        buffer.append("% Check on Inital Conditions\n");
        return(buffer);
    }
    
    public StringBuffer getSensExpHeaderBuffer(StringBuffer buffer, String expId, Date date, String cite){
        buffer.append("function [Tsim, Xsim, Ssim] = expSens_"+expId+"(DF)\n");
        buffer.append("% --Machine Made via BioChemExp on "+date.toString()+"--\n");
        buffer.append("% This code was designed to mimic experiment "+expId+".\n");
        buffer.append("% Ref: "+cite+"\n");
        buffer.append("% There is an error here:\n");
        buffer.append("% SolveAdjBal.m assumes S IC to be zero\n");
        buffer.append("% This is not true when we call it multiple times in a row.\n");
        buffer.append("% Need to change that program to take last points from previous run.\n");
        buffer.append("% -----------------------------------------\n");
        buffer.append("% Initailize vars\n");
        buffer.append("\tnP = DF.NUMBER_PARAMETERS;\n");
        buffer.append("\tnX = DF.NUMBER_OF_STATES;\n");
        buffer.append("\tXsim = [];\n");
        buffer.append("\tTsim = [];\n");
        buffer.append("\tSsim = [];\n");
        buffer.append("% Check on Inital Conditions\n");
        return(buffer);
    }
    
    public StringBuffer getAddStimulusBuffer(StringBuffer buffer,String paramId, String value, int basis){
        buffer.append("\tDF.PARAMETER_VECTOR("+paramId+")=");
        // check to see what kind of change we need to issue
        if(basis==0){
            // a relative change, and we assume the given value is a fraction
            buffer.append("DF.PARAMETER_VECTOR("+paramId+")");
            buffer.append("*"+value+";\n");
        }
        else if(basis==1){
            // an absolute change
            buffer.append("DF.PARAMETER_VECTOR("+paramId+")");
            buffer.append("+"+value+";\n");
        }
        else if(basis==2){
            // an absolute value
            buffer.append(value+";\n");
        }
        return(buffer);
    }
    
    public StringBuffer getExpSteadyStateBuffer(StringBuffer buffer){
        buffer.append("% Run to steady state\n");
        buffer.append("\tSSdelta = 1;\n");
        buffer.append("\twhile(SSdelta>.001)\n");
        double[] T = {0, 1000, 1000};
        buffer = this.getExpRunBuffer(buffer,T);
        T = new double[]{0,1,1};
        buffer = this.getExpRunBuffer(buffer,T);
        buffer.append("\tSStot = sum(X');\n");
        buffer.append("\tSSdelta = (SStot(1)-SStot(2))/SStot(2);\n");
        buffer.append("\tend\n");
        return(buffer);
        
    }
    
     public StringBuffer getSensExpSteadyStateBuffer(StringBuffer buffer){
        buffer.append("% Run to steady state\n");
        buffer.append("\tSSdelta = 1;\n");
        buffer.append("\twhile(SSdelta>.001)\n");
        double[] T = {0, 1000, 1000};
        buffer = this.getExpRunBuffer(buffer,T);
        T = new double[]{0,1,1};
        buffer = this.getExpRunBuffer(buffer,T);
        buffer.append("\tSStot = sum(X');\n");
        buffer.append("\tSSdelta = (SStot(1)-SStot(2))/SStot(2);\n");
        buffer.append("\tend\n");
        return(buffer);
        
    }
    
    public StringBuffer getExpRunBuffer(StringBuffer buffer, double[] T){
        // lets look to see if the step is too big, cuz we always want to capture 
        // somthing from each run
        double Tstart = T[0];
        double Tstop = T[1];
        double Tstep = T[2];
        double Td = Tstop - Tstart;
        if(Tstep>Td){
            Tstep = Td;
        }
                
        // let change these to a string
        String[] Ts = {String.valueOf(Tstart),
                    String.valueOf(Tstop),String.valueOf(Tstep)};
        // first we need to set up the itital conditions
        buffer.append("\tDF.INITIAL_CONDITIONS=DF.PARAMETER_VECTOR(nP+1:end);\n");
        
        
        // run the model
        buffer.append("\t[T,X]=SolveMassBalances(@DataFile,"+Ts[0]+","+Ts[1]+","+
                Ts[2]+",DF);\n");
        // now set the IC in the Parameter Vectorfor next time 
        buffer.append("\tDF.PARAMETER_VECTOR(nP+1:end) = X(end,:)';\n");
        
        return(buffer);
    }
    
    public StringBuffer getSensExpRunBuffer(StringBuffer buffer, double[] T){
        // lets look to see if the step is too big, cuz we always want to capture 
        // somthing from each run
        double Tstart = T[0];
        double Tstop = T[1];
        double Tstep = T[2];
        double Td = Tstop - Tstart;
        if(Tstep>Td){
            Tstep = Td;
        }
                
        // let change these to a string
        String[] Ts = {String.valueOf(Tstart),
                    String.valueOf(Tstop),String.valueOf(Tstep)};
        // first we need to set up the itital conditions
        buffer.append("\tDF.INITIAL_CONDITIONS=DF.PARAMETER_VECTOR(nP+1:end);\n");
        // run the model
        buffer.append("\t[T,X,S,estT]=SolveAdjBalC(@DataFile,"+Ts[0]+","+Ts[1]+","+
                Ts[2]+",DF);\n");
        // now set the IC in the Parameter Vectorfor next time 
        buffer.append("\tDF.PARAMETER_VECTOR(nP+1:end) = X(end,:)';\n");
        
        return(buffer);
    }
    
    public StringBuffer getExpUpdateResultsBuffer(StringBuffer buffer){
        // override last endpoint with new initial point:
        buffer.append("\tXsim = [Xsim(1:end-1,:);X];\n");
        buffer.append("\tTsim = [Tsim(1:end-1);T'];\n");
        buffer.append("\tSsim = [Ssim;S(1:end-nX,:)];\n");
        return(buffer);
    }
    
    public StringBuffer getSensExpUpdateResultsBuffer(StringBuffer buffer){
        // override last endpoint with new initial point:
        buffer.append("\tXsim = [Xsim(1:end-1,:);X];\n");
        buffer.append("\tTsim = [Tsim(1:end-1);T'];\n");
        return(buffer);
    }
    
    public StringBuffer getExpFooterBuffer(StringBuffer buffer){
        buffer.append("return;\n");
        return(buffer);
    }
    
    public StringBuffer getSensExpFooterBuffer(StringBuffer buffer){
        buffer.append("return;\n");
        return(buffer);
    }
    
    public void writeExpBuffer(StringBuffer buffer, String expId) throws Exception {
        CUtil.write(new String("exp_"+expId+".m"),buffer,_xmlPropTree);
    }
    
    public void writeSensExpBuffer(StringBuffer buffer, String expId) throws Exception {
        CUtil.write(new String("expSen_"+expId+".m"),buffer,_xmlPropTree);
    }
    
    public StringBuffer getMSEGraphFntHeader(StringBuffer buffer,Date date){
        
    	// ok, we need to set the grapher name -
    	String strGrapherFileNameRaw = _xmlPropTree.getProperty("//Model/grapher_filename/text()");
    	
    	// Cut the extension off -
    	int INT_2_DOT = strGrapherFileNameRaw.lastIndexOf(".");
    	String strGrapherFileName = strGrapherFileNameRaw.substring(0, INT_2_DOT);
    	
    	// update the name of the grapher file -
    	buffer.append("function [MSE] = ");
    	buffer.append(strGrapherFileName);
    	buffer.append("(DF)\n");
    	
    	buffer.append("\n");
        buffer.append("% --Machine Made via BioChemExp on "+date.toString()+"--\n");
        buffer.append("% This code was designed to graph relative\n");
        buffer.append("% simulation and experimental results\n");
        buffer.append("% -----------------------------------------\n");
        // set up the total data variable to calculate the MSE
        buffer.append("\t% create a place to save all data for MSE\n");
        buffer.append("\ttotData = [];\n");
        return(buffer);
    }
    
    public StringBuffer getMSEFntRun(StringBuffer buffer,Vector expList){
        buffer.append("\t% Run all of the experiments\n");
        for(int i=0;i<expList.size();++i)
        {
            Exp tmpExp = (Exp)expList.get(i);
        	String id = tmpExp.expId;
            buffer.append("\t[T"+id+", X"+id+"] = exp_"+id+"(DF);\n");
        }
        return(buffer);
    }


	public StringBuffer getMSEFntRun(StringBuffer buffer, Object[] expList) {
		// TODO Auto-generated method stub
		return null;
	}
}
