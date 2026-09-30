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
import java.util.List;

public class TpaRequestMenu {

    public static final Component TITLE = Component.text("✦ TPA Request ✦", NamedTextColor.GOLD);
    public static final Component TITLE_HERE = Component.text("✦ TPAHere Request ✦", NamedTextColor.AQUA);

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaRequestMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void openRequestMenu(Player receiver, Player sender, boolean here) {
        Component title = here ? TITLE_HERE : TITLE;
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // قاب شیشه‌ای
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.text(" ", NamedTextColor.GRAY));
        glass.setItemMeta(glassMeta);

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, glass);
            }
        }

        // سر فرستنده با اطلاعات کامل
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(sender);
        headMeta.displayName(Component.text("✦ " + sender.getName() + " ✦", NamedTextColor.YELLOW));

        List<Component> headLore = new ArrayList<>();
        headLore.add(Component.text(""));
        headLore.add(Component.text("  ⚡ Status: ", NamedTextColor.GRAY)
                .append(Component.text("Online", NamedTextColor.GREEN)));
        headLore.add(Component.text("  ❤ Health: ", NamedTextColor.GRAY)
                .append(Component.text((int) sender.getHealth() + "/20", NamedTextColor.RED)));
        headLore.add(Component.text("  📍 World: ", NamedTextColor.GRAY)
                .append(Component.text(sender.getWorld().getName(), NamedTextColor.AQUA)));
        headLore.add(Component.text("  📐 Coords: ", NamedTextColor.GRAY)
                .append(Component.text(
                        (int) sender.getLocation().getX() + ", " +
                        (int) sender.getLocation().getY() + ", " +
                        (int) sender.getLocation().getZ(),
                        NamedTextColor.WHITE)));
        headLore.add(Component.text(""));
        headLore.add(Component.text(
                here ? "  Wants you to teleport to them" : "  Wants to teleport to you",
                NamedTextColor.GOLD));

        headMeta.lore(headLore);
        head.setItemMeta(headMeta);
        inv.setItem(13, head);

        // دکمه Accept
        ItemStack accept = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta acceptMeta = accept.getItemMeta();
        acceptMeta.displayName(Component.text("✔ ACCEPT", NamedTextColor.GREEN));
        List<Component> acceptLore = new ArrayList<>();
        acceptLore.add(Component.text("Click to accept this request", NamedTextColor.GRAY));
        acceptMeta.lore(acceptLore);
        accept.setItemMeta(acceptMeta);
        inv.setItem(11, accept);

        // دکمه Deny
        ItemStack deny = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta denyMeta = deny.getItemMeta();
        denyMeta.displayName(Component.text("✖ DENY", NamedTextColor.RED));
        List<Component> denyLore = new ArrayList<>();
        denyLore.add(Component.text("Click to deny this request", NamedTextColor.GRAY));
        denyMeta.lore(denyLore);
        deny.setItemMeta(denyMeta);
        inv.setItem(15, deny);

        receiver.playSound(receiver.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
        receiver.openInventory(inv);
    }
}
