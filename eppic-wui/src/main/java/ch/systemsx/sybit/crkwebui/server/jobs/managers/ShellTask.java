package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import eppic.commons.util.StreamGobbler;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

public class ShellTask implements Callable<Integer> {

    private List<String> cmd;
    private File stdOut;
    private File stdErr;

    private Process process;

    public ShellTask(List<String> cmd, File stdOut, File stdErr) {
        this.cmd = cmd;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    @Override
    public Integer call() throws Exception {
        ProcessBuilder builder = new ProcessBuilder();

        builder.command(cmd);

        // TODO change
        //builder.directory(new File(System.getProperty("user.home")));
        process = builder.start();

        StreamGobbler s1 = new StreamGobbler ("stdout", process.getInputStream ());
        StreamGobbler s2 = new StreamGobbler ("stderr", process.getErrorStream ());
        s1.start();
        s2.start();
        // TODO write stdout and stderr to provided files

        return process.waitFor();
    }

    public void stop() {
        process.destroyForcibly(); //TODO or destroy?
    }
}
