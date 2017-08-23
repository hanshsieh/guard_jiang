package org.guard_jiang;

import org.guard_jiang.Account;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * Created by icand on 2017/8/19.
 */
public interface AccountsGetter {
    @Nonnull
    List<Account> get() throws IOException;
}
