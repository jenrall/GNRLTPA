package com.gnrl.tpa;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TpaManager {

    private final GNRLTPA plugin;
    private final Map<UUID, TpaRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> autoAccept = ConcurrentHashMap.newKeySet();

    public TpaManager(GNRLTPA plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(Player player) {
        long cooldown = plugin.getConfig().getLong("settings.cooldown-seconds", 10) * 1000L;
        Long lastUse = cooldowns.get(player.getUniqueId());
        return lastUse != null && (System.currentTimeMillis() - lastUse) < cooldown;
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isAutoAccept(Player player) {
        return autoAccept.contains(player.getUniqueId());
    }

    public boolean toggleAutoAccept(Player player) {
        UUID id = player.getUniqueId();
        if (autoAccept.contains(id)) {
            autoAccept.remove(id);
            return false;
        }
        autoAccept.add(id);
        return true;
    }

    public void sendRequest(Player sender, Player target, boolean here) {
        UUID targetId = target.getUniqueId();
        UUID senderId = sender.getUniqueId();

        pendingRequests.put(targetId, new TpaRequest(senderId, targetId, here, System.currentTimeMillis()));

        if (isAutoAccept(target)) {
            sender.sendRichMessage("<green>" + target.getName() + " has auto-accept enabled.</green>");
            target.sendRichMessage("<green>Auto-accepted request from " + sender.getName() + "</green>");
            acceptRequest(target);
            return;
        }

        int timeout = plugin.getConfig().getInt("settings.request-timeout-seconds", 60);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TpaRequest req = pendingRequests.get(targetId);
            if (req != null && req.senderId().equals(senderId)) {
                pendingRequests.remove(targetId);
                Player s = Bukkit.getPlayer(senderId);
                if (s != null && s.isOnline()) {
                    s.sendRichMessage("<red>Your request to " + target.getName() + " timed out.</red>");
                }
            }
        }, timeout * 20L);
    }

    public boolean hasPendingRequest(Player target) {
        return pendingRequests.containsKey(target.getUniqueId());
    }

    public TpaRequest getRequest(Player target) {
        return pendingRequests.get(target.getUniqueId());
    }

    public boolean cancelSentRequest(Player sender) {
        UUID senderId = sender.getUniqueId();
        for (Map.Entry<UUID, TpaRequest> entry : pendingRequests.entrySet()) {
            if (entry.getValue().senderId().equals(senderId)) {
                pendingRequests.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    public void acceptRequest(Player target) {
        TpaRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) return;

        Player sender = Bukkit.getPlayer(request.senderId());
        if (sender == null || !sender.isOnline()) return;

        sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);

        int delay = plugin.getConfig().getInt("settings.teleport-delay-seconds", 3);
        String teleportingMsg = plugin.getConfig().getString("messages.teleporting", "")
                .replace("<seconds>", String.valueOf(delay));

        if (delay <= 0) {
            doTeleport(sender, target, request.here());
            return;
        }

        sender.sendRichMessage(teleportingMsg);
        target.sendRichMessage(teleportingMsg);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(sender.getUniqueId());
            if (sender.isOnline() && target.isOnline()) {
                doTeleport(sender, target, request.here());
            }
        }, delay * 20L);

        pendingTeleports.put(sender.getUniqueId(), task);
    }

    private void doTeleport(Player sender, Player target, boolean here) {
        Player whoMoves = here ? target : sender;
        Player whoStays = here ? sender : target;

        whoMoves.getWorld().spawnParticle(Particle.PORTAL, whoMoves.getLocation(), 50, 0.5, 1, 0.5, 0.1);
        whoStays.getWorld().spawnParticle(Particle.PORTAL, whoStays.getLocation(), 50, 0.5, 1, 0.5, 0.1);

        whoMoves.playSound(whoMoves.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        whoStays.playSound(whoStays.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        whoMoves.teleport(whoStays.getLocation());

        whoMoves.getWorld().spawnParticle(Particle.REVERSE_PORTAL, whoMoves.getLocation(), 50, 0.5, 1, 0.5, 0.1);

        whoMoves.sendRichMessage(plugin.getConfig().getString("messages.teleported", ""));
        whoStays.sendRichMessage(plugin.getConfig().getString("messages.teleported", ""));
    }

    public void denyRequest(Player target) {
        TpaRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) return;

        Player sender = Bukkit.getPlayer(request.senderId());
        if (sender != null && sender.isOnline()) {
            sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);

            String msg = plugin.getConfig().getString("messages.request-denied", "")
                    .replace("<player>", target.getName());
            sender.sendRichMessage(msg);
        }
    }

    public void clearAll() {
        pendingTeleports.values().forEach(BukkitTask::cancel);
        pendingTeleports.clear();
        pendingRequests.clear();
    }

    public record TpaRequest(UUID senderId, UUID targetId, boolean here, long timestamp) {}
}
