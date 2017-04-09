package org.guard_jiang.storage;

import javax.annotation.Nonnull;

/**
 * Created by someone on 4/9/2017.
 */
public enum StorageEnv {
    BETA_1(1),
    PROD_1(101);
    private final int id;
    StorageEnv(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    @Nonnull
    public static StorageEnv fromId(int id) {
        for (StorageEnv env : StorageEnv.values()) {
            if (env.getId() == id) {
                return env;
            }
        }
        throw new IllegalArgumentException("Fail to find env for ID " + id);
    }
}
