package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.Date;
import java.util.List;

import model.JobDB;
import model.PDBScoreItemDB;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.ProcessingInProgressData;

public interface JobDAO 
{
	public void insertNewJob(String jobId, 
							 String sessionId,
							 String email, 
							 String input,
							 String ip,
							 Date submissionDate,
							 int inputType) throws CrkWebException;
	
	public void updateStatusOfJob(String jobId, String status) throws CrkWebException;
	
	public void untieJobsFromSession(String sessionId) throws CrkWebException;
	
	public List<ProcessingInProgressData> getJobsForSession(String sessionId) throws CrkWebException;
	
	public Long getNrOfJobsForSessionId(String sessionId) throws CrkWebException;
	
	public String getStatusForJob(String jobId) throws CrkWebException;
	
	public int getInputTypeForJob(String jobId) throws CrkWebException;
	 
	public ProcessingInProgressData createProcessingInProgressData(JobDB job);
	
	public Long getNrOfJobsForIPDuringLastDay(String ip) throws CrkWebException;
	
	public Date getOldestJobSubmissionDateDuringLastDay(String ip) throws CrkWebException;

	public void untieSelectedJobFromSession(String sessionId, String jobToUntie) throws CrkWebException;

	public String getInputForJob(String jobId) throws CrkWebException;

	public void setPdbScoreItemForJob(String jobId, PDBScoreItemDB pdbScoreItem) throws CrkWebException;
	
}
