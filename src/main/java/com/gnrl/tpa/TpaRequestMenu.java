package com.gnrl.tpa;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;

public class TpaRequestMenu {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaRequestMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void openRequestMenu(Player receiver, Player sender, boolean here) {
        Gui gui = Gui.gui()
                .title(Component.text(here ? "✦ TPAHere Request ✦" : "✦ TPA Request ✦", here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                .rows(3)
                .disableAllInteractions()
                .create();

        gui.getFiller().fill(ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
                .name(Component.text(" "))
                .asGuiItem());

        gui.setItem(13, ItemBuilder.from(Material.PLAYER_HEAD)
                .name(Component.text("✦ " + sender.getName() + " ✦", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.empty(),
                        Component.text("⚡ Status: ", NamedTextColor.GRAY).append(Component.text("Online", NamedTextColor.GREEN)).decoration(TextDecoration.ITALIC, false),
                        Component.text("❤ Health: ", NamedTextColor.GRAY).append(Component.text((int) sender.getHealth() + "/20", NamedTextColor.RED)).decoration(TextDecoration.ITALIC, false),
                        Component.text("📍 World: ", NamedTextColor.GRAY).append(Component.text(sender.getWorld().getName(), NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text(here ? "Wants you to teleport to them" : "Wants to teleport to you", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
                )
                .asGuiItem(item -> {
                    if (item.getItemMeta() instanceof SkullMeta meta) {
                        meta.setOwningPlayer(sender);
                        item.setItemMeta(meta);
                    }
                }));

        gui.setItem(11, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE)
                .name(Component.text("✔ ACCEPT", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Click to accept this request", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    receiver.closeInventory();
                    manager.confirmRequest(receiver);
                }));

        gui.setItem(15, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text("✖ DENY", NamedTextColor.RED).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false))
                .lore(Component.text("Click to deny this request", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .asGuiItem(event -> {
                    event.setCancelled(true);
                    receiver.closeInventory();
                    manager.denyRequest(receiver);
                }));

        receiver.playSound(receiver.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
        gui.open(receiver);
    }
}
