package com.gnrl.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TpaMenu implements CommandExecutor, Listener {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        openMenu(player);
        return true;
    }

    public void openMenu(Player player) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) {
                onlinePlayers.add(p);
            }
        }

        if (onlinePlayers.isEmpty()) {
            player.sendRichMessage("<yellow>No players online.</yellow>");
            return;
        }

        int size = 27;
        Inventory inv = Bukkit.createInventory(null, size,
                Component.text("TPA Menu", NamedTextColor.DARK_GRAY));

        int slot = 10;
        for (Player target : onlinePlayers) {
            if (slot >= size) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text(target.getName(), NamedTextColor.YELLOW));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Click to send TPA request", NamedTextColor.GRAY));
            meta.lore(lore);
            head.setItemMeta(meta);

            inv.setItem(slot, head);
            slot++;

            if (slot % 9 == 8) slot += 2;
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = event.getView().title();
        if (!title.equals(Component.text("TPA Menu", NamedTextColor.DARK_GRAY))) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

        if (!(clicked.getItemMeta() instanceof SkullMeta skullMeta)) return;
        if (skullMeta.getOwningPlayer() == null) return;

        Player target = skullMeta.getOwningPlayer().getPlayer();
        if (target == null || !target.isOnline()) {
            player.sendRichMessage("<red>Player is no longer online.</red>");
            player.closeInventory();
            return;
        }

        player.closeInventory();
        player.performCommand("tpa " + target.getName());
    }
}
