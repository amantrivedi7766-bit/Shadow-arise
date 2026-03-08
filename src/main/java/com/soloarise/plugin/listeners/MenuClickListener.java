package com.soloarise.plugin.listeners;  // Correct package - LISTENERS, not managers

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuClickListener implements Listener {
    
    private final SoloArisePlugin plugin;
    
    public MenuClickListener(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        
        // Check if it's our soul menu
        if (title.equals("§5§l✦ YOUR SOULS ✦")) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null) return;
            
            ItemStack clicked = event.getCurrentItem();
            if (!clicked.hasItemMeta()) return;
            
            ItemMeta meta = clicked.getItemMeta();
            String displayName = meta.getDisplayName();
            
            // Extract soul name from display
            String soulName = displayName.replaceAll("§[0-9a-f]", "").replace(" [SUMMONED]", "").trim();
            
            // Find soul
            PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
            if (soulData == null) return;
            
            // Find soul by name (simplified - you might need better matching)
            CapturedSoul selectedSoul = null;
            for (CapturedSoul soul : soulData.getSouls()) {
                if (soul.getFormattedName().equalsIgnoreCase(soulName)) {
                    selectedSoul = soul;
                    break;
                }
            }
            
            if (selectedSoul == null) return;
            
            // Check if already summoned
            if (selectedSoul.isSummoned()) {
                player.sendMessage("§cThis soul is already summoned!");
                player.closeInventory();
                return;
            }
            
            // Check if dead
            if (selectedSoul.isDead()) {
                player.sendMessage("§cThis soul is dead! Use §e/soulheal " + 
                    selectedSoul.getFormattedName() + " §cto revive it.");
                player.closeInventory();
                return;
            }
            
            // Summon the soul
            boolean success = plugin.getSoulManager().summonSoul(player, selectedSoul);
            
            if (success) {
                player.closeInventory();
                player.sendMessage("§a✓ " + selectedSoul.getFormattedName() + " summoned!");
            }
        }
    }
}
