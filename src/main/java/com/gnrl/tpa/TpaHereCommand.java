package com.gnrl.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpaHereCommand implements CommandExecutor {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaHereCommand(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("gnrltpa.use")) {
            player.sendRichMessage(plugin.getConfig().getString("messages.no-permission", ""));
            return true;
        }

        if (args.length == 0) {
            player.sendRichMessage("<yellow>Usage: /tpahere <player></yellow>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendRichMessage(plugin.getConfig().getString("messages.player-not-found", ""));
            return true;
        }

        if (target.equals(player)) {
            player.sendRichMessage(plugin.getConfig().getString("messages.self-request", ""));
            return true;
        }

        if (manager.isOnCooldown(player)) {
            player.sendRichMessage(plugin.getConfig().getString("messages.cooldown", ""));
            return true;
        }

        if (manager.hasPendingRequest(target)) {
            player.sendRichMessage(plugin.getConfig().getString("messages.already-pending", ""));
            return true;
        }

        manager.sendRequest(player, target, true);
        manager.setCooldown(player);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

        // پیام قاب‌دار برای فرستنده
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("  ╭──────────────────────────╮", NamedTextColor.GOLD));
        player.sendMessage(Component.text("  │  ", NamedTextColor.GOLD)
                .append(Component.text("✦ TPAHere Sent ✦", NamedTextColor.AQUA))
                .append(Component.text("      │", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("  │  ", NamedTextColor.GOLD)
                .append(Component.text("to: ", NamedTextColor.GRAY))
                .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                .append(Component.text("              │", NamedTextColor.GOLD)));
        player.sendMessage(Component.text("  ╰──────────────────────────╯", NamedTextColor.GOLD));
        player.sendMessage(Component.text(""));

        TpaCommand.sendRequestToTarget(player, target, true);

        return true;
    }
}
