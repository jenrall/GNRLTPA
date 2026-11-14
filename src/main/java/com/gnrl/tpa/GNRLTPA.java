package com.gnrl.tpa;

import org.bukkit.plugin.java.JavaPlugin;

public class GNRLTPA extends JavaPlugin {

    private TpaManager tpaManager;
    private PlayerSettings playerSettings;
    private TpaMenu tpaMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // راه‌اندازی منیجرها
        this.tpaManager = new TpaManager(this);
        this.playerSettings = new PlayerSettings(); // اگر ورودی داره، اینجا اضافه کن
        this.tpaMenu = new TpaMenu(this, tpaManager);

        // ثبت کامندها
        getCommand("tpa").setExecutor(new TpaCommand(this, tpaManager));
        getCommand("tpahere").setExecutor(new TpaHereCommand(this, tpaManager));
        getCommand("tpaccept").setExecutor(new TpaAcceptCommand(this, tpaManager));
        getCommand("tpdeny").setExecutor(new TpaDenyCommand(this, tpaManager));
        getCommand("tpacancel").setExecutor(new TpaCancelCommand(this, tpaManager));
        getCommand("tpauto").setExecutor(new TpaAutoCommand(this, tpaManager));
        getCommand("tptoggle").setExecutor(new TpaToggleCommand(this));
        getCommand("tpblock").setExecutor(new TpaBlockCommand(this));
        getCommand("tpreload").setExecutor(new TpaReloadCommand(this));

        // ثبت تب‌کامپلتر (فعلاً غیرفعال تا از ارور جلوگیری شود)
        // TpaTabCompleter tabCompleter = new TpaTabCompleter();
        // getCommand("tpa").setTabCompleter(tabCompleter);
        // getCommand("tpahere").setTabCompleter(tabCompleter);
        // getCommand("tpauto").setTabCompleter(tabCompleter);

        // ثبت ایونت‌ها
        getServer().getPluginManager().registerEvents(new TpaMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new TpaMoveListener(tpaManager), this);
        getServer().getPluginManager().registerEvents(new TpaDialogListener(this), this);

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

    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    public TpaMenu getTpaMenu() {
        return tpaMenu;
    }
}
