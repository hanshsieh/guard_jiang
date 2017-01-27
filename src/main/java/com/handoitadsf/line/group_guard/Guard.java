package com.handoitadsf.line.group_guard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cahsieh on 1/26/17.
 */
public class Guard {
    private final Collection<Defender> defenders = new ArrayList<Defender>();
    public void addDefender(Defender defender) {
        defenders.add(defender);
    }
    public void run() {

    }
}
