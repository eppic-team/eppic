package eppic.rest.service;

import com.mongodb.client.MongoDatabase;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.dto.SubmissionStatus;
import eppic.model.shared.StatusOfJob;
import eppic.rest.commons.ServerProperties;
import eppic.rest.jobs.EmailData;
import eppic.rest.jobs.EmailMessageData;
import eppic.rest.jobs.EppicCliGenerator;
import eppic.rest.jobs.JobHandlerException;
import eppic.rest.jobs.JobManager;
import eppic.rest.jobs.JobManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

@Service
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

    private MongoDatabase mongoDbUserJobs;

    private final ServerProperties serverProperties;

    @Autowired
    public SubmitService(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
        init();
    }

    private void init() {
        baseOutDir = new File(serverProperties.getBaseUserjobsDir());
        int numThreadsJobManager = serverProperties.getNumThreadsJobManager();
        javaVMExec = serverProperties.getJavaJreExec();
        numThreadsEppicProcess = serverProperties.getNumThreadsEppicProcess();
        memForEppicProcess = serverProperties.getMemEppicProcess();
        eppicJarPath = serverProperties.getEppicJarPath();
        cliConfigFile = new File(serverProperties.getCliConfigFile());
        if (jobManager == null) {
            // init only first time, it is a singleton
            jobManager = JobManagerFactory.getJobManager(baseOutDir.getAbsolutePath(), numThreadsJobManager);
        }
        emailData = new EmailData();
        emailData.setHost(serverProperties.getEmailHost());
        emailData.setPort(String.valueOf(serverProperties.getEmailPort()));
        emailData.setEmailSenderPassword(serverProperties.getEmailPassword());
        emailData.setEmailSenderUserName(serverProperties.getEmailUsername());
        emailData.setReplyToAddress(serverProperties.getEmailReplytoAddress());
        EmailMessageData emailMessageData = new EmailMessageData();
        emailData.setEmailMessageData(emailMessageData);
        emailMessageData.setEmailJobSubmittedTitle(serverProperties.getEmailJobSubmittedTitle());
        emailMessageData.setEmailJobSubmittedMessage(serverProperties.getEmailJobSubmittedMessage());
        emailMessageData.setEmailJobFinishedTitle(serverProperties.getEmailJobFinishedTitle());
        emailMessageData.setEmailJobFinishedMessage(serverProperties.getEmailJobFinishedMessage());
        emailMessageData.setEmailJobErrorTitle(serverProperties.getEmailJobErrorTitle());
        emailMessageData.setEmailJobErrorMessage(serverProperties.getEmailJobErrorMessage());
        emailMessageData.setBaseUrlJobRetrieval(serverProperties.getEmailBaseUrlJobRetrieval());

        mongoDbUserJobs = MongoUtils.getMongoDatabase(serverProperties.getDbNameUserjobs(), serverProperties.getMongoUriUserjobs());
    }

    /**
     *
     * @return the newly created job identifier for the submission
     */
    public SubmissionStatus submit(String fileName, InputStream inputStream, String email, boolean skipEvolAnalysis) throws JobHandlerException, IOException {
        // 1 validate
        email = validateEmail(email);
        if (email != null) {
            emailData.setEmailRecipient(email);
        } else {
            emailData = null;
        }

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
        File file = new File(outDir, submissionId);
        logger.info("Writing user's coordinate input file for job '{}' to file '{}'", submissionId, file);
        // note that file will be written ungzipped
        writeToFile(handleGzip(inputStream), file);

        // TODO write original file name to serialized file and then to db, then we'd have a nice display name for UI

        // 4 submit CLI job async: at end of job persist to db and send notification email
        List<String> cmd = EppicCliGenerator.generateCommand(javaVMExec, eppicJarPath, file, submissionId, outDir.getAbsolutePath(), numThreadsEppicProcess, memForEppicProcess, cliConfigFile, skipEvolAnalysis);
        jobManager.startJob(submissionId, cmd, outDir, DEFAULT_NUM_THREADS_PER_JOB, mongoDbUserJobs, emailData);

        // 5 return generated id
        return new SubmissionStatus(submissionId, StatusOfJob.WAITING);
    }

    public SubmissionStatus getStatus(String submissionId) throws JobHandlerException {
        StatusOfJob status = jobManager.getStatusOfJob(submissionId);
        return new SubmissionStatus(submissionId, status);
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
