package com.handoitadsf.line.group_guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by cahsieh on 1/27/17.
 */
public class PrioritizedExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrioritizedExecutor.class);
    private static final int QUEUE_INIT_CAPACITY = 10;
    private boolean isShutdown = false;
    private final PriorityBlockingQueue<ExecutorEntry> queue =
        new PriorityBlockingQueue<>(QUEUE_INIT_CAPACITY, (entry1, entry2) -> {
            long diff = entry2.adjustedPriority - entry1.adjustedPriority;
            if (diff == 0) {
                return 0;
            } else if (diff < 0) {
                return -1;
            } else {
                return 1;
        }});
    private final Thread thread;
    private static final long WAIT_TIMEOUT = 1000;
    private long sequenceId = 0;
    public PrioritizedExecutor() {
        thread = new ExecutorThread();
        thread.setDaemon(true);
        thread.start();
    }
    public void shutdown() {
        thread.interrupt();
        isShutdown = true;
    }
    public void submit(PrioritizedTask task) {

        // We use a rough implementation to prevent a task from starving.
        // Every task is assigned a sequence ID, which is increased by one
        // for each task.
        // The priority of a new task will be subtracted with the max difference
        // between the sequence ID assigned to the task and sequence ID of the oldest task in the queue.
        Iterator<ExecutorEntry> itr = queue.iterator();
        ++sequenceId;
        long minSequenceId = sequenceId;
        while (itr.hasNext()) {
            minSequenceId = Math.min(minSequenceId, itr.next().sequenceId);
        }

        ExecutorEntry entry = new ExecutorEntry();
        entry.task = task;
        entry.sequenceId = sequenceId;
        entry.adjustedPriority = task.getPriority() - (sequenceId - minSequenceId);
        queue.offer(entry);
    }

    private static class ExecutorEntry {
        public PrioritizedTask task;
        public long sequenceId;
        public long adjustedPriority;
    }
    private class ExecutorThread extends Thread {
        @Override
        public void run() {
            try {
                LOGGER.debug("Executor starts running");
                while (!isShutdown) {
                    ExecutorEntry entry;
                    try {
                        entry = queue.poll(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
                        if (entry == null) {
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        continue;
                    }
                    try {
                        entry.task.run();
                    } catch (Exception ex) {
                        LOGGER.debug("Exception is thrown from task {}: ", entry.task.getClass().getName(), ex);
                    }
                    ++sequenceId;
                }
            } finally {
                LOGGER.debug("Executor thread is shutdown");
            }
        }
    }
}
