package ch.systemsx.sybit.crkwebui.client.data;

import java.io.Serializable;

public class ResultsData implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String jobId;

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}
	

}
