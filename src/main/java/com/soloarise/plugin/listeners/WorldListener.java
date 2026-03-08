package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public WorldListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // Handle world load
    }
    
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        // Handle world unload
    }
}
