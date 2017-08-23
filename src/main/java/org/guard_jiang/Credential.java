package org.guard_jiang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import io.cslinmiso.line.model.LoginCallback;

/**
 * Created by cahsieh on 1/26/17.
 */
public interface Credential {
    @Nonnull
    String getEmail();

    @Nonnull
    String getPassword();

    @Nonnull
    String getCertificate();

    @Nullable
    String getAuthToken();

    @Nonnull
    CredentialUpdater update() throws IOException;
}
