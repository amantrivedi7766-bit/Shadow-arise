package com.soloarise.plugin.commands;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.ArisePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;

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
        
        ArisePlayer arisePlayer = plugin.getPlayerManager().getPlayer(player);
        
        if (!arisePlayer.hasArisePower() && arisePlayer.hasActiveTask()) {
            // Check if task is complete
            if (plugin.getPlayerManager().completeTask(player)) {
                player.sendMessage("§aYou have claimed the Arise power!");
                return true;
            } else {
                player.sendMessage("§cYou haven't completed your task yet!");
                return true;
            }
        }
        
        if (!arisePlayer.hasArisePower()) {
            player.sendMessage("§cYou need to complete your task first!");
            return true;
        }
        
        // Ray trace to find target entity
        RayTraceResult result = player.rayTraceEntities(10);
        if (result == null || !(result.getHitEntity() instanceof LivingEntity target)) {
            player.sendMessage("§cLook at a mob or player to capture their soul!");
            return true;
        }
        
        // Attempt to capture soul
        plugin.getSoulManager().attemptCapture(player, target);
        return true;
    }
}
