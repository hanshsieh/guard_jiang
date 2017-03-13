package com.handoitadsf.line.group_guard;

import line.thrift.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Created by someone on 1/31/2017.
 */
class OperationDispatcher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationFetcher.class);

    private final AccountManager accountManager;
    private final Operation operation;

    public OperationDispatcher(@Nonnull AccountManager accountManager, @Nonnull Operation operation) {
        this.accountManager = accountManager;
        this.operation = operation;
    }

    @Override
    public void run() {
        try {
            if (LOGGER.isDebugEnabled()) {
                String userId = accountManager.getAccount().getProfile().getMid();
                LOGGER.debug("Receive operation. userId: {}, operation: {}", userId, operation);
            }
            switch (operation.getType()) {
                case NOTIFIED_INVITE_INTO_GROUP:
                    accountManager.onNotifiedInviteIntoGroup(
                            operation.getParam1(), operation.getParam2(), operation.getParam3());
                    break;
                case ACCEPT_GROUP_INVITATION:
                    accountManager.onAcceptGroupInvitation(operation.getParam1());
                    break;
                case NOTIFIED_KICKOUT_FROM_GROUP:
                    accountManager.onNotifiedKickOutFromGroup(
                            operation.getParam1(), operation.getParam2(), operation.getParam3());
                    break;
                case LEAVE_ROOM:
                    accountManager.onLeaveGroup(operation.getParam1());
                    break;
            }
        } catch (Exception ex) {
            LOGGER.error("Fail to handle operation {}", operation, ex);
        }
    }
}
