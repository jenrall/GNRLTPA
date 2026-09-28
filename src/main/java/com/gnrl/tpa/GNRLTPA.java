package com.gnrl.tpa;

import org.bukkit.plugin.java.JavaPlugin;

public class GNRLTPA extends JavaPlugin {

    private TpaManager tpaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        tpaManager = new TpaManager(this);

        getCommand("tpa").setExecutor(new TpaCommand(this, tpaManager));
        getCommand("tpaccept").setExecutor(new TpaAcceptCommand(this, tpaManager));
        getCommand("tpdeny").setExecutor(new TpaDenyCommand(this, tpaManager));
        getCommand("tpmenu").setExecutor(new TpaMenu(this, tpaManager));

        getServer().getPluginManager().registerEvents(
                new TpaDialogListener(this, tpaManager), this);

        getLogger().info("GNRLTPA enabled!");
    }

    @Override
    public void onDisable() {
        if (tpaManager != null) {
            tpaManager.clearAll();
        }
        getLogger().info("GNRLTPA disabled.");
    }

    public TpaManager getTpaManager() {
        return tpaManager;
    }
}
