package com.handoitadsf.line.group_guard;

import com.google.common.collect.Lists;
import io.cslinmiso.line.model.LineGroup;
import line.thrift.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by someone on 1/31/2017.
 */
class AccountManager implements OperationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManager.class);

    private Guard guard;
    private final Account account;

    public AccountManager(Guard guard, @Nonnull Account account) {
        this.guard = guard;
        this.account = account;
    }

    private String getId() throws IOException {
        return account.getProfile().getMid();
    }

    @Override
    public void onOperation(@Nonnull Operation operation) {
        try {
            switch (operation.getType()) {
                case NOTIFIED_INVITE_INTO_GROUP:
                    onNotifiedInviteIntoGroup(
                            operation.getParam1(), operation.getParam2(), operation.getParam3());
                    break;
                case ACCEPT_GROUP_INVITATION:
                    onAcceptGroupInvitation(operation.getParam1());
                    break;
                case NOTIFIED_KICKOUT_FROM_GROUP:
                    onNotifiedKickOutFromGroup(
                            operation.getParam1(), operation.getParam2(), operation.getParam3());
                    break;
                case LEAVE_ROOM:
                    onLeaveGroup(operation.getParam1());
                    break;
            }
        } catch (IOException ex) {
            LOGGER.error("Fail to execute operation {}", operation, ex);
        }
    }

    /**
     * Someone has invited another user into a group.
     *
     * @param groupId   Group ID.
     * @param inviterId Inviter's ID.
     * @param inviteeId Invitee's ID.
     * @throws IOException
     */
    public void onNotifiedInviteIntoGroup(
            @Nonnull String groupId,
            @Nonnull String inviterId,
            @Nonnull String inviteeId) throws IOException {
        LOGGER.info("{} invites {} into group {}", inviterId, inviteeId, groupId);
        String myId = getId();

        if (!myId.equals(inviteeId)) {

            GroupProfile groupProfile = guard.getGroupProfile(groupId);
            if (groupProfile == null) {
                return;
            }

            BlockingEntry blockingEntry = groupProfile.getBlockingEntry(inviteeId);
            if (blockingEntry == null) {
                return;
            }

            Role inviterRole = guard.getRole(inviterId, groupId);

            if (Role.DEFENDER.equals(inviterRole) || Role.SUPPORTER.equals(inviterRole)) {
                return;
            }
            account.cancelGroupInvitation(groupId, Collections.singletonList(inviteeId));
            account.kickOutFromGroup(groupId, Collections.singletonList(inviterId));
            groupProfile.addBlockedUser(inviterId);

            return;
        }

        // Get my role in the group
        Role myRole = guard.getRole(myId, groupId);
        if (myRole == null) {

            // I don't have role in the group
            // Cancel the invitation
            account.cancelGroupInvitation(groupId, Collections.singletonList(myId));
        } else if (Role.DEFENDER.equals(myRole)) {

            // I'm defender of the group, so I should join the group
            account.acceptGroupInvitation(groupId);
        }

        // If I'm supporter of the group, do nothing.
        // Just leave the invitation there.
    }

    /**
     * The user has accepted a group invitation.
     *
     * @param groupId Group ID.
     */
    public void onAcceptGroupInvitation(@Nonnull String groupId) throws IOException {

        String myId = getId();

        LOGGER.info("User {} has accepted invitation into group {}", myId, groupId);

        // Get my role in the group
        Role role = guard.getRole(myId, groupId);

        // I don't have any role in this group
        if (role == null) {

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
        account.refreshGroups();
    }

    /**
     * A user has kicked out another user from a group.
     *
     * @param groupId   ID of the group.
     * @param removerId ID of the user that kick out another user.
     * @param removedId ID of the user that is kicked out.
     * @throws IOException IO error occurs.
     */
    public void onNotifiedKickOutFromGroup(
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
        groupProfile.addBlockedUser(removerId);
    }

    public void onLeaveGroup(@Nonnull String groupId) throws IOException {
        account.refreshGroups();
    }

    @Nonnull
    private Set<String> getGroupAdmins(@Nonnull GroupProfile groupProfile) throws IOException {
        Set<String> admins = groupProfile.getAdminIds();

        // If not admin is configured
        if (admins.isEmpty()) {

            // Use group creator by default
            LineGroup group = account.getGroup(groupProfile.getGroupId());
            if (group != null) {
                Collections.singleton(group.getCreator().getId());
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
