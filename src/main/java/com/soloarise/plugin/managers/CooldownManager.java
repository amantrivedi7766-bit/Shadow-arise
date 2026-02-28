package com.soloarise.plugin.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {
    
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final long cooldownTime;
    
    public CooldownManager(long cooldownTime) {
        this.cooldownTime = cooldownTime * 1000; // Convert to milliseconds
    }
    
    public boolean isOnCooldown(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long timeLeft = cooldowns.get(playerId) - System.currentTimeMillis();
        return timeLeft > 0;
    }
    
    public long getRemainingCooldown(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long timeLeft = cooldowns.get(playerId) - System.currentTimeMillis();
        return Math.max(0, timeLeft / 1000);
    }
    
    public void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() + cooldownTime);
    }
    
    public void removeCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }
    
    public void clearAll() {
        cooldowns.clear();
    }
}
