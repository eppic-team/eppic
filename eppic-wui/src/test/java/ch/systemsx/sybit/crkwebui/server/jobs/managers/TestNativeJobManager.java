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

    private static final int SLEEP_TIME = 4;

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
    public void testBasicSubmit() throws JobHandlerException, InterruptedException {
        String jobId = "abcdefgh";
        File dir = new File (jobDir, jobId);
        List<String> cmd = new ArrayList<>();
        cmd.add(script.toString());

        String submissionId = jobManager.startJob(jobId, cmd , dir.toString(),1);
        assertNotNull(submissionId);

        // initially should be queuing, it takes some small time to schedule it
        StatusOfJob statusOfJob = jobManager.getStatusOfJob(jobId, submissionId);
        assertEquals(StatusOfJob.QUEUING, statusOfJob);

        // after half of the time, it should be running
        Thread.sleep(SLEEP_TIME*1000/2);
        statusOfJob = jobManager.getStatusOfJob(jobId, submissionId);
        assertEquals(StatusOfJob.RUNNING, statusOfJob);

        // after full time is over, it should be finished
        Thread.sleep(SLEEP_TIME*1000/2 + 1000);
        statusOfJob = jobManager.getStatusOfJob(jobId, submissionId);
        assertEquals(StatusOfJob.FINISHED, statusOfJob);
    }

    @Test
    public void testQueuing() throws JobHandlerException, InterruptedException {
        String jobId1 = "abc";
        String jobId2 = "def";
        String jobId3 = "ghi";
        File dir1 = new File (jobDir, jobId1);
        File dir2 = new File (jobDir, jobId2);
        File dir3 = new File (jobDir, jobId3);

        List<String> cmd = new ArrayList<>();
        cmd.add(script.toString());

        String submissionId1 = jobManager.startJob(jobId1, cmd , dir1.toString(),1);
        String submissionId2 = jobManager.startJob(jobId2, cmd , dir2.toString(),1);
        String submissionId3 = jobManager.startJob(jobId3, cmd , dir3.toString(),1);

        assertNotEquals(submissionId1, submissionId2);
        assertNotEquals(submissionId1, submissionId3);

        // queue is of size 2: 2 running and the 3rd task should be queuing
        Thread.sleep(SLEEP_TIME*1000/2);
        StatusOfJob statusOfJob = jobManager.getStatusOfJob(jobId1, submissionId1);
        assertEquals(StatusOfJob.RUNNING, statusOfJob);

        statusOfJob = jobManager.getStatusOfJob(jobId2, submissionId2);
        assertEquals(StatusOfJob.RUNNING, statusOfJob);

        statusOfJob = jobManager.getStatusOfJob(jobId3, submissionId3);
        assertEquals(StatusOfJob.QUEUING, statusOfJob);

        Thread.sleep(SLEEP_TIME*1000/2 + 1000);

        // 2 should be done, 3rd running
        statusOfJob = jobManager.getStatusOfJob(jobId1, submissionId1);
        assertEquals(StatusOfJob.FINISHED, statusOfJob);

        statusOfJob = jobManager.getStatusOfJob(jobId2, submissionId2);
        assertEquals(StatusOfJob.FINISHED, statusOfJob);

        statusOfJob = jobManager.getStatusOfJob(jobId3, submissionId3);
        assertEquals(StatusOfJob.RUNNING, statusOfJob);

        Thread.sleep(SLEEP_TIME*1000 + 1000);

        statusOfJob = jobManager.getStatusOfJob(jobId3, submissionId3);
        assertEquals(StatusOfJob.FINISHED, statusOfJob);
    }
}
