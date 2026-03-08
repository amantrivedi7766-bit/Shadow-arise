package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.ArisePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, ArisePlayer> players = new ConcurrentHashMap<>();
    
    public PlayerManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    public ArisePlayer getPlayer(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), k -> new ArisePlayer(player));
    }
    
    public Map<UUID, ArisePlayer> getAllPlayers() {
        return players;
    }
    
    // All players have Arise power by default now
    public boolean hasArisePower(Player player) {
        return true; // Always true in new system
    }
    
    public void saveAllPlayers() {
        // Save to database if implemented
    }
    
    public void loadAllPlayers() {
        // Load from database if implemented
    }
}
