package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public PlayerChatListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        if (message.equalsIgnoreCase("arise") && plugin.getPlayerManager().getPlayer(player).hasArisePower()) {
            event.setCancelled(true);
            plugin.getSoulManager().attemptChatCapture(player);
        }
    }
}
