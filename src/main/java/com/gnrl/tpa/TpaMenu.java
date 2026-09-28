package com.gnrl.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TpaMenu implements CommandExecutor, Listener {

    private final GNRLTPA plugin;
    private final TpaManager manager;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public TpaMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("gnrltpa.menu")) {
            player.sendMessage(plugin.getConfig().getString("messages.no-permission"));
            return true;
        }
        openMenu(player);
        return true;
    }

    public void openMenu(Player player) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.remove(player);

        if (onlinePlayers.isEmpty()) {
            player.sendMessage("<yellow>هیچ بازیکنی آنلاین نیست که بهش درخواست بدی.</yellow>");
            return;
        }

        int page = playerPages.getOrDefault(player.getUniqueId(), 0);
        int perPage = 7;
        int totalPages = (int) Math.ceil((double) onlinePlayers.size() / perPage);
        if (page >= totalPages) page = totalPages - 1;

        String rawTitle = plugin.getConfig().getString("gui.menu-title", "<dark_gray>منوی TPA");
        Component title = mm.deserialize(rawTitle);
        int rows = plugin.getConfig().getInt("gui.menu-rows", 3);

        Inventory inv = Bukkit.createInventory(null, rows * 9, title);

        int start = page * perPage;
        int slot = 10;

        for (int i = start; i < Math.min(start + perPage, onlinePlayers.size()); i++) {
            Player target = onlinePlayers.get(i);
            ItemStack head = createPlayerHead(target);
            inv.setItem(slot++, head);
        }

        // دکمه‌های ناوبری
        if (page > 0) {
            inv.setItem(rows * 9 - 6, createNavItem(Material.ARROW, "<green>صفحه قبل</green>"));
        }
        if (page < totalPages - 1) {
            inv.setItem(rows * 9 - 4, createNavItem(Material.ARROW, "<green>صفحه بعد</green>"));
        }

        player.openInventory(inv);
    }

    private ItemStack createPlayerHead(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.displayName(mm.deserialize("<yellow>" + target.getName() + "</yellow>"));
        meta.lore(List.of(
                mm.deserialize("<gray>برای ارسال درخواست TPA کلیک کن</gray>"),
                mm.deserialize("<dark_gray>سلامتی: " + (int) target.getHealth() + "/20</dark_gray>")
        ));
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack createNavItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(mm.deserialize(name));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(mm.deserialize(
                plugin.getConfig().getString("gui.menu-title", "<dark_gray>منوی TPA")))) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        int rows = plugin.getConfig().getInt("gui.menu-rows", 3);

        // ناوبری
        if (slot == rows * 9 - 6) {
            int page = playerPages.getOrDefault(player.getUniqueId(), 0);
            playerPages.put(player.getUniqueId(), Math.max(0, page - 1));
            openMenu(player);
            return;
        }
        if (slot == rows * 9 - 4) {
            int page = playerPages.getOrDefault(player.getUniqueId(), 0);
            playerPages.put(player.getUniqueId(), page + 1);
            openMenu(player);
            return;
        }

        // کلیک روی سر بازیکن
        if (clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta() instanceof SkullMeta skullMeta) {
            if (skullMeta.getOwningPlayer() != null) {
                Player target = skullMeta.getOwningPlayer().getPlayer();
                if (target != null && target.isOnline() && !target.equals(player)) {
                    player.closeInventory();
                    player.performCommand("tpa " + target.getName());
                }
            }
        }
    }
}
