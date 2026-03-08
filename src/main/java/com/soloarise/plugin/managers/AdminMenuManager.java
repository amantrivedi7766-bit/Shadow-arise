package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;  // Add this import
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class AdminMenuManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, Inventory> openMenus = new HashMap<>();
    
    public AdminMenuManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void openAdminMenu(Player admin) {
        Inventory menu = Bukkit.createInventory(null, 54, "§4§l✦ SOLOARISE ADMIN ✦");
        
        // Player Management Section (Row 1)
        menu.setItem(0, createMenuItem(Material.PLAYER_HEAD, "§ePlayer List", 
            "§7View all players with souls",
            "§7Click to select player"));
        
        menu.setItem(1, createMenuItem(Material.SOUL_SAND, "§5Give Soul", 
            "§7Give a soul to a player",
            "§7Click to select player and soul"));
        
        menu.setItem(2, createMenuItem(Material.SOUL_CAMPFIRE, "§cRemove Soul", 
            "§7Remove a soul from player",
            "§7Click to select player"));
        
        menu.setItem(3, createMenuItem(Material.NETHER_STAR, "§aHeal All Souls", 
            "§7Heal all souls of all players",
            "§7Click to execute"));
        
        menu.setItem(4, createMenuItem(Material.BARRIER, "§c§lClear All Souls", 
            "§7WARNING: Clears ALL souls",
            "§7from ALL players",
            "§cThis cannot be undone!"));
        
        // Separator
        menu.setItem(8, createMenuItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ", " "));
        
        // Server Control Section (Row 2)
        menu.setItem(9, createMenuItem(Material.REDSTONE, "§cReload Config", 
            "§7Reload plugin configuration",
            "§7Click to reload"));
        
        menu.setItem(10, createMenuItem(Material.BOOK, "§bServer Stats", 
            "§7View server statistics",
            "§7Total players, souls, etc."));
        
        menu.setItem(11, createMenuItem(Material.CLOCK, "§6Toggle Features", 
            "§7Enable/disable features",
            "§7Click to configure"));
        
        menu.setItem(12, createMenuItem(Material.DIAMOND_SWORD, "§6Toggle Soul PvP", 
            "§7Enable/disable soul PvP",
            "§7Currently: §aEnabled"));
        
        menu.setItem(13, createMenuItem(Material.ENDER_PEARL, "§dTeleport to Soul", 
            "§7Teleport to a summoned soul",
            "§7Click to select"));
        
        // Separator
        menu.setItem(17, createMenuItem(Material.BLACK_STAINED_GLASS_PANE, "§8 ", " "));
        
        // Player Heads (Row 3-5) - Show online players
        int slot = 18;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(player);
            meta.setDisplayName("§e" + player.getName());
            
            List<String> lore = new ArrayList<>();
            var soulData = plugin.getSoulManager().getPlayerSoulData(player);
            int soulCount = soulData != null ? soulData.getSouls().size() : 0;
            int activeCount = soulData != null ? soulData.getActiveSummonCount() : 0;
            
            lore.add("§7Souls: §f" + soulCount);
            lore.add("§7Active: §f" + activeCount);
            lore.add("");
            lore.add("§7Click to manage player");
            
            meta.setLore(lore);
            head.setItemMeta(meta);
            
            menu.setItem(slot++, head);
        }
        
        // Control Panel (Bottom Row)
        for (int i = 45; i < 54; i++) {
            if (i == 49) {
                menu.setItem(i, createMenuItem(Material.BARRIER, "§cClose", "§7Click to close"));
            } else if (i == 48) {
                menu.setItem(i, createMenuItem(Material.EMERALD, "§aSave All", "§7Save all player data"));
            } else if (i == 50) {
                menu.setItem(i, createMenuItem(Material.REDSTONE_BLOCK, "§cEmergency Stop", 
                    "§7Recall all souls",
                    "§7and disable temporarily"));
            } else {
                menu.setItem(i, createMenuItem(Material.GRAY_STAINED_GLASS_PANE, "§8 ", " "));
            }
        }
        
        admin.openInventory(menu);
        openMenus.put(admin.getUniqueId(), menu);
    }
    
    public void openPlayerManageMenu(Player admin, Player target) {
        Inventory menu = Bukkit.createInventory(null, 27, "§5§lManage: §d" + target.getName());
        
        var soulData = plugin.getSoulManager().getPlayerSoulData(target);
        
        if (soulData != null) {
            int slot = 0;
            for (CapturedSoul soul : soulData.getSouls()) {
                if (slot >= 27) break;
                
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setDisplayName(soul.getColoredName());
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Rank: " + soul.getRank().getDisplayName());
                lore.add("§7Health: §c" + (int)soul.getCurrentHealth() + "/" + (int)soul.getMaxHealth());
                lore.add("§7Status: " + (soul.isSummoned() ? "§aSummoned" : "§7Available"));
                lore.add("§7Ability: §f" + soul.getAbility().getName());
                lore.add("");
                lore.add("§eLeft-click: Remove soul");
                lore.add("§eRight-click: Heal soul");
                lore.add("§eShift-click: Teleport to soul");
                
                meta.setLore(lore);
                head.setItemMeta(meta);
                
                menu.setItem(slot++, head);
            }
        }
        
        // Control buttons
        menu.setItem(22, createMenuItem(Material.NETHER_STAR, "§aHeal All", 
            "§7Heal all of " + target.getName() + "'s souls"));
        
        menu.setItem(23, createMenuItem(Material.BARRIER, "§cRemove All", 
            "§7Remove all of " + target.getName() + "'s souls",
            "§cThis cannot be undone!"));
        
        menu.setItem(26, createMenuItem(Material.ARROW, "§7Back", "§7Return to main menu"));
        
        admin.openInventory(menu);
    }
    
    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
    
    public boolean isAdminMenu(Inventory inv) {
        // Safe way to check
        return false; // Will be handled in listener
    }
    
    public void closeMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }
}
