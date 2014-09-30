package ch.systemsx.sybit.crkwebui.client.commons.events;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.JobListRetrievedHandler;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when list of jobs was retrieved.
 * @author AS
 */
public class JobListRetrievedEvent extends GwtEvent<JobListRetrievedHandler> 
{
	public static Type<JobListRetrievedHandler> TYPE = new Type<JobListRetrievedHandler>();
	
	/**
	 * List of retrieved jobs.
	 */
	private final List<ProcessingInProgressData> jobs;
	
	public JobListRetrievedEvent(List<ProcessingInProgressData> jobs)
	{
		this.jobs = jobs;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<JobListRetrievedHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(JobListRetrievedHandler handler) 
	{
		handler.onJobListRetrieved(this);
	}

	/**
	 * Gets list of retrieved jobs.
	 * @return list of retrieved jobs
	 */
	public List<ProcessingInProgressData> getJobs() {
		return jobs;
	}
}
