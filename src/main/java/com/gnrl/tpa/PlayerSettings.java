package com.gnrl.tpa;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSettings {

    private final Set<UUID> tpaDisabled = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, Set<UUID>> blockedPlayers = new ConcurrentHashMap<>();

    public boolean isTpaDisabled(UUID id) {
        return tpaDisabled.contains(id);
    }

    public boolean toggleTpa(UUID id) {
        if (tpaDisabled.contains(id)) {
            tpaDisabled.remove(id);
            return false;
        }
        tpaDisabled.add(id);
        return true;
    }

    public boolean isBlocked(UUID owner, UUID target) {
        Set<UUID> set = blockedPlayers.get(owner);
        return set != null && set.contains(target);
    }

    public boolean toggleBlock(UUID owner, UUID target) {
        Set<UUID> set = blockedPlayers.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet());
        if (set.contains(target)) {
            set.remove(target);
            return false;
        }
        set.add(target);
        return true;
    }
}
