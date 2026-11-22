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
        // 1. بررسی اینکه فرستنده یک بازیکن است
        if (!(sender instanceof Player player)) {
            sender.sendMessage("این دستور فقط برای بازیکنان قابل استفاده است.");
            return true;
        }

        // 2. بررسی اینکه بازیکن به تعداد کافی آنلاین وجود دارد
        // (این بررسی در متد openMenu هم انجام می‌شود، اما برای پیام‌دهی بهتر است اینجا هم چک شود)
        if (plugin.getServer().getOnlinePlayers().size() <= 1) {
            player.sendRichMessage("<yellow>هیچ بازیکن دیگری برای ارسال درخواست آنلاین نیست.</yellow>");
            return true;
        }

        // 3. ساخت نمونه‌ای از TpaMenu و باز کردن منو
        // از آنجایی که در TpaMenu از Dialog استفاده می‌کنیم، پارامتر here را false می‌فرستیم (برای /tpa)
        TpaMenu menu = new TpaMenu(plugin, manager);
        menu.openMenu(player, false);

        return true;
    }
}
