package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

public class ShellTask implements Callable<Integer> {

    public static final int CANT_START_PROCESS_ERROR_CODE = -1;

    private List<String> cmd;
    private File stdOut;
    private File stdErr;

    private Process process;

    private boolean isRunning;

    public ShellTask(List<String> cmd, File stdOut, File stdErr) {
        this.cmd = cmd;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        isRunning = false;
    }

    @Override
    public Integer call() throws Exception {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command(cmd);

        try {
            process = builder.start();
        } catch (IOException e) {
            // if file not found or can't be executed
            return CANT_START_PROCESS_ERROR_CODE;
        }
        isRunning = true;

        builder.redirectOutput(stdOut);
        builder.redirectError(stdErr);

        return process.waitFor();
    }

    public void stop() {
        process.destroy(); //TODO or destroyForcibly?
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
