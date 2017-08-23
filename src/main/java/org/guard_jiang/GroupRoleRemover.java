package org.guard_jiang;

import org.guard_jiang.services.storage.sql.SqlSessionFactory;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by icand on 2017/8/20.
 */
public interface GroupRoleRemover {

    @Nonnull
    GroupRoleRemover withGroupId(@Nonnull String groupId);

    @Nonnull
    GroupRoleRemover withUserId(@Nonnull String userId);

    /**
     * Remove the group role.
     *
     * @return True if the group role is removed. False if the group role doesn't exist.
     * @throws IOException IO error occurs.
     */
    boolean remove() throws IOException;
}
