package com.gnrl.tpa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TpaReloadCommand implements CommandExecutor {

    private final GNRLTPA plugin;

    public TpaReloadCommand(GNRLTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("gnrltpa.admin")) {
            sender.sendRichMessage(plugin.getConfig().getString("messages.no-permission", ""));
            return true;
        }

        plugin.reloadConfig();
        sender.sendRichMessage(plugin.getConfig().getString("messages.reloaded", ""));
        return true;
    }
}
