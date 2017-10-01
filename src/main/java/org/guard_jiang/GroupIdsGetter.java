package org.guard_jiang;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Set;

/**
 * Created by icand on 2017/8/26.
 */
public interface GroupIdsGetter {
    @Nonnull
    GroupIdsGetter withRoleCreators(@Nonnull Set<String> userIds);

    @Nonnull
    Set<String> get() throws IOException;
}
