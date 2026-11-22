package com.gnrl.tpa;

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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("این دستور فقط برای بازیکنان قابل استفاده است.");
            return true;
        }

        if (plugin.getServer().getOnlinePlayers().size() <= 1) {
            player.sendRichMessage("<yellow>هیچ بازیکن دیگری برای ارسال درخواست آنلاین نیست.</yellow>");
            return true;
        }

        // ساخت TpaMenu و باز کردن دیالوگ جستجو
        TpaMenu menu = new TpaMenu(plugin, manager);
        menu.openMenu(player, false);

        return true;
    }
}
