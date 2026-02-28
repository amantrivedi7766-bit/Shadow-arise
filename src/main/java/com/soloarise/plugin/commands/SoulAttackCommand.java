package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoulAttackCommand implements CommandExecutor {
    
    private final SoloArisePlugin plugin;
    
    public SoulAttackCommand(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cUsage: /soulattack <playername>");
            return true;
        }
        
        String targetName = args[0];
        plugin.getSoulManager().attackPlayer(player, targetName);
        return true;
    }
}
