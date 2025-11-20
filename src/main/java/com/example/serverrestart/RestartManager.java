package com.example.serverrestart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RestartManager {

    private final ServerRestart plugin;
    private BukkitTask restartTask;
    private BukkitTask autoRestartTask;
    private boolean restartScheduled;
    private String restartReason;

    public RestartManager(ServerRestart plugin) {
        this.plugin = plugin;
        this.restartScheduled = false;
        this.restartReason = null;
    }

    public void startRestart() {
        startRestart(null);
    }

    public void startRestart(String reason) {
        startRestart(null, reason);
    }

    public void startRestart(Integer customSeconds, String reason) {
        if (restartScheduled) {
            return;
        }

        restartScheduled = true;
        this.restartReason = reason;
        
        // Get countdown time from config or use custom time
        int countdownSeconds = (customSeconds != null) ? customSeconds : plugin.getConfig().getInt("countdown-seconds", 10);
        
        // Broadcast initial message
        String announcement = plugin.getConfig().getString("messages.restart-announcement", "&c&lServer is restarting in {time} seconds!")
                .replace("{time}", String.valueOf(countdownSeconds));
        Bukkit.broadcastMessage(colorize(announcement));
        
        // Broadcast reason if provided
        if (reason != null && !reason.isEmpty()) {
            String reasonMsg = plugin.getConfig().getString("messages.restart-reason", "&eReason: &f{reason}")
                    .replace("{reason}", reason);
            Bukkit.broadcastMessage(colorize(reasonMsg));
        }

        // Start countdown task
        restartTask = new BukkitRunnable() {
            int countdown = countdownSeconds;

            @Override
            public void run() {
                if (countdown > 0) {
                    // Broadcast countdown
                    String countdownMsg = plugin.getConfig().getString("messages.countdown", "&eServer restarting in &c{time} &esecond{s}...")
                            .replace("{time}", String.valueOf(countdown))
                            .replace("{s}", countdown == 1 ? "" : "s");
                    Bukkit.broadcastMessage(colorize(countdownMsg));
                    
                    // Show title and actionbar to all players
                    if (plugin.getConfig().getBoolean("notifications.title-enabled", true)) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            String title = colorize(plugin.getConfig().getString("notifications.title", "&c&lSERVER RESTART")
                                    .replace("{time}", String.valueOf(countdown)));
                            String subtitle = colorize(plugin.getConfig().getString("notifications.subtitle", "&e{time} second{s}")
                                    .replace("{time}", String.valueOf(countdown))
                                    .replace("{s}", countdown == 1 ? "" : "s"));
                            player.sendTitle(title, subtitle, 5, 30, 10);
                        }
                    }
                    
                    if (plugin.getConfig().getBoolean("notifications.actionbar-enabled", true)) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            String actionbar = colorize(plugin.getConfig().getString("notifications.actionbar", "&c⚠ Server restarting in {time}s")
                                    .replace("{time}", String.valueOf(countdown)));
                            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionbar));
                        }
                    }
                    
                    // Play sounds at specific intervals
                    if (plugin.getConfig().getBoolean("sounds.enabled", true)) {
                        if (countdown <= 5 || countdown == 10 || countdown == 30 || countdown == 60) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                String soundName = plugin.getConfig().getString("sounds.warning-sound", "BLOCK_NOTE_BLOCK_PLING");
                                try {
                                    Sound sound = Sound.valueOf(soundName);
                                    player.playSound(player.getLocation(), sound, 1.0f, countdown <= 3 ? 2.0f : 1.0f);
                                } catch (IllegalArgumentException e) {
                                    plugin.getLogger().warning("Invalid sound: " + soundName);
                                }
                            }
                        }
                    }
                    
                    countdown--;
                } else {
                    // Restart the server
                    String restartMsg = plugin.getConfig().getString("messages.restart-now");
                    Bukkit.broadcastMessage(colorize(restartMsg));
                    
                    // Kick all players with custom message
                    String kickMessage = plugin.getConfig().getString("messages.kick-message", 
                            "&eServer is restarting\n&7It's back up soon!");
                    
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // Kick all online players
                            Bukkit.getOnlinePlayers().forEach(player -> 
                                player.kickPlayer(colorize(kickMessage))
                            );
                            
                            // Restart the server after kicking players
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Bukkit.getServer().spigot().restart();
                                }
                            }.runTaskLater(plugin, 10L); // Wait 0.5 seconds after kicking
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

    public void scheduleAutoRestart() {
        if (!plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            return;
        }

        int intervalHours = plugin.getConfig().getInt("auto-restart.interval-hours", 6);
        long intervalTicks = intervalHours * 60 * 60 * 20L; // Convert hours to ticks

        autoRestartTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!restartScheduled) {
                    String autoReason = plugin.getConfig().getString("auto-restart.reason", "Scheduled maintenance");
                    startRestart(autoReason);
                }
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);

        plugin.getLogger().info("Auto-restart scheduled every " + intervalHours + " hours");
    }

    public void cancelAutoRestart() {
        if (autoRestartTask != null) {
            autoRestartTask.cancel();
            autoRestartTask = null;
        }
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
