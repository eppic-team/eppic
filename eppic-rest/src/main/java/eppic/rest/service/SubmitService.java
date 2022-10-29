package eppic.rest.service;

import eppic.model.dto.SubmissionStatus;
import eppic.model.shared.StatusOfJob;
import eppic.rest.commons.FileFormat;
import eppic.rest.jobs.JobHandlerException;
import eppic.rest.jobs.JobManager;
import eppic.rest.jobs.JobManagerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;


public class SubmitService {

    private static final int DEFAULT_NUM_THREADS_PER_JOB = 2;

    // TODO we need a config file with these an other possible server settings
    private static JobManager jobManager = JobManagerFactory.getJobManager("/tmp", 2);

    /**
     *
     * @return the newly created job identifier for the submission
     */
    public Response submit(FileFormat fileFormat, String fileName, InputStream fileInputStream, String email) throws JobHandlerException {
        // 1 validate
        email = validateEmail(email);
        fileName = validateFileName(fileName);

        // 2 write to disk so that CLI can read: first check if stream is gzipped or not, then write to disk ungzipped
        // TODO implement

        // 3 submit CLI job async: at end of job persist to db and send notification email
        // TODO build command
        String submissionId = jobManager.startJob(fileName, null, null, DEFAULT_NUM_THREADS_PER_JOB);
        // TODO write to db and emailing at completion or error

        // 4 return generated id
        return buildResponse(new SubmissionStatus(submissionId, StatusOfJob.WAITING));
    }

    public Response getStatus(String submissionId) throws JobHandlerException {
        StatusOfJob status = jobManager.getStatusOfJob(submissionId);
        return buildResponse(new SubmissionStatus(submissionId, status));
    }

    private Response buildResponse(Object obj) {
        Response.ResponseBuilder responseBuilder =  Response
                .status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(obj);
        return responseBuilder.build();
    }

    private String validateEmail(String email) {
        // email (if not null), fileName is not empty and finishes with one of the allowed formats .pdb/.pdb.gz .cif/.cif.gz .bcif/.bcif.gz
        if (email == null) {
            return null;
        }
        long count = email.chars().filter(ch -> ch == '@').count();
        if (count!=1) {
            throw new BadRequestException("Email address provided does not seem valid");
        }
        String[] tokens = email.split("@");
        if (!tokens[1].contains(".")) {
            throw new BadRequestException("Email address provided does not seem valid");
        }


        return email.trim();
    }

    private String validateFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new BadRequestException("File name must not be empty");
        }
        return fileName.trim();
    }
}
