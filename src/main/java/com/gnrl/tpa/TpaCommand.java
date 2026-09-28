package com.gnrl.tpa;

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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("فقط بازیکن‌ها می‌تونن از این دستور استفاده کنن.");
            return true;
        }

        if (!player.hasPermission("gnrltpa.use")) {
            player.sendMessage(plugin.getConfig().getString("messages.no-permission"));
            return true;
        }

        if (args.length == 0) {
            new TpaMenu(plugin, manager).openMenu(player);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getConfig().getString("messages.player-not-found"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getConfig().getString("messages.self-request"));
            return true;
        }

        if (manager.isOnCooldown(player)) {
            player.sendMessage("<red>لطفاً قبل از ارسال درخواست بعدی صبر کن.</red>");
            return true;
        }

        if (manager.hasPendingRequest(target)) {
            player.sendMessage("<red>این بازیکن از قبل یه درخواست در انتظار داره.</red>");
            return true;
        }

        manager.sendRequest(player, target);
        manager.setCooldown(player);

        player.sendMessage(plugin.getConfig().getString("messages.request-sent")
                .replace("<player>", target.getName()));
        target.sendMessage(plugin.getConfig().getString("messages.request-received")
                .replace("<player>", player.getName()));

        return true;
    }
}
