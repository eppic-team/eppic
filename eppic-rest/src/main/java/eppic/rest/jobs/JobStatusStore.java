package eppic.rest.jobs;

import eppic.model.shared.StatusOfJob;

import java.util.Collection;
import java.util.Date;

/**
 * Shared store of user job statuses, so that any replica of the API can report the status of a job,
 * not only the replica that runs it.
 */
public interface JobStatusStore {

    /**
     * The status of a job as recorded in the store.
     * @param status the last status written by the replica running the job
     * @param host the replica (pod hostname) that runs the job
     * @param heartbeatAt the last time the running replica confirmed it is still processing the job
     */
    record JobStatusRecord(StatusOfJob status, String host, Date heartbeatAt) {}

    /**
     * Records the status of a job, creating its record if it doesn't exist. Also refreshes its heartbeat.
     * @param submissionId the job's submission id
     * @param status the new status
     * @throws JobHandlerException if the status can't be written
     */
    void setStatus(String submissionId, StatusOfJob status) throws JobHandlerException;

    /**
     * Refreshes the heartbeat of the given jobs, signalling that this replica is still processing them.
     * @param submissionIds the jobs' submission ids
     * @throws JobHandlerException if the heartbeats can't be written
     */
    void heartbeat(Collection<String> submissionIds) throws JobHandlerException;

    /**
     * Gets the recorded status of a job.
     * @param submissionId the job's submission id
     * @return the record, or null if there is none for the job
     * @throws JobHandlerException if the store can't be read
     */
    JobStatusRecord get(String submissionId) throws JobHandlerException;
}
