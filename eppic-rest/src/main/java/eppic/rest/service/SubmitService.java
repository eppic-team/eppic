package eppic.rest.service;

import eppic.model.dto.SubmissionStatus;
import eppic.model.shared.StatusOfJob;
import eppic.rest.commons.FileFormat;
import eppic.rest.jobs.EppicCliGenerator;
import eppic.rest.jobs.JobHandlerException;
import eppic.rest.jobs.JobManager;
import eppic.rest.jobs.JobManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;


public class SubmitService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitService.class);

    private static final int DEFAULT_NUM_THREADS_PER_JOB = 2;
    /**
     * Max allowed file size (uncompressed) in bytes. If more than this, it is rejected with a 400
     */
    private static final long MAX_ALLOWED_FILE_SIZE = 10 * 1024 * 1024;


    private final JobManager jobManager;
    private final File baseOutDir;

    public SubmitService() {
        // TODO we need a config file with these an other possible server settings
        baseOutDir = new File("/tmp");
        jobManager = JobManagerFactory.getJobManager(baseOutDir.getAbsolutePath(), 2);
    }

    /**
     *
     * @return the newly created job identifier for the submission
     */
    public Response submit(FileFormat fileFormat, String fileName, InputStream inputStream, String email) throws JobHandlerException, IOException {
        // 1 validate
        email = validateEmail(email);
        fileName = validateFileName(fileName);

        // 2 Create a submission id
        String submissionId = UUID.randomUUID().toString(); // perhaps strip hyphens?

        // 3 write to disk so that CLI can read: first check if stream is gzipped or not, then write to disk ungzipped. Also validates the file size
        File outDir = new File(baseOutDir, submissionId);
        File file = new File(baseOutDir, fileName);
        writeToFile(handleGzip(inputStream), file);

        // 4 submit CLI job async: at end of job persist to db and send notification email
        List<String> cmd = EppicCliGenerator.generateCommand(javaVMExec, eppicJarPath, file , outDir, nrOfThreadsForSubmission, assignedMemory);
        jobManager.startJob(submissionId, cmd, outDir.getAbsolutePath(), DEFAULT_NUM_THREADS_PER_JOB);
        // TODO write to db and emailing at completion or error

        // 5 return generated id
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

    private InputStream handleGzip(InputStream inputStream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        bufferedInputStream.mark(2);
        int b0 = bufferedInputStream.read();
        int b1 = bufferedInputStream.read();
        bufferedInputStream.reset();

        // apply GZIPInputStream decorator if gzipped content
        if ((b1 << 8 | b0) == GZIPInputStream.GZIP_MAGIC) {
            logger.debug("Reading gzip");
            return new GZIPInputStream(bufferedInputStream);
        }
        logger.debug("Reading uncompressed");
        return bufferedInputStream;
    }

    private void writeToFile(InputStream is, File file) throws IOException, BadRequestException {
        byte[] buffer = is.readAllBytes();
        if (buffer.length > MAX_ALLOWED_FILE_SIZE) {
            long maxInMb = MAX_ALLOWED_FILE_SIZE / (1024*1024);
            throw new BadRequestException("Input file exceeds the maximum allowed size. Please only submit files up to " + maxInMb +" MB");
        }
        OutputStream outStream = new FileOutputStream(file);
        outStream.write(buffer);
        outStream.close();
    }
}
