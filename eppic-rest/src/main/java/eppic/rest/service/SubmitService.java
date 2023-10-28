package eppic.rest.service;

import eppic.db.mongoutils.MongoDbStore;
import eppic.model.dto.SubmissionStatus;
import eppic.model.shared.StatusOfJob;
import eppic.rest.commons.FileFormat;
import eppic.rest.jobs.EmailData;
import eppic.rest.jobs.EmailMessageData;
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
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;


public class SubmitService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitService.class);

    private static final int DEFAULT_NUM_THREADS_PER_JOB = 2;
    /**
     * Max allowed file size (uncompressed) in bytes. If more than this, it is rejected with a 400
     */
    private static final long MAX_ALLOWED_FILE_SIZE = 10 * 1024 * 1024;


    // note the dreaded singleton! Jersey inits the service class with every request, so I don't see a way to avoid the singleton here
    private static JobManager jobManager;
    private File baseOutDir;
    private String javaVMExec;
    private int memForEppicProcess;
    private int numThreadsEppicProcess;
    private String eppicJarPath;
    private File cliConfigFile;

    private EmailData emailData;

    public SubmitService(Map<String, Object> props) {
        setConfigs(props);
    }

    private void setConfigs(Map<String, Object> props) {
        baseOutDir = new File((String)props.get("base.userjobs.dir"));
        int numThreadsJobManager = Integer.parseInt((String)props.get("num.threads.job.manager"));
        javaVMExec = (String) props.get("java.jre.exec");
        numThreadsEppicProcess = Integer.parseInt((String)props.get("num.threads.eppic.process"));
        memForEppicProcess = Integer.parseInt((String)props.get("mem.eppic.process"));
        eppicJarPath = (String) props.get("eppic.jar.path");
        cliConfigFile = new File((String) props.get("cli.config.file"));
        if (jobManager == null) {
            // init only first time, it is a singleton
            jobManager = JobManagerFactory.getJobManager(baseOutDir.getAbsolutePath(), numThreadsJobManager);
        }
        emailData = new EmailData();
        emailData.setHost((String)props.get("email_host"));
        emailData.setPort((String)props.get("email_port"));
        emailData.setEmailSenderPassword((String)props.get("email_password"));
        emailData.setEmailSenderUserName((String)props.get("email_username"));
        emailData.setReplyToAddress((String)props.get("email_replyto_address"));
        EmailMessageData emailMessageData = new EmailMessageData();
        emailData.setEmailMessageData(emailMessageData);
        emailMessageData.setEmailJobSubmittedTitle((String)props.get("email_job_submitted_title"));
        emailMessageData.setEmailJobSubmittedMessage((String)props.get("email_job_submitted_message"));
        emailMessageData.setEmailJobFinishedTitle((String)props.get("email_job_finished_title"));
        emailMessageData.setEmailJobFinishedMessage((String)props.get("email_job_finished_message"));
        emailMessageData.setEmailJobErrorTitle((String)props.get("email_job_error_title"));
        emailMessageData.setEmailJobErrorMessage((String)props.get("email_job_error_message"));
        emailMessageData.setBaseUrlJobRetrieval((String)props.get("email_base_url_job_retrieval"));
    }

    /**
     *
     * @return the newly created job identifier for the submission
     */
    public Response submit(FileFormat fileFormat, String fileName, InputStream inputStream, String email) throws JobHandlerException, IOException {
        // 1 validate
        email = validateEmail(email);
        if (email != null) {
            emailData.setEmailRecipient(email);
        } else {
            emailData = null;
        }
        fileName = validateFileName(fileName);

        // 2 Create a submission id
        String submissionId = UUID.randomUUID().toString(); // perhaps strip hyphens?

        // 3 write to disk so that CLI can read: first check if stream is gzipped or not, then write to disk ungzipped. Also validates the file size
        File outDir = new File(baseOutDir, submissionId);
        if (!outDir.exists()) {
            boolean created = outDir.mkdir();
            if (!created) {
                logger.error("Could not create job dir {}", outDir);
                throw new IOException("Could not create job dir "+outDir);
            } else {
                logger.info("Created job dir {}", outDir);
            }
        }
        File file = new File(outDir, fileName);
        File link = new File(outDir, submissionId);
        logger.info("Writing user's file for job '{}' to file '{}'", submissionId, file);
        writeToFile(handleGzip(inputStream), file);
        String baseNameForOutput = truncateFileName(file.getName());

        // A symlink link to the input file that uses the submissionId, needed by file download endpoints (see JobService.getCoordinateFile())
        logger.info("Creating symlink for job '{}'. From '{}' to '{}'", submissionId, link, file);
        Files.createSymbolicLink(link.toPath(), file.toPath());

        // 4 submit CLI job async: at end of job persist to db and send notification email
        List<String> cmd = EppicCliGenerator.generateCommand(javaVMExec, eppicJarPath, file, submissionId, baseNameForOutput, outDir.getAbsolutePath(), numThreadsEppicProcess, memForEppicProcess, cliConfigFile);
        jobManager.startJob(submissionId, cmd, outDir, baseNameForOutput, DEFAULT_NUM_THREADS_PER_JOB, MongoDbStore.getMongoDbUserJobs(), emailData);

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

    /**
     * Truncates the given fileName by removing anything after the last dot.
     * If no dot present in fileName then nothing is truncated.
     * @param fileName the file name
     * @return the truncated file name
     */
    private static String truncateFileName(String fileName) {
        if( fileName == null) return null;

        String newName = fileName;
        int lastPeriodPos = fileName.lastIndexOf('.');
        if (lastPeriodPos >= 0)
        {
            newName = fileName.substring(0, lastPeriodPos);
        }
        return newName;
    }
}
