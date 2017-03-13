package com.handoitadsf.line.group_guard;

import line.thrift.Operation;

import javax.annotation.Nonnull;

/**
 * Created by someone on 1/28/2017.
 */
public interface OperationListener {
    void onOperation(@Nonnull Operation operation);
}
