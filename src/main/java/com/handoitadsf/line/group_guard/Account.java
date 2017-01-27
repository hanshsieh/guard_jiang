package com.handoitadsf.line.group_guard;

import java.util.List;

import javax.annotation.Nonnull;

import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineGroup;

/**
 * Created by cahsieh on 1/26/17.
 */
public class Account {
    private final LineClient client;
    public Account(@Nonnull LineClient client) {
        this.client = client;
    }
    LineClient getClient() {
        return client;
    }
}
