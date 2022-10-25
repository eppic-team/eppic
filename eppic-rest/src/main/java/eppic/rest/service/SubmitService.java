package eppic.rest.service;

import eppic.rest.commons.FileFormat;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public class SubmitService {

    /**
     *
     * @return the newly created job identifier for the submission
     */
    public Response submit(FileFormat fileFormat, String fileName, InputStream fileInputStream, String email) {
        // TODO implement
        // Steps
        // 1 validate
        // 2 generate id
        //     String uuid = UUID.randomUUID().toString(); // perhaps strip hyphens?
        // 3 write to disk so that CLI can read: first check if stream is gzipped or not, then write to disk ungzipped
        // 4 submit CLI job async: at end of job persist to db
        // 5 return generated id
        // Question: when to persist in db?
        return null;

    }

    public Response getStatus(String jobId) {
        // TODO implement
        return null;
    }
}
