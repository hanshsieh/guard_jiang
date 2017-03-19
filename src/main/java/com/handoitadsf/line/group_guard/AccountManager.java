package com.handoitadsf.line.group_guard;

import io.cslinmiso.line.model.LineContact;
import io.cslinmiso.line.model.LineGroup;
import line.thrift.Contact;
import line.thrift.Group;
import line.thrift.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by someone on 1/31/2017.
 */
class AccountManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManager.class);
    private static final int THREAD_NAME_MID_PREFIX_LEN = 5;
    private static final long SUPPORTING_MS = 1000 * 60 * 5;
    private Guard guard;
    private final Account account;
    private AccountWatcher watcher;
    private Instant lastRectifyTime = null;

    // A map from group ID to the time when I joined the group
    private Map<String, Instant> groupJoinedTime = new HashMap<>();

    public AccountManager(Guard guard, @Nonnull Account account) {
        this.guard = guard;
        this.account = account;
    }

    public synchronized void start() {
        if (watcher != null) {
            throw new IllegalStateException("Already started");
        }
        watcher = new AccountWatcher(this);
        watcher.setName("mid-" + account.getMid().substring(0, THREAD_NAME_MID_PREFIX_LEN));
        watcher.start();
    }

    public synchronized void stop() {
        if (watcher == null) {
            throw new IllegalStateException("Already stopped");
        }
        watcher.shouldStop();
        watcher = null;
    }

    private String getId() {
        return account.getMid();
    }

    @Nullable
    Instant getLastRectifyTime() {
        return lastRectifyTime;
    }

    void rectifyStatus() throws IOException {
        LOGGER.debug("Rectifying status");
        long now = System.currentTimeMillis();
        String myId = account.getMid();
        List<String> groupIdsJoined = account.getGroupIdsJoined();
        for (String groupId : groupIdsJoined) {
            Role role = guard.getRole(myId, groupId);

            boolean shouldLeave = false;
            if (Role.SUPPORTER.equals(role)) {
                Instant joinedTime = groupJoinedTime.get(groupId);
                if (joinedTime == null ||
                    now - joinedTime.toEpochMilli() > SUPPORTING_MS) {
                    shouldLeave = true;
                }
            } else if (!shouldAcceptInvitation(role)) {
                shouldLeave = true;
            }
            if (shouldLeave) {
                account.leaveGroup(groupId);
            }
        }

        List<String> groupIdsInvited = account.getGroupIdsInvited();
        for (String groupId : groupIdsInvited) {
            Role role = guard.getRole(myId, groupId);
            if (shouldAcceptInvitation(role)) {
                account.acceptGroupInvitation(groupId);
            }
            account.rejectGroupInvitation(groupId);
        }
        lastRectifyTime = Instant.now();
    }

    void onOperation(@Nonnull Operation operation) throws IOException {
        if (operation.getType() == null) {
            return;
        }
        switch (operation.getType()) {

            // Another user invites someone (maybe myself) into a group
            case NOTIFIED_INVITE_INTO_GROUP:
                onNotifiedInviteIntoGroup(
                        operation.getParam1(), operation.getParam2(), operation.getParam3());
                break;

            // I have invited another user to a group
            case INVITE_INTO_GROUP:
                onInviteIntoGroup(operation.getParam1(), operation.getParam2());
                break;

            // I have accepted a group invitation
            case ACCEPT_GROUP_INVITATION:
                onAcceptGroupInvitation(operation.getParam1());
                break;

            // Another user accepts a group invitation
            // Including via invitation link
            case NOTIFIED_ACCEPT_GROUP_INVITATION:
                onNotifiedAcceptGroupInvitation(operation.getParam1(), operation.getParam2());
                break;

            case NOTIFIED_KICKOUT_FROM_GROUP:
                onNotifiedKickOutFromGroup(
                        operation.getParam1(), operation.getParam2(), operation.getParam3());
                break;
            case LEAVE_GROUP:
                onLeaveGroup(operation.getParam1());
        }
    }

    /**
     * It should be invoked when the user him self has been invited into a group.
     * @param groupId Group ID.
     * @throws IOException IO error occurs.
     */
    private void onInvitedIntoGroup(@Nonnull String groupId)
        throws IOException {
        String myId = getId();

        // Get my role in the group
        Role myRole = guard.getRole(myId, groupId);
        if (!shouldAcceptInvitation(myRole)) {

            // I don't have role in the group
            // Cancel the invitation
            account.rejectGroupInvitation(groupId);
        } else {

            // I'm defender or supporter of the group, so I should join the group
            account.acceptGroupInvitation(groupId);
        }
    }

    /**
     * Someone has invited another user into a group.
     *
     * @param groupId   Group ID.
     * @param inviterId Inviter's ID. Not myself.
     * @param inviteeId Invitee's ID.
     * @throws IOException
     */
    private void onNotifiedInviteIntoGroup(
            @Nonnull String groupId,
            @Nonnull String inviterId,
            @Nonnull String inviteeId) throws IOException {
        LOGGER.info("{} invites {} into group {}", inviterId, inviteeId, groupId);
        String myId = getId();

        if (myId.equals(inviteeId)) {
            onInvitedIntoGroup(groupId);
            return;
        }

        GroupProfile groupProfile = guard.getGroupProfile(groupId);
        if (groupProfile == null) {
            return;
        }

        // Check if the invitee is in the black list
        BlockingEntry blockingEntry = groupProfile.getBlockingEntry(inviteeId);
        if (blockingEntry == null) {
            return;
        }

        Role inviterRole = guard.getRole(inviterId, groupId);

        account.cancelGroupInvitation(groupId, Collections.singletonList(inviteeId));

        // If the inviter is our own fellow
        if (Role.DEFENDER.equals(inviterRole) || Role.SUPPORTER.equals(inviterRole)) {
            LOGGER.warn("{} has role {} but has invited blocked user {} to group {}",
                    inviterId,
                    inviterRole,
                    inviteeId,
                    groupId);
            return;
        }
        account.kickOutFromGroup(groupId, Collections.singletonList(inviterId));
        groupProfile.addBlockedUser(inviterId);
    }

    /**
     * I have invited another user into a group.
     *
     * @param groupId Group ID.
     * @param inviteeId Invitee's ID.
     * @throws IOException IO error occurs.
     */
    private void onInviteIntoGroup(@Nonnull String groupId, @Nonnull String inviteeId)
            throws IOException {

        LOGGER.info("User {} invites {} into group {}", account.getMid(), inviteeId, groupId);

        GroupProfile groupProfile = guard.getGroupProfile(groupId);
        if (groupProfile == null) {
            account.cancelGroupInvitation(groupId, Collections.singletonList(inviteeId));
            return;
        }

        // Check if the invitee is in the black list
        BlockingEntry blockingEntry = groupProfile.getBlockingEntry(inviteeId);
        if (blockingEntry != null) {
            account.cancelGroupInvitation(groupId, Collections.singletonList(inviteeId));
        }
    }

    private void onLeaveGroup(@Nonnull String groupId) throws IOException {
        LOGGER.info("User {} leaves group {}", account.getMid(), groupId);
        groupJoinedTime.remove(groupId);
    }

    private boolean shouldAcceptInvitation(Role role) {
        return Role.DEFENDER.equals(role) || Role.SUPPORTER.equals(role);
    }

    private void updateGroupAdmin(@Nonnull String groupId) throws IOException {
        GroupProfile groupProfile = guard.getGroupProfile(groupId);
        Group group = account.getGroup(groupId);
        if (groupProfile == null || group == null) {
            return;
        }
        Contact creator = group.getCreator();
        if (creator == null) {
            return;
        }
        groupProfile.addAdminIdIfEmpty(creator.getMid());
    }

    /**
     * The user has accepted a group invitation.
     *
     * @param groupId Group ID.
     */
    private void onAcceptGroupInvitation(@Nonnull String groupId) throws IOException {

        groupJoinedTime.put(groupId, Instant.now());

        String myId = getId();

        LOGGER.info("User {} has accepted invitation into group {}", myId, groupId);

        updateGroupAdmin(groupId);

        // Get my role in the group
        Role role = guard.getRole(myId, groupId);

        // I don't have accepted the group invitation
        if (!shouldAcceptInvitation(role)) {

            // Leave the group
            account.leaveGroup(groupId);
            return;
        }

        // I'm a defender
        if (Role.DEFENDER.equals(role)) {

            // Get other defenders of the group
            Set<String> otherDefenderIds = guard.getGroupRoleMembers(groupId, Role.DEFENDER).stream()
                    .filter(id -> !myId.equals(id)).collect(Collectors.toSet());

            // Invite other defenders
            // TODO If someone is already in group, will it fails? Will it continue for the remaining groups?
            account.inviteIntoGroup(groupId, new ArrayList<>(otherDefenderIds));
        }
    }

    private void onNotifiedAcceptGroupInvitation(@Nonnull String groupId, @Nonnull String inviteeId)
        throws IOException {
        GroupProfile groupProfile = guard.getGroupProfile(groupId);
        if (groupProfile == null) {
            return;
        }

        BlockingEntry blockingEntry = groupProfile.getBlockingEntry(inviteeId);
        if (blockingEntry != null) {
            account.kickOutFromGroup(groupId, Collections.singletonList(inviteeId));
        }
    }

    /**
     * Another user has kicked out another user (maybe myself) from a group.
     *
     * @param groupId   ID of the group.
     * @param removerId ID of the user that kick out another user.
     * @param removedId ID of the user that is kicked out.
     * @throws IOException IO error occurs.
     */
    private void onNotifiedKickOutFromGroup(
            @Nonnull String groupId, @Nonnull String removerId, @Nonnull String removedId) throws IOException {

        LOGGER.info("User {} has kicked out user {} from group {}", removerId, removedId, groupId);

        String myId = getId();

        Role myRole = guard.getRole(myId, groupId);

        // If I'm not a defender or supporter
        if (!Role.DEFENDER.equals(myRole) && !Role.SUPPORTER.equals(myRole)) {

            // Do nothing
            return;
        }

        // If the remover is our own guy
        Role removerRole = guard.getRole(removerId, groupId);
        if (Role.DEFENDER.equals(removerRole) || Role.SUPPORTER.equals(removerRole)) {
            return;
        }

        GroupProfile groupProfile = guard.getGroupProfile(groupId);

        if (groupProfile == null) {
            return;
        }

        // If remover is admin of the group
        if (getGroupAdmins(groupProfile).contains(removerId)) {

            // Do nothing
            return;
        }

        if (!myId.equals(removedId)) {

            // Kick out remover
            account.kickOutFromGroup(groupId, Collections.singletonList(removerId));

            // Invite supporters and the removed user
            Set<String> supporters = guard.getGroupRoleMembers(groupId, Role.SUPPORTER);
            List<String> invitees = new ArrayList<>(supporters);
            if (!supporters.contains(removedId)) {
                invitees.add(removedId);
            }
            invitees.remove(myId);
            account.inviteIntoGroup(groupId, invitees);
        } else {
            LOGGER.info("{} {} has been kicked out from group {}", myRole, myId, groupId);
        }

        groupProfile.addBlockedUser(removerId);
    }

    @Nonnull
    private Set<String> getGroupAdmins(@Nonnull GroupProfile groupProfile) throws IOException {
        Set<String> admins = groupProfile.getAdminIds();

        // If not admin is configured
        if (admins.isEmpty()) {

            // Use group creator by default
            Group group = account.getGroup(groupProfile.getGroupId());

            // If I can get the group information (I'm in the group or I'm invited),
            // and the creator of the group is available
            // p.s. If the creator of the group leaves the group before, then the creator
            // may be null.
            if (group != null && group.getCreator() != null) {
                String creatorId = group.getCreator().getMid();
                groupProfile.addAdminIdIfEmpty(creatorId);
                return Collections.singleton(creatorId);
            }
            return Collections.emptySet();
        } else {
            return admins;
        }
    }

    @Nonnull
    public Account getAccount() {
        return account;
    }

}
