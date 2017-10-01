package org.guard_jiang.services.storage.sql.mappers;

import org.apache.ibatis.annotations.Param;
import org.guard_jiang.services.storage.sql.records.AccountRecord;
import org.guard_jiang.services.storage.sql.records.CredentialRecord;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Created by icand on 2017/8/26.
 */
public interface GuardAccountMapper {

    @Nonnull
    List<AccountRecord> getGuardAccounts(
            @Param("partition") int partition);

    void createGuardAccount(
            @Param("account") @Nonnull AccountRecord accountData);

    int updateGuardAccount(
            @Param("account") @Nonnull AccountRecord accountData);

    int updateAccountCredential(
            @Param("accountId") @Nonnull String accountId,
            @Param("credential") @Nonnull CredentialRecord credential);
}
