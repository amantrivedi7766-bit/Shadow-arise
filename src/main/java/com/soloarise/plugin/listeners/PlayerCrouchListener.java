package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCrouchListener implements Listener {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, CrouchData> crouchTracker = new HashMap<>();
    
    public PlayerCrouchListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerCrouch(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        
        // Only count when starting to crouch, not when standing up
        if (!event.isSneaking()) return;
        
        UUID playerId = player.getUniqueId();
        CrouchData data = crouchTracker.getOrDefault(playerId, new CrouchData());
        
        // Check if within time window (5 seconds)
        if (System.currentTimeMillis() - data.getLastCrouchTime() > 5000) {
            data.reset();
        }
        
        data.addCrouch();
        data.setLastCrouchTime(System.currentTimeMillis());
        crouchTracker.put(playerId, data);
        
        // Check if reached 5 crouches
        if (data.getCrouchCount() >= 5) {
            // Open summon menu
            plugin.getSoulManager().openSummonMenu(player);
            
            // Reset counter
            crouchTracker.remove(playerId);
            
            // Visual feedback
            player.sendMessage("§d✦ Summoning menu opened!");
        } else {
            // Show progress
            int remaining = 5 - data.getCrouchCount();
            player.sendMessage("§7Crouch " + remaining + " more time" + 
                (remaining > 1 ? "s" : "") + " to summon souls...");
        }
    }
    
    private static class CrouchData {
        private int crouchCount = 0;
        private long lastCrouchTime = 0;
        
        public void addCrouch() { crouchCount++; }
        public int getCrouchCount() { return crouchCount; }
        public long getLastCrouchTime() { return lastCrouchTime; }
        public void setLastCrouchTime(long time) { lastCrouchTime = time; }
        public void reset() { crouchCount = 1; }
    }
}
