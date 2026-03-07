package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SummonSessionManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, SummonSession> activeSessions = new HashMap<>();
    
    public SummonSessionManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
        
        // Clean up expired sessions every 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                activeSessions.entrySet().removeIf(entry -> 
                    currentTime - entry.getValue().getStartTime() > 30000); // 30 seconds timeout
            }
        }.runTaskTimer(plugin, 600L, 600L);
    }
    
    public void startSession(Player player, List<CapturedSoul> souls) {
        activeSessions.put(player.getUniqueId(), new SummonSession(souls, System.currentTimeMillis()));
        
        // Auto-end session after 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                endSession(player);
                if (player.isOnline()) {
                    player.sendMessage("§cSummon selection timed out!");
                }
            }
        }.runTaskLater(plugin, 600L); // 30 seconds = 600 ticks
    }
    
    public List<CapturedSoul> getSession(Player player) {
        SummonSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return null;
        
        // Check if session is expired (30 seconds)
        if (System.currentTimeMillis() - session.getStartTime() > 30000) {
            activeSessions.remove(player.getUniqueId());
            return null;
        }
        
        return session.getSouls();
    }
    
    public void endSession(Player player) {
        activeSessions.remove(player.getUniqueId());
    }
    
    // Inner class for session data
    private static class SummonSession {
        private final List<CapturedSoul> souls;
        private final long startTime;
        
        public SummonSession(List<CapturedSoul> souls, long startTime) {
            this.souls = souls;
            this.startTime = startTime;
        }
        
        public List<CapturedSoul> getSouls() {
            return souls;
        }
        
        public long getStartTime() {
            return startTime;
        }
    }
}
