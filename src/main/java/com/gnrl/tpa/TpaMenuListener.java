package com.gnrl.tpa;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class TpaMenuListener implements Listener {

    private final GNRLTPA plugin;
    private final TpaManager manager;
    private final TpaMenu menu;

    public TpaMenuListener(GNRLTPA plugin, TpaManager manager, TpaMenu menu) {
        this.plugin = plugin;
        this.manager = manager;
        this.menu = menu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        boolean isTpa = event.getView().title().equals(TpaMenu.TPA_TITLE);
        boolean isTpaHere = event.getView().title().equals(TpaMenu.TPAHERE_TITLE);

        if (!isTpa && !isTpaHere) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        int size = event.getInventory().getSize();

        // دکمه Close
        if (slot == size - 5) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);
            player.closeInventory();
            return;
        }

        // دکمه Refresh
        if (slot == size - 1) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
            menu.openMenu(player, isTpaHere, menu.getPage(player));
            return;
        }

        // دکمه Previous
        if (slot == size - 9) {
            int page = menu.getPage(player);
            menu.openMenu(player, isTpaHere, page - 1);
            return;
        }

        // دکمه Next
        if (slot == size - 2) {
            int page = menu.getPage(player);
            menu.openMenu(player, isTpaHere, page + 1);
            return;
        }

        // کلیک روی سر بازیکن
        if (clicked.getType() != Material.PLAYER_HEAD) return;
        if (!(clicked.getItemMeta() instanceof SkullMeta skullMeta)) return;
        if (skullMeta.getOwningPlayer() == null) return;

        Player target = skullMeta.getOwningPlayer().getPlayer();
        if (target == null || !target.isOnline()) {
            player.sendRichMessage("<red>Player is no longer online.</red>");
            player.closeInventory();
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
        player.closeInventory();

        if (isTpaHere) {
            player.performCommand("tpahere " + target.getName());
        } else {
            player.performCommand("tpa " + target.getName());
        }
    }
}
