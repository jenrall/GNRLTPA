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

        // صدای قبول برای هر دو
        sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);

        int delay = plugin.getConfig().getInt("settings.teleport-delay-seconds", 3);
        String teleportingMsg = plugin.getConfig().getString("messages.teleporting", "")
                .replace("<seconds>", String.valueOf(delay));

        if (delay <= 0) {
            doTeleport(sender, target);
            return;
        }

        sender.sendRichMessage(teleportingMsg);
        target.sendRichMessage(teleportingMsg);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(sender.getUniqueId());
            if (sender.isOnline() && target.isOnline()) {
                doTeleport(sender, target);
            }
        }, delay * 20L);

        pendingTeleports.put(sender.getUniqueId(), task);
    }

    private void doTeleport(Player sender, Player target) {
        // ذرات قبل از تلپورت
        sender.getWorld().spawnParticle(Particle.PORTAL, sender.getLocation(), 50, 0.5, 1, 0.5, 0.1);
        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 50, 0.5, 1, 0.5, 0.1);

        // صدای تلپورت
        sender.playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        sender.teleport(target.getLocation());

        // ذرات بعد از تلپورت
        sender.getWorld().spawnParticle(Particle.REVERSE_PORTAL, sender.getLocation(), 50, 0.5, 1, 0.5, 0.1);

        sender.sendRichMessage(plugin.getConfig().getString("messages.teleported", ""));
        target.sendRichMessage(plugin.getConfig().getString("messages.teleported", ""));
    }

    public void denyRequest(Player target) {
        TpaRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) return;

        Player sender = Bukkit.getPlayer(request.senderId());
        if (sender != null && sender.isOnline()) {
            // صدای رد
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

    public record TpaRequest(UUID senderId, UUID targetId, long timestamp) {}
}
