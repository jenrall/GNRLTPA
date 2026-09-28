package com.gnrl.tpa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TpaManager {

    private final GNRLTPA plugin;
    private final Map<UUID, TpaRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();

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

    public void sendRequest(Player sender, Player target) {
        UUID targetId = target.getUniqueId();
        UUID senderId = sender.getUniqueId();
        pendingRequests.put(targetId, new TpaRequest(senderId, targetId, System.currentTimeMillis()));

        int timeout = plugin.getConfig().getInt("settings.request-timeout-seconds", 60);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            TpaRequest req = pendingRequests.get(targetId);
            if (req != null && req.senderId().equals(senderId)) {
                pendingRequests.remove(targetId);
            }
        }, timeout * 20L);
    }

    public boolean hasPendingRequest(Player target) {
        return pendingRequests.containsKey(target.getUniqueId());
    }

    public void acceptRequest(Player target) {
        TpaRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) return;

        Player sender = Bukkit.getPlayer(request.senderId());
        if (sender == null || !sender.isOnline()) return;

        int delay = plugin.getConfig().getInt("settings.teleport-delay-seconds", 3);
        String teleportingMsg = plugin.getConfig().getString("messages.teleporting", "")
                .replace("<seconds>", String.valueOf(delay));

        if (delay <= 0) {
            sender.teleport(target.getLocation());
            sender.sendRichMessage(plugin.getConfig().getString("messages.teleported", ""));
            target.sendRichMessage(plugin.getConfig().getString("messages.teleported", ""));
            return;
        }

        sender.sendRichMessage(teleportingMsg);
        target.sendRichMessage(teleportingMsg);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(sender.getUniqueId());
            if (sender.isOnline() && target.isOnline()) {
                sender.teleport(target.getLocation());
                sender.sendRichMessage(plugin.getConfig().getString("messages.teleported", ""));
                target.sendRichMessage(plugin.getConfig().getString("messages.teleported", ""));
            }
        }, delay * 20L);

        pendingTeleports.put(sender.getUniqueId(), task);
    }

    public void denyRequest(Player target) {
        TpaRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) return;

        Player sender = Bukkit.getPlayer(request.senderId());
        if (sender != null && sender.isOnline()) {
            String msg = plugin.getConfig().getString("messages.request-denied", "")
                    .replace("<player>", target.getName());
            sender.sendRichMessage(msg);
        }
    }

    public void cancelTeleport(Player player) {
        BukkitTask task = pendingTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            player.sendRichMessage(plugin.getConfig().getString("messages.teleport-cancelled", ""));
        }
    }

    public void clearAll() {
        pendingTeleports.values().forEach(BukkitTask::cancel);
        pendingTeleports.clear();
        pendingRequests.clear();
    }

    public record TpaRequest(UUID senderId, UUID targetId, long timestamp) {}
}
