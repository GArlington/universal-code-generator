package org.varnerlab.server.transport;

public class NullLogic implements IVLProcessServerRequest,IVLValidateServerJob {

	public Object getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean processMessage(String strMessage, VLServerSession session)
			throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	public Object validateJob(Object obj) throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Code:181\n");
		return(buffer);
	}

}
