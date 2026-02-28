package com.soloarise.plugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void sendMessage(Player player, String message) {
        player.sendMessage(colorize(message));
    }
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }
    
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(colorize(title), colorize(subtitle), fadeIn, stay, fadeOut);
    }
    
    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(colorize(message));
    }
    
    public static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
}
