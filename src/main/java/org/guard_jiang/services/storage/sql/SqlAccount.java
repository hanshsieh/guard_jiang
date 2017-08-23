package org.guard_jiang.services.storage.sql;

import org.guard_jiang.Account;
import org.guard_jiang.Credential;

import javax.annotation.Nonnull;

/**
 * Created by icand on 2017/8/19.
 */
public class SqlAccount extends Account {

    private final int partition;
    private final SqlSessionFactory sqlSessionFactory;

    public SqlAccount(
            @Nonnull String mid,
            int partition,
            @Nonnull Credential credential,
            @Nonnull SqlSessionFactory sqlSessionFactory) {
        super(mid, credential);
        this.partition = partition;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Nonnull
    public int getPartition() {
        return partition;
    }

    @Nonnull
    protected SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }
}
