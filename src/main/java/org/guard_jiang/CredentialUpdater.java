package org.guard_jiang;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by icand on 2017/8/19.
 */
public interface CredentialUpdater {
    @Nonnull
    CredentialUpdater withEmail(@Nonnull String email);

    @Nonnull
    CredentialUpdater withPassword(@Nonnull String password);

    @Nonnull
    CredentialUpdater withCertificate(@Nonnull String certificate);

    @Nonnull
    CredentialUpdater withAuthToken(@Nonnull String authToken);

    @Nonnull
    Credential update() throws IOException;
}
