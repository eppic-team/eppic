package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;
import eppic.model.shared.StatusOfJob;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TestNativeJobManager {

    private static JobManager jobManager;
    private static final String jobDir = System.getProperty("java.io.tmpdir");
    private static File script;

    private static final int SLEEP_TIME = 10;

    @BeforeClass
    public static void init() throws JobManagerException, IOException {
        jobManager = JobManagerFactory.getJobManager(jobDir, 2);
        writeScript();
    }

    @AfterClass
    public static void destroy() {
        //script.delete();
    }

    private static void writeScript() throws IOException {
        script = new File(jobDir,"eppicJobManagerTest.sh");
        PrintWriter pw = new PrintWriter(script);
        pw.println("#!/bin/sh");
        pw.println("sleep " + SLEEP_TIME);
        pw.close();
        script.setExecutable(true);
    }

    @Test
    public void testSubmit() throws JobHandlerException, InterruptedException {
        String jobId = "abcdefgh";
        File dir = new File (jobDir, jobId);
        List<String> cmd = new ArrayList<>();
        cmd.add(script.toString());

        String submissionId = jobManager.startJob(jobId, cmd , dir.toString(),1);
        assertNotNull(submissionId);
        StatusOfJob statusOfJob = jobManager.getStatusOfJob(jobId, submissionId);
        assertEquals(StatusOfJob.RUNNING, statusOfJob);

        Thread.sleep(SLEEP_TIME*1000 + 5000);

        statusOfJob = jobManager.getStatusOfJob(jobId, submissionId);

        assertEquals(StatusOfJob.FINISHED, statusOfJob);
    }
}
