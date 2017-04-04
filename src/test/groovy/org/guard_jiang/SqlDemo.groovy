package org.guard_jiang

import org.guard_jiang.storage.SqlStorage;

/**
 * Created by someone on 4/4/2017.
 */
public class SqlDemo {
    public static void main(String[] args) throws Exception {
        new SqlDemo()
    }

    public SqlDemo() {
        def storage = new SqlStorage()
        def guard = new Guard(storage)
        guard.start()
    }
}
