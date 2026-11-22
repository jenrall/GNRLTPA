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

    public TpaMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player, boolean here) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) onlinePlayers.add(p);
        }

        if (onlinePlayers.isEmpty()) {
            player.sendRichMessage("<yellow>هیچ بازیکن دیگری آنلاین نیست.</yellow>");
            return;
        }

        // ساخت لیست دکمه‌ها با فاصله‌گذاری بین بازیکنان
        List<ActionButton> buttons = new ArrayList<>();
        int count = 0;

        for (Player target : onlinePlayers) {
            if (count >= 10) break; // محدودیت تعداد بازیکنان برای جلوگیری از شلوغی
            count++;

            // دکمه اصلی بازیکن
            String actionKey = "gnrltpa:select_" + (here ? "here_" : "tpa_") + target.getUniqueId();
            ActionButton playerBtn = ActionButton.create(
                    Component.text("✦ " + target.getName(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
                    Component.text("World: " + target.getWorld().getName() + " | Health: " + (int) target.getHealth(), NamedTextColor.GRAY),
                    100,
                    DialogAction.customClick(Key.key(actionKey), null)
            );
            buttons.add(playerBtn);

            // اضافه کردن یک دکمه خالی (Spacer) بین دکمه‌ها برای ایجاد فاصله
            // فقط اگر آخرین دکمه نباشد
            if (count < onlinePlayers.size() && count < 10) {
                ActionButton spacer = ActionButton.create(
                        Component.text(" "), // متن خالی
                        Component.empty(),
                        1,
                        null // بدون اکشن
                );
                buttons.add(spacer);
            }
        }

        Component title = Component.text(here ? "✦ TPAHere - انتخاب بازیکن ✦" : "✦ TPA - انتخاب بازیکن ✦",
                here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(Component.text("یک بازیکن را برای ارسال درخواست انتخاب کنید:", NamedTextColor.WHITE))))
                        .canCloseWithEscape(true)
                        .build())
                // استفاده از ۱ ستون برای نمایش هر دکمه در یک خط جداگانه (فاصله بیشتر)
                .type(DialogType.multiAction(buttons, null, 1))
        );

        player.showDialog(dialog);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
    }

    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        Player player = conn.getPlayer();
        String key = event.getIdentifier().asString();

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
