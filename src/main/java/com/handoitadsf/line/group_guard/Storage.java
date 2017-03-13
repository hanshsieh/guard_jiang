package com.handoitadsf.line.group_guard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 2/3/2017.
 */
public interface Storage {
    @Nonnull
    Collection<AccountCredential> getAccountCredentials() throws IOException;

    @Nonnull
    Map<Relation, Role> getRoles() throws IOException;
}
