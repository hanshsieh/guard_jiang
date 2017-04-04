package org.guard_jiang;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

/**
 * Created by someone on 1/31/2017.
 */
public class Relation {
    private final String userId;
    private final String groupId;
    private final int hashCode;

    public Relation(@Nonnull String userId, @Nonnull String groupId) {
        this.userId = userId;
        this.groupId = groupId;
        this.hashCode = new HashCodeBuilder()
                .append(userId)
                .append(groupId)
                .hashCode();
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Relation)) {
            return false;
        }
        Relation that = (Relation) other;
        return userId.equals(that.userId) &&
                groupId.equals(that.groupId);
    }
}
