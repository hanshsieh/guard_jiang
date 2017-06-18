package org.guard_jiang.chat.phase;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.guard_jiang.Account;
import org.guard_jiang.Guard;
import org.guard_jiang.License;
import org.guard_jiang.chat.ChatStatus;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * This chat phase is used for creating a license for the LINE user.
 */
public class LicenseCreationChatPhase extends ChatPhase {

    protected static final int MAX_TRIAL_LICENSE = 10;

    /**
     * Construct a new instance.
     *
     * @param guard Guard.
     * @param account The guard account.
     * @param userId ID of the LINE user that it guard is chatting with.
     * @param data Data.
     */
    public LicenseCreationChatPhase(
            @Nonnull Guard guard,
            @Nonnull Account account,
            @Nonnull String userId,
            @Nonnull ObjectNode data) {
        super(guard, account, userId, data);
    }

    @Override
    public void onEnter() throws IOException {
        Guard guard = getGuard();
        Account account = getAccount();
        List<License> licenses = guard.getLicensesOfUser(account.getMid());
        if (licenses.size() >= MAX_TRIAL_LICENSE) {
            sendTextMessage("您所建立的金鑰個數已經超過上限囉");
            return;
        }

        License license = guard.createTrialLicense(getUserId());
        String key = license.getReadableKey();
        sendTextMessage("已為您建立金鑰並綁定至您的帳號\n金鑰: " + key);
        sendTextMessage("您接下來可以用此金鑰保護您的群組");
        leavePhase();
    }

    @Override
    public void onReturn(@Nonnull ChatStatus returnStatus, @Nonnull ObjectNode returnData) throws IOException {
        leavePhase();
    }

    @Override
    public void onReceiveTextMessage(@Nonnull String text) throws IOException {

        // To prevent user from being stuck at this phase
        leavePhase();
    }
}
