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
        
        // Load player data
        plugin.getSoulManager().getPlayerSouls().putIfAbsent(
            player.getUniqueId(), 
            new com.soloarise.plugin.models.PlayerSoulData(player.getUniqueId())
        );
        
        // Welcome message
        player.sendMessage("§5§l✦ SOLO ARISE ✦");
        player.sendMessage("§7Kill mobs and use §e/arise §7to capture their souls!");
        player.sendMessage("§7Crouch §e5 times §7to summon your souls!");
        
        // Setup main scoreboard
        plugin.getScoreboardManager().updateMainScoreboard(player);
    }
}
