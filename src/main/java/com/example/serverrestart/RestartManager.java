package com.example.serverrestart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RestartManager {

    private final ServerRestart plugin;
    private BukkitTask restartTask;
    private boolean restartScheduled;

    public RestartManager(ServerRestart plugin) {
        this.plugin = plugin;
        this.restartScheduled = false;
    }

    public void startRestart() {
        if (restartScheduled) {
            return;
        }

        restartScheduled = true;
        
        // Get countdown time from config
        int countdownSeconds = plugin.getConfig().getInt("countdown-seconds", 10);
        
        // Broadcast initial message
        String announcement = plugin.getConfig().getString("messages.restart-announcement")
                .replace("{time}", String.valueOf(countdownSeconds));
        Bukkit.broadcastMessage(colorize(announcement));

        // Start countdown task
        restartTask = new BukkitRunnable() {
            int countdown = countdownSeconds;

            @Override
            public void run() {
                if (countdown > 0) {
                    // Broadcast countdown
                    String countdownMsg = plugin.getConfig().getString("messages.countdown")
                            .replace("{time}", String.valueOf(countdown))
                            .replace("{s}", countdown == 1 ? "" : "s");
                    Bukkit.broadcastMessage(colorize(countdownMsg));
                    countdown--;
                } else {
                    // Restart the server
                    String restartMsg = plugin.getConfig().getString("messages.restart-now");
                    Bukkit.broadcastMessage(colorize(restartMsg));
                    
                    // Schedule restart for next tick to ensure message is sent
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.getServer().spigot().restart();
                        }
                    }.runTaskLater(plugin, 20L); // Wait 1 second (20 ticks)
                    
                    this.cancel();
                    restartScheduled = false;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }

    public void cancelRestart() {
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }
        restartScheduled = false;
        String cancelMsg = plugin.getConfig().getString("messages.restart-cancelled");
        Bukkit.broadcastMessage(colorize(cancelMsg));
    }

    public boolean isRestartScheduled() {
        return restartScheduled;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
