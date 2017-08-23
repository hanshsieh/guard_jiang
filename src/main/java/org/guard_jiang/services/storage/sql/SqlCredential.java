package org.guard_jiang.services.storage.sql;

import org.apache.commons.lang3.Validate;
import org.guard_jiang.Credential;
import org.guard_jiang.CredentialUpdater;
import org.guard_jiang.services.storage.sql.records.AccountRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Created by icand on 2017/8/19.
 */
public class SqlCredential implements Credential {

    private final SqlAccount sqlAccount;
    private final AccountRecord accountRecord;

    public SqlCredential(
            @Nonnull SqlAccount sqlAccount,
            @Nonnull AccountRecord accountRecord) {
        Validate.notNull(accountRecord.getEmail(), "Email cannot be null");
        Validate.notNull(accountRecord.getPassword(), "Password cannot be null");
        Validate.notNull(accountRecord.getCertificate(), "Certificate cannot be null");
        Validate.notNull(accountRecord.getId(), "ID cannot be null");
        Validate.notNull(accountRecord.getPartition() >= 0, "Partition must be specified");
        this.sqlAccount = sqlAccount;
        this.accountRecord = accountRecord;
    }

    @Nonnull
    @Override
    public String getEmail() {
        assert accountRecord.getEmail() != null;
        return accountRecord.getEmail();
    }

    @Nonnull
    @Override
    public String getPassword() {
        assert accountRecord.getPassword() != null;
        return accountRecord.getPassword();
    }

    @Nonnull
    @Override
    public String getCertificate() {
        assert accountRecord.getCertificate() != null;
        return accountRecord.getCertificate();
    }

    @Nullable
    @Override
    public String getAuthToken() {
        return accountRecord.getAuthToken();
    }

    @Nonnull
    @Override
    public CredentialUpdater update() throws IOException {
        return new SqlCredentialUpdater(sqlAccount, new AccountRecord(accountRecord));
    }
}
