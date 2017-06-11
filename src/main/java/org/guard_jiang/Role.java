package org.guard_jiang;

import javax.annotation.Nonnull;

/**
 * Created by someone on 1/31/2017.
 */
public enum Role {
    DEFENDER(0),
    SUPPORTER(1),
    ADMIN(2);
    private final int code;
    Role(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
    @Nonnull
    public static Role fromCode(int code) {
        for (Role role : Role.values()) {
            if (role.code == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("Illegal code: " + code);
    }
}
