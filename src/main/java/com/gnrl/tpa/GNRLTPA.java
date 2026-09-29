package com.gnrl.tpa;

import org.bukkit.plugin.java.JavaPlugin;

public class GNRLTPA extends JavaPlugin {

    private TpaManager tpaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        tpaManager = new TpaManager(this);

        getCommand("tpa").setExecutor(new TpaCommand(this, tpaManager));
        getCommand("tpahere").setExecutor(new TpaHereCommand(this, tpaManager));
        getCommand("tpaccept").setExecutor(new TpaAcceptCommand(this, tpaManager));
        getCommand("tpdeny").setExecutor(new TpaDenyCommand(this, tpaManager));
        getCommand("tpacancel").setExecutor(new TpaCancelCommand(this, tpaManager));
        getCommand("tpauto").setExecutor(new TpaAutoCommand(this, tpaManager));

        getServer().getPluginManager().registerEvents(
                new TpaMenuListener(this, tpaManager), this);

        printLogo();
    }

    private void printLogo() {
        getLogger().info("");
        getLogger().info("  \u001B[33m╔══════════════════════════════════════╗");
        getLogger().info("  \u001B[33m║  \u001B[36m✦ GNRLTPA v1.0.0 ✦\u001B[33m                  ║");
        getLogger().info("  \u001B[33m║  \u001B[37mAuthor: GNRLFlawless\u001B[33m                 ║");
        getLogger().info("  \u001B[33m║  \u001B[37mgithub.com/jenrall/GNRLTPA\u001B[33m          ║");
        getLogger().info("  \u001B[33m╚══════════════════════════════════════╝");
        getLogger().info("  \u001B[32m✔ Plugin loaded successfully!");
        getLogger().info("");
    }

    @Override
    public void onDisable() {
        if (tpaManager != null) {
            tpaManager.clearAll();
        }
    }

    public TpaManager getTpaManager() {
        return tpaManager;
    }
}
