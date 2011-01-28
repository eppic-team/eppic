package ch.systemsx.sybit.crkwebui.client.gui;

import com.extjs.gxt.ui.client.data.BaseModel;

public class MyJobsModel extends BaseModel
{
	private static final long serialVersionUID = 1L;  
	  
	public MyJobsModel() 
	{
		
	}  
	
	public MyJobsModel(String jobId,
					   String status,
					   String input) 
	{  
		set("jobId", jobId);  
		set("status", status);
		set("input", input);
	}  
	
	public String getJobId() 
	{  
		return (String) get("jobId");  
	}  
	
	public String getStatus() 
	{  
		return (String) get("status");  
	} 
	
	public String getInput() 
	{  
		return (String) get("input");  
	} 
}
