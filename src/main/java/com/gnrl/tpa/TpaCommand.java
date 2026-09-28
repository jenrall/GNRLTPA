package com.gnrl.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpaCommand implements CommandExecutor {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaCommand(GNRLTPA plugin, TpaManager manager) {
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
            player.sendRichMessage("<yellow>Usage: /tpa <player></yellow>");
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

        manager.sendRequest(player, target);
        manager.setCooldown(player);

        String sentMsg = plugin.getConfig().getString("messages.request-sent", "")
                .replace("<player>", target.getName());
        player.sendRichMessage(sentMsg);

        String recvMsg = plugin.getConfig().getString("messages.request-received", "")
                .replace("<player>", player.getName());
        target.sendRichMessage(recvMsg);

        Component accept = Component.text("[Accept]", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/tpaccept"));
        Component deny = Component.text("[Deny]", NamedTextColor.RED)
                .clickEvent(ClickEvent.runCommand("/tpdeny"));
        Component buttons = accept.append(Component.text("  ")).append(deny);

        target.sendMessage(buttons);

        return true;
    }
}
