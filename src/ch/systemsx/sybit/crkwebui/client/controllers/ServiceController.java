package ch.systemsx.sybit.crkwebui.client.controllers;

import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

public interface ServiceController {
	public abstract void test(String testValue);

	public abstract void getResultsOfProcessing(String jobId);

	public abstract void killJob(String selectedId);

	public abstract void getJobsForCurrentSession();

	public abstract void untieJobsFromSession();

	public abstract void loadSettings();

	public abstract void runJob(RunJobData runJobData);

	public abstract void getInterfaceResidues(String jobId, int interfaceId);

	public abstract void getCurrentStatusData(String jobId);

}
