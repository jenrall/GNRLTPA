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
            player.sendRichMessage("<yellow>Usage: /tpauto <toggle|deny|accept></yellow>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "toggle" -> {
                TpaManager.AutoMode next = manager.cycleAutoMode(player);
                String key = switch (next) {
                    case ALLOW -> "messages.auto-allow-on";
                    case DENY  -> "messages.auto-deny-on";
                    case ASK   -> "messages.auto-ask-on";
                };
                String def = switch (next) {
                    case ALLOW -> "<green>✔ Auto-Accept <yellow>ENABLED</yellow>.</green>";
                    case DENY  -> "<red>✔ Auto-Deny <yellow>ENABLED</yellow>.</red>";
                    case ASK   -> "<gray>✔ Auto mode <yellow>DISABLED</yellow> (manual accept).</gray>";
                };
                player.sendRichMessage(plugin.getConfig().getString(key, def));
            }
            case "deny" -> {
                manager.setAutoMode(player, TpaManager.AutoMode.DENY);
                player.sendRichMessage(plugin.getConfig().getString("messages.auto-deny-on",
                        "<red>✔ Auto-Deny <yellow>ENABLED</yellow>.</red>"));
            }
            case "accept" -> {
                manager.setAutoMode(player, TpaManager.AutoMode.ASK);
                player.sendRichMessage(plugin.getConfig().getString("messages.auto-ask-on",
                        "<gray>✔ Auto mode <yellow>DISABLED</yellow> (manual accept).</gray>"));
            }
            default -> player.sendRichMessage("<yellow>Usage: /tpauto <toggle|deny|accept></yellow>");
        }

        return true;
    }
}
