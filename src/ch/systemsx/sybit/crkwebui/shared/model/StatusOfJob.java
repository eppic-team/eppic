package ch.systemsx.sybit.crkwebui.shared.model;

/**
 * This enum is used to represent current status of processing for the job
 * @author AS
 */
public enum StatusOfJob
{
	RUNNING("Running"),
	STOPPED("Stopped"),
	FINISHED("Finished"),
	ERROR("Error"),
	NONEXISTING("nonexisting"),
	QUEUING("queuing");
	
	StatusOfJob(String name) {
		this.name = name;
	}
	
	private String name;
	
	public String getName()
	{
		return name;
	}
}
