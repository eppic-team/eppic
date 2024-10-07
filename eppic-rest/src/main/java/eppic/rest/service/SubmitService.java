package eppic.rest.service;

import com.mongodb.client.MongoDatabase;
import eppic.commons.util.FileTypeGuesser;
import eppic.db.mongoutils.MongoUtils;
import eppic.model.dto.SubmissionStatus;
import eppic.model.dto.UserJobSubmission;
import eppic.model.shared.StatusOfJob;
import eppic.rest.commons.EppicRestProperties;
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
import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class SubmitService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitService.class);

    private static final int DEFAULT_NUM_THREADS_PER_JOB = 2;
    /**
     * Max allowed file size to write to db (gzip compressed). If more than this, it is rejected with a 400
     */
    private static final long MAX_ALLOWED_FILE_SIZE = 10 * 1024 * 1024;
    /**
     * Max allowed file size to upload (however the upload comes, be it gzipped or not) in bytes
     */
    private static final long MAX_ALLOWED_FILE_SIZE_UPLOAD = 100 * 1024 * 1024;



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

    private final EppicRestProperties eppicRestProperties;

    @Autowired
    public SubmitService(EppicRestProperties eppicRestProperties) {
        this.eppicRestProperties = eppicRestProperties;
        init();
    }

    private void init() {
        baseOutDir = new File(eppicRestProperties.getBaseUserjobsDir());
        int numThreadsJobManager = eppicRestProperties.getNumThreadsJobManager();
        javaVMExec = eppicRestProperties.getJavaJreExec();
        numThreadsEppicProcess = eppicRestProperties.getNumThreadsEppicProcess();
        memForEppicProcess = eppicRestProperties.getMemEppicProcess();
        eppicJarPath = eppicRestProperties.getEppicJarPath();
        cliConfigFile = new File(eppicRestProperties.getCliConfigFile());
        if (jobManager == null) {
            // init only first time, it is a singleton
            jobManager = JobManagerFactory.getJobManager(baseOutDir.getAbsolutePath(), numThreadsJobManager);
        }
        emailData = new EmailData();
        emailData.setHost(eppicRestProperties.getEmailHost());
        emailData.setPort(String.valueOf(eppicRestProperties.getEmailPort()));
        emailData.setEmailSenderPassword(eppicRestProperties.getEmailPassword());
        emailData.setEmailSenderUserName(eppicRestProperties.getEmailUsername());
        emailData.setReplyToAddress(eppicRestProperties.getEmailReplytoAddress());
        EmailMessageData emailMessageData = new EmailMessageData();
        emailData.setEmailMessageData(emailMessageData);
        emailMessageData.setEmailJobSubmittedTitle(eppicRestProperties.getEmailJobSubmittedTitle());
        emailMessageData.setEmailJobSubmittedMessage(eppicRestProperties.getEmailJobSubmittedMessage());
        emailMessageData.setEmailJobFinishedTitle(eppicRestProperties.getEmailJobFinishedTitle());
        emailMessageData.setEmailJobFinishedMessage(eppicRestProperties.getEmailJobFinishedMessage());
        emailMessageData.setEmailJobErrorTitle(eppicRestProperties.getEmailJobErrorTitle());
        emailMessageData.setEmailJobErrorMessage(eppicRestProperties.getEmailJobErrorMessage());
        emailMessageData.setBaseUrlJobRetrieval(eppicRestProperties.getEmailBaseUrlJobRetrieval());

        logger.info("Initialised email data: {}", emailData);

        mongoDbUserJobs = MongoUtils.getMongoDatabase(eppicRestProperties.getDbNameUserjobs(), eppicRestProperties.getMongoUriUserjobs());
    }

    /**
     *
     * @return the newly created job identifier for the submission
     */
    public SubmissionStatus submit(UserJobSubmission userJobSubmission) throws JobHandlerException, IOException {
        // TODO what to do with fileName (that comes in userJobSumission object)

        // 1 validate
        String email = validateEmail(userJobSubmission.getEmail());
        if (email != null) {
            emailData.setEmailRecipient(email);
        }

        // 2 Create a submission id
        String submissionId = UUID.randomUUID().toString(); // perhaps strip hyphens?

        // 3 write to disk so that CLI can read: first check if stream is gzipped or not, then write to disk gzipped. Also validates the file size
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
        byte[] uploadedFileContent = Base64.getDecoder().decode(userJobSubmission.getData());
        InputStream inputStream = new ByteArrayInputStream(uploadedFileContent);
        byte[] bytes = inputStream.readAllBytes();
        if (bytes.length > MAX_ALLOWED_FILE_SIZE_UPLOAD) {
            long maxInMb = MAX_ALLOWED_FILE_SIZE_UPLOAD / (1024*1024);
            throw new BadRequestException("Input file exceeds the maximum allowed size. Please only submit files up to " + maxInMb +" MB");
        }
        // note that file will be written gzipped (whatever the input was)
        writeToFile(FileTypeGuesser.handleGzip(new ByteArrayInputStream(bytes)), file);

        // TODO write original file name to serialized file and then to db, then we'd have a nice display name for UI

        // 4 submit CLI job async: at end of job persist to db and send notification email
        List<String> cmd = EppicCliGenerator.generateCommand(javaVMExec, eppicJarPath, file, submissionId, outDir.getAbsolutePath(), numThreadsEppicProcess, memForEppicProcess, cliConfigFile, userJobSubmission.isSkipEvolAnalysis());
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

    private void writeToFile(InputStream is, File file) throws IOException, BadRequestException {
        byte[] buffer = is.readAllBytes();

        OutputStream outStream = new GZIPOutputStream(new FileOutputStream(file));
        outStream.write(buffer);
        outStream.close();

        // we check the size of the gzipped file
        if (file.length() > MAX_ALLOWED_FILE_SIZE) {
            long maxInMb = MAX_ALLOWED_FILE_SIZE / (1024*1024);
            throw new BadRequestException("Input file exceeds the maximum allowed size. Please only submit files up to " + maxInMb +" MB");
        }
    }

}
