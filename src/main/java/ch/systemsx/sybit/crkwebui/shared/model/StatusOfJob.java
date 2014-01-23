package ch.systemsx.sybit.crkwebui.shared.model;

/**
 * This enum is used to represent current status of processing for the job.
 * @author AS
 */
public enum StatusOfJob
{
	RUNNING("Running"),
	FINISHED("Finished"),
	ERROR("Error"),
	STOPPED("Stopped"),
	NONEXISTING("Nonexisting"),
	QUEUING("Queuing"),
	WAITING("Waiting");

	StatusOfJob(String name) {
		this.name = name;
	}

	private String name;

	public String getName()
	{
		return name;
	}

	public static StatusOfJob getByName(String name)
	{
		for(StatusOfJob statusOfJob : StatusOfJob.values())
		{
			if(statusOfJob.getName().equals(name))
			{
				return statusOfJob;
			}
		}

		return null;
	}
}
