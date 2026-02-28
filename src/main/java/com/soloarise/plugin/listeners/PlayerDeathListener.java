package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public PlayerDeathListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer != null) {
            // Allow soul capture on death
            plugin.getSoulManager().markForCapture(victim, killer);
        }
    }
}
