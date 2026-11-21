package eppic.rest.jobs;

import eppic.model.shared.StatusOfJob;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Tests that job statuses are shared between replicas through a {@link JobStatusStore}. Two job managers sharing
 * an in-memory store stand for two replicas sharing the Mongo store.
 */
public class TestSharedJobStatus {

    private static final File jobDir = new File(System.getProperty("java.io.tmpdir"));
    private static File script;

    private static final int SLEEP_TIME = 3;

    /**
     * In-memory store, with heartbeats that can be backdated to simulate a dead replica
     */
    static class InMemoryJobStatusStore implements JobStatusStore {
        final Map<String, JobStatusRecord> records = new ConcurrentHashMap<>();

        @Override
        public void setStatus(String submissionId, StatusOfJob status) {
            records.put(submissionId, new JobStatusRecord(status, "test-host", new Date()));
        }

        @Override
        public void heartbeat(Collection<String> submissionIds) {
            for (String id : submissionIds) {
                records.computeIfPresent(id, (k, r) -> new JobStatusRecord(r.status(), r.host(), new Date()));
            }
        }

        @Override
        public JobStatusRecord get(String submissionId) {
            return records.get(submissionId);
        }

        void backdateHeartbeat(String submissionId, long millisAgo) {
            records.computeIfPresent(submissionId, (k, r) ->
                    new JobStatusRecord(r.status(), r.host(), new Date(System.currentTimeMillis() - millisAgo)));
        }
    }

    @BeforeClass
    public static void init() throws IOException {
        script = new File(jobDir, "eppicSharedJobStatusTest.sh");
        try (PrintWriter pw = new PrintWriter(script)) {
            pw.println("#!/bin/sh");
            pw.println("sleep " + SLEEP_TIME);
        }
        script.setExecutable(true);
    }

    @AfterClass
    public static void destroy() {
        script.delete();
    }

    private static File jobDir(String id) {
        File dir = new File(jobDir, id);
        dir.mkdir();
        return dir;
    }

    @Test
    public void testOtherReplicaSeesStatus() throws Exception {
        InMemoryJobStatusStore store = new InMemoryJobStatusStore();
        NativeJobManager runner = new NativeJobManager(jobDir.getAbsolutePath(), 2, store);
        NativeJobManager other = new NativeJobManager(jobDir.getAbsolutePath(), 2, store);
        try {
            String id = "shared-status-1";
            runner.startJob(id, List.of(script.toString()), jobDir(id), 1, null, null);

            // recorded synchronously on submission
            assertNotNull(store.get(id));
            assertNotEquals(StatusOfJob.NONEXISTING, other.getStatusOfJob(id));

            Thread.sleep(SLEEP_TIME * 1000 / 2);
            assertEquals(StatusOfJob.RUNNING, other.getStatusOfJob(id));

            Thread.sleep(SLEEP_TIME * 1000 / 2 + 1000);
            assertEquals(StatusOfJob.FINISHED, runner.getStatusOfJob(id));
            assertEquals(StatusOfJob.FINISHED, other.getStatusOfJob(id));
        } finally {
            runner.close();
            other.close();
        }
    }

    @Test
    public void testOtherReplicaSeesError() throws Exception {
        InMemoryJobStatusStore store = new InMemoryJobStatusStore();
        NativeJobManager runner = new NativeJobManager(jobDir.getAbsolutePath(), 2, store);
        NativeJobManager other = new NativeJobManager(jobDir.getAbsolutePath(), 2, store);
        try {
            String id = "shared-status-2";
            runner.startJob(id, List.of("unexistingScript.sh"), jobDir(id), 1, null, null);
            Thread.sleep(1000);
            assertEquals(StatusOfJob.ERROR, other.getStatusOfJob(id));
        } finally {
            runner.close();
            other.close();
        }
    }

    @Test
    public void testStaleHeartbeatIsError() throws Exception {
        InMemoryJobStatusStore store = new InMemoryJobStatusStore();
        NativeJobManager other = new NativeJobManager(jobDir.getAbsolutePath(), 2, store);
        try {
            // a replica recorded the job as running, then disappeared without a trace
            String id = "shared-status-3";
            store.setStatus(id, StatusOfJob.RUNNING);
            assertEquals(StatusOfJob.RUNNING, other.getStatusOfJob(id));

            store.backdateHeartbeat(id, NativeJobManager.HEARTBEAT_STALE_MS + 1000);
            assertEquals(StatusOfJob.ERROR, other.getStatusOfJob(id));

            // a final status doesn't depend on heartbeats
            store.setStatus(id, StatusOfJob.FINISHED);
            store.backdateHeartbeat(id, NativeJobManager.HEARTBEAT_STALE_MS + 1000);
            assertEquals(StatusOfJob.FINISHED, other.getStatusOfJob(id));
        } finally {
            other.close();
        }
    }

    @Test
    public void testCloseMarksUnfinishedJobsAsError() throws Exception {
        InMemoryJobStatusStore store = new InMemoryJobStatusStore();
        // a single worker: the 1st job runs, the 2nd is queued
        NativeJobManager runner = new NativeJobManager(jobDir.getAbsolutePath(), 1, store);
        NativeJobManager other = new NativeJobManager(jobDir.getAbsolutePath(), 1, store);
        try {
            String running = "shared-status-4";
            String queued = "shared-status-5";
            runner.startJob(running, List.of(script.toString()), jobDir(running), 1, null, null);
            runner.startJob(queued, List.of(script.toString()), jobDir(queued), 1, null, null);
            Thread.sleep(1000);
            assertEquals(StatusOfJob.RUNNING, other.getStatusOfJob(running));
            assertEquals(StatusOfJob.QUEUING, other.getStatusOfJob(queued));

            // the replica shuts down (e.g. pod terminated)
            runner.close();

            assertEquals(StatusOfJob.ERROR, other.getStatusOfJob(running));
            assertEquals(StatusOfJob.ERROR, other.getStatusOfJob(queued));
        } finally {
            other.close();
        }
    }
}
