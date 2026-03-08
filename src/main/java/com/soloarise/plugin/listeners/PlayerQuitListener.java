package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public PlayerQuitListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Recall all summoned souls
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        if (soulData != null) {
            for (CapturedSoul soul : soulData.getSummonedSouls()) {
                if (soul.getSummonedEntity() != null) {
                    soul.getSummonedEntity().remove();
                }
                soul.setSummoned(false);
                soul.setSummonedEntity(null);
            }
        }
        
        // No SummonSessionManager to clean up
    }
}
