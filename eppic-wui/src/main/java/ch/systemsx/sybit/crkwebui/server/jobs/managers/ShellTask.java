package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import java.io.File;
import java.io.IOException;
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

    // in milliseconds
    private long submissionTime;
    private long executionTime;
    private long finishTime;

    public ShellTask(List<String> cmd, File stdOut, File stdErr, String jobId) {
        this.cmd = cmd;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        isRunning = false;
        submissionTime = System.currentTimeMillis();
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

        executionTime = System.currentTimeMillis();

        try {
            int exitStatus = process.waitFor();

            finishTime = System.currentTimeMillis();

            isRunning = false;

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

    public String getJobId() {
        return jobId;
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
}
