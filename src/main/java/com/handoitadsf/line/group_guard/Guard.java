package com.handoitadsf.line.group_guard;

import sun.jvm.hotspot.memory.DefNewGeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.cslinmiso.line.model.LineBase;
import io.cslinmiso.line.model.LineGroup;

/**
 * Created by cahsieh on 1/26/17.
 */
public class Guard implements GroupsUpdateListener {
    private final Collection<Defender> defenders = new ArrayList<Defender>();
    private boolean started = false;
    public void addDefender(Defender defender) {
        defenders.add(defender);
        defender.setGroupsUpdateListener(this);
    }
    public void start() {
        if (started) {
            throw new IllegalStateException("Already running");
        }
        defenders.forEach(Defender::start);
    }
    public void stop() {
        if (!started) {
            throw new IllegalStateException("Not running");
        }
        defenders.forEach(Defender::stop);
    }

    @Override
    public void onGroupsUpdate(@Nonnull GuardRole role) {
        if (!(role instanceof Defender)) {
            return;
        }
        List<LineGroup> groups = role.getGroups();
        if (groups == null) {
            return;
        }
        Set<String> groupIds = groups.stream()
            .filter(LineGroup::isJoined)
            .map(LineBase::getId)
            .collect(Collectors.toSet());

        Defender updatedDefender = (Defender) role;
        for (Defender defender : defenders) {
            if (defender.equals(updatedDefender)) {
                continue;
            }
            List<LineGroup> otherGroups = defender.getGroups();

            if (otherGroups == null) {
                continue;
            }

            Set<String> otherGroupIds = otherGroups.stream()
                .filter(LineGroup::isJoined)
                .map(LineBase::getId)
                .collect(Collectors.toSet());


        }
    }
}
