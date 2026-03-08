package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AriseCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public AriseCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        // Attempt to capture soul from last kill
        boolean success = plugin.getSoulManager().attemptCapture(player);
        
        if (success) {
            // Success message already sent in SoulManager
        }
        
        return true;
    }
}
