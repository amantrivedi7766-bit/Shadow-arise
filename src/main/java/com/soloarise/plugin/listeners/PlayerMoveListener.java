package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public PlayerMoveListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().distanceSquared(event.getTo()) > 0) {
            double distance = event.getFrom().distance(event.getTo());
            plugin.getPlayerManager().getPlayer(event.getPlayer()).addDistanceTraveled((int) distance);
        }
    }
}
