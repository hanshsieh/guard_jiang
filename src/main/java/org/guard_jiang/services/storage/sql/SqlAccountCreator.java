package org.guard_jiang.services.storage.sql;

import org.apache.commons.lang3.Validate;
import org.apache.ibatis.session.SqlSession;
import org.guard_jiang.Account;
import org.guard_jiang.Credential;
import org.guard_jiang.AccountCreator;
import org.guard_jiang.services.storage.sql.mappers.SqlStorageMapper;
import org.guard_jiang.services.storage.sql.records.CredentialRecord;
import org.guard_jiang.services.storage.sql.records.AccountRecord;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by icand on 2017/8/19.
 */
public class SqlAccountCreator implements AccountCreator {

    private final SqlSessionFactory sqlSessionFactory;
    private final AccountRecord accountRecord = new AccountRecord();

    public SqlAccountCreator(@Nonnull SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public AccountCreator withMid(@Nonnull String mid) {
        accountRecord.setId(mid);
        return this;
    }

    @Nonnull
    @Override
    public AccountCreator withPartition(int partition) {
        accountRecord.setPartition(partition);
        return this;
    }

    @Nonnull
    @Override
    public AccountCreator withCredential(@Nonnull Credential credential) {
        CredentialRecord credentialRecord = new CredentialRecord();
        credentialRecord.setCertificate(credential.getCertificate());
        credentialRecord.setAuthToken(credential.getAuthToken());
        credentialRecord.setEmail(credential.getEmail());
        credentialRecord.setPassword(credential.getPassword());
        accountRecord.setCredential(credentialRecord);
        return this;
    }

    @Nonnull
    @Override
    public Account create() throws IOException {
        Validate.notNull(accountRecord.getId(), "Account ID must be set");
        Validate.isTrue(accountRecord.getPartition() >= 0, "Partition must be set");
        Validate.notNull(accountRecord.getCredential(), "Credential must be set");
        try (SqlSession session = sqlSessionFactory.openWriteSession()) {
            SqlStorageMapper mapper = session.getMapper(SqlStorageMapper.class);
            mapper.createGuardAccount(accountRecord);
            session.commit();
        }
        return new SqlAccount(
                accountRecord.getId(),
                accountRecord.getPartition(),
                new SqlCredential(accountRecord.getCredential()));
    }
}
