@EventHandler
public void onCustomClick(PlayerCustomClickEvent event) {
    if (!(event.getCommonConnection() instanceof io.papermc.paper.connection.PlayerGameConnection conn)) return;
    Player player = conn.getPlayer();

    Key key = event.getIdentifier();
    String keyString = key.asString();

    if (keyString.equals("gnrltpa:close")) {
        player.closeDialog();
        return;
    }

    if (keyString.startsWith("gnrltpa:send_tpa_")) {
        String targetName = keyString.substring("gnrltpa:send_tpa_".length());

        Player target = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(targetName)) {
                target = p;
                break;
            }
        }

        if (target == null || !target.isOnline()) {
            player.sendRichMessage(plugin.getConfig().getString("messages.player-not-found", ""));
            return;
        }

        player.closeDialog();
        player.performCommand("tpa " + target.getName());
    }
}
