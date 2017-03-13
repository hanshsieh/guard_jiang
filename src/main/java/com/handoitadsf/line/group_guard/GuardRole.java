package com.handoitadsf.line.group_guard;

import java.io.IOException;
import java.util.List;

import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineGroup;
import line.thrift.Operation;
import line.thrift.Profile;

import javax.annotation.Nonnull;

/**
 * Created by cahsieh on 1/27/17.
 */
public abstract class GuardRole {
    private final LineClient client;
    public GuardRole(@Nonnull LineClient client) {
        this.client = client;
    }
    @Nonnull
    protected LineClient getClient() {
        return client;
    }
    @Nonnull
    List<LineGroup> getGroups() throws IOException {
        List<LineGroup> groups = client.getGroups();
        if (groups != null) {
            return groups;
        }
        try {
            client.refreshGroups();
        } catch (Exception ex) {
            throw new IOException("Fail to get group list", ex);
        }
        groups = client.getGroups();
        assert groups != null;
        return groups;
    }
    @Nonnull
    Profile getProfile() throws IOException {
        try {
            return client.getProfile();
        } catch (Exception ex) {
            throw new IOException("Fail to get profile", ex);
        }
    }
    void inviteIntoGroup(@Nonnull String groupId, @Nonnull List<String> contactIds) throws IOException {
        try {
            client.inviteIntoGroup(groupId, contactIds);
        } catch (Exception ex) {
            throw new IOException("Fail to invite users into group " + groupId, ex);
        }
    }
    @Nonnull
    List<Operation> fetchOperations(long revision, int count) throws IOException {
        try {
            return client.getApi().fetchOperations(revision, count);
        } catch (Exception ex) {
            throw new IOException("Fail to fetch operations", ex);
        }
    }
    long getRevision() {
        return client.getRevision();
    }
    void setRevision(long revision) {
        client.setRevision(revision);
    }
    void refreshGroups() throws IOException {
        try {
            client.refreshGroups();
        } catch (Exception ex) {
            throw new IOException("Fail to refresh groups", ex);
        }
    }
}
