package org.guard_jiang;

import javax.annotation.Nonnull;

/**
 * Created by someone on 1/31/2017.
 */
public enum Role {
    DEFENDER(0),
    SUPPORTER(1),
    ADMIN(2);
    private final int id;
    Role(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    @Nonnull
    public static Role fromCode(int id) {
        for (Role role : Role.values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Illegal ID: " + id);
    }
}
