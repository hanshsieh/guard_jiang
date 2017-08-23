package org.guard_jiang.exception;

import javax.annotation.Nonnull;

/**
 * Created by cahsieh on 1/26/17.
 */
public class LoginFailureException extends Exception {
    public LoginFailureException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }

    public LoginFailureException(@Nonnull String message) {
        super(message);
    }
}
