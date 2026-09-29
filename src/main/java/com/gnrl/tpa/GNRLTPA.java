package com.gnrl.tpa;

import org.bukkit.plugin.java.JavaPlugin;

public class GNRLTPA extends JavaPlugin {

    private TpaManager tpaManager;
    private PlayerSettings playerSettings;
    private TpaMenu tpaMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        tpaManager = new TpaManager(this);
        playerSettings = new PlayerSettings();
        tpaMenu = new TpaMenu(this, tpaManager);

        getCommand("tpa").setExecutor(new TpaCommand(this, tpaManager));
        getCommand("tpahere").setExecutor(new TpaHereCommand(this, tpaManager));
        getCommand("tpaccept").setExecutor(new TpaAcceptCommand(this, tpaManager));
        getCommand("tpdeny").setExecutor(new TpaDenyCommand(this, tpaManager));
        getCommand("tpacancel").setExecutor(new TpaCancelCommand(this, tpaManager));
        getCommand("tpauto").setExecutor(new TpaAutoCommand(this, tpaManager));
        getCommand("tptoggle").setExecutor(new TpaToggleCommand(this));
       
