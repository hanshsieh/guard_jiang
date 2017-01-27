package com.handoitadsf.line.group_guard;

import java.util.List;

import io.cslinmiso.line.model.LineClient;
import io.cslinmiso.line.model.LineGroup;

/**
 * Created by cahsieh on 1/27/17.
 */
public interface GuardRole {
    List<LineGroup> getGroups();
}
