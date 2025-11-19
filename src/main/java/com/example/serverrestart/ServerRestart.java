package com.example.serverrestart;

import org.bukkit.plugin.java.JavaPlugin;

public class ServerRestart extends JavaPlugin {

    private RestartManager restartManager;

    @Override
    public void onEnable() {
        // Create plugin folder and save default config
        saveDefaultConfig();
        
        restartManager = new RestartManager(this);
        
        // Register command
        getCommand("resta").setExecutor(new RestartCommand(this, restartManager));
        
        getLogger().info("========================================");
        getLogger().info("ServerRestart made by TimLuist");
        getLogger().info("Loaded successfully!");
        getLogger().info("Configuration loaded from plugins/ServerRestart/config.yml");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        // Cancel any pending restart
        if (restartManager != null) {
            restartManager.cancelRestart();
        }
        
        getLogger().info("ServerRestart plugin has been disabled!");
    }
}
