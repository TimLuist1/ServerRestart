package com.example.serverrestart;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class RestartCommand implements CommandExecutor, TabCompleter {

    private final ServerRestart plugin;
    private final RestartManager restartManager;

    public RestartCommand(ServerRestart plugin, RestartManager restartManager) {
        this.plugin = plugin;
        this.restartManager = restartManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle /resta help
        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            int countdownTime = plugin.getConfig().getInt("countdown-seconds", 10);
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.help-header")));
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.help-restart").replace("{time}", String.valueOf(countdownTime))));
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.help-stop")));
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.help-help")));
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.help-footer")));
            return true;
        }

        // Check if player has permission
        if (!sender.hasPermission("serverrestart.use")) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        // Handle /resta stop
        if (args.length > 0 && args[0].equalsIgnoreCase("stop")) {
            if (!sender.hasPermission("serverrestart.stop")) {
                sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-permission")));
                return true;
            }
            if (restartManager.isRestartScheduled()) {
                restartManager.cancelRestart();
            } else {
                sender.sendMessage(colorize(plugin.getConfig().getString("messages.no-restart-in-progress")));
            }
            return true;
        }

        // Handle /resta [time] [reason] or /resta [reason]
        if (restartManager.isRestartScheduled()) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.already-in-progress")));
            return true;
        }

        // Check if first argument is a time format (e.g., 30sec, 15min, 2hour)
        Integer customSeconds = null;
        int reasonStartIndex = 0;
        
        if (args.length > 0) {
            customSeconds = parseTime(args[0]);
            if (customSeconds != null) {
                reasonStartIndex = 1; // Reason starts from second argument
            }
        }
        
        // Get optional reason (everything after time, or everything if no time specified)
        String reason = null;
        if (args.length > reasonStartIndex) {
            String[] reasonArgs = new String[args.length - reasonStartIndex];
            System.arraycopy(args, reasonStartIndex, reasonArgs, 0, reasonArgs.length);
            reason = String.join(" ", reasonArgs);
        }
        
        // Start restart with custom time or default
        if (customSeconds != null) {
            restartManager.startRestart(customSeconds, reason);
        } else {
            // Treat all args as reason if no time format detected
            if (args.length > 0) {
                reason = String.join(" ", args);
            }
            restartManager.startRestart(reason);
        }
        return true;
    }
    
    /**
     * Parse time string like "30sec", "15min", "2hour"
     * @param timeStr Time string to parse
     * @return Seconds, or null if invalid format
     */
    private Integer parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        
        timeStr = timeStr.toLowerCase();
        
        try {
            // Parse format: [number][unit]
            if (timeStr.endsWith("sec") || timeStr.endsWith("s")) {
                String numStr = timeStr.replaceAll("[^0-9]", "");
                int value = Integer.parseInt(numStr);
                return value; // Already in seconds
            } else if (timeStr.endsWith("min") || timeStr.endsWith("m")) {
                String numStr = timeStr.replaceAll("[^0-9]", "");
                int value = Integer.parseInt(numStr);
                return value * 60; // Convert to seconds
            } else if (timeStr.endsWith("hour") || timeStr.endsWith("h")) {
                String numStr = timeStr.replaceAll("[^0-9]", "");
                int value = Integer.parseInt(numStr);
                return value * 3600; // Convert to seconds
            }
        } catch (NumberFormatException e) {
            return null;
        }
        
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument - suggest time formats and subcommands
            if (sender.hasPermission("serverrestart.use")) {
                // Time suggestions in seconds
                completions.add("10sec");
                completions.add("30sec");
                completions.add("45sec");
                completions.add("60sec");
                
                // Time suggestions in minutes
                completions.add("1min");
                completions.add("5min");
                completions.add("15min");
                completions.add("30min");
                completions.add("45min");
                completions.add("60min");
                
                // Time suggestions in hours
                completions.add("1hour");
                completions.add("2hour");
                completions.add("6hour");
                completions.add("12hour");
                completions.add("24hour");
                
                // Subcommands
                completions.add("stop");
                completions.add("help");
            }
            
            // Filter by what the player has typed so far
            String input = args[0].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        }
        
        return completions;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
