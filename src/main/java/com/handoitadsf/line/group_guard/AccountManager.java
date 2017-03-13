package com.handoitadsf.line.group_guard;

import io.cslinmiso.line.model.LineGroup;
import line.thrift.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
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
            guard.submitOperation(getId(), operation);
        } catch (IOException ex) {
            LOGGER.error("Fail to submit operation {}", operation, ex);
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
        String myId = getId();
        if (!myId.equals(inviteeId)) {
            return;
        }

        // Get my role in the group
        Role role = guard.getRole(myId, groupId);
        if (role == null) {
            // I don't have role in the group
            // Cancel the invitation
            account.cancelGroupInvitation(groupId, Collections.singletonList(myId));
        } else if (Role.DEFENDER.equals(role)) {

            // I'm defender of the group, so I should join the group
            account.acceptGroupInvitation(groupId);
        }
    }

    /**
     * The user has accepted a group invitation.
     *
     * @param groupId Group ID.
     */
    public void onAcceptGroupInvitation(@Nonnull String groupId) throws IOException {
        String myId = getId();
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
            Set<String> otherDefenderIds = guard.getDefendersOfGroup(groupId).stream()
                    .filter(id -> !myId.equals(id)).collect(Collectors.toSet());
            assert otherDefenderIds != null;

            // Invite other defenders
            // TODO If someone is already in group, will it fails? Will it continue for the remaining groups?
            account.inviteIntoGroup(groupId, new ArrayList<>(otherDefenderIds));
        }
        account.refreshGroups();
    }

    public void onNotifiedKickOutFromGroup(
            @Nonnull String groupId, @Nonnull String removerId, @Nonnull String removedId) throws IOException {
        String myId = getId();
        Role myRole = guard.getRole(myId, groupId);

        // If I'm not a defender or supporter
        if (!Role.DEFENDER.equals(myRole) && !Role.SUPPORTER.equals(myRole)) {

            // Do nothing
            return;
        }

        // If the remover is our own guy
        if (guard.getAccountIds().contains(removerId)) {
            return;
        }

        GroupProfile groupProfile = guard.getGroupProfile(groupId);

        // If remover is admin of the group
        if (getGroupAdmins(groupProfile).contains(removerId)) {

            // Do nothing
            return;
        }

        // Kick out remover
        account.kickOutFromGroup(groupId, Collections.singletonList(removerId));

        // Invite supporters and the removed user
        Set<String> supporters = guard.getSupportersOfGroup(groupId);
        List<String> invitees = new ArrayList<>(supporters);
        if (!supporters.contains(removedId)) {
            invitees.add(removedId);
        }
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
            for (LineGroup group : account.getGroups()) {
                if (groupProfile.getGroupId().equals(group.getId())) {
                    return Collections.singleton(group.getCreator().getId());
                }
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
