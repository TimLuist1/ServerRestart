package com.example.serverrestart;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RestartCommand implements CommandExecutor {

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

        // Handle /resta [reason]
        if (restartManager.isRestartScheduled()) {
            sender.sendMessage(colorize(plugin.getConfig().getString("messages.already-in-progress")));
            return true;
        }

        // Start the restart countdown with optional reason
        String reason = null;
        if (args.length > 0) {
            reason = String.join(" ", args);
        }
        restartManager.startRestart(reason);
        return true;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
