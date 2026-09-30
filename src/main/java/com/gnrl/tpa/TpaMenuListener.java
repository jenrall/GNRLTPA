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

        // منوی انتخاب بازیکن (TPA / TPAHere)
        boolean isTpa = event.getView().title().equals(TpaMenu.TPA_TITLE);
        boolean isTpaHere = event.getView().title().equals(TpaMenu.TPAHERE_TITLE);

        // منوی درخواست (گیرنده)
        boolean isRequest = event.getView().title().equals(TpaRequestMenu.TITLE);
        boolean isRequestHere = event.getView().title().equals(TpaRequestMenu.TITLE_HERE);

        if (isTpa || isTpaHere) {
            handleSelectMenu(event, player, isTpaHere);
            return;
        }

        if (isRequest || isRequestHere) {
            handleRequestMenu(event, player);
            return;
        }
    }

    private void handleSelectMenu(InventoryClickEvent event, Player player, boolean isTpaHere) {
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        int size = event.getInventory().getSize();

        if (slot == size - 5) { // Close
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);
            player.closeInventory();
            return;
        }

        if (slot == size - 1) { // Refresh
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
            menu.openMenu(player, isTpaHere, menu.getPage(player));
            return;
        }

        if (slot == size - 9) { // Previous
            menu.openMenu(player, isTpaHere, menu.getPage(player) - 1);
            return;
        }

        if (slot == size - 2) { // Next
            menu.openMenu(player, isTpaHere, menu.getPage(player) + 1);
            return;
        }

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

    private void handleRequestMenu(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot == 11) { // Accept
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            player.closeInventory();
            player.performCommand("tpaccept");
        } else if (slot == 15) { // Deny
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            player.closeInventory();
            player.performCommand("tpdeny");
        }
    }
}
