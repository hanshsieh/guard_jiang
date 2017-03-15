package com.handoitadsf.line.group_guard;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineGroup;
import line.thrift.Operation;
import line.thrift.Profile;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cahsieh on 1/26/17.
 */
public class Account {
    private static final Logger LOGGER = LoggerFactory.getLogger(Account.class);
    private final LineClient client;
    public Account(@Nonnull LineClient client) {
        try {
            client.checkAuth();
        } catch (Exception ex) {
            throw new IllegalStateException("The client has not yet logged in", ex);
        }
        this.client = client;
    }
    @Nonnull
    public Profile getProfile() throws IOException {
        try {
            return client.getProfile();
        } catch (Exception ex) {
            throw new IOException("Fail to get profile", ex);
        }
    }

    public List<LineGroup> getGroups() throws IOException {
        try {
            List<LineGroup> groups = client.getGroups();
            if (groups == null) {
                client.refreshGroups();
                groups = client.getGroups();
            }
            return groups;
        } catch (Exception ex) {
            throw new IOException("Fail to get groups", ex);
        }
    }

    @Nullable
    public LineGroup getGroup(@Nonnull String groupId) throws IOException {
        for (LineGroup group : getGroups()) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    @Nonnull
    public List<Operation> fetchOperations(long revision, int count) throws IOException {
        do {
            try {
                return client.getApi().fetchOperations(revision, count);
            } catch (TTransportException ex) {
                if (ex.getMessage().contains("HTTP Response code: 410")) {
                    LOGGER.debug("Receive 410 when fetching operations, sending request again");
                    continue;
                }
                throw new IOException("Fail to fetch operations", ex);
            } catch (Exception ex) {
                throw new IOException("Fail to fetch operations", ex);
            }
        } while (true);
    }

    public long getLastOpRevision() throws IOException {
        try {
            return client.getApi().getLastOpRevision();
        } catch (Exception ex) {
            throw new IOException("Fail to get last operation's revision", ex);
        }
    }

    public void acceptGroupInvitation(@Nonnull String groupId)
        throws IOException {
        try {
            client.getApi().acceptGroupInvitation(0, groupId);
        } catch (Exception ex) {
            throw new IOException("Fail to accept group invitation");
        }
    }

    public void cancelGroupInvitation(@Nonnull String groupId, @Nonnull List<String> contactIds)
            throws IOException {
        try {
            client.getApi().cancelGroupInvitation(0, groupId, contactIds);
        } catch (Exception ex){
            throw new IOException("Fail to cancel group invitation. groupId: "
                    + groupId + ", contacts: " + contactIds);
        }
    }

    public void leaveGroup(@Nonnull String groupId) throws IOException {
        try {
            client.getApi().leaveGroup(groupId);
        } catch (Exception ex) {
            throw new IOException("Fail to leave a group", ex);
        }
    }

    public void inviteIntoGroup(@Nonnull String groupId, @Nonnull List<String> contactIds)
        throws IOException {
        try {
            client.inviteIntoGroup(groupId, contactIds);
        } catch (Exception ex) {
            throw new IOException("Fail to invite into groups. group ID: " + groupId + ", contacts: " + contactIds);
        }
    }

    public void kickOutFromGroup(@Nonnull String groupId, @Nonnull List<String> contactIds)
        throws IOException {
        try {
            client.getApi().kickoutFromGroup(0, groupId, contactIds);
        } catch (Exception ex) {
            throw new IOException("Fail to kick out from group. groupId: "
                    + groupId + ", contacts: " + contactIds, ex);
        }
    }

    public void refreshGroups() throws IOException {
        try {
            client.refreshGroups();
        } catch (Exception ex) {
            throw new IOException("Fail to refresh groups");
        }
    }

    public String getAuthToken() {
        return client.getAuthToken();
    }

    public String getCertificate() {
        return client.getCertificate();
    }
}
