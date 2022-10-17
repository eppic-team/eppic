package eppic.rest.service;

import eppic.rest.commons.FileFormat;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public class SubmitService {

    /**
     *
     * @return the newly created job identifier for the submission
     */
    public Response submit(FileFormat fileFormat, boolean gzipped, String fileName, InputStream fileInputStream, String email) {
        // TODO implement
        return null;

    }

    public Response getStatus(String jobId) {
        // TODO implement
        return null;
    }
}
