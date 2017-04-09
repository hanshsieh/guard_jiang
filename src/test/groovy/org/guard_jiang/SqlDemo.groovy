package org.guard_jiang

import org.guard_jiang.storage.SqlStorage
import org.guard_jiang.storage.StorageEnv;

/**
 * Created by someone on 4/4/2017.
 */
public class SqlDemo {
    public static void main(String[] args) throws Exception {
        new SqlDemo(args)
    }

    public SqlDemo(String[] args) {
        def env = StorageEnv.valueOf(args[0])
        def storage = new SqlStorage(env)
        def guard = new Guard(storage)
        guard.start()
    }
}
