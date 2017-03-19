package com.handoitadsf.line.group_guard;

import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by cahsieh on 1/27/17.
 */
public abstract class PrioritizedTask {
    private int priority;
    private Listener listener;
    public PrioritizedTask(int priority) {
        this.priority = priority;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public abstract void run() throws Exception;

    public void start() {
        if (listener != null) {
            listener.onStart();
        }
        try {
            run();
            if (listener != null) {
                listener.onStop();
            }
        } catch (Throwable ex) {
            if (listener != null) {
                listener.onStop(ex);
            }
        }
    }

    public int getPriority() {
        return priority;
    }

    public static abstract class Listener {
        public void onStart() {}
        public void onStop(@Nonnull Throwable error) {}
        public void onStop() {}
    }
}
