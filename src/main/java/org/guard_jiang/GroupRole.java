package org.guard_jiang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * It represents a user's role in a group.
 */
public interface GroupRole {

    @Nonnull
    String getId();

    @Nonnull
    String getGroupId();

    @Nonnull
    String getUserId();

    @Nonnull
    Role getRole();

    @Nonnull
    String getLicenseId();
}
