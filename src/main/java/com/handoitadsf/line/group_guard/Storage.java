package com.handoitadsf.line.group_guard;

import com.sun.istack.internal.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by someone on 2/3/2017.
 */
public interface Storage {

    @Nonnull
    Set<String> getAccountIds() throws IOException;

    @Nullable
    AccountCredential getAccountCredential(@Nonnull String mid) throws IOException;

    void setAccountCredential(@Nonnull String mid, @Nonnull AccountCredential credential) throws IOException;

    @Nonnull
    Map<String, Role> getGroupRoles(@Nonnull String groupId) throws IOException;

    @Nonnull
    Set<String> getGroupAdminIds(@Nonnull String groupId) throws IOException;

    void setGroupAdmins(@Nonnull String groupId, @Nonnull Set<String> admins) throws IOException;

    @Nonnull
    Collection<BlockingRecord> getGroupBlockingRecords(@Nonnull String groupId) throws IOException;

    void putGroupBlockingRecord(@Nonnull String groupId, @Nonnull BlockingRecord blockingRecord) throws IOException;
}
