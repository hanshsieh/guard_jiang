package org.guard_jiang;

import org.guard_jiang.exception.ConflictException;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by icand on 2017/8/20.
 */
public interface GroupRoleCreator {
    @Nonnull
    GroupRoleCreator withGroupId(@Nonnull String groupId);

    @Nonnull
    GroupRoleCreator withUserId(@Nonnull String userId);

    @Nonnull
    GroupRoleCreator withRole(@Nonnull Role role);

    @Nonnull
    GroupRoleCreator withLicenseId(@Nonnull String licenseId);

    @Nonnull
    GroupRole create() throws IllegalArgumentException, ConflictException, IOException;
}
