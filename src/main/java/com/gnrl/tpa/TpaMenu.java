package com.gnrl.tpa;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
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
import java.util.stream.Collectors;

public class TpaMenu implements Listener {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // مرحله ۱: نمایش دیالوگ جستجو به همراه لیست اسم بازیکن‌ها
    public void openMenu(Player player, boolean here) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) onlinePlayers.add(p);
        }

        if (onlinePlayers.isEmpty()) {
            player.sendRichMessage("<yellow>هیچ بازیکن دیگری آنلاین نیست.</yellow>");
            return;
        }

        // ساخت متن لیست بازیکن‌ها برای نمایش در بدنه دیالوگ
        String names = onlinePlayers.stream()
                .map(Player::getName)
                .collect(Collectors.joining("، "));

        Component title = Component.text(here ? "✦ TPAHere - جستجو ✦" : "✦ TPA - جستجو ✦",
                here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        // بدنه دیالوگ شامل راهنما و لیست اسم بازیکن‌ها
        DialogBody listBody = DialogBody.plainMessage(Component.text(
                "بازیکنان آنلاین: " + names + "\n\n" +
                "نام بازیکن را وارد کنید یا خالی بگذارید تا همه را ببینید:",
                NamedTextColor.WHITE));

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(listBody))
                        .inputs(List.of(
                                DialogInput.text("search_query", Component.text("جستجو", NamedTextColor.YELLOW))
                                        .width(200)
                                        .build()
                        ))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.create(
                                Component.text("🔍 جستجو", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true),
                                Component.text("Search", NamedTextColor.GRAY),
                                100,
                                DialogAction.customClick(Key.key("gnrltpa:search_" + (here ? "here" : "tpa")), null)
                        ),
                        ActionButton.create(
                                Component.text("✖ انصراف", NamedTextColor.RED).decoration(TextDecoration.BOLD, true),
                                Component.text("Cancel", NamedTextColor.GRAY),
                                100,
                                null
                        )
                ))
        );

        player.showDialog(dialog);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
    }

    // مرحله ۲: نمایش نتایج فیلتر شده
    private void showFilteredPlayers(Player player, boolean here, String filter) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) onlinePlayers.add(p);
        }

        List<Player> filtered = onlinePlayers;
        if (filter != null && !filter.isEmpty()) {
            String lowerFilter = filter.toLowerCase();
            filtered = onlinePlayers.stream()
                    .filter(p -> p.getName().toLowerCase().contains(lowerFilter))
                    .collect(Collectors.toList());
        }

        if (filtered.isEmpty()) {
            player.sendRichMessage("<yellow>بازیکنی با این نام پیدا نشد.</yellow>");
            return;
        }

        List<ActionButton> buttons = new ArrayList<>();
        int count = 0;
        for (Player target : filtered) {
            if (count >= 10) break;
            count++;

            String actionKey = "gnrltpa:select_" + (here ? "here_" : "tpa_") + target.getUniqueId();
            ActionButton btn = ActionButton.create(
                    Component.text("✦ " + target.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
                    Component.text("World: " + target.getWorld().getName() + " | Health: " + (int) target.getHealth(), NamedTextColor.GRAY),
                    100,
                    DialogAction.customClick(Key.key(actionKey), null)
            );
            buttons.add(btn);
        }

        Component title = Component.text(here ? "✦ TPAHere - نتایج ✦" : "✦ TPA - نتایج ✦",
                here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(Component.text("بازیکن مورد نظر را انتخاب کنید:", NamedTextColor.WHITE))))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, null, 1))
        );

        player.showDialog(dialog);
    }

    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        Player player = conn.getPlayer();
        String key = event.getIdentifier().asString();

        // پردازش جستجو
        if (key.startsWith("gnrltpa:search_")) {
            boolean isHere = key.endsWith("_here");
            player.closeDialog();

            DialogResponseView view = event.getDialogResponseView();
            String query = "";
            if (view != null) {
                String text = view.getText("search_query");
                if (text != null) query = text;
            }

            showFilteredPlayers(player, isHere, query);
            return;
        }

        // پردازش انتخاب بازیکن
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
