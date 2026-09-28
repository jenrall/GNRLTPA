package com.gnrl.tpa;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class TpaHelper {

    public static void sendRequestToTarget(GNRLTPA plugin, Player player, Player target, boolean here) {
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);

        target.sendMessage(Component.text(""));
        target.sendMessage(Component.text("  ╭──────────────────────────╮", NamedTextColor.GOLD));
        target.sendMessage(Component.text("  │  ", NamedTextColor.GOLD)
                .append(Component.text(here ? "✦ TPAHere Request ✦" : "✦ New TPA Request ✦", NamedTextColor.AQUA))
                .append(Component.text("  │", NamedTextColor.GOLD)));
        target.sendMessage(Component.text("  │  ", NamedTextColor.GOLD)
                .append(Component.text("from: ", NamedTextColor.GRAY))
                .append(Component.text(player.getName(), NamedTextColor.YELLOW))
                .append(Component.text("            │", NamedTextColor.GOLD)));
        target.sendMessage(Component.text("  ╰──────────────────────────╯", NamedTextColor.GOLD));

        Component accept = Component.text("  ►  ", NamedTextColor.DARK_GRAY)
                .append(Component.text("ACCEPT", NamedTextColor.GREEN))
                .append(Component.text("  ◄  ", NamedTextColor.DARK_GRAY))
                .clickEvent(ClickEvent.runCommand("/tpaccept"))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to accept", NamedTextColor.GREEN)));

        Component deny = Component.text("  ►  ", NamedTextColor.DARK_GRAY)
                .append(Component.text("DENY", NamedTextColor.RED))
                .append(Component.text("  ◄  ", NamedTextColor.DARK_GRAY))
                .clickEvent(ClickEvent.runCommand("/tpdeny"))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to deny", NamedTextColor.RED)));

        target.sendMessage(Component.text(""));
        target.sendMessage(accept.append(Component.text("     ")).append(deny));
        target.sendMessage(Component.text(""));
    }
}
