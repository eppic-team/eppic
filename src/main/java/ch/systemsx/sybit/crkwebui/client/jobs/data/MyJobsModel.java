package ch.systemsx.sybit.crkwebui.client.jobs.data;

import java.io.Serializable;

/**
 * Data model for jobs grid.
 * @author nikhil
 *
 */
public class MyJobsModel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String jobid;
	private String status;
	private String input;
	
	public MyJobsModel() {
		this.jobid = "";
		this.status = "";
		this.input = "";
	}

	public MyJobsModel(String inputData, String status, String input) 
	{
		this.jobid = inputData;
		this.status = status;
		this.input = input;
	}

	public String getJobid() {
		return jobid;
	}
	
	public void setJobid(String jobid) {
		this.jobid = jobid;
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public String getInput() {
		return input;
	}
	
	public void setInput(String input) {
		this.input = input;
	}
	
}
