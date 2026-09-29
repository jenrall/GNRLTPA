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

        boolean enabled = manager.toggleAutoAccept(player);

        if (enabled) {
            player.sendRichMessage("<green>✔ Auto-Accept is now <yellow>ENABLED</yellow>.</green>");
        } else {
            player.sendRichMessage("<red>✖ Auto-Accept is now <yellow>DISABLED</yellow>.</red>");
        }

        return true;
    }
}
