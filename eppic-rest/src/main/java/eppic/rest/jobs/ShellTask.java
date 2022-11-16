package eppic.rest.jobs;

import com.mongodb.client.MongoDatabase;
import eppic.db.dao.DaoException;
import eppic.db.dao.InterfaceResidueFeaturesDAO;
import eppic.db.dao.PDBInfoDAO;
import eppic.db.dao.mongo.InterfaceResidueFeaturesDAOMongo;
import eppic.db.dao.mongo.PDBInfoDAOMongo;
import eppic.db.loaders.EntryData;
import eppic.db.loaders.UploadToDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ShellTask implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ShellTask.class);

    public static final int CANT_START_PROCESS_ERROR_CODE = -1;
    public static final int SIGTERM_ERROR_CODE = 143;

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

    private final String baseNameForOutput;
    private final MongoDatabase mongoDb;
    private final String email;

    /**
     *
     * @param cmd the full EPPIC CLI command
     * @param jobDirectory the output directory for files produced by EPPIC CLI
     * @param baseNameForOutput the base name for files produced by EPPIC CLI
     * @param submissionId the submission id
     * @param mongoDb the Mongo db, if null no writing to DB will be performed
     * @param email the email address to notify, if null no email is sent
     */
    public ShellTask(List<String> cmd, File jobDirectory, String baseNameForOutput, String submissionId, MongoDatabase mongoDb, String email) {
        this.cmd = cmd;
        this.stdErr = new File(jobDirectory, submissionId + ".e");
        this.stdOut = new File(jobDirectory, submissionId + ".o");
        isRunning = false;
        submissionTime = System.currentTimeMillis();
        this.submissionId = submissionId;
        this.jobDirectory = jobDirectory;
        this.baseNameForOutput = baseNameForOutput;
        this.mongoDb = mongoDb;
        this.email = email;
    }

    @Override
    public Integer call() throws Exception {

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
                    notifyByEmail();
                } catch (IOException | DaoException e) {
                    logger.error("Could not write results to db. Setting exit status to 1 (error). Error message was: {}", e.getMessage());
                    exitStatus = 1;
                }
            }

            if (exitStatus == 0) {
                // TODO send success email
                //EmailData data = new EmailData();
                //EmailSender sender = new EmailSender(data);
                //sender.send();
            } else {
                // TODO send error email
            }

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
        EntryData entryData = UploadToDb.readSerializedFile(new File(jobDirectory, baseNameForOutput + UploadToDb.SERIALIZED_FILE_SUFFIX));
        if (entryData == null) {
            throw new IOException("Could not read serialized file");
        }
        dao.insertPDBInfo(entryData.getPdbInfoDB());
        interfResDao.insertInterfResFeatures(entryData.getInterfResFeaturesDB());
    }

    private void notifyByEmail() {
        if (email == null) {
            logger.info("No email will be sent because email was set to null");
            return;
        }
        // TODO implement
    }
}
