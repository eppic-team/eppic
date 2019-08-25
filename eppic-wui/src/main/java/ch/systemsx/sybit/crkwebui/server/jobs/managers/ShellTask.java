package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class ShellTask implements Callable<Integer> {

    public static final int CANT_START_PROCESS_ERROR_CODE = -1;
    public static final int SIGTERM_ERROR_CODE = 143;

    private List<String> cmd;
    private File stdOut;
    private File stdErr;

    private String jobId;

    private Process process;

    private boolean isRunning;

    private Future<Integer> output;

    private Date submissionDate;
    private Date executionDate;

    public ShellTask(List<String> cmd, File stdOut, File stdErr, String jobId) {
        this.cmd = cmd;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        isRunning = false;
        submissionDate = new Date();
        this.jobId = jobId;
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
        executionDate = new Date();

        return process.waitFor();
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

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public String getJobId() {
        return jobId;
    }
}
