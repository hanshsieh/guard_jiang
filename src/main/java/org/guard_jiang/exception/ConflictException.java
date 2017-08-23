package org.guard_jiang.exception;

import javax.annotation.Nonnull;

/**
 * Created by icand on 2017/8/20.
 */
public class ConflictException extends Exception {
    public ConflictException(@Nonnull String message) {
        super(message);
    }

    public ConflictException(@Nonnull String message, @Nonnull Throwable throwable) {
        super(message, throwable);
    }
}
