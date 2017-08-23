package org.guard_jiang;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Created by icand on 2017/8/20.
 */
public interface GroupRolesGetter {
    @Nonnull
    GroupRolesGetter withGroupId(@Nonnull String groupId);

    @Nonnull
    GroupRolesGetter withRole(@Nonnull Role role);

    @Nonnull
    GroupRolesGetter withUserId(@Nonnull String userId);

    @Nonnull
    List<GroupRole> get() throws IOException;
}
