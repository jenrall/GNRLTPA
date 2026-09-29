package com.gnrl.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TpaMenu {

    public static final Component TPA_TITLE = Component.text("✦ TPA Menu ✦", NamedTextColor.GOLD);
    public static final Component TPAHERE_TITLE = Component.text("✦ TPAHere Menu ✦", NamedTextColor.AQUA);

    private static final int PER_PAGE = 21;
    private final Map<UUID, Integer> pages = new HashMap<>();

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void openMenu(Player player, boolean here) {
        openMenu(player, here, 0);
    }

    public void openMenu(Player player, boolean here, int page) {
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

        int totalPages = (int) Math.ceil((double) onlinePlayers.size() / PER_PAGE);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        pages.put(player.getUniqueId(), page);

        Component title = here ? TPAHERE_TITLE : TPA_TITLE;
        int rows = plugin.getConfig().getInt("gui.rows", 6);
        int size = rows * 9;
        Inventory inv = Bukkit.createInventory(null, size, title);

        // قاب شیشه‌ای
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.text(" ", NamedTextColor.GRAY));
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
            }
        }

        // لوگو وسط بالا
        ItemStack logo = new ItemStack(here ? Material.ENDER_PEARL : Material.NETHER_STAR);
        ItemMeta logoMeta = logo.getItemMeta();
        logoMeta.displayName(Component.text(
                here ? "✦ TPAHere ✦" : "✦ GNRLFlawless TPA ✦",
                here ? NamedTextColor.AQUA : NamedTextColor.GOLD));
        List<Component> logoLore = new ArrayList<>();
        logoLore.add(Component.text(""));
        logoLore.add(Component.text(
                here ? "Request players to come to you" : "Request to teleport to a player",
                NamedTextColor.GRAY));
        logoLore.add(Component.text(""));
        logoLore.add(Component.text("Plugin by ", NamedTextColor.GRAY)
                .append(Component.text("GNRLFlawless", NamedTextColor.YELLOW)));
        logoMeta.lore(logoLore);
        logo.setItemMeta(logoMeta);
        inv.setItem(4, logo);

        // سر بازیکن‌ها
        int[] slots = {10, 11, 12, 13, 14, 15, 16,
                       19, 20, 21, 22, 23, 24, 25,
                       28, 29, 30, 31, 32, 33, 34};

        int start = page * PER_PAGE;
        int index = 0;
        for (int i = start; i < onlinePlayers.size() && index < slots.length; i++) {
            Player target = onlinePlayers.get(i);

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
            lore.add(Component.text(here ? "  ▸ Click to request here" : "  ▸ Click to send TPA",
                    NamedTextColor.GREEN));

            meta.lore(lore);
            head.setItemMeta(meta);

            inv.setItem(slots[index], head);
            index++;
        }

        // دکمه‌های ناوبری پایین
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("✖ Close", NamedTextColor.RED));
        close.setItemMeta(closeMeta);
        inv.setItem(size - 5, close);

        ItemStack refresh = new ItemStack(Material.SUNFLOWER);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.displayName(Component.text("⟳ Refresh", NamedTextColor.YELLOW));
        refresh.setItemMeta(refreshMeta);
        inv.setItem(size - 1, refresh);

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.displayName(Component.text("◀ Previous Page", NamedTextColor.GREEN));
            prev.setItemMeta(prevMeta);
            inv.setItem(size - 9, prev);
        }

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.displayName(Component.text("Next Page ▶", NamedTextColor.GREEN));
            next.setItemMeta(nextMeta);
            inv.setItem(size - 2, next);
        }

        // نمایش شماره صفحه
        ItemStack pageInfo = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageInfo.getItemMeta();
        pageMeta.displayName(Component.text("Page " + (page + 1) + "/" + totalPages, NamedTextColor.WHITE));
        pageInfo.setItemMeta(pageMeta);
        inv.setItem(size - 6, pageInfo);

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
        player.openInventory(inv);
    }

    public int getPage(Player player) {
        return pages.getOrDefault(player.getUniqueId(), 0);
    }
}
