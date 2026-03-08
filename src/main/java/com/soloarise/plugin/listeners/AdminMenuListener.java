package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class AdminMenuListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public AdminMenuListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        
        // Check if it's admin menu
        if (title.equals("§4§l✦ ADMIN PANEL ✦")) {
            event.setCancelled(true); // Prevent taking items
            
            if (event.getCurrentItem() == null) return;
            
            // Handle admin actions based on clicked slot
            int slot = event.getSlot();
            
            switch(slot) {
                case 10: // Reload
                    plugin.getConfigManager().reload();
                    player.sendMessage("§a✓ Config reloaded!");
                    break;
                    
                case 13: // Heal all
                    plugin.getSoulManager().healAllSouls();
                    player.sendMessage("§a✓ All souls healed!");
                    break;
                    
                case 16: // Clear all
                    plugin.getSoulManager().clearAllSouls();
                    player.sendMessage("§c✓ All souls cleared!");
                    break;
                    
                case 26: // Close
                    player.closeInventory();
                    break;
            }
        }
        
        // Check if it's soul selection menu - also prevent taking items
        if (title.equals("§5§l✦ YOUR SOULS ✦")) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        
        // Prevent dragging in admin and soul menus
        if (title.equals("§4§l✦ ADMIN PANEL ✦") || 
            title.equals("§5§l✦ YOUR SOULS ✦")) {
            event.setCancelled(true);
        }
    }
}
