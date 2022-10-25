package eppic.rest.service;

import eppic.rest.commons.FileFormat;
import eppic.rest.jobs.JobManager;
import eppic.rest.jobs.JobManagerFactory;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public class SubmitService {

    // TODO we need a config file with these an other possible server settings
    private static JobManager jobManager = JobManagerFactory.getJobManager("/tmp", 2);

    /**
     *
     * @return the newly created job identifier for the submission
     */
    public Response submit(FileFormat fileFormat, String fileName, InputStream fileInputStream, String email) {
        // TODO implement
        // Steps
        // 1 validate: email (if not null), fileName is not empty and finishes with one of the allowed formats .pdb/.pdb.gz .cif/.cif.gz .bcif/.bcif.gz
        // 2 generate id
        //     String uuid = UUID.randomUUID().toString(); // perhaps strip hyphens?
        // 3 write to disk so that CLI can read: first check if stream is gzipped or not, then write to disk ungzipped
        // 4 submit CLI job async: at end of job persist to db and send notification email
        // 5 return generated id
        return null;

    }

    public Response getStatus(String submissionId) {
        //jobManager.getStatusOfJob();
        // TODO implement
        return null;
    }
}
