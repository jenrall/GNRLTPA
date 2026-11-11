package com.gnrl.tpa;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;

public class TpaMoveListener implements Listener {

    private final TpaManager manager;

    public TpaMoveListener(TpaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!manager.isTeleportPending(player)) return;

        if (event.getFrom().distanceSquared(event.getTo()) > 0.01) {
            manager.cancelTeleportOnMove(player);
        }
    }
}
