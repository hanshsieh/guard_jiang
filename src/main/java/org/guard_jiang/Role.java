package org.guard_jiang;

import javax.annotation.Nonnull;

/**
 * Guard role.
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

    /**
     * Get the role from its ID.
     *
     * @param id Role ID.
     * @return Role.
     * @throws IllegalArgumentException If the ID is incorrect.
     */
    @Nonnull
    public static Role fromId(int id) throws IllegalArgumentException {
        for (Role role : Role.values()) {
            if (role.id == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Illegal ID: " + id);
    }
}
