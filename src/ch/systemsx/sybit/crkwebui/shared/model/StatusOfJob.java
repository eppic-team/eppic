package ch.systemsx.sybit.crkwebui.shared.model;

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
