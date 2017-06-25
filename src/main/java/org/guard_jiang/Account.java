package org.guard_jiang;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.cslinmiso.line.model.LineClient;
import line.thrift.*;
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
    private final Credential credential;
    public Account(@Nonnull Credential credential) throws IOException {
        this.client = new LineClient();
        this.credential = credential;
    }

    public void login() throws IOException, LoginFailureException {
        try {
            boolean loggedIn = false;
            if (credential.getAuthToken() != null) {
                LOGGER.debug("Trying to login with auth token. email: {}", credential.getEmail());
                try {
                    client.loginWithAuthToken(credential.getAuthToken());
                    loggedIn = true;
                } catch (TException ex) {
                    LOGGER.debug("Fail to login with auth token: {}", ex.getMessage());
                }
            }
            if (!loggedIn) {
                LOGGER.debug("Cannot login with auth token, trying to login with certificate only. "
                             + "email: {}",
                             credential.getEmail(),
                             credential.getCertificate());
                client.login(
                        credential.getEmail(),
                        credential.getPassword(),
                        credential.getCertificate(),
                        null
                );
            }
            credential.setAuthToken(client.getAuthToken());
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

    public void sendMessage(@Nonnull Message message) throws IOException {
        try {
            LOGGER.debug("User {} is sending message to {}", mid, message.getToId());
            client.getApi().sendMessage(0, message);
        } catch (Exception ex) {
            throw new IOException("Fail to send message to " + message.getToId());
        }
    }

    public void sendTextMessage(@Nonnull String text, @Nonnull String toId) throws IOException {
        Message message = new Message();
        message.setText(text);
        message.setToId(toId);
        sendMessage(message);
    }

    public void sendContactMessage(@Nonnull String toId, @Nonnull String contactMid) throws IOException {
        Message message = new Message();
        message.setContentType(ContentType.CONTACT);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("mid", contactMid);
        message.setContentMetadata(metadata);
        message.setToId(toId);
        sendMessage(message);
    }

    public String getAuthToken() {
        return client.getAuthToken();
    }

    public String getCertificate() {
        return credential.getCertificate();
    }

    @Nonnull
    public Contact getContact(@Nonnull String contactId) throws IOException {
        try {
            return client.getApi().findContactByUserid(contactId);
            // TODO Handle not found case
        } catch (Exception ex) {
            throw new IOException("Fail to find contact by ID " + contactId);
        }
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

    @Nonnull
    public Group findGroupByTicket(@Nonnull String ticketId) throws IOException {
        try {
            return client.getApi().findGroupByTicket(ticketId);
        } catch (TException ex) {
            // TODO handle not found case
            throw new IOException("Fail to find group by ticket", ex);
        }
    }
}
