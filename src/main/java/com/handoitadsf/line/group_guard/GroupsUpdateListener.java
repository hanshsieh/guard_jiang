package com.handoitadsf.line.group_guard;

import java.util.List;

import javax.annotation.Nonnull;

import io.cslinmiso.line.model.LineGroup;

/**
 * Created by cahsieh on 1/27/17.
 */
public interface GroupsUpdateListener {
    void onGroupsUpdate(@Nonnull GuardRole role);
}
