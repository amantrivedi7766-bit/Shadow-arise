package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public PlayerJoinListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is new (first join)
        if (!player.hasPlayedBefore()) {
            plugin.getPlayerManager().assignTask(player);
        }
        
        // Load player data
        plugin.getPlayerManager().getPlayer(player);
        
        // Setup scoreboard
        plugin.getScoreboardManager().updateScoreboard(player);
    }
}
