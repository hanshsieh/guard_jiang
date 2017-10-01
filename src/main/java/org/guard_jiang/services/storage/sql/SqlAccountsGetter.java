package org.guard_jiang.services.storage.sql;

import org.apache.commons.lang3.Validate;
import org.apache.ibatis.session.SqlSession;
import org.guard_jiang.Account;
import org.guard_jiang.AccountsGetter;
import org.guard_jiang.services.storage.sql.mappers.GuardAccountMapper;
import org.guard_jiang.services.storage.sql.records.AccountRecord;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by icand on 2017/8/19.
 */
public class SqlAccountsGetter implements AccountsGetter {

    private final SqlSessionFactory sqlSessionFactory;
    private final int partition;

    public SqlAccountsGetter(int partition, @Nonnull SqlSessionFactory sqlSessionFactory) {
        this.partition = partition;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public List<Account> get() throws IOException {
        try (SqlSession session = sqlSessionFactory.openReadSession()) {
            GuardAccountMapper mapper = session.getMapper(GuardAccountMapper.class);
            List<AccountRecord> accountRecords = mapper.getGuardAccounts(partition);
            return accountRecords.stream()
                    .map(accountRecord -> {
                        Validate.notNull(accountRecord.getId());
                        return new SqlAccount(
                                accountRecord.getId(),
                                accountRecord.getPartition(),
                                new SqlCredential(sqlSessionFactory, accountRecord),
                                sqlSessionFactory);
                    })
                    .collect(Collectors.toList());
        }
    }
}
