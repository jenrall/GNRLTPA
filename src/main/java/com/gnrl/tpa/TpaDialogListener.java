package com.gnrl.tpa;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TpaDialogListener implements Listener {

    private final GNRLTPA plugin;

    public TpaDialogListener(GNRLTPA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        Player player = conn.getPlayer();

        Key key = event.getIdentifier();
        String keyString = key.asString();

        if (keyString.equals("gnrltpa:close")) {
            player.closeDialog();
            return;
        }

        if (keyString.startsWith("gnrltpa:send_tpa_")) {
            String targetName = keyString.substring("gnrltpa:send_tpa_".length());
            Player target = null;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equalsIgnoreCase(targetName)) {
                    target = p;
                    break;
                }
            }
            if (target == null) {
                player.sendRichMessage("<red>Player not found.</red>");
                return;
            }
            player.closeDialog();
            player.performCommand("tpa " + target.getName());
        }
    }
}
