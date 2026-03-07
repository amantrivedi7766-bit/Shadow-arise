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
        
        // 1.21.4 compatible ray tracing
        LivingEntity target = getTargetEntity(player);
        
        if (target == null) {
            player.sendMessage("§cLook at a mob or player to capture their soul!");
            return true;
        }
        
        plugin.getSoulManager().attemptCapture(player, target);
        return true;
    }
    
    private LivingEntity getTargetEntity(Player player) {
        try {
            // 1.21.4 method
            RayTraceResult result = player.rayTraceEntities(10);
            if (result != null && result.getHitEntity() instanceof LivingEntity) {
                return (LivingEntity) result.getHitEntity();
            }
        } catch (NoSuchMethodError e) {
            // Fallback for older versions
            return player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                10,
                entity -> entity instanceof LivingEntity && entity != player
            ).getHitEntity() instanceof LivingEntity hit ? hit : null;
        }
        
        return null;
    }
}
