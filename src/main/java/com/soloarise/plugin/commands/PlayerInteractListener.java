package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInteractListener implements Listener {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, ClickData> clickTracker = new HashMap<>();
    
    public PlayerInteractListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
        
        // Clean up old click data every minute
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                clickTracker.entrySet().removeIf(entry -> 
                    currentTime - entry.getValue().getLastClickTime() > 3000); // Remove after 3 seconds
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // Run every minute
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is shifting (sneaking)
        if (!player.isSneaking()) {
            return;
        }
        
        // Check if it's a left click
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        
        // Check if player has arise power
        if (!plugin.getPlayerManager().getPlayer(player).hasArisePower()) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        ClickData clickData = clickTracker.getOrDefault(playerId, new ClickData());
        
        // Record this click
        clickData.addClick();
        clickTracker.put(playerId, clickData);
        
        // Check if player has clicked 3 times
        if (clickData.getClickCount() >= 3) {
            // Check if clicks were within time window (3 seconds)
            if (clickData.isValid()) {
                // Trigger soul summon menu
                openSummonMenu(player);
            }
            
            // Reset clicks
            clickTracker.remove(playerId);
        }
    }
    
    private void openSummonMenu(Player player) {
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        
        if (soulData == null || soulData.getSouls().isEmpty()) {
            player.sendMessage("§cYou have no souls to summon!");
            return;
        }
        
        // Create a simple menu in chat (you can replace with GUI later)
        player.sendMessage("§8§m--------------------------------------------------");
        player.sendMessage("§5§l✦ SELECT SOUL TO SUMMON ✦");
        player.sendMessage("§8§m--------------------------------------------------");
        
        int index = 1;
        for (CapturedSoul soul : soulData.getSouls()) {
            if (soul.isSummoned()) {
                player.sendMessage("§7" + index + ". " + soul.getRank().getDisplayName() + " §f" + soul.getName() + 
                    " §c[ALREADY SUMMONED]");
            } else if (soul.getCurrentHealth() <= 0) {
                player.sendMessage("§7" + index + ". " + soul.getRank().getDisplayName() + " §f" + soul.getName() + 
                    " §c[DEAD - NEEDS HEALING]");
            } else {
                player.sendMessage("§e" + index + ". " + soul.getRank().getDisplayName() + " §f" + soul.getName() + 
                    " §7(Health: " + (int)soul.getCurrentHealth() + "/" + (int)soul.getMaxHealth() + ")");
            }
            index++;
        }
        
        player.sendMessage("§8§m--------------------------------------------------");
        player.sendMessage("§7Type §e/summon <number> §7to summon a soul");
        
        // Store the soul list for this player temporarily
        plugin.getSummonSessionManager().startSession(player, soulData.getSouls());
    }
    
    // Inner class to track clicks
    private static class ClickData {
        private int clickCount = 0;
        private long firstClickTime = 0;
        private long lastClickTime = 0;
        
        public void addClick() {
            long currentTime = System.currentTimeMillis();
            
            if (clickCount == 0) {
                firstClickTime = currentTime;
            }
            
            clickCount++;
            lastClickTime = currentTime;
        }
        
        public int getClickCount() {
            return clickCount;
        }
        
        public long getLastClickTime() {
            return lastClickTime;
        }
        
        public boolean isValid() {
            // Clicks must be within 3 seconds
            return (lastClickTime - firstClickTime) <= 3000;
        }
    }
}
