package com.gnrl.tpa;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TpaHereCommand implements CommandExecutor {

    private final GNRLTPA plugin;
    private final TpaManager manager;
    private final TpaMenu menu;

    public TpaHereCommand(GNRLTPA plugin, TpaManager manager, TpaMenu menu) {
        this.plugin = plugin;
        this.manager = manager;
        this.menu = menu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("این دستور فقط برای بازیکنان قابل استفاده است.");
            return true;
        }

        if (plugin.getServer().getOnlinePlayers().size() <= 1) {
            player.sendRichMessage("<yellow>هیچ بازیکن دیگری برای ارسال درخواست آنلاین نیست.</yellow>");
            return true;
        }

        menu.openMenu(player, true); // here = true

        return true;
    }
}
