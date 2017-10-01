package org.guard_jiang.services.storage.sql;

import org.apache.ibatis.session.SqlSession;
import org.guard_jiang.GroupIdsGetter;
import org.guard_jiang.services.storage.sql.mappers.GroupRoleMapper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by icand on 2017/8/26.
 */
public class SqlGroupIdsGetter implements GroupIdsGetter {

    private final SqlSessionFactory sqlSessionFactory;
    private final Set<String> roleCreators = new HashSet<>();

    public SqlGroupIdsGetter(@Nonnull SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public GroupIdsGetter withRoleCreators(@Nonnull Set<String> userIds) {
        roleCreators.clear();
        roleCreators.addAll(userIds);
        return this;
    }

    @Nonnull
    @Override
    public Set<String> get() throws IOException {
        try (SqlSession session = sqlSessionFactory.openReadSession()) {
            GroupRoleMapper mapper = session.getMapper(GroupRoleMapper.class);
            return mapper.getGroupsWithRolesCreatedByUsers(roleCreators);
        }
    }
}
