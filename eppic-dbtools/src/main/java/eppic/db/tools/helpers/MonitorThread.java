package eppic.db.tools.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class MonitorThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MonitorThread.class);

    private ThreadPoolExecutor executor;
    private int seconds;
    private boolean run = true;

    public MonitorThread(ThreadPoolExecutor executor, int delay) {
        this.executor = executor;
        this.seconds = delay;
    }

    public void shutDown() {
        this.run = false;
    }

    @Override
    public void run() {
        while (run) {
            logger.info(
                    String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s, remaining queue capacity: %d",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated(),
                            this.executor.getQueue().remainingCapacity()));

            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }
}
