package com.gnrl.tpa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpaToggleCommand implements CommandExecutor {

    private final GNRLTPA plugin;

    public TpaToggleCommand(GNRLTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        boolean nowDisabled = plugin.getPlayerSettings().toggleTpa(player.getUniqueId());

        if (nowDisabled) {
            player.sendRichMessage(plugin.getConfig().getString("messages.tpa-toggled-off", ""));
        } else {
            player.sendRichMessage(plugin.getConfig().getString("messages.tpa-toggled-on", ""));
        }

        return true;
    }
}
