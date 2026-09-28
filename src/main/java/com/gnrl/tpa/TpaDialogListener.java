package com.gnrl.tpa;

import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TpaDialogListener implements Listener {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaDialogListener(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onCustomClick(PlayerCustomClickEvent event) {
        Player player = (Player) event.getPlayer();
        Key key = event.getIdentifier();
        String keyString = key.asString();

        // کلیک روی دکمه بستن
        if (keyString.equals("gnrltpa:close")) {
            player.closeDialog();
            return;
        }

        // کلیک روی دکمه ارسال TPA
        if (keyString.startsWith("gnrltpa:send_tpa_")) {
            String targetName = keyString.substring("gnrltpa:send_tpa_".length());
            Player target = Bukkit.getPlayerExact(targetName);

            if (target == null || !target.isOnline()) {
                player.sendRichMessage(plugin.getConfig().getString("messages.player-not-found", ""));
                return;
            }

            player.closeDialog();

            // فراخوانی دستور tpa
            player.performCommand("tpa " + target.getName());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // پاک‌سازی (اگه لازم شد)
    }
}
