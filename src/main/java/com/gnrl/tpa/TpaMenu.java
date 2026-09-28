package com.gnrl.tpa;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TpaMenu implements CommandExecutor {

    private final GNRLTPA plugin;
    private final TpaManager manager;

    public TpaMenu(GNRLTPA plugin, TpaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("gnrltpa.menu")) {
            player.sendRichMessage(plugin.getConfig().getString("messages.no-permission", ""));
            return true;
        }
        openDialog(player);
        return true;
    }

    public void openDialog(Player player) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : player.getServer().getOnlinePlayers()) {
            if (!p.equals(player)) {
                onlinePlayers.add(p);
            }
        }

        if (onlinePlayers.isEmpty()) {
            player.sendRichMessage("<yellow>No players online to send a request to.</yellow>");
            return;
        }

        // ساخت دکمه برای هر بازیکن آنلاین
        List<ActionButton> buttons = new ArrayList<>();
        for (Player target : onlinePlayers) {
            String label = plugin.getConfig()
                    .getString("dialog.player-button-label", "<yellow>Send TPA to <player>")
                    .replace("<player>", target.getName());

            ActionButton button = ActionButton.builder(Component.text(label))
                    .action(DialogAction.customClick(
                            Key.key("gnrltpa:send_tpa_" + target.getName()),
                            null
                    ))
                    .build();
            buttons.add(button);
        }

        // دکمه بستن
        ActionButton closeButton = ActionButton.builder(
                Component.text(plugin.getConfig().getString("dialog.close-button-label", "<gray>Close"))
        ).action(DialogAction.customClick(Key.key("gnrltpa:close"), null))
                .build();

        // ساخت Dialog
        Dialog dialog = Dialog.create(builder -> builder
                .base(DialogBase.builder(
                        Component.text(plugin.getConfig().getString("dialog.title", "TPA Menu"))
                )
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(
                                        Component.text("Select a player to send a TPA request.")
                                )
                        ))
                        .build())
                .type(DialogType.multiAction(buttons, closeButton, 2))
                .build());

        player.showDialog(dialog);
    }
}
