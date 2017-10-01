package org.guard_jiang.services.storage.sql;

import org.apache.commons.lang3.Validate;
import org.apache.ibatis.session.SqlSession;
import org.guard_jiang.GroupRole;
import org.guard_jiang.GroupRolesGetter;
import org.guard_jiang.Role;
import org.guard_jiang.services.storage.sql.mappers.GroupRoleMapper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by icand on 2017/8/20.
 */
public class SqlGroupRolesGetter implements GroupRolesGetter {

    private final SqlSessionFactory sqlSessionFactory;
    private String groupId;
    private String userId;
    private Role role;

    public SqlGroupRolesGetter(@Nonnull SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Nonnull
    @Override
    public GroupRolesGetter withGroupId(@Nonnull String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Nonnull
    @Override
    public GroupRolesGetter withRole(@Nonnull Role role) {
        this.role = role;
        return this;
    }

    @Nonnull
    @Override
    public GroupRolesGetter withUserId(@Nonnull String userId) {
        this.userId = userId;
        return this;
    }

    @Nonnull
    @Override
    public List<GroupRole> get() throws IOException {
        Validate.notNull(groupId, "Group ID must be specified");
        try (SqlSession session = sqlSessionFactory.openReadSession()) {
            GroupRoleMapper mapper = session.getMapper(GroupRoleMapper.class);
            return mapper.getGroupRoles(groupId, role, userId, false)
                    .stream()
                    .map(SqlGroupRole::new)
                    .collect(Collectors.toList());
        }
    }
}
