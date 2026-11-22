package com.gnrl.tpa;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody; // ← Import اضافه شد
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
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openRequestMenu(Player receiver, Player sender, boolean here) {
        Component title = Component.text(here ? "✦ TPAHere Request ✦" : "✦ TPA Request ✦",
                here ? NamedTextColor.AQUA : NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        // ✅ اصلاح: استفاده از DialogBody به جای DialogBase.DialogBody
        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(Component.text(
                "Player: " + sender.getName() + "\n" +
                "World: " + sender.getWorld().getName() + "\n" +
                "Health: " + (int) sender.getHealth() + "/20",
                NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)));

        ActionButton acceptBtn = ActionButton.create(
                Component.text("✔ ACCEPT", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true),
                Component.text("Click to accept this request", NamedTextColor.GRAY),
                100,
                DialogAction.customClick(Key.key("gnrltpa:accept_" + sender.getUniqueId()), null)
        );

        ActionButton denyBtn = ActionButton.create(
                Component.text("✖ DENY", NamedTextColor.RED).decoration(TextDecoration.BOLD, true),
                Component.text("Click to deny this request", NamedTextColor.GRAY),
                100,
                DialogAction.customClick(Key.key("gnrltpa:deny_" + sender.getUniqueId()), null)
        );

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(body)
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(List.of(acceptBtn, denyBtn), null, 2))
        );

        receiver.showDialog(dialog);
        receiver.playSound(receiver.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
    }

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
