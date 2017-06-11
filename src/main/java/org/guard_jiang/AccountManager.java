package org.guard_jiang;

import line.thrift.Contact;
import line.thrift.Group;
import line.thrift.Message;
import line.thrift.Operation;
import org.guard_jiang.chat.MessageManager;
import org.guard_jiang.storage.GroupMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by someone on 1/31/2017.
 */
class AccountManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManager.class);
    private static final long DEFAULT_BLOCKING_MS = 1000 * 60 * 60;
    private static final int THREAD_NAME_MID_PREFIX_LEN = 5;
    private static final long SUPPORTING_MS = 1000 * 60 * 5;
    private static final long RECOVERY_MS = 1000 * 60 * 5;
    private static final String SEPARATOR = "\u001E";
    private Guard guard;
    private final Account account;
    private AccountWatcher watcher;
    private Instant lastRectifyTime = null;

    // A map from group ID to the time when I joined the group
    private Map<String, Instant> groupJoinedTime = new HashMap<>();

    private final MessageManager messageManager;

    public AccountManager(@Nonnull Guard guard, @Nonnull Account account) {
        this.guard = guard;
        this.account = account;
        this.messageManager = new MessageManager(guard, account);
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
        Set<String> contactsToAdd = checkJoinedGroups();

        // Check the invited groups
        checkInvitedGroups();

        contactsToAdd.removeAll(account.getContactIds());
        contactsToAdd.remove(account.getMid());
        for (String contactToAdd : contactsToAdd) {
            account.addContact(contactToAdd);
        }

        lastRectifyTime = Instant.now();
    }

    private void checkInvitedGroups() throws IOException {
        List<String> groupIdsInvited = account.getGroupIdsInvited();
        for (String groupId : groupIdsInvited) {
            account.acceptGroupInvitation(groupId);
        }
    }

    private Set<String> checkJoinedGroups() throws IOException {
        Set<String> contactsToAdd = new HashSet<>();
        List<String> groupIdsJoined = account.getGroupIdsJoined();
        for (String groupId : groupIdsJoined) {
            contactsToAdd.addAll(checkJoinedGroup(groupId));
        }

        Iterator<Map.Entry<String, Instant>> itr = groupJoinedTime.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Instant> entry = itr.next();

            if (!groupIdsJoined.contains(entry.getKey())) {
                itr.remove();
            }
        }
        return contactsToAdd;
    }

    @Nullable
    private Role getRoleFromAll(@Nonnull Collection<GroupRole> groupRoles, @Nonnull String userId) {
        for (GroupRole groupRole : groupRoles) {
            if(userId.equals(groupRole.getUserId())) {
                return groupRole.getRole();
            }
        }
        return null;
    }

    @Nullable
    private Role getMyRoleFromAll(@Nonnull Collection<GroupRole> groupRoles) {
        String myId = account.getMid();
        return getRoleFromAll(groupRoles, myId);
    }

    @Nonnull
    private Set<String> checkJoinedGroup(@Nonnull String groupId) throws IOException {
        Set<String> contactsToAdd = new HashSet<>();

        long now = System.currentTimeMillis();
        GuardGroup guardGroup = guard.getGroup(groupId);

        List<GroupRole> roles = guardGroup.getRoles();
        Role myRole = getMyRoleFromAll(roles);

        boolean shouldLeave = false;

        if (isGuardRole(myRole)) {
            Set<String> memberIds = getGroupMemberIds(groupId);
            Set<String> blockIds = getGroupBlockedIds(guardGroup);

            kickoutBlockedMembers(guardGroup, memberIds, blockIds);
            inviteDefenders(guardGroup, memberIds);

            backupGroupMembers(guardGroup, memberIds, blockIds);
            contactsToAdd.addAll(memberIds);
            contactsToAdd.addAll(roles.stream().map(GroupRole::getUserId).collect(Collectors.toList()));
        }

        if (!Role.DEFENDER.equals(myRole)) {
            Instant joinedTime = groupJoinedTime.get(groupId);
            if (joinedTime == null ||
                    now - joinedTime.toEpochMilli() > SUPPORTING_MS) {
                shouldLeave = true;
            }
        }

        if (shouldLeave) {
            account.leaveGroup(groupId);
        }
        return contactsToAdd;
    }

    private Set<String> getGroupBlockedIds(@Nonnull GuardGroup guardGroup) throws IOException {
        return guardGroup.getBlockingRecords()
                .stream()
                .map(BlockingRecord::getUserId)
                .collect(Collectors.toSet());
    }

    private void recoverGroupMembers(
            @Nonnull GuardGroup guardGroup,
            @Nonnull Set<String> groupMembers,
            @Nonnull Set<String> blockedMembers) throws IOException {
        GroupMetadata metadata = guardGroup.getMetadata();
        Instant expiryTime = metadata.getRecoveryExpiryTime();
        long now = System.currentTimeMillis();
        String groupId = guardGroup.getId();

        if (expiryTime != null && expiryTime.toEpochMilli() >= now) {
            LOGGER.info("Recovering members of group {} from snapshot of time {}",
                    groupId, metadata.getMembersBackupTime());
            Set<String> membersBackup = guardGroup.getMembersBackup()
                    .stream()
                    .filter(memberId -> !groupMembers.contains(memberId) && !blockedMembers.contains(memberId))
                    .collect(Collectors.toSet());
            account.inviteIntoGroup(groupId, membersBackup);
        }
    }

    private void backupGroupMembers(
            @Nonnull GuardGroup guardGroup,
            @Nonnull Set<String> groupMembers,
            @Nonnull Set<String> blockedMembers) throws IOException {
        String groupId = guardGroup.getId();
        long now = System.currentTimeMillis();
        GroupMetadata metadata = guardGroup.getMetadata();
        Instant expiryTime = metadata.getRecoveryExpiryTime();
        if (expiryTime == null || expiryTime.toEpochMilli() < now) {
            LOGGER.debug("Backing up members for group {}", groupId);
            Set<String> members = groupMembers.stream()
                    .filter(memberId -> !blockedMembers.contains(memberId))
                    .collect(Collectors.toSet());
            guardGroup.setMembersBackup(members);
            metadata.setMembersBackupTime(Instant.now());
            guardGroup.setMetadata(metadata);
        }
    }

    private void kickoutBlockedMembers(
            @Nonnull GuardGroup guardGroup,
            @Nonnull Set<String> memberIds,
            @Nonnull Set<String> blockedIds) throws IOException {
        String groupId = guardGroup.getId();
        Group group = account.getGroup(groupId);
        if (group == null) {
            return;
        }
        List<Contact> invitees = group.getInvitee();
        Set<String> inviteeIds;
        if (invitees == null) {
            inviteeIds = Collections.emptySet();
        } else {
            inviteeIds = invitees
                    .stream()
                    .map(Contact::getMid)
                    .collect(Collectors.toSet());
        }
        Set<String> blockedMembers = blockedIds
                .stream()
                .filter(memberIds::contains)
                .collect(Collectors.toSet());
        List<String> blockedInvitees = blockedIds
                .stream()
                .filter(inviteeIds::contains)
                .collect(Collectors.toList());
        for (String blockedMember : blockedMembers) {
            account.kickOutFromGroup(groupId, blockedMember);
        }
        account.cancelGroupInvitation(groupId, blockedInvitees);
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

            // Another user has kicked out another user (maybe myself) from a group.
            case NOTIFIED_KICKOUT_FROM_GROUP:
                onNotifiedKickOutFromGroup(
                        operation.getParam1(),
                        operation.getParam2(),
                        Arrays.asList(operation.getParam3().split(SEPARATOR)));
                break;
            case LEAVE_GROUP:
                onLeaveGroup(operation.getParam1());
                break;

            case NOTIFIED_LEAVE_GROUP:
                onNotifiedLeaveGroup(operation.getParam1(), operation.getParam2());
                break;

            case RECEIVE_MESSAGE:
                onReceiveMessage(operation.getMessage());
                break;
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
        account.acceptGroupInvitation(groupId);
    }

    @Nullable
    private static Role getRoleFromGroupRole(@Nullable GroupRole groupRole) {
        if (groupRole != null) {
            return groupRole.getRole();
        }
        return null;
    }

    /**
     * Someone has invited another user into a group.
     *
     * @param groupId    Group ID.
     * @param inviterId  Inviter's ID. Not myself.
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
                .map(BlockingRecord::getUserId)
                .filter(userId -> inviteeIds.contains(userId) && !myId.equals(userId))
                .collect(Collectors.toList());
        if (blockedInvitees.isEmpty()) {
            return;
        }

        account.cancelGroupInvitation(groupId, blockedInvitees);

        Role inviterRole = getRoleFromGroupRole(group.getRoleOfUser(inviterId));

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
        group.blockUser(inviterId,
                        Instant.ofEpochMilli(System.currentTimeMillis() + DEFAULT_BLOCKING_MS));
    }

    /**
     * I have invited another user into a group.
     *
     * @param groupId    Group ID.
     * @param inviteeIds List of invitee ID's.
     * @throws IOException IO error occurs.
     */
    private void onInviteIntoGroup(@Nonnull String groupId, @Nonnull List<String> inviteeIds)
            throws IOException {

        String myId = account.getMid();

        LOGGER.info("I({}) have invited {} into group {}", myId, inviteeIds, groupId);

        GuardGroup group = guard.getGroup(groupId);

        GroupRole myGroupRole = group.getRoleOfUser(myId);
        if (myGroupRole == null) {
            return;
        }

        // Check if the invitee is in the black list
        List<String> blockedInvitee = group.getBlockingRecords()
                .stream()
                .map(BlockingRecord::getUserId)
                .filter(inviteeIds::contains)
                .collect(Collectors.toList());

        if (!blockedInvitee.isEmpty()) {
            account.cancelGroupInvitation(groupId, blockedInvitee);
        }
    }

    private void onLeaveGroup(@Nonnull String groupId) throws IOException {
        LOGGER.info("I({}) have left group {}", account.getMid(), groupId);
        groupJoinedTime.remove(groupId);
    }

    private void onNotifiedLeaveGroup(@Nonnull String groupId, @Nonnull String userId) throws IOException {
        LOGGER.info("Another user {} has left group {}", userId, groupId);
        GuardGroup guardGroup = guard.getGroup(groupId);

        backupGroupMembers(
                guardGroup,
                getGroupMemberIds(groupId),
                getGroupBlockedIds(guardGroup));
    }

    private void onReceiveMessage(@Nonnull Message message) throws IOException {
        messageManager.onReceiveMessage(message);
    }

    private boolean isGuardRole(Role role) {
        return Role.DEFENDER.equals(role) || Role.SUPPORTER.equals(role);
    }

    /**
     * The user has accepted a group invitation.
     *
     * @param groupId Group ID.
     */
    private void onAcceptGroupInvitation(@Nonnull String groupId) throws IOException {

        groupJoinedTime.put(groupId, Instant.now());

        String myId = getId();

        LOGGER.info("I({}) has accepted invitation into group {}", myId, groupId);

        GuardGroup group = guard.getGroup(groupId);

        // Get my role in the group
        Role myRole = getRoleFromGroupRole(group.getRoleOfUser(myId));

        if (!isGuardRole(myRole)) {

            return;
        }

        Set<String> groupMembers = getGroupMemberIds(groupId);
        Set<String> blockedMembers = getGroupBlockedIds(group);

        kickoutBlockedMembers(group, groupMembers, blockedMembers);
        inviteDefenders(group, getGroupMemberIds(groupId));
        recoverGroupMembers(group, groupMembers, blockedMembers);
    }

    private Set<String> getGroupMemberIds(@Nonnull String groupId) throws IOException {
        Group group = account.getGroup(groupId);
        if (group == null) {
            return Collections.emptySet();
        }
        return group.getMembers()
                .stream()
                .map(Contact::getMid)
                .collect(Collectors.toSet());
    }

    private void inviteDefenders(
            @Nonnull GuardGroup group,
            @Nonnull Set<String> memberIds) throws IOException {
        String groupId = group.getId();

        // Get other defenders of the group
        Set<String> otherDefenderIds = group.getRoles(Role.DEFENDER)
                .stream()
                .filter(groupRole ->
                        !memberIds.contains(groupRole.getUserId()))
                .map(GroupRole::getUserId)
                .collect(Collectors.toSet());

        // Invite other defenders
        if (!otherDefenderIds.isEmpty()) {
            for (String otherDefenderId : otherDefenderIds) {
                account.addContact(otherDefenderId);
            }
            account.inviteIntoGroup(groupId, otherDefenderIds);
        }
    }

    private void onNotifiedAcceptGroupInvitation(@Nonnull String groupId, @Nonnull String inviteeId)
            throws IOException {
        GuardGroup group = guard.getGroup(groupId);
        Set<String> blockIds = getGroupBlockedIds(group);
        if (blockIds.contains(inviteeId)) {
            account.kickOutFromGroup(groupId, inviteeId);
        } else {
            account.addContact(inviteeId);
            backupGroupMembers(
                    group,
                    getGroupMemberIds(groupId),
                    blockIds);
        }
    }

    /**
     * Another user has kicked out another user (maybe myself) from a group.
     *
     * @param groupId    ID of the group.
     * @param removerId  ID of the user that kick out another user.
     * @param removedIds ID of the users that are kicked out.
     * @throws IOException IO error occurs.
     */
    private void onNotifiedKickOutFromGroup(
            @Nonnull String groupId, @Nonnull String removerId, @Nonnull List<String> removedIds) throws IOException {

        LOGGER.info("User {} has kicked out users {} from group {}", removerId, removedIds, groupId);

        GuardGroup group = guard.getGroup(groupId);

        String myId = getId();

        List<GroupRole> groupRoles = group.getRoles();

        Role myRole = getMyRoleFromAll(groupRoles);

        // If I'm not a defender or supporter
        if (!isGuardRole(myRole)) {

            // Do nothing
            return;
        }

        // If the remover is our own guy
        Role removerRole = getRoleFromAll(groupRoles, removerId);
        if (isGuardRole(removerRole)) {
            return;
        }

        // If remover is admin of the group
        if (Role.ADMIN.equals(removerRole)) {

            LOGGER.info("Admin {} of group {} has kicked out {}", removerId, groupId, removedIds);

            // Do nothing
            return;
        }

        // Add the remover to black list first because I may have been kicked out
        // and the following operations may fail
        group.blockUser(
                removerId,
                Instant.ofEpochMilli(System.currentTimeMillis() + DEFAULT_BLOCKING_MS));

        GroupMetadata metadata = group.getMetadata();
        metadata.setRecoveryExpiryTime(Instant.now().plus(RECOVERY_MS, ChronoUnit.MILLIS));
        group.setMetadata(metadata);

        // Kick out remover
        account.kickOutFromGroup(groupId, removerId);

        // Invite supporters and the removed user
        Set<String> supporters = groupRoles
                .stream()
                .filter(groupRole -> Role.SUPPORTER.equals(groupRole.getRole()))
                .map(GroupRole::getUserId)
                .collect(Collectors.toSet());
        Set<String> invitees = new HashSet<>(supporters);

        Set<String> blockedIds = group.getBlockingRecords()
                .stream()
                .map(BlockingRecord::getUserId)
                .collect(Collectors.toSet());

        invitees.addAll(removedIds
                .stream()
                .filter(removedId -> !blockedIds.contains(removedId))
                .collect(Collectors.toList()));
        invitees.remove(myId);

        // I must add the user as friend before inviting the user
        // to a group
        LOGGER.debug("Contacts to invite to handle kick out event: {}", invitees);
        for (String invitee : invitees) {
            account.addContact(invitee);
        }
        account.inviteIntoGroup(groupId, invitees);
    }

    @Nonnull
    public Account getAccount() {
        return account;
    }
}
