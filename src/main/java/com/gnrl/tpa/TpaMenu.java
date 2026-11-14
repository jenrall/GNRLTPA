package com.gnrl.tpa;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class TpaMenu {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void openMenu(Player player, boolean here) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) onlinePlayers.add(p);
        }

        if (onlinePlayers.isEmpty()) {
            player.sendRichMessage("<yellow>No players online.</yellow>");
            return;
        }

        PaginatedGui gui = Gui.paginated()
                .title(Component.text(here ? "✦ TPAHere Menu ✦" : "✦ TPA Menu ✦", here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                .rows(6)
                .pageSize(21)
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .asGuiItem());

        // لوگو بالای منو
        gui.setItem(4, ItemBuilder.from(here ? Material.ENDER_PEARL : Material.NETHER_STAR)
                .name(Component.text(here ? "✦ TPAHere ✦" : "✦ GNRLFlawless TPA ✦", here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.empty(),
                        Component.text(here ? "Request players to come to you" : "Request to teleport to a player", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Plugin by ", NamedTextColor.GRAY).append(Component.text("GNRLFlawless", NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false)
                )
                .asGuiItem());

        // دکمه‌های پایین
        gui.setItem(6, 5, ItemBuilder.from(Material.BARRIER)
                .name(Component.text("✖ Close", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> event.getWhoClicked().closeInventory()));

        gui.setItem(6, 1, ItemBuilder.from(Material.ARROW)
                .name(Component.text("◀ Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> gui.previous()));

        gui.setItem(6, 9, ItemBuilder.from(Material.ARROW)
                .name(Component.text("Next Page ▶", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> gui.next()));

        gui.setItem(6, 4, ItemBuilder.from(Material.PAPER)
                .name(Component.text("⟳ Refresh", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> {
                    player.closeInventory();
                    openMenu(player, here);
                }));

        // اضافه کردن بازیکنان به لیست
        for (Player target : onlinePlayers) {
            GuiItem head = ItemBuilder.from(Material.PLAYER_HEAD)
                    .name(Component.text("✦ " + target.getName() + " ✦", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                    .lore(
                            Component.empty(),
                            Component.text("  ⚡ Status: ", NamedTextColor.GRAY).append(Component.text("Online", NamedTextColor.GREEN)).decoration(TextDecoration.ITALIC, false),
                            Component.text("  ❤ Health: ", NamedTextColor.GRAY).append(Component.text((int) target.getHealth() + "/20", NamedTextColor.RED)).decoration(TextDecoration.ITALIC, false),
                            Component.text("  📍 World: ", NamedTextColor.GRAY).append(Component.text(target.getWorld().getName(), NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false),
                            Component.empty(),
                            Component.text(here ? "  ▸ Click to request here" : "  ▸ Click to send TPA", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                    )
                    .asGuiItem(event -> {
                        event.setCancelled(true);
                        player.closeInventory();
                        if (here) {
                            manager.sendRequest(player, target, true);
                        } else {
                            manager.sendRequest(player, target, false);
                        }
                    });

            if (head.getItemStack().getItemMeta() instanceof SkullMeta meta) {
                meta.setOwningPlayer(target);
                head.getItemStack().setItemMeta(meta);
            }
            gui.addItem(head);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
        gui.open(player);
    }
}
