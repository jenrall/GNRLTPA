package com.gnrl.tpa;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpaBlockCommand implements CommandExecutor {

    private final GNRLTPA plugin;

    public TpaBlockCommand(GNRLTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.sendRichMessage("<yellow>Usage: /tpblock <player></yellow>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendRichMessage(plugin.getConfig().getString("messages.player-not-found", ""));
            return true;
        }

        if (target.equals(player)) {
            player.sendRichMessage("<red>You can't block yourself.</red>");
            return true;
        }

        boolean nowBlocked = plugin.getPlayerSettings().toggleBlock(player.getUniqueId(), target.getUniqueId());

        if (nowBlocked) {
            player.sendRichMessage(plugin.getConfig().getString("messages.blocked-player", "")
                    .replace("<player>", target.getName()));
        } else {
            player.sendRichMessage(plugin.getConfig().getString("messages.unblocked-player", "")
                    .replace("<player>", target.getName()));
        }

        return true;
    }
}
