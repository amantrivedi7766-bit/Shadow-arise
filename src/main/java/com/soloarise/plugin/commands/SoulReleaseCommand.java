package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoulReleaseCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public SoulReleaseCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cUsage: /soulrelease <soulname/groupname>");
            return true;
        }
        
        String identifier = args[0];
        
        if (plugin.getSoulManager().releaseSoul(player, identifier)) {
            player.sendMessage("§a✓ Soul released successfully!");
        } else {
            player.sendMessage("§cSoul not found!");
        }
        
        return true;
    }
}
