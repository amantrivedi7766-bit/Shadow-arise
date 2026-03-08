package com.soloarise.plugin.listeners;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
        if (!title.equals("§5§l✦ YOUR SOULS ✦")) return;
        
        // Cancel the click
        event.setCancelled(true);
        
        // Check if clicked a slot
        if (event.getCurrentItem() == null) return;
        
        ItemStack clicked = event.getCurrentItem();
        if (!clicked.hasItemMeta()) return;
        if (!(clicked.getItemMeta() instanceof SkullMeta meta)) return;
        
        // Get soul name from item
        String displayName = meta.getDisplayName();
        String soulName = displayName.replaceAll("§[0-9a-f]", "").replace(" [SUMMONED]", "").trim();
        
        // Find soul
        PlayerSoulData soulData = plugin.getSoulManager().getPlayerSouls().get(player.getUniqueId());
        if (soulData == null) return;
        
        CapturedSoul soul = soulData.getSoulByFormattedName(soulName);
        if (soul == null) return;
        
        // Check if already summoned
        if (soul.isSummoned()) {
            player.sendMessage("§cThis soul is already summoned!");
            player.closeInventory();
            return;
        }
        
        // Check if dead
        if (soul.getCurrentHealth() <= 0) {
            player.sendMessage("§cThis soul is dead! Use diamonds to heal it.");
            player.closeInventory();
            return;
        }
        
        // Summon the soul
        boolean success = plugin.getSoulManager().summonSoul(player, soul);
        
        if (success) {
            player.closeInventory();
            player.sendMessage("§a✓ Soul summoned!");
        }
    }
}
