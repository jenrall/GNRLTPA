package com.gnrl.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TpaMenu implements CommandExecutor, Listener {

    private final GNRLTPA plugin;
    private final TpaManager manager;
    private static final Component MENU_TITLE = Component.text("✦ GNRL TPA ✦", NamedTextColor.GOLD);

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

        int rows = 6;
        int size = rows * 9;
        Inventory inv = Bukkit.createInventory(null, size, MENU_TITLE);

        // قاب شیشه‌ای دور منو
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.text(" ", NamedTextColor.GRAY));
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
            }
        }

        // دکمه‌های تزئینی بالای منو
        ItemStack logo = new ItemStack(Material.NETHER_STAR);
        ItemMeta logoMeta = logo.getItemMeta();
        logoMeta.displayName(Component.text("✦ GNRLTPA ✦", NamedTextColor.GOLD));

        List<Component> logoLore = new ArrayList<>();
        logoLore.add(Component.text(""));
        logoLore.add(Component.text("Plugin by ", NamedTextColor.GRAY)
                .append(Component.text("GNRL", NamedTextColor.YELLOW)));
        logoLore.add(Component.text("github.com/jenrall/GNRLTPA", NamedTextColor.DARK_GRAY));
        logoLore.add(Component.text("v1.0.0", NamedTextColor.DARK_GRAY));
        logoMeta.lore(logoLore);
        logo.setItemMeta(logoMeta);
        inv.setItem(4, logo);

        // سر بازیکن‌ها
        int[] slots = {10, 11, 12, 13, 14, 15, 16,
                       19, 20, 21, 22, 23, 24, 25,
                       28, 29, 30, 31, 32, 33, 34};

        int index = 0;
        for (Player target : onlinePlayers) {
            if (index >= slots.length) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text("✦ " + target.getName() + " ✦", NamedTextColor.YELLOW));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("  ⚡ Status: ", NamedTextColor.GRAY)
                    .append(Component.text("Online", NamedTextColor.GREEN)));
            lore.add(Component.text("  ❤ Health: ", NamedTextColor.GRAY)
                    .append(Component.text((int) target.getHealth() + "/20", NamedTextColor.RED)));
            lore.add(Component.text("  📍 World: ", NamedTextColor.GRAY)
                    .append(Component.text(target.getWorld().getName(), NamedTextColor.AQUA)));
            lore.add(Component.text(""));
            lore.add(Component.text("  ▸ Click to send TPA", NamedTextColor.GREEN));

            meta.lore(lore);
            head.setItemMeta(meta);

            inv.setItem(slots[index], head);
            index++;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(MENU_TITLE)) return;

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

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
        player.closeInventory();
        player.performCommand("tpa " + target.getName());
    }
}
