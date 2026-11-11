package com.gnrl.tpa;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TpaMoveListener implements Listener {

    private final TpaManager manager;

    public TpaMoveListener(TpaManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!manager.isTeleportPending(player)) return;

        // بررسی حرکت واقعی (نه فقط چرخش سر)
        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            manager.cancelTeleportOnMove(player);
        }
    }
}
