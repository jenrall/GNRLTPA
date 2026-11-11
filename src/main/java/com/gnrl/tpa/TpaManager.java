package com.gnrl.tpa;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TpaManager {

    private final GNRLTPA plugin;
    private final Map<UUID, TpaRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();
    private final Map<UUID, Location> teleportStartLocations = new ConcurrentHashMap<>();

    // حالت auto: هیچ‌کس در این Set نیست = ASK (پیش‌فرض)
    private final Set<UUID> autoAccept = ConcurrentHashMap.newKeySet();   // allow
    private final Set<UUID> autoDeny = ConcurrentHashMap.newKeySet();     // deny

    public TpaManager(GNRLTPA plugin) {
        this.plugin = plugin;
    }

    // ---------- Cooldown ----------
    public boolean isOnCooldown(Player player) {
        long cooldown = plugin.getConfig().getLong("settings.cooldown-seconds", 10) * 1000L;
        Long lastUse = cooldowns.get(player.getUniqueId());
        return lastUse != null && (System.currentTimeMillis() - lastUse) < cooldown;
    }

    public void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // ---------- Auto Modes ----------
    public enum AutoMode { ASK, ALLOW, DENY }

    public AutoMode getAutoMode(Player player) {
        UUID id = player.getUniqueId();
        if (autoAccept.contains(id)) return AutoMode.ALLOW;
        if (autoDeny.contains(id)) return AutoMode.DENY;
        return AutoMode.ASK;
    }

    public void setAutoMode(Player player, AutoMode mode) {
        UUID id = player.getUniqueId();
        autoAccept.remove(id);
        autoDeny.remove(id);
        switch (mode) {
            case ALLOW -> autoAccept.add(id);
            case DENY -> autoDeny.add(id);
            case ASK -> { /* هیچ‌کدام */ }
        }
    }

    public AutoMode cycleAutoMode(Player player) {
        AutoMode current = getAutoMode(player);
        AutoMode next = switch (current) {
            case ASK -> AutoMode.ALLOW;
            case ALLOW -> AutoMode.DENY;
            case DENY -> AutoMode.ASK;
        };
        setAutoMode(player, next);
        return next;
    }

    public boolean isAutoAccept(Player player) {
        return autoAccept.contains(player.getUniqueId());
    }

    // ---------- Send Request ----------
    public void sendRequest(Player sender, Player target, boolean here) {
        UUID targetId = target.getUniqueId();
        UUID senderId = sender.getUniqueId();

        pendingRequests.put(targetId, new TpaRequest(senderId, targetId, here, System.currentTimeMillis()));

        AutoMode mode = getAutoMode(target);

        // اگر ALLOW → فوری قبول
        if (mode == AutoMode.ALLOW) {
            sender.sendRichMessage("<green>" + target.getName() + " has auto-accept enabled.</green>");
            target.sendRichMessage("<green>Auto-accepted request from " + sender.getName() + "</green>");
            acceptRequest(target);
            return;
        }

        // اگر DENY → فوری رد
        if (mode == AutoMode.DENY) {
            pendingRequests.remove(targetId);
            sender.sendRichMessage("<red>" + target.getName() + " has auto-deny enabled.</red>");
            target.sendRichMessage("<red>Auto-denied request from " + sender.getName() + "</red>");
            return;
        }

        // حالت ASK → پیام با دکمه به گیرنده (GUI باز نمی‌شود)
        TpaHelper.sendRequestToTarget(plugin, sender, target, here);

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

    // ---------- Accept (بعد از کلیک روی Accept در چت → GUI باز می‌شود) ----------
    public void acceptRequest(Player target) {
        TpaRequest request = pendingRequests.get(target.getUniqueId());
        if (request == null) return;

        Player sender = Bukkit.getPlayer(request.senderId());
        if (sender == null || !sender.isOnline()) {
            pendingRequests.remove(target.getUniqueId());
            return;
        }

        // باز کردن GUI تأیید نهایی
        new TpaRequestMenu(plugin, this).openRequestMenu(target, sender, request.here());
    }

    // ---------- تأیید نهایی از داخل GUI ----------
    public void confirmRequest(Player target) {
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

        boolean cancelOnMove = plugin.getConfig().getBoolean("settings.cancel-on-move", true);
        Player whoMoves = request.here() ? target : sender;

        if (cancelOnMove) {
            teleportStartLocations.put(whoMoves.getUniqueId(), whoMoves.getLocation().clone());
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(whoMoves.getUniqueId());
            teleportStartLocations.remove(whoMoves.getUniqueId());
            if (sender.isOnline() && target.isOnline()) {
                doTeleport(sender, target, request.here());
            }
        }, delay * 20L);

        pendingTeleports.put(whoMoves.getUniqueId(), task);
    }

    // ---------- چک حرکت (از Listener صدا زده می‌شود) ----------
    public boolean isTeleportPending(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    public void cancelTeleportOnMove(Player player) {
        BukkitTask task = pendingTeleports.remove(player.getUniqueId());
        Location start = teleportStartLocations.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            player.sendRichMessage(plugin.getConfig().getString("messages.teleport-cancelled", ""));
        }
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
        teleportStartLocations.clear();
        pendingRequests.clear();
    }

    public record TpaRequest(UUID senderId, UUID targetId, boolean here, long timestamp) {}
}
