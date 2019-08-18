package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

public class ShellTask implements Callable<Integer> {

    private List<String> cmd;
    private File outDir;
    private File stdOut;
    private File stdErr;

    private Process process;

    private boolean isRunning;

    public ShellTask(List<String> cmd, File outDir, File stdOut, File stdErr) {
        this.cmd = cmd;
        this.outDir = outDir;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        isRunning = false;
    }

    @Override
    public Integer call() throws Exception {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command(cmd);

        builder.directory(outDir);

        process = builder.start();
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
