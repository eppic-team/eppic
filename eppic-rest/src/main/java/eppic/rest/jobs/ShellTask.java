package eppic.rest.jobs;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.BlobsDao;
import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceResidueFeaturesDAO;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.dao.mongo.BlobsDAOMongo;
import eppic.db.dao.mongo.InterfaceResidueFeaturesDAOMongo;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.loaders.EntryData;
import eppic.db.loaders.UploadToDb;
import eppic.model.db.BlobIdentifierDB;
import eppic.model.db.FileTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class ShellTask implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ShellTask.class);

    public static final int CANT_START_PROCESS_ERROR_CODE = -1;
    public static final int SIGTERM_ERROR_CODE = 143;

    public static final int CANT_WRITE_TO_DB_ERROR_CODE = 123;

    private final List<String> cmd;
    private final File stdOut;
    private final File stdErr;

    private final String submissionId;

    private Process process;

    private boolean isRunning;

    private Future<Integer> output;

    // in milliseconds
    private long submissionTime;
    private long executionTime;
    private long finishTime;

    private final File jobDirectory;

    private final MongoDatabase mongoDb;
    private final EmailData emailData;

    /**
     *
     * @param cmd the full EPPIC CLI command
     * @param jobDirectory the output directory for files produced by EPPIC CLI
     * @param submissionId the submission id
     * @param mongoDb the Mongo db, if null no writing to DB will be performed
     * @param emailData the email data for email notification, if null no email is sent
     */
    public ShellTask(List<String> cmd, File jobDirectory, String submissionId, MongoDatabase mongoDb, EmailData emailData) {
        this.cmd = cmd;
        this.stdErr = new File(jobDirectory, submissionId + ".e");
        this.stdOut = new File(jobDirectory, submissionId + ".o");
        isRunning = false;
        submissionTime = System.currentTimeMillis();
        this.submissionId = submissionId;
        this.jobDirectory = jobDirectory;
        this.mongoDb = mongoDb;
        this.emailData = emailData;
    }

    @Override
    public Integer call() throws Exception {

        logger.info("Running shell task with submissionId {}", submissionId);

        notifyByEmailOnSubmit();

        ProcessBuilder builder = new ProcessBuilder();

        builder.command(cmd);

        // only write if dir exists, otherwise process fails
        if (stdOut.getParentFile().exists()) {
            builder.redirectOutput(stdOut);
        }
        if (stdErr.getParentFile().exists()) {
            builder.redirectError(stdErr);
        }

        try {
            process = builder.start();
        } catch (IOException e) {
            logger.error("Could not start shell command. Error: {}", e.getMessage());
            // if file not found or can't be executed
            return CANT_START_PROCESS_ERROR_CODE;
        }

        isRunning = true;

        executionTime = System.currentTimeMillis();

        try {
            int exitStatus = process.waitFor();

            finishTime = System.currentTimeMillis();

            isRunning = false;

            if (exitStatus == 0) {
                try {
                    writeToDb();
                } catch (IOException | DaoException e) {
                    logger.error("Could not write results to db. Setting exit status to {} (error). Error message was: {}", CANT_WRITE_TO_DB_ERROR_CODE, e.getMessage());
                    exitStatus = CANT_WRITE_TO_DB_ERROR_CODE;
                }
            }
            notifyByEmailOnFinish(exitStatus);

            return exitStatus;

        } catch (InterruptedException e){
            isRunning = false;
            throw e;
        }
    }

    public void stop() {
        process.destroy();
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    void setOutput(Future<Integer> future) {
        this.output = future;
    }

    public Future<Integer> getOutput() {
        return output;
    }

    public long getSubmissionTime() {
        return submissionTime;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    /**
     * Get the number of milliseconds that this task was queuing
     * @return the queuing time in milliseconds
     */
    public long getTimeInQueue() {
        if (executionTime == 0) {
            return System.currentTimeMillis() - submissionTime;
        }
        return executionTime - submissionTime;
    }

    /**
     * Get the number of milliseconds that this task was running. If still running, then current running time reported.
     * If still queuing then -1 returned.
     * @return the running time in milliseconds
     */
    public long getTimeRunning() {
        long time;
        // case it is running
        if (isRunning) {
            time =  System.currentTimeMillis() - executionTime;
        } else if (executionTime == 0) {
            // case it is queuing still
            time = -1;
        } else {
            // case it is done
            time = finishTime - executionTime;
        }
        return time;
    }

    private void writeToDb() throws IOException, DaoException {
        if (mongoDb == null) {
            logger.info("No writing to DB will be performed because MongoDB was set to null");
            return;
        }
        PDBInfoDAO dao = new PDBInfoDAOMongo(mongoDb);
        InterfaceResidueFeaturesDAO interfResDao = new InterfaceResidueFeaturesDAOMongo(mongoDb);
        EntryData entryData = UploadToDb.readSerializedFile(new File(jobDirectory, submissionId + UploadToDb.SERIALIZED_FILE_SUFFIX));
        if (entryData == null) {
            throw new IOException("Could not read serialized file");
        }
        logger.info("Will write user job '{}' to db", submissionId);

        // important write the upload date so that we can track age of jobs
        entryData.getPdbInfoDB().setUploadDate(new Date());

        dao.insertPDBInfo(entryData.getPdbInfoDB());
        interfResDao.insertInterfResFeatures(entryData.getInterfResFeaturesDB());

        // write input file and images as blobs to db
        BlobsDao blobsDao = new BlobsDAOMongo(mongoDb);
        File inputFile = new File(jobDirectory, submissionId);
        // note SubmitService writes gzipped, then this will write to db gzipped
        try (InputStream is = new FileInputStream(inputFile)) {
            logger.info("Writing input coordinates file '{}' to db", inputFile);
            blobsDao.insert(new BlobIdentifierDB(submissionId, FileTypeEnum.COORDS, null), is.readAllBytes());
        }
        try (Stream<Path> stream = Files.list(jobDirectory.toPath())) {
            List<Path> list = stream
                    .filter(file -> !Files.isDirectory(file))
                    .filter(file -> file.toFile().getName().endsWith(".png"))
                    .toList();
            for (Path f : list) {
                // expected file name is like abcdef-abcdef-abcdef.assembly.1.75x75.png
                String[] tokens = f.toFile().getName().split("\\.");
                if (tokens.length != 5) {
                    logger.warn("File {} does not have the expected 5 tokens", f);
                    continue;
                }
                BlobIdentifierDB blobId = new BlobIdentifierDB(tokens[0], FileTypeEnum.valueOf(tokens[1].toUpperCase()), tokens[2]);
                try (InputStream pngIs = new FileInputStream(f.toFile())) {
                    logger.info("Writing image file '{}' to db", f);
                    blobsDao.insert(blobId, pngIs.readAllBytes());
                }
            }
        }
    }

    private void notifyByEmailOnSubmit() {
        if (emailData == null) {
            logger.info("No email will be sent because email was set to null");
            return;
        }
        EmailSender sender = new EmailSender(emailData);

        try {
            sender.sendSubmittedEmail(submissionId);
        } catch(MessagingException e) {
            logger.error("Could not send job submitted email. Error: {}", e.getMessage());
        }
    }

    private void notifyByEmailOnFinish(int exitStatus) {
        if (emailData == null) {
            logger.info("No email will be sent because email was set to null");
            return;
        }

        EmailSender sender = new EmailSender(emailData);

        try {
            if (exitStatus == 0) {
                sender.sendFinishSuccesfullyEmail(submissionId);
            } else {
                sender.sendFinishWithErrorEmail(submissionId);
            }
        } catch (MessagingException e) {
            logger.error("Could not send job finished email. Error: {}", e.getMessage());
        }
    }
}
