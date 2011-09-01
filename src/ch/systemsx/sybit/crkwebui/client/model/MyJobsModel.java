package ch.systemsx.sybit.crkwebui.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Data model for jobs grid
 * @author srebniak_a
 *
 */
public class MyJobsModel extends BaseModel 
{
	private static final long serialVersionUID = 1L;

	public MyJobsModel() {
		set("jobid", "");
		set("status", "");
		set("input", "");
	}

	public MyJobsModel(String inputData, String status, String input) 
	{
		set("jobid", inputData);
		set("status", status);
		set("input", input);
	}

	public String getJobid() {
		return (String) get("jobid");
	}

	public String getStatus() {
		return (String) get("status");
	}

	public String getInput() {
		return (String) get("input");
	}
}
