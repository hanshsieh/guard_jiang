package org.guard_jiang;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * This class is used to create a guard account.
 */
public interface AccountCreator {
    @Nonnull
    AccountCreator withMid(@Nonnull String mid);

    @Nonnull
    AccountCreator withPartition(int partition);

    @Nonnull
    AccountCreator withCredential(@Nonnull Credential credential);

    @Nonnull
    Account create() throws IOException;
}
