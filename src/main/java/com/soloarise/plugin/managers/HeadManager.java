package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import com.soloarise.plugin.models.SoulRank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HeadManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, Inventory> openMenus = new ConcurrentHashMap<>();
    
    public HeadManager(SoloArisePlugin plugin) {
        this.plugin = plugin;
    }
    
    public void openSoulSelectionMenu(Player player, PlayerSoulData soulData) {
        int size = ((soulData.getSouls().size() - 1) / 9 + 1) * 9;
        size = Math.min(54, Math.max(9, size)); // Between 9 and 54 slots
        
        Inventory menu = Bukkit.createInventory(null, size, "§5§l✦ YOUR SOULS ✦");
        
        int slot = 0;
        for (CapturedSoul soul : soulData.getSouls()) {
            if (slot >= size) break;
            
            ItemStack head = getMobHead(soul);
            menu.setItem(slot++, head);
        }
        
        player.openInventory(menu);
        openMenus.put(player.getUniqueId(), menu);
    }
    
    private ItemStack getMobHead(CapturedSoul soul) {
        ItemStack head;
        String mobType = soul.getMobType().toLowerCase();
        
        // Try to get custom head based on mob type
        switch(mobType) {
            case "zombie":
                head = new ItemStack(Material.ZOMBIE_HEAD);
                break;
            case "skeleton":
                head = new ItemStack(Material.SKELETON_SKULL);
                break;
            case "wither_skeleton":
                head = new ItemStack(Material.WITHER_SKELETON_SKULL);
                break;
            case "creeper":
                head = new ItemStack(Material.CREEPER_HEAD);
                break;
            case "piglin":
            case "piglin_brute":
            case "zombified_piglin":
                head = new ItemStack(Material.PIGLIN_HEAD);
                break;
            case "ender_dragon":
                head = new ItemStack(Material.DRAGON_HEAD);
                break;
            default:
                // Use player head with custom texture
                head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(soul.getMobType()));
                head.setItemMeta(meta);
                break;
        }
        
        // If still default, use zombie head
        if (head.getType() == Material.AIR) {
            head = new ItemStack(Material.ZOMBIE_HEAD);
        }
        
        ItemMeta meta = head.getItemMeta();
        String rankColor = getRankColor(soul.getRank());
        meta.setDisplayName(rankColor + soul.getFormattedName() + 
            (soul.isSummoned() ? " §c[SUMMONED]" : ""));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Rank: " + soul.getRank().getDisplayName());
        lore.add("§7Health: " + getHealthBar(soul.getCurrentHealth(), soul.getMaxHealth()));
        lore.add("§7" + (int)soul.getCurrentHealth() + "/" + (int)soul.getMaxHealth() + " HP");
        lore.add("");
        lore.add("§7Ability: §f" + soul.getAbility().getName());
        lore.add("§7" + soul.getAbility().getDescription());
        lore.add("");
        
        if (soul.isSummoned()) {
            lore.add("§cAlready summoned!");
            lore.add("§7Use §f/soulcome§7 to recall");
        } else if (soul.isDead()) {
            lore.add("§cSoul is dead!");
            lore.add("§7Use §e/soulheal§7 to revive");
        } else {
            lore.add("§aClick to summon!");
        }
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        return head;
    }
    
    private String getRankColor(SoulRank rank) {
        return switch(rank) {
            case NORMAL -> "§7";
            case WARRIOR -> "§a";
            case ELITE -> "§9";
            case BOSS -> "§c";
            case PLAYER -> "§d";
        };
    }
    
    private String getHealthBar(double current, double max) {
        int bars = 10;
        int healthBars = (int) ((current / max) * bars);
        
        StringBuilder bar = new StringBuilder("§c");
        for (int i = 0; i < healthBars; i++) bar.append("❤");
        bar.append("§8");
        for (int i = healthBars; i < bars; i++) bar.append("❤");
        
        return bar.toString();
    }
    
    public boolean isSoulMenu(Inventory inv) {
        // Safe way to check title without direct method
        String title = null;
        try {
            // Try to get title via reflection or use a different approach
            // For now, we'll check via the view
            return false; // Placeholder - will be handled in listener
        } catch (Exception e) {
            return false;
        }
    }
    
    public void closeMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }
}
