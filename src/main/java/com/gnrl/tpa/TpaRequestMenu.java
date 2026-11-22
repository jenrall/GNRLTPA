package com.gnrl.tpa;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class TpaRequestMenu implements Listener {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaRequestMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        // ثبت لیسنر برای دیالوگ
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openRequestMenu(Player receiver, Player sender, boolean here) {
        Component title = Component.text(here ? "✦ TPAHere Request ✦" : "✦ TPA Request ✦", 
                here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        // ساخت بدنه‌ی دیالوگ (اطلاعات فرستنده)
        List<DialogBase.DialogBody> body = new ArrayList<>();
        body.add(DialogBase.DialogBody.plainMessage(Component.text(
                "Player: " + sender.getName() + "\n" +
                "World: " + sender.getWorld().getName() + "\n" +
                "Health: " + (int) sender.getHealth() + "/20",
                NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));

        // دکمه Accept
        ActionButton acceptBtn = ActionButton.create(
                Component.text("✔ ACCEPT", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true),
                Component.text("Click to accept this request", NamedTextColor.GRAY),
                100,
                DialogAction.customClick(Key.key("gnrltpa:accept_" + sender.getUniqueId()), null)
        );

        // دکمه Deny
        ActionButton denyBtn = ActionButton.create(
                Component.text("✖ DENY", NamedTextColor.RED).decoration(TextDecoration.BOLD, true),
                Component.text("Click to deny this request", NamedTextColor.GRAY),
                100,
                DialogAction.customClick(Key.key("gnrltpa:deny_" + sender.getUniqueId()), null)
        );

        // ساخت دیالوگ از نوع multiAction با ۲ ستون (دقیقاً شبیه عکس)
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(body)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(List.of(acceptBtn, denyBtn), null, 2))
        );

        // نمایش به بازیکن گیرنده
        receiver.showDialog(dialog);
        receiver.playSound(receiver.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
    }

    // هندل کردن کلیک روی دکمه‌های دیالوگ
    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent event) {
        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        Player player = conn.getPlayer();
        String key = event.getIdentifier().asString();

        if (key.startsWith("gnrltpa:accept_")) {
            player.closeDialog();
            manager.confirmRequest(player);
        } else if (key.startsWith("gnrltpa:deny_")) {
            player.closeDialog();
            manager.denyRequest(player);
        }
    }
}
