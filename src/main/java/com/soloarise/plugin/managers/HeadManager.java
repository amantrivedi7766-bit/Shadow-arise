package com.soloarise.plugin.managers;

import com.soloarise.plugin.SoloArisePlugin;
import com.soloarise.plugin.models.CapturedSoul;
import com.soloarise.plugin.models.PlayerSoulData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HeadManager {
    
    private final SoloArisePlugin plugin;
    private final Map<UUID, Inventory> openMenus = new HashMap<>();
    
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
        
        // Try to get custom head based on mob type
        String mobType = soul.getMobType().toLowerCase();
        
        // Common mob heads
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
            case "dragon":
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
        meta.setDisplayName(getRankColor(soul) + formatName(soul.getMobType()) + 
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
        } else if (soul.getCurrentHealth() <= 0) {
            lore.add("§cSoul is dead!");
            lore.add("§7Use diamonds to heal");
        } else {
            lore.add("§aClick to summon!");
        }
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        return head;
    }
    
    private String getRankColor(CapturedSoul soul) {
        return switch(soul.getRank()) {
            case NORMAL -> "§7";
            case WARRIOR -> "§a";
            case ELITE -> "§9";
            case BOSS -> "§c";
            case PLAYER -> "§d";
        };
    }
    
    private String formatName(String mobType) {
        String[] words = mobType.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
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
}
