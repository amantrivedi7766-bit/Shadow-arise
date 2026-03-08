package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public InventoryListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Handle inventory clicks
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Handle inventory close
    }
}
