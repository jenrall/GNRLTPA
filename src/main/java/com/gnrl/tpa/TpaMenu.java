package com.gnrl.tpa;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TpaMenu implements Listener {

    private final GNRLTPA plugin;
    private final TpaManager manager;
    private static final int PLAYERS_PER_PAGE = 10; // تعداد بازیکن در هر صفحه

    public TpaMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // متد اصلی که صفحه مشخصی را باز می‌کند
    public void openMenu(Player player, boolean here, int page) {
        List<Player> allPlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) allPlayers.add(p);
        }

        if (allPlayers.isEmpty()) {
            player.sendRichMessage("<yellow>هیچ بازیکن دیگری آنلاین نیست.</yellow>");
            return;
        }

        int totalPages = (int) Math.ceil((double) allPlayers.size() / PLAYERS_PER_PAGE);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * PLAYERS_PER_PAGE;
        int end = Math.min(start + PLAYERS_PER_PAGE, allPlayers.size());
        List<Player> pagePlayers = allPlayers.subList(start, end);

        List<ActionButton> buttons = new ArrayList<>();

        // دکمه‌های بازیکنان در این صفحه
        for (Player target : pagePlayers) {
            String actionKey = "gnrltpa:select_" + (here ? "here_" : "tpa_") + target.getUniqueId();
            buttons.add(ActionButton.create(
                    Component.text("✦ " + target.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
                    Component.text("World: " + target.getWorld().getName() + " | Health: " + (int) target.getHealth(), NamedTextColor.GRAY),
                    100,
                    DialogAction.customClick(Key.key(actionKey), null)
            ));
        }

        // دکمه Previous (اگر صفحه قبل وجود دارد)
        if (page > 1) {
            String prevKey = "gnrltpa:page_" + (here ? "here_" : "tpa_") + (page - 1);
            buttons.add(ActionButton.create(
                    Component.text("◀ Previous", NamedTextColor.GREEN),
                    Component.text("Go to page " + (page - 1), NamedTextColor.GRAY),
                    100,
                    DialogAction.customClick(Key.key(prevKey), null)
            ));
        }

        // دکمه Next (اگر صفحه بعد وجود دارد)
        if (page < totalPages) {
            String nextKey = "gnrltpa:page_" + (here ? "here_" : "tpa_") + (page + 1);
            buttons.add(ActionButton.create(
                    Component.text("Next ▶", NamedTextColor.GREEN),
                    Component.text("Go to page " + (page + 1), NamedTextColor.GRAY),
                    100,
                    DialogAction.customClick(Key.key(nextKey), null)
            ));
        }

        Component title = Component.text(here ? "✦ TPAHere (Page " + page + "/" + totalPages + ") ✦" : "✦ TPA (Page " + page + "/" + totalPages + ") ✦",
                here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(Component.text("Select a player:", NamedTextColor.WHITE))))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, null, 1)) // 1 ستون برای خوانایی بهتر
        );

        player.showDialog(dialog);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
    }

    // اورلود برای وقتی که فقط می‌خواهیم صفحه اول باز شود
    public void openMenu(Player player, boolean here) {
        openMenu(player, here, 1);
    }

    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        Player player = conn.getPlayer();
        String key = event.getIdentifier().asString();

        // پردازش دکمه‌های صفحه‌بندی
        if (key.startsWith("gnrltpa:page_")) {
            player.closeDialog();
            String rest = key.substring("gnrltpa:page_".length());
            boolean isHere = rest.startsWith("here_");
            int page = Integer.parseInt(rest.substring(isHere ? "here_".length() : "tpa_".length()));
            openMenu(player, isHere, page);
            return;
        }

        // پردازش انتخاب بازیکن (همان کد قبلی)
        if (key.startsWith("gnrltpa:select_")) {
            player.closeDialog();
            String rest = key.substring("gnrltpa:select_".length());
            boolean isHere;
            String uuidStr;
            if (rest.startsWith("here_")) {
                isHere = true;
                uuidStr = rest.substring("here_".length());
            } else if (rest.startsWith("tpa_")) {
                isHere = false;
                uuidStr = rest.substring("tpa_".length());
            } else {
                return;
            }
            try {
                UUID targetId = UUID.fromString(uuidStr);
                Player target = Bukkit.getPlayer(targetId);
                if (target != null && target.isOnline()) {
                    manager.sendRequest(player, target, isHere);
                } else {
                    player.sendRichMessage("<red>بازیکن مورد نظر دیگر آنلاین نیست.</red>");
                }
            } catch (IllegalArgumentException e) {
                // UUID نامعتبر
            }
        }
    }
}
