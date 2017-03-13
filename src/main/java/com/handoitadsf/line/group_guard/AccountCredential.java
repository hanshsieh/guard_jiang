package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.io.IOException;
import io.cslinmiso.line.model.LoginCallback;

/**
 * Created by cahsieh on 1/26/17.
 */
public interface AccountCredential {
    Account login(@Nonnull LoginCallback loginCallBack) throws IOException, LoginFailureException;
    Account login() throws IOException, LoginFailureException;
}
