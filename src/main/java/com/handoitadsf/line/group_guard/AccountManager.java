package com.handoitadsf.line.group_guard;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by someone on 1/31/2017.
 */
class AccountManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManager.class);
    private static final int THREAD_NAME_MID_PREFIX_LEN = 5;
    private static final long SUPPORTING_MS = 1000 * 60 * 5;
    private static final String SEPARATOR = "\u001E";
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

    public boolean isStarted() {
        return watcher != null;
    }

    public synchronized void start() {
        if (isStarted()) {
            throw new IllegalStateException("Already started");
        }
        watcher = new AccountWatcher(this);
        watcher.setName("mid-" + account.getMid().substring(0, THREAD_NAME_MID_PREFIX_LEN));
        watcher.start();
    }

    public synchronized void stop() {
        if (!isStarted()) {
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

        // Check the joined groups
        checkJoinedGroups();

        // Check the invited groups
        checkInvitedGroups();

        lastRectifyTime = Instant.now();
    }

    private void checkInvitedGroups() throws IOException {
        String myId = account.getMid();
        List<String> groupIdsInvited = account.getGroupIdsInvited();
        for (String groupId : groupIdsInvited) {
            GuardGroup group = guard.getGroup(groupId);
            Role myRole = group.getRoles().get(myId);
            if (shouldAcceptInvitation(myRole)) {
                account.acceptGroupInvitation(groupId);
            } else {
                account.rejectGroupInvitation(groupId);
            }
        }
    }

    private void checkJoinedGroups() throws IOException {
        String myId = account.getMid();
        long now = System.currentTimeMillis();
        List<String> groupIdsJoined = account.getGroupIdsJoined();
        for (String groupId : groupIdsJoined) {
            GuardGroup group = guard.getGroup(groupId);

            Role myRole = group.getRoles().get(myId);

            boolean shouldLeave = false;

            if (!shouldAcceptInvitation(myRole)) {
                shouldLeave = true;
            } else {
                kickoutBlockedMembers(group);
                inviteDefenders(group);
                if (Role.SUPPORTER.equals(myRole)) {
                    Instant joinedTime = groupJoinedTime.get(groupId);
                    if (joinedTime == null ||
                            now - joinedTime.toEpochMilli() > SUPPORTING_MS) {
                        shouldLeave = true;
                    }
                }
            }

            if (shouldLeave) {
                account.leaveGroup(groupId);
            } else {
                updateGroupAdmin(groupId);
            }
        }

        Iterator<Map.Entry<String, Instant>> itr = groupJoinedTime.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Instant> entry = itr.next();

            if (groupIdsJoined.contains(entry.getKey())) {
                itr.remove();
            }
        }
    }

    private void kickoutBlockedMembers(@Nonnull GuardGroup guardGroup) throws IOException {
        String groupId = guardGroup.getId();
        Group group = account.getGroup(groupId);
        if (group == null) {
            return;
        }
        Set<String> memberIds = group.getMembers()
            .stream()
            .map(Contact::getMid)
            .collect(Collectors.toSet());
        Set<String> blockedMembers = guardGroup.getBlockingRecords()
                .stream()
                .map(BlockingRecord::getAccountId)
                .filter(memberIds::contains)
                .collect(Collectors.toSet());
        for (String blockedMember : blockedMembers) {
            account.kickOutFromGroup(groupId, blockedMember);
        }
    }

    void onOperation(@Nonnull Operation operation) throws IOException {
        if (operation.getType() == null) {
            return;
        }
        switch (operation.getType()) {

            // Another user invites someone (maybe myself) into a group
            case NOTIFIED_INVITE_INTO_GROUP:
                onNotifiedInviteIntoGroup(
                        operation.getParam1(), operation.getParam2(), Arrays.asList(operation.getParam3().split(SEPARATOR)));
                break;

            // I have invited another user to a group
            case INVITE_INTO_GROUP:
                onInviteIntoGroup(operation.getParam1(), Arrays.asList(operation.getParam2().split(SEPARATOR)));
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
                        operation.getParam1(),
                        operation.getParam2(),
                        Arrays.asList(operation.getParam3().split(SEPARATOR)));
                break;
            case LEAVE_GROUP:
                onLeaveGroup(operation.getParam1());
        }
    }

    /**
     * It should be invoked when the user him self has been invited into a group.
     *
     * @param groupId Group ID.
     * @throws IOException IO error occurs.
     */
    private void onInvitedIntoGroup(@Nonnull String groupId)
        throws IOException {
        String myId = getId();

        // Get my role in the group
        GuardGroup group = guard.getGroup(groupId);
        Map<String, Role> roles = group.getRoles();
        Role myRole = roles.get(myId);
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
     * @param inviteeIds List of invitee ID's.
     * @throws IOException
     */
    private void onNotifiedInviteIntoGroup(
            @Nonnull String groupId,
            @Nonnull String inviterId,
            @Nonnull List<String> inviteeIds) throws IOException {
        LOGGER.info("Another user {} invites {} into group {}", inviterId, inviteeIds, groupId);
        String myId = getId();

        if (inviteeIds.contains(myId)) {
            onInvitedIntoGroup(groupId);
        }

        // Filter out the invitee's that are in the black list
        GuardGroup group = guard.getGroup(groupId);
        List<String> blockedInvitees = group.getBlockingRecords()
            .stream()
            .map(BlockingRecord::getAccountId)
            .filter(inviteeIds::contains)
            .collect(Collectors.toList());
        if (blockedInvitees.isEmpty()) {
            return;
        }

        account.cancelGroupInvitation(groupId, blockedInvitees);

        Role inviterRole = group.getRoles().get(inviterId);

        // If the inviter is our own fellow
        if (Role.DEFENDER.equals(inviterRole) || Role.SUPPORTER.equals(inviterRole)) {
            LOGGER.warn("{} has role {} but has invited blocked users {} to group {}",
                    inviterId,
                    inviterRole,
                    blockedInvitees,
                    groupId);
            return;
        }
        account.kickOutFromGroup(groupId, inviterId);
        group.putBlockingRecord(new BlockingRecord(inviterId));
    }

    /**
     * I have invited another user into a group.
     *
     * @param groupId Group ID.
     * @param inviteeIds List of invitee ID's.
     * @throws IOException IO error occurs.
     */
    private void onInviteIntoGroup(@Nonnull String groupId, @Nonnull List<String> inviteeIds)
            throws IOException {

        String myId = account.getMid();

        LOGGER.info("I({}) have invited {} into group {}", myId, inviteeIds, groupId);

        GuardGroup group = guard.getGroup(groupId);

        Role myRole = group.getRoles().get(myId);
        if (myRole == null) {
            return;
        }

        // Check if the invitee is in the black list
        List<String> blockedInvitee = group.getBlockingRecords()
                .stream()
                .map(BlockingRecord::getAccountId)
                .filter(inviteeIds::contains)
                .collect(Collectors.toList());

        if (!blockedInvitee.isEmpty()) {
            account.cancelGroupInvitation(groupId, blockedInvitee);
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
        Group group = account.getGroup(groupId);
        if (group == null) {
            return;
        }
        Contact creator = group.getCreator();

        // If the creator of the group leaves the group before, then the creator
        // may be null.
        if (creator == null) {
            return;
        }
        GuardGroup guardGroup = guard.getGroup(groupId);
        Set<String> oldGroupAdmins = guardGroup.getAdmins();
        if (oldGroupAdmins.isEmpty()) {
            guardGroup.setAdmins(Collections.singleton(creator.getMid()));
        }
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

        GuardGroup group = guard.getGroup(groupId);

        // Get my role in the group
        Role myRole = group.getRoles().get(myId);

        // I shouldn't have accepted the group invitation
        if (!shouldAcceptInvitation(myRole)) {

            // Leave the group
            account.leaveGroup(groupId);
            return;
        }

        updateGroupAdmin(groupId);
        inviteDefenders(group);
    }

    private void inviteDefenders(@Nonnull GuardGroup group) throws IOException {
        String myId = account.getMid();
        String groupId = group.getId();

        // Get other defenders of the group
        Set<String> otherDefenderIds = group.getRoles()
                .entrySet()
                .stream()
                .filter(entry ->
                        !entry.getKey().equals(myId) &&
                                Role.DEFENDER.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Invite other defenders
        account.inviteIntoGroup(groupId, new ArrayList<>(otherDefenderIds));
    }

    private void onNotifiedAcceptGroupInvitation(@Nonnull String groupId, @Nonnull String inviteeId)
        throws IOException {
        GuardGroup group = guard.getGroup(groupId);
        Collection<BlockingRecord> records = group.getBlockingRecords();
        boolean inviteeBlocked = false;
        for (BlockingRecord record : records) {
            if (inviteeId.equals(record.getAccountId())) {
                inviteeBlocked = true;
                break;
            }
        }
        if (inviteeBlocked) {
            account.kickOutFromGroup(groupId, inviteeId);
        }
    }

    /**
     * Another user has kicked out another user (maybe myself) from a group.
     *
     * @param groupId   ID of the group.
     * @param removerId ID of the user that kick out another user.
     * @param removedIds ID of the users that are kicked out.
     * @throws IOException IO error occurs.
     */
    private void onNotifiedKickOutFromGroup(
            @Nonnull String groupId, @Nonnull String removerId, @Nonnull List<String> removedIds) throws IOException {

        LOGGER.info("User {} has kicked out users {} from group {}", removerId, removedIds, groupId);

        GuardGroup group = guard.getGroup(groupId);

        String myId = getId();

        Map<String, Role> roles = group.getRoles();

        Role myRole = roles.get(myId);

        // If I'm not a defender or supporter
        if (!shouldAcceptInvitation(myRole)) {

            // Do nothing
            return;
        }

        // If the remover is our own guy
        Role removerRole = roles.get(removerId);
        if (shouldAcceptInvitation(removerRole)) {
            return;
        }

        // If remover is admin of the group
        if (group.getAdmins().contains(removerId)) {

            LOGGER.info("Admin {} of group {} has kicked out {}", removerId, groupId, removedIds);

            // Do nothing
            return;
        }

        // Kick out remover
        account.kickOutFromGroup(groupId, removerId);

        // Invite supporters and the removed user
        Set<String> supporters = roles.entrySet()
                .stream()
                .filter(entry -> Role.SUPPORTER.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Set<String> invitees = new HashSet<>(supporters);

        Set<String> blockedIds = group.getBlockingRecords()
                .stream()
                .map(BlockingRecord::getAccountId)
                .collect(Collectors.toSet());

        invitees.addAll(removedIds
            .stream()
            .filter(removedId -> !blockedIds.contains(removedId))
            .collect(Collectors.toList()));
        invitees.remove(myId);
        account.inviteIntoGroup(groupId, new ArrayList<>(invitees));

        group.putBlockingRecord(new BlockingRecord(removerId));
    }

    @Nonnull
    public Account getAccount() {
        return account;
    }
}
