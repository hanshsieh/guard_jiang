package com.handoitadsf.line.group_guard;

import java.io.IOException;

/**
 * Created by cahsieh on 1/26/17.
 */
public interface AccountCredential {
    Account login() throws IOException, LoginFailureException;
}
