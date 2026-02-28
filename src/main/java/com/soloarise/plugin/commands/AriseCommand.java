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
        
        // Ray trace to find target entity - Fixed for 1.21
        // Using getTargetEntity method instead of rayTraceEntities
        LivingEntity target = getTargetEntity(player);
        
        if (target == null) {
            player.sendMessage("§cLook at a mob or player to capture their soul!");
            return true;
        }
        
        // Attempt to capture soul
        plugin.getSoulManager().attemptCapture(player, target);
        return true;
    }
    
    private LivingEntity getTargetEntity(Player player) {
        // Simple method to get the entity the player is looking at
        // This works across all versions
        double range = 10.0;
        RayTraceResult result = player.getWorld().rayTraceEntities(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            range,
            entity -> entity instanceof LivingEntity && entity != player
        );
        
        if (result != null && result.getHitEntity() instanceof LivingEntity) {
            return (LivingEntity) result.getHitEntity();
        }
        
        return null;
    }
}
