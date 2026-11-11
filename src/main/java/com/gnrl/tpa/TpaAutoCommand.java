package com.gnrl.tpa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpaAutoCommand implements CommandExecutor {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaAutoCommand(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.sendRichMessage("<yellow>Usage: /tpauto <allow|deny|toggle></yellow>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "allow" -> {
                manager.setAutoMode(player, TpaManager.AutoMode.ALLOW);
                player.sendRichMessage(plugin.getConfig().getString("messages.auto-allow-on",
                        "<green>✔ Auto-Accept <yellow>ENABLED</yellow>.</green>"));
            }
            case "deny" -> {
                manager.setAutoMode(player, TpaManager.AutoMode.DENY);
                player.sendRichMessage(plugin.getConfig().getString("messages.auto-deny-on",
                        "<red>✔ Auto-Deny <yellow>ENABLED</yellow>.</red>"));
            }
            case "toggle" -> {
                manager.setAutoMode(player, TpaManager.AutoMode.ASK);
                player.sendRichMessage(plugin.getConfig().getString("messages.auto-ask-on",
                        "<gray>✔ Auto mode <yellow>DISABLED</yellow> (ask).</gray>"));
            }
            default -> player.sendRichMessage("<yellow>Usage: /tpauto <allow|deny|toggle></yellow>");
        }

        return true;
    }
}
