package org.guard_jiang.services.storage.sql;

import org.apache.commons.lang3.Validate;
import org.apache.ibatis.session.SqlSession;
import org.guard_jiang.Credential;
import org.guard_jiang.CredentialUpdater;
import org.guard_jiang.services.storage.sql.mappers.GuardAccountMapper;
import org.guard_jiang.services.storage.sql.records.AccountRecord;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Created by icand on 2017/8/20.
 */
public class SqlCredentialUpdater implements CredentialUpdater {

    private final AccountRecord accountRecord;
    private final SqlSessionFactory sqlSessionFactory;

    public SqlCredentialUpdater(
            @Nonnull SqlSessionFactory sqlSessionFactory,
            @Nonnull AccountRecord accountRecord) {
        this.accountRecord = accountRecord;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public CredentialUpdater withEmail(@Nonnull String email) {
        accountRecord.setEmail(email);
        return this;
    }

    @Nonnull
    @Override
    public CredentialUpdater withPassword(@Nonnull String password) {
        accountRecord.setPassword(password);
        return this;
    }

    @Nonnull
    @Override
    public CredentialUpdater withCertificate(@Nonnull String certificate) {
        accountRecord.setCertificate(certificate);
        return this;
    }

    @Nonnull
    @Override
    public CredentialUpdater withAuthToken(@Nonnull String authToken) {
        accountRecord.setAuthToken(authToken);
        return this;
    }

    @Nonnull
    @Override
    public Credential update() throws IOException {
        Validate.notNull(accountRecord.getEmail(), "Email cannot be null");
        Validate.notNull(accountRecord.getPassword(), "Password cannot be null");
        Validate.notNull(accountRecord.getCertificate(), "Certificate cannot be null");
        try (SqlSession session = sqlSessionFactory.openWriteSession()) {
            GuardAccountMapper mapper = session.getMapper(GuardAccountMapper.class);
            int nUpdated = mapper.updateGuardAccount(accountRecord);
            if (nUpdated <= 0) {
                throw new IOException("The account doesn't exist");
            }
            session.commit();
        }
        return new SqlCredential(sqlSessionFactory, accountRecord);
    }
}
