package ch.systemsx.sybit.crkwebui.client.controllers;

import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;


public interface ServiceController
{
	public abstract void test(String testValue);
	
	public abstract void checkIfDataProcessed(String selectedId);

	public abstract void getStatusData(String selectedId);

	public abstract void getResultData(String selectedId);
	
	public abstract void killJob(String selectedId);
	
	public abstract void getJobsForCurrentSession();

	public abstract void untieJobsFromSession();
	
	public abstract void loadSettings();

	public abstract void runJob(RunJobData runJobData);

}
