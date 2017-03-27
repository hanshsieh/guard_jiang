package com.handoitadsf.line.group_guard;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineGroup;
import line.thrift.Contact;
import line.thrift.ContactSetting;
import line.thrift.Group;
import line.thrift.Operation;
import line.thrift.Profile;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cahsieh on 1/26/17.
 */
public class Account {
    private static final Logger LOGGER = LoggerFactory.getLogger(Account.class);
    private final LineClient client;
    private String mid;
    private Instant authTokenLastRefreshTime = null;
    private AccountCredential credential;
    public Account(@Nonnull AccountCredential credential) throws IOException {
        this.client = new LineClient();
        this.credential = credential;
    }

    public void login() throws IOException, LoginFailureException {
        try {
            boolean loggedIn = false;
            if (credential.getAuthToken() != null) {
                LOGGER.debug("Trying to login with auth token");
                try {
                    client.loginWithAuthToken(credential.getAuthToken());
                    loggedIn = true;
                } catch (TException ex) {
                    LOGGER.debug("Fail to login with auth token", ex);
                }
            }
            if (!loggedIn) {
                client.login(
                        credential.getEmail(),
                        credential.getPassword(),
                        credential.getCertificate(),
                        null
                );
            }
            credential.setAuthToken(client.getAuthToken());
            credential.setCertificate(client.getCertificate());
            mid = client.getProfile().getMid();
            authTokenLastRefreshTime = Instant.now();
        } catch (TException ex) {
            throw new LoginFailureException("Fail to login with email and password", ex);
        } catch (Exception ex) {
            throw new IOException("Fail to login", ex);
        }
    }

    @Nonnull
    public String getMid() {
        if (mid == null) {
            throw new IllegalStateException("Not yet logged in");
        }
        return mid;
    }

    @Nullable
    public Group getGroup(@Nonnull String groupId) throws IOException {
        try {
            LOGGER.debug("Getting info of group {} for user {}", groupId, mid);
            List<Group> group = client.getApi().getGroups(Collections.singletonList(groupId));
            if (group.isEmpty()) {
                return null;
            }
            return group.get(0);
        } catch (TException ex) {
            throw new IOException("Fail to get info of group " + groupId, ex);
        }
    }

    public List<String> getGroupIdsJoined() throws IOException {
        try {
            LOGGER.debug("Getting IDs of groups joined of user {}", mid);
            return client.getApi().getGroupIdsJoined();
        } catch (TException ex) {
            throw new IOException("Fail to get groups joined", ex);
        }
    }

    public List<String> getGroupIdsInvited() throws IOException {
        try {
            LOGGER.debug("Getting IDs of groups invited of user {}", mid);
            return client.getApi().getGroupIdsInvited();
        } catch (TException ex) {
            throw new IOException("Fail to get groups invited", ex);
        }
    }

    @Nonnull
    public List<Operation> fetchOperations(long revision, int count) throws IOException {
        do {
            try {
                LOGGER.debug("Fetching operations of user {} with revision {}", mid, revision);
                return client.getApi().fetchOperations(revision, count);
            } catch (TTransportException ex) {
                if (ex.getMessage().contains("HTTP Response code: 410")) {
                    LOGGER.debug("Receive 410 when fetching operations");
                    return Collections.emptyList();
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
            LOGGER.debug("User {} accepts group {}'s invitation", mid, groupId);
            client.getApi().acceptGroupInvitation(0, groupId);
        } catch (Exception ex) {
            throw new IOException("Fail to accept group invitation");
        }
    }

    public void rejectGroupInvitation(@Nonnull String groupId)
            throws IOException {
        try {
            LOGGER.debug("User {} rejects group {}'s invitation", mid, groupId);
            client.getApi().rejectGroupInvitation(0, groupId);
        } catch (Exception ex){
            throw new IOException("Fail to reject group invitation. groupId: "
                    + groupId);
        }
    }

    public void cancelGroupInvitation(@Nonnull String groupId, @Nonnull List<String> contactIds)
            throws IOException {
        if (contactIds.isEmpty()) {
            return;
        }
        try {
            LOGGER.debug("User {} cancel group {}'s invitation for users {}", mid, groupId, contactIds);
            client.getApi().cancelGroupInvitation(0, groupId, contactIds);
        } catch (Exception ex){
            throw new IOException("Fail to cancel group invitation. groupId: "
                    + groupId + ", contacts: " + contactIds);
        }
    }

    public void leaveGroup(@Nonnull String groupId) throws IOException {
        try {
            LOGGER.debug("User {} leave group {}", mid, groupId);
            client.getApi().leaveGroup(groupId);
        } catch (Exception ex) {
            throw new IOException("Fail to leave a group", ex);
        }
    }

    public void inviteIntoGroup(@Nonnull String groupId, @Nonnull Collection<String> contactIds)
        throws IOException {
        if (contactIds.isEmpty()) {
            return;
        }
        try {
            LOGGER.debug("User {} invites {} into group {}", mid, contactIds, groupId);
            client.inviteIntoGroup(groupId, new ArrayList<>(contactIds));
        } catch (Exception ex) {
            throw new IOException("Fail to invite into groups. group ID: " + groupId + ", contacts: " + contactIds);
        }
    }

    public void kickOutFromGroup(@Nonnull String groupId, @Nonnull String contactId)
        throws IOException {
        try {
            LOGGER.debug("User {} kick out {} from group {}", mid, contactId, groupId);
            client.getApi().kickoutFromGroup(0, groupId, Collections.singletonList(contactId));
        } catch (Exception ex) {
            throw new IOException("Fail to kick out from group. groupId: "
                    + groupId + ", contact: " + contactId, ex);
        }
    }

    public void addContact(String contact) throws IOException {
        try {
            LOGGER.debug("User {} is adding {} to contact", mid, contact);
            client.getApi().findAndAddContactsByMid(0, contact);
        } catch (Exception ex) {
            throw new IOException("Fail to add " + contact + " to contact", ex);
        }
    }

    @Nonnull
    public List<String> getContactIds() throws IOException {
        try {
            return client.getApi().getAllContactIds();
        } catch (Exception ex) {
            throw new IOException("Fail to get contacts", ex);
        }
    }

    public List<Contact> getContacts(List<String> contacts) throws IOException {
        try {
            return client.getApi().getContacts(contacts);
        } catch (Exception ex) {
            throw new IOException("Fail to get contacts for " + contacts, ex);
        }
    }

    public void removeContact(@Nonnull String contact) throws IOException {
        try {
            LOGGER.debug("User {} is removing {} from contacts", mid, contact);
            client.getApi().updateContactSetting(0, contact, ContactSetting.CONTACT_SETTING_DELETE, "true");
        } catch (Exception ex) {
            throw new IOException("Fail to remove contact " + contact, ex);
        }
    }

    public String getAuthToken() {
        return client.getAuthToken();
    }

    public String getCertificate() {
        return credential.getCertificate();
    }

    @Nonnull
    public Instant getAuthTokenLastRefreshTime() {
        if (authTokenLastRefreshTime == null) {
            throw new IllegalStateException("Not yet logged in");
        }
        return authTokenLastRefreshTime;
    }

    public void refreshAuthToken() throws IOException {
        try {
            LOGGER.info("Refreshing auth token of user {}", mid);
            credential.setAuthToken(null);
            login();
        } catch (Exception ex) {
            throw new IOException("Fail to refresh auth token", ex);
        }
    }
}
